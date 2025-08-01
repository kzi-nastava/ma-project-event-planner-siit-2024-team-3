package com.example.eveant.budget;



import android.os.Bundle;
import android.util.Log;
import android.util.LogPrinter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.category.CategoryAdapter;
import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.EventType;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateBudget extends Fragment  {

    private String eventTypeName;
    private List<Category> suggestedCategories = new ArrayList<>();
    private List<Item> itemList = new ArrayList<>();
    private CategoryAdapter categoryAdapter;
    private ItemAdapter itemAdapter;
    private final int budgetId = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_budget, container, false);

        Button nextButton = view.findViewById(R.id.save_button);
        nextButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(CreateBudget.this);
            navController.navigate(R.id.action_createBudget_to_reviewBudget);

        });

        RecyclerView itemRecycler = view.findViewById(R.id.budget_item);

        itemRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView recyclerView = view.findViewById(R.id.budget_item_category);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        categoryAdapter = new CategoryAdapter((ArrayList<Category>) suggestedCategories, this);
        recyclerView.setAdapter(categoryAdapter);
        itemAdapter = new ItemAdapter(itemList);
        itemAdapter.setOnItemEditListener(item -> {
            AddItemDialogFragment dialog = new AddItemDialogFragment(item.getName(), item.getMaxPrice(), (newName, newPrice) -> {
                updateItemInBudget(item, newName, newPrice);
            });
            dialog.show(getChildFragmentManager(), "EditItemDialog");
        });


        categoryAdapter.setBudgetMode(true);

        categoryAdapter.setOnAddToBudgetClickListener(category -> {
            AddItemDialogFragment dialog = new AddItemDialogFragment((name, price) -> {
                addItemToBudget(category, name, price);
            });
            dialog.show(getChildFragmentManager(), "AddItemDialog");
        });
        itemRecycler.setAdapter(itemAdapter);

        if (getArguments() != null) {
            EventType eventType = (EventType) getArguments().getSerializable("eventType");
            if (eventType != null) {
                suggestedCategories.clear();
                suggestedCategories.addAll(eventType.getSuggestedCategories());
                categoryAdapter.notifyDataSetChanged();
            }
        }
        loadItems();
        return view;
    }

    private void loadItems() {
        RetrofitClient.budgetService.getAllItemsForBudget(budgetId).enqueue(new Callback<ArrayList<Item>>() {
            @Override
            public void onResponse(Call<ArrayList<Item>> call, Response<ArrayList<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.clear();
                    itemList.addAll(response.body());
                    itemAdapter.notifyDataSetChanged();
                    updateTotalBudget();
                } else {
                    Log.e("LoadItems", "Greska u odgovoru: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Item>> call, Throwable t) {
                Log.e("LoadItems", "Neuspesan poziv", t);
            }
        });
    }

    private void addItemToBudget(Category category, String name, double maxPrice) {
        ItemDTO newItem = new ItemDTO(name, category, (long) maxPrice, null);

        RetrofitClient.budgetService.addItemToBudget(budgetId, newItem).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.add(response.body());
                    itemAdapter.notifyDataSetChanged();
                    updateTotalBudget();
                } else {
                    Log.e("AddItem", "Greska: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Log.e("AddItem", "Greska pri dodavanju", t);
            }
        });
    }


    private void updateTotalBudget() {
        double total = 0;
        for (Item item : itemList) {
            total += item.getMaxPrice();
        }

        TextView totalTextView = getView().findViewById(R.id.total_text);
        totalTextView.setText("Total: " + total + " RSD");
    }


    private void updateItemInBudget(Item item, String newName, double newPrice) {
        ItemDTO updatedItemDTO = new ItemDTO(newName, item.getCategory(), (long) newPrice,null);

        RetrofitClient.budgetService.updateItem(item.getId(), updatedItemDTO).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int index = itemList.indexOf(item);
                    itemList.set(index, response.body());
                    itemAdapter.notifyItemChanged(index);
                    updateTotalBudget();
                } else {
                    Log.e("UpdateItem", "Greška kod ažuriranja: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Log.e("UpdateItem", "Neuspešno ažuriranje", t);
            }
        });
    }

}
