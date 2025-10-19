package com.example.eveant.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.model.OfferDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfferListFragment extends Fragment {

    private RecyclerView rvOffers;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private List<OfferDTO> offers = new ArrayList<>();

    private int categoryId;
    private double remainingBudget;
    private double maxPriceForItem;

    public OfferListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_offer_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOffers = view.findViewById(R.id.rvOffers);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmptyState);

        rvOffers.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (getArguments() != null) {
            categoryId = getArguments().getInt("categoryId", -1);
            remainingBudget = getArguments().getFloat("remainingBudget", 0.0f);
            maxPriceForItem = getArguments().getFloat("maxPrice", 0.0f);
        }

        loadOffers();
    }

    private void loadOffers() {
        progressBar.setVisibility(View.VISIBLE);
        NavController navController = NavHostFragment.findNavController(this);
        RetrofitClient.offerService.getOffersByCategory(categoryId).enqueue(new Callback<List<OfferDTO>>() {
            @Override
            public void onResponse(Call<List<OfferDTO>> call, Response<List<OfferDTO>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    offers.clear();
                    offers.addAll(response.body());

                    if (offers.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        OfferAdapter adapter = new OfferAdapter(
                                offers,
                                remainingBudget,
                                maxPriceForItem,
                                getArguments().getInt("budgetId"),
                                (Item) getArguments().getSerializable("selectedItem"),
                                navController
                        );

                        rvOffers.setAdapter(adapter);
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load offers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OfferDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
