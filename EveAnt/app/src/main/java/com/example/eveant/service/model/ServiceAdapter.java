package com.example.eveant.service.model;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.service.model.Service;

import java.util.ArrayList;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private ArrayList<Service> serviceList;

    public ServiceAdapter(ArrayList<Service> serviceList) {
        this.serviceList = serviceList;
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
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.serviceName.setText(service.getName());
        holder.serviceStatus.setText(service.getStatus().toString());
        holder.servicePrice.setText(service.getPrice().toString());
        holder.serviceCategory.setText(service.getCategory().getName());
        /*holder.serviceDescription.setText(service.getDescription());*/
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView serviceName, serviceDescription,serviceCategory,servicePrice,serviceStatus;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.service_name);
            serviceCategory = itemView.findViewById(R.id.service_category);
            serviceStatus = itemView.findViewById(R.id.service_availability);
            servicePrice = itemView.findViewById(R.id.service_price);

        }
    }
}
