package com.example.eveant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView;

public class ProductCreateFragment1 extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_create1, container, false);

        ToggleButton availableButton = view.findViewById(R.id.availableButton);
        ToggleButton unavailableButton = view.findViewById(R.id.unavailableButton);

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
            ((ProductCreateActivity) requireActivity()).replaceFragment(new ProductCreateFragment2());
        });

        Spinner categorySpinner = view.findViewById(R.id.category_spinner);
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

        Button buttonShowCheckboxes=view.findViewById(R.id.buttonShowCheckboxes);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(),
                R.array.event_types, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        buttonShowCheckboxes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCheckboxDialog();
            }
        });

        return view;
    }
    private void showCheckboxDialog() {
        // Kreiranje CheckBox-ova
        final String[] events = {"Event 1", "Event 2", "Event 3", "Event 4"};
        boolean[] checkedItems = {false, false, false, false}; // Početno stanje checkbox-ova

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Events");

        // Postavljanje checkbox-a u dijalog
        builder.setMultiChoiceItems(events, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // Možeš obraditi klikove na checkbox-ove ovde ako je potrebno
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Prikazivanje selektovanih događaja
                StringBuilder selectedEvents = new StringBuilder();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        selectedEvents.append(events[i]).append("\n");
                    }
                }
                Toast.makeText(getContext(), "Selected Events:\n" + selectedEvents.toString(), Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }
}