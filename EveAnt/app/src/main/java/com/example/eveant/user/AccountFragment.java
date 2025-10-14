package com.example.eveant.user;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eveant.RetrofitClient;
import com.example.eveant.databinding.FragmentAccountBinding;
import com.example.eveant.user.model.Address;
import com.example.eveant.user.model.Organizer;
import com.example.eveant.user.model.Profile;
import com.example.eveant.user.model.Provider;
import com.example.eveant.user.model.User;
import com.example.eveant.user.security.AuthManager;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {
    private User user;
    private Profile profile;
    private String token;
    private String role;
    private FragmentAccountBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        user = new User();
        user.setAddress(new Address());
        profile = new Profile();

        AuthManager auth = AuthManager.getInstance(requireContext());
        role = auth.getRole();
        fetchProfile(auth.getEmail());
        fetchUserDetails(auth.getEmail());
        return root;
    }


    private void fetchProfile(String email) {
        Call<Profile> profileCall = RetrofitClient.userService.getProfile(email);
        profileCall.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    profile = response.body();
                    binding.setProfile(profile);
                } else {
                    try {
                        Log.e("AccountFragment", "Failed to fetch profile: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e("AccountFragment", "Error reading errorBody", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Log.e("AccountFragment", "Failed to fetch profile", t);
            }
        });
    }

    private void fetchUserDetails(String email) {
        Call<User> call = RetrofitClient.userService.getUserByEmail(email);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User fetchedUser = response.body();
                    // Instantiate the correct subclass based on role
                    if ("PROVIDER".equalsIgnoreCase(role)) {
                        Provider provider = new Provider();
                        copyUserToUserSubclass(fetchedUser, provider);
                        user = provider;
                    } else if ("ORGANIZER".equalsIgnoreCase(role)) {
                        Organizer organizer = new Organizer();
                        copyUserToUserSubclass(fetchedUser, organizer);
                        user = organizer;
                        Log.e("AccountFragment", String.valueOf(user));
                    } else {
                        user = fetchedUser;
                    }

                    // Ensure Address is initialized
                    if (user.getAddress() == null) user.setAddress(new Address());

                    binding.setUser(user);
                    Log.d("AccountFragment", "User fetched: " + user.getFirstName());
                } else {
                    Log.e("AccountFragment", "Failed to fetch user: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("AccountFragment", "Error fetching user", t);
            }
        });
    }

    private void copyUserToUserSubclass(User source, User target) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setAddress(source.getAddress());
        target.setDateOfBirth(source.getDateOfBirth());
        target.setPhoneNumber(source.getPhoneNumber());
        target.setGender(source.getGender());

        // For provider only
        if (target instanceof Provider && source instanceof Provider) {
            ((Provider) target).setCompany(((Provider) source).getCompany());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        disableEditing();

        binding.saveChangesButton.setOnClickListener(v -> saveChanges());
        binding.editPersonalInfoButton.setOnClickListener(v -> enableEditing());
        binding.logout.setOnClickListener(v -> performLogout());
        binding.btnDeactivateAccount.setOnClickListener(v -> confirmAndDeactivate());
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

        binding.saveChangesButton.setVisibility(View.GONE);
        binding.editPersonalInfoButton.setVisibility(View.VISIBLE);
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

        binding.saveChangesButton.setVisibility(View.VISIBLE);
        binding.editPersonalInfoButton.setVisibility(View.GONE);
    }

    private void updateUser() {
        String email = profile.getEmail();
        Log.e("AccountFragment", String.valueOf(user));
        if (email == null) {
            Log.e("AccountFragment", "Email is null, cannot update user");
            return;
        }

        if (user instanceof Provider) {
            Provider provider = (Provider) user;
            Call<Provider> call = RetrofitClient.userService.updateProvider( provider, email);
            call.enqueue(new Callback<Provider>() {
                @Override
                public void onResponse(Call<Provider> call, Response<Provider> response) {
                    if (response.isSuccessful()) {
                        Log.i("AccountFragment", "Provider updated successfully");
                    } else {
                        Log.e("AccountFragment", "Failed to update provider: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Provider> call, Throwable t) {
                    Log.e("AccountFragment", "Error updating provider", t);
                }
            });
        } else if (user instanceof Organizer) {
            Organizer organizer = (Organizer) user;
            Call<Organizer> call = RetrofitClient.userService.updateOrganizer(organizer, email);
            call.enqueue(new Callback<Organizer>() {
                @Override
                public void onResponse(Call<Organizer> call, Response<Organizer> response) {
                    if (response.isSuccessful()) {
                        Log.i("AccountFragment", "Organizer updated successfully");
                    } else {
                        Log.e("AccountFragment", "Failed to update organizer: " + response);
                    }
                }

                @Override
                public void onFailure(Call<Organizer> call, Throwable t) {
                    Log.e("AccountFragment", "Error updating organizer", t);
                }
            });
        } else {
            Log.e("AccountFragment", "User type unknown, cannot update");
        }
    }

    private void updateProfile() {

        Call<Profile> updateProfileCall = RetrofitClient.userService.updateProfile( profile, profile.getEmail());
        updateProfileCall.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful()) {
                    Log.i("AccountFragment", "Profile updated successfully");
                } else {
                    Log.e("AccountFragment", "Failed to update profile: " + response);
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Log.e("AccountFragment", "Error updating profile", t);
            }
        });
    }
    private void performLogout() {
        // 1) Clear local auth/session
        SharedPreferences sp = requireActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        sp.edit()
                .remove("token")
                .apply();

        Intent i = new Intent(requireContext(), com.example.eveant.user.LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        requireActivity().finish();
    }
    private void confirmAndDeactivate() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Deactivate account?")
                .setMessage("You won’t be able to use your account until it’s reactivated.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Deactivate", (d, which) -> callDeactivate())
                .show();
    }

    private void callDeactivate() {
        binding.btnDeactivateAccount.setEnabled(false);

        RetrofitClient.userService.deactivateAccount(profile.getEmail())
                .enqueue(new retrofit2.Callback<java.util.Map<String, String>>() {
                    @Override
                    public void onResponse(Call<java.util.Map<String, String>> call,
                                           Response<java.util.Map<String, String>> response) {
                        binding.btnDeactivateAccount.setEnabled(true);

                        if (response.isSuccessful()) {
                            String msg = response.body() != null ? response.body().get("message") : "Account deactivated.";
                            android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_LONG).show();

                            // After deactivation, kick user to login & clear session
                            performLogout();
                            return;
                        }

                        // Show server error message (400/404) if present
                        try {
                            String err = response.errorBody() != null ? response.errorBody().string() : null;
                            String toShow = "Error";
                            if (err != null) {
                                try {
                                    org.json.JSONObject obj = new org.json.JSONObject(err);
                                    toShow = obj.optString("error", toShow);
                                } catch (Exception ignore) {}
                            }
                            android.widget.Toast.makeText(requireContext(), toShow, android.widget.Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            android.widget.Toast.makeText(requireContext(), "Request failed.", android.widget.Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.Map<String, String>> call, Throwable t) {
                        binding.btnDeactivateAccount.setEnabled(true);
                        android.widget.Toast.makeText(requireContext(), "Network error.", android.widget.Toast.LENGTH_LONG).show();
                        Log.e("AccountFragment", "Deactivate failed", t);
                    }
                });
    }


}
