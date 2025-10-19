package com.example.eveant.budget;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.service.model.OfferDTO;

import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private final List<OfferDTO> offers;
    private final double remainingBudget;
    private final double maxPriceForItem;

    public OfferAdapter(List<OfferDTO> offers, double remainingBudget, double maxPriceForItem) {
        this.offers = offers;
        this.remainingBudget = remainingBudget;
        this.maxPriceForItem = maxPriceForItem;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        OfferDTO offer = offers.get(position);
        holder.tvName.setText(offer.getName());
        holder.tvType.setText(offer.getType().equalsIgnoreCase("service") ? "Service" : "Product");
        holder.tvPrice.setText(offer.getPrice() + " RSD");

        holder.btnViewDetails.setOnClickListener(v -> {
            double offerPrice = offer.getPrice() != null ? offer.getPrice() : 0.0;

            // ✅ Proveri budžet i maxPrice pre nego što dozvoli detalje/rezervaciju
            if (offerPrice > remainingBudget) {
                Toast.makeText(v.getContext(), " Not enough remaining budget.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (offerPrice > maxPriceForItem) {
                Toast.makeText(v.getContext(), "Offer exceeds max price for this item.", Toast.LENGTH_SHORT).show();
                return;
            }

            NavController navController = Navigation.findNavController(v);
            Bundle args = new Bundle();
            args.putInt("offerId", offer.getId());

            navController.navigate(R.id.action_offerListFragment_to_serviceDetails, args);

            /*if ("service".equalsIgnoreCase(offer.getType())) {
                navController.navigate(R.id.action_offerListFragment_to_serviceDetails, args);
            } else {
                navController.navigate(R.id.action_offerListFragment_to_productDetails, args);
            }*/
        });
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvPrice;
        Button btnViewDetails;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvOfferName);
            tvType = itemView.findViewById(R.id.tvOfferType);
            tvPrice = itemView.findViewById(R.id.tvOfferPrice);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
