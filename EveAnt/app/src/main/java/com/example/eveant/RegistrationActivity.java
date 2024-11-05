package com.example.eveant;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ToggleButton;
import android.widget.CompoundButton;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        ToggleButton organizerButton = findViewById(R.id.organizerButton);
        ToggleButton providerButton = findViewById(R.id.providerButton);

        organizerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    providerButton.setChecked(false);
                    organizerButton.setTextColor(getColor(R.color.white));
                    providerButton.setTextColor(getColor(R.color.black));
                }
            }
        });

        providerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    organizerButton.setChecked(false);
                    organizerButton.setTextColor(getColor(R.color.black));
                    providerButton.setTextColor(getColor(R.color.white));
                }
            }
        });
    }
}