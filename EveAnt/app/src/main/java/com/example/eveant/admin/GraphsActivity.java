// GraphsActivity.java
package com.example.eveant.admin;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.invitations.Invitation;
import com.example.eveant.reviews.Review;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GraphsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";        // legacy single-event
    public static final String EXTRA_EVENT_IDS = "event_ids";       // int[]
    public static final String EXTRA_EVENT_NAMES = "event_names";   // String[] aligned with ids
    public static final String EXTRA_TYPE = "type";
    public static final int TYPE_ATTENDANCE = 1;
    public static final int TYPE_REVIEWS = 2;

    private int eventId;
    private int type;
    private int[] allEventIds;
    private String[] allEventNames;

    private BarChartView chart;
    private Button btnExport;

    // what we last drew (for export)
    private String lastTitle = "";
    private String[] lastLabels = new String[0];
    private int[] lastValues = new int[0];
    private double[] pendingAvgsForPdf = null; // null unless reviews chart is loaded

    public static Intent intentFor(Context ctx, int eventId, int type) {
        Intent i = new Intent(ctx, GraphsActivity.class);
        i.putExtra(EXTRA_EVENT_ID, eventId);
        i.putExtra(EXTRA_TYPE, type);
        return i;
    }

    public static Intent intentForAll(Context ctx, int[] eventIds, String[] eventNames, int type) {
        Intent i = new Intent(ctx, GraphsActivity.class);
        i.putExtra(EXTRA_EVENT_ID, -1);
        i.putExtra(EXTRA_EVENT_IDS, eventIds);
        i.putExtra(EXTRA_EVENT_NAMES, eventNames);
        i.putExtra(EXTRA_TYPE, type);
        return i;
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        eventId = getIntent().getIntExtra(EXTRA_EVENT_ID, -1);
        type = getIntent().getIntExtra(EXTRA_TYPE, TYPE_ATTENDANCE);
        allEventIds = getIntent().getIntArrayExtra(EXTRA_EVENT_IDS);
        allEventNames = getIntent().getStringArrayExtra(EXTRA_EVENT_NAMES);

        chart = findViewById(R.id.barChart);
        chart.setLabelAngleDegrees(45f);

        Button btnRefresh = findViewById(R.id.btnRefresh);
        btnExport = findViewById(R.id.btnExportPdf);
        btnExport.setEnabled(false);

        btnRefresh.setOnClickListener(v -> loadData());
        btnExport.setOnClickListener(v -> exportPdf());

        loadData();
    }

    private void loadData() {
        btnExport.setEnabled(false);
        pendingAvgsForPdf = null;

        // ALL events mode
        if (allEventIds != null && allEventIds.length > 0) {
            if (type == TYPE_ATTENDANCE) {
                loadPerEventAttendance(allEventIds, allEventNames);
            } else {
                loadPerEventReviewsCount(allEventIds, allEventNames);
            }
            return;
        }

        // Single-event (legacy) mode
        if (type == TYPE_ATTENDANCE) {
            RetrofitClient.invitationEventService.getInvitations(eventId).enqueue(new Callback<List<Invitation>>() {
                @Override public void onResponse(Call<List<Invitation>> call, Response<List<Invitation>> resp) {
                    List<Invitation> list = (resp.isSuccessful() && resp.body() != null) ? resp.body() : Collections.emptyList();
                    String[] labels = new String[] {"Event " + eventId};
                    int[] values = new int[] { list.size() }; // total invitations
                    render("Attendance (Event " + eventId + ")", labels, values);
                }
                @Override public void onFailure(Call<List<Invitation>> call, Throwable t) {
                    render("Attendance (Event " + eventId + ")", new String[]{"Event " + eventId}, new int[]{0});
                }
            });
        } else {
            RetrofitClient.reviewService.getReviews(eventId).enqueue(new Callback<List<Review>>() {
                @Override public void onResponse(Call<List<Review>> call, Response<List<Review>> resp) {
                    List<Review> list = (resp.isSuccessful() && resp.body() != null) ? resp.body() : Collections.emptyList();
                    int count = list.size();
                    double sum = 0.0;
                    for (Review r : list) sum += r.getRating(); // assumes getRating()
                    double avg = count > 0 ? (sum / count) : 0.0;

                    String[] labels = new String[] {"Event " + eventId};
                    int[] values = new int[] { count }; // bars show counts
                    render("Reviews (Event " + eventId + ")", labels, values, new double[]{ avg });
                }
                @Override public void onFailure(Call<List<Review>> call, Throwable t) {
                    render("Reviews (Event " + eventId + ")", new String[]{"Event " + eventId}, new int[]{0}, new double[]{0.0});
                }
            });
        }
    }

    // === All events: per-event TOTAL invitations (matches adapter "Attendance: X") ===
    private void loadPerEventAttendance(int[] ids, String[] names) {
        final int n = ids.length;
        final int[] done = {0};
        final int[] values = new int[n];
        final String[] labels = new String[n];

        for (int i = 0; i < n; i++) {
            final int idx = i;
            labels[idx] = shortenName(names != null && idx < names.length ? names[idx] : ("#" + ids[idx]));
            RetrofitClient.invitationEventService.getInvitations(ids[idx]).enqueue(new Callback<List<Invitation>>() {
                @Override public void onResponse(Call<List<Invitation>> call, Response<List<Invitation>> resp) {
                    values[idx] = (resp.isSuccessful() && resp.body() != null) ? resp.body().size() : 0; // total invitations
                    if (++done[0] == n) {
                        render("Attendance (All Public Events)", labels, values);
                    }
                }
                @Override public void onFailure(Call<List<Invitation>> call, Throwable t) {
                    values[idx] = 0;
                    if (++done[0] == n) {
                        render("Attendance (All Public Events)", labels, values);
                    }
                }
            });
        }
    }

    // === All events: per-event reviews COUNT + overlay AVG text (matches adapter) ===
    private void loadPerEventReviewsCount(int[] ids, String[] names) {
        final int n = ids.length;
        final int[] done = {0};
        final int[] counts = new int[n];
        final double[] avgs = new double[n];
        final String[] labels = new String[n];

        for (int i = 0; i < n; i++) {
            final int idx = i;
            labels[idx] = shortenName(names != null && idx < names.length ? names[idx] : ("#" + ids[idx]));
            RetrofitClient.reviewService.getReviews(ids[idx]).enqueue(new Callback<List<Review>>() {
                @Override public void onResponse(Call<List<Review>> call, Response<List<Review>> resp) {
                    int c = 0; double sum = 0.0;
                    if (resp.isSuccessful() && resp.body() != null) {
                        List<Review> list = resp.body();
                        c = list.size();
                        for (Review r : list) sum += r.getRating(); // assumes getRating()
                    }
                    counts[idx] = c;
                    avgs[idx] = c > 0 ? (sum / c) : 0.0;

                    if (++done[0] == n) {
                        render("Reviews (All Public Events)", labels, counts, avgs);
                    }
                }
                @Override public void onFailure(Call<List<Review>> call, Throwable t) {
                    counts[idx] = 0;
                    avgs[idx] = 0.0;
                    if (++done[0] == n) {
                        render("Reviews (All Public Events)", labels, counts, avgs);
                    }
                }
            });
        }
    }

    private void render(String title, String[] labels, int[] values) {
        render(title, labels, values, null);
    }

    private void render(String title, String[] labels, int[] values, @Nullable double[] avgs) {
        chart.setData(title, labels, values); // your BarChartView
        lastTitle = title;
        lastLabels = labels.clone();
        lastValues = values.clone();
        pendingAvgsForPdf = avgs; // null unless reviews; used in export overlay
        btnExport.setEnabled(true);
        Toast.makeText(this, "Loaded " + labels.length + " bars", Toast.LENGTH_SHORT).show();
    }

    private String shortenName(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() <= 16) return s;
        return s.substring(0, 14) + "…";
    }

    // ===== Export to Downloads/EveAnt via MediaStore (Android 10+ path) =====
    private void exportPdf() {
        PdfDocument doc = null;
        try {
            doc = new PdfDocument();
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = doc.startPage(info);
            Canvas c = page.getCanvas();

            Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
            label.setTextSize(12); label.setColor(Color.DKGRAY);
            Paint axis = new Paint(Paint.ANTI_ALIAS_FLAG);
            axis.setColor(Color.BLACK); axis.setStrokeWidth(3);
            Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG);
            bar.setColor(Color.rgb(128,148,219));
            Paint titleP = new Paint(Paint.ANTI_ALIAS_FLAG);
            titleP.setTextSize(20); titleP.setColor(Color.BLACK); titleP.setFakeBoldText(true);

            int margin = 36;
            c.drawText(lastTitle == null ? "" : lastTitle, margin, margin + 10, titleP);
            drawBars(c, margin, 120, 500, 400, lastLabels, lastValues, pendingAvgsForPdf, label, axis, bar);

            doc.finishPage(page);

            String fileName = (type == TYPE_ATTENDANCE ? "attendance_per_event" : "reviews_per_event") + ".pdf";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/EveAnt");
                values.put(MediaStore.Downloads.IS_PENDING, 1);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new IllegalStateException("Couldn't create MediaStore entry");
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    if (os == null) throw new IllegalStateException("Couldn't open OutputStream");
                    doc.writeTo(os);
                }
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                getContentResolver().update(uri, values, null, null);

                Toast.makeText(this, "Saved to Downloads/EveAnt/" + fileName, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Export requires Android 10+ or legacy permission handling", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (doc != null) doc.close();
        }
    }

    private void drawBars(Canvas c, int left, int top, int width, int height,
                          String[] labels, int[] values, @Nullable double[] avgs,
                          Paint labelPaint, Paint axisPaint, Paint barPaint) {

        if (labels == null || values == null || labels.length == 0 || labels.length != values.length) return;

        int n = values.length;
        int max = 1;
        for (int v : values) max = Math.max(max, v);

        // axes
        c.drawLine((float) left, (float) top, (float) left, (float) (top + height), axisPaint);
        c.drawLine((float) left, (float) (top + height), (float) (left + width), (float) (top + height), axisPaint);
        int gap = 12;
        int barW = (width - gap * (n + 1)) / Math.max(1, n);
        barW = Math.max(6, barW);
        int x = left + gap;

        for (int i = 0; i < n; i++) {
            int barH = (int) Math.round((values[i] / (double) max) * (height - 30));
            int y = top + height - barH;
            c.drawRect(x, y, x + barW, top + height, barPaint);

            // bar value
            c.drawText(String.valueOf(values[i]), x + barW / 2f - 6, y - 6, labelPaint);

            // optional avg overlay (for reviews)
            if (avgs != null && i < avgs.length && avgs[i] > 0) {
                String avgText = "avg " + String.format(Locale.US, "%.1f", avgs[i]);
                c.drawText(avgText, x + barW / 2f - 18, y - 22, labelPaint);
            }

            // x label
            c.drawText(labels[i], x, top + height + 16, labelPaint);
            x += barW + gap;
        }
    }
}
