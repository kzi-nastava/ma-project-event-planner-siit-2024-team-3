package com.example.eveant.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eveant.R;
import com.example.eveant.service.serviceCreate.ServiceCreateFragment;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class OfferFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_offer, container, false);

        v.findViewById(R.id.createProductButton).setOnClickListener(b ->
                findNavController(this).navigate(R.id.action_offerFragment_to_createProductFragment));

        v.findViewById(R.id.createServiceButton).setOnClickListener(b ->
                findNavController(this).navigate(R.id.action_offerFragment_to_serviceCreateFragment1));

        return v;
    }
}

