package com.example.eveant.service.serviceDetails;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eveant.R;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.model.Service;

public class ServiceDetails extends Fragment {

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_service_details, container, false);
        ServiceCreateViewModel viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);

        ImageView btnAddToFavourites = view.findViewById(R.id.favourite);
        Button btnBuyProduct = view.findViewById(R.id.btn_buy_product);
        Button btnProviderInfo = view.findViewById(R.id.btn_provider_info);
        Button btnCompanyInfo = view.findViewById(R.id.btn_company_info);


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

        Service service = viewModel.getService().getValue();

        TextView serviceName=view.findViewById(R.id.serviceName);
        TextView serviceCategory=view.findViewById(R.id.serviceCategory);
        TextView description=view.findViewById(R.id.description);
        TextView specification=view.findViewById(R.id.specification);
        TextView newPrice =view.findViewById(R.id.newPrice);
        TextView oldPrice=view.findViewById(R.id.oldPrice);
        TextView discountBadge=view.findViewById(R.id.discountBadge);

        serviceName.setText(service.getName());
        serviceCategory.setText(service.getCategory().getName());
        description.setText(service.getDescription());
        specification.setText(service.getSpecification());
        oldPrice.setText(service.getPrice().toString());
        discountBadge.setText(String.valueOf(service.getDiscount()));
        newPrice.setText(String.valueOf((int) (service.getPrice()-(service.getPrice()*service.getDiscount()/100))));

        return view;
    }
}