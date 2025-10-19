package com.example.eveant.product;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.eventType.EventType;
import com.example.eveant.product.Product;
import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.OfferStatus;
import com.example.eveant.user.security.AuthManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateProductFragment extends Fragment {

    private EditText etName, etDescription, etNewCategory, etPrice, etDiscount;
    private Spinner spCategory;
    private CheckBox cbNewCategory;
    private ToggleButton tbVisible, tbHidden, tbAvailable, tbUnavailable;
    private Button btnEventTypes, btnSubmit;
    private TextView tvSelectedEvents;

    private final List<EventType> selectedEventTypes = new ArrayList<>();
    private final List<Category> fetchedCategories = new ArrayList<>();
    private final List<EventType> fetchedEventTypes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_product, container, false);

        etName = v.findViewById(R.id.name);
        etDescription = v.findViewById(R.id.description);
        etNewCategory = v.findViewById(R.id.new_category_input);
        etPrice = v.findViewById(R.id.price);
        etDiscount = v.findViewById(R.id.discount);

        spCategory = v.findViewById(R.id.category_spinner);
        cbNewCategory = v.findViewById(R.id.checkbox_new_category);

        tbVisible = v.findViewById(R.id.visibleButton);
        tbHidden = v.findViewById(R.id.hiddenButton);
        tbAvailable = v.findViewById(R.id.availableButton);
        tbUnavailable = v.findViewById(R.id.unavailableButton);

        btnEventTypes = v.findViewById(R.id.buttonShowCheckboxes);
        tvSelectedEvents = v.findViewById(R.id.selectedEventsTextView);

        btnSubmit = v.findViewById(R.id.submit_button);

        setupToggles();
        setupCategoryUiToggle();

        // ✨ Fetch data from backend
        fetchCategories();
        fetchEventTypes();

        // Open multi-select with fetched event types
        btnEventTypes.setOnClickListener(view -> showEventTypesDialog());

        btnSubmit.setOnClickListener(view -> submitProduct());
        return v;
    }

    /* -------------------- FETCH & BIND: Categories -------------------- */

    private void fetchCategories() {
        RetrofitClient.categoryService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    fetchedCategories.clear();
                    fetchedCategories.addAll(response.body());
                    bindCategoriesToSpinner();
                } else {
                    Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<Category>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Categories error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindCategoriesToSpinner() {
        List<String> names = new ArrayList<>();
        for (Category c : fetchedCategories) names.add(c.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_color, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    private void setupCategoryUiToggle() {
        cbNewCategory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etNewCategory.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            spCategory.setEnabled(!isChecked);
            spCategory.setAlpha(isChecked ? 0.5f : 1f);
        });
    }

    /* -------------------- FETCH & DIALOG: Event Types -------------------- */

    private void fetchEventTypes() {
        RetrofitClient.eventTypeService.getAll().enqueue(new Callback<List<EventType>>() {
            @Override public void onResponse(Call<List<EventType>> call, Response<List<EventType>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    fetchedEventTypes.clear();
                    fetchedEventTypes.addAll(response.body());
                    // (No UI to bind immediately; used when opening dialog)
                } else {
                    Toast.makeText(requireContext(), "Failed to load event types", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<EventType>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Event types error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEventTypesDialog() {
        if (fetchedEventTypes.isEmpty()) {
            Toast.makeText(requireContext(), "No event types available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] labels = new String[fetchedEventTypes.size()];
        boolean[] checked = new boolean[fetchedEventTypes.size()];
        for (int i = 0; i < fetchedEventTypes.size(); i++) {
            labels[i] = fetchedEventTypes.get(i).getName();
            checked[i] = selectedEventTypes.contains(fetchedEventTypes.get(i));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_type_of_events)
                .setMultiChoiceItems(labels, checked, (d, which, isChecked) -> {
                    EventType et = fetchedEventTypes.get(which);
                    if (isChecked) {
                        if (!selectedEventTypes.contains(et)) selectedEventTypes.add(et);
                    } else {
                        selectedEventTypes.remove(et);
                    }
                })
                .setPositiveButton(android.R.string.ok, (d, w) -> renderSelectedEventTypes())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void renderSelectedEventTypes() {
        if (selectedEventTypes.isEmpty()) {
            tvSelectedEvents.setVisibility(View.GONE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedEventTypes.size(); i++) {
            sb.append(selectedEventTypes.get(i).getName());
            if (i < selectedEventTypes.size() - 1) sb.append(", ");
        }
        tvSelectedEvents.setText(sb.toString());
        tvSelectedEvents.setVisibility(View.VISIBLE);
    }

    /* -------------------- TOGGLES, VALIDATION & SUBMIT -------------------- */

    private void setupToggles() {
        tbVisible.setOnCheckedChangeListener((b, checked) -> { if (checked) tbHidden.setChecked(false); });
        tbHidden.setOnCheckedChangeListener((b, checked) -> { if (checked) tbVisible.setChecked(false); });
        tbAvailable.setOnCheckedChangeListener((b, checked) -> { if (checked) tbUnavailable.setChecked(false); });
        tbUnavailable.setOnCheckedChangeListener((b, checked) -> { if (checked) tbAvailable.setChecked(false); });
        tbVisible.setChecked(true);      // defaults
        tbAvailable.setChecked(true);
    }

    private void submitProduct() {
        String name = etName.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String discountStr = etDiscount.getText().toString().trim();
        AuthManager auth = AuthManager.getInstance(requireContext());
        String email = auth.getEmail();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(requireContext(), "Name, description and price are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Long price = Long.valueOf(priceStr);
        int discount = TextUtils.isEmpty(discountStr) ? 0 : Integer.parseInt(discountStr);

        // Category
        Category category = new Category();
        if (cbNewCategory.isChecked()) {
            String newCat = etNewCategory.getText().toString().trim();
            if (TextUtils.isEmpty(newCat)) {
                Toast.makeText(requireContext(), "Enter a new category name.", Toast.LENGTH_SHORT).show();
                return;
            }
            category.setName(newCat);
        } else {
            if (spCategory.getSelectedItem() == null) {
                Toast.makeText(requireContext(), "Select category", Toast.LENGTH_SHORT).show();
                return;
            }
            category.setName(String.valueOf(spCategory.getSelectedItem()));
        }

        // Visibility & Status
        boolean visible = tbVisible.isChecked();
        OfferStatus status = tbAvailable.isChecked() ? OfferStatus.AVAILABLE : OfferStatus.UNAVAILABLE;

        // Build Product (extends Offer)
        Product product = new Product();
        product.setName(name);
        product.setDescription(desc);
        product.setCategory(category);
        product.setEventTypes(new ArrayList<>(selectedEventTypes));
        product.setPrice(price);
        product.setDiscount(discount);
        product.setVisible(visible);
        product.setStatus(status);
        product.setProvider(email);

        // POST to backend
        RetrofitClient.productService.createProduct(product).enqueue(new Callback<Product>() {
            @Override public void onResponse(Call<Product> call, Response<Product> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Product created!", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(requireContext(), "Create failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Product> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
