package com.example.eveant.budget;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.model.EventType;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateBudget1 extends Fragment {

    private Spinner eventTypeSpinner;
    private Button nextButton;
    List<String> eventTypeNames = new ArrayList<>();
    private List<EventType> eventTypes = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_budget1, container, false);

        eventTypeSpinner = view.findViewById(R.id.event_type);
        nextButton = view.findViewById(R.id.next_button);

        loadEventTypes();

        nextButton.setOnClickListener(v -> {
            int selectedPosition = eventTypeSpinner.getSelectedItemPosition();
            if (selectedPosition != Spinner.INVALID_POSITION) {

                EventType selectedEventType = eventTypes.get(selectedPosition);

                Bundle bundle = new Bundle();
                bundle.putSerializable("eventType", selectedEventType);

                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.action_createBudget1_to_createBudget, bundle);
            }
        });



        return view;
    }

    private void loadEventTypes() {
        RetrofitClient.eventTypeService.getEventTypes().enqueue(new Callback<List<EventType>>() {
            @Override
            public void onResponse(Call<List<EventType>> call, Response<List<EventType>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.clear();
                    for (EventType event : response.body()) {
                        if (event.isActive()) {
                            Log.i("EVENT Ovde", event.getName());
                            Log.i("event kategorij koje mi trebaju",event.getSuggestedCategories().toString());
                            eventTypes.add(event);
                        }
                    }

                    List<String> eventTypeNames = new ArrayList<>();
                    for (EventType event : eventTypes) {
                        eventTypeNames.add(event.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            eventTypeNames
                    );
                    eventTypeSpinner.setAdapter(adapter);
                } else {
                    Log.e("EventType", "Greška u odgovoru: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EventType>> call, Throwable t) {
                Log.e("EventType", "Neuspešan poziv", t);
            }
        });
    }

}
