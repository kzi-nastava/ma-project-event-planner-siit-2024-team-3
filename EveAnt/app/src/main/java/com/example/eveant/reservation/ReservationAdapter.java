package com.example.eveant.reservation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.reservation.Reservation;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private Reservation[] reservations;

    // Constructor to receive the list of reservations
    public ReservationAdapter(Reservation[] reservations) {
        this.reservations = reservations;
    }

    @Override
    public ReservationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReservationViewHolder holder, int position) {
        Reservation reservation = reservations[position];

        // Setting the ordinal number (position + 1)
        holder.ordinalNumberTextView.setText(String.valueOf(position + 1));

        // Setting the start and end times
        String startEndTime = "Start: " + reservation.getStartTime() + " - End: " + reservation.getEndTime();
        holder.startEndTimeTextView.setText(startEndTime);
    }

    @Override
    public int getItemCount() {
        return reservations.length;
    }

    public static class ReservationViewHolder extends RecyclerView.ViewHolder {

        TextView ordinalNumberTextView;
        TextView startEndTimeTextView;

        public ReservationViewHolder(View itemView) {
            super(itemView);
            ordinalNumberTextView = itemView.findViewById(R.id.ordinal_number);
            startEndTimeTextView = itemView.findViewById(R.id.start_end_time);
        }
    }
}
