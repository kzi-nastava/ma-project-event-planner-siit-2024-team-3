package com.example.eveant.communication;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.user.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private ChatAdapter chatAdapter;
    private EditText editTextMessage;
    private ImageButton buttonSend;

    private ChatSession chatSession;
    private List<Message> messages = new ArrayList<>();

    private User currentUser;
    private User organiser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        int userId = getIntent().getIntExtra("userId", -1);
        int providerId = getIntent().getIntExtra("providerId", -1);

        Log.d(TAG, "onCreate: "+userId);
        Log.d(TAG, "onCreate: "+providerId);

        if (userId != -1 && providerId != -1) {
            Log.d(TAG, "User ID: " + userId + ", Provider ID: " + providerId);
        } else {
            Toast.makeText(this, "Invalid user or provider data", Toast.LENGTH_SHORT).show();
            finish();
        }

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        chatAdapter = new ChatAdapter(messages, currentUser);
        recyclerViewMessages.setAdapter(chatAdapter);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        startChat();

        buttonSend.setOnClickListener(v -> sendMessage());
    }



    private void startChat() {
        StartChatRequest request = new StartChatRequest();
        request.setOrganiser(organiser);
        request.setUser(currentUser);
        RetrofitClient.chatSessionService.startChat(request).enqueue(new Callback<ChatSession>() {
            @Override
            public void onResponse(Call<ChatSession> call, Response<ChatSession> response) {
                Log.d(TAG, "onResponse: " + response.body().getId());
                if (response.isSuccessful()) {
                    chatSession = response.body();
                    loadMessages();
                }
            }

            @Override
            public void onFailure(Call<ChatSession> call, Throwable t) {
                // Obrađivanje greške
            }
        });
    }

    private void loadMessages() {
        RetrofitClient.chatSessionService.getMessages(chatSession.getId()).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful()) {
                    messages.clear();
                    messages.addAll(response.body());
                    chatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                // Obrada greške
            }
        });
    }

    private void sendMessage() {
        String messageContent = editTextMessage.getText().toString();
        if (!messageContent.isEmpty()) {
            SendMessageRequest request = new SendMessageRequest();
            request.setChatSession(chatSession);
            request.setSender(1);  /*id treutnog usera*/
            request.setContent(messageContent);

            RetrofitClient.chatSessionService.sendMessage(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        loadMessages();
                        editTextMessage.setText("");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Obrađivanje greške
                }
            });
        }
    }
}
