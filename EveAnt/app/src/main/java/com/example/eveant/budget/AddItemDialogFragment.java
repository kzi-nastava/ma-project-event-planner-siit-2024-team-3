package com.example.eveant.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eveant.R;

public class AddItemDialogFragment extends DialogFragment {
    private EditText nameInput, priceInput;
    private Button confirmButton;

    public interface OnItemAddedListener {
        void onItemAdded(String name, double price);
    }

    private OnItemAddedListener listener;

    public AddItemDialogFragment(OnItemAddedListener listener) {
        this.listener = listener;
    }

    private String initialName;
    private double initialPrice;

    public AddItemDialogFragment(String name, double price, OnItemAddedListener listener) {
        this.initialName = name;
        this.initialPrice = price;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_item, container, false);

        nameInput = view.findViewById(R.id.item_name_input);
        priceInput = view.findViewById(R.id.item_price_input);
        confirmButton = view.findViewById(R.id.confirm_item_button);

        if (initialName != null) nameInput.setText(initialName);
        if (initialPrice != 0) priceInput.setText(String.valueOf(initialPrice));

        confirmButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            double price = Double.parseDouble(priceInput.getText().toString());

            listener.onItemAdded(name, price);
            dismiss();
        });

        return view;
    }
}
