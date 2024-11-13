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
import android.widget.CompoundButton;
import android.widget.ToggleButton;


public class ProductCreateFragment2 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_create2, container, false);

        // Dugme za čuvanje podataka
        view.findViewById(R.id.save_button).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ServicesViewActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.previous_button).setOnClickListener(v -> {
            ((ProductCreateActivity) requireActivity()).replaceFragment(new ProductCreateFragment1());
        });

        ToggleButton manualButton = view.findViewById(R.id.manualButton);
        ToggleButton automaticButton = view.findViewById(R.id.automaticButton);

        manualButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    automaticButton.setChecked(false);
                    manualButton.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                    automaticButton.setTextColor(ContextCompat.getColor(getContext(),R.color.black));
                }
            }
        });

        automaticButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    manualButton.setChecked(false);
                    manualButton.setTextColor(ContextCompat.getColor(getContext(),R.color.black));
                    automaticButton.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                }
            }
        });

        return view;
    }
}