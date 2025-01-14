package com.example.eveant.reservation;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private TextView selectedDateView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        initWidgets(view);
        selectedDate = LocalDate.now();
        setMonthView();

        Button previousMonthButton = view.findViewById(R.id.previousMonthButton);
        Button nextMonthButton = view.findViewById(R.id.nextMonthButton);
        Button nextButton = view.findViewById(R.id.nextButton);
        Button backButton = view.findViewById(R.id.backButton);

        previousMonthButton.setOnClickListener(v -> previousMonthAction(v));
        nextMonthButton.setOnClickListener(v -> nextMonthAction(v));
        nextButton.setOnClickListener(v -> nextButton(v));

        return view;
    }

    private void nextButton(View view) {
        if (selectedDate != null) {
            // Create the next fragment (TimelineFragment)
            Bundle bundle = new Bundle();
            bundle.putString("selectedDate", selectedDate.toString());
            Fragment nextFragment = new TimelineFragment();
            nextFragment.setArguments(bundle);

            // Use FragmentManager to navigate to the new fragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, nextFragment) // Replace the container with the TimelineFragment
                    .addToBackStack(null)  // Add the fragment to the back stack so the user can go back
                    .commit();
        } else {
            Toast.makeText(requireContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
        }
    }



    private void initWidgets(View view){
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.monthYearTV);
    }

    private void setMonthView(){
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private String monthYearFromDate(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
        return date.format(formatter);
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        // Get the first day of the month
        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 is Monday, 7 is Sunday

        // Add empty strings for days before the first day of the month
        for (int i = 1; i < dayOfWeek; i++) {
            daysInMonthArray.add("");
        }

        // Add actual days of the month
        for (int day = 1; day <= daysInMonth; day++) {
            daysInMonthArray.add(String.valueOf(day));
        }

        // Fill remaining cells to complete the 6 rows (42 cells) in the calendar
        int remainingCells = 42 - daysInMonthArray.size();
        for (int i = 0; i < remainingCells; i++) {
            daysInMonthArray.add("");
        }

        return daysInMonthArray;
    }


    public void previousMonthAction(View view)
    {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view)
    {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, TextView day) {
        if (!day.getText().toString().isEmpty()) {
            int dayOfMonth = Integer.parseInt(day.getText().toString());
            LocalDate clickedDate = selectedDate.withDayOfMonth(dayOfMonth);
            if (clickedDate.isBefore(LocalDate.now())) {
                Toast.makeText(requireContext(), "Past dates are not selectable", Toast.LENGTH_SHORT).show();
                return;
            }
            if (clickedDate.equals(LocalDate.now())){
                Toast.makeText(requireContext(), "Today is not selectable", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDateView != null) {
                selectedDateView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.cell_border));
            }

            day.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.selected_cell_background));
            selectedDateView = day;

            selectedDate = clickedDate;

            String message = "Selected Date: " + selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault()));
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }


}
