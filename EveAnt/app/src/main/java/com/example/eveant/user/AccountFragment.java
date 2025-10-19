package com.example.eveant.user;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eveant.BaseFragment;
import com.example.eveant.HomeFragment;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.databinding.FragmentAccountBinding;
import com.example.eveant.user.model.Address;
import com.example.eveant.user.model.Organizer;
import com.example.eveant.user.model.Profile;
import com.example.eveant.user.model.Provider;
import com.example.eveant.user.model.User;
import com.example.eveant.user.security.AuthManager;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends BaseFragment {

    private User user;
    private Profile profile;
    private String role;
    private FragmentAccountBinding binding;

    @Override protected int getMainContainerId() { return R.id.home_container; }
    @NonNull @Override protected Fragment createHomeFragment() { return new HomeFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);

        user = new User();
        user.setAddress(new Address());
        profile = new Profile();

        AuthManager auth = AuthManager.getInstance(requireContext());
        role = auth.getRole();

        fetchProfile(auth.getEmail());
        fetchUserDetails(auth.getEmail());
        return binding.getRoot();
    }

    // -------------------- API calls --------------------

    private void fetchProfile(String email) {
        RetrofitClient.userService.getProfile(email).enqueue(new Callback<Profile>() {
            @Override public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    profile = response.body();
                    binding.setProfile(profile);
                    bindProfilePhoto(profile.getProfilePhoto());
                } else {
                    try {
                        Log.e("AccountFragment", "Failed to fetch profile: " +
                                (response.errorBody() != null ? response.errorBody().string() : "no error body"));
                    } catch (Exception e) {
                        Log.e("AccountFragment", "Error reading errorBody", e);
                    }
                }
            }
            @Override public void onFailure(Call<Profile> call, Throwable t) {
                Log.e("AccountFragment", "Failed to fetch profile", t);
            }
        });
    }

    private void fetchUserDetails(String email) {
        RetrofitClient.userService.getUserByEmail(email).enqueue(new Callback<User>() {
            @Override public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User fetchedUser = response.body();

                    if ("PROVIDER".equalsIgnoreCase(role)) {
                        Provider provider = new Provider();
                        copyUserToUserSubclass(fetchedUser, provider);
                        user = provider;
                    } else if ("ORGANIZER".equalsIgnoreCase(role)) {
                        Organizer organizer = new Organizer();
                        copyUserToUserSubclass(fetchedUser, organizer);
                        user = organizer;
                    } else {
                        user = fetchedUser;
                    }

                    if (user.getAddress() == null) user.setAddress(new Address());
                    binding.setUser(user);
                } else {
                    Log.e("AccountFragment", "Failed to fetch user: " + response.code());
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {
                Log.e("AccountFragment", "Error fetching user", t);
            }
        });
    }

    private void updateUser() {
        String email = profile.getEmail();
        if (email == null) {
            Log.e("AccountFragment", "Email is null, cannot update user");
            return;
        }

        if (user instanceof Provider) {
            RetrofitClient.userService.updateProvider((Provider) user, email)
                    .enqueue(new Callback<Provider>() {
                        @Override public void onResponse(Call<Provider> call, Response<Provider> response) {
                            if (!response.isSuccessful()) {
                                Log.e("AccountFragment", "Failed to update provider: " + response.code());
                            }
                        }
                        @Override public void onFailure(Call<Provider> call, Throwable t) {
                            Log.e("AccountFragment", "Error updating provider", t);
                        }
                    });
        } else if (user instanceof Organizer) {
            RetrofitClient.userService.updateOrganizer((Organizer) user, email)
                    .enqueue(new Callback<Organizer>() {
                        @Override public void onResponse(Call<Organizer> call, Response<Organizer> response) {
                            if (!response.isSuccessful()) {
                                Log.e("AccountFragment", "Failed to update organizer: " + response.code());
                            }
                        }
                        @Override public void onFailure(Call<Organizer> call, Throwable t) {
                            Log.e("AccountFragment", "Error updating organizer", t);
                        }
                    });
        } else {
            Log.e("AccountFragment", "User type unknown, cannot update");
        }
    }

    private void updateProfile() {
        RetrofitClient.userService.updateProfile(profile, profile.getEmail())
                .enqueue(new Callback<Profile>() {
                    @Override public void onResponse(Call<Profile> call, Response<Profile> response) {
                        if (!response.isSuccessful()) {
                            Log.e("AccountFragment", "Failed to update profile: " + response.code());
                        }
                    }
                    @Override public void onFailure(Call<Profile> call, Throwable t) {
                        Log.e("AccountFragment", "Error updating profile", t);
                    }
                });
    }

    // -------------------- UI lifecycle --------------------

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackBar(view);
        disableEditing();

        // Save / Edit
        binding.saveChangesButton.setOnClickListener(v -> saveChanges());
        binding.editPersonalInfoButton.setOnClickListener(v -> enableEditing());

        // Address edit buttons (since you have separate ones in XML)
        binding.saveChangesButtonAddress.setOnClickListener(v -> saveChanges());
        binding.editPersonalInfoButtonAddress.setOnClickListener(v -> enableEditing());

        // Photo actions (Option B delete)
        binding.deletePhotoButton.setOnClickListener(v -> confirmDeletePhoto());

        // Logout & Deactivate
        binding.logout.setOnClickListener(v -> performLogout());
        binding.btnDeactivateAccount.setOnClickListener(v -> confirmAndDeactivate());
    }

    // -------------------- Photo binding & delete (Option B) --------------------

    private void bindProfilePhoto(@Nullable String photo) {
        if (binding == null) return;

        if (photo == null || photo.trim().isEmpty()) {
            binding.profilePhoto.setImageResource(R.drawable.rounded_corners_image);
            return;
        }

        try {
            if (photo.startsWith("data:")) {
                int idx = photo.indexOf("base64,");
                if (idx >= 0) {
                    String b64 = photo.substring(idx + "base64,".length());
                    byte[] bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.profilePhoto.setImageBitmap(bmp);
                    return;
                }
            }

            if (photo.startsWith("http")) {
                // If you use Glide, uncomment:
                // Glide.with(this).load(photo).placeholder(R.drawable.rounded_corners_image).into(binding.profilePhoto);
                // Temporary fallback:
                binding.profilePhoto.setImageResource(R.drawable.rounded_corners_image);
                return;
            }

            binding.profilePhoto.setImageResource(R.drawable.rounded_corners_image);
        } catch (Exception e) {
            Log.e("AccountFragment", "Failed to bind photo", e);
            binding.profilePhoto.setImageResource(R.drawable.rounded_corners_image);
        }
    }

    private void confirmDeletePhoto() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Remove profile photo?")
                .setMessage("Your profile picture will be cleared from your account.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (d, w) -> deletePhotoOptionB())
                .show();
    }

    /** Option B: set photo to null and reuse updateProfile(...) */
    private void deletePhotoOptionB() {
        if (profile == null || profile.getEmail() == null) {
            Toast.makeText(requireContext(), "No profile loaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Optimistic UI
        profile.setProfilePhoto(null);
        bindProfilePhoto(null);
        binding.deletePhotoButton.setEnabled(false);

        RetrofitClient.userService.updateProfile(profile, profile.getEmail())
                .enqueue(new Callback<Profile>() {
                    @Override public void onResponse(Call<Profile> call, Response<Profile> response) {
                        binding.deletePhotoButton.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            profile = response.body();
                            Toast.makeText(requireContext(), "Photo removed.", Toast.LENGTH_SHORT).show();
                            bindProfilePhoto(profile.getProfilePhoto());
                        } else {
                            String msg = "Failed to remove photo.";
                            try {
                                if (response.errorBody() != null) {
                                    String raw = response.errorBody().string();
                                    try {
                                        JSONObject obj = new JSONObject(raw);
                                        if (obj.has("error")) msg = obj.optString("error", msg);
                                        else if (obj.has("message")) msg = obj.optString("message", msg);
                                    } catch (Exception ignored) {}
                                }
                            } catch (Exception ignored) {}
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                            Log.e("AccountFragment", msg);
                        }
                    }

                    @Override public void onFailure(Call<Profile> call, Throwable t) {
                        binding.deletePhotoButton.setEnabled(true);
                        Toast.makeText(requireContext(), "Network error.", Toast.LENGTH_LONG).show();
                        Log.e("AccountFragment", "deletePhotoOptionB failed", t);
                    }
                });
    }

    // -------------------- Save / Edit --------------------

    private void saveChanges() {
        user.setFirstName(binding.name.getText().toString());
        user.setLastName(binding.surname.getText().toString());
        user.setDateOfBirth(binding.birthday.getText().toString());
        user.setPhoneNumber(binding.phoneNumber.getText().toString());

        profile.setEmail(binding.email.getText().toString());
        profile.setUsername(binding.username.getText().toString());

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

        binding.saveChangesButtonAddress.setVisibility(View.GONE);
        binding.editPersonalInfoButtonAddress.setVisibility(View.VISIBLE);
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

        binding.saveChangesButtonAddress.setVisibility(View.VISIBLE);
        binding.editPersonalInfoButtonAddress.setVisibility(View.GONE);
    }

    // -------------------- Deactivate (better UX + reason) --------------------

    private void confirmAndDeactivate() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Deactivate account?")
                .setMessage("You won’t be able to use your account until it’s reactivated.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Deactivate", (d, which) -> callDeactivateWithUi())
                .show();
    }

    private void callDeactivateWithUi() {
        binding.btnDeactivateAccount.setEnabled(false);

        RetrofitClient.userService.deactivateAccount(profile.getEmail())
                .enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        binding.btnDeactivateAccount.setEnabled(true);

                        if (response.isSuccessful()) {
                            String msg = "Account deactivated.";
                            if (response.body() != null && response.body().get("message") != null) {
                                msg = response.body().get("message");
                            }
                            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                    .setTitle("Deactivated")
                                    .setMessage(msg)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dd, w) -> performLogout())
                                    .show();
                            return;
                        }

                        // Not successful → show WHY from server JSON
                        String reason = "Could not deactivate account.";
                        try {
                            if (response.errorBody() != null) {
                                String err = response.errorBody().string();
                                try {
                                    JSONObject obj = new JSONObject(err);
                                    if (obj.has("error"))      reason = obj.optString("error", reason);
                                    else if (obj.has("message")) reason = obj.optString("message", reason);
                                    else if (obj.has("details")) reason = obj.optString("details", reason);
                                } catch (Exception ignore) {}
                            }
                        } catch (Exception ignore) {}

                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle("Cannot deactivate")
                                .setMessage(reason)
                                .setPositiveButton("OK", null)
                                .show();
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        binding.btnDeactivateAccount.setEnabled(true);
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle("Network error")
                                .setMessage("Could not reach the server. Please try again.")
                                .setPositiveButton("OK", null)
                                .show();
                        Log.e("AccountFragment", "Deactivate failed", t);
                    }
                });
    }

    // -------------------- Helpers --------------------

    private void copyUserToUserSubclass(User source, User target) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setAddress(source.getAddress());
        target.setDateOfBirth(source.getDateOfBirth());
        target.setPhoneNumber(source.getPhoneNumber());
        target.setGender(source.getGender());

        if (target instanceof Provider && source instanceof Provider) {
            ((Provider) target).setCompany(((Provider) source).getCompany());
        }
    }

    private void performLogout() {
        SharedPreferences sp = requireActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        sp.edit().remove("token").apply();

        Intent i = new Intent(requireContext(), com.example.eveant.user.LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        requireActivity().finish();
    }
}
