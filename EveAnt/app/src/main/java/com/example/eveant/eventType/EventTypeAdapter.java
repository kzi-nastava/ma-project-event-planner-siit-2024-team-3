package com.example.eveant.eventType;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.databinding.ItemEventTypeRowBinding;
import com.example.eveant.service.model.Category;

import java.util.ArrayList;
import java.util.List;

public class EventTypeAdapter extends RecyclerView.Adapter<EventTypeAdapter.VH> {

    interface OnEditClick { void onEdit(EventType et); }
    interface OnToggleActive { void onToggle(EventType et, boolean newActive, int adapterPosition); }

    private final List<EventType> items = new ArrayList<>();
    private OnEditClick editClick;
    private OnToggleActive toggleActive;

    void setOnEditClick(OnEditClick cb) { this.editClick = cb; }
    void setOnToggleActive(OnToggleActive cb) { this.toggleActive = cb; }

    void submit(List<EventType> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemEventTypeRowBinding b;
        VH(ItemEventTypeRowBinding b) { super(b.getRoot()); this.b = b; }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventTypeRowBinding b = ItemEventTypeRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EventType it = items.get(position);

        h.b.tvTitle.setText(it.getName() == null ? "" : it.getName());
        h.b.tvSubtitle.setText(it.getDescription() == null ? "" : it.getDescription());

        // Category chips (blue by default; green if “used”)
        h.b.llSuggestedContainer.removeAllViews();
        if (it.getSuggestedCategories() != null) {
            final int padH = (int) (10f * h.itemView.getResources().getDisplayMetrics().density);
            final int padV = (int) (6f  * h.itemView.getResources().getDisplayMetrics().density);
            final int margin = (int) (6f  * h.itemView.getResources().getDisplayMetrics().density);

            for (Category c : it.getSuggestedCategories()) {
                TextView chip = new TextView(h.itemView.getContext());
                chip.setText(c.getName() == null ? "" : c.getName());
                chip.setTypeface(Typeface.DEFAULT_BOLD);
                chip.setMaxLines(1);
                chip.setEllipsize(TextUtils.TruncateAt.END);
                chip.setPadding(padH, padV, padH, padV);
                chip.setBackgroundResource(R.drawable.bg_primary_pill);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.rightMargin = margin; lp.bottomMargin = margin;
                chip.setLayoutParams(lp);
                h.b.llSuggestedContainer.addView(chip);
            }
        }

        h.b.btnEdit.setOnClickListener(v -> { if (editClick != null) editClick.onEdit(it); });

        boolean isActive = Boolean.TRUE.equals(it.getActive());
        h.b.swActive.setOnCheckedChangeListener(null);
        h.b.swActive.setChecked(isActive);
        h.b.swActive.setText(isActive ? "Active" : "Inactive");
        h.b.swActive.setOnCheckedChangeListener((buttonView, checked) -> {
            h.b.swActive.setText(checked ? "Active" : "Inactive");
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && toggleActive != null) {
                toggleActive.onToggle(it, checked, pos);
            }
        });
    }

    @Override public int getItemCount() { return items.size(); }

}