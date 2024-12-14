package com.example.eveant;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;

import com.example.eveant.serviceCreate.ServiceCreateFragment;
import com.example.eveant.serviceEdit.ServiceEditFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ServicesViewFragment extends Fragment {

    private RelativeLayout filterButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_services_view, container, false);


        // Povezivanje dugmeta za filtere
        filterButton = view.findViewById(R.id.filter_button);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterBottomSheet();
            }
        });

        // Edit dugme - otvara novu aktivnost
        ImageButton editServiceButton = view.findViewById(R.id.editServiceButton);
        editServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = ((MainActivity) getActivity()).getNavController();
                navController.navigate(R.id.serviceEditFragment);
            }
        });

        // Delete dugme - prikazuje dijalog
        ImageButton deleteServiceButton = view.findViewById(R.id.deleteServiceButton);
        deleteServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });

        ImageButton viewMoreButton = view.findViewById(R.id.viewMoreButton);
        viewMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = ((MainActivity) getActivity()).getNavController();
                navController.navigate(R.id.serviceDetailsFragment);
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
                        "Filter: Cena do " + selectedPrice + "€, Dostupnost: " + isAvailableOnly,
                        Toast.LENGTH_SHORT).show();

                // Zatvaranje BottomSheet-a
                bottomSheetDialog.dismiss();
            }
        });

        // Prikaz BottomSheet-a
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void showDeleteDialog() {
        // Kreiraj AlertDialog sa prilagođenim stilom i layout-om
        AlertDialog.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(getContext(), R.style.CustomDialog);
        }
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.delete_dialog_box, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Postavi zaobljenu pozadinu iz drawable resursa
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Postavi akcije za dugmad iz dijaloga
        Button buttonYes = dialogView.findViewById(R.id.button_yes);
        Button buttonNo = dialogView.findViewById(R.id.button_no);

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logika za brisanje usluge
                dialog.dismiss(); // Zatvori dijalog
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Zatvori dijalog bez dodatne akcije
                dialog.dismiss();
            }
        });

        dialog.show();
    }


}
