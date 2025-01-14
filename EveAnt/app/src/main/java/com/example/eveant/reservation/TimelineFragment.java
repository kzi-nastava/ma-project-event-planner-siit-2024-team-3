package com.example.eveant.reservation;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.service.model.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

public class TimelineFragment extends Fragment {
    private Service service;
    private Reservation[] reservations;
    private boolean isUpdating = false; // Flag to prevent recursive triggers

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
        Button backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pop the current fragment from the fragment back stack
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
        service = new Service();
        service.setMaxEngagement(40);
        service.setMinEngagement(30);

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.reservation_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create and set the adapter for the RecyclerView
        ReservationAdapter adapter = new ReservationAdapter(reservations);
        recyclerView.setAdapter(adapter);

        EditText startHourEditText = view.findViewById(R.id.startHourEditText);
        EditText startMinuteEditText = view.findViewById(R.id.startMinuteEditText);
        EditText endHourEditText = view.findViewById(R.id.endHourEditText);
        EditText endMinuteEditText = view.findViewById(R.id.endMinuteEditText);

        // Disable end time fields if max engagement is 0
        if (service.getMaxEngagement() == 0) {
            endHourEditText.setFocusable(false);
            endHourEditText.setClickable(false);
            endMinuteEditText.setFocusable(false);
            endMinuteEditText.setClickable(false);
            endHourEditText.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.greyed_out_edittext_background));
            endMinuteEditText.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.greyed_out_edittext_background));
        }

        // Automatically calculate end time whenever start time is updated
        startHourEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                calculateEndTime(startHourEditText, startMinuteEditText, endHourEditText, endMinuteEditText);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        startMinuteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                calculateEndTime(startHourEditText, startMinuteEditText, endHourEditText, endMinuteEditText);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Button to trigger manual validation
        Button validateButton = view.findViewById(R.id.bookButton);
        validateButton.setOnClickListener(v -> validateTimeInputs(startHourEditText, startMinuteEditText, endHourEditText, endMinuteEditText));

        return view;
    }

    private void calculateEndTime(EditText startHourEditText, EditText startMinuteEditText, EditText endHourEditText, EditText endMinuteEditText) {
        if (isUpdating) return; // Prevent recursive triggers

        try {
            isUpdating = true; // Set the flag to true

            String startHourText = startHourEditText.getText().toString();
            String startMinuteText = startMinuteEditText.getText().toString();

            // Skip calculation if any of the start time fields are empty
            if (startHourText.isEmpty() || startMinuteText.isEmpty()) {
                clearEndTimeFields(endHourEditText, endMinuteEditText); // Clear dependent fields
                return;
            }

            int startHour = Integer.parseInt(startHourText);
            int startMinute = Integer.parseInt(startMinuteText);

            // Validate start time
            if (startHour < 0 || startHour > 23 || startMinute < 0 || startMinute > 59) {
                showToast("Start time is invalid.");
                clearEndTimeFields(endHourEditText, endMinuteEditText);
                return;
            }

            // Automatically calculate end time based on start time and min engagement
            if (service.getMaxEngagement() == 0) {
                if (startHour >= 0 && startMinute >= 0) { // Ensure valid start time
                    int minEngagement = service.getMinEngagement() != null ? service.getMinEngagement() : 0;
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, startHour);
                    calendar.set(Calendar.MINUTE, startMinute);
                    calendar.add(Calendar.MINUTE, minEngagement);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    String[] calculatedEndTime = timeFormat.format(calendar.getTime()).split(":");

                    // Prevent triggering the TextWatcher during programmatic updates
                    isUpdating = true;
                    endHourEditText.setText(calculatedEndTime[0]);
                    endMinuteEditText.setText(calculatedEndTime[1]);
                    isUpdating = false;
                }
            }

        } catch (NumberFormatException e) {
            showToast("Invalid input. Please enter valid times.");
            clearEndTimeFields(endHourEditText, endMinuteEditText);
        } finally {
            isUpdating = false; // Reset the flag
        }
    }

    private void clearEndTimeFields(EditText endHourEditText, EditText endMinuteEditText) {
        endHourEditText.setText("");
        endMinuteEditText.setText("");
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void validateTimeInputs(EditText startHourEditText, EditText startMinuteEditText, EditText endHourEditText, EditText endMinuteEditText) {
        String startHourText = startHourEditText.getText().toString();
        String startMinuteText = startMinuteEditText.getText().toString();
        String endHourText = endHourEditText.getText().toString();
        String endMinuteText = endMinuteEditText.getText().toString();

        if (startHourText.isEmpty() || startMinuteText.isEmpty()) {
            showToast("Start time cannot be empty.");
            return;
        }

        if (endHourText.isEmpty() || endMinuteText.isEmpty()) {
            showToast("End time cannot be empty.");
            return;
        }

        int startHour = Integer.parseInt(startHourText);
        int startMinute = Integer.parseInt(startMinuteText);
        int endHour = Integer.parseInt(endHourText);
        int endMinute = Integer.parseInt(endMinuteText);

        // Validate start and end times are valid
        if (startHour < 0 || startHour > 23 || startMinute < 0 || startMinute > 59 || endHour < 0 || endHour > 23 || endMinute < 0 || endMinute > 59) {
            showToast("Invalid time input.");
            return;
        }

        // Validate that end time is greater than start time
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, startHour);
        startCalendar.set(Calendar.MINUTE, startMinute);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, endHour);
        endCalendar.set(Calendar.MINUTE, endMinute);

        if (endCalendar.before(startCalendar)) {
            showToast("End time must be after start time.");
            return;
        }

        // Check if end time exceeds 24 hours (from start time)
        long timeDifference = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        long oneDayInMillis = 24 * 60 * 60 * 1000;

        if (timeDifference > oneDayInMillis) {
            showToast("End time exceeds 24 hours.");
            return;
        }

        // Validate that total time is between minEngagement and maxEngagement if maxEngagement > 0
        if (service.getMaxEngagement() > 0) {
            int minEngagement = service.getMinEngagement() != null ? service.getMinEngagement() : 0;

            long minEngagementMillis = minEngagement * 60 * 1000; // Convert to milliseconds
            long maxEngagementMillis = service.getMaxEngagement() * 60 * 1000; // Convert to milliseconds

            if (timeDifference < minEngagementMillis || timeDifference > maxEngagementMillis) {
                showToast("Total time is must be between " + minEngagement + " and " + service.getMaxEngagement() + ".");
                return;
            }

        }

        showToast("Time input is valid.");
    }

}
