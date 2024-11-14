package com.example.eveant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class AddressFragment extends Fragment {
    private EditText country, city, street, postalNumber;
    private Button goToAreYou, goToYourself;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);

        country = view.findViewById(R.id.country);
        city = view.findViewById(R.id.city);
        street = view.findViewById(R.id.street);
        postalNumber = view.findViewById(R.id.postalNumber);
        goToAreYou = view.findViewById(R.id.goToAreYou);
        goToYourself = view.findViewById(R.id.goToYourself);


        goToAreYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new OrganizerProviderFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        goToYourself.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new PersonalInfoFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }
}