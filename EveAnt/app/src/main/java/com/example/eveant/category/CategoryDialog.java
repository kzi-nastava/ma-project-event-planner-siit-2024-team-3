package com.example.eveant.category;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eveant.R;

public class CategoryDialog extends DialogFragment {

    public interface OnCategorySavedListener {
        void onCategorySaved(String name, String description);
    }

    private final OnCategorySavedListener listener;

    public CategoryDialog(OnCategorySavedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.category_dialog_box, null);

        EditText inputName = view.findViewById(R.id.input_name);
        EditText inputDesc = view.findViewById(R.id.input_description);
        Button saveButton = view.findViewById(R.id.button_save);

        saveButton.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();

            if (name.isEmpty()) {
                inputName.setError("Name is required");
                return;
            }
            listener.onCategorySaved(name, desc);
            dismiss();
        });

        builder.setView(view)
                .setTitle("Create Category")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}
