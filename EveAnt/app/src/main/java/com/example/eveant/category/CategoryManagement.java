package com.example.eveant.category;

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

public class CategoryManagement extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_category_management, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_suggested_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayList<Category> categories = new ArrayList<>();
        CategoryAdapter adapter = new CategoryAdapter(categories,this);
        recyclerView.setAdapter(adapter);

        RetrofitClient.categoryService.getCategories().enqueue(new Callback<List<Category>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        categories.clear();

                        List<Category> suggestedCategories = new ArrayList<>();
                        List<Category> otherCategories = new ArrayList<>();

                        for (Category category : response.body()) {
                            if (CategoryStatus.SUGGESTED.equals(category.getStatus())) {
                                suggestedCategories.add(category);
                            } else {
                                otherCategories.add(category);
                            }
                        }
                        categories.addAll(suggestedCategories);
                        categories.addAll(otherCategories);

                        adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), " eco me tu sam Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(getContext(), "evo me tu sam Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}