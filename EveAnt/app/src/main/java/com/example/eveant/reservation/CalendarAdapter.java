package com.example.eveant.reservation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;

import java.time.YearMonth;
import java.util.ArrayList;
import androidx.core.content.ContextCompat;
import java.time.LocalDate;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
   private final ArrayList<String> daysOfMonth;
   private final OnItemListener onItemListener;


    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String day = daysOfMonth.get(position);
        holder.dayOfMonth.setText(day);

        if (!day.isEmpty()) {
            int dayOfMonth = Integer.parseInt(day);
            YearMonth currentYearMonth = YearMonth.from(LocalDate.now());
            LocalDate cellDate = currentYearMonth.atDay(dayOfMonth);

            if (cellDate.isBefore(LocalDate.now())) {
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
                holder.itemView.setEnabled(false); // Disable past dates
            } else if (cellDate.equals(LocalDate.now())) {
                holder.dayOfMonth.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.today_cell_background));
            } else {
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
                holder.itemView.setEnabled(true); // Enable future dates
            }
        }
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public interface OnItemListener{
        void onItemClick(int position, TextView day);
    }
}
