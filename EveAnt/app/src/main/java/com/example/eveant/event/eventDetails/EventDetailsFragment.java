package com.example.eveant.event.eventDetails;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.Event;
import com.example.eveant.event.agenda.Activity;
import com.example.eveant.event.invitations.Invitation;
import com.example.eveant.event.invitations.InviteRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";

    // header views (nullable-safe)
    private ImageView ivPhoto;
    private TextView tvEventType, tvTitle, tvAddress, tvDate, tvTime, tvGuests, tvDescription;
    private Button btnDownloadPdf, btnJoin;

    // lists
    private RecyclerView rvActivities, rvComments;
    private EditText etComment; private Button btnPost;

    // adapters
    private MiniActivityAdapter miniAdapter;
    private CommentAdapter commentAdapter; // your adapter

    // OSM map
    private MapView mapView;
    private Marker marker;

    private int eventId = -1;
    private Event currentEvent;
    private boolean hasJoinedFlag = false;

    private final List<Activity> currentActivities = new ArrayList<>();

    public static EventDetailsFragment newInstance(int eventId) {
        Bundle b = new Bundle();
        b.putInt(ARG_EVENT_ID, eventId);
        EventDetailsFragment f = new EventDetailsFragment();
        f.setArguments(b);
        return f;
    }

    @Override public void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        if (getArguments()!=null) eventId = getArguments().getInt(ARG_EVENT_ID, -1);
    }

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_event_details, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        // Force white background / black text feel without changing global night mode
        v.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        btnJoin = v.findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(v1 -> handleJoinClick());
        // header refs (any may be absent in XML -> keep null-safe usage)
        ivPhoto       = v.findViewById(R.id.ivPhoto);
        tvEventType   = v.findViewById(R.id.tvEventType);
        tvTitle       = v.findViewById(R.id.tvTitle);
        tvAddress     = v.findViewById(R.id.tvAddress);
        tvDate        = v.findViewById(R.id.tvDate);
        tvTime        = v.findViewById(R.id.tvTime);
        tvGuests      = v.findViewById(R.id.tvGuests);
        tvDescription = v.findViewById(R.id.tvDescription);
        btnDownloadPdf= v.findViewById(R.id.btnDownloadPdf);
        btnJoin       = v.findViewById(R.id.btnJoin);

        // activities (horizontal)
        rvActivities = v.findViewById(R.id.rvActivities);
        if (rvActivities != null) {
            rvActivities.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            miniAdapter = new MiniActivityAdapter();
            rvActivities.setAdapter(miniAdapter);
        }

        // comments
        rvComments = v.findViewById(R.id.rvComments);
        if (rvComments != null) {
            rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
            commentAdapter = new CommentAdapter(new ArrayList<>());
            rvComments.setAdapter(commentAdapter);
        }

        btnDownloadPdf.setOnClickListener(l -> exportEventPdf());

        etComment = v.findViewById(R.id.etComment);
        btnPost   = v.findViewById(R.id.btnPost);
        if (btnPost != null) {
            btnPost.setOnClickListener(x -> {
                String txt = etComment != null ? String.valueOf(etComment.getText()).trim() : "";
                if (TextUtils.isEmpty(txt)) { if (etComment!=null) etComment.setError("Required"); return; }
                if (etComment!=null) { etComment.setError(null); etComment.setText(""); }
                if (commentAdapter!=null) commentAdapter.addFirst(new Comment("You", txt, "Just now"));
                toast("Comment posted");
            });
        }

        // OSM map setup (if present)
        mapView = v.findViewById(R.id.mapView);
        if (mapView != null) {
            Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);
            mapView.getController().setZoom(15.0);
        }

        if (eventId > 0) {
            fetchEvent();
            fetchActivities();
        }
    }



    /* ------------------------ API: Event + Activities ------------------------ */

    private void fetchEvent() {
        RetrofitClient.eventService.getEventById(eventId).enqueue(new Callback<Event>() {
            @Override public void onResponse(Call<Event> c, Response<Event> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful() || r.body()==null) { toast("Load event failed: "+r.code()); return; }
                bindEvent(r.body());
            }
            @Override public void onFailure(Call<Event> c, Throwable t) { if (isAdded()) toast("Error: "+t.getMessage()); }
        });
    }

    private void fetchActivities() {
        RetrofitClient.activityService.getByEventId(eventId).enqueue(new Callback<List<Activity>>() {
            @Override public void onResponse(Call<List<Activity>> c, Response<List<Activity>> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful() || r.body()==null) { toast("Load agenda failed: "+r.code()); return; }
                if (miniAdapter == null) return;

                List<Activity> data = new ArrayList<>(r.body());
                currentActivities.clear();
                currentActivities.addAll(data);
                Collections.sort(data, Comparator.comparing(a -> safe(a.startTime)));

                List<Mini> minis = new ArrayList<>();
                for (Activity a : data) {
                    String st = extractTime(a.startTime);
                    String et = extractTime(a.endTime);
                    String win = (isDash(st) || isDash(et)) ? st : (st + " - " + et);

                    String where = "";
                    if (a.address != null) {
                        where = join(", ",
                                a.address.getStreet(),
                                a.address.getCity());
                    }
                    minis.add(new Mini(win, safe(a.name), where));
                }
                miniAdapter.submit(minis);
            }
            @Override public void onFailure(Call<List<Activity>> c, Throwable t) { if (isAdded()) toast("Error: "+t.getMessage()); }
        });
    }

    /* ------------------------ Bind Event Header ------------------------ */

    private void bindEvent(Event e) {
        currentEvent = e;
        if (tvTitle != null) tvTitle.setText(nz(e.getName()));
        if (tvEventType != null) tvEventType.setText(e.getEventType()!=null ? nz(e.getEventType().getName()) : "All");
        if (tvDescription != null) tvDescription.setText(nz(e.getDescription()));
        if (tvGuests != null) tvGuests.setText(e.getMaxAttendance()==null ? "—" : String.valueOf(e.getMaxAttendance()));

        String datePart = extractDate(e.getDate());
        String timePart = extractTime(e.getDate());
        if (tvDate != null) tvDate.setText(datePart);
        if (tvTime != null) tvTime.setText(timePart);

        if (tvAddress != null && e.getAddress()!=null) {
            String displayAddr = join(", ",
                    e.getAddress().getStreet(),
                    e.getAddress().getHouseNumber(),
                    e.getAddress().getCity(),
                    e.getAddress().getCountry());
            tvAddress.setText(displayAddr);

            String queryAddr = buildAddressLine(e);
            if (!queryAddr.isEmpty() && mapView != null) geocodeWithNominatim(queryAddr);
        }

        boolean isPublic = (e.getStatus() != null && e.getStatus() == com.example.eveant.event.EventStatus.PUBLIC);
        btnJoin.setVisibility(isPublic ? View.VISIBLE : View.GONE);

        if (isPublic) {
            checkIfUserHasJoined(); // updates the button text
        }
        bindHeaderPhoto(e);
    }
    private void checkIfUserHasJoined() {
        String email = com.example.eveant.user.security.AuthManager
                .getInstance(requireContext())
                .getEmail();

        RetrofitClient.invitationService.getInvitations(eventId).enqueue(new retrofit2.Callback<java.util.List<Invitation>>() {
            @Override public void onResponse(retrofit2.Call<java.util.List<Invitation>> c,
                                             retrofit2.Response<java.util.List<Invitation>> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null) {
                    hasJoinedFlag = false;
                    for (Invitation inv : r.body()) {
                        if (email.equalsIgnoreCase(inv.email)) { hasJoinedFlag = true; break; }
                    }
                    refreshJoinButton();
                } else if (r.code() == 404) {
                    // no invitations for event
                    hasJoinedFlag = false;
                    refreshJoinButton();
                } else {
                    // leave previous state, but set as not joined on error if you prefer
                    refreshJoinButton();
                }
            }
            @Override public void onFailure(retrofit2.Call<java.util.List<Invitation>> c, Throwable t) {
                if (!isAdded()) return;
                // network error → keep default false
                refreshJoinButton();
            }
        });
    }

    private void refreshJoinButton() {
        btnJoin.setText(hasJoinedFlag ? "Leave event" : "Join event");
    }

    private void handleJoinClick() {
        if (isEventOver(currentEvent != null ? currentEvent.getDate() : null)) {
            Toast.makeText(getContext(), "This event is over.", Toast.LENGTH_SHORT).show();
            return;
        }
        toggleJoin();
    }

    private void toggleJoin() {
        if (hasJoinedFlag) leaveEvent();
        else joinEvent();
    }

    private void joinEvent() {
        btnJoin.setEnabled(false);
        String email = com.example.eveant.user.security.AuthManager
                .getInstance(requireContext())
                .getEmail();

        RetrofitClient.invitationService
                .sendInvitation(eventId, new InviteRequest(email, "", eventId))
                .enqueue(new retrofit2.Callback<Void>() {
                    @Override public void onResponse(retrofit2.Call<Void> c, retrofit2.Response<Void> r) {
                        if (!isAdded()) return;
                        btnJoin.setEnabled(true);
                        if (r.isSuccessful()) {
                            hasJoinedFlag = true;
                            refreshJoinButton();
                            Toast.makeText(getContext(), "Joined", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Join failed: " + r.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(retrofit2.Call<Void> c, Throwable t) {
                        if (!isAdded()) return;
                        btnJoin.setEnabled(true);
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void leaveEvent() {
        btnJoin.setEnabled(false);
        String email = com.example.eveant.user.security.AuthManager
                .getInstance(requireContext())
                .getEmail();

        RetrofitClient.invitationService
                .declineInvitation(eventId, java.net.URLEncoder.encode(email))
                .enqueue(new retrofit2.Callback<Void>() {
                    @Override public void onResponse(retrofit2.Call<Void> c, retrofit2.Response<Void> r) {
                        if (!isAdded()) return;
                        btnJoin.setEnabled(true);
                        if (r.isSuccessful()) {
                            hasJoinedFlag = false;
                            refreshJoinButton();
                            Toast.makeText(getContext(), "Left event", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Leave failed: " + r.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(retrofit2.Call<Void> c, Throwable t) {
                        if (!isAdded()) return;
                        btnJoin.setEnabled(true);
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Same semantics as your Angular guard:
    private boolean isEventOver(@Nullable String iso) {
        if (iso == null || iso.isEmpty()) return false;
        try {
            int t = Math.max(iso.indexOf('T'), iso.indexOf(' '));
            String date = (t > 0) ? iso.substring(0, t) : iso;
            String time = (t > 0 && iso.length() >= t + 5) ? iso.substring(t + 1, t + 6) : "23:59";
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US);
            java.util.Date eventDt = in.parse(date + " " + time);
            return eventDt != null && eventDt.before(new java.util.Date());
        } catch (Exception ignored) { return false; }
    }


    private void bindHeaderPhoto(Event e) {
        if (ivPhoto == null) return;
        String cover = (e.getPhotos() != null && !e.getPhotos().isEmpty()) ? e.getPhotos().get(0) : null;

        if (cover != null && (cover.startsWith("http://") || cover.startsWith("https://"))) {
            Glide.with(ivPhoto).load(cover)
                    .placeholder(R.drawable.rounded_corners_image_blue)
                    .into(ivPhoto);
            return;
        }
        // Base64 path
        if (cover != null) {
            try {
                if (cover.startsWith("data:image")) {
                    int comma = cover.indexOf(',');
                    if (comma > 0) cover = cover.substring(comma + 1);
                }
                if (cover.length() > 64) { // heuristic to avoid tiny strings
                    byte[] data = android.util.Base64.decode(cover, android.util.Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (bmp != null) {
                        ivPhoto.setImageBitmap(bmp);
                        return;
                    }
                }
            } catch (Exception ignore) { }
        }
        ivPhoto.setImageResource(R.drawable.rounded_corners_image_blue);
    }

    // ====== PDF EXPORT ======

    private void exportEventPdf() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to grab the image shown in the header, if any
        Bitmap cover = null;
        try {
            if (ivPhoto.getDrawable() instanceof android.graphics.drawable.BitmapDrawable) {
                cover = ((android.graphics.drawable.BitmapDrawable) ivPhoto.getDrawable()).getBitmap();
            }
        } catch (Exception ignored) {}

        if (cover == null) {
            // Fallback to Base64 (if your backend returns base64 strings in photos)
            String b64 = firstBase64(currentEvent.getPhotos());
            if (b64 != null) cover = decodeBase64Bitmap(b64);
        }

        byte[] bytes = buildEventPdfBytes(currentEvent, cover, currentActivities);
        if (bytes == null || bytes.length == 0) {
            Toast.makeText(getContext(), "Failed to build PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = safeFileName((currentEvent.getName() == null || currentEvent.getName().isEmpty())
                ? "event" : currentEvent.getName()) + ".pdf";

        try {
            String path = savePdfToDownloads(bytes, filename);
            Toast.makeText(getContext(), "Saved: " + path, Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getContext(), "Save failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private String firstBase64(@Nullable List<String> photos) {
        if (photos == null || photos.isEmpty()) return null;
        String p = photos.get(0);
        if (p == null) return null;
        // strip "data:image/*;base64," prefix if present
        if (p.startsWith("data:")) {
            int comma = p.indexOf(',');
            if (comma > 0) p = p.substring(comma + 1);
        }
        // Heuristic: only treat as base64 if it is reasonably long and not a URL
        if (p.startsWith("http://") || p.startsWith("https://")) return null;
        return p.length() > 60 ? p : null;
    }

    @Nullable
    private Bitmap decodeBase64Bitmap(String base64) {
        try {
            byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] buildEventPdfBytes(Event e, @Nullable Bitmap coverBmp, List<Activity> agenda) {
        final int pageWidth  = 612; // 8.5" * 72
        final int pageHeight = 792; // 11"  * 72
        final int margin = 36;      // 0.5"
        final int contentWidth = pageWidth - margin * 2;

        android.graphics.pdf.PdfDocument pdf = new android.graphics.pdf.PdfDocument();
        android.graphics.pdf.PdfDocument.PageInfo info =
                new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        android.graphics.pdf.PdfDocument.Page page = pdf.startPage(info);
        android.graphics.Canvas canvas = page.getCanvas();

        android.graphics.Paint titlePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(android.graphics.Color.BLACK);
        titlePaint.setTextSize(18f);
        titlePaint.setFakeBoldText(true);

        android.graphics.Paint sectionPaint = new android.graphics.Paint(titlePaint);
        sectionPaint.setTextSize(14f);

        android.graphics.Paint labelPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(android.graphics.Color.rgb(80,80,80));
        labelPaint.setTextSize(11f);

        android.graphics.Paint valuePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(android.graphics.Color.BLACK);
        valuePaint.setTextSize(12f);

        android.graphics.Paint monoPaint = new android.graphics.Paint(valuePaint);
        monoPaint.setTypeface(android.graphics.Typeface.MONOSPACE);

        android.graphics.Paint linePaint = new android.graphics.Paint();
        linePaint.setColor(android.graphics.Color.LTGRAY);
        linePaint.setStrokeWidth(1f);

        int x = margin;
        int y = margin;

        // Title
        y += drawMultilineText(canvas, e.getName() == null ? "Event" : e.getName(), x, y, contentWidth, titlePaint) + dp(6);

        // Cover (scaled)
        if (coverBmp != null && coverBmp.getWidth() > 0 && coverBmp.getHeight() > 0) {
            int w = contentWidth;
            int h = (int) (w * (coverBmp.getHeight() / (float) coverBmp.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(coverBmp, w, Math.max(1, h), true);
            canvas.drawBitmap(scaled, x, y, null);
            y += h + dp(10);
        }

        // Separator
        canvas.drawLine(x, y, x + contentWidth, y, linePaint); y += dp(12);

        // Quick facts
        String type = (e.getEventType()!=null && e.getEventType().getName()!=null) ? e.getEventType().getName() : "—";
        String date = "—", time = "—";
        if (e.getDate()!=null && !e.getDate().isEmpty()) {
            int tPos = Math.max(e.getDate().indexOf('T'), e.getDate().indexOf(' '));
            if (tPos > 0) {
                date = e.getDate().substring(0, tPos);
                time = e.getDate().substring(tPos + 1);
                if (time.length() >= 5) time = time.substring(0, 5);
            } else date = e.getDate();
        }
        String guests = (e.getMaxAttendance()==null) ? "—" : String.valueOf(e.getMaxAttendance());
        String status = (e.getStatus()==null) ? "—" : e.getStatus().name();
        String address = "";
        if (e.getAddress()!=null) {
            address = join(", ",
                    nz(e.getAddress().getStreet()),
                    nz(e.getAddress().getHouseNumber()),
                    nz(e.getAddress().getCity()),
                    nz(e.getAddress().getCountry()));
        }

        y += drawLabelValue(canvas, "Type", type, x, y, contentWidth, labelPaint, valuePaint);
        y += drawLabelValue(canvas, "Date", date, x, y, contentWidth, labelPaint, valuePaint);
        y += drawLabelValue(canvas, "Time", time, x, y, contentWidth, labelPaint, valuePaint);
        y += drawLabelValue(canvas, "Guests", guests, x, y, contentWidth, labelPaint, valuePaint);
        y += drawLabelValue(canvas, "Status", status, x, y, contentWidth, labelPaint, valuePaint);
        if (!address.isEmpty()) {
            y += drawLabelValue(canvas, "Address", address, x, y, contentWidth, labelPaint, valuePaint);
        }

        // Separator
        y += dp(8); canvas.drawLine(x, y, x + contentWidth, y, linePaint); y += dp(12);

        // Description
        if (e.getDescription()!=null && !e.getDescription().trim().isEmpty()) {
            y += drawMultilineText(canvas, "Description", x, y, contentWidth, sectionPaint) + dp(6);
            y += drawMultilineText(canvas, e.getDescription().trim(), x, y, contentWidth, valuePaint) + dp(8);
        }

        // Agenda (activities) – HH:mm – Title
        if (agenda != null && !agenda.isEmpty()) {
            y += drawMultilineText(canvas, "Agenda", x, y, contentWidth, sectionPaint) + dp(6);

            // Sort by start time if needed (optional)
            List<Activity> sorted = new ArrayList<>(agenda);
            try {
                sorted.sort((a, b) -> {
                    long sa = parseMinutes(a.startTime);
                    long sb = parseMinutes(b.startTime);
                    return Long.compare(sa, sb);
                });
            } catch (Exception ignored) {}

            for (Activity a : sorted) {
                String hhmm = formatHHmm(a.startTime);
                String line = String.format(Locale.getDefault(), "%s  –  %s", hhmm, nz(a.name));
                y += drawMultilineText(canvas, line, x, y, contentWidth, monoPaint) + dp(2);
                // stop if close to bottom margin
                if (y > pageHeight - margin - dp(24)) break;
            }
        }

        pdf.finishPage(page);

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try { pdf.writeTo(bos); } catch (Exception ignore) {}
        finally { pdf.close(); }
        return bos.toByteArray();
    }

    private int drawLabelValue(android.graphics.Canvas c, String label, String value,
                                 int x, int y, int width,
                                 android.graphics.Paint labelPaint, android.graphics.Paint valuePaint) {
        int line = (int)(valuePaint.getTextSize() + dp(6));
        c.drawText(label + ":", x, y + valuePaint.getTextSize(), labelPaint);
        int labelWidth = (int) labelPaint.measureText(label + ":  ");
        int used = drawMultilineText(c, value, x + labelWidth + dp(4), y, width - labelWidth - dp(4), valuePaint);
        return Math.max(line, used + dp(6));
    }

    private int drawMultilineText(android.graphics.Canvas c, String text, int x, int y, int width, android.graphics.Paint p) {
        if (text == null || text.isEmpty()) return 0;
        int start = 0, totalH = 0;
        float lineH = (float) Math.ceil(p.getTextSize() * 1.4f);
        final int len = text.length();
        while (start < len) {
            int count = p.breakText(text, start, len, true, width, null);
            c.drawText(text, start, start + count, x, y + p.getTextSize() + totalH, p);
            start += count;
            totalH += lineH;
        }
        return (int) totalH;
    }

    private String savePdfToDownloads(byte[] data, String displayName) throws Exception {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            android.content.ContentResolver cr = requireContext().getContentResolver();
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.Downloads.DISPLAY_NAME, displayName);
            values.put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(android.provider.MediaStore.Downloads.IS_PENDING, 1);

            android.net.Uri uri = cr.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new IllegalStateException("Insert failed");

            try (java.io.OutputStream os = cr.openOutputStream(uri)) {
                if (os == null) throw new IllegalStateException("Cannot open stream");
                os.write(data);
            }
            values.clear();
            values.put(android.provider.MediaStore.Downloads.IS_PENDING, 0);
            cr.update(uri, values, null, null);
            return "Downloads/" + displayName;
        } else {
            java.io.File dir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) dir.mkdirs();
            java.io.File out = new java.io.File(dir, displayName);
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(out)) {
                fos.write(data);
            }
            requireContext().sendBroadcast(new android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    android.net.Uri.fromFile(out)));
            return out.getAbsolutePath();
        }
    }


    private String formatHHmm(@Nullable String iso) {
        if (iso == null) return "—";
        try {
            // Try the common formats
            int t = Math.max(iso.indexOf('T'), iso.indexOf(' '));
            if (t > 0 && iso.length() >= t + 5) {
                return iso.substring(t + 1, t + 6); // HH:mm
            }
            return iso;
        } catch (Exception e) {
            return "—";
        }
    }

    private long parseMinutes(@Nullable String iso) {
        if (iso == null) return Long.MAX_VALUE;
        try {
            int t = Math.max(iso.indexOf('T'), iso.indexOf(' '));
            if (t > 0 && iso.length() >= t + 5) {
                int h = Integer.parseInt(iso.substring(t + 1, t + 3));
                int m = Integer.parseInt(iso.substring(t + 4, t + 6));
                return h * 60L + m;
            }
        } catch (Exception ignored) {}
        return Long.MAX_VALUE;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static String safeFileName(String s){
        return s.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }


    /* ------------------------ Geocoding (OSM Nominatim) ------------------------ */

    private String buildAddressLine(Event e) {
        if (e.getAddress() == null) return "";
        String street  = nz(e.getAddress().getStreet());
        String house   = nz(e.getAddress().getHouseNumber());
        String city    = nz(e.getAddress().getCity());
        String country = nz(e.getAddress().getCountry());
        String line = join(" ", street, house);
        return join(", ", line.isEmpty()? null: line, city, country);
    }

    private void geocodeWithNominatim(String fullAddress) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String base = "https://nominatim.openstreetmap.org/search";
                String q = URLEncoder.encode(fullAddress, StandardCharsets.UTF_8.name());
                String urlStr = base + "?format=json&limit=1&q=" + q;

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", requireContext().getPackageName());
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder(); String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();

                    JSONArray arr = new JSONArray(sb.toString());
                    if (arr.length() > 0 && isAdded()) {
                        JSONObject obj = arr.getJSONObject(0);
                        double lat = Double.parseDouble(obj.optString("lat","0"));
                        double lon = Double.parseDouble(obj.optString("lon","0"));
                        if (!isAdded()) { conn.disconnect(); return; }
                        requireActivity().runOnUiThread(() -> placeMarker(lat, lon));
                    }
                }
                conn.disconnect();
            } catch (Exception ignore) { }
        });
    }

    private void placeMarker(double lat, double lon) {
        if (mapView == null) return;
        GeoPoint point = new GeoPoint(lat, lon);
        mapView.getController().setCenter(point);

        if (marker == null) {
            marker = new Marker(mapView);
            marker.setTitle("Exact location");
            mapView.getOverlays().add(marker);
        }
        marker.setPosition(point);
        mapView.invalidate();
    }

    /* ------------------------ Utils ------------------------ */

    private void toast(String s){ if (isAdded()) Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show(); }
    private String nz(String s){ return s==null? "": s; }
    private String safe(String s){ return s==null? "": s; }
    private boolean isDash(String s){ return s==null || s.isEmpty() || "—".equals(s); }

    private String join(String sep, String... parts){
        StringBuilder sb=new StringBuilder();
        for (String p:parts) if (p!=null && !p.trim().isEmpty()){ if (sb.length()>0) sb.append(sep); sb.append(p.trim()); }
        return sb.toString();
    }

    private static final Pattern DATE_RE = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern TIME_RE = Pattern.compile("(\\d{2}:\\d{2})");

    private String extractDate(String s) {
        if (TextUtils.isEmpty(s)) return "—";
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                return odt.toLocalDate().toString();
            } catch (Exception ignored) {
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                    return ldt.toLocalDate().toString();
                } catch (Exception ignored2) { }
            }
        }
        Matcher m = DATE_RE.matcher(s);
        return m.find() ? m.group(1) : "—";
    }

    private String extractTime(String s) {
        if (TextUtils.isEmpty(s)) return "—";
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                return odt.toLocalTime().toString().substring(0,5);
            } catch (Exception ignored) {
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                    return ldt.toLocalTime().toString().substring(0,5);
                } catch (Exception ignored2) { }
            }
        }
        Matcher m = TIME_RE.matcher(s);
        return m.find() ? m.group(1) : "—";
    }

    /* ------------------------ Mini agenda (HH:mm) adapter ------------------------ */

    private static class Mini {
        final String time;
        final String title;
        final String where;
        Mini(String t, String ti, String w) { time = t; title = ti; where = w; }
    }

    private static class MiniVH extends RecyclerView.ViewHolder {
        TextView tvTime, tvTitle, tvWhere;
        MiniVH(@NonNull View v) {
            super(v);
            tvTime  = v.findViewById(R.id.tvTime);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvWhere = v.findViewById(R.id.tvAddress);
        }
    }

    private class MiniActivityAdapter extends RecyclerView.Adapter<MiniVH> {
        private final List<Mini> items = new ArrayList<>();
        void submit(List<Mini> data) { items.clear(); if (data!=null) items.addAll(data); notifyDataSetChanged(); }

        @NonNull @Override public MiniVH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_activity_card, p, false);
            return new MiniVH(v);
        }
        @Override public void onBindViewHolder(@NonNull MiniVH h, int pos) {
            Mini m = items.get(pos);
            h.tvTime.setText(m.time);
            h.tvTitle.setText(m.title);
            h.tvWhere.setText(TextUtils.isEmpty(m.where) ? "—" : m.where);
        }
        @Override public int getItemCount() { return items.size(); }
    }
}
