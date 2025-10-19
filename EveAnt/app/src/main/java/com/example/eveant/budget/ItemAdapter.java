package com.example.eveant.budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eveant.R;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface ItemListener {
        void onEdit(Item item);
        void onDelete(Item item);
        void onSelect(Item item);
    }

    private final List<Item> items;
    private final ItemListener listener;
    private final boolean isFlowMode;

    public ItemAdapter(List<Item> items, ItemListener listener, boolean isFlowMode) {
        this.items = items;
        this.listener = listener;
        this.isFlowMode = isFlowMode;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory() != null ? item.getCategory().getName() : "N/A");
        holder.tvMaxPrice.setText(item.getMaxPrice() + " RSD");

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));

        if (isFlowMode) {
            holder.btnSelect.setVisibility(View.GONE);
        } else {
            holder.btnSelect.setVisibility(View.VISIBLE);
            holder.btnSelect.setOnClickListener(v -> listener.onSelect(item));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvMaxPrice;
        Button btnEdit, btnDelete, btnSelect;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvMaxPrice = itemView.findViewById(R.id.tvItemMaxPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSelect = itemView.findViewById(R.id.btnSelectOffer);
        }
    }
}
