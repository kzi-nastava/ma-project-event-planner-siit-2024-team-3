package com.example.eveant.event.eventDetails;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.Event;
import com.example.eveant.event.agenda.Activity;
import com.example.eveant.event.agenda.ActivityCardAdapter;

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
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";

    // header views
    private ImageView ivPhoto;
    private TextView tvEventType, tvTitle, tvAddress, tvDate, tvTime, tvGuests, tvDescription;
    private Button btnDownloadPdf;

    // lists
    private androidx.recyclerview.widget.RecyclerView rvActivities, rvComments;
    private EditText etComment; private Button btnPost;

    // adapters
    private ActivityCardAdapter activityAdapter;
    private CommentAdapter commentAdapter;

    // OSM map
    private MapView mapView;
    private Marker marker;

    private int eventId = -1;

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

        // header refs
        ivPhoto       = v.findViewById(R.id.ivPhoto);
        tvEventType   = v.findViewById(R.id.tvEventType);
        tvTitle       = v.findViewById(R.id.tvTitle);
        tvAddress     = v.findViewById(R.id.tvAddress);
        tvDate        = v.findViewById(R.id.tvDate);
        tvTime        = v.findViewById(R.id.tvTime);
        tvGuests      = v.findViewById(R.id.tvGuests);
        tvDescription = v.findViewById(R.id.tvDescription);
        btnDownloadPdf= v.findViewById(R.id.btnDownloadPdf);

        // activities timeline (horizontal) using your existing adapter
        rvActivities = v.findViewById(R.id.rvActivities);
        rvActivities.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        activityAdapter = new ActivityCardAdapter(new ArrayList<>()); // read-only ctor from your adapter
        rvActivities.setAdapter(activityAdapter);

        // comments
        rvComments = v.findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(new ArrayList<>());
        rvComments.setAdapter(commentAdapter);
        etComment = v.findViewById(R.id.etComment);
        btnPost   = v.findViewById(R.id.btnPost);
        btnPost.setOnClickListener(x -> {
            String txt = String.valueOf(etComment.getText()).trim();
            if (TextUtils.isEmpty(txt)) { etComment.setError("Required"); return; }
            etComment.setError(null);
            commentAdapter.addFirst(new Comment("You", txt, "Just now"));
            etComment.setText("");
            Toast.makeText(getContext(), "Comment posted", Toast.LENGTH_SHORT).show();
        });

        // OSM map setup (free)
        mapView = v.findViewById(R.id.mapView);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        if (eventId > 0) {
            fetchEvent();
            fetchActivities();
        }
    }

    private void fetchEvent() {
        RetrofitClient.eventService.getEventById(eventId).enqueue(new Callback<Event>() {
            @Override public void onResponse(Call<Event> c, Response<Event> r) {
                if (!r.isSuccessful() || r.body()==null) { toast("Load event failed: "+r.code()); return; }
                bindEvent(r.body());
            }
            @Override public void onFailure(Call<Event> c, Throwable t) { toast("Error: "+t.getMessage()); }
        });
    }

    private void bindEvent(Event e) {
        tvTitle.setText(nz(e.getName()));
        if (e.getEventType()!=null) tvEventType.setText(nz(e.getEventType().getName()));
        tvDescription.setText(nz(e.getDescription()));
        if (e.getMaxAttendance()!=null) tvGuests.setText(String.valueOf(e.getMaxAttendance()));

        String date = nz(e.getDate()), time = "";
        int t = Math.max(date.indexOf('T'), date.indexOf(' '));
        if (t>0) { time = "  •  " + date.substring(t+1); date = date.substring(0, t); }
        tvDate.setText(date); tvTime.setText(time);

        if (e.getAddress()!=null) {
            String displayAddr = join(", ",
                    e.getAddress().getStreet(),
                    e.getAddress().getHouseNumber(),
                    e.getAddress().getCity(),
                    e.getAddress().getCountry());
            tvAddress.setText(displayAddr);

            String queryAddr = buildAddressLine(e);
            if (!queryAddr.isEmpty()) geocodeWithNominatim(queryAddr);
        }
    }

    private void fetchActivities() {
        RetrofitClient.activityService.getByEventId(eventId).enqueue(new Callback<List<Activity>>() {
            @Override public void onResponse(Call<List<Activity>> c, Response<List<Activity>> r) {
                if (!r.isSuccessful() || r.body()==null) { toast("Load agenda failed: "+r.code()); return; }
                rvActivities.setAdapter(new ActivityCardAdapter(r.body())); // read-only
            }
            @Override public void onFailure(Call<List<Activity>> c, Throwable t) { toast("Error: "+t.getMessage()); }
        });
    }

    /* ---------- FREE GEOCODING (OSM Nominatim) ---------- */

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
                // Nominatim usage policy requires a valid User-Agent (your package is fine)
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
                    if (arr.length() > 0) {
                        JSONObject obj = arr.getJSONObject(0);
                        double lat = Double.parseDouble(obj.getString("lat"));
                        double lon = Double.parseDouble(obj.getString("lon"));
                        requireActivity().runOnUiThread(() -> placeMarker(lat, lon));
                    }
                }
                conn.disconnect();
            } catch (Exception ignored) { /* network/parse issues; keep map without marker */ }
        });
    }

    private void placeMarker(double lat, double lon) {
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

    /* ---------- Utils ---------- */

    private void toast(String s){ Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show(); }
    private String nz(String s){ return s==null? "": s; }
    private String join(String sep, String... parts){
        StringBuilder sb=new StringBuilder();
        for (String p:parts) if (p!=null && !p.isEmpty()){ if (sb.length()>0) sb.append(sep); sb.append(p); }
        return sb.toString();
    }

    /* MapView (osmdroid) has no special lifecycle calls needed here */
}
