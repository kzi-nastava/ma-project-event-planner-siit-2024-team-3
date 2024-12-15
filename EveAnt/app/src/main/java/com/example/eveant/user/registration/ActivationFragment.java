package com.example.eveant.user.registration;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.user.UserClientUtils;
import com.example.eveant.user.UserService;
import com.example.eveant.user.model.Profile;
import com.example.eveant.user.model.User;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import android.os.Handler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ActivationFragment extends Fragment {

    private Handler handler;
    private UserService userService;
    private String email;
    private final long checkInterval = 5000; // Check every 5 seconds

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activation, container, false);
        Button checkEmailButton = view.findViewById(R.id.check_email_button);

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
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        fetchUserData(token);
        userService = UserClientUtils.getClient().create(UserService.class);
        userService.checkActivationStatus(email).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    if (handler != null) {
                        handler.removeCallbacksAndMessages(null); // Stop polling
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
    private String fetchUserData(String token) {
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
    }

    private void fetchProfile(String username) {
        Call<Profile> profileCall = userService.getProfile(username);
        profileCall.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Profile profile = response.body();
                    email = profile.getEmail();
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
}
