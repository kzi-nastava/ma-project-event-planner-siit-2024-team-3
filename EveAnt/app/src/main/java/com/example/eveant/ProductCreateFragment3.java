package com.example.eveant;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ProductCreateFragment3 extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_create3, container, false);

        view.findViewById(R.id.previous_button).setOnClickListener(v -> {
            ((ProductCreateActivity) requireActivity()).replaceFragment(new ProductCreateFragment1());
        });

        view.findViewById(R.id.next_button).setOnClickListener(v -> {
            ((ProductCreateActivity) requireActivity()).replaceFragment(new ProductCreateFragment2());
        });

        return view;
    }



}