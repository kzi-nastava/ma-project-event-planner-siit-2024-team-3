package com.example.eveant;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class OrganizerProviderFragment extends Fragment {

    ToggleButton organizerButton, providerButton;
    Button goToAddress, goToHome, goToCompany;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_provider, container, false);
        organizerButton = view.findViewById(R.id.organizerButton);
        providerButton = view.findViewById(R.id.providerButton);
        goToHome = view.findViewById(R.id.goToHome);  // Ensure this ID matches the button in your XML
        goToAddress = view.findViewById(R.id.goToAddress);

        organizerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    providerButton.setChecked(false);
                    goToHome.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }
        });

        providerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    organizerButton.setChecked(false);
                    goToHome.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.container, new CompanyFragment1());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    });
                }
            }
        });
        goToAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new AddressFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }
}