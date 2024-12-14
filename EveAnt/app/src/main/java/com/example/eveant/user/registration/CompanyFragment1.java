package com.example.eveant.user.registration;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.eveant.R;
import com.example.eveant.user.model.Company;
import com.example.eveant.user.model.UserProfileRequest;

public class CompanyFragment1 extends Fragment {
    private Button goToNext, goToBack;
    private EditText companyName, companyEmail, companyDescription, companyPhoneNumber;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company_1, container, false);

        goToBack = getActivity().findViewById(R.id.goToBack);
        goToNext = getActivity().findViewById(R.id.goToNext);
        companyName = view.findViewById(R.id.companyName);
        companyEmail = view.findViewById(R.id.companyEmail);
        companyPhoneNumber = view.findViewById(R.id.companyPhoneNumber);
        companyDescription = view.findViewById(R.id.companyDescription);
        Bundle args = getArguments();
        UserProfileRequest userProfileRequest = args != null ?
                args.getParcelable("userProfileRequest") : null;
        if (goToBack != null) {

            goToBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, new OrganizerProviderFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    if (requireActivity() instanceof RegistrationActivity) {
                        ((RegistrationActivity) requireActivity()).updateProgress(4); // Replace '1' with the fragment index
                    }
                }
            });
        }
        if (goToNext != null) {

            goToNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle nextBundle = new Bundle();
                    Company company = new Company();
                    String companyNameText = companyName.getText().toString().trim();
                    String companyEmailText = companyEmail.getText().toString().trim();
                    String companyContactText = companyPhoneNumber.getText().toString().trim();
                    String companyDescriptionText = companyDescription.getText().toString().trim();
                    company.setCompanyName(companyNameText);
                    company.setEmail(companyEmailText);
                    company.setContact(companyContactText);
                    company.setDescription(companyDescriptionText);
                    userProfileRequest.getCreateUserDTO().setCompany(company);
                    nextBundle.putParcelable("userProfileRequest", userProfileRequest);

                    CompanyFragment2 companyFragment2 = new CompanyFragment2();
                    companyFragment2.setArguments(nextBundle);

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, companyFragment2);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    if (requireActivity() instanceof RegistrationActivity) {
                        ((RegistrationActivity) requireActivity()).updateProgress(6); // Replace '1' with the fragment index
                    }
                }
            });
        }

        return view;
    }
}