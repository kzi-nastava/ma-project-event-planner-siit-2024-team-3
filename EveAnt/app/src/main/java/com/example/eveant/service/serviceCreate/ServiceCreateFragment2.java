package com.example.eveant.service.serviceCreate;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.eveant.MainActivity;
import com.example.eveant.R;
import com.example.eveant.service.ServiceCreateViewModel;
import com.example.eveant.service.model.Service;


public class ServiceCreateFragment2 extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_create2, container, false);

        view.findViewById(R.id.previous_button).setOnClickListener(v -> {
            NavController navController = ((MainActivity) getActivity()).getNavController();
            navController.navigate(R.id.serviceCreateFragment1);
        });


        ServiceCreateViewModel viewModel = new ViewModelProvider(requireActivity()).get(ServiceCreateViewModel.class);

        EditText description=view.findViewById(R.id.description);
        EditText specification=view.findViewById(R.id.specifications);

        view.findViewById(R.id.next_button).setOnClickListener(v -> {
            Service service = viewModel.getService().getValue();

            String serviceDescription = description.getText().toString();
            service.setDescription(serviceDescription);
            String serviceSpecification = specification.getText().toString();
            service.setSpecification(serviceSpecification);

            /*staviti za upload slika*/

            viewModel.updateService(service);

            NavController navController = ((MainActivity) getActivity()).getNavController();
            navController.navigate(R.id.serviceCreateFragment3);

        });


        return view;
    }



}