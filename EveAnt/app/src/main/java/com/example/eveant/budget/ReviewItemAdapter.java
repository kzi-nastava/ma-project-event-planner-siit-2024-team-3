package com.example.eveant.budget;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;
import java.util.HashMap;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.model.Service;
import com.example.eveant.service.model.ServiceDTO;
import com.example.eveant.service.serviceDetails.ServiceDetails;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class ReviewItemAdapter extends RecyclerView.Adapter<ReviewItemAdapter.ViewHolder> {
    private List<Item> items;
    private Runnable refreshCallback;
    private Map<Integer, List<ServiceDTO>> expandedServices = new HashMap<>();

    public ReviewItemAdapter(List<Item> items, Runnable refreshCallback) {
        this.items = items;
        this.refreshCallback = refreshCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_overview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Item item = items.get(position);

        holder.itemName.setText(item.getName());
        holder.category.setText(item.getCategory().getName());

        if (expandedServices.containsKey(item.getId())) {
            holder.servicesConteiner.setVisibility(View.VISIBLE);
            holder.viewSuggested.setImageResource(R.drawable.ic_arrow);
        } else {
            holder.servicesConteiner.setVisibility(View.GONE);
            holder.viewSuggested.setImageResource(R.drawable.ic_spinner_arrow_right);
        }

        if (item.getOffer() != null) {
            holder.name.setText(item.getOffer().getName()); /*naziv offera */
            holder.price.setText(item.getOffer().getPrice() + "$");
            holder.viewMore.setVisibility(View.VISIBLE);
            holder.viewSuggested.setVisibility(View.GONE);

            holder.viewMore.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putLong("serviceId", item.getOffer().getId());

                NavController navController = NavHostFragment.findNavController(
                        ((FragmentActivity) v.getContext())
                                .getSupportFragmentManager()
                                .findFragmentById(R.id.nav_host_fragment)
                );

                navController.navigate(R.id.action_reviewBudget_to_serviceDetailsFragment, args);

            });



        } else {
            holder.viewMore.setVisibility(View.GONE);
            holder.viewSuggested.setVisibility(View.VISIBLE);

            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putSerializable("category", item.getCategory());
                args.putLong("itemId", item.getId());

                NavController navController = NavHostFragment.findNavController(
                        ((FragmentActivity) v.getContext())
                                .getSupportFragmentManager()
                                .findFragmentById(R.id.nav_host_fragment)
                );

                navController.navigate(R.id.action_reviewBudget_to_servicesForCategoryFragment, args);
            });
        }

        holder.delete.setOnClickListener(v -> {
            showDeleteDialog(v, () -> {
                if (item.getOffer() == null) {
                    RetrofitClient.budgetService.deleteItem(item.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                refreshCallback.run();
                            } else {
                                Toast.makeText(v.getContext(), "Can't delete item because it's reserved.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("DeleteItem", "Greška prilikom brisanja", t);
                        }
                    });
                } else {
                    Toast.makeText(v.getContext(), "Can't delete item because it's reserved.", Toast.LENGTH_SHORT).show();
                }
            });
        });
        holder.viewSuggested.setOnClickListener(v -> {
            Log.i("ovde", "ovde");

            if (expandedServices.containsKey(item.getId())) {
                expandedServices.remove(item.getId());
                holder.servicesConteiner.setVisibility(View.GONE);
                holder.viewSuggested.setImageResource(R.drawable.ic_spinner_arrow_right);
            } else {
                RetrofitClient.serviceService.getServicesByCategory(item.getCategory().getId())
                        .enqueue(new Callback<List<ServiceDTO>>() {
                            @Override
                            public void onResponse(Call<List<ServiceDTO>> call, Response<List<ServiceDTO>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    expandedServices.put(item.getId(), response.body());
                                    Log.i("OVDE SAM I ISPISUJEM DA SAM TU", "ovde sam");

                                    // Popuni container i prikaži
                                    holder.servicesConteiner.removeAllViews();
                                    for (ServiceDTO service : response.body()) {
                                        View serviceView = LayoutInflater.from(holder.servicesConteiner.getContext())
                                                .inflate(R.layout.item_service, holder.servicesConteiner, false);

                                        TextView name = serviceView.findViewById(R.id.service_name);
                                        TextView category = serviceView.findViewById(R.id.service_category);
                                        TextView price = serviceView.findViewById(R.id.service_price);

                                        name.setText(service.getName());
                                        category.setText(service.getCategory().getName()); // category je String u DTO-u
                                        price.setText(service.getPrice() + " RSD");

                                        serviceView.findViewById(R.id.editServiceButton).setVisibility(View.GONE);
                                        serviceView.findViewById(R.id.deleteServiceButton).setVisibility(View.GONE);

                                        holder.servicesConteiner.addView(serviceView);
                                    }

                                    holder.servicesConteiner.setVisibility(View.VISIBLE);
                                    holder.viewSuggested.setImageResource(R.drawable.ic_arrow); // strelica dole
                                }
                            }

                            @Override
                            public void onFailure(Call<List<ServiceDTO>> call, Throwable t) {
                                Log.e("LoadServices", "Error", t);
                                Toast.makeText(v.getContext(), "Error loading services", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });



        holder.servicesConteiner.removeAllViews();
        holder.servicesConteiner.removeAllViews();
        List<ServiceDTO> services = expandedServices.get(item.getId());
        if (services != null) {
            for (ServiceDTO service : services) {
                View serviceView = LayoutInflater.from(holder.servicesConteiner.getContext()).inflate(R.layout.item_service, holder.servicesConteiner, false);
                TextView name = serviceView.findViewById(R.id.service_name);
                TextView category = serviceView.findViewById(R.id.service_category);
                TextView price = serviceView.findViewById(R.id.service_price);
                name.setText(service.getName());
                category.setText(service.getCategory().getCreatedBy());
                price.setText(service.getPrice() + " RSD");
                serviceView.findViewById(R.id.editServiceButton).setVisibility(View.GONE);
                serviceView.findViewById(R.id.deleteServiceButton).setVisibility(View.GONE);
                holder.servicesConteiner.addView(serviceView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    private void showDeleteDialog(View view, Runnable onYesClicked) {
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.delete_dialog_box, null);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(view.getContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.button_yes).setOnClickListener(v -> {
            dialog.dismiss();
            onYesClicked.run();
        });

        dialogView.findViewById(R.id.button_no).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, category, price, itemName;
        ImageButton viewMore, delete, viewSuggested;
        LinearLayout servicesConteiner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            name = itemView.findViewById(R.id.item_name_offer);
            category = itemView.findViewById(R.id.item_category);
            price = itemView.findViewById(R.id.offer_price);
            viewMore = itemView.findViewById(R.id.viewMoreButton);
            delete = itemView.findViewById(R.id.deleteServiceButton);
            viewSuggested=itemView.findViewById(R.id.viewSuggestedOffers);
            servicesConteiner=itemView.findViewById(R.id.services_preview_container);
        }
    }

}
