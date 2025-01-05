package com.example.eveant.service;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.model.Service;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private ArrayList<Service> serviceList;
    private Fragment fragment;
    public ServiceAdapter(ArrayList<Service> serviceList) {
        this.serviceList = serviceList;
    }

    public ServiceAdapter(ArrayList<Service> serviceList, Fragment fragment) {
        this.serviceList = serviceList;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }



    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Service service = serviceList.get(position);
        holder.serviceName.setText(service.getName());
        holder.serviceStatus.setText(service.getStatus().toString());
        holder.servicePrice.setText(service.getPrice().toString());
        holder.serviceCategory.setText(service.getCategory().getName());
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(service, position, holder.itemView.getContext());
            }
        });

        holder.editIcon.setOnClickListener(v -> {
            ServiceCreateViewModel viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(ServiceCreateViewModel.class);
            viewModel.updateService(service);

            NavController navController = NavHostFragment.findNavController(fragment);
            navController.navigate(R.id.serviceEditFragment1);
        });



    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName, serviceDescription,serviceCategory,servicePrice,serviceStatus;
        ImageButton deleteIcon;
        ImageButton editIcon;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.service_name);
            serviceCategory = itemView.findViewById(R.id.service_category);
            serviceStatus = itemView.findViewById(R.id.service_availability);
            servicePrice = itemView.findViewById(R.id.service_price);
            deleteIcon = itemView.findViewById(R.id.deleteServiceButton);
            editIcon=itemView.findViewById(R.id.editServiceButton);
        }


    }
    private void showDeleteDialog(Service service, int position, Context context) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.delete_dialog_box, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button buttonYes = dialogView.findViewById(R.id.button_yes);
        Button buttonNo = dialogView.findViewById(R.id.button_no);

        AlertDialog dialog = builder.create();

        buttonYes.setOnClickListener(v -> {
            RetrofitClient.apiService.deleteService(service.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        serviceList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Service deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete service", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });

        buttonNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }



}
