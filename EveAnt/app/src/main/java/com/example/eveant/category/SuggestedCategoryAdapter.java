package com.example.eveant.category;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eveant.R;
import com.example.eveant.service.model.Category;

import java.util.List;

public class SuggestedCategoryAdapter extends RecyclerView.Adapter<SuggestedCategoryAdapter.ViewHolder> {

    private final Context context;
    private final List<Category> categoryList;
    private final OnSuggestedActionListener listener;

    public interface OnSuggestedActionListener {
        void onApprove(Category category);
        void onReject(Category category);
    }

    public SuggestedCategoryAdapter(Context context, List<Category> categoryList, OnSuggestedActionListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.suggested_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.name.setText(category.getName());

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(category));
        holder.btnReject.setOnClickListener(v -> listener.onReject(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageButton btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.categoryName);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }

}
