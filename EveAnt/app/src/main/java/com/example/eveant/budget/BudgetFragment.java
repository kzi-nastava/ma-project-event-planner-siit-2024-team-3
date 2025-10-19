package com.example.eveant.budget;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.Event;
import com.example.eveant.event.EventActivity;
import com.example.eveant.event.EventCreationViewModel;
import com.example.eveant.eventType.EventType;
import com.example.eveant.service.model.Category;

import java.util.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetFragment extends Fragment {

    private LinearLayout suggestedCategoriesContainer;
    private RecyclerView rvItems;
    private TextView tvTotalBudget, tvRemainingBudget;
    private Button btnNext;

    private List<Category> suggestedCategories = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private ItemAdapter itemAdapter;
    private List<Category> allCategories = new ArrayList<>();
    private double totalBudget = 0.0;
    private double remainingBudget = 0.0;
    private int eventTypeId;
    private boolean isFlowMode = false;


    private int budgetId;
    private int eventId;

    public BudgetFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        suggestedCategoriesContainer = v.findViewById(R.id.suggestedCategoriesContainer);
        rvItems = v.findViewById(R.id.rvItems);
        tvTotalBudget = v.findViewById(R.id.tvTotalBudget);
        tvRemainingBudget = v.findViewById(R.id.tvRemainingBudget);
        Button btnAddItem = v.findViewById(R.id.btnAddItem);
        btnAddItem.setOnClickListener(v2 -> showGlobalAddItemDialog());


        // Preuzimanje eventId i budgetId iz argumenata
        if (getArguments() != null) {
            eventId = getArguments().getInt("eventId", -1);
            budgetId = getArguments().getInt("budgetId", -1);
            eventTypeId = getArguments().getInt("eventTypeId", -1);
            isFlowMode = getArguments().getBoolean("isFlowMode", false);
        }
        Log.d("BudgetFragment", "📊 eventId = " + eventId + ", budgetId = " + budgetId);
        Toast.makeText(requireContext(), "eventId=" + eventId + " budgetId=" + budgetId, Toast.LENGTH_LONG).show();

        setupRecyclerView();

        loadItems();
        loadAllCategories();
        if (eventTypeId != -1) {
            loadSuggestedCategories();
        } else {
            suggestedCategories.clear();
            renderSuggestedCategories();
        }

    }

    private void setupRecyclerView() {
        itemAdapter = new ItemAdapter(items, new ItemAdapter.ItemListener() {
            @Override
            public void onEdit(Item item) {
                showEditDialog(item);
            }

            @Override
            public void onDelete(Item item) {
                confirmDelete(item);
            }

            @Override
            public void onSelect(Item item) {
                if (!isFlowMode) {
                    navigateToOffers(item);
                } else {
                    Toast.makeText(requireContext(), "Offer selection is disabled in flow mode.", Toast.LENGTH_SHORT).show();
                }
            }
        }, isFlowMode);
        rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvItems.setAdapter(itemAdapter);
    }


    /** Učitaj sve stavke u budžetu */
    private void loadItems() {
        RetrofitClient.budgetService.getItems(budgetId).enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    items.clear();
                    items.addAll(response.body());
                    itemAdapter.notifyDataSetChanged();
                    recalculateBudget();
                } else {
                    Toast.makeText(requireContext(), "Failed to load budget items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Učitaj predložene kategorije */
    private void loadSuggestedCategories() {
        if (eventTypeId == -1) {
            Toast.makeText(requireContext(), "Event type ID not provided", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.eventTypeService.getEventTypeCategories(eventTypeId).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Set<Integer> usedCategoryIds = new HashSet<>();
                    for (Item item : items) {
                        if (item.getCategory() != null) usedCategoryIds.add(item.getCategory().getId());
                    }

                    suggestedCategories.clear();
                    for (Category c : response.body()) {
                        if (!usedCategoryIds.contains(c.getId())) {
                            suggestedCategories.add(c);
                        }
                    }

                    renderSuggestedCategories();
                } else {
                    Toast.makeText(requireContext(), "Failed to load suggested categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    /** Render dugmadi za sugestije */
    private void renderSuggestedCategories() {
        suggestedCategoriesContainer.removeAllViews();
        for (Category category : suggestedCategories) {
            Button btn = new Button(requireContext());
            btn.setText("+ " + category.getName());
            btn.setAllCaps(false);
            btn.setPadding(16, 12, 16, 12);
            btn.setTextColor(getResources().getColor(R.color.purple));
            btn.setBackgroundResource(R.drawable.bg_event_type_choice);

            btn.setOnClickListener(v -> showAddItemDialog(category));
            suggestedCategoriesContainer.addView(btn);
        }
    }

    /** Popup dodavanje nove stavke */
    private void showAddItemDialog(Category category) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null, false);
        EditText etName = dialogView.findViewById(R.id.etItemName);
        EditText etMaxPrice = dialogView.findViewById(R.id.etMaxPrice);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Item for " + category.getName())
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etMaxPrice.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                        Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double maxPrice = Double.parseDouble(priceStr);
                    Item newItem = new Item();
                    newItem.setName(name);
                    newItem.setCategory(category);
                    newItem.setMaxPrice(maxPrice);

                    RetrofitClient.budgetService.addItem(budgetId, newItem).enqueue(new Callback<Item>() {
                        @Override
                        public void onResponse(Call<Item> call, Response<Item> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                items.add(response.body());
                                itemAdapter.notifyItemInserted(items.size() - 1);
                                recalculateBudget();

                                suggestedCategories.removeIf(c -> c.getId() == category.getId());
                                renderSuggestedCategories();

                            } else {
                                Toast.makeText(requireContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
                            }
                        }


                        @Override
                        public void onFailure(Call<Item> call, Throwable t) {
                            Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Izmena stavke */
    private void showEditDialog(Item item) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null, false);
        EditText etName = dialogView.findViewById(R.id.etItemName);
        EditText etMaxPrice = dialogView.findViewById(R.id.etMaxPrice);

        etName.setText(item.getName());
        etMaxPrice.setText(String.valueOf(item.getMaxPrice()));

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Item")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    item.setName(etName.getText().toString().trim());
                    item.setMaxPrice(Double.parseDouble(etMaxPrice.getText().toString().trim()));

                    RetrofitClient.budgetService.updateItem(budgetId, item).enqueue(new Callback<Item>() {
                        @Override
                        public void onResponse(Call<Item> call, Response<Item> response) {
                            if (response.isSuccessful()) {
                                itemAdapter.notifyDataSetChanged();
                                recalculateBudget();
                            }
                        }

                        @Override
                        public void onFailure(Call<Item> call, Throwable t) {
                            Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Brisanje stavke */
    private void confirmDelete(Item item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete item")
                .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (item.getOffer() != null) {
                        Toast.makeText(requireContext(), "Cannot delete item with reserved offer.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int position = items.indexOf(item);
                    if (position != -1) {
                        items.remove(position);
                        itemAdapter.notifyItemRemoved(position);
                    }
                    recalculateBudget();
                    renderSuggestedCategories();

                    Toast.makeText(requireContext(), "Item removed", Toast.LENGTH_SHORT).show();
                    loadSuggestedCategories();
                })
                .setNegativeButton("Cancel", null)
                .show();


    }




    /** Računanje ukupnog i preostalog budžeta */
    private void recalculateBudget() {
        totalBudget = 0.0;
        for (Item i : items) {
            totalBudget += (i.getMaxPrice() != null ? i.getMaxPrice() : 0.0);
        }

        double used = 0.0;
        for (Item i : items) {
            if (i.getOffer() != null && i.getOffer().getPrice() != null) {
                used += i.getOffer().getPrice();
            }
        }

        remainingBudget = totalBudget - used;
        tvTotalBudget.setText("Total: " + totalBudget + " RSD");
        tvRemainingBudget.setText("Remaining: " + remainingBudget + " RSD");
    }

    /** Navigacija do ponuda (placeholder – kasnije ide OfferSelectionFragment) */
    private void navigateToOffers(Item item) {
        if (item.getCategory() == null) {
            Toast.makeText(requireContext(), "This item has no category assigned.", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryId = item.getCategory().getId();

        Bundle args = new Bundle();
        args.putInt("categoryId", categoryId);
        args.putFloat("remainingBudget", (float) remainingBudget);
        args.putFloat("maxPrice", (float) (item.getMaxPrice() != null ? item.getMaxPrice() : 0.0f));
        args.putInt("budgetId", budgetId);
        args.putSerializable("selectedItem", item);


        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_budgetFragment_to_offerListFragment, args);
    }

    /** Popup: ručno dodavanje stavke sa izborom kategorije */
    private void showGlobalAddItemDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item_full, null, false);
        EditText etName = dialogView.findViewById(R.id.etItemName);
        EditText etMaxPrice = dialogView.findViewById(R.id.etMaxPrice);
        Spinner spinnerCategories = dialogView.findViewById(R.id.spinnerCategories);

        if (allCategories.isEmpty()) {
            Toast.makeText(requireContext(), "No categories available. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Napuni spinner svim kategorijama
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                getCategoryNames(allCategories)
        );
        spinnerCategories.setAdapter(adapter);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Budget Item")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etMaxPrice.getText().toString().trim();
                    int selectedPos = spinnerCategories.getSelectedItemPosition();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || selectedPos == -1) {
                        Toast.makeText(requireContext(), "All fields required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double maxPrice = Double.parseDouble(priceStr);
                    Category selectedCategory = allCategories.get(selectedPos);

                    Item newItem = new Item();
                    newItem.setName(name);
                    newItem.setCategory(selectedCategory);
                    newItem.setMaxPrice(maxPrice);

                    RetrofitClient.budgetService.addItem(budgetId, newItem).enqueue(new Callback<Item>() {
                        @Override
                        public void onResponse(Call<Item> call, Response<Item> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                items.add(response.body());
                                itemAdapter.notifyItemInserted(items.size() - 1);
                                recalculateBudget();

                                if (response.body().getCategory() != null) {
                                    int catId = response.body().getCategory().getId();
                                    suggestedCategories.removeIf(c -> c.getId() == catId);
                                    renderSuggestedCategories();
                                }

                            } else {
                                Toast.makeText(requireContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
                            }
                        }


                        @Override
                        public void onFailure(Call<Item> call, Throwable t) {
                            Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private List<String> getCategoryNames(List<Category> list) {
        List<String> names = new ArrayList<>();
        for (Category c : list) names.add(c.getName());
        return names;
    }
    private void loadAllCategories() {
        RetrofitClient.categoryService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCategories.clear();
                    allCategories.addAll(response.body());
                    Log.d("BudgetFragment", "Loaded all categories: " + allCategories.size());
                } else {
                    Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
