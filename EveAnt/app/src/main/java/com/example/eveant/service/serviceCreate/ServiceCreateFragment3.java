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
import com.example.eveant.user.security.AuthManager;
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
    private EditText durationHoursField, durationMinutesField, minHoursField, minMinutesField, maxHoursField, maxMinutesField;

    private ServiceCreateViewModel viewModel;
    private boolean isEditMode = false;

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

        durationHoursField = view.findViewById(R.id.durationHours);
        durationMinutesField = view.findViewById(R.id.durationMinutes);
        minHoursField = view.findViewById(R.id.minHours);
        minMinutesField = view.findViewById(R.id.minMinutes);
        maxHoursField = view.findViewById(R.id.maxHours);
        maxMinutesField = view.findViewById(R.id.maxMinutes);

        // Provera da li smo u edit modu
        Service existing = viewModel.getService().getValue();
        if (existing != null && existing.getId() != null) {
            isEditMode = true;
            prefillFields(existing);
        }

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

        layoutDuration.setOnClickListener(v -> {
            resetSelections();
            layoutDuration.setBackgroundResource(R.drawable.background_selected);
            automaticButton.setChecked(true);
            automaticButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            manualButton.setChecked(false);
            manualButton.setEnabled(false);
        });

        layoutRange.setOnClickListener(v -> {
            resetSelections();
            layoutRange.setBackgroundResource(R.drawable.background_selected);
            manualButton.setEnabled(true);
        });

        view.findViewById(R.id.save_button).setOnClickListener(v -> handleSave(view));

        return view;
    }

    private void prefillFields(Service existing) {
        // Popuni vreme angažovanja
        if (existing.getMinEngagement() != null && existing.getMaxEngagement() != null) {
            if (existing.getMaxEngagement() == 0) {
                // Jedna vrednost
                layoutDuration.performClick();
                int hours = existing.getMinEngagement() / 60;
                int minutes = existing.getMinEngagement() % 60;
                durationHoursField.setText(String.valueOf(hours));
                durationMinutesField.setText(String.valueOf(minutes));
            } else {
                // Range
                layoutRange.performClick();
                int minHours = existing.getMinEngagement() / 60;
                int minMinutes = existing.getMinEngagement() % 60;
                int maxHours = existing.getMaxEngagement() / 60;
                int maxMinutes = existing.getMaxEngagement() % 60;
                minHoursField.setText(String.valueOf(minHours));
                minMinutesField.setText(String.valueOf(minMinutes));
                maxHoursField.setText(String.valueOf(maxHours));
                maxMinutesField.setText(String.valueOf(maxMinutes));
            }
        }

        // Deadline & Cancellation
        if (existing.getReservationDeadLine() != null)
            deadlineField.setText(String.valueOf(existing.getReservationDeadLine()));
        if (existing.getCancellationPeriod() != null)
            cancellationField.setText(String.valueOf(existing.getCancellationPeriod()));

        // Automatizacija
        if (existing.getAutomation() != null) {
            automaticButton.setChecked(existing.getAutomation());
            manualButton.setChecked(!existing.getAutomation());
        }
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
            int hours = parseOrZero(durationHoursField.getText().toString());
            int minutes = parseOrZero(durationMinutesField.getText().toString());
            service.setMinEngagement((hours * 60) + minutes);
            service.setMaxEngagement(0);

        } else if (isRangeSelected) {
            int minHours = parseOrZero(minHoursField.getText().toString());
            int minMinutes = parseOrZero(minMinutesField.getText().toString());
            int maxHours = parseOrZero(maxHoursField.getText().toString());
            int maxMinutes = parseOrZero(maxMinutesField.getText().toString());

            int totalMin = (minHours * 60) + minMinutes;
            int totalMax = (maxHours * 60) + maxMinutes;

            if (totalMin >= totalMax) {
                Toast.makeText(getContext(), "Min duration must be less than max duration", Toast.LENGTH_SHORT).show();
                return;
            }

            service.setMinEngagement(totalMin);
            service.setMaxEngagement(totalMax);


        }
        AuthManager auth = AuthManager.getInstance(requireContext());
        String email = auth.getEmail();
        service.setProvider(email);
        // Deadline i otkazni rok
        service.setReservationDeadLine(parseOrZero(deadlineField.getText().toString()));
        service.setCancellationPeriod(parseOrZero(cancellationField.getText().toString()));

        // Automatizacija
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
        ServiceService serviceService = RetrofitClient.serviceService;

        if (isEditMode) {
            //  EDIT PUT request
            serviceService.updateService(serviceToSave.getId(), serviceDTO).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Service updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to update service", Toast.LENGTH_SHORT).show();
                    }
                    navigateToList();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // CREATE POST request
            serviceService.createService(serviceDTO).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Service created successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to create service", Toast.LENGTH_SHORT).show();
                    }
                    navigateToList();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void navigateToList() {
        NavController navController = ((MainActivity) getActivity()).getNavController();
        navController.navigate(R.id.servicesViewFragment);
    }
}
