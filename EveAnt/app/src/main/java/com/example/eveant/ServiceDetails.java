package com.example.eveant;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class ServiceDetails extends Fragment {

    private ImageView btnAddToFavourites;
    private Button btnBuyProduct;
    private Button btnProviderInfo;
    private Button btnCompanyInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_service_details, container, false);

        // Find buttons
        btnAddToFavourites = view.findViewById(R.id.favourite);
        btnBuyProduct = view.findViewById(R.id.btn_buy_product);
        btnProviderInfo = view.findViewById(R.id.btn_provider_info);
        btnCompanyInfo = view.findViewById(R.id.btn_company_info);

        // Set click listeners
        btnAddToFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Add to Favourites action
                Toast.makeText(getActivity(), "Added to Favourites", Toast.LENGTH_SHORT).show();
            }
        });

        btnBuyProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Buy Product action
                Toast.makeText(getActivity(), "Product Bought", Toast.LENGTH_SHORT).show();
            }
        });

        btnProviderInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Provider Info action
                Toast.makeText(getActivity(), "Provider Info", Toast.LENGTH_SHORT).show();
            }
        });

        btnCompanyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Company Info action
                Toast.makeText(getActivity(), "Company Info", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}