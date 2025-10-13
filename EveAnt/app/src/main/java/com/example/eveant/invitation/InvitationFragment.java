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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.notification.Notification;
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

    private int eventId = 2; // TODO: Replace with actual event ID

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invitation, container, false);

        emailInput = view.findViewById(R.id.emailInput);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        guestRecycler = view.findViewById(R.id.guestRecyclerView);

        guestRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        invitationService = RetrofitClient.invitationService;
        adapter = new InvitationAdapter(invitationService);
        guestRecycler.setAdapter(adapter);

        loadInvitations();

        sendButton.setOnClickListener(v -> sendInvitation());

        return view;
    }

    private void loadInvitations() {
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
                Toast.makeText(getContext(), "Invitation sent!", Toast.LENGTH_SHORT).show();
                loadInvitations();
                emailInput.setText("");
                messageInput.setText("");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to send invitation", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
