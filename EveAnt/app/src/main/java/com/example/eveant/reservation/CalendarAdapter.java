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
    private final LocalDate selectedDate;  // Store the selectedDate
    private final int reservationDeadline;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, LocalDate selectedDate, int reservationDeadline) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.selectedDate = selectedDate;  // Assign selectedDate
        this.reservationDeadline = reservationDeadline;
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
            YearMonth displayedMonth = YearMonth.from(LocalDate.of(selectedDate.getYear(), selectedDate.getMonth(), 1));
            LocalDate cellDate = displayedMonth.atDay(dayOfMonth);
            LocalDate maxSelectableDate = LocalDate.now().plusDays(reservationDeadline);

            if (cellDate.isBefore(LocalDate.now())) {
                // Grey out past dates
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
                holder.itemView.setEnabled(false);
            }  else if (cellDate.equals(LocalDate.now())) {
                // Highlight today's date
                holder.dayOfMonth.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.today_cell_background));
            } else if (cellDate.isBefore(maxSelectableDate)) {
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
            } else {
                // Enable future dates
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
                holder.itemView.setEnabled(true);
            }
        }
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, TextView day);
    }
}
