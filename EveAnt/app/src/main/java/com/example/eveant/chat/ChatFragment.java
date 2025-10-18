package com.example.eveant.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.user.model.User;
import com.example.eveant.user.security.AuthManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private RecyclerView messagesRecycler;
    private EditText inputMessage;
    private ImageButton btnSend;
    private ChatAdapter adapter;
    private final List<ChatMessage> messages = new ArrayList<>();

    private String recipientUsername;
    private String currentUser;
    private String jwtToken;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recipientUsername = getArguments().getString("username");
        jwtToken = AuthManager.getInstance(requireContext()).getToken();

        messagesRecycler = view.findViewById(R.id.messages_recycler);
        inputMessage = view.findViewById(R.id.input_message);
        btnSend = view.findViewById(R.id.btn_send);

        messagesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // 🔁 1️⃣ Prvo dohvati username iz backend-a
        String email = AuthManager.getInstance(requireContext()).getEmail();
        RetrofitClient.userService.getUserByEmail(email).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body().getProfile().getUsername();
                    Log.d("CHAT", "Current user: " + currentUser);

                    // 2️⃣ Kreiraj adapter TEK sada
                    adapter = new ChatAdapter(messages, currentUser);
                    messagesRecycler.setAdapter(adapter);

                    // 3️⃣ Poveži websocket (sa JWT)
                    ChatWebSocketManager.getInstance().connect(currentUser, jwtToken);

                    // 4️⃣ Pretplati se na real-time poruke
                    observeIncomingMessages();

                    // 5️⃣ Učitaj istoriju poruka
                    loadMessages();
                } else {
                    Log.e("CHAT", "Failed to get username from email.");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("CHAT", "Error getting username", t);
            }
        });

        btnSend.setOnClickListener(v -> sendMessage());
        return view;
    }

    private void observeIncomingMessages() {
        ChatWebSocketManager.getInstance().incomingMessage.observe(getViewLifecycleOwner(), new Observer<ChatMessage>() {
            @Override
            public void onChanged(ChatMessage message) {
                if (message == null) return;

                boolean isForThisChat =
                        (message.getSenderUsername().equals(recipientUsername) && message.getRecipientUsername().equals(currentUser)) ||
                                (message.getSenderUsername().equals(currentUser) && message.getRecipientUsername().equals(recipientUsername));

                if (isForThisChat) {
                    messages.add(message);
                    adapter.notifyItemInserted(messages.size() - 1);
                    messagesRecycler.scrollToPosition(messages.size() - 1);
                }
            }
        });
    }

    private void loadMessages() {
        if (currentUser == null || recipientUsername == null) {
            Log.e("Chat", "Missing usernames for loading messages");
            return;
        }

        String chatId = currentUser + "_" + recipientUsername;
        RetrofitClient.chatApiService.getMessagesByChatId(chatId).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messages.clear();
                    messages.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    messagesRecycler.scrollToPosition(messages.size() - 1);
                    Log.d("Chat", "Loaded " + messages.size() + " messages");
                } else {
                    Log.e("Chat", "No messages received or failed");
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Log.e("Chat", "Failed to load messages", t);
            }
        });
    }

    private void sendMessage() {
        String text = inputMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        if (!ChatWebSocketManager.getInstance().isConnected()) {
            Log.e("WS", "Cannot send: WebSocket not connected!");
            return;
        }

        ChatMessage msg = new ChatMessage(currentUser, recipientUsername, text, new Date());
        ChatWebSocketManager.getInstance().sendMessage(msg);

        // Lokalno dodaj poruku odmah
        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        messagesRecycler.scrollToPosition(messages.size() - 1);
        inputMessage.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ChatWebSocketManager.getInstance().disconnect();
    }
}
