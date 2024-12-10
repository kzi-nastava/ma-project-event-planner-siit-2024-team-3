package com.example.eveant.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eveant.R;
import com.example.eveant.databinding.FragmentAccount2Binding;

public class AccountFragment2 extends Fragment {

    private FragmentAccount2Binding binding;
    private User userProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simulate fetching user data from a database or API
        User userProfile = new User(
                "1",
                "John",
                "Doe",
                "john.doe@example.com",
                "organizer",
                "john_doe",
                "1990-01-01",
                "+123456789",
                "USA",
                "New York",
                "5th Avenue",
                "10001"
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccount2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Display user data in views
        binding.name.setText(userProfile.getName());
        binding.surname.setText(userProfile.getSurname());
        binding.email.setText(userProfile.getEmail());
        binding.username.setText(userProfile.getUsername());
        binding.birthday.setText(userProfile.getBirthday());
        binding.phoneNumber.setText(userProfile.getPhoneNumber());
        binding.country.setText(userProfile.getCountry());
        binding.city.setText(userProfile.getCity());
        binding.street.setText(userProfile.getStreet());
        binding.postalNumber.setText(userProfile.getPostalNumber());

        // Set up listeners for buttons
        binding.editPersonalInfoButton.setOnClickListener(v -> enableEditingPersonalInfo());
        binding.saveChangesButton.setOnClickListener(v -> saveChanges());
    }

    private void enableEditingPersonalInfo() {
        binding.name.setEnabled(true);
        binding.surname.setEnabled(true);
        binding.email.setEnabled(true);
        binding.username.setEnabled(true);
        binding.birthday.setEnabled(true);
        binding.phoneNumber.setEnabled(true);
    }

    private void saveChanges() {
        // Save changes to the userProfile object
        userProfile.setName(binding.name.getText().toString());
        userProfile.setSurname(binding.surname.getText().toString());
        userProfile.setEmail(binding.email.getText().toString());
        userProfile.setUsername(binding.username.getText().toString());
        userProfile.setBirthday(binding.birthday.getText().toString());
        userProfile.setPhoneNumber(binding.phoneNumber.getText().toString());

        // Optionally save the data to a database or API

        // Disable editing after saving
        binding.name.setEnabled(false);
        binding.surname.setEnabled(false);
        binding.email.setEnabled(false);
        binding.username.setEnabled(false);
        binding.birthday.setEnabled(false);
        binding.phoneNumber.setEnabled(false);
    }
}
