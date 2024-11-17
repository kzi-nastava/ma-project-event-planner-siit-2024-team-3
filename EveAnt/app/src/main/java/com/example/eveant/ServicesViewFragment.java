package com.example.eveant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ServicesViewFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_services_view, container, false);

        // Edit button - opens a new activity
        ImageButton editServiceButton = view.findViewById(R.id.editServiceButton);
        editServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ProductCreateActivity.class);
                startActivity(intent);
            }
        });

        // Delete button - shows a dialog
        ImageButton deleteServiceButton = view.findViewById(R.id.deleteServiceButton);
        deleteServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });

        return view;
    }

    private void showDeleteDialog() {
        // Create AlertDialog with custom style and layout
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomDialog);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.delete_dialog_box, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Set rounded background from drawable resource
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Set actions for dialog buttons
        Button buttonYes = dialogView.findViewById(R.id.button_yes);
        Button buttonNo = dialogView.findViewById(R.id.button_no);

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logic for deleting the service
                dialog.dismiss(); // Close the dialog
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog without any additional actions
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
