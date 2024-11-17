package com.example.eveant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ServicesViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_services_view);

        // Edit dugme - otvara novu aktivnost
        ImageButton editServiceButton = findViewById(R.id.editServiceButton);
        editServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServicesViewActivity.this, ProductCreateActivity.class);
                startActivity(intent);
            }
        });

        // Delete dugme - prikazuje dijalog
        ImageButton deleteServiceButton = findViewById(R.id.deleteServiceButton);
        deleteServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
    }

    private void showDeleteDialog() {
        // Kreiraj AlertDialog sa prilagođenim stilom i layout-om
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.delete_dialog_box, null);
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
