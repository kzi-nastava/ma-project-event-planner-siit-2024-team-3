package com.example.eveant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ServicesViewActivity extends AppCompatActivity {

    private RelativeLayout filterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_services_view);

        // Povezivanje dugmeta za filtere
        filterButton = findViewById(R.id.filter_button);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterBottomSheet();
            }
        });
    }

    private void showFilterBottomSheet() {
        // Kreiramo BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ServicesViewActivity.this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.filter_bottom_sheet, findViewById(android.R.id.content), false);

        // Povezivanje elemenata
        Spinner categorySpinner = bottomSheetView.findViewById(R.id.category_spinner);
        SeekBar priceSeekBar = bottomSheetView.findViewById(R.id.price_seekbar);
        CheckBox availabilityCheckbox = bottomSheetView.findViewById(R.id.availability_checkbox);
        Button applyFiltersButton = bottomSheetView.findViewById(R.id.apply_filters_button);

        // Postavljanje dugmeta za primenu filtera
        applyFiltersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPrice = priceSeekBar.getProgress();
                boolean isAvailableOnly = availabilityCheckbox.isChecked();

                // Prikazivanje rezultata filtriranja (primer)
                Toast.makeText(ServicesViewActivity.this,
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
}
