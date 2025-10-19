package com.example.eveant.user.registration;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
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
import com.example.eveant.RetrofitClient;
import com.example.eveant.user.model.Address;
import com.example.eveant.user.model.Company;
import com.example.eveant.user.model.ProfileDTO;
import com.example.eveant.user.model.UserDTO;
import com.example.eveant.user.model.UserProfileRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import android.os.Handler;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivationFragment extends Fragment {

    private Handler handler;
    private static final String TAG = "Activation fragment";
    private String email;
    private final long checkInterval = 5000; // Check every 5 seconds

    // ---------- helpers to encode the picked image ----------
    /** Prefer compressing to keep TEXT column reasonable in size. */
    private String toJpegDataUri(@NonNull Uri uri) {
        try {
            Bitmap bmp;
            if (Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source src = ImageDecoder.createSource(requireContext().getContentResolver(), uri);
                bmp = ImageDecoder.decodeBitmap(src);
            } else {
                bmp = android.provider.MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // 80% quality is a good balance
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            String b64 = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);
            return "data:image/jpeg;base64," + b64;
        } catch (Exception e) {
            Log.e(TAG, "Image compress failed, falling back to raw", e);
            return toRawDataUri(uri);
        }
    }

    /** Raw data: keeps original mime, no recompression. */
    private String toRawDataUri(@NonNull Uri uri) {
        try {
            String mime = requireContext().getContentResolver().getType(uri);
            if (mime == null) mime = "application/octet-stream";
            try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
                String b64 = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);
                return "data:" + mime + ";base64," + b64;
            }
        } catch (Exception e) {
            Log.e(TAG, "Image read failed", e);
            return null;
        }
    }
    // -------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activation, container, false);
        Button checkEmailButton = view.findViewById(R.id.check_email_button);
        Button sendActivationLink = view.findViewById(R.id.send_activation_link);
        Button goToLogin = view.findViewById(R.id.login);
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

        // --------- read args incl. photo ----------
        Bundle bundle = getArguments() != null ? getArguments() : new Bundle();

        ProfileDTO profile = new ProfileDTO();
        profile.setUsername(bundle.getString("username"));
        profile.setEmail(bundle.getString("email"));
        profile.setPassword(bundle.getString("password"));
        email = bundle.getString("email");

        // If earlier step passed a URI string (e.g., content://...), convert to data: URL
        String profilePhotoUriStr = bundle.getString("profilePhotoUri", null);
        if (profilePhotoUriStr != null) {
            Uri photoUri = Uri.parse(profilePhotoUriStr);
            // Smaller payload: JPEG-compress it
            String dataUri = toJpegDataUri(photoUri);
            profile.setProfilePhoto(dataUri); // <-- put on DTO so backend stores it in TEXT column
        }
        // If you already passed a *URL* from earlier (e.g., S3, server), do: profile.setProfilePhoto(bundle.getString("profilePhotoUrl"))

        Address address = new Address();
        address.setCountry(bundle.getString("country"));
        address.setCity(bundle.getString("city"));
        address.setStreet(bundle.getString("street"));
        address.setPostalNumber(bundle.getString("postalNumber"));
        address.setHouseNumber(bundle.getString("houseNumber"));

        UserDTO user = new UserDTO();
        user.setAddress(address);
        user.setFirstName(bundle.getString("firstName"));
        user.setLastName(bundle.getString("lastName"));
        user.setGender(bundle.getString("gender"));
        user.setPhoneNumber(bundle.getString("phoneNumber"));
        user.setDateOfBirth(bundle.getString("birthday"));
        user.setRole(bundle.getString("role"));

        if ("PROVIDER".equals(user.getRole())) {
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

        // Build request with the photo included
        UserProfileRequest userProfileRequest = new UserProfileRequest();
        userProfileRequest.setCreateProfileDTO(profile);
        userProfileRequest.setCreateUserDTO(user);

        // --------- register user (now includes profilePhoto) ----------
        Call<ResponseBody> call = RetrofitClient.userService.registerUser(userProfileRequest);
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
                Log.e(TAG, "Registration error: " + t.getMessage());
                showError("Error: " + t.getMessage());
            }
        });

        sendActivationLink.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Sending activation link...", Toast.LENGTH_SHORT).show();
            sendActivation();
        });
        checkEmailButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Checking activation status...", Toast.LENGTH_SHORT).show();
            startPollingForActivation();
        });

        goToLogin.setOnClickListener(v -> {
            // Optional: show a short message
            Toast.makeText(requireContext(), "Returning to login...", Toast.LENGTH_SHORT).show();

            // Clear the current activity stack and start LoginActivity
            Intent intent = new Intent(requireContext(), com.example.eveant.user.LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Finish the current activity to remove it from the back stack
            requireActivity().finish();
        });


        return view;
    }

    private void sendActivation(){
        RetrofitClient.userService.sendActivationEmail(email.trim()).enqueue(new Callback<Map<String, String>>() {
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
        RetrofitClient.userService.checkActivationStatus(email).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    if (handler != null) handler.removeCallbacksAndMessages(null);
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
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) handler.removeCallbacksAndMessages(null);
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
