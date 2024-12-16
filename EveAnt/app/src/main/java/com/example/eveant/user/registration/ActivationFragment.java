package com.example.eveant.user.registration;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.user.UserClientUtils;
import com.example.eveant.user.UserService;
import com.example.eveant.user.model.Address;
import com.example.eveant.user.model.Company;
import com.example.eveant.user.model.Profile;
import com.example.eveant.user.model.User;
import com.example.eveant.user.model.UserProfileRequest;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import android.os.Handler;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ActivationFragment extends Fragment {

    private Handler handler;
    private static final String TAG = "Activation fragment";
    private UserService userService;
    private String email;
    private final long checkInterval = 5000; // Check every 5 seconds

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activation, container, false);
        Button checkEmailButton = view.findViewById(R.id.check_email_button);
        LinearLayout progressRegistration = requireActivity().findViewById(R.id.progress_registration);
        LinearLayout firstHalf = requireActivity().findViewById(R.id.firstHalf);
        LinearLayout secondHalf = requireActivity().findViewById(R.id.secondHalf);
        if (progressRegistration != null && firstHalf != null && secondHalf != null) {
            progressRegistration.setEnabled(false);
            progressRegistration.setVisibility(View.GONE);
            firstHalf.setEnabled(false);
            firstHalf.setVisibility(View.GONE);
            secondHalf.setEnabled(false);
            secondHalf.setVisibility(View.GONE);
        }

        Bundle bundle = getArguments() != null ? getArguments() : new Bundle();

        Profile profile = new Profile();

        profile.setUsername(bundle.getString("username"));
        profile.setEmail(bundle.getString("email"));
        profile.setPassword(bundle.getString("password"));
        email = bundle.getString("email");
        Address address = new Address();

        address.setCountry(bundle.getString("country"));
        address.setCity(bundle.getString("city"));
        address.setStreet(bundle.getString("street"));
        address.setPostalNumber(bundle.getString("postalNumber"));
        address.setHouseNumber(bundle.getString("houseNumber"));

        User user = new User();

        user.setAddress(address);
        user.setFirstName(bundle.getString("firstName"));
        user.setLastName(bundle.getString("lastName"));
        user.setGender(bundle.getString("gender"));
        user.setPhoneNumber(bundle.getString("phoneNumber"));
        user.setDateOfBirth(bundle.getString("birthday"));

        if (bundle.getString("role").equals("PROVIDER")){
            Company company = new Company();
            company.setCompanyName(bundle.getString("companyName"));
            company.setEmail(bundle.getString("companyEmail"));
            company.setContact(bundle.getString("companyContact"));
            company.setDescription(bundle.getString("companyDescription"));

            Address companyAddress = new Address();

            companyAddress.setCountry(bundle.getString("companyCountry"));
            companyAddress.setCity(bundle.getString("companyCity"));
            companyAddress.setStreet(bundle.getString("companyStreet"));
            companyAddress.setHouseNumber(bundle.getString("companyHouse"));
            companyAddress.setPostalNumber(bundle.getString("companyPostalNumber"));

            company.setAddress(companyAddress);
            user.setCompany(company);
        }
        UserProfileRequest userProfileRequest = new UserProfileRequest();
        userProfileRequest.setCreateProfileDTO(profile);
        userProfileRequest.setCreateUserDTO(user);
        userService = UserClientUtils.getClient().create(UserService.class);
        Call<ResponseBody> call = userService.registerUser(userProfileRequest);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "User registered successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"User registered successfully!");
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorMessage = response.errorBody().string();
                            Log.e(TAG, "Registration failed: " + errorMessage);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading errorBody: " + e.getMessage());
                    }
                    showError("Failed to register user. Try again.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error reading errorBody: " + t.getMessage());
                showError("Error: " + t.getMessage());
            }

        });
        userService.sendActivationEmail(email.trim()).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Activation email sent!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to send activation email.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                Log.d("ActivationEmail", "Error sending activation email: " + t.getMessage());
            }
        });

        checkEmailButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Checking activation status...", Toast.LENGTH_SHORT).show();
            startPollingForActivation();
        });

        return view;
    }

    private void startPollingForActivation() {
        handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                checkActivationStatus();
                handler.postDelayed(this, checkInterval);
            }
        };
        handler.post(runnable);
    }

    private void checkActivationStatus() {
        userService.checkActivationStatus(email).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    if (handler != null) {
                        handler.removeCallbacksAndMessages(null);
                    }
                    navigateToNextScreen();
                } else {
                    Log.d("Activation", "Account not activated yet");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Log.e("Activation", "Error checking activation status: " + t.getMessage());
            }
        });
    }

    private void navigateToNextScreen() {
        Toast.makeText(requireContext(), "Account activated! Redirecting...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
