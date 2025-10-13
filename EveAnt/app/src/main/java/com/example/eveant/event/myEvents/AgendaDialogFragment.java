package com.example.eveant.event.myEvents;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eveant.R;

public class AgendaDialogFragment extends androidx.fragment.app.DialogFragment {

    private static final String ARG_EVENT_ID = "event_id";

    public static AgendaDialogFragment newInstance(int eventId) {
        Bundle b = new Bundle();
        b.putInt(ARG_EVENT_ID, eventId);
        AgendaDialogFragment f = new AgendaDialogFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Optional: your custom dialog style (rounded corners etc.)
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the host layout that contains agendaContainer
        return inflater.inflate(R.layout.dialog_agenda_host, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int eventId = (getArguments() != null) ? getArguments().getInt(ARG_EVENT_ID, -1) : -1;

        // Attach your existing AgendaFragment safely here
        Fragment child = com.example.eveant.event.agenda.AgendaFragment.newInstance(eventId);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.agendaContainer, child, "agenda_child")
                .commitNow(); // safe now because the view exists
    }

    @Override
    public void onStart() {
        super.onStart();
        // Optional: set dialog size & transparent background for rounded corners
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            // Set a comfortable size
            getDialog().getWindow().setLayout(
                    (int)(requireContext().getResources().getDisplayMetrics().widthPixels * 0.95),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
