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
import android.widget.ToggleButton;
import android.widget.AdapterView;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.OfferStatus;
import com.example.eveant.service.model.Service;
import com.example.eveant.service.model.ServiceDTO;
import com.example.eveant.service.model.ServiceMapper;

import java.util.ArrayList;
import java.util.List;

public class ServiceCreateFragment1 extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_create1, container, false);
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

        ServiceCreateViewModel viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);

        viewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                List<String> categoryNames = new ArrayList<>();
                for (Category category : categories) {
                    categoryNames.add(category.getName());
                }

                Log.d(TAG, "onCreateView: Dobavljene kategorije: " + categoryNames);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_spinner_item,
                        categoryNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                categorySpinner.setAdapter(adapter);
            }
        });

        viewModel.fetchCategories();

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

        TextView selectedEventsTextView = view.findViewById(R.id.selectedEventsTextView);

        Button buttonShowCheckboxes = view.findViewById(R.id.buttonShowCheckboxes);

        buttonShowCheckboxes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCheckboxDialog(selectedEventsTextView);
            }
        });

      /*  viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);
*/

        TextView name = view.findViewById(R.id.name);
        final Spinner category = view.findViewById(R.id.category_spinner);
        EditText price = view.findViewById(R.id.price);
        EditText discount = view.findViewById(R.id.discount);



        view.findViewById(R.id.next_button).setOnClickListener(v -> {
            Service service = viewModel.getService().getValue();

            ServiceDTO serviceDTO = ServiceMapper.INSTANCE.toDTO(service);

            String serviceName = name.getText().toString();
            serviceDTO.setName(serviceName);

            if (checkBoxNewCategory.isChecked()) {
                String newCategory = newCategoryInput.getText().toString();
                serviceDTO.setCategory(newCategory);
            } else {
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                serviceDTO.setCategory(selectedCategory);
            }

            /*tip usluga*/

            if (availableButton.isChecked()) {
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
}