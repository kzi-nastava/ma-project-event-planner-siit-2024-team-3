package com.example.eveant.admin;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.eveant.MainActivity;
import com.example.eveant.R;

public class CategoryManagement extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_category_management, container, false);




       // Edit dugme - otvara novu aktivnost
        ImageButton editButtoncategory = view.findViewById(R.id.editButtoncategory);
        editButtoncategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {showCategoryDialog();
            }
        });

        // Add dugme - otvara novu aktivnost
        ImageButton addCategoryButton = view.findViewById(R.id.addCategoryButton);
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {showCategoryDialog();
            }

        });

        // Delete dugme - prikazuje dijalog
        ImageButton deleteButtonCategory = view.findViewById(R.id.deleteButtonCategory);
        deleteButtonCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
        return view;
    }

    private void showDeleteDialog() {
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

    private void showCategoryDialog() {
        // Kreiraj AlertDialog sa prilagođenim stilom i layout-om
        AlertDialog.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(getContext(), R.style.CustomDialog);
        }
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.category_dialog_box, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Postavi zaobljenu pozadinu iz drawable resursa
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button buttonSave = dialogView.findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logika za cuvanje kategorije
                // ako je bila da se doda nova dodati je , ako je bilo da se edituje editovati je
                dialog.dismiss();
            }
        });



        dialog.show();
    }
}