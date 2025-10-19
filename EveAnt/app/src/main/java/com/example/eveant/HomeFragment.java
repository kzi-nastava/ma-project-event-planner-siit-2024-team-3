package com.example.eveant;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.viewpager2.widget.ViewPager2;
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
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;
    private FrameLayout searchResultsContainer;
    private boolean isSearchResultsVisible = false;

    // Services for search functionality
    private EventService eventService;
    private ServiceService serviceService;

    // Current search state
    private String currentSearchQuery = "";
    private String selectedCategory = "events";
    private List<String> selectedEventTypes = new ArrayList<>();
    private List<String> selectedCategories = new ArrayList<>();

    // Service-specific filters
    private Calendar serviceStartDate = null;
    private Calendar serviceEndDate = null;
    private int serviceMinPrice = 0;
    private int serviceMaxPrice = 100000;
    private Calendar currentServiceMonth;

    // Event-specific filters
    private Calendar eventStartDate = null;
    private Calendar eventEndDate = null;
    private Calendar currentEventMonth;

    // Product-specific filters
    private int productMinPrice = 0;
    private int productMaxPrice = 100000;

    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Views
    private EditText homeSearchEditText;
    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize services
        eventService = RetrofitClient.retrofit.create(EventService.class);
        serviceService = RetrofitClient.retrofit.create(ServiceService.class);

        // Initialize views
        viewPager = view.findViewById(R.id.home_search_rectangle_layout);
        searchResultsContainer = view.findViewById(R.id.search_results_container);
        homeSearchEditText = view.findViewById(R.id.search_home);

        // Initialize carousel
        setupCarousel(view);

        // Initialize search and filter functionality
        setupSearchAndFilter();
    }

    private void setupCarousel(View view) {
        List<SlideItem> slideItems = new ArrayList<>();
        slideItems.add(new SlideItem(R.drawable.darko_rundek, "Darko Rundek", "Novosadski sajam", "21:00", "30\nNOV"));
        slideItems.add(new SlideItem(R.drawable.zimzolend, "Zimzolend", "Novi Sad, Centar", "20:00", "23\nDEC"));
        ImagePagerAdapter adapter = new ImagePagerAdapter(requireContext(), slideItems);
        viewPager.setAdapter(adapter);

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == adapter.getItemCount()) currentPage = 0;
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(runnable, 3000);
    }

    private void setupSearchAndFilter() {
        RelativeLayout filterButton = getView().findViewById(R.id.filter_button);

        if (homeSearchEditText != null) {
            homeSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = homeSearchEditText.getText().toString().trim();
                    currentSearchQuery = query;
                    Log.d(TAG, "Search action - Query: '" + currentSearchQuery + "'");
                    showSearchResults();
                    return true;
                }
                return false;
            });

            // Real-time search updates when overlay is visible
            homeSearchEditText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    if (isSearchResultsVisible) {
                        currentSearchQuery = s.toString().trim();
                        Log.d(TAG, "Text changed - Query: '" + currentSearchQuery + "'");
                        refreshSearchResults();
                    }
                }
            });
        }

        if (filterButton != null) {
            filterButton.setOnClickListener(v -> {
                if (isSearchResultsVisible) {
                    hideSearchResults();
                } else {
                    // Get current search query from the home search bar
                    if (homeSearchEditText != null) {
                        currentSearchQuery = homeSearchEditText.getText().toString().trim();
                    }
                    Log.d(TAG, "Filter button clicked - Query: '" + currentSearchQuery + "'");
                    showSearchResults();
                }
            });
        }
    }

    private void showSearchResults() {
        if (searchResultsContainer == null) return;

        // Clear any existing search results
        searchResultsContainer.removeAllViews();

        // Inflate the search results layout
        View searchResultsView = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_search_results, searchResultsContainer, false);

        // Add it to the container
        searchResultsContainer.addView(searchResultsView);

        // Initialize the SearchResults functionality
        initializeSearchResults(searchResultsView);

        // Show the container
        searchResultsContainer.setVisibility(View.VISIBLE);
        isSearchResultsVisible = true;

        // Load data with current search query
        Log.d(TAG, "Showing search results with query: '" + currentSearchQuery + "'");
        loadSearchResultsData(searchResultsView);
    }

    private void hideSearchResults() {
        if (searchResultsContainer != null) {
            searchResultsContainer.setVisibility(View.GONE);
            searchResultsContainer.removeAllViews();
            isSearchResultsVisible = false;
            Log.d(TAG, "Hiding search results");
        }
    }

    private void refreshSearchResults() {
        if (searchResultsContainer != null && searchResultsContainer.getChildCount() > 0) {
            View searchResultsView = searchResultsContainer.getChildAt(0);
            Log.d(TAG, "Refreshing search results with query: '" + currentSearchQuery + "'");
            loadSearchResultsData(searchResultsView);
        }
    }

    private void initializeSearchResults(View searchResultsView) {
        // Initialize RecyclerView
        RecyclerView recyclerView = searchResultsView.findViewById(R.id.recycler_search_results);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setNestedScrollingEnabled(false);
        }

        // Set up category buttons
        setupCategoryButtons(searchResultsView);

        // Set up sort spinner
        setupSortSpinner(searchResultsView);

        // Set up event types and categories
        setupFilterOptions(searchResultsView);

        // Set up city input listener
        setupCityInputListener(searchResultsView);

        // Set up service-specific filters
        setupServiceFilters(searchResultsView);

        // Set up event-specific filters
        setupEventFilters(searchResultsView);

        // Set up product-specific filters
        setupProductFilters(searchResultsView);
    }

    private void setupCityInputListener(View searchResultsView) {
        EditText inputCity = searchResultsView.findViewById(R.id.input_city);
        if (inputCity != null) {
            inputCity.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    // Refresh results when city changes
                    refreshSearchResults();
                }
            });
        }
    }

    private void setupCategoryButtons(View searchResultsView) {
        Button btnEvents = searchResultsView.findViewById(R.id.btn_events);
        Button btnProducts = searchResultsView.findViewById(R.id.btn_products);
        Button btnServices = searchResultsView.findViewById(R.id.btn_services);

        if (btnEvents != null && btnProducts != null && btnServices != null) {
            View.OnClickListener categoryClickListener = v -> {
                btnEvents.setSelected(v.getId() == R.id.btn_events);
                btnProducts.setSelected(v.getId() == R.id.btn_products);
                btnServices.setSelected(v.getId() == R.id.btn_services);

                // Handle category switching
                if (v.getId() == R.id.btn_events) {
                    selectedCategory = "events";
                    Log.d(TAG, "Category switched to: events");
                    switchSearchCategory(searchResultsView);
                } else if (v.getId() == R.id.btn_products) {
                    selectedCategory = "products";
                    Log.d(TAG, "Category switched to: products");
                    switchSearchCategory(searchResultsView);
                } else if (v.getId() == R.id.btn_services) {
                    selectedCategory = "services";
                    Log.d(TAG, "Category switched to: services");
                    switchSearchCategory(searchResultsView);
                }
            };

            btnEvents.setOnClickListener(categoryClickListener);
            btnProducts.setOnClickListener(categoryClickListener);
            btnServices.setOnClickListener(categoryClickListener);

            // Set initial state
            btnEvents.setSelected(true);
            selectedCategory = "events";
        }
    }

    private void switchSearchCategory(View searchResultsView) {
        // Show/hide filter options based on category
        LinearLayout eventOptions = searchResultsView.findViewById(R.id.event_options);
        LinearLayout productOptions = searchResultsView.findViewById(R.id.product_options);
        LinearLayout serviceOptions = searchResultsView.findViewById(R.id.service_options);

        if (eventOptions != null) eventOptions.setVisibility(View.GONE);
        if (productOptions != null) productOptions.setVisibility(View.GONE);
        if (serviceOptions != null) serviceOptions.setVisibility(View.GONE);

        switch (selectedCategory) {
            case "events":
                if (eventOptions != null) eventOptions.setVisibility(View.VISIBLE);
                break;
            case "products":
                if (productOptions != null) productOptions.setVisibility(View.VISIBLE);
                break;
            case "services":
                if (serviceOptions != null) serviceOptions.setVisibility(View.VISIBLE);
                break;
        }

        // Reload data for the selected category
        refreshSearchResults();
    }

    private void setupSortSpinner(View searchResultsView) {
        Spinner sortSpinner = searchResultsView.findViewById(R.id.sort_spinner);
        if (sortSpinner != null) {
            String[] sortOptions = {"Name", "Date", "Price ↑", "Price ↓"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, sortOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sortSpinner.setAdapter(adapter);

            sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    refreshSearchResults();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void setupFilterOptions(View searchResultsView) {
        // Setup event types for events
        LinearLayout eventTypesLayout = searchResultsView.findViewById(R.id.event_types_list);
        if (eventTypesLayout != null) {
            String[] eventTypes = {"Conference", "Workshop", "Concert", "Festival", "Sports"};
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
                    Log.d(TAG, "Event types updated: " + selectedEventTypes);
                    refreshSearchResults();
                });
                eventTypesLayout.addView(checkBox);
            }
        }

        // Setup categories for products
        LinearLayout categoriesLayout = searchResultsView.findViewById(R.id.categories_list);
        if (categoriesLayout != null) {
            String[] categories = {"Electronics", "Clothing", "Food", "Decoration", "Photography"};
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
                    Log.d(TAG, "Categories updated: " + selectedCategories);
                    refreshSearchResults();
                });
                categoriesLayout.addView(checkBox);
            }
        }

        // Setup service-specific event types
        LinearLayout serviceEventTypesLayout = searchResultsView.findViewById(R.id.service_event_types);
        if (serviceEventTypesLayout != null) {
            String[] serviceEventTypes = {"Conference", "Workshop", "Concert", "Festival", "Sports"};
            for (String eventType : serviceEventTypes) {
                CheckBox checkBox = new CheckBox(requireContext());
                checkBox.setText(eventType);
                checkBox.setTextSize(12);
                checkBox.setPadding(0, 4, 0, 4);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedEventTypes.add(eventType);
                    } else {
                        selectedEventTypes.remove(eventType);
                    }
                    Log.d(TAG, "Service event types updated: " + selectedEventTypes);
                    refreshSearchResults();
                });
                serviceEventTypesLayout.addView(checkBox);
            }
        }

        // Setup service-specific categories
        LinearLayout serviceCategoriesLayout = searchResultsView.findViewById(R.id.service_categories);
        if (serviceCategoriesLayout != null) {
            String[] serviceCategories = {"Catering", "Photography", "Decoration", "Entertainment", "Venue"};
            for (String category : serviceCategories) {
                CheckBox checkBox = new CheckBox(requireContext());
                checkBox.setText(category);
                checkBox.setTextSize(12);
                checkBox.setPadding(0, 4, 0, 4);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedCategories.add(category);
                    } else {
                        selectedCategories.remove(category);
                    }
                    Log.d(TAG, "Service categories updated: " + selectedCategories);
                    refreshSearchResults();
                });
                serviceCategoriesLayout.addView(checkBox);
            }
        }
    }

    private void setupServiceFilters(View searchResultsView) {
        setupServiceCalendar(searchResultsView);
        setupServicePriceRange(searchResultsView);
    }

    private void setupEventFilters(View searchResultsView) {
        setupEventCalendar(searchResultsView);
    }

    private void setupProductFilters(View searchResultsView) {
        setupProductPriceRange(searchResultsView);
    }

    private void setupServiceCalendar(View searchResultsView) {
        // Initialize current month
        currentServiceMonth = Calendar.getInstance();

        Button prevMonthBtn = searchResultsView.findViewById(R.id.service_prev_month);
        Button nextMonthBtn = searchResultsView.findViewById(R.id.service_next_month);
        TextView monthYearText = searchResultsView.findViewById(R.id.service_month_year);
        GridLayout calendarGrid = searchResultsView.findViewById(R.id.service_calendar_grid);
        TextView dateRangeText = searchResultsView.findViewById(R.id.service_date_range_text);

        // Update calendar display
        updateServiceCalendar(monthYearText, calendarGrid, dateRangeText);

        // Month navigation
        if (prevMonthBtn != null) {
            prevMonthBtn.setOnClickListener(v -> {
                currentServiceMonth.add(Calendar.MONTH, -1);
                updateServiceCalendar(monthYearText, calendarGrid, dateRangeText);
            });
        }

        if (nextMonthBtn != null) {
            nextMonthBtn.setOnClickListener(v -> {
                currentServiceMonth.add(Calendar.MONTH, 1);
                updateServiceCalendar(monthYearText, calendarGrid, dateRangeText);
            });
        }
    }

    private void setupEventCalendar(View searchResultsView) {
        // Initialize current month
        currentEventMonth = Calendar.getInstance();

        Button prevMonthBtn = searchResultsView.findViewById(R.id.event_prev_month);
        Button nextMonthBtn = searchResultsView.findViewById(R.id.event_next_month);
        TextView monthYearText = searchResultsView.findViewById(R.id.event_month_year);
        GridLayout calendarGrid = searchResultsView.findViewById(R.id.event_calendar_grid);
        TextView dateRangeText = searchResultsView.findViewById(R.id.event_date_range_text);

        // Update calendar display
        updateEventCalendar(monthYearText, calendarGrid, dateRangeText);

        // Month navigation
        if (prevMonthBtn != null) {
            prevMonthBtn.setOnClickListener(v -> {
                currentEventMonth.add(Calendar.MONTH, -1);
                updateEventCalendar(monthYearText, calendarGrid, dateRangeText);
            });
        }

        if (nextMonthBtn != null) {
            nextMonthBtn.setOnClickListener(v -> {
                currentEventMonth.add(Calendar.MONTH, 1);
                updateEventCalendar(monthYearText, calendarGrid, dateRangeText);
            });
        }
    }

    private void updateServiceCalendar(TextView monthYearText, GridLayout calendarGrid, TextView dateRangeText) {
        updateCalendar(monthYearText, calendarGrid, dateRangeText, currentServiceMonth, serviceStartDate, serviceEndDate);
    }

    private void updateEventCalendar(TextView monthYearText, GridLayout calendarGrid, TextView dateRangeText) {
        updateCalendar(monthYearText, calendarGrid, dateRangeText, currentEventMonth, eventStartDate, eventEndDate);
    }

    private void updateCalendar(TextView monthYearText, GridLayout calendarGrid, TextView dateRangeText,
                                Calendar currentMonth, Calendar startDate, Calendar endDate) {
        if (monthYearText != null) {
            monthYearText.setText(monthYearFormat.format(currentMonth.getTime()));
        }

        if (calendarGrid != null) {
            calendarGrid.removeAllViews();

            // Add day headers
            String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String day : days) {
                TextView dayHeader = new TextView(requireContext());
                dayHeader.setText(day);
                dayHeader.setGravity(android.view.Gravity.CENTER);
                dayHeader.setTextSize(12);
                dayHeader.setPadding(4, 8, 4, 8);
                calendarGrid.addView(dayHeader);
            }

            // Get first day of month and number of days
            Calendar calendar = (Calendar) currentMonth.clone();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Add empty cells for days before the first day of month
            for (int i = 1; i < firstDayOfWeek; i++) {
                TextView emptyView = new TextView(requireContext());
                calendarGrid.addView(emptyView);
            }

            // Add day buttons
            for (int day = 1; day <= daysInMonth; day++) {
                Button dayButton = new Button(requireContext());
                dayButton.setText(String.valueOf(day));
                dayButton.setPadding(4, 8, 4, 8);
                dayButton.setTextSize(10);

                final int currentDay = day;
                final Calendar dayCalendar = (Calendar) currentMonth.clone();
                dayCalendar.set(Calendar.DAY_OF_MONTH, currentDay);

                // Check if this day is selected
                if (isDateSelected(dayCalendar, startDate, endDate)) {
                    dayButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                }

                dayButton.setOnClickListener(v -> {
                    if (calendarGrid.getId() == R.id.service_calendar_grid) {
                        handleServiceDateSelection(dayCalendar, dateRangeText);
                        updateServiceCalendar(monthYearText, calendarGrid, dateRangeText);
                    } else if (calendarGrid.getId() == R.id.event_calendar_grid) {
                        handleEventDateSelection(dayCalendar, dateRangeText);
                        updateEventCalendar(monthYearText, calendarGrid, dateRangeText);
                    }
                });

                calendarGrid.addView(dayButton);
            }
        }

        if (calendarGrid.getId() == R.id.service_calendar_grid) {
            updateServiceDateRangeText(dateRangeText);
        } else if (calendarGrid.getId() == R.id.event_calendar_grid) {
            updateEventDateRangeText(dateRangeText);
        }
    }

    private boolean isDateSelected(Calendar date, Calendar startDate, Calendar endDate) {
        if (startDate != null && endDate != null) {
            return !date.before(startDate) && !date.after(endDate);
        } else if (startDate != null) {
            return isSameDay(date, startDate);
        }
        return false;
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void handleServiceDateSelection(Calendar selectedDate, TextView dateRangeText) {
        if (serviceStartDate == null) {
            // First selection - set start date
            serviceStartDate = (Calendar) selectedDate.clone();
            serviceEndDate = null;
        } else if (serviceEndDate == null) {
            // Second selection - set end date
            if (selectedDate.before(serviceStartDate)) {
                serviceEndDate = (Calendar) serviceStartDate.clone();
                serviceStartDate = (Calendar) selectedDate.clone();
            } else {
                serviceEndDate = (Calendar) selectedDate.clone();
            }
        } else {
            // Reset selection
            serviceStartDate = (Calendar) selectedDate.clone();
            serviceEndDate = null;
        }

        updateServiceDateRangeText(dateRangeText);
        refreshSearchResults();
    }

    private void handleEventDateSelection(Calendar selectedDate, TextView dateRangeText) {
        if (eventStartDate == null) {
            // First selection - set start date
            eventStartDate = (Calendar) selectedDate.clone();
            eventEndDate = null;
        } else if (eventEndDate == null) {
            // Second selection - set end date
            if (selectedDate.before(eventStartDate)) {
                eventEndDate = (Calendar) eventStartDate.clone();
                eventStartDate = (Calendar) selectedDate.clone();
            } else {
                eventEndDate = (Calendar) selectedDate.clone();
            }
        } else {
            // Reset selection
            eventStartDate = (Calendar) selectedDate.clone();
            eventEndDate = null;
        }

        updateEventDateRangeText(dateRangeText);
        refreshSearchResults();
    }

    private void updateServiceDateRangeText(TextView dateRangeText) {
        updateDateRangeText(dateRangeText, serviceStartDate, serviceEndDate);
    }

    private void updateEventDateRangeText(TextView dateRangeText) {
        updateDateRangeText(dateRangeText, eventStartDate, eventEndDate);
    }

    private void updateDateRangeText(TextView dateRangeText, Calendar startDate, Calendar endDate) {
        if (dateRangeText == null) return;

        if (startDate != null && endDate != null) {
            String rangeText = String.format("Selected: %s - %s",
                    apiDateFormat.format(startDate.getTime()),
                    apiDateFormat.format(endDate.getTime()));
            dateRangeText.setText(rangeText);
        } else if (startDate != null) {
            String rangeText = String.format("Selected: %s (select end date)",
                    apiDateFormat.format(startDate.getTime()));
            dateRangeText.setText(rangeText);
        } else {
            dateRangeText.setText("No date range selected");
        }
    }

    private void setupServicePriceRange(View searchResultsView) {
        setupPriceRange(
                searchResultsView.findViewById(R.id.service_min_price_seek_bar),
                searchResultsView.findViewById(R.id.service_max_price_seek_bar),
                searchResultsView.findViewById(R.id.service_min_price_text),
                searchResultsView.findViewById(R.id.service_max_price_text),
                serviceMinPrice,
                serviceMaxPrice,
                (min, max) -> {
                    serviceMinPrice = min;
                    serviceMaxPrice = max;
                    refreshSearchResults();
                }
        );
    }

    private void setupProductPriceRange(View searchResultsView) {
        setupPriceRange(
                searchResultsView.findViewById(R.id.product_min_price_seek_bar),
                searchResultsView.findViewById(R.id.product_max_price_seek_bar),
                searchResultsView.findViewById(R.id.product_min_price_text),
                searchResultsView.findViewById(R.id.product_max_price_text),
                productMinPrice,
                productMaxPrice,
                (min, max) -> {
                    productMinPrice = min;
                    productMaxPrice = max;
                    refreshSearchResults();
                }
        );
    }

    private void setupPriceRange(SeekBar minPriceSeekBar, SeekBar maxPriceSeekBar,
                                 TextView minPriceText, TextView maxPriceText,
                                 int currentMinPrice, int currentMaxPrice,
                                 PriceRangeChangeListener listener) {

        // Create final copies for use in inner classes
        final int[] minPrice = {currentMinPrice};
        final int[] maxPrice = {currentMaxPrice};

        if (minPriceSeekBar != null && minPriceText != null) {
            minPriceSeekBar.setProgress(minPrice[0]);
            updateMinPriceText(minPriceText, minPrice[0]);

            minPriceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Round to nearest 100 for better UX
                    int roundedProgress = (progress / 100) * 100;
                    updateMinPriceText(minPriceText, roundedProgress);

                    // Ensure min price doesn't exceed max price
                    if (roundedProgress > maxPrice[0] && maxPriceSeekBar != null) {
                        maxPriceSeekBar.setProgress(roundedProgress);
                        maxPrice[0] = roundedProgress;
                        updateMaxPriceText(maxPriceText, maxPrice[0]);
                        listener.onPriceRangeChanged(roundedProgress, maxPrice[0]);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int newMinPrice = (seekBar.getProgress() / 100) * 100;
                    minPrice[0] = newMinPrice;
                    listener.onPriceRangeChanged(newMinPrice, maxPrice[0]);
                }
            });
        }

        if (maxPriceSeekBar != null && maxPriceText != null) {
            maxPriceSeekBar.setProgress(maxPrice[0]);
            updateMaxPriceText(maxPriceText, maxPrice[0]);

            maxPriceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Round to nearest 100 for better UX
                    int roundedProgress = (progress / 100) * 100;
                    if (roundedProgress == 0) roundedProgress = 100; // Minimum $100
                    updateMaxPriceText(maxPriceText, roundedProgress);

                    // Ensure max price doesn't go below min price
                    if (roundedProgress < minPrice[0] && minPriceSeekBar != null) {
                        minPriceSeekBar.setProgress(roundedProgress);
                        minPrice[0] = roundedProgress;
                        updateMinPriceText(minPriceText, minPrice[0]);
                        listener.onPriceRangeChanged(minPrice[0], roundedProgress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int newMaxPrice = (seekBar.getProgress() / 100) * 100;
                    if (newMaxPrice == 0) newMaxPrice = 100;
                    maxPrice[0] = newMaxPrice;
                    listener.onPriceRangeChanged(minPrice[0], newMaxPrice);
                }
            });
        }
    }

    private void updateMinPriceText(TextView priceText, int price) {
        if (priceText != null) {
            String priceTextStr = String.format("Minimum price: $%,d", price);
            priceText.setText(priceTextStr);
        }
    }

    private void updateMaxPriceText(TextView priceText, int price) {
        if (priceText != null) {
            String priceTextStr = String.format("Maximum price: $%,d", price);
            priceText.setText(priceTextStr);
        }
    }

    private void loadSearchResultsData(View searchResultsView) {
        RecyclerView recyclerView = searchResultsView.findViewById(R.id.recycler_search_results);
        EditText inputCity = searchResultsView.findViewById(R.id.input_city);

        String city = inputCity != null ? inputCity.getText().toString().trim() : "";

        // Get sort option
        Spinner sortSpinner = searchResultsView.findViewById(R.id.sort_spinner);
        String sortBy = "date";
        String order = "asc";
        if (sortSpinner != null && sortSpinner.getSelectedItem() != null) {
            String selectedSort = sortSpinner.getSelectedItem().toString();
            switch (selectedSort) {
                case "Name":
                    sortBy = "name";
                    break;
                case "Date":
                    sortBy = "date";
                    break;
                case "Price ↑":
                    sortBy = "price";
                    order = "asc";
                    break;
                case "Price ↓":
                    sortBy = "price";
                    order = "desc";
                    break;
            }
        }

        // Debug logging to see exactly what's being sent to the API
        Log.d(TAG, "API Call - Query: '" + currentSearchQuery +
                "', Category: " + selectedCategory +
                ", City: '" + city + "'" +
                ", Event Types: " + selectedEventTypes +
                ", Categories: " + selectedCategories +
                ", Service Start Date: " + (serviceStartDate != null ? apiDateFormat.format(serviceStartDate.getTime()) : "null") +
                ", Service End Date: " + (serviceEndDate != null ? apiDateFormat.format(serviceEndDate.getTime()) : "null") +
                ", Service Min Price: " + serviceMinPrice +
                ", Service Max Price: " + serviceMaxPrice +
                ", Event Start Date: " + (eventStartDate != null ? apiDateFormat.format(eventStartDate.getTime()) : "null") +
                ", Event End Date: " + (eventEndDate != null ? apiDateFormat.format(eventEndDate.getTime()) : "null") +
                ", Product Min Price: " + productMinPrice +
                ", Product Max Price: " + productMaxPrice);

        switch (selectedCategory) {
            case "events":
                loadEvents(recyclerView, currentSearchQuery, city, sortBy, order);
                break;
            case "products":
                loadProducts(recyclerView, currentSearchQuery, city, sortBy, order);
                break;
            case "services":
                loadServices(recyclerView, currentSearchQuery, city, sortBy, order);
                break;
        }
    }

    private void loadEvents(RecyclerView recyclerView, String searchQuery, String city, String sortBy, String order) {
        if (recyclerView == null) return;

        Log.d(TAG, "Loading events with search: '" + searchQuery + "', city: '" + city + "'");

        // Prepare date parameters for events
        String startDateParam = null;
        String endDateParam = null;
        if (eventStartDate != null && eventEndDate != null) {
            startDateParam = apiDateFormat.format(eventStartDate.getTime());
            endDateParam = apiDateFormat.format(eventEndDate.getTime());
        }

        Call<List<Event>> call = eventService.searchEvents(
                searchQuery.isEmpty() ? null : searchQuery,
                selectedEventTypes.isEmpty() ? null : selectedEventTypes,
                "PUBLIC",
                startDateParam,
                endDateParam,
                city.isEmpty() ? null : city,
                null, // userEmail
                sortBy,
                order
        );

        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recyclerView.setAdapter(new EventAdapter(response.body()));
                    Log.d(TAG, "Events loaded: " + response.body().size() + " items for query: '" + searchQuery + "'");
                    Toast.makeText(getContext(), "Found " + response.body().size() + " events for: " + (searchQuery.isEmpty() ? "all events" : searchQuery), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "No events found for query: '" + searchQuery + "'");
                    Toast.makeText(getContext(), "No events found for: " + (searchQuery.isEmpty() ? "all events" : searchQuery), Toast.LENGTH_SHORT).show();
                    recyclerView.setAdapter(new EventAdapter(new ArrayList<>()));
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Log.e(TAG, "Error loading events: " + t.getMessage());
                Toast.makeText(getContext(), "Error loading events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts(RecyclerView recyclerView, String searchQuery, String city, String sortBy, String order) {
        if (recyclerView == null) return;

        Log.d(TAG, "Loading products with search: '" + searchQuery + "', city: '" + city + "'");

        // TODO: Replace with actual product service call when available
        // For now, show a message that products are coming soon
        Toast.makeText(getContext(), "Products with price filtering coming soon! Min: $" + productMinPrice + ", Max: $" + productMaxPrice, Toast.LENGTH_SHORT).show();

        // Clear any existing results
        recyclerView.setAdapter(new EventAdapter(new ArrayList<>()));

        /*
        // Example of how the product service call would look when implemented:
        Call<List<Product>> call = productService.searchProducts(
                searchQuery.isEmpty() ? null : searchQuery,
                selectedCategories.isEmpty() ? null : selectedCategories,
                city.isEmpty() ? null : city,
                productMinPrice,
                productMaxPrice,
                null, // userEmail
                sortBy,
                order
        );

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recyclerView.setAdapter(new ProductAdapter(response.body()));
                    Log.d(TAG, "Products loaded: " + response.body().size() + " items for query: '" + searchQuery + "'");
                    Toast.makeText(getContext(), "Found " + response.body().size() + " products for: " + (searchQuery.isEmpty() ? "all products" : searchQuery), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "No products found for query: '" + searchQuery + "'");
                    Toast.makeText(getContext(), "No products found for: " + (searchQuery.isEmpty() ? "all products" : searchQuery), Toast.LENGTH_SHORT).show();
                    recyclerView.setAdapter(new ProductAdapter(new ArrayList<>()));
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Error loading products: " + t.getMessage());
                Toast.makeText(getContext(), "Error loading products", Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    private void loadServices(RecyclerView recyclerView, String searchQuery, String city, String sortBy, String order) {
        if (recyclerView == null) return;

        Log.d(TAG, "Loading services with search: '" + searchQuery + "', city: '" + city + "'");

        // Prepare date parameters for services
        //
        String startDateParam = null;
        String endDateParam = null;
        if (serviceStartDate != null && serviceEndDate != null) {
            startDateParam = apiDateFormat.format(serviceStartDate.getTime());
            endDateParam = apiDateFormat.format(serviceEndDate.getTime());
        }

        Call<List<Service>> call = serviceService.getServices(
                searchQuery.isEmpty() ? null : searchQuery,
                "AVAILABLE",
                city.isEmpty() ? null : city,
                startDateParam,
                endDateParam,
                serviceMinPrice, // minPrice
                serviceMaxPrice, // maxPrice
                selectedEventTypes.isEmpty() ? null : selectedEventTypes,
                selectedCategories.isEmpty() ? null : selectedCategories,
                null, // userEmail
                sortBy,
                order
        );

        call.enqueue(new Callback<List<Service>>() {
            @Override
            public void onResponse(Call<List<Service>> call, Response<List<Service>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recyclerView.setAdapter(new ServiceAdapter((ArrayList<Service>) response.body(), null));
                    Log.d(TAG, "Services loaded: " + response.body().size() + " items for query: '" + searchQuery + "'");
                    Toast.makeText(getContext(), "Found " + response.body().size() + " services for: " + (searchQuery.isEmpty() ? "all services" : searchQuery), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "No services found for query: '" + searchQuery + "'");
                    Toast.makeText(getContext(), "No services found for: " + (searchQuery.isEmpty() ? "all services" : searchQuery), Toast.LENGTH_SHORT).show();
                    recyclerView.setAdapter(new ServiceAdapter(new ArrayList<>(), null));
                }
            }

            @Override
            public void onFailure(Call<List<Service>> call, Throwable t) {
                Log.e(TAG, "Error loading services: " + t.getMessage());
                Toast.makeText(getContext(), "Error loading services", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    // Interface for price range changes
    private interface PriceRangeChangeListener {
        void onPriceRangeChanged(int minPrice, int maxPrice);
    }
}