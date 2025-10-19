package com.example.eveant.event.eventDetails;

import android.view.View;

import com.example.eveant.R;

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
import java.util.concurrent.Executors;

class MapController {

    private MapView mapView;
    private Marker marker;
    private final View root;

    MapController(View root) {
        this.root = root;
        mapView = root.findViewById(R.id.mapView);
        if (mapView != null) {
            Configuration.getInstance().setUserAgentValue(root.getContext().getPackageName());
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);
            mapView.getController().setZoom(15.0);
        }
    }

    void geocodeAndPlace(String fullAddress) {
        if (mapView == null || fullAddress == null || fullAddress.isEmpty()) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String base = "https://nominatim.openstreetmap.org/search";
                String q = URLEncoder.encode(fullAddress, StandardCharsets.UTF_8.name());
                String urlStr = base + "?format=json&limit=1&q=" + q;

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", root.getContext().getPackageName());
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder(); String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    JSONArray arr = new JSONArray(sb.toString());
                    if (arr.length() > 0) {
                        JSONObject obj = arr.getJSONObject(0);
                        double lat = Double.parseDouble(obj.optString("lat","0"));
                        double lon = Double.parseDouble(obj.optString("lon","0"));
                        root.post(() -> placeMarker(lat, lon));
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
}
