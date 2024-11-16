package com.example.eveant;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class UsernamePasswordFragment extends Fragment {

    private Button goToYourselfButton;
    private Button goToLoginButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_username_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        goToYourselfButton = view.findViewById(R.id.goToYourself);
        goToLoginButton = view.findViewById(R.id.goToLogin);

        goToYourselfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToNextFragment();
            }
        });

        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    private void navigateToNextFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new PersonalInfoFragment());
        transaction.addToBackStack(null); // Allows user to go back to this fragment
        transaction.commit();
    }

    private void navigateToLogin() {
         Intent intent = new Intent(getActivity(), LoginActivity.class);
         startActivity(intent);
    }
}