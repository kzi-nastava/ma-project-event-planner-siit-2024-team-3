package com.example.eveant.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.eveant.eventType.EventType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.model.OfferStatus;
import com.example.eveant.service.model.Service;
import com.example.eveant.user.security.AuthManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServicesViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private List<Service> allServices = new ArrayList<>();
    private List<Service> filteredServices = new ArrayList<>();
    private EditText searchInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_services_view, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_services);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ServiceAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        searchInput = view.findViewById(R.id.search_);
        RelativeLayout filterBtn = view.findViewById(R.id.filter_button);

        // Load services
        fetchProviderServices();

        // 🔍 Search po imenu
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterServices(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 📊 Filter bottom sheet (možeš iskoristiti stari kod)
        filterBtn.setOnClickListener(v -> showFilterBottomSheet());

        return view;
    }

    private void fetchProviderServices() {
//        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
//        String username = prefs.getString("email", "");
//
//        if(username==null || username.isEmpty()){
//            username="provider";
//        }
//        Log.d( "fetchProviderServices: ", username);

        AuthManager auth = AuthManager.getInstance(requireContext());
        String email = "networkingProvider";

        RetrofitClient.serviceService.getAllServicesForProvider(email).enqueue(new Callback<ArrayList<Service>>() {
            @Override
            public void onResponse(Call<ArrayList<Service>> call, Response<ArrayList<Service>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allServices.clear();
                    allServices.addAll(response.body());

                    allServices.removeIf(s -> s.getStatus() == OfferStatus.DELETED);

                    filteredServices = new ArrayList<>(allServices);
                    adapter.updateData(filteredServices);
                } else {
                    Toast.makeText(getContext(), "Failed to load provider services", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Service>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterServices(String query) {
        filteredServices = allServices.stream()
                .filter(s -> s.getName() != null && s.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        adapter.updateData(filteredServices);
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.filter_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        LinearLayout categoryContainer = bottomSheetView.findViewById(R.id.category_container);
        LinearLayout eventTypeContainer = bottomSheetView.findViewById(R.id.event_type_container);
        SeekBar priceSeekBar = bottomSheetView.findViewById(R.id.price_seekbar);
        TextView currentPriceText = bottomSheetView.findViewById(R.id.current_price_text);
        CheckBox availabilityCheckbox = bottomSheetView.findViewById(R.id.availability_checkbox);
        Button applyFiltersButton = bottomSheetView.findViewById(R.id.apply_filters_button);

        // 🔹 1. Dinamičko popunjavanje kategorija
        List<String> categoryNames = allServices.stream()
                .map(s -> s.getCategory() != null ? s.getCategory().getName() : "")
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        List<CheckBox> categoryCheckBoxes = new ArrayList<>();
        for (String category : categoryNames) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(category);
            checkBox.setTextColor(getResources().getColor(R.color.black));
            categoryContainer.addView(checkBox);
            categoryCheckBoxes.add(checkBox);
        }

        // 🔹 2. Dinamičko popunjavanje tipova eventa
        List<String> eventTypeNames = allServices.stream()
                .flatMap(s -> s.getEventTypes() != null ? s.getEventTypes().stream() : new ArrayList<>().stream())
                .map(et -> ((EventType) et).getName())
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        List<CheckBox> eventTypeCheckBoxes = new ArrayList<>();
        for (String eventType : eventTypeNames) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(eventType);
            checkBox.setTextColor(getResources().getColor(R.color.black));
            eventTypeContainer.addView(checkBox);
            eventTypeCheckBoxes.add(checkBox);
        }

        // 🔹 3. Slajder cene – prikaži trenutnu vrednost
        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentPriceText.setText("Price to: " + progress + " €");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 🔹 4. Primeni filtere
        applyFiltersButton.setOnClickListener(v -> {
            int maxPrice = priceSeekBar.getProgress();
            boolean onlyAvailable = availabilityCheckbox.isChecked();

            List<String> selectedCategories = categoryCheckBoxes.stream()
                    .filter(CheckBox::isChecked)
                    .map(CheckBox::getText)
                    .map(CharSequence::toString)
                    .collect(Collectors.toList());

            List<String> selectedEventTypes = eventTypeCheckBoxes.stream()
                    .filter(CheckBox::isChecked)
                    .map(CheckBox::getText)
                    .map(CharSequence::toString)
                    .collect(Collectors.toList());

            // 🔎 Filtriranje
            filteredServices = allServices.stream()
                    .filter(s -> s.getName() != null) // sigurnost
                    .filter(s -> selectedCategories.isEmpty() ||
                            (s.getCategory() != null && selectedCategories.contains(s.getCategory().getName())))
                    .filter(s -> selectedEventTypes.isEmpty() ||
                            (s.getEventTypes() != null && s.getEventTypes().stream()
                                    .anyMatch(et -> selectedEventTypes.contains(et.getName()))))
                    .filter(s -> maxPrice == 0 || (s.getPrice() != null && s.getPrice() <= maxPrice))
                    .filter(s -> !onlyAvailable || s.getStatus() == OfferStatus.AVAILABLE)
                    .collect(Collectors.toList());

            adapter.updateData(filteredServices);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

}
