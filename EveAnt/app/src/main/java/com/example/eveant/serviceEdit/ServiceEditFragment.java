package com.example.eveant.serviceEdit;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.activity.EdgeToEdge;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eveant.MainActivity;
import com.example.eveant.R;

public class ServiceEditFragment extends Fragment {

    public ServiceEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_edit, container, false);
        EdgeToEdge.enable(getActivity());
        NavController navController = ((MainActivity) getActivity()).getNavController();
        navController.navigate(R.id.serviceEditFragment1);
        return view;
    }
}
