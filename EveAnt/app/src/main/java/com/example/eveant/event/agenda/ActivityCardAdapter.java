package com.example.eveant.event.agenda;

import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eveant.R;
import com.example.eveant.event.agenda.Activity;
import java.util.List;

public class ActivityCardAdapter extends RecyclerView.Adapter<ActivityCardAdapter.Holder> {

    public interface Listener {
        void onEdit(Activity a);
        void onDelete(Activity a);
    }

    private final List<Activity> data;
    @Nullable
    private final Listener listener;
    private final boolean readOnly;

    public ActivityCardAdapter(List<Activity> data, Listener l) {
        this.data = data; this.listener = l;
        this.readOnly = false;
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p, int vtype) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_activity_card, p, false);
        return new Holder(v);
    }
    public ActivityCardAdapter(List<Activity> data) {
        this.data = data; this.listener = null; this.readOnly = true;
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int pos) {
        Activity a = data.get(pos);
        h.tvTitle.setText(a.name);
        h.tvTime.setText(a.startTime + " - " + a.endTime);
        String addr = "";
        if (a.address.getStreet() != null) addr += a.address.getStreet() + " ";
        if (a.address.getHouseNumber() != null) addr += a.address.getHouseNumber() + ", ";
        if (a.address.getCity() != null) addr += a.address.getCity();
        h.tvAddress.setText(addr.trim());
        h.tvDesc.setText(a.description);

        h.btnEdit.setOnClickListener(v -> listener.onEdit(a));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(a));
    }

    @Override public int getItemCount() { return data.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvAddress, tvDesc;
        ImageButton btnEdit, btnDelete;
        Holder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvTime  = v.findViewById(R.id.tvTime);
            tvAddress = v.findViewById(R.id.tvAddress);
            tvDesc = v.findViewById(R.id.tvDesc);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
