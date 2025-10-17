package com.example.eveant.service;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.model.OfferStatus;
import com.example.eveant.service.model.Service;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> services;
    private Fragment fragment;


    public ServiceAdapter(List<Service> services, Fragment fragment) {
        this.services = services;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.serviceName.setText(service.getName());
        holder.serviceCategory.setText(service.getCategory().getName());
        holder.servicePrice.setText(service.getPrice() + " €");

        OfferStatus status = service.getStatus();

        if (status != null) {
            holder.serviceStatus.setText(status.toString());
        } else {
            holder.serviceStatus.setText("UNKNOWN");
            holder.serviceStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.gray)
            );
        }

        String imageUrl = null;

        if (service.getPhotos() != null && !service.getPhotos().isEmpty()) {
            Log.d( "onBindViewHolder: ", "da li mozda dodjem dovde");
            imageUrl = service.getPhotos().get(0); // uzimamo prvu
            Log.d( "onBindViewHolder: ", imageUrl.toString());
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.pastry)
                    .error(R.drawable.pastry)
                    .into(holder.serviceImage);
        } else {
            holder.serviceImage.setImageResource(R.drawable.pastry);
        }


        holder.viewMoreButton.setOnClickListener(v -> {
            // Detalji
            NavController navController = NavHostFragment.findNavController(fragment);
            navController.navigate(R.id.serviceDetailsFragment);
        });

        holder.editIcon.setOnClickListener(v -> {
            ServiceCreateViewModel viewModel = new ViewModelProvider(fragment.requireActivity())
                    .get(ServiceCreateViewModel.class);

            viewModel.updateService(service);
            viewModel.setEditMode(true);

            NavController navController = NavHostFragment.findNavController(fragment);
            navController.navigate(R.id.serviceCreateFragment1);
        });

        holder.deleteIcon.setOnClickListener(v -> deleteService(service, position));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public void updateData(List<Service> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName, serviceCategory, servicePrice, serviceStatus;
        ImageButton viewMoreButton, editIcon, deleteIcon;
        ImageView serviceImage;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.service_name);
            serviceCategory = itemView.findViewById(R.id.service_category);
            servicePrice = itemView.findViewById(R.id.service_price);
            serviceStatus = itemView.findViewById(R.id.service_availability);
            viewMoreButton = itemView.findViewById(R.id.viewMoreButton);
            editIcon = itemView.findViewById(R.id.editServiceButton);
            deleteIcon = itemView.findViewById(R.id.deleteServiceButton);
            serviceImage = itemView.findViewById(R.id.serviceImage);
        }
    }

    private void deleteService(Service service, int position) {
        RetrofitClient.serviceService.deleteService(service.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    services.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(fragment.getContext(), "Service deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(fragment.getContext(), "Can not be deleted. Service has reservation", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(fragment.getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
