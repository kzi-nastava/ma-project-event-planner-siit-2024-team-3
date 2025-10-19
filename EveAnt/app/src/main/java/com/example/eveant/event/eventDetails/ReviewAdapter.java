package com.example.eveant.event.eventDetails;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eveant.event.eventDetails.utils.*;

import com.example.eveant.R;
import com.example.eveant.reviews.Review;

import java.util.ArrayList;
import java.util.List;

/** Minimal adapter that shows author • rating • text • when. */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    private final List<Review> items = new ArrayList<>();

    public void replaceAll(List<Review> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public List<Review> getItems() { return new ArrayList<>(items); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Review r = items.get(pos);
        h.tvAuthor.setText(Str.nz(r.getAuthor(), "Anonymous"));
        h.tvWhen.setText(Str.trim(Str.join(" ", TimeFmt.extractDate(r.getCreatedAt()), TimeFmt.extractTime(r.getCreatedAt()))));
        h.tvComment.setText(Str.nz(r.getComment(), ""));
        Integer rating = r.getRating();
        if (rating == null) rating = 0;
        h.ratingBar.setRating(rating);
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvWhen, tvComment;
        RatingBar ratingBar;
        VH(@NonNull View v) {
            super(v);
            tvAuthor  = v.findViewById(R.id.tvAuthor);
            tvWhen    = v.findViewById(R.id.tvWhen);
            tvComment = v.findViewById(R.id.tvComment);
            ratingBar = v.findViewById(R.id.ratingBar);
        }
    }
}
