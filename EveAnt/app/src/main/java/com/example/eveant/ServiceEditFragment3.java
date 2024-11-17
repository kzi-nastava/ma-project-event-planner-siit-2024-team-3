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
import android.widget.LinearLayout;
import android.widget.ToggleButton;


public class ServiceEditFragment3 extends Fragment {

    private LinearLayout layoutDuration;
    private LinearLayout layoutRange;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_edit3, container, false);

        // Dugme za čuvanje podataka
        view.findViewById(R.id.save_button).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ServicesViewActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.previous_button).setOnClickListener(v -> {
            ((ServiceEditActivity) requireActivity()).replaceFragment(new ServiceEditFragment2());
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

        // Inicijalizacija layout-ova
        layoutDuration = view.findViewById(R.id.layoutDuration);
        layoutRange = view.findViewById(R.id.layoutRange);

        // Postavljanje klik listener-a
        layoutDuration.setOnClickListener(v -> {
            resetSelections();
            layoutDuration.setBackgroundResource(R.drawable.background_selected);
        });

        layoutRange.setOnClickListener(v -> {
            resetSelections();
            layoutRange.setBackgroundResource(R.drawable.background_selected);
        });

        return view;
    }

    // Ispravno definisana metoda za resetovanje selekcije
    private void resetSelections() {
        layoutDuration.setBackgroundResource(R.drawable.background_unselected);
        layoutRange.setBackgroundResource(R.drawable.background_unselected);
    }
}