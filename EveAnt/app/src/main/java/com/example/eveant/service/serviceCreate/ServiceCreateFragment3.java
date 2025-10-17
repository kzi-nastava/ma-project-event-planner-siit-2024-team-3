package com.example.eveant.service.serviceCreate;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.ServiceService;
import com.example.eveant.service.model.Service;
import com.example.eveant.service.model.ServiceDTO;
import com.example.eveant.service.model.ServiceMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceCreateFragment3 extends Fragment {

    private LinearLayout layoutDuration;
    private LinearLayout layoutRange;
    private ToggleButton manualButton, automaticButton;
    private EditText deadlineField, cancellationField;

    private ServiceCreateViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_create3, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);

        layoutDuration = view.findViewById(R.id.layoutDuration);
        layoutRange = view.findViewById(R.id.layoutRange);
        manualButton = view.findViewById(R.id.manualButton);
        automaticButton = view.findViewById(R.id.automaticButton);
        deadlineField = view.findViewById(R.id.deadline);
        cancellationField = view.findViewById(R.id.cacnellationPeriod);

        // Navigacija nazad
        view.findViewById(R.id.previous_button).setOnClickListener(v -> {
            NavController navController = ((MainActivity) getActivity()).getNavController();
            navController.navigate(R.id.serviceCreateFragment2);
        });

        // Toggle handling
        manualButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                automaticButton.setChecked(false);
                manualButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                automaticButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            }
        });

        automaticButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                manualButton.setChecked(false);
                manualButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                automaticButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            }
        });

        // Izbor između duration i range
        layoutDuration.setOnClickListener(v -> {
            resetSelections();
            layoutDuration.setBackgroundResource(R.drawable.background_selected);
            // Ako je samo duration => potvrda rezervacije MORA biti automatic
            automaticButton.setChecked(true);
            automaticButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            manualButton.setChecked(false);
            manualButton.setEnabled(false);
        });

        layoutRange.setOnClickListener(v -> {
            resetSelections();
            layoutRange.setBackgroundResource(R.drawable.background_selected);
            // Ako je range => može se birati između manual i automatic
            manualButton.setEnabled(true);
        });

        view.findViewById(R.id.save_button).setOnClickListener(v -> handleSave(view));

        return view;
    }

    private void resetSelections() {
        layoutDuration.setBackgroundResource(R.drawable.background_unselected);
        layoutRange.setBackgroundResource(R.drawable.background_unselected);
    }

    private void handleSave(View view) {
        Service service = viewModel.getService().getValue();
        if (service == null) {
            Toast.makeText(getContext(), "Service data missing", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isDurationSelected = layoutDuration.getBackground().getConstantState().equals(
                ContextCompat.getDrawable(requireContext(), R.drawable.background_selected).getConstantState()
        );
        boolean isRangeSelected = layoutRange.getBackground().getConstantState().equals(
                ContextCompat.getDrawable(requireContext(), R.drawable.background_selected).getConstantState()
        );

        if (!isDurationSelected && !isRangeSelected) {
            Toast.makeText(getContext(), "Select duration or range", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isDurationSelected) {
            // Jedinstvena duration
            EditText durationHoursField = view.findViewById(R.id.durationHours);
            EditText durationMinutesField = view.findViewById(R.id.durationMinutes);

            int hours = parseOrZero(durationHoursField.getText().toString());
            int minutes = parseOrZero(durationMinutesField.getText().toString());

            if (minutes < 0 || minutes >= 60) {
                Toast.makeText(getContext(), "Minutes must be between 0 and 59", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalMinutes = (hours * 60) + minutes;
            service.setMinEngagement(totalMinutes);
            service.setMaxEngagement(0);

        } else if (isRangeSelected) {
            // Range
            EditText minHoursField = view.findViewById(R.id.minHours);
            EditText minMinutesField = view.findViewById(R.id.minMinutes);
            EditText maxHoursField = view.findViewById(R.id.maxHours);
            EditText maxMinutesField = view.findViewById(R.id.maxMinutes);

            int minHours = parseOrZero(minHoursField.getText().toString());
            int minMinutes = parseOrZero(minMinutesField.getText().toString());
            int maxHours = parseOrZero(maxHoursField.getText().toString());
            int maxMinutes = parseOrZero(maxMinutesField.getText().toString());

            if (minMinutes < 0 || minMinutes >= 60 || maxMinutes < 0 || maxMinutes >= 60) {
                Toast.makeText(getContext(), "Minutes must be between 0 and 59", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalMin = (minHours * 60) + minMinutes;
            int totalMax = (maxHours * 60) + maxMinutes;

            if (totalMin >= totalMax) {
                Toast.makeText(getContext(), "Min duration must be less than max duration", Toast.LENGTH_SHORT).show();
                return;
            }

            service.setMinEngagement(totalMin);
            service.setMaxEngagement(totalMax);
        }

        // Reservation deadline
        int deadline = parseOrZero(deadlineField.getText().toString());
        if (deadline > 10) {
            Toast.makeText(getContext(), "Reservation deadline must be smaller than 10 days", Toast.LENGTH_SHORT).show();
            return;
        }
        service.setReservationDeadLine(deadline);

        // Cancellation period
        int cancellation = parseOrZero(cancellationField.getText().toString());
        if (cancellation > 10) {
            Toast.makeText(getContext(), "Cancellation period must be smaller than 10 days", Toast.LENGTH_SHORT).show();
            return;
        }
        service.setCancellationPeriod(cancellation);

        // Automation setting
        service.setAutomation(automaticButton.isChecked());

        viewModel.updateService(service);
        saveService();
    }

    private int parseOrZero(String text) {
        if (TextUtils.isEmpty(text)) return 0;
        return Integer.parseInt(text);
    }

    private void saveService() {
        Service serviceToSave = viewModel.getService().getValue();
        ServiceDTO serviceDTO = ServiceMapper.toDTO(serviceToSave);

        if (serviceDTO != null) {
            ServiceService serviceService = RetrofitClient.serviceService;
            Log.d(TAG, "Saving service: " + serviceDTO.toString());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Log.d("ServiceDTO_JSON", gson.toJson(serviceDTO));

            serviceService.createService(serviceDTO).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Service created successfully!", Toast.LENGTH_SHORT).show();
                        NavController navController = ((MainActivity) getActivity()).getNavController();
                        navController.navigate(R.id.actionCreateFragment_toViewServices);
                    } else {
                        Log.e("SaveService", "Failed. Code: " + response.code());
                        Toast.makeText(getContext(), "Failed to create service.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("SaveService", "Error: " + t.getMessage());
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
