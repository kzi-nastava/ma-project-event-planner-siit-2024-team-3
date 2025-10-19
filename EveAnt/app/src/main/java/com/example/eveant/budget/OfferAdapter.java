package com.example.eveant.budget;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.model.OfferDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private final List<OfferDTO> offers;
    private final double remainingBudget;
    private final double maxPrice;
    private final int budgetId;
    private final Item selectedItem;
    private final NavController navController;

    public OfferAdapter(List<OfferDTO> offers,
                        double remainingBudget,
                        double maxPrice,
                        int budgetId,
                        Item selectedItem,
                        NavController navController) {
        this.offers = offers;
        this.remainingBudget = remainingBudget;
        this.maxPrice = maxPrice;
        this.budgetId = budgetId;
        this.selectedItem = selectedItem;
        this.navController = navController;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        OfferDTO offer = offers.get(position);

        holder.tvName.setText(offer.getName());
        holder.tvPrice.setText("Price: " + offer.getPrice() + " RSD");
        holder.tvDiscount.setText("Discount: " + (offer.getDiscount() != null ? offer.getDiscount() + "%" : "0%"));

        // 🔎 View Details
        holder.btnViewDetail.setOnClickListener(v -> {
            if (navController != null) {
                Bundle bundle = new Bundle();
                bundle.putInt("offerId", offer.getId());
                navController.navigate(R.id.action_offerListFragment_to_offerDetailsFragment, bundle);
            } else {
                Toast.makeText(v.getContext(), "Navigation not available", Toast.LENGTH_SHORT).show();
            }
        });

        // 📦 Reserve Offer
        holder.btnReserve.setOnClickListener(v -> {
            Log.d("BUDGET_DEBUG",
                    "offerPrice = " + offer.getPrice() +
                            ", remainingBudget = " + remainingBudget +
                            ", maxPrice = " + maxPrice);

            if (offer.getPrice() > remainingBudget || offer.getPrice() > maxPrice) {
                Toast.makeText(v.getContext(), "Offer exceeds budget!", Toast.LENGTH_SHORT).show();
            } else {
                selectedItem.setOffer(offer);
                RetrofitClient.budgetService.updateItem(budgetId, selectedItem)
                        .enqueue(new Callback<Item>() {
                            @Override
                            public void onResponse(Call<Item> call, Response<Item> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(v.getContext(), "Offer reserved successfully!", Toast.LENGTH_SHORT).show();
                                    notifyItemChanged(holder.getAdapterPosition());
                                } else {
                                    Toast.makeText(v.getContext(), "Failed to reserve offer", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Item> call, Throwable t) {
                                Toast.makeText(v.getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // 🎯 Rezervisana ponuda — vizuelni prikaz
        OfferDTO reservedOffer = selectedItem.getOffer();
        boolean isReserved = reservedOffer != null && reservedOffer.getId() == offer.getId();

        if (isReserved) {
            holder.itemView.setBackgroundResource(R.drawable.bg_reserved_offer);
            holder.tvName.setText(offer.getName() + " (Reserved)");
            holder.btnReserve.setEnabled(false);
            holder.btnReserve.setText("Reserved");
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_offer_default);
            holder.btnReserve.setEnabled(true);
            holder.btnReserve.setText("Reserve");
        }
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvDiscount;
        Button btnViewDetail, btnReserve;

        OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvOfferName);
            tvPrice = itemView.findViewById(R.id.tvOfferPrice);
            tvDiscount = itemView.findViewById(R.id.tvOfferDiscount);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetails);
            btnReserve = itemView.findViewById(R.id.btnReserve);
        }
    }
}
