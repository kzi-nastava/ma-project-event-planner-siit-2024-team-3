package com.example.eveant.user.registration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.user.UserClientUtils;
import com.example.eveant.user.UserService;
import com.example.eveant.user.model.Address;
import com.example.eveant.user.model.Profile;
import com.example.eveant.user.model.User;
import com.example.eveant.user.model.UserProfileRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrganizerProviderFragment extends Fragment {

    private ToggleButton organizerButton, providerButton;
    private Button goToBack, goToNext;
    private static final String TAG = "OrganizerProviderFragment";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_provider, container, false);

        // Initialize views
        organizerButton = view.findViewById(R.id.organizerButton);
        providerButton = view.findViewById(R.id.providerButton);
        goToBack = getActivity().findViewById(R.id.goToBack);
        goToNext = getActivity().findViewById(R.id.goToNext);

        // Setup toggle buttons
        organizerButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                providerButton.setChecked(false);
            }
        });

        providerButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                organizerButton.setChecked(false);
            }
        });

        // Handle "Next" button click
        if (goToNext != null) {
            goToNext.setOnClickListener(v -> {
                if (!organizerButton.isChecked() && !providerButton.isChecked()) {
                    showError("Please select either Organizer or Provider.");
                    return;
                }

                boolean isOrganizer = organizerButton.isChecked();

                Bundle bundle = getArguments() != null ? getArguments() : new Bundle();

                Profile profile = new Profile();

                profile.setUsername(bundle.getString("username"));
                profile.setEmail(bundle.getString("email"));
                profile.setPassword(bundle.getString("password"));

                Address address = new Address();

                address.setCountry(bundle.getString("country"));
                address.setCity(bundle.getString("city"));
                address.setStreet(bundle.getString("street"));
                address.setPostalNumber(bundle.getString("postalNumber"));

                User user = new User();

                user.setAddress(address);
                user.setFirstName(bundle.getString("firstName"));
                user.setLastName(bundle.getString("lastName"));
                user.setGender(bundle.getString("gender"));
                user.setPhoneNumber(bundle.getString("phoneNumber"));
                user.setDateOfBirth(bundle.getString("birthday"));

                UserProfileRequest userProfileRequest = new UserProfileRequest();
                userProfileRequest.setCreateProfileDTO(profile);
                userProfileRequest.setCreateUserDTO(user);
                if (isOrganizer) {
                    user.setRole("ORGANIZER");
                    UserService userService = UserClientUtils.getClient().create(UserService.class);

                    Call<ResponseBody> call = userService.registerUser(userProfileRequest);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "User registered successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
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

                } else {
                    bundle.putString("Role", "PROVIDER");
                    CompanyFragment1 companyFragment1 = new CompanyFragment1();
                    companyFragment1.setArguments(bundle);

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, companyFragment1);
                    transaction.addToBackStack(null);
                    transaction.commit();

                    if (requireActivity() instanceof RegistrationActivity) {
                        ((RegistrationActivity) requireActivity()).updateProgress(5);
                    }
                }
            });
        }

        // Handle "Back" button click
        if (goToBack != null) {
            goToBack.setOnClickListener(v -> {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                AddressFragment addressFragment = new AddressFragment();

                addressFragment.setArguments(getArguments());

                transaction.replace(R.id.container, addressFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                if (requireActivity() instanceof RegistrationActivity) {
                    ((RegistrationActivity) requireActivity()).updateProgress(3);
                }
            });
        }

        return view;
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
