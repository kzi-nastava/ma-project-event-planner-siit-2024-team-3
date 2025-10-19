package com.example.eveant;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.category.CategoryDialog;
import com.example.eveant.category.SuggestedCategoryAdapter;
import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.CategoryStatus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryManagement extends Fragment {

    private RecyclerView suggestedRecycler, categoryRecycler;
    private SuggestedCategoryAdapter suggestedAdapter;
    private CategoryAdapter categoryAdapter;

    private final List<Category> suggestedCategories = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_management, container, false);

        suggestedRecycler = view.findViewById(R.id.recycler_suggested_categories);
        categoryRecycler = view.findViewById(R.id.recycler_categories);
        suggestedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // Adapter za suggested kategorije
        suggestedAdapter = new SuggestedCategoryAdapter(getContext(), suggestedCategories, new SuggestedCategoryAdapter.OnSuggestedActionListener() {
            @Override
            public void onApprove(Category category) {
                approveCategory(category);
            }

            @Override
            public void onReject(Category category) {
                confirmRejectAndDelete(category);
            }
        });

        // Adapter za aktivne kategorije
        categoryAdapter = new CategoryAdapter(new ArrayList<>(categories), this);

        suggestedRecycler.setAdapter(suggestedAdapter);
        categoryRecycler.setAdapter(categoryAdapter);

        // Dodaj novu kategoriju
        view.findViewById(R.id.fabAddCategory).setOnClickListener(v -> showCreateCategoryDialog());

        loadCategories();

        return view;
    }

    /**
     * Učitavanje svih kategorija i razdvajanje suggested/approved
     */
    private void loadCategories() {
        RetrofitClient.categoryService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    suggestedCategories.clear();
                    categories.clear();

                    for (Category c : response.body()) {
                        if (c.getStatus() == CategoryStatus.SUGGESTED) {
                            suggestedCategories.add(c);
                        } else {
                            categories.add(c);
                        }
                    }
                    suggestedAdapter.notifyDataSetChanged();
                    categoryAdapter.updateData(categories);
                } else {
                    Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Approve suggested kategorije
     */
    private void approveCategory(Category category) {
        category.setStatus(CategoryStatus.APPROVED);
        RetrofitClient.categoryService.updateCategory(category.getId(), category).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Category approved", Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else {
                    Toast.makeText(getContext(), "Failed to approve category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                Toast.makeText(getContext(), "Approve failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Potvrda brisanja suggested kategorije
     */
    private void confirmRejectAndDelete(Category category) {
        new AlertDialog.Builder(getContext())
                .setTitle("Reject category")
                .setMessage("Are you sure you want to delete this suggested category?")
                .setPositiveButton("Delete", (dialog, which) -> rejectAndDelete(category))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Brisanje kategorije
     */
    private void rejectAndDelete(Category category) {
        RetrofitClient.categoryService.deleteCategory(category.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else if (response.code() == 400) {
                    Toast.makeText(getContext(), "Cannot delete category: offers are linked.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Failed to delete category", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Delete failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Popup za kreiranje nove kategorije
     */
    private void showCreateCategoryDialog() {
        CategoryDialog dialog = new CategoryDialog((name, desc) -> {
            Category c = new Category();
            c.setName(name);
            c.setDescription(desc);
            c.setStatus(CategoryStatus.APPROVED);

            RetrofitClient.categoryService.createCategory(c).enqueue(new Callback<Category>() {
                @Override
                public void onResponse(Call<Category> call, Response<Category> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Category created", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    } else {
                        Toast.makeText(getContext(), "Failed to create category", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Category> call, Throwable t) {
                    Toast.makeText(getContext(), "Create failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show(getParentFragmentManager(), "createCategoryDialog");
    }
}
