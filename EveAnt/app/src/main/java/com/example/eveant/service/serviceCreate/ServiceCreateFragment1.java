package com.example.eveant.service.serviceCreate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.eventType.EventType;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.OfferStatus;
import com.example.eveant.service.model.Service;
import com.example.eveant.service.model.ServiceMapper;

import java.util.ArrayList;
import java.util.List;

public class ServiceCreateFragment1 extends Fragment {

    private ServiceCreateViewModel viewModel;
    private EditText nameInput, priceInput, discountInput, newCategoryInput;
    private Spinner categorySpinner;
    private CheckBox newCategoryCheckBox;
    private ToggleButton availableBtn, unavailableBtn, visibleBtn, hiddenBtn;
    private TextView selectedEventsText;
    private List<EventType> selectedEventTypes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_create1, container, false);

        // Init UI
        nameInput = view.findViewById(R.id.name);
        priceInput = view.findViewById(R.id.price);
        discountInput = view.findViewById(R.id.discount);
        newCategoryInput = view.findViewById(R.id.new_category_input);
        categorySpinner = view.findViewById(R.id.category_spinner);
        newCategoryCheckBox = view.findViewById(R.id.checkbox_new_category);
        availableBtn = view.findViewById(R.id.availableButton);
        unavailableBtn = view.findViewById(R.id.unavailableButton);
        visibleBtn = view.findViewById(R.id.visibleButton);
        hiddenBtn = view.findViewById(R.id.hiddenButton);
        selectedEventsText = view.findViewById(R.id.selectedEventsTextView);
        Button chooseEventsBtn = view.findViewById(R.id.buttonShowCheckboxes);
        Button nextBtn = view.findViewById(R.id.next_button);



        // ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);

        // Availability toggle
        availableBtn.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                unavailableBtn.setChecked(false);
                availableBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                unavailableBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            }
        });
        unavailableBtn.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                availableBtn.setChecked(false);
                availableBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                unavailableBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            }
        });

        // Visibility toggle
        visibleBtn.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                hiddenBtn.setChecked(false);
                visibleBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                hiddenBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            }
        });
        hiddenBtn.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                visibleBtn.setChecked(false);
                hiddenBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                visibleBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            }
        });

        // Category handling
        newCategoryCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            newCategoryInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            categorySpinner.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        // Observe categories from backend
        viewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                List<String> names = new ArrayList<>();
                for (Category c : categories) names.add(c.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
            }
        });
        viewModel.fetchCategories();

        // Observe event types
        viewModel.getEventTypesLiveData().observe(getViewLifecycleOwner(), eventTypes -> {
            // prikazaće se kad korisnik klikne dugme
        });
        viewModel.fetchEventTypes();


        // Choose events dialog
        chooseEventsBtn.setOnClickListener(v -> showEventDialog());

        // Next button
        nextBtn.setOnClickListener(v -> handleNext());

        //Ako već postoji service u ViewModel-u (korisnik se vratio nazad), popuni polja
        Service existing = viewModel.getService().getValue();;
        if (existing != null && existing.getName() != null) {
                nameInput.setText(existing.getName());

            if (existing.getPrice() != null) {
                priceInput.setText(String.valueOf(existing.getPrice()));
            }

           /* if (existing.getDiscount() != null) {
                discountInput.setText(String.valueOf(existing.getDiscount()));
            }
*/
            // Category
            if (existing.getCategory() != null && existing.getCategory().getName() != null) {
                String existingCategoryName = existing.getCategory().getName();
                if (categorySpinner.getAdapter() != null) {
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
                    int pos = adapter.getPosition(existingCategoryName);
                    if (pos >= 0) {
                        categorySpinner.setSelection(pos);
                    } else {
                        // Ako kategorija nije u listi, verovatno je bila "nova kategorija"
                        newCategoryCheckBox.setChecked(true);
                        newCategoryInput.setText(existingCategoryName);
                    }
                }
            }

            // Status
            if (existing.getStatus() != null) {
                if (existing.getStatus() == OfferStatus.AVAILABLE) {
                    availableBtn.setChecked(true);
                } else {
                    unavailableBtn.setChecked(true);
                }
            }

            // Visibility
            if (existing.getVisible() != null) {
                if (existing.getVisible()) {
                    visibleBtn.setChecked(true);
                } else {
                    hiddenBtn.setChecked(true);
                }
            }

            // Event types
            if (existing.getEventTypes() != null && !existing.getEventTypes().isEmpty()) {
                selectedEventTypes.clear();
                selectedEventTypes.addAll(existing.getEventTypes());
                StringBuilder sb = new StringBuilder();
                for (EventType et : selectedEventTypes) {
                    sb.append(et.getName()).append("\n");
                }
                selectedEventsText.setText(sb.toString().trim());
                selectedEventsText.setVisibility(View.VISIBLE);
            }
        }


        return view;
    }

    private void handleNext() {
        String serviceName = nameInput.getText().toString().trim();
        String strPrice = priceInput.getText().toString().trim();
        String strDiscount = discountInput.getText().toString().trim();

        // Validations
        if (TextUtils.isEmpty(serviceName)) {
            Toast.makeText(getContext(), "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(strPrice)) {
            Toast.makeText(getContext(), "Price is required", Toast.LENGTH_SHORT).show();
            return;
        }
        long price = Long.parseLong(strPrice);
        if (price < 0) {
            Toast.makeText(getContext(), "Price cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }
        int discount = TextUtils.isEmpty(strDiscount) ? 0 : Integer.parseInt(strDiscount);
        if (discount < 0 || discount > 100) {
            Toast.makeText(getContext(), "Discount must be 0–100", Toast.LENGTH_SHORT).show();
            return;
        }
        String categoryName;
        if (newCategoryCheckBox.isChecked()) {
            categoryName = newCategoryInput.getText().toString().trim();
            if (categoryName.isEmpty()) {
                Toast.makeText(getContext(), "Enter new category", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (categorySpinner.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Select category", Toast.LENGTH_SHORT).show();
                return;
            }
            categoryName = categorySpinner.getSelectedItem().toString();
        }
        if (!availableBtn.isChecked() && !unavailableBtn.isChecked()) {
            Toast.makeText(getContext(), "Select availability", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!visibleBtn.isChecked() && !hiddenBtn.isChecked()) {
            Toast.makeText(getContext(), "Select visibility", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedEventTypes.isEmpty()) {
            Toast.makeText(getContext(), "Select at least one event type", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build service entity (mapped to DTO kasnije)
        Service service = new Service();
        service.setName(serviceName);
        service.setPrice(price);
        service.setDiscount(discount);
        service.setVisible(visibleBtn.isChecked());
        service.setStatus(availableBtn.isChecked() ? OfferStatus.AVAILABLE : OfferStatus.UNAVAILABLE);
        service.setCategory(new Category(categoryName));
        // Map event type IDs or names depending on backend – ovde name
        List<EventType> eventTypes = new ArrayList<>();
        for (EventType et : selectedEventTypes) {
            if (et.getId() != null) eventTypes.add(et);
        }
        service.setEventTypes(eventTypes);

        // Save to ViewModel
        viewModel.updateService(service);

        // Navigate
        NavController navController = ((MainActivity) getActivity()).getNavController();
        navController.navigate(R.id.serviceCreateFragment2);
    }

    private void showEventDialog() {
        List<EventType> allEvents = viewModel.getEventTypesLiveData().getValue();
        if (allEvents == null || allEvents.isEmpty()) {
            Toast.makeText(getContext(), "No event types available", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[allEvents.size()];
        boolean[] checked = new boolean[allEvents.size()];
        for (int i = 0; i < allEvents.size(); i++) {
            names[i] = allEvents.get(i).getName();
            checked[i] = selectedEventTypes.contains(allEvents.get(i));
        }
        new AlertDialog.Builder(getContext())
                .setTitle("Select Event Types")
                .setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> {
                    EventType et = allEvents.get(which);
                    if (isChecked) {
                        if (!selectedEventTypes.contains(et)) selectedEventTypes.add(et);
                    } else {
                        selectedEventTypes.remove(et);
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    StringBuilder sb = new StringBuilder();
                    for (EventType et : selectedEventTypes) {
                        sb.append(et.getName()).append("\n");
                    }
                    selectedEventsText.setText(sb.toString().trim());
                    selectedEventsText.setVisibility(sb.length() > 0 ? View.VISIBLE : View.GONE);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
