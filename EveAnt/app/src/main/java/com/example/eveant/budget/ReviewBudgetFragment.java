package com.example.eveant.budget;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewBudgetFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView totalText;
    private ReviewItemAdapter adapter;
    private List<Item> itemList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_budget, container, false);
        recyclerView = view.findViewById(R.id.review_budget_recycler);
        totalText = view.findViewById(R.id.total_text_review);

        adapter = new ReviewItemAdapter(itemList, this::loadItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadItems();
        return view;
    }

    private void loadItems() {
        RetrofitClient.budgetService.getAllItemsForBudget(1).enqueue(new Callback<ArrayList<Item>>() {
            @Override
            public void onResponse(Call<ArrayList<Item>> call, Response<ArrayList<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.clear();
                    itemList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateTotal();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Item>> call, Throwable t) {
                Log.e("LoadItems", "Greska", t);
            }
        });
    }

    private void updateTotal() {
        long total = 0;
        for (Item item : itemList) {
            if (item.getOffer() != null) total += item.getOffer().getPrice();
        }
           Log.i("Total", String.valueOf(total));
        totalText.setText("Total: " + total + " $");
    }
}
