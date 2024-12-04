package com.example.eveant;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private Handler handler;
    private Runnable runnable;
    private RelativeLayout filterButton;
    private int currentPage = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        filterButton = view.findViewById(R.id.filter_button);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterBottomSheet();
            }
        });
        return view;
    }


    private void showFilterBottomSheet() {
        // Kreiramo BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        View bottomSheetView = LayoutInflater.from(getContext())
                .inflate(R.layout.filter_bottom_sheet, getActivity().findViewById(android.R.id.content), false);

        SeekBar priceSeekBar = bottomSheetView.findViewById(R.id.price_seekbar);
        TextView currentPriceText = bottomSheetView.findViewById(R.id.current_price_text);
        CheckBox availabilityCheckbox = bottomSheetView.findViewById(R.id.availability_checkbox);
        Button applyFiltersButton = bottomSheetView.findViewById(R.id.apply_filters_button);

        // Nizovi sa podacima (može se zameniti podacima iz baze)
        String[] categories = {"Party", "Wedding", "Conference", "Birthday"};
        String[] eventTypes = {"Standard", "VIP", "Premium"};

        // Kontejneri za dinamičke CheckBox-ove
        LinearLayout categoryContainer = bottomSheetView.findViewById(R.id.category_container);
        LinearLayout eventTypeContainer = bottomSheetView.findViewById(R.id.event_type_container);

        // Dinamičko dodavanje CheckBox-ova za kategorije
        List<CheckBox> categoryCheckBoxes = new ArrayList<>();
        for (String category : categories) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(category);
            checkBox.setTextColor(getResources().getColor(R.color.black));
            checkBox.setButtonTintList(getResources().getColorStateList(R.color.purple));
            categoryContainer.addView(checkBox);
            categoryCheckBoxes.add(checkBox);
        }

        // Dinamičko dodavanje CheckBox-ova za tipove eventa
        List<CheckBox> eventTypeCheckBoxes = new ArrayList<>();
        for (String eventType : eventTypes) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(eventType);
            checkBox.setTextColor(getResources().getColor(R.color.black));
            checkBox.setButtonTintList(getResources().getColorStateList(R.color.purple));
            eventTypeContainer.addView(checkBox);
            eventTypeCheckBoxes.add(checkBox);
        }

        // Listener za SeekBar promenu vrednosti
        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Prikaz trenutne cene
                currentPriceText.setText("Price: " + progress + "€");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Opcionalno: dodaj logiku ako je potrebna prilikom početka dodira
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Opcionalno: dodaj logiku ako je potrebna prilikom završetka dodira
            }
        });

        // Postavljanje dugmeta za primenu filtera
        applyFiltersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPrice = priceSeekBar.getProgress();
                boolean isAvailableOnly = availabilityCheckbox.isChecked();

                // Prikazivanje rezultata filtriranja
                Toast.makeText(getContext(),
                        "Filter: Price up to: " + selectedPrice + "€, Availability: " + isAvailableOnly,
                        Toast.LENGTH_SHORT).show();

                // Zatvaranje BottomSheet-a
                bottomSheetDialog.dismiss();
            }
        });

        // Prikaz BottomSheet-a
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = view.findViewById(R.id.home_search_rectangle_layout);

        // Add your image resource IDs to the list
        List<SlideItem> slideItems = new ArrayList<>();
        slideItems.add(new SlideItem(R.drawable.darko_rundek, "Darko Rundek", "Novosadski sajam", "21:00", "30\nNOV"));
        slideItems.add(new SlideItem(R.drawable.zimzolend, "Zimzolend", "Novi Sad, Centar", "20:00", "23\nDEC"));
        slideItems.add(new SlideItem(R.drawable.darko_rundek, "Darko Rundek", "Novosadski sajam", "21:00", "30\nNOV"));
        slideItems.add(new SlideItem(R.drawable.zimzolend, "Zimzolend", "Novi Sad, Centar", "20:00", "23\nDEC"));

        ImagePagerAdapter adapter = new ImagePagerAdapter(requireContext(), slideItems);
        viewPager.setAdapter(adapter);

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == adapter.getItemCount()) {
                    currentPage = 0; // Reset to first page
                }
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, 3000); // Slide every 3 seconds
            }
        };

        // Start auto-slide
        handler.postDelayed(runnable, 3000);

        // Optional: Stop auto-slide on user interaction
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position + 1;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable); // Prevent memory leaks
    }
}
