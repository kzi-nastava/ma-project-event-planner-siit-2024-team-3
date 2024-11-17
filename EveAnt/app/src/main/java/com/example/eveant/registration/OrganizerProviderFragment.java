package com.example.eveant.registration;

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

import com.example.eveant.R;
import com.example.eveant.ServicesViewActivity;

public class OrganizerProviderFragment extends Fragment {

    ToggleButton organizerButton, providerButton;
    Button goToBack, goToHome, goToNext;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_provider, container, false);
        organizerButton = view.findViewById(R.id.organizerButton);
        providerButton = view.findViewById(R.id.providerButton);
        goToBack = getActivity().findViewById(R.id.goToBack);
        goToNext = getActivity().findViewById(R.id.goToNext);

            organizerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    providerButton.setChecked(false);
                    goToNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ServicesViewActivity.class);
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
                    goToNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.container, new CompanyFragment1());
                            transaction.addToBackStack(null);
                            transaction.commit();
                            if (requireActivity() instanceof RegistrationActivity) {
                                ((RegistrationActivity) requireActivity()).updateProgress(5); // Replace '1' with the fragment index
                            }
                        }
                    });
                }
            }
        });

        if (goToBack != null) {
            goToBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, new AddressFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    if (requireActivity() instanceof RegistrationActivity) {
                        ((RegistrationActivity) requireActivity()).updateProgress(3); // Replace '1' with the fragment index
                    }
                }
            });
        }

        return view;
    }
}