package com.example.eveant.service.serviceCreate;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.CategoryDTO;
import com.example.eveant.service.model.CategoryStatus;
import com.example.eveant.service.model.EventType;
import com.example.eveant.service.model.EventTypeDTO;
import com.example.eveant.service.model.OfferStatus;
import com.example.eveant.service.model.Service;
import com.example.eveant.service.model.ServiceDTO;
import com.example.eveant.service.model.ServiceMapper;
import com.example.eveant.service.model.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.List;

public class ServiceCreateFragment1 extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_create1, container, false);
        ServiceCreateViewModel viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);

        Service current = viewModel.getService().getValue();
        TextView name = view.findViewById(R.id.name);
        EditText price = view.findViewById(R.id.price);
        EditText discount = view.findViewById(R.id.discount);
        if (current != null) {
            if (current.getName() != null) name.setText(current.getName());
            if (current.getPrice() != null) price.setText(String.valueOf(current.getPrice()));
            if (current.getDiscount() != null) discount.setText(String.valueOf(current.getDiscount()));
        }

        ToggleButton availableButton = view.findViewById(R.id.availableButton);
        ToggleButton unavailableButton = view.findViewById(R.id.unavailableButton);

        availableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    unavailableButton.setChecked(false);
                    availableButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    unavailableButton.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                }
            }
        });

        unavailableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    availableButton.setChecked(false);
                    availableButton.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                    unavailableButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                }
            }
        });

        ToggleButton hiddenButton = view.findViewById(R.id.hiddenButton);
        ToggleButton visibleButton = view.findViewById(R.id.visibleButton);

        hiddenButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    visibleButton.setChecked(false);
                    hiddenButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    visibleButton.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                }
            }
        });

        visibleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hiddenButton.setChecked(false);
                    hiddenButton.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                    visibleButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                }
            }
        });

        Button buttonSelectEventTypes = view.findViewById(R.id.buttonSelectEventTypes);
        TextView selectedEventsTextView = view.findViewById(R.id.selectedEventsTextView);


        buttonSelectEventTypes.setOnClickListener(v -> {
            List<EventType> eventTypes = viewModel.getEventTypesLiveData().getValue();
            if (eventTypes != null && !eventTypes.isEmpty()) {
                showEventTypeDialog(eventTypes, selectedEventsTextView, viewModel);
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle("No Event Types")
                        .setMessage("Event types are not loaded yet. Please try again later.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });


        viewModel.fetchEventTypes();


        view.findViewById(R.id.next_button).setOnClickListener(v -> {
            NavController navController = ((MainActivity) getActivity()).getNavController();
            navController.navigate(R.id.serviceCreateFragment2);
        });


        CheckBox checkBoxNewCategory = view.findViewById(R.id.checkbox_new_category);
        EditText newCategoryInput = view.findViewById(R.id.new_category_input);
        Spinner categorySpinner = view.findViewById(R.id.category_spinner);

        checkBoxNewCategory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                newCategoryInput.setVisibility(View.VISIBLE);
                categorySpinner.setVisibility(View.GONE);
                categorySpinner.setEnabled(false);
            } else {
                newCategoryInput.setVisibility(View.GONE);
                categorySpinner.setVisibility(View.VISIBLE);
                categorySpinner.setEnabled(true);
            }
        });



        viewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                List<String> categoryNames = new ArrayList<>();
                for (Category category : categories) {
                    categoryNames.add(category.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_spinner_item,
                        categoryNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                categorySpinner.setAdapter(adapter);

                Toast.makeText(getContext(), "Loaded " + categoryNames.size() + " categories", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No categories loaded", Toast.LENGTH_SHORT).show();
            }
        });


        viewModel.fetchCategories();
        viewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                Log.d("CATEGORIES_RESPONSE", "Dobavljene kategorije: " + categories.size());
                for (Category c : categories) {
                    Log.d("CATEGORIES_RESPONSE", c.toString());
                }
            } else {
                Log.d("CATEGORIES_RESPONSE", "Nema dobavljenih kategorija (null).");
            }
        });

        viewModel.getEventTypesLiveData().observe(getViewLifecycleOwner(), eventTypes -> {
            if (eventTypes != null) {
                Log.d("EVENTTYPES_RESPONSE", "Dobavljeni event types: " + eventTypes.size());
                for (EventType e : eventTypes) {
                    Log.d("EVENTTYPES_RESPONSE", e.getName() + " (id=" + e.getId() + ")");
                }
            } else {
                Log.d("EVENTTYPES_RESPONSE", "Nema dobavljenih event types (null).");
            }
        });


        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCategory = parentView.getItemAtPosition(position).toString();

                if (position != 0) {
                    categorySpinner.setPrompt("Select category");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Optionally handle this case if nothing is selected
            }
        });



        final Spinner category = view.findViewById(R.id.category_spinner);




        view.findViewById(R.id.next_button).setOnClickListener(v -> {
            if (!validateFields((EditText) name, price, discount)) return;


            Service service = viewModel.getService().getValue();

            ServiceDTO serviceDTO = ServiceMapper.INSTANCE.toDTO(service);

            String serviceName = name.getText().toString();
            serviceDTO.setName(serviceName);

            if (checkBoxNewCategory.isChecked()) {
                String newCategory = newCategoryInput.getText().toString();
                CategoryDTO cat=new CategoryDTO();
                cat.setName(newCategory);
                cat.setStatus(CategoryStatus.SUGGESTED);
                cat.setCreatedBy("provider"); /*TODO da bude automatski ulogovani korisnik*/
                cat.setDescription("");
                serviceDTO.setCategory(cat);
            } else {
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                CategoryDTO cat=new CategoryDTO();
                cat.setName(selectedCategory);
                serviceDTO.setCategory(cat);
            }

            List<EventType> selectedEventTypes = viewModel.getSelectedEventTypes().getValue();
            if (selectedEventTypes != null && !selectedEventTypes.isEmpty()) {
                List<EventTypeDTO> eventTypeDTOs = new ArrayList<>();
                for (EventType et : selectedEventTypes) {
                    EventTypeDTO dto = new EventTypeDTO();
                    dto.setName(et.getName());
                    eventTypeDTOs.add(dto);
                }
                serviceDTO.setEventTypes(eventTypeDTOs);
            }


            if(checkBoxNewCategory.isChecked()){
                serviceDTO.setStatus(OfferStatus.PENDING);
            } else if (availableButton.isChecked()) {
                serviceDTO.setStatus(OfferStatus.AVAILABLE);
            } else {
                serviceDTO.setStatus(OfferStatus.UNAVAILABLE);
            }

            serviceDTO.setVisible(visibleButton.isChecked());


            String strPrice = price.getText().toString();
            Long servicePrice = strPrice.isEmpty() ? 0:Long.parseLong(strPrice);

            serviceDTO.setPrice(servicePrice);

            String strDisc = discount.getText().toString();
            Integer serviceDiscount = strDisc.isEmpty() ? 0:Integer.parseInt(strDisc);

            serviceDTO.setDiscount(serviceDiscount);

            Service service1 = ServiceMapper.INSTANCE.toEntity(serviceDTO);
            viewModel.updateService(service1);

            NavController navController = ((MainActivity) getActivity()).getNavController();
            navController.navigate(R.id.serviceCreateFragment2);
        });

        setupValidation((EditText) name, price, discount);

        return view;
    }

    private void showCheckboxDialog(TextView selectedEventsTextView) {
        final String[] events = {"Event 1", "Event 2", "Event 3", "Event 4"};
        boolean[] checkedItems = {false, false, false, false};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Events");

        builder.setMultiChoiceItems(events, checkedItems, null);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringBuilder selectedEvents = new StringBuilder();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        selectedEvents.append(events[i]).append("\n");
                    }
                }
                if (selectedEvents.length() > 0) {
                    selectedEventsTextView.setText(selectedEvents.toString().trim());
                    selectedEventsTextView.setVisibility(View.VISIBLE);
                } else {
                    selectedEventsTextView.setVisibility(View.GONE);
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showEventTypeDialog(List<EventType> eventTypes, TextView selectedEventsTextView, ServiceCreateViewModel viewModel) {
        boolean[] checkedItems = new boolean[eventTypes.size()];
        String[] eventNames = new String[eventTypes.size()];
        for (int i = 0; i < eventTypes.size(); i++) {
            eventNames[i] = eventTypes.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Event Types");

        builder.setMultiChoiceItems(eventNames, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            List<EventType> selectedTypes = new ArrayList<>();
            StringBuilder selectedNames = new StringBuilder();
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    selectedTypes.add(eventTypes.get(i));
                    selectedNames.append(eventTypes.get(i).getName()).append("\n");
                }
            }
            if (!selectedTypes.isEmpty()) {
                selectedEventsTextView.setText(selectedNames.toString().trim());
                selectedEventsTextView.setVisibility(View.VISIBLE);
                viewModel.updateSelectedEventTypes(selectedTypes);
            } else {
                selectedEventsTextView.setVisibility(View.GONE);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void setupValidation(EditText name, EditText price, EditText discount) {
        name.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    name.setError("Name is required");
                } else {
                    name.setError(null);
                }
            }

        });

        price.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    long val = Long.parseLong(s.toString());
                    if (val < 0) {
                        price.setError("Price must be >= 0");
                    } else {
                        price.setError(null);
                    }
                } catch (NumberFormatException e) {
                    price.setError("Invalid price");
                }
            }
        });

        discount.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int val = Integer.parseInt(s.toString());
                    if (val < 0 || val > 100) {
                        discount.setError("Discount must be between 0 and 100");
                    } else {
                        discount.setError(null);
                    }
                } catch (NumberFormatException e) {
                    discount.setError("Invalid discount");
                }
            }
        });
    }

    private boolean validateFields(EditText name, EditText price, EditText discount) {
        boolean valid = true;

        if (name.getText().toString().trim().isEmpty()) {
            name.setError("Name is required");
            valid = false;
        }

        try {
            long p = Long.parseLong(price.getText().toString());
            if (p < 0) {
                price.setError("Price must be >= 0");
                valid = false;
            }
        } catch (NumberFormatException e) {
            price.setError("Invalid price");
            valid = false;
        }

        try {
            int d = Integer.parseInt(discount.getText().toString());
            if (d < 0 || d > 100) {
                discount.setError("Discount must be between 0 and 100");
                valid = false;
            }
        } catch (NumberFormatException e) {
            discount.setError("Invalid discount");
            valid = false;
        }

        return valid;
    }


}