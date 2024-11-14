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

public class CompanyFragment1 extends Fragment {
    private Button goToAreYou, continueToCompany2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company_1, container, false);
        goToAreYou = view.findViewById(R.id.goToAreYou);
        continueToCompany2 = view.findViewById(R.id.continueToCompany2);

        goToAreYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new OrganizerProviderFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        continueToCompany2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new CompanyFragment2());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }
}