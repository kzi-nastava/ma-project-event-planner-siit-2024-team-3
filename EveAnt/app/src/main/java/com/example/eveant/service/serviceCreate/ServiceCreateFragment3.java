package com.example.eveant.service.serviceCreate;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.ServiceService;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.model.Service;
import com.example.eveant.service.model.ServiceDTO;
import com.example.eveant.service.model.ServiceMapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceCreateFragment3 extends Fragment {

    private LinearLayout layoutDuration;
    private LinearLayout layoutRange;
    private ServiceCreateViewModel viewModel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_create3, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);

        view.findViewById(R.id.previous_button).setOnClickListener(v -> {
            NavController navController = ((MainActivity) getActivity()).getNavController();
            navController.navigate(R.id.serviceCreateFragment2);
        });

        ToggleButton manualButton = view.findViewById(R.id.manualButton);
        ToggleButton automaticButton = view.findViewById(R.id.automaticButton);

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

        layoutDuration = view.findViewById(R.id.layoutDuration);
        layoutRange = view.findViewById(R.id.layoutRange);

        layoutDuration.setOnClickListener(v -> {
            resetSelections();
            layoutDuration.setBackgroundResource(R.drawable.background_selected);
        });

        layoutRange.setOnClickListener(v -> {
            resetSelections();
            layoutRange.setBackgroundResource(R.drawable.background_selected);
        });


        EditText reservationPeriod = view.findViewById(R.id.deadline);
        EditText cancellationPeriod = view.findViewById(R.id.cacnellationPeriod);


        view.findViewById(R.id.save_button).setOnClickListener(v -> {
            Service service = viewModel.getService().getValue();
            if (service != null) {
                if (layoutDuration.getBackground().getConstantState().equals(
                        ContextCompat.getDrawable(requireContext(), R.drawable.background_selected).getConstantState())) {

                    EditText durationHoursField = view.findViewById(R.id.durationHours);
                    EditText durationMinutesField = view.findViewById(R.id.durationMinutes);

                    String strHours = durationHoursField.getText().toString();
                    String strMinutes = durationMinutesField.getText().toString();

                    int hours = strHours.isEmpty() ? 0 : Integer.parseInt(strHours);
                    int minutes = strMinutes.isEmpty() ? 0 : Integer.parseInt(strMinutes);
                    int totalMinutes = (hours * 60) + minutes;

                    service.setMinEngagement(totalMinutes);
                    service.setMaxEngagement(0);
                } else if (layoutRange.getBackground().getConstantState().equals(
                        ContextCompat.getDrawable(requireContext(), R.drawable.background_selected).getConstantState())) {

                    EditText minHours = view.findViewById(R.id.minHours);
                    EditText minMinutes = view.findViewById(R.id.minMinutes);
                    EditText maxHours = view.findViewById(R.id.maxHours);
                    EditText maxMinutes = view.findViewById(R.id.maxMinutes);

                    String strHours = minHours.getText().toString();
                    String strMinutes = minMinutes.getText().toString();

                    int hours = strHours.isEmpty() ? 0 : Integer.parseInt(strHours);
                    int minutes = strMinutes.isEmpty() ? 0 : Integer.parseInt(strMinutes);
                    int totalMinutesMin = (hours * 60) + minutes;

                    String strHoursMax = maxHours.getText().toString();
                    String strMinutesMax = maxMinutes.getText().toString();

                    int hoursMax = strHoursMax.isEmpty() ? 0 : Integer.parseInt(strHoursMax);
                    int minutesMax = strMinutesMax.isEmpty() ? 0 : Integer.parseInt(strMinutesMax);
                    int totalMinutesMax = (hoursMax * 60) + minutesMax;

                    service.setMinEngagement(totalMinutesMin);
                    service.setMaxEngagement(totalMinutesMax);
                }

                String strDeadline = reservationPeriod.getText().toString();
                int serviceDeadline = strDeadline.isEmpty() ? 0 : Integer.parseInt(strDeadline);
                service.setReservationDeadLine(serviceDeadline);

                String strCancellation = cancellationPeriod.getText().toString();
                int serviceCancellation = strCancellation.isEmpty() ? 0 : Integer.parseInt(strCancellation);
                service.setCancellationPeriod(serviceCancellation);

                service.setAutomation(automaticButton.isChecked());

                viewModel.updateService(service);
                saveService();
            }
        });


        return view;
    }

    private void resetSelections() {
        layoutDuration.setBackgroundResource(R.drawable.background_unselected);
        layoutRange.setBackgroundResource(R.drawable.background_unselected);
    }

    private void saveService() {
        Service serviceToSave = viewModel.getService().getValue();

        ServiceDTO serviceDTO = ServiceMapper.INSTANCE.toDTO(serviceToSave);

        Log.d(TAG, "saveService: "+serviceDTO.getPrice());
        if (serviceDTO != null) {
            ServiceService serviceService = RetrofitClient.serviceService;
            Log.d("ServiceToSave", "Service to save: " + serviceDTO.toString());

            serviceService.createService(serviceDTO).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        NavController navController = ((MainActivity) getActivity()).getNavController();
                        navController.navigate(R.id.actionCreateFragment_toViewServices);
                    } else {
                        Log.e("SaveService", "Failed to save service. Response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("SaveService", "Error occurred: " + t.getMessage());
                }
            });
        }
    }
}
