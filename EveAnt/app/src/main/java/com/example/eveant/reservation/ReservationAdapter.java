package com.example.eveant.reservation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.reservation.Reservation;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private Reservation[] reservations;

    public ReservationAdapter(Reservation[] reservations) {
        // Sort the reservations by start time
        Arrays.sort(reservations, new Comparator<Reservation>() {
            @Override
            public int compare(Reservation r1, Reservation r2) {
                LocalTime time1 = LocalTime.parse(r1.getStartTime().split("T")[1]);
                LocalTime time2 = LocalTime.parse(r2.getStartTime().split("T")[1]);
                return time1.compareTo(time2);
            }
        });

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

        // Extract and format the time parts
        String startTime = reservation.getStartTime().split("T")[1].substring(0, 5);
        String endTime = reservation.getEndTime().split("T")[1].substring(0, 5);

        String startEndTime = "Start: " + startTime + " - End: " + endTime;
        holder.startEndTimeTextView.setText(startEndTime);

        holder.ordinalNumberTextView.setText(String.valueOf(position + 1));
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
