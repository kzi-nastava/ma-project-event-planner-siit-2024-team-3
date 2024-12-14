package com.example.eveant.user.registration;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Patterns;
import android.widget.ToggleButton;

import com.example.eveant.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class PersonalInfoFragment extends Fragment {

    private EditText name, surname, phoneNumber;
    private ToggleButton maleButton, femaleButton, otherButton;
    private Button goToBack, goToNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_info, container, false);

        name = view.findViewById(R.id.name);
        surname = view.findViewById(R.id.surname);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        goToBack = getActivity().findViewById(R.id.goToBack);
        goToNext = getActivity().findViewById(R.id.goToNext);
        maleButton = view.findViewById(R.id.male);
        femaleButton = view.findViewById(R.id.female);
        otherButton = view.findViewById(R.id.other);
        Spinner daySpinner = view.findViewById(R.id.spinner_day);
        Spinner monthSpinner = view.findViewById(R.id.spinner_month);
        Spinner yearSpinner = view.findViewById(R.id.spinner_year);

// Populate Day Spinner
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            days.add(String.valueOf(i));
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_color, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

// Populate Month Spinner
        List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_color, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

// Populate Year Spinner
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= 1900; i--) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_color, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        maleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                femaleButton.setChecked(false);
                otherButton.setChecked(false);
            }
        });

        femaleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                maleButton.setChecked(false);
                otherButton.setChecked(false);
            }
        });

        otherButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                maleButton.setChecked(false);
                femaleButton.setChecked(false);
            }
        });
        if (goToNext != null) {
            goToNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstName = name.getText().toString();
                    String lastName = surname.getText().toString();
                    String phone = phoneNumber.getText().toString();
                    String selectedDay = daySpinner.getSelectedItem().toString();
                    String selectedMonth = monthSpinner.getSelectedItem().toString();
                    String selectedYear = yearSpinner.getSelectedItem().toString();
                    String birthdayText = selectedYear  + "/" + selectedMonth + "/" + selectedDay;

                    if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)
                             || TextUtils.isEmpty(phone) || TextUtils.isEmpty(birthdayText)) {
                        showError("All fields are required.");
                        return;
                    }

                    if (!Patterns.PHONE.matcher(phone).matches()) {
                        showError("Invalid phone number.");
                        return;
                    }
                    String gender = null;
                    if (maleButton.isChecked()) {
                        gender = "MALE";
                    } else if (femaleButton.isChecked()) {
                        gender = "FEMALE";
                    } else if (otherButton.isChecked()) {
                        gender = "OTHER";
                    } else {
                        showError("Please select a gender.");
                        return;
                    }
                    Bundle bundle = getArguments() != null ? getArguments() : new Bundle();
                    bundle.putString("firstName", firstName);
                    bundle.putString("lastName", lastName);
                    bundle.putString("phoneNumber", phone);
                    bundle.putString("birthday", birthdayText);
                    bundle.putString("gender", gender);

                    AddressFragment addressFragment = new AddressFragment();
                    addressFragment.setArguments(bundle);


                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, addressFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    if (requireActivity() instanceof RegistrationActivity) {

                        ((RegistrationActivity) requireActivity()).updateProgress(3);                    }
                }
            });
        }
        goToBack.setVisibility(View.VISIBLE);
        if (goToBack != null) {
            goToBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, new UsernamePasswordFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    if (requireActivity() instanceof RegistrationActivity) {
                        ((RegistrationActivity) requireActivity()).updateProgress(1);                    }
                }
            });
        }

        return view;
    }
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}