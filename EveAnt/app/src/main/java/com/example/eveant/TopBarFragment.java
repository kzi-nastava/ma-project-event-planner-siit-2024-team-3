package com.example.eveant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eveant.R;
import com.example.eveant.notification.NotificationFragment;

public class TopBarFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_top_bar, container, false);

        ImageButton backButton = v.findViewById(R.id.back_button);
        ImageButton notifButton = v.findViewById(R.id.notification_button);

        backButton.setOnClickListener(view -> requireActivity().onBackPressed());

        notifButton.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), NotificationFragment.class);
            startActivity(intent);
        });

        return v;
    }
}
