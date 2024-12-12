package com.example.eveant.user.registration;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.eveant.R;

public class AddressFragment extends Fragment {
    private EditText country, city, street, postalNumber;
    private Button goToBack, goToNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);
        country = view.findViewById(R.id.country);
        city = view.findViewById(R.id.city);
        street = view.findViewById(R.id.street);
        postalNumber = view.findViewById(R.id.postalNumber);
        goToBack = getActivity().findViewById(R.id.goToBack);
        goToNext = getActivity().findViewById(R.id.goToNext);

        if (goToNext != null) {

            goToNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String countryText = country.getText().toString().trim();
                    String cityText = city.getText().toString().trim();
                    String streetText = street.getText().toString().trim();
                    String postalNumberText = postalNumber.getText().toString().trim();
                    if (TextUtils.isEmpty(countryText) || TextUtils.isEmpty(cityText) ||
                            TextUtils.isEmpty(streetText) || TextUtils.isEmpty(postalNumberText)) {
                        showError("All fields are required.");
                        return;
                    }

                    if (!postalNumberText.matches("\\d+")) {
                        showError("Postal number must be numeric.");
                        return;
                    }

                    Bundle bundle = getArguments() != null ? getArguments() : new Bundle();
                    bundle.putString("country", countryText);
                    bundle.putString("city", cityText);
                    bundle.putString("street", streetText);
                    bundle.putString("postalNumber", postalNumberText);

                    OrganizerProviderFragment organizerProviderFragment = new OrganizerProviderFragment();
                    organizerProviderFragment.setArguments(bundle);

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, organizerProviderFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    if (requireActivity() instanceof RegistrationActivity) {
                        ((RegistrationActivity) requireActivity()).updateProgress(4); // Replace '1' with the fragment index
                    }
                }
            });
        }
        if (goToBack != null) {

            goToBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, new PersonalInfoFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    if (requireActivity() instanceof RegistrationActivity) {
                        ((RegistrationActivity) requireActivity()).updateProgress(2);
                    }
                }
            });
        }
        return view;
    }
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}