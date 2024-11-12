package com.example.eveant;

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
        /*Spinner categorySpinner = view.findViewById(R.id.category_spinner);
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
                    categorySpinner.setPrompt("Category Selected");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Optionally handle this case if nothing is selected
            }
        });*/

        return view;
    }
}