package com.example.eveant.communication;

import static com.example.eveant.RetrofitClient.chatService;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eveant.RetrofitClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatViewModel extends ViewModel {

    private final MutableLiveData<List<ChatMessageDTO>> messages = new MutableLiveData<>(new ArrayList<>());
    private ChatWebSocketClient webSocketClient;

    public LiveData<List<ChatMessageDTO>> getMessages() {
        return messages;
    }

    public void connect(String username) {
        Log.d("ChatViewModel", "Povezivanje korisnika na WebSocket: " + username);
        webSocketClient = new ChatWebSocketClient();
        webSocketClient.connect(username, msg -> {
            Log.d("ChatViewModel", "Primljena poruka od: " + msg.getSenderUsername() + " - " + msg.getContent());
            List<ChatMessageDTO> current = messages.getValue();
            if (current == null) current = new ArrayList<>();
            current.add(msg);
            messages.postValue(current);
        });
    }


    public void sendMessage(ChatMessageDTO msg) {
        if (webSocketClient != null) {
            Log.d("ChatViewModel", "Slanje poruke: " + msg.getContent() +
                    " od " + msg.getSenderUsername() +
                    " ka " + msg.getRecipientUsername());
            webSocketClient.sendMessage(msg);
            List<ChatMessageDTO> current = messages.getValue();
            current.add(msg);
            messages.postValue(current);
        }
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        if (webSocketClient != null) webSocketClient.disconnect();
    }
    // ChatViewModel.java
    public void loadMessages(String sender, String recipient) {
        Log.d("ChatViewModel", "Pozvano loadMessages: sender=" + sender + ", recipient=" + recipient);
        chatService.getChatId(sender, recipient).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String chatId = response.body().string();
                        fetchMessages(chatId);
                        Log.d("ChatViewModel", "Dobijen chatId: " + chatId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<
                    ResponseBody> call, Throwable t) {
                Log.e("ChatViewModel", "Greška pri dobijanju chatId", t);
            }
        });

    }


    private void fetchMessages(String chatId) {

        chatService.getMessages(chatId).enqueue(new Callback<List<ChatMessageDTO>>() {
            @Override
            public void onResponse(Call<List<ChatMessageDTO>> call, Response<List<ChatMessageDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatMessageDTO> fetchedMessages = response.body();
                    messages.postValue(fetchedMessages);  // update LiveData
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessageDTO>> call, Throwable t) {
                Log.e("ChatViewModel", "Greška pri dobijanju poruka", t);
            }
        });
    }


} 
