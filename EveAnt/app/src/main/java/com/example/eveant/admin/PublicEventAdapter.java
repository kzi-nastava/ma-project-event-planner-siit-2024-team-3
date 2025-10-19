package com.example.eveant.admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.Event;
import com.example.eveant.event.invitations.Invitation;
import com.example.eveant.reviews.Review;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicEventAdapter extends RecyclerView.Adapter<PublicEventAdapter.VH> {

    private final List<Event> data = new ArrayList<>();

    public void submit(List<Event> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_event_public, p, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Event e = data.get(pos);

        // Basic info
        h.tvTitle.setText(e.getName() != null ? e.getName() : "Untitled");
        h.tvType.setText(e.getEventType() != null && e.getEventType().getName() != null ? e.getEventType().getName() : "All");

        String city = e.getAddress() != null ? e.getAddress().getCity() : null;
        String street = e.getAddress() != null ? e.getAddress().getStreet() : null;
        String loc = (street != null ? street : "") + (city != null ? (", " + city) : "");
        h.tvLocation.setText(!loc.trim().isEmpty() ? loc : "—");

        String date = e.getDate();
        String dateText = "", timeText = "";
        if (date != null && date.contains("T")) {
            String[] parts = date.split("T");
            dateText = parts[0];
            timeText = parts[1];
            if (timeText.length() >= 5) timeText = timeText.substring(0, 5);
        } else if (date != null) {
            dateText = date;
        }
        h.tvDate.setText(dateText);
        h.tvTime.setText(timeText);

        // --- COVER IMAGE ---
        String cover = (e.getPhotos() != null && !e.getPhotos().isEmpty()) ? e.getPhotos().get(0) : null;

        if (!TextUtils.isEmpty(cover)) {
            if (cover.startsWith("http://") || cover.startsWith("https://")) {
                // Full URL
                Glide.with(h.itemView)
                        .load(cover)
                        .placeholder(R.drawable.rounded_corners_image_blue)
                        .error(R.drawable.rounded_corners_image_blue)
                        .into(h.imgCover);

            } else if (isLikelyBase64(cover)) {
                // Base64
                try {
                    String b64 = stripDataUriPrefix(cover);
                    byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    h.imgCover.setImageBitmap(bmp);
                } catch (Exception ex) {
                    h.imgCover.setImageResource(R.drawable.rounded_corners_image_blue);
                }

            } else {
                // Relative or unknown → just placeholder
                h.imgCover.setImageResource(R.drawable.rounded_corners_image_blue);
            }
        } else {
            // No photo at all → placeholder
            h.imgCover.setImageResource(R.drawable.rounded_corners_image_blue);
        }

        // --- ATTENDANCE ---
        RetrofitClient.invitationEventService.getInvitations(e.getId()).enqueue(new Callback<List<Invitation>>() {
            @Override
            public void onResponse(Call<List<Invitation>> call, Response<List<Invitation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int att = response.body().size();
                    h.tvAttendance.setText("Attendance: " + att);
                } else {
                    h.tvAttendance.setText("Attendance: –");
                }
            }

            @Override
            public void onFailure(Call<List<Invitation>> call, Throwable t) {
                h.tvAttendance.setText("Attendance: –");
            }
        });

        // --- REVIEWS ---
        RetrofitClient.reviewService.getReviews(e.getId()).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Review> reviews = response.body();
                    int count = reviews.size();
                    double avg = 0.0;
                    if (count > 0) {
                        double sum = 0.0;
                        for (Review r : reviews) sum += r.getRating();
                        avg = sum / count;
                    }
                    String revText = "Reviews: " + count + (count > 0 ? "  •  Avg " + String.format(Locale.US, "%.1f", avg) : "");
                    h.tvReviews.setText(revText);
                } else {
                    h.tvReviews.setText("Reviews: –");
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                h.tvReviews.setText("Reviews: –");
            }
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CardView card;
        ImageView imgCover;
        TextView tvType, tvTitle, tvLocation, tvDate, tvTime, tvAttendance, tvReviews;
        View btnAttendanceGraph, btnReviewsGraph;
        VH(@NonNull View v) {
            super(v);
            card = (CardView) v;
            imgCover = v.findViewById(R.id.imgCover);
            tvType = v.findViewById(R.id.tvType);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvLocation = v.findViewById(R.id.tvLocation);
            tvDate = v.findViewById(R.id.tvDate);
            tvTime = v.findViewById(R.id.tvTime);
            tvAttendance = v.findViewById(R.id.tvAttendance);
            tvReviews = v.findViewById(R.id.tvReviews);
        }
    }

    // --- helpers ---
    private static boolean isLikelyBase64(String s) {
        if (TextUtils.isEmpty(s)) return false;
        if (s.startsWith("data:image")) return true;
        if (s.length() < 100) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean ok = (c >= 'A' && c <= 'Z') ||
                    (c >= 'a' && c <= 'z') ||
                    (c >= '0' && c <= '9') ||
                    c == '+' || c == '/' || c == '=' || c == '\n' || c == '\r';
            if (!ok) return false;
        }
        return true;
    }

    private static String stripDataUriPrefix(String s) {
        if (s == null) return "";
        int idx = s.indexOf("base64,");
        return (idx >= 0) ? s.substring(idx + "base64,".length()) : s;
    }
}
