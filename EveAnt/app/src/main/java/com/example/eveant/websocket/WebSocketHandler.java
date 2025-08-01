package com.example.eveant.websocket;

import com.example.eveant.BuildConfig;

import okhttp3.*;

public class WebSocketHandler {
    private static final String BASE_URL = "ws://"+BuildConfig.IP_ADDR+"/ws";
    private WebSocket webSocket;
    private OkHttpClient client;

    public WebSocketHandler() {
        client = new OkHttpClient();
    }

    public void connect(WebSocketListener listener) {
        Request request = new Request.Builder().url(BASE_URL).build();
        webSocket = client.newWebSocket(request, listener);
    }

    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    public void closeConnection() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing connection");
        }
    }
}
