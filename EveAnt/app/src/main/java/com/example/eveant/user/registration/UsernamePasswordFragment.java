package com.example.eveant.user.registration;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eveant.RetrofitClient;
import com.example.eveant.user.LoginActivity;
import com.example.eveant.R;
import com.example.eveant.user.UserService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsernamePasswordFragment extends Fragment {

    private Button goToLogin, goToNext, goToBack, btnPickPhoto;
    private EditText username, password, confirmPassword, emailEditText;
    private ImageView ivProfile;

    private Uri selectedPhotoUri = null;
    private boolean isEmailValid = false;
    private boolean isUsernameValid = false;

    // Activity Result API: pick an image from gallery
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    ivProfile.setImageURI(uri); // preview
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_username_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NOTE: use view.findViewById for views that live inside THIS fragment layout
        goToLogin = view.findViewById(R.id.goToLogin);
        emailEditText = view.findViewById(R.id.email);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnPickPhoto = view.findViewById(R.id.btnPickPhoto);

        // These three might be in the Activity toolbar/footer; if they are in THIS fragment, use view.findViewById
        goToBack = getActivity().findViewById(R.id.goToBack);
        goToNext = getActivity().findViewById(R.id.goToNext);
        username = getActivity().findViewById(R.id.username);
        password = getActivity().findViewById(R.id.password);
        confirmPassword = getActivity().findViewById(R.id.confirmPassword);

        if (goToBack != null) goToBack.setVisibility(View.GONE);

        btnPickPhoto.setOnClickListener(v -> pickImage.launch("image/*"));

        if (goToNext != null) {
            goToNext.setOnClickListener(v -> {
                String usernameInput = username.getText().toString().trim();
                String passwordInput = password.getText().toString();
                String confirmPasswordInput = confirmPassword.getText().toString();
                String emailText = emailEditText.getText().toString().trim();

                // Basic validation
                if (usernameInput.isEmpty() || passwordInput.isEmpty() || confirmPasswordInput.isEmpty() || emailText.isEmpty()) {
                    showError("All fields are required.");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                    showError("Invalid email format.");
                    return;
                }
                if (!passwordInput.equals(confirmPasswordInput)) {
                    showError("Passwords do not match.");
                    return;
                }

                // IMPORTANT: async checks — only navigate AFTER both are valid.
                // We'll run both, and proceed in the username callback if both flags are true.
                checkEmailAvailability(emailText, () -> {
                    checkUsernameAvailability(usernameInput, () -> {
                        if (isEmailValid && isUsernameValid) {
                            Bundle bundle = new Bundle();
                            bundle.putString("username", usernameInput);
                            bundle.putString("password", passwordInput);
                            bundle.putString("email", emailText);
                            // Pass photo as a stringified Uri (optional)
                            if (selectedPhotoUri != null) {
                                bundle.putString("profilePhotoUri", selectedPhotoUri.toString());
                            }

                            PersonalInfoFragment personalInfoFragment = new PersonalInfoFragment();
                            personalInfoFragment.setArguments(bundle);

                            FragmentTransaction tx = getParentFragmentManager().beginTransaction();
                            tx.replace(R.id.container, personalInfoFragment);
                            tx.addToBackStack("Registration");
                            tx.commit();

                            if (requireActivity() instanceof RegistrationActivity) {
                                ((RegistrationActivity) requireActivity()).updateProgress(2);
                            }
                        }
                    });
                });
            });
        }

        goToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
    }

    // Overloads with a completion callback so we can chain properly
    private void checkEmailAvailability(String email, Runnable onDone) {
        RetrofitClient.userService.checkEmailExists(email).enqueue(new Callback<Boolean>() {
            @Override public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean exists = response.body();
                    isEmailValid = !exists;
                    if (exists) showError("Email is already taken");
                } else {
                    isEmailValid = false;
                    Log.e("EmailCheck", "Error response: " + response.message());
                    showError("Could not verify email.");
                }
                onDone.run();
            }
            @Override public void onFailure(Call<Boolean> call, Throwable t) {
                isEmailValid = false;
                Log.e("EmailCheck", "Error checking email: " + t.getMessage());
                Toast.makeText(getContext(), "Error checking email", Toast.LENGTH_SHORT).show();
                onDone.run();
            }
        });
    }

    private void checkUsernameAvailability(String user, Runnable onDone) {
        RetrofitClient.userService.checkUsernameExists(user).enqueue(new Callback<Boolean>() {
            @Override public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean exists = response.body();
                    isUsernameValid = !exists;
                    if (exists) showError("Username is already taken");
                } else {
                    isUsernameValid = false;
                    Log.e("UsernameCheck", "Error response: " + response.message());
                    showError("Could not verify username.");
                }
                onDone.run();
            }
            @Override public void onFailure(Call<Boolean> call, Throwable t) {
                isUsernameValid = false;
                Log.e("UsernameCheck", "Error checking username: " + t.getMessage());
                Toast.makeText(getContext(), "Error checking username", Toast.LENGTH_SHORT).show();
                onDone.run();
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}

