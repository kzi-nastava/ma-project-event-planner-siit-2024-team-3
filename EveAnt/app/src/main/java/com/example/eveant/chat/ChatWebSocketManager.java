package com.example.eveant.chat;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.Observable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.LifecycleEvent;
import ua.naiksoftware.stomp.dto.StompHeader;

public class ChatWebSocketManager {

    private static ChatWebSocketManager instance;
    private StompClient stompClient;
    private final String WS_URL = "ws://192.168.1.10:8080/ws/websocket";
    private String currentUsername;
    private boolean isConnected = false;
    private String jwtToken;

    public MutableLiveData<ChatMessage> incomingMessage = new MutableLiveData<>();
    private Disposable heartbeatDisposable;

    private ChatWebSocketManager() {}

    public static synchronized ChatWebSocketManager getInstance() {
        if (instance == null) {
            instance = new ChatWebSocketManager();
        }
        return instance;
    }

    @SuppressLint("CheckResult")
    public void connect(String username, String token) {
        this.currentUsername = username;
        this.jwtToken = token;

        if (stompClient != null && stompClient.isConnected()) {
            Log.d("WS", "Already connected as " + currentUsername);
            return;
        }

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);

        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    isConnected = true;
                    Log.d("WS", " WebSocket OPENED as " + currentUsername);
                    break;

                case ERROR:
                    isConnected = false;
                    Log.e("WS", "WebSocket error", lifecycleEvent.getException());
                    break;

                case CLOSED:
                    isConnected = false;
                    Log.d("WS", "WebSocket closed — reconnecting...");
                    reconnect();
                    break;
            }
        });

        stompClient.connect(new ArrayList<StompHeader>() {{
            add(new StompHeader("Authorization", "Bearer " + jwtToken));
        }});

        stompClient.topic("/user/" + currentUsername + "/queue/messages").subscribe(event -> {
            Log.d("WS", "📨 Message event: " + event.getPayload());
            try {
                JSONObject json = new JSONObject(event.getPayload());
                ChatMessage message = new ChatMessage(
                        json.getString("senderUsername"),
                        json.getString("recipientUsername"),
                        json.getString("content"),
                        null
                );
                incomingMessage.postValue(message);
                Log.d("WS", "Message received realtime: " + message.getContent());
            } catch (Exception e) {
                Log.e("WS", "Failed to parse message", e);
            }
        }, throwable -> Log.e("WS", "Subscription error", throwable));
    }


    @SuppressLint("CheckResult")
    private void subscribeToMessages() {
        if (stompClient == null || !stompClient.isConnected()) return;

        String topic = "/user/" + currentUsername + "/queue/messages";
        Log.d("WS", "📡 Subscribing to: " + topic);

        stompClient.topic(topic).subscribe(event -> {
            try {
                JSONObject json = new JSONObject(event.getPayload());
                ChatMessage message = new ChatMessage();
                message.setSenderUsername(json.getString("senderUsername"));
                message.setRecipientUsername(json.getString("recipientUsername"));
                message.setContent(json.getString("content"));

                incomingMessage.postValue(message);
                Log.d("WS", " Message received: " + message.getContent());
            } catch (Exception e) {
                Log.e("WS", "Failed to parse incoming message", e);
            }
        }, throwable -> Log.e("WS", "Subscription error on " + topic, throwable));
    }



    @SuppressLint("CheckResult")
    public void sendMessage(ChatMessage message) {
        if (!isConnected) {
            Log.e("WS", "Cannot send: not connected!");
            reconnect();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("senderUsername", message.getSenderUsername());
            json.put("recipientUsername", message.getRecipientUsername());
            json.put("content", message.getContent());

            stompClient.send("/app/chat", json.toString())
                    .subscribe(
                            () -> Log.d("WS", "Message sent: " + message.getContent()),
                            throwable -> Log.e("WS", "Failed to send", throwable)
                    );
        } catch (Exception e) {
            Log.e("WS", "JSON build error", e);
        }
    }

    private void reconnect() {
        if (currentUsername != null && jwtToken != null) {
            Log.d("WS", "Reconnecting...");
            connect(currentUsername, jwtToken);
        }
    }

    private void startHeartbeat() {
        if (heartbeatDisposable != null && !heartbeatDisposable.isDisposed()) {
            heartbeatDisposable.dispose();
        }
        heartbeatDisposable = Observable.interval(25, TimeUnit.SECONDS)
                .subscribe(tick -> {
                    if (stompClient != null && stompClient.isConnected()) {
                        stompClient.send("/app/ping", "{}").subscribe();
                    }
                });
    }

    public boolean isConnected() {
        return isConnected && stompClient != null && stompClient.isConnected();
    }

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
            isConnected = false;
        }
        if (heartbeatDisposable != null && !heartbeatDisposable.isDisposed()) {
            heartbeatDisposable.dispose();
        }
    }
}
