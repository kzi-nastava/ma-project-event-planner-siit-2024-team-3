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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Patterns;
import android.widget.ToggleButton;

import com.example.eveant.R;

public class PersonalInfoFragment extends Fragment {

    private EditText name, surname, email, phoneNumber, birthday;
    private ToggleButton maleButton, femaleButton, otherButton;
    private Button goToBack, goToNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_info, container, false);

        name = view.findViewById(R.id.name);
        surname = view.findViewById(R.id.surname);
        email = view.findViewById(R.id.email);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        birthday = view.findViewById(R.id.birthday);
        goToBack = getActivity().findViewById(R.id.goToBack);
        goToNext = getActivity().findViewById(R.id.goToNext);
        maleButton = view.findViewById(R.id.male);
        femaleButton = view.findViewById(R.id.female);
        otherButton = view.findViewById(R.id.other);
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
                    String emailText = email.getText().toString();
                    String birthdayText = birthday.getText().toString();

                    if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                            TextUtils.isEmpty(emailText) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(birthdayText)) {
                        showError("All fields are required.");
                        return;
                    }
                    if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                        showError("Invalid email format.");
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
                    bundle.putString("email", emailText);
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