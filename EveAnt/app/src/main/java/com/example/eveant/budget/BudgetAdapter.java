package com.example.eveant.budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    public interface OnBudgetClickListener {
        void onClick(Budget budget);
    }

    private final List<Budget> budgets;
    private final OnBudgetClickListener listener;

    public BudgetAdapter(List<Budget> budgets, OnBudgetClickListener listener) {
        this.budgets = budgets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.budget_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        holder.tvTotalBudget.setText("Total budget: " + budget.getTotalBudget() + " RSD");

        holder.itemView.setOnClickListener(v -> listener.onClick(budget));
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTotalBudget;
        ViewHolder(View v) {
            super(v);
            tvTotalBudget = v.findViewById(R.id.tvTotalBudget);
        }
    }
}
