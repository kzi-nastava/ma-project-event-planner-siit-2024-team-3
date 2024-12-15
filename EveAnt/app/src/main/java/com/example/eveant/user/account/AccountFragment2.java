package com.example.eveant.user.account;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import com.example.eveant.user.UserClientUtils;
import com.example.eveant.user.UserService;
import com.example.eveant.user.model.Address;
import com.example.eveant.user.model.Profile;
import com.example.eveant.user.model.User;
import com.example.eveant.user.model.UserProfileRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment2 extends Fragment {
    private UserService userService;
    private User user;
    private Profile profile;
    private FragmentAccount2Binding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccount2Binding.inflate(inflater, container, false);
        View root = binding.getRoot();
        user = new User();
        profile = new Profile();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        userService = UserClientUtils.getClient().create(UserService.class);
        fetchUserData(token);
        return root;
    }
    private void fetchUserData(String token) {
        String username = "";
        try {
            String[] parts = token.split("\\.");

            if (parts.length == 3) {
                String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(payload);
                username = jsonObject.optString("sub");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        fetchProfile(username);
        fetchUserDetails(username);
    }

    private void fetchProfile(String username) {
        Call<Profile> profileCall = userService.getProfile(username);
        profileCall.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    profile = response.body();
                    binding.setProfile(profile);
                } else {
                    Log.e("AccountFragment2", "Failed to fetch profile");
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Log.e("AccountFragment2", "Failed to fetch profile", t);
            }
        });
    }
    private void fetchUserDetails(String username) {
        Call<User> userCall = userService.getUser(username);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    user = response.body();
                    binding.setUser(user);
                } else {
                    Log.e("AccountFragment2", "Failed to fetch user details");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("AccountFragment2", "Failed to fetch user details", t);
            }
        });
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        disableEditing();
        binding.saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();  // Call the saveChanges method when clicked
            }
        });

        binding.editPersonalInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEditing();  // Call the editPersonalInfo method when clicked
            }
        });
    }
    private void saveChanges() {
        user.setFirstName(binding.name.getText().toString());
        user.setLastName(binding.surname.getText().toString());
        profile.setEmail(binding.email.getText().toString());
        profile.setUsername(binding.username.getText().toString());
        user.setDateOfBirth(binding.birthday.getText().toString());
        user.setPhoneNumber(binding.phoneNumber.getText().toString());

        String streetAndHouse = binding.street.getText().toString();
        String[] streetParts = streetAndHouse.split(" ");
        String street = streetParts.length > 0 ? streetParts[0] : "";
        String houseNumber = streetParts.length > 1 ? streetParts[1] : "";

        Address address = user.getAddress();
        address.setCountry(binding.country.getText().toString());
        address.setCity(binding.city.getText().toString());
        address.setStreet(street);
        address.setHouseNumber(houseNumber);
        address.setPostalNumber(binding.postalNumber.getText().toString());

        disableEditing();
        updateProfile();
        updateUser();
    }

    private void disableEditing() {
        binding.name.setEnabled(false);
        binding.surname.setEnabled(false);
        binding.email.setEnabled(false);
        binding.username.setEnabled(false);
        binding.birthday.setEnabled(false);
        binding.phoneNumber.setEnabled(false);
        binding.country.setEnabled(false);
        binding.city.setEnabled(false);
        binding.street.setEnabled(false);
        binding.postalNumber.setEnabled(false);

        binding.saveChangesButton.setVisibility(View.GONE); // Hide Save button
        binding.editPersonalInfoButton.setVisibility(View.VISIBLE); // Show Edit button
    }
    private void enableEditing() {
        binding.name.setEnabled(true);
        binding.surname.setEnabled(true);
        binding.email.setEnabled(true);
        binding.username.setEnabled(true);
        binding.birthday.setEnabled(true);
        binding.phoneNumber.setEnabled(true);
        binding.country.setEnabled(true);
        binding.city.setEnabled(true);
        binding.street.setEnabled(true);
        binding.postalNumber.setEnabled(true);

        binding.saveChangesButton.setVisibility(View.VISIBLE); // Hide Save button
        binding.editPersonalInfoButton.setVisibility(View.GONE); // Show Edit button
    }


    private void updateUser() {
        Call<Void> updateUserCall = userService.updateUser(user.getId(), user);
        updateUserCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("AccountFragment2", "User updated successfully");
                } else {
                    Log.e("AccountFragment2", "Failed to update user: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("AccountFragment2", "Error updating user", t);
            }
        });
    }

    private void updateProfile() {
        Call<Void> updateProfileCall = userService.updateProfile(profile.getId(), profile);
        updateProfileCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i("AccountFragment2", "Profile updated successfully");
                } else {
                    Log.e("AccountFragment2", "Failed to update profile: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("AccountFragment2", "Error updating profile", t);
            }
        });
    }
}
