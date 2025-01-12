package com.example.eveant.reservation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.eveant.R;

public class TimelineFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        // Get the selected date
        Bundle arguments = getArguments();
        if (arguments != null) {
            String selectedDate = arguments.getString("selectedDate");
            TextView dateTextView = view.findViewById(R.id.dateTextView);
            dateTextView.setText("Selected Date: " + selectedDate);
        }

        return view;
    }
}