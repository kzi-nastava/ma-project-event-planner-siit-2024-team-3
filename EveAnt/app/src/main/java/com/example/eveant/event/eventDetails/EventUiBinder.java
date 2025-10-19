package com.example.eveant.event.eventDetails;

import com.example.eveant.event.eventDetails.utils.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eveant.R;
import com.example.eveant.event.Event;
import com.example.eveant.event.EventStatus;

/**
 * Handles all header/UI field bindings for an EventDetails screen.
 * This keeps the fragment clean and focused on logic.
 */
class EventUiBinder {

    interface OnAddressResolved {
        void onReady(String fullAddress);
    }

    static void bindHeader(Context ctx, View root, Event e, OnAddressResolved onAddressReady) {
        if (e == null || root == null) return;

        TextView tvEventType   = root.findViewById(R.id.tvEventType);
        TextView tvTitle       = root.findViewById(R.id.tvTitle);
        TextView tvAddress     = root.findViewById(R.id.tvAddress);
        TextView tvDate        = root.findViewById(R.id.tvDate);
        TextView tvTime        = root.findViewById(R.id.tvTime);
        TextView tvGuests      = root.findViewById(R.id.tvGuests);
        TextView tvDescription = root.findViewById(R.id.tvDescription);
        ImageView ivPhoto      = root.findViewById(R.id.ivPhoto);

        if (tvTitle != null) tvTitle.setText(Str.nz(e.getName()));
        if (tvEventType != null) tvEventType.setText(
                e.getEventType() != null ? Str.nz(e.getEventType().getName()) : "—"
        );
        if (tvDescription != null) tvDescription.setText(Str.nz(e.getDescription()));
        if (tvGuests != null) tvGuests.setText(
                e.getMaxAttendance() == null ? "—" : String.valueOf(e.getMaxAttendance())
        );

        String datePart = TimeFmt.extractDate(e.getDate());
        String timePart = TimeFmt.extractTime(e.getDate());
        if (tvDate != null) tvDate.setText(datePart);
        if (tvTime != null) tvTime.setText(timePart);

        // Address
        if (tvAddress != null && e.getAddress() != null) {
            String displayAddr = Str.join(", ",
                    e.getAddress().getStreet(),
                    e.getAddress().getHouseNumber(),
                    e.getAddress().getCity(),
                    e.getAddress().getCountry());
            tvAddress.setText(displayAddr);

            String fullAddr = buildAddressLine(e);
            if (!TextUtils.isEmpty(fullAddr) && onAddressReady != null) {
                onAddressReady.onReady(fullAddr); // MapController will geocode
            }
        }

        // Cover image
        bindHeaderPhoto(ctx, ivPhoto, e);
    }

    private static void bindHeaderPhoto(Context ctx, ImageView iv, Event e) {
        if (iv == null) return;
        String cover = (e.getPhotos() != null && !e.getPhotos().isEmpty()) ? e.getPhotos().get(0) : null;

        if (cover != null && (cover.startsWith("http://") || cover.startsWith("https://"))) {
            Glide.with(ctx).load(cover)
                    .placeholder(R.drawable.rounded_corners_image_blue)
                    .into(iv);
            return;
        }
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
                        iv.setImageBitmap(bmp);
                        return;
                    }
                }
            } catch (Exception ignore) { }
        }
        iv.setImageResource(R.drawable.rounded_corners_image_blue);
    }

    private static String buildAddressLine(Event e) {
        if (e.getAddress() == null) return "";
        String street  = Str.nz(e.getAddress().getStreet());
        String house   = Str.nz(e.getAddress().getHouseNumber());
        String city    = Str.nz(e.getAddress().getCity());
        String country = Str.nz(e.getAddress().getCountry());
        String line = Str.join(" ", street, house);
        return Str.join(", ", line.isEmpty()? null: line, city, country);
    }
}
