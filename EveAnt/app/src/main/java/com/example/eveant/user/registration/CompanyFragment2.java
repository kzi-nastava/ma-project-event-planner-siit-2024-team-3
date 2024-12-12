package com.example.eveant.user.registration;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.eveant.MainActivity;
import com.example.eveant.R;

public class CompanyFragment2 extends Fragment {
    private Button goToNext, goToBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_company_2, container, false);
        goToBack = getActivity().findViewById(R.id.goToBack);
        goToNext = getActivity().findViewById(R.id.goToNext);
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
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
            });
        }

        return view;
    }
}