package com.example.eveant.user.registration;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eveant.user.LoginActivity;
import com.example.eveant.R;
import com.example.eveant.user.UserClientUtils;
import com.example.eveant.user.UserService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsernamePasswordFragment extends Fragment {

    private Button goToLogin, goToNext, goToBack;
    private EditText username, password, confirmPassword, emailEditText;
    private boolean isEmailValid = false;
    private boolean isUsernameValid = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_username_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        goToBack = getActivity().findViewById(R.id.goToBack);
        goToNext = getActivity().findViewById(R.id.goToNext);
        goToLogin = view.findViewById(R.id.goToLogin);
        username = getActivity().findViewById(R.id.username);
        password = getActivity().findViewById(R.id.password);
        confirmPassword = getActivity().findViewById(R.id.confirmPassword);
        emailEditText = view.findViewById(R.id.email);


        if(goToNext != null){
            goToNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String usernameInput = username.getText().toString().trim();
                    String passwordInput = password.getText().toString();
                    String confirmPasswordInput = confirmPassword.getText().toString();
                    String emailText = emailEditText.getText().toString();

                    // Validation
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

                    checkEmailAvailability(emailText);

                    checkUsernameAvailability(usernameInput);
                    if (isEmailValid && isUsernameValid){
                        Bundle bundle = new Bundle();
                        bundle.putString("username", usernameInput);
                        bundle.putString("password", passwordInput);
                        bundle.putString("email", emailText);

                        PersonalInfoFragment personalInfoFragment = new PersonalInfoFragment();
                        personalInfoFragment.setArguments(bundle);

                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, personalInfoFragment);
                        transaction.addToBackStack("Registration"); // Allows user to go back to this fragment
                        transaction.commit();
                        if (requireActivity() instanceof RegistrationActivity) {
                            ((RegistrationActivity) requireActivity()).updateProgress(2);

                        }
                    }

                }

            });
        }
       goToBack.setVisibility(View.GONE);
        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void checkEmailAvailability(String email) {
        UserService userService = UserClientUtils.getClient().create(UserService.class);
        Call<Boolean> call = userService.checkEmailExists(email);

        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean exists = response.body();
                    Log.d("EmailCheck", "Email check response: " + exists); // Log the response
                    if (exists) {
                        showError("Email is already taken");
                        isEmailValid = false;
                    }
                    else{
                        isEmailValid = true;
                    }
                } else {
                    Log.e("EmailCheck", "Error response: " + response.message()); // Log error response
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("EmailCheck", "Error checking email: " + t.getMessage()); // Log failure
                Toast.makeText(getContext(), "Error checking email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUsernameAvailability(String checkUsername) {
        UserService userService = UserClientUtils.getClient().create(UserService.class);
        Call<Boolean> call = userService.checkUsernameExists(checkUsername);

        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean exists = response.body();
                    Log.d("UsernameCheck", "Username check response: " + exists); // Log the response
                    if (exists) {
                        isUsernameValid = false;
                        showError("Username is already taken");
                    }
                    else{
                        isUsernameValid = true;
                    }
                } else {
                    Log.e("UsernameCheck", "Error response: " + response.message()); // Log error response
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("UsernameCheck", "Error checking username: " + t.getMessage()); // Log failure
                Toast.makeText(getContext(), "Error checking username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}