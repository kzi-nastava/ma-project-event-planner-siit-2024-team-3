package com.example.eveant.user.registration;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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
                Bundle bundle = getArguments() != null ? getArguments() : new Bundle();

                boolean isOrganizer = organizerButton.isChecked();


                if (isOrganizer) {
                    bundle.putString("role", "ORGANIZER");
                    ActivationFragment activationFragment = new ActivationFragment();
                    activationFragment.setArguments(bundle);
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, activationFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();

                    if (requireActivity() instanceof RegistrationActivity) {
                        ((RegistrationActivity) requireActivity()).updateProgress(5);
                    }

                } else {
                    bundle.putString("role", "PROVIDER");

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
