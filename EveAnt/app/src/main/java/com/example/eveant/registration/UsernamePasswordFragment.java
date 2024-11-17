package com.example.eveant.registration;
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

import com.example.eveant.LoginActivity;
import com.example.eveant.R;

public class UsernamePasswordFragment extends Fragment {

    private Button goToLogin, goToNext, goToBack;

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
        if(goToNext != null){
            goToNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, new PersonalInfoFragment());
                    transaction.addToBackStack("Registration"); // Allows user to go back to this fragment
                    transaction.commit();
                    if (requireActivity() instanceof RegistrationActivity) {
                        ((RegistrationActivity) requireActivity()).updateProgress(2);

                    }
                }

            });
        }
       goToBack.setVisibility(View.GONE);
        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);            }
        });
    }
}