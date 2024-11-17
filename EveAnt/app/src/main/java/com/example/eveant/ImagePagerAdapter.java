package com.example.eveant;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.SlideItem;

import org.w3c.dom.Text;

import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

    private final List<SlideItem> slideItems; // List containing image, text, and action data.
    private final Context context;

    public ImagePagerAdapter(Context context, List<SlideItem> slideItems) {
        this.context = context;
        this.slideItems = slideItems;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.homepage_hero_section_image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        SlideItem currentItem = slideItems.get(position);

        holder.imageView.setImageResource(currentItem.getImageResId());
        holder.titleTextView.setText(currentItem.getTitle());
        holder.timeTextView.setText(currentItem.getTime());
        holder.locationTextView.setText(currentItem.getLocation());
        holder.dateTextView.setText(currentItem.getDate());
        holder.seeMoreButton.setOnClickListener(v -> {
        });
    }

    @Override
    public int getItemCount() {
        return slideItems.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView timeTextView;
        TextView locationTextView;
        TextView dateTextView;
        Button seeMoreButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.homepage_hero_section_image);
            titleTextView = itemView.findViewById(R.id.title_image_item);
            timeTextView = itemView.findViewById(R.id.time_of_event);
            locationTextView = itemView.findViewById(R.id.location);
            dateTextView = itemView.findViewById(R.id.date_of_event);
            seeMoreButton = itemView.findViewById(R.id.seeMoreButton);
        }
    }
}
