package com.example.eveant.event.eventDetails;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.event.agenda.Activity;
import com.example.eveant.event.eventDetails.utils.Str;
import com.example.eveant.event.eventDetails.utils.TimeFmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class ActivitiesController {

    private final RecyclerView rv;
    private final MiniActivityAdapter adapter = new MiniActivityAdapter();
    private final List<Activity> current = new ArrayList<>();

    ActivitiesController(RecyclerView rv) {
        this.rv = rv;
        if (this.rv != null) this.rv.setAdapter(adapter);
    }

    void submit(List<Activity> activities) {
        current.clear();
        if (activities != null) current.addAll(activities);
        List<Activity> data = new ArrayList<>(current);
        Collections.sort(data, Comparator.comparing(a -> a.startTime == null ? "" : a.startTime));

        List<Mini> minis = new ArrayList<>();
        for (Activity a : data) {
            String st = TimeFmt.extractTime(a.startTime);
            String et = TimeFmt.extractTime(a.endTime);
            String win = (isDash(st) || isDash(et)) ? st : (st + " - " + et);

            String where = "";
            if (a.address != null) {
                where = Str.join(", ",
                        a.address.getStreet(),
                        a.address.getCity());
            }
            minis.add(new Mini(win, Str.nz(a.name), where));
        }
        adapter.submit(minis);
    }

    List<Activity> getCurrentActivities() { return new ArrayList<>(current); }

    private boolean isDash(String s){ return s==null || s.isEmpty() || "—".equals(s); }

    /* ---------- adapter + helper models ---------- */

    private static class Mini {
        final String time, title, where;
        Mini(String t, String ti, String w) { time = t; title = ti; where = w; }
    }

    private static class MiniVH extends RecyclerView.ViewHolder {
        android.widget.TextView tvTime, tvTitle, tvWhere;
        MiniVH(@NonNull View v) {
            super(v);
            tvTime  = v.findViewById(R.id.tvTime);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvWhere = v.findViewById(R.id.tvAddress);
        }
    }

    private static class MiniActivityAdapter extends RecyclerView.Adapter<MiniVH> {
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
