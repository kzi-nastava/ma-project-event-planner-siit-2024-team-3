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
        Call<UserProfileRequest> userCall = userService.getUserProfile(username);
        userCall.enqueue(new Callback<UserProfileRequest>() {
            @Override
            public void onResponse(Call<UserProfileRequest> call, Response<UserProfileRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileRequest userDTO = response.body();
                    profile = userDTO.getCreateProfileDTO();
                    user = userDTO.getCreateUserDTO();

                    binding.setProfile(profile);
                    binding.setUser(user);
                }
            }


            @Override
            public void onFailure(Call<UserProfileRequest> call, Throwable t) {
                // Handle failure (e.g., show error message)
                Log.e("AccountFragment2", "Failed to load user data", t);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.name.setText(user.getFirstName());
        binding.surname.setText(user.getLastName());
        binding.email.setText(profile.getEmail());
        binding.username.setText(profile.getUsername());
        binding.birthday.setText(user.getDateOfBirth());
        binding.phoneNumber.setText(user.getPhoneNumber());
        binding.country.setText(user.getAddress().getCountry());
        binding.city.setText(user.getAddress().getCity());
        String streetAndHouse = user.getAddress().getStreet() + " " + user.getAddress().getHouseNumber();
        binding.street.setText(streetAndHouse);
        binding.postalNumber.setText(user.getAddress().getPostalNumber());

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
        user.setFirstName(binding.name.getText().toString());
        user.setLastName(binding.surname.getText().toString());
        profile.setEmail(binding.email.getText().toString());
        profile.setUsername(binding.username.getText().toString());
        user.setDateOfBirth(binding.birthday.getText().toString());
        user.setPhoneNumber(binding.phoneNumber.getText().toString());

        String[] addressParts = binding.country.getText().toString().split(" ");
        String street = addressParts[0];
        String house = addressParts.length > 1 ? addressParts[1] : "";
        Address address = new Address();

        address.setCountry(binding.country.getText().toString());
        address.setCity(binding.city.getText().toString());
        address.setStreet(street);
        address.setStreet(house);
        address.setPostalNumber(binding.postalNumber.getText().toString());

        binding.name.setEnabled(false);
        binding.surname.setEnabled(false);
        binding.email.setEnabled(false);
        binding.username.setEnabled(false);
        binding.birthday.setEnabled(false);
        binding.phoneNumber.setEnabled(false);
    }
}
