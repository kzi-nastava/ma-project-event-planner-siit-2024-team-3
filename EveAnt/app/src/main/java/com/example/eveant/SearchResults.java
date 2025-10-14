package com.example.eveant;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eveant.event.Event;
import com.example.eveant.event.EventAdapter;
import com.example.eveant.event.EventService;
import com.example.eveant.service.ServiceAdapter;
import com.example.eveant.service.ServiceService;
import com.example.eveant.service.model.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.*;

public class SearchResults extends Fragment {

    private String selectedCategory = "events";
    private List<String> selectedEventTypes = new ArrayList<>();
    private List<String> selectedCategories = new ArrayList<>();
    private String priceRangeStart = "0";
    private String priceRangeEnd = "100000";
    private String selectedSortOption = "date";
    private String sortOrder = "asc";
    private String searchQuery = "";

    // Date selection
    private Calendar selectedStartDate = null;
    private Calendar selectedEndDate = null;

    // Filter views
    private LinearLayout eventOptions, productOptions, serviceOptions;
    private RecyclerView recyclerView;
    private EditText inputCity, searchEditText;
    private Spinner sortSpinner;
    private TextView currentMonthYear;
    private GridLayout calendarGrid;
    private SeekBar priceSeekBar;
    private TextView priceRangeText;

    // Services
    private EventService eventService;
    private ServiceService serviceService;

    // Sample data matching your Angular code
    private final String[] eventTypes = {"Conference", "Workshop", "Concert", "Festival", "Sports"};
    private final String[] categories = {"Electronics", "Clothing", "Food", "Decoration", "Photography"};
    private EditText eventSearchText, productSearchText, serviceSearchText;
    private String eventSearchQuery = "";
    private String productSearchQuery = "";
    private String serviceSearchQuery = "";
    private Calendar currentCalendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize services
        eventService = RetrofitClient.retrofit.create(EventService.class);
        serviceService = RetrofitClient.retrofit.create(ServiceService.class);

        // Get search query from arguments if any
        if (getArguments() != null) {
            searchQuery = getArguments().getString("searchQuery", "");
        }

        // Initialize views
        initializeViews(view);
        setupCategoryButtons(view);
        setupSortSpinner();
        setupCalendar(view);
        setupPriceRange(view);

        // Set initial search query
        if (searchEditText != null) {
            searchEditText.setText(searchQuery);
        }

        // Load initial data
        loadData();
    }

    private void initializeViews(View view) {
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_search_results);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        // Initialize main search and filter views
        inputCity = view.findViewById(R.id.input_city);
        searchEditText = view.findViewById(R.id.search_edit_text);
        sortSpinner = view.findViewById(R.id.sort_spinner);
        currentMonthYear = view.findViewById(R.id.current_month_year);
        calendarGrid = view.findViewById(R.id.calendar_grid);
        priceSeekBar = view.findViewById(R.id.price_seek_bar);
        priceRangeText = view.findViewById(R.id.price_range_text);

        // Initialize category-specific search fields with null checks
        eventSearchText = view.findViewById(R.id.event_search_text);
        productSearchText = view.findViewById(R.id.product_search_text);
        serviceSearchText = view.findViewById(R.id.service_search_text);

        // Log which search fields are missing
        if (eventSearchText == null) Log.d("SearchResults", "event_search_text not found in layout");
        if (productSearchText == null) Log.d("SearchResults", "product_search_text not found in layout");
        if (serviceSearchText == null) Log.d("SearchResults", "service_search_text not found in layout");

        // Filter option containers
        eventOptions = view.findViewById(R.id.event_options);
        productOptions = view.findViewById(R.id.product_options);
        serviceOptions = view.findViewById(R.id.service_options);

        // Setup search functionality only if views exist
        setupSearchFunctionality();
        setupCategorySearchFunctionality();

        // Setup event type and category selection
        setupEventTypes(view);
        setupCategories(view);
    }

    private void setupCategorySearchFunctionality() {
        // Event search - only setup if the field exists
        if (eventSearchText != null) {
            eventSearchText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    eventSearchQuery = eventSearchText.getText().toString().trim();
                    if (selectedCategory.equals("events")) {
                        loadData();
                    }
                    return true;
                }
                return false;
            });
        }

        // Product search - only setup if the field exists
        if (productSearchText != null) {
            productSearchText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    productSearchQuery = productSearchText.getText().toString().trim();
                    if (selectedCategory.equals("products")) {
                        loadData();
                    }
                    return true;
                }
                return false;
            });
        }

        // Service search - only setup if the field exists
        if (serviceSearchText != null) {
            serviceSearchText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    serviceSearchQuery = serviceSearchText.getText().toString().trim();
                    if (selectedCategory.equals("services")) {
                        loadData();
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private void setupSearchFunctionality() {
        // Only setup if searchEditText exists
        if (searchEditText != null) {
            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchQuery = searchEditText.getText().toString().trim();
                    loadData();
                    return true;
                }
                return false;
            });
        }

        // Search button is optional - only setup if it exists
        ImageView searchButton = getView().findViewById(R.id.search_home);
        if (searchButton != null && searchEditText != null) {
            searchButton.setOnClickListener(v -> {
                searchQuery = searchEditText.getText().toString().trim();
                loadData();
            });
        }
    }

    private void setupCategoryButtons(View view) {
        Button btnEvents = view.findViewById(R.id.btn_events);
        Button btnProducts = view.findViewById(R.id.btn_products);
        Button btnServices = view.findViewById(R.id.btn_services);

        // Check if buttons exist
        if (btnEvents == null || btnProducts == null || btnServices == null) {
            Log.e("SearchResults", "One or more category buttons not found");
            return;
        }

        // Set up the sliding indicator
        View sliderFilter = view.findViewById(R.id.slider_filter);

        View.OnClickListener categoryClickListener = v -> {
            // Update button states
            btnEvents.setSelected(v.getId() == R.id.btn_events);
            btnProducts.setSelected(v.getId() == R.id.btn_products);
            btnServices.setSelected(v.getId() == R.id.btn_services);

            // Move slider
            if (sliderFilter != null) {
                sliderFilter.animate().x(v.getX()).setDuration(200).start();
                sliderFilter.getLayoutParams().width = v.getWidth();
                sliderFilter.requestLayout();
            }

            // Switch category
            if (v.getId() == R.id.btn_events) {
                switchCategory("events");
            } else if (v.getId() == R.id.btn_products) {
                switchCategory("products");
            } else if (v.getId() == R.id.btn_services) {
                switchCategory("services");
            }
        };

        btnEvents.setOnClickListener(categoryClickListener);
        btnProducts.setOnClickListener(categoryClickListener);
        btnServices.setOnClickListener(categoryClickListener);

        // Set initial state
        btnEvents.setSelected(true);
        if (sliderFilter != null) {
            // Initialize slider position
            sliderFilter.post(() -> {
                sliderFilter.getLayoutParams().width = btnEvents.getWidth();
                sliderFilter.setX(btnEvents.getX());
            });
        }
    }

    private void switchCategory(String category) {
        selectedCategory = category;

        // Hide all options first
        if (eventOptions != null) eventOptions.setVisibility(View.GONE);
        if (productOptions != null) productOptions.setVisibility(View.GONE);
        if (serviceOptions != null) serviceOptions.setVisibility(View.GONE);

        // Show selected category options
        switch (category) {
            case "events":
                if (eventOptions != null) eventOptions.setVisibility(View.VISIBLE);
                updateSortOptions(getResources().getStringArray(R.array.event_sort_options));
                break;
            case "products":
                if (productOptions != null) productOptions.setVisibility(View.VISIBLE);
                updateSortOptions(getResources().getStringArray(R.array.product_sort_options));
                break;
            case "services":
                if (serviceOptions != null) serviceOptions.setVisibility(View.VISIBLE);
                updateSortOptions(getResources().getStringArray(R.array.service_sort_options));
                break;
        }

        loadData();
    }

    private void updateSortOptions(String[] options) {
        if (sortSpinner != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, options);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sortSpinner.setAdapter(adapter);
        }
    }

    private void setupEventTypes(View view) {
        LinearLayout eventTypesLayout = view.findViewById(R.id.event_types_list);
        if (eventTypesLayout != null) {
            eventTypesLayout.removeAllViews();

            for (String eventType : eventTypes) {
                CheckBox checkBox = new CheckBox(requireContext());
                checkBox.setText(eventType);
                checkBox.setTextSize(14);
                checkBox.setPadding(0, 8, 0, 8);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedEventTypes.add(eventType);
                    } else {
                        selectedEventTypes.remove(eventType);
                    }
                    loadData(); // Real-time filtering like Angular version
                });
                eventTypesLayout.addView(checkBox);
            }
        }
    }

    private void setupCategories(View view) {
        LinearLayout categoriesLayout = view.findViewById(R.id.categories_list);
        if (categoriesLayout != null) {
            categoriesLayout.removeAllViews();

            for (String category : categories) {
                CheckBox checkBox = new CheckBox(requireContext());
                checkBox.setText(category);
                checkBox.setTextSize(14);
                checkBox.setPadding(0, 8, 0, 8);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedCategories.add(category);
                    } else {
                        selectedCategories.remove(category);
                    }
                    loadData(); // Real-time filtering like Angular version
                });
                categoriesLayout.addView(checkBox);
            }
        }
    }

    private void setupCalendar(View view) {
        currentCalendar = Calendar.getInstance();
        updateCalendar();

        Button prevMonth = view.findViewById(R.id.prev_month);
        Button nextMonth = view.findViewById(R.id.next_month);

        if (prevMonth != null) {
            prevMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, -1);
                updateCalendar();
            });
        }

        if (nextMonth != null) {
            nextMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, 1);
                updateCalendar();
            });
        }
    }

    private void updateCalendar() {
        // Update month/year display
        if (currentMonthYear != null) {
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            currentMonthYear.setText(monthFormat.format(currentCalendar.getTime()));
        }
    }


    private void setupPriceRange(View view) {
        if (priceSeekBar != null) {
            priceSeekBar.setMax(100000);
            priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    priceRangeEnd = String.valueOf(progress);
                    if (priceRangeText != null) {
                        priceRangeText.setText(String.format("Price up to: $%s", formatPrice(progress)));
                    }
                    if (fromUser) {
                        loadData(); // Real-time filtering
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    private String formatPrice(int price) {
        if (price >= 1000) {
            return (price / 1000) + "k";
        }
        return String.valueOf(price);
    }

    private void setupSortSpinner() {
        // Initial sort options for events
        updateSortOptions(getResources().getStringArray(R.array.event_sort_options));

        if (sortSpinner != null) {
            sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selected = parent.getItemAtPosition(position).toString();
                    // Map UI labels to API parameters
                    switch (selected) {
                        case "Name": selectedSortOption = "name"; break;
                        case "Date": selectedSortOption = "date"; break;
                        case "Price ↑":
                            selectedSortOption = "price";
                            sortOrder = "asc";
                            break;
                        case "Price ↓":
                            selectedSortOption = "price";
                            sortOrder = "desc";
                            break;
                        case "Event Type": selectedSortOption = "eventType"; break;
                        case "Category": selectedSortOption = "category"; break;
                    }
                    loadData(); // Real-time sorting like Angular version
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void loadData() {
        String city = inputCity != null ? inputCity.getText().toString().trim() : "";

        switch (selectedCategory) {
            case "events":
                loadEvents(city);
                break;
            case "services":
                loadServices(city);
                break;
            case "products":
                Toast.makeText(getContext(), "Products coming soon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void loadEvents(String city) {
        // Use category-specific search if available, otherwise use main search
        String effectiveSearchQuery = !eventSearchQuery.isEmpty() ? eventSearchQuery : searchQuery;

        // Format dates if selected
        String startDate = null;
        String endDate = null;
        if (selectedStartDate != null) {
            startDate = formatDateForApi(selectedStartDate);
        }
        if (selectedEndDate != null) {
            endDate = formatDateForApi(selectedEndDate);
        }

        eventService.searchEvents(
                effectiveSearchQuery.isEmpty() ? null : effectiveSearchQuery,
                selectedEventTypes.isEmpty() ? null : selectedEventTypes,
                "PUBLIC", // status
                startDate,
                endDate,
                city.isEmpty() ? null : city,
                null, // userEmail
                selectedSortOption,
                sortOrder
        ).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null && recyclerView != null) {
                    recyclerView.setAdapter(new EventAdapter(response.body()));
                } else {
                    Toast.makeText(getContext(), "No events found", Toast.LENGTH_SHORT).show();
                    if (recyclerView != null) {
                        recyclerView.setAdapter(new EventAdapter(new ArrayList<>()));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadServices(String city) {
        // Use category-specific search if available, otherwise use main search
        String effectiveSearchQuery = !serviceSearchQuery.isEmpty() ? serviceSearchQuery : searchQuery;

        // Handle null values properly
        String searchParam = effectiveSearchQuery.isEmpty() ? null : effectiveSearchQuery;
        List<String> categoriesParam = selectedCategories.isEmpty() ? null : selectedCategories;
        String cityParam = city.isEmpty() ? null : city;

        // Parse price ranges with null checks
        Integer minPrice, maxPrice;
        try {
            minPrice = Integer.valueOf(priceRangeStart);
            maxPrice = Integer.valueOf(priceRangeEnd);
        } catch (NumberFormatException e) {
            minPrice = 0;
            maxPrice = 100000;
        }

        // Use the existing getServices method
        serviceService.getServices(
                searchParam,
                "AVAILABLE", // status
                cityParam,
                null, // startDate
                null, // endDate
                minPrice,
                maxPrice,
                selectedEventTypes.isEmpty() ? null : selectedEventTypes,
                categoriesParam,
                null, // userEmail
                selectedSortOption,
                sortOrder
        ).enqueue(new Callback<List<Service>>() {
            @Override
            public void onResponse(Call<List<Service>> call, Response<List<Service>> response) {
                if (response.isSuccessful() && response.body() != null && recyclerView != null) {
                    recyclerView.setAdapter(new ServiceAdapter((ArrayList<Service>) response.body()));
                } else {
                    Toast.makeText(getContext(), "No services found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Service>> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading services", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDateForApi(Calendar calendar) {
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return apiFormat.format(calendar.getTime());
    }

    // Method to handle date selection from calendar
    public void onDateSelected(Calendar date) {
        if (selectedStartDate == null || (selectedStartDate != null && selectedEndDate != null)) {
            selectedStartDate = date;
            selectedEndDate = null;
        } else if (selectedStartDate != null && selectedEndDate != null) {
            if (date.after(selectedStartDate)) {
                selectedEndDate = date;
            } else {
                selectedStartDate = date;
            }
        }
        loadData(); // Real-time filtering
    }
}