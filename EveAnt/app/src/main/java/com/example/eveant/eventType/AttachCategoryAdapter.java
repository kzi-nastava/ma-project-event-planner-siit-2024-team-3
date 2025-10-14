package com.example.eveant.eventType;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.service.model.Category;

import java.util.List;
import java.util.Set;

public class AttachCategoryAdapter extends RecyclerView.Adapter<AttachCategoryAdapter.VH> {
    private final List<Category> data;
    private final Set<Integer> selectedIds;
    public AttachCategoryAdapter(List<Category> data, Set<Integer> selectedIds) { this.data = data; this.selectedIds = selectedIds; setHasStableIds(true); }
    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc; View row; androidx.appcompat.widget.SwitchCompat sw;
        VH(@NonNull View itemView) { super(itemView);
            row = itemView.findViewById(R.id.root);
            tvName = itemView.findViewById(R.id.tvCatName);
            tvDesc = itemView.findViewById(R.id.tvCatDesc);
            sw = itemView.findViewById(R.id.swActive);
        }
    }
    @Override public long getItemId(int position) { Integer id = safeId(data.get(position)); return id == null ? RecyclerView.NO_ID : id; }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_attach_category_row, p, false); return new VH(v);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Category c = data.get(pos); Integer id = safeId(c);
        h.tvName.setText(nz(c.getName())); h.tvDesc.setText(nz(c.getDescription()));
        h.sw.setOnCheckedChangeListener(null);
        boolean checked = (id != null) && selectedIds.contains(id);
        h.sw.setChecked(checked);
        h.sw.setOnCheckedChangeListener((button, isChecked) -> { if (id == null) return; if (isChecked) selectedIds.add(id); else selectedIds.remove(id); });
        h.row.setOnClickListener(v -> {
            boolean newChecked = !h.sw.isChecked();
            h.sw.setOnCheckedChangeListener(null);
            h.sw.setChecked(newChecked);
            h.sw.setOnCheckedChangeListener((button, isChecked) -> { if (id == null) return; if (isChecked) selectedIds.add(id); else selectedIds.remove(id); });
            if (id != null) { if (newChecked) selectedIds.add(id); else selectedIds.remove(id); }
        });
    }
    @Override public int getItemCount() { return data.size(); }
    private String nz(String s) { return s == null ? "" : s; }
    private Integer safeId(Category c) { return (c == null) ? null : c.getId(); }
}