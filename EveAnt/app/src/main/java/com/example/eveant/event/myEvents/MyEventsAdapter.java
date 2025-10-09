package com.example.eveant.event.myEvents;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eveant.R;
import com.example.eveant.event.Event;
import com.example.eveant.eventType.EventType;
import com.example.eveant.user.model.Address;

import java.util.ArrayList;
import java.util.List;

public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.VH> {

    public interface Listener {
        void onEdit(Event e);
        void onDelete(Event e);
        void onOpen(Event e);
    }

    private final Listener listener;
    private final List<Event> items = new ArrayList<>();

    public MyEventsAdapter(Listener l) { this.listener = l; }

    public void submit(List<Event> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Event e = items.get(pos);

        String type = "All";
        EventType et = e.getEventType();
        if (et != null && et.getName() != null) type = et.getName();
        h.tvType.setText(type);

        h.tvTitle.setText(e.getName() == null ? "Event" : e.getName());

        Address a = e.getAddress();
        String addr = "";
        if (a != null) {
            // Make a short address line like "Street No, City"
            String s = nullToEmpty(a.getStreet());
            String no = nullToEmpty(a.getHouseNumber());
            String city = nullToEmpty(a.getCity());
            String country = nullToEmpty(a.getCountry());
            String left = s + (no.isEmpty() ? "" : " " + no);
            addr = (left.trim() + (city.isEmpty() ? "" : ", " + city)).trim();
            if (addr.isEmpty()) addr = country;
        }
        h.tvLocation.setText(addr.isEmpty() ? "—" : addr);

        // Date/time (works for "yyyy-MM-ddTHH:mm:ss" or "yyyy-MM-dd HH:mm")
        String dt = e.getDate() == null ? "" : e.getDate();
        String date = dt;
        String time = "";
        int tPos = dt.indexOf('T');
        if (tPos < 0) tPos = dt.indexOf(' ');
        if (tPos > 0) {
            date = dt.substring(0, tPos);
            if (dt.length() >= tPos + 6) time = dt.substring(tPos + 1, Math.min(dt.length(), tPos + 6));
        }
        h.tvDate.setText(date);
        h.tvTime.setText(time);

        // Cover image: prefer first photo. Supports URL or base64.
        String cover = (e.getPhotos() != null && !e.getPhotos().isEmpty()) ? e.getPhotos().get(0) : null;
        if (cover != null && (cover.startsWith("http://") || cover.startsWith("https://"))) {
            Glide.with(h.imgCover).load(cover).placeholder(R.drawable.rounded_corners_image_blue).into(h.imgCover);
        } else if (cover != null && cover.length() > 100) { // likely base64
            try {
                byte[] data = Base64.decode(cover, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                h.imgCover.setImageBitmap(bmp);
            } catch (Exception ex) {
                h.imgCover.setImageResource(R.drawable.rounded_corners_image_blue);
            }
        } else {
            h.imgCover.setImageResource(R.drawable.rounded_corners_image_blue);
        }

        h.itemView.setOnClickListener(v -> listener.onOpen(e));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(e));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(e));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvType, tvTitle, tvLocation, tvDate, tvTime;
        ImageButton btnEdit, btnDelete;

        VH(@NonNull View v) {
            super(v);
            imgCover  = v.findViewById(R.id.imgCover);
            tvType    = v.findViewById(R.id.tvType);
            tvTitle   = v.findViewById(R.id.tvTitle);
            tvLocation= v.findViewById(R.id.tvLocation);
            tvDate    = v.findViewById(R.id.tvDate);
            tvTime    = v.findViewById(R.id.tvTime);
            btnEdit   = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
