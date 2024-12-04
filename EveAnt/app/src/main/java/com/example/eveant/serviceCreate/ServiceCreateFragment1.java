package com.example.eveant.serviceCreate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

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

public class ServiceCreateFragment1 extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_create1, container, false);

        ToggleButton availableButton = view.findViewById(R.id.availableButton);
        ToggleButton unavailableButton = view.findViewById(R.id.unavailableButton);
        NavController navController = ((MainActivity) getActivity()).getNavController();
        availableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    unavailableButton.setChecked(false);
                    availableButton.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                    unavailableButton.setTextColor(ContextCompat.getColor(getContext(),R.color.black));
                }
            }
        });

        unavailableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    availableButton.setChecked(false);
                    availableButton.setTextColor(ContextCompat.getColor(getContext(),R.color.black));
                    unavailableButton.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
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
                    hiddenButton.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                    visibleButton.setTextColor(ContextCompat.getColor(getContext(),R.color.black));
                }
            }
        });

        visibleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hiddenButton.setChecked(false);
                    hiddenButton.setTextColor(ContextCompat.getColor(getContext(),R.color.black));
                    visibleButton.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                }
            }
        });

        view.findViewById(R.id.next_button).setOnClickListener(v -> {
            navController.navigate(R.id.serviceCreateFragment2);
        });


        CheckBox checkBoxNewCategory = view.findViewById(R.id.checkbox_new_category);
        EditText newCategoryInput = view.findViewById(R.id.new_category_input);
        Spinner categorySpinner = view.findViewById(R.id.category_spinner);

        checkBoxNewCategory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Prikaži polje za unos nove kategorije
                newCategoryInput.setVisibility(View.VISIBLE);
                categorySpinner.setVisibility(View.GONE);
                categorySpinner.setEnabled(false); // Onemogući Spinner
            } else {
                // Sakrij polje za unos nove kategorije
                newCategoryInput.setVisibility(View.GONE);
                categorySpinner.setVisibility(View.VISIBLE);
                categorySpinner.setEnabled(true); // Omogući Spinner
            }
        });

        /*Spinner categorySpinner = view.findViewById(R.id.category_spinner);*/
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set the default hint for Spinner to be removed once selection is made
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get selected category
                String selectedCategory = parentView.getItemAtPosition(position).toString();

                // Make "Select Category" disappear after selection
                if (position != 0) {  // Position 0 is assumed to be "Select Category"
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