package com.example.eveant.budget;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.CategoryStatus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CreateBudget extends Fragment {
    private int budgetId=1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {




        View view = inflater.inflate(R.layout.fragment_create_budget, container, false);

        /*RecyclerView recyclerView = view.findViewById(R.id.budget_item_category);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayList<Category> categories = new ArrayList<>();
        BudgetAdapter adapter = new BudgetAdapter(categories,this);
        recyclerView.setAdapter(adapter);*/

        ArrayList<Category> categories = new ArrayList<>();
        ArrayList<Item> itemList = new ArrayList<>();


        RecyclerView itemsRecyclerView = view.findViewById(R.id.budget_item);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemAdapter itemAdapter = new ItemAdapter(itemList);
        itemsRecyclerView.setAdapter(itemAdapter);

        RecyclerView categoriesRecyclerView = view.findViewById(R.id.budget_item_category);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        BudgetAdapter budgetAdapter = new BudgetAdapter(categories,this);
        categoriesRecyclerView.setAdapter(budgetAdapter);




        RetrofitClient.categoryService.getCategories().enqueue(new Callback<List<Category>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();

                    List<Category> otherCategories = new ArrayList<>();

                    for (Category category : response.body()) {
                        if (CategoryStatus.APPROVED.equals(category.getStatus())) {
                            otherCategories.add(category);
                        }
                    }
                    categories.addAll(otherCategories);
                    budgetAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), " eco me tu sam Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(getContext(), "evo me tu sam Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RetrofitClient.budgetService.getAllItemsForBudget(budgetId).enqueue(new Callback<ArrayList<Item>>() {
            public void onResponse(Call<ArrayList<Item>> call, Response<ArrayList<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.clear();

                    List<Item> items = new ArrayList<>();

                    for (Item item : response.body()) {
                       items.add(item);
                    }
                    itemList.addAll(items);
                    itemAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), " eco me tu sam Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Item>> call, Throwable t) {
                Toast.makeText(getContext(), "evo me tu sam Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


}