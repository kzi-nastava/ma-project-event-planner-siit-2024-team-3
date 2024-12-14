package com.example.eveant.user.registration;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.user.UserClientUtils;
import com.example.eveant.user.UserService;
import com.example.eveant.user.model.Address;
import com.example.eveant.user.model.UserProfileRequest;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompanyFragment2 extends Fragment {
    private Button goToNext, goToBack;
    private EditText companyCountry, companyCity, companyStreet, companyPostalNumber, companyHouseNumber;
    private static final String TAG = "CompanyFragment";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company_2, container, false);
        goToBack = getActivity().findViewById(R.id.goToBack);
        goToNext = getActivity().findViewById(R.id.goToNext);
        companyCountry = view.findViewById(R.id.companyCountry);
        companyCity = view.findViewById(R.id.companyCity);
        companyStreet = view.findViewById(R.id.companyStreet);
        companyHouseNumber = view.findViewById(R.id.companyHouse);
        companyPostalNumber = view.findViewById(R.id.companyPostalNumber);
        Bundle args = getArguments();
        UserProfileRequest userProfileRequest = args != null ?
                args.getParcelable("userProfileRequest") : null;
        if (goToBack!= null){
            goToBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, new CompanyFragment1());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    if (requireActivity() instanceof RegistrationActivity) {
                        ((RegistrationActivity) requireActivity()).updateProgress(5); // Replace '1' with the fragment index
                    }
                }
            });
        }
        if (goToNext != null) {
            goToNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Address address = new Address();
                    String countryText = companyCountry.getText().toString().trim();
                    String cityText = companyCity.getText().toString().trim();
                    String streetText = companyStreet.getText().toString().trim();
                    String houseText = companyHouseNumber.getText().toString().trim();
                    String postalNumberText = companyPostalNumber.getText().toString().trim();
                    address.setCountry(countryText);
                    address.setCity(cityText);
                    address.setStreet(streetText);
                    address.setHouseNumber(houseText);
                    address.setPostalNumber(postalNumberText);

                    userProfileRequest.getCreateUserDTO().getCompany().setAddress(address);
                    userProfileRequest.getCreateUserDTO().getCompany().setPhotos(new ArrayList<>());
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
                }
            });
        }

        return view;
    }
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}