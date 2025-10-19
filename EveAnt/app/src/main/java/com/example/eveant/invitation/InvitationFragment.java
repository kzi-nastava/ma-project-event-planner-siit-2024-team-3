package com.example.eveant.invitation;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.event.EventCreationViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvitationFragment extends Fragment {

    private EditText emailInput, messageInput;
    private Button sendButton;
    private RecyclerView guestRecycler;
    private InvitationAdapter adapter;

    private InvitationService invitationService;

    private EventCreationViewModel vm;
    private int eventId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invitation, container, false);

        emailInput = view.findViewById(R.id.emailInput);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        guestRecycler = view.findViewById(R.id.guestRecyclerView);

        guestRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new InvitationAdapter(invitationService);
        guestRecycler.setAdapter(adapter);

        // Always set the click listener; guard inside sendInvitation()
        sendButton.setOnClickListener(v -> sendInvitation());

        // Hook the shared ViewModel from the Activity
        vm = new ViewModelProvider(requireActivity()).get(EventCreationViewModel.class);
        vm.getEventId().observe(getViewLifecycleOwner(), id -> {
            if (id == null || id <= 0) {
                eventId = -1;
                sendButton.setEnabled(false);
                return;
            }
            // If eventId changes (e.g., after create), refresh UI
            boolean firstSetOrChanged = (eventId != id);
            eventId = id;
            sendButton.setEnabled(true);
            if (firstSetOrChanged) {
                loadInvitations();
            }
        });

        // If VM value was already set before observer attached, observer above fires immediately.
        // Otherwise, button remains disabled until create flow sets the id.

        return view;
    }

    private void loadInvitations() {
        if (eventId <= 0) return;
        invitationService.getInvitations(eventId).enqueue(new Callback<List<Invitation>>() {
            @Override
            public void onResponse(Call<List<Invitation>> call, Response<List<Invitation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setInvitations(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Invitation>> call, Throwable t) {
                Log.e("InvitationFragment", "Error loading invitations", t);
            }
        });
    }

    private void sendInvitation() {
        if (eventId <= 0) {
            Toast.makeText(getContext(), "Event ID is not available yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = emailInput.getText().toString();
        String message = messageInput.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(message)) {
            Toast.makeText(getContext(), "Email and message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        InvitationRequest request = new InvitationRequest(email, message, eventId);
        invitationService.sendInvitation(eventId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Invitation sent!", Toast.LENGTH_SHORT).show();
                    loadInvitations();
                    emailInput.setText("");
                    messageInput.setText("");
                } else {
                    Toast.makeText(getContext(), "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to send invitation", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
