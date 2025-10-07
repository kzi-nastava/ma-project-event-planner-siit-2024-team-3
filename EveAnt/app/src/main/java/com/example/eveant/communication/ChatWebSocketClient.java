package com.example.eveant.communication;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class ChatWebSocketClient {

    private StompClient stompClient;
    private static final String WS_URL = "ws://10.0.2.2:8080/ws"; // emulator
    private static final String TOKEN = "Bearer " + "TVOJ_JWT_TOKEN";

    public void connect(String username, Consumer<ChatMessageDTO> onMessageReceived) {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);


        // Dodavanje header-a
        List<StompHeader> headers = Collections.singletonList(new StompHeader("Authorization", TOKEN));
        stompClient.connect(headers);

        stompClient.topic("/user/" + username + "/queue/messages").subscribe(topicMessage -> {
            ChatMessageDTO msg = new Gson().fromJson(topicMessage.getPayload(), ChatMessageDTO.class);
            onMessageReceived.accept(msg);
        });
    }

    public void sendMessage(ChatMessageDTO message) {
        String json = new Gson().toJson(message);
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.send("/app/chat", json).subscribe();
        }

    }

    public void disconnect() {
        if (stompClient != null) stompClient.disconnect();
    }
}
