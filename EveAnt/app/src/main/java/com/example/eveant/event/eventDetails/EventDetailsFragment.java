package com.example.eveant.event.eventDetails;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.eveant.comment.CommentService;
import com.example.eveant.comment.Comment;
import com.example.eveant.event.Event;
import com.example.eveant.event.agenda.Activity;
import com.example.eveant.event.invitations.Invitation;
import com.example.eveant.event.invitations.InviteRequest;
import com.example.eveant.user.security.AuthManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventDetailsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";

    // header views
    private ImageView ivPhoto;
    private TextView tvEventType, tvTitle, tvAddress, tvDate, tvTime, tvGuests, tvDescription;
    private Button btnDownloadPdf, btnJoin;

    // comments views
    private RecyclerView rvActivities, rvComments;
    private EditText etComment;
    private Button btnPost;
    private TextView emptyComments;
    private TextView commentConfirmation;
    private LinearLayout commentInputSection;

    // adapters
    private MiniActivityAdapter miniAdapter;
    private CommentAdapter commentAdapter;

    // OSM map
    private MapView mapView;
    private Marker marker;

    // services
    private CommentService commentService;
    private AuthManager authManager;

    private int eventId = -1;
    private Event currentEvent;
    private boolean hasJoinedFlag = false;
    private final List<Activity> currentActivities = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    public static EventDetailsFragment newInstance(int eventId) {
        Bundle b = new Bundle();
        b.putInt(ARG_EVENT_ID, eventId);
        EventDetailsFragment f = new EventDetailsFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        if (getArguments() != null) eventId = getArguments().getInt(ARG_EVENT_ID, -1);

        // Initialize services
        commentService = RetrofitClient.retrofit.create(CommentService.class);
        authManager = AuthManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inf.inflate(R.layout.fragment_event_details, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        // Force white background
        v.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        initializeViews(v);
        setupClickListeners();
        setupRecyclerViews();
        setupMap();

        if (eventId > 0) {
            fetchEvent();
            fetchActivities();
            loadComments();
        }
    }

    private void initializeViews(View v) {
        // Header views
        ivPhoto = v.findViewById(R.id.ivPhoto);
        tvEventType = v.findViewById(R.id.tvEventType);
        tvTitle = v.findViewById(R.id.tvTitle);
        tvAddress = v.findViewById(R.id.tvAddress);
        tvDate = v.findViewById(R.id.tvDate);
        tvTime = v.findViewById(R.id.tvTime);
        tvGuests = v.findViewById(R.id.tvGuests);
        tvDescription = v.findViewById(R.id.tvDescription);
        btnDownloadPdf = v.findViewById(R.id.btnDownloadPdf);
        btnJoin = v.findViewById(R.id.btnJoin);

        // Comments views
        rvComments = v.findViewById(R.id.rvComments);
        etComment = v.findViewById(R.id.etComment);
        btnPost = v.findViewById(R.id.btnPost);
        emptyComments = v.findViewById(R.id.empty_comments);
        commentConfirmation = v.findViewById(R.id.comment_confirmation);
        commentInputSection = v.findViewById(R.id.comment_input_section);

        // Activities
        rvActivities = v.findViewById(R.id.rvActivities);

        // Map
        mapView = v.findViewById(R.id.mapView);
    }

    private void setupClickListeners() {
        btnJoin.setOnClickListener(v -> handleJoinClick());
        btnDownloadPdf.setOnClickListener(v -> exportEventPdf());
        btnPost.setOnClickListener(v -> postComment());
    }

    private void setupRecyclerViews() {
        // Activities RecyclerView
        if (rvActivities != null) {
            rvActivities.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            miniAdapter = new MiniActivityAdapter();
            rvActivities.setAdapter(miniAdapter);
        }

        // Comments RecyclerView
        if (rvComments != null) {
            rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
            commentAdapter = new CommentAdapter(new ArrayList<>());
            rvComments.setAdapter(commentAdapter);
        }
    }

    private void setupMap() {
        if (mapView != null) {
            Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);
            mapView.getController().setZoom(15.0);
        }
    }

    /* ------------------------ API Calls ------------------------ */

    private void fetchEvent() {
        RetrofitClient.eventService.getEventById(eventId).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> c, Response<Event> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful() || r.body() == null) {
                    toast("Load event failed: " + r.code());
                    return;
                }
                bindEvent(r.body());
            }

            @Override
            public void onFailure(Call<Event> c, Throwable t) {
                if (isAdded()) toast("Error: " + t.getMessage());
            }
        });
    }

    private void fetchActivities() {
        RetrofitClient.activityService.getByEventId(eventId).enqueue(new Callback<List<Activity>>() {
            @Override
            public void onResponse(Call<List<Activity>> c, Response<List<Activity>> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful() || r.body() == null) {
                    toast("Load agenda failed: " + r.code());
                    return;
                }
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
                        where = join(", ", a.address.getStreet(), a.address.getCity());
                    }
                    minis.add(new Mini(win, safe(a.name), where));
                }
                miniAdapter.submit(minis);
            }

            @Override
            public void onFailure(Call<List<Activity>> c, Throwable t) {
                if (isAdded()) toast("Error: " + t.getMessage());
            }
        });
    }

    private void loadComments() {
        if (eventId <= 0) return;

        Call<List<Comment>> call = commentService.getApprovedComments(eventId);
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> comments = response.body();
                    updateCommentsUI(comments);
                } else {
                    toast("Failed to load comments");
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                toast("Error loading comments: " + t.getMessage());
            }
        });
    }

    /* ------------------------ Comment Functionality ------------------------ */

    private void postComment() {
        String commentText = etComment != null ? etComment.getText().toString().trim() : "";

        if (TextUtils.isEmpty(commentText)) {
            if (etComment != null) etComment.setError("Please enter a comment");
            return;
        }

        if (!authManager.isLoggedIn()) {
            toast("Please log in to comment");
            return;
        }

        String userEmail = authManager.getEmail();

        // Disable button during submission
        if (btnPost != null) btnPost.setEnabled(false);

        CommentService.CommentRequest commentRequest = new CommentService.CommentRequest(
                commentText, eventId, userEmail
        );

        Call<Comment> call = commentService.addComment(commentRequest);
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (btnPost != null) btnPost.setEnabled(true);

                if (response.isSuccessful()) {
                    // Clear input
                    if (etComment != null) etComment.setText("");

                    // Show confirmation message
                    showCommentConfirmation("Your comment has been sent to the admin for approval.");

                    // Refresh comments to show the new one
                    loadComments();
                } else {
                    toast("Failed to post comment");
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                if (btnPost != null) btnPost.setEnabled(true);
                toast("Network error: " + t.getMessage());
            }
        });
    }

    private void showCommentConfirmation(String message) {
        if (commentConfirmation != null) {
            commentConfirmation.setText(message);
            commentConfirmation.setVisibility(View.VISIBLE);

            handler.postDelayed(() -> {
                if (commentConfirmation != null) {
                    commentConfirmation.setVisibility(View.GONE);
                }
            }, 5000);
        }
    }

    private void updateCommentsUI(List<Comment> comments) {
        if (commentAdapter != null) {
            commentAdapter.setComments(comments);
        }

        // Update empty state
        if (emptyComments != null) {
            if (comments.isEmpty()) {
                emptyComments.setVisibility(View.VISIBLE);
                if (rvComments != null) rvComments.setVisibility(View.GONE);
            } else {
                emptyComments.setVisibility(View.GONE);
                if (rvComments != null) rvComments.setVisibility(View.VISIBLE);
            }
        }
    }

    private void checkCommentPermissions() {
        boolean isLoggedIn = authManager.isLoggedIn();

        if (commentInputSection != null) {
            commentInputSection.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);
        }

        if (etComment != null) {
            etComment.setEnabled(isLoggedIn);
            if (!isLoggedIn) {
                etComment.setHint("Please log in to comment");
            }
        }

        if (btnPost != null) {
            btnPost.setEnabled(isLoggedIn);
        }
    }

    /* ------------------------ Event Binding ------------------------ */

    private void bindEvent(Event e) {
        currentEvent = e;
        if (tvTitle != null) tvTitle.setText(nz(e.getName()));
        if (tvEventType != null) tvEventType.setText(e.getEventType() != null ? nz(e.getEventType().getName()) : "All");
        if (tvDescription != null) tvDescription.setText(nz(e.getDescription()));
        if (tvGuests != null) tvGuests.setText(e.getMaxAttendance() == null ? "—" : String.valueOf(e.getMaxAttendance()));

        String datePart = extractDate(e.getDate());
        String timePart = extractTime(e.getDate());
        if (tvDate != null) tvDate.setText(datePart);
        if (tvTime != null) tvTime.setText(timePart);

        if (tvAddress != null && e.getAddress() != null) {
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
            checkIfUserHasJoined();
        }

        checkCommentPermissions();
        bindHeaderPhoto(e);
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
                if (cover.length() > 64) {
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

    /* ------------------------ Join/Leave Event ------------------------ */

    private void checkIfUserHasJoined() {
        String email = authManager.getEmail();

        RetrofitClient.invitationEventService.getInvitations(eventId).enqueue(new Callback<List<Invitation>>() {
            @Override
            public void onResponse(Call<List<Invitation>> c, Response<List<Invitation>> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null) {
                    hasJoinedFlag = false;
                    for (Invitation inv : r.body()) {
                        if (email.equalsIgnoreCase(inv.email)) {
                            hasJoinedFlag = true;
                            break;
                        }
                    }
                    refreshJoinButton();
                } else if (r.code() == 404) {
                    hasJoinedFlag = false;
                    refreshJoinButton();
                } else {
                    refreshJoinButton();
                }
            }

            @Override
            public void onFailure(Call<List<Invitation>> c, Throwable t) {
                if (!isAdded()) return;
                refreshJoinButton();
            }
        });
    }

    private void refreshJoinButton() {
        btnJoin.setText(hasJoinedFlag ? "Leave event" : "Join event");
    }

    private void handleJoinClick() {
        if (isEventOver(currentEvent != null ? currentEvent.getDate() : null)) {
            toast("This event is over.");
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
        String email = authManager.getEmail();

        RetrofitClient.invitationEventService
                .sendInvitation(eventId, new InviteRequest(email, "", eventId))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> c, Response<Void> r) {
                        if (!isAdded()) return;
                        btnJoin.setEnabled(true);
                        if (r.isSuccessful()) {
                            hasJoinedFlag = true;
                            refreshJoinButton();
                            toast("Joined");
                        } else {
                            toast("Join failed: " + r.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> c, Throwable t) {
                        if (!isAdded()) return;
                        btnJoin.setEnabled(true);
                        toast("Network error: " + t.getMessage());
                    }
                });
    }

    private void leaveEvent() {
        btnJoin.setEnabled(false);
        String email = authManager.getEmail();

        RetrofitClient.invitationEventService
                .declineInvitation(eventId, java.net.URLEncoder.encode(email))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> c, Response<Void> r) {
                        if (!isAdded()) return;
                        btnJoin.setEnabled(true);
                        if (r.isSuccessful()) {
                            hasJoinedFlag = false;
                            refreshJoinButton();
                            toast("Left event");
                        } else {
                            toast("Leave failed: " + r.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> c, Throwable t) {
                        if (!isAdded()) return;
                        btnJoin.setEnabled(true);
                        toast("Network error: " + t.getMessage());
                    }
                });
    }

    private boolean isEventOver(@Nullable String iso) {
        if (iso == null || iso.isEmpty()) return false;
        try {
            int t = Math.max(iso.indexOf('T'), iso.indexOf(' '));
            String date = (t > 0) ? iso.substring(0, t) : iso;
            String time = (t > 0 && iso.length() >= t + 5) ? iso.substring(t + 1, t + 6) : "23:59";
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            Date eventDt = in.parse(date + " " + time);
            return eventDt != null && eventDt.before(new Date());
        } catch (Exception ignored) {
            return false;
        }
    }

    /* ------------------------ PDF Export ------------------------ */

    private void exportEventPdf() {
        if (currentEvent == null) {
            toast("Event not loaded yet");
            return;
        }

        Bitmap cover = null;
        try {
            if (ivPhoto.getDrawable() instanceof android.graphics.drawable.BitmapDrawable) {
                cover = ((android.graphics.drawable.BitmapDrawable) ivPhoto.getDrawable()).getBitmap();
            }
        } catch (Exception ignored) {}

        if (cover == null) {
            String b64 = firstBase64(currentEvent.getPhotos());
            if (b64 != null) cover = decodeBase64Bitmap(b64);
        }

        byte[] bytes = buildEventPdfBytes(currentEvent, cover, currentActivities);
        if (bytes == null || bytes.length == 0) {
            toast("Failed to build PDF");
            return;
        }

        String filename = safeFileName((currentEvent.getName() == null || currentEvent.getName().isEmpty())
                ? "event" : currentEvent.getName()) + ".pdf";

        try {
            String path = savePdfToDownloads(bytes, filename);
            toast("Saved: " + path);
        } catch (Exception ex) {
            toast("Save failed: " + ex.getMessage());
        }
    }

    private String firstBase64(@Nullable List<String> photos) {
        if (photos == null || photos.isEmpty()) return null;
        String p = photos.get(0);
        if (p == null) return null;
        if (p.startsWith("data:")) {
            int comma = p.indexOf(',');
            if (comma > 0) p = p.substring(comma + 1);
        }
        if (p.startsWith("http://") || p.startsWith("https://")) return null;
        return p.length() > 60 ? p : null;
    }

    @Nullable
    private Bitmap decodeBase64Bitmap(String base64) {
        try {
            byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] buildEventPdfBytes(Event e, @Nullable Bitmap coverBmp, List<Activity> agenda) {
        final int pageWidth = 612;
        final int pageHeight = 792;
        final int margin = 36;
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
        labelPaint.setColor(android.graphics.Color.rgb(80, 80, 80));
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
        canvas.drawLine(x, y, x + contentWidth, y, linePaint);
        y += dp(12);

        // Quick facts
        String type = (e.getEventType() != null && e.getEventType().getName() != null) ? e.getEventType().getName() : "—";
        String date = "—", time = "—";
        if (e.getDate() != null && !e.getDate().isEmpty()) {
            int tPos = Math.max(e.getDate().indexOf('T'), e.getDate().indexOf(' '));
            if (tPos > 0) {
                date = e.getDate().substring(0, tPos);
                time = e.getDate().substring(tPos + 1);
                if (time.length() >= 5) time = time.substring(0, 5);
            } else date = e.getDate();
        }
        String guests = (e.getMaxAttendance() == null) ? "—" : String.valueOf(e.getMaxAttendance());
        String status = (e.getStatus() == null) ? "—" : e.getStatus().name();
        String address = "";
        if (e.getAddress() != null) {
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
        y += dp(8);
        canvas.drawLine(x, y, x + contentWidth, y, linePaint);
        y += dp(12);

        // Description
        if (e.getDescription() != null && !e.getDescription().trim().isEmpty()) {
            y += drawMultilineText(canvas, "Description", x, y, contentWidth, sectionPaint) + dp(6);
            y += drawMultilineText(canvas, e.getDescription().trim(), x, y, contentWidth, valuePaint) + dp(8);
        }

        // Agenda (activities) – HH:mm – Title
        if (agenda != null && !agenda.isEmpty()) {
            y += drawMultilineText(canvas, "Agenda", x, y, contentWidth, sectionPaint) + dp(6);

            List<Activity> sorted = new ArrayList<>(agenda);
            try {
                sorted.sort((a, b) -> {
                    long sa = parseMinutes(a.startTime);
                    long sb = parseMinutes(b.startTime);
                    return Long.compare(sa, sb);
                });
            } catch (Exception ignored) {
            }

            for (Activity a : sorted) {
                String hhmm = formatHHmm(a.startTime);
                String line = String.format(Locale.getDefault(), "%s  –  %s", hhmm, nz(a.name));
                y += drawMultilineText(canvas, line, x, y, contentWidth, monoPaint) + dp(2);
                if (y > pageHeight - margin - dp(24)) break;
            }
        }

        pdf.finishPage(page);

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try {
            pdf.writeTo(bos);
        } catch (Exception ignore) {
        } finally {
            pdf.close();
        }
        return bos.toByteArray();
    }

    private int drawLabelValue(android.graphics.Canvas c, String label, String value,
                               int x, int y, int width,
                               android.graphics.Paint labelPaint, android.graphics.Paint valuePaint) {
        int line = (int) (valuePaint.getTextSize() + dp(6));
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
            int t = Math.max(iso.indexOf('T'), iso.indexOf(' '));
            if (t > 0 && iso.length() >= t + 5) {
                return iso.substring(t + 1, t + 6);
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
        } catch (Exception ignored) {
        }
        return Long.MAX_VALUE;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static String safeFileName(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    /* ------------------------ Geocoding ------------------------ */

    private String buildAddressLine(Event e) {
        if (e.getAddress() == null) return "";
        String street = nz(e.getAddress().getStreet());
        String house = nz(e.getAddress().getHouseNumber());
        String city = nz(e.getAddress().getCity());
        String country = nz(e.getAddress().getCountry());
        String line = join(" ", street, house);
        return join(", ", line.isEmpty() ? null : line, city, country);
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
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();

                    JSONArray arr = new JSONArray(sb.toString());
                    if (arr.length() > 0 && isAdded()) {
                        JSONObject obj = arr.getJSONObject(0);
                        double lat = Double.parseDouble(obj.optString("lat", "0"));
                        double lon = Double.parseDouble(obj.optString("lon", "0"));
                        if (!isAdded()) {
                            conn.disconnect();
                            return;
                        }
                        requireActivity().runOnUiThread(() -> placeMarker(lat, lon));
                    }
                }
                conn.disconnect();
            } catch (Exception ignore) {
            }
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

    private void toast(String s) {
        if (isAdded()) Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private boolean isDash(String s) {
        return s == null || s.isEmpty() || "—".equals(s);
    }

    private String join(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts)
            if (p != null && !p.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(sep);
                sb.append(p.trim());
            }
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
                } catch (Exception ignored2) {
                }
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
                return odt.toLocalTime().toString().substring(0, 5);
            } catch (Exception ignored) {
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                    return ldt.toLocalTime().toString().substring(0, 5);
                } catch (Exception ignored2) {
                }
            }
        }
        Matcher m = TIME_RE.matcher(s);
        return m.find() ? m.group(1) : "—";
    }

    /* ------------------------ Adapters ------------------------ */

    private static class Mini {
        final String time;
        final String title;
        final String where;

        Mini(String t, String ti, String w) {
            time = t;
            title = ti;
            where = w;
        }
    }

    private static class MiniVH extends RecyclerView.ViewHolder {
        TextView tvTime, tvTitle, tvWhere;

        MiniVH(@NonNull View v) {
            super(v);
            tvTime = v.findViewById(R.id.tvTime);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvWhere = v.findViewById(R.id.tvAddress);
        }
    }

    private class MiniActivityAdapter extends RecyclerView.Adapter<MiniVH> {
        private final List<Mini> items = new ArrayList<>();

        void submit(List<Mini> data) {
            items.clear();
            if (data != null) items.addAll(data);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MiniVH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
            View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_activity_card, p, false);
            return new MiniVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MiniVH h, int pos) {
            Mini m = items.get(pos);
            h.tvTime.setText(m.time);
            h.tvTitle.setText(m.title);
            h.tvWhere.setText(TextUtils.isEmpty(m.where) ? "—" : m.where);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}