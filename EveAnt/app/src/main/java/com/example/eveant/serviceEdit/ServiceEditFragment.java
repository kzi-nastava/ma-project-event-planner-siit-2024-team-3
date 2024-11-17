package com.example.eveant.serviceEdit;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.activity.EdgeToEdge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eveant.R;

public class ServiceEditFragment extends Fragment {

    public ServiceEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_service_edit, container, false);

        // Enable Edge to Edge
        EdgeToEdge.enable(getActivity());

        // Other logic for setting up the fragment, similar to what you did in the activity
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ServiceEditFragment1())
                    .commit();
        }

        return view;
    }

    // Method to replace fragments inside the fragment
    public void replaceFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
