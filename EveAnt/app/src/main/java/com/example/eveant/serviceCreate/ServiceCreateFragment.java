package com.example.eveant.serviceCreate;
import com.example.eveant.serviceEdit.ServiceEditFragment1;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import com.example.eveant.MainActivity;
import com.example.eveant.R;

public class ServiceCreateFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_create, container, false);
        NavController navController = ((MainActivity) getActivity()).getNavController();
        navController.navigate(R.id.serviceCreateFragment1);
        return view;
    }
}
