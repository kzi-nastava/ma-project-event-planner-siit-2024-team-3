package com.example.eveant.websocket;

import android.util.Log;
import org.json.JSONObject;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public abstract class NotificationWebSocketListener extends WebSocketListener {
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.d("WebSocket", "Connected to server");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d("WebSocket", "Message received: " + text);

        try {
            JSONObject message = new JSONObject(text);
            String content = message.getString("content");
            String type = message.getString("type");

            if ("NOTIFICATION".equals(type)) {
                showNotification(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Log.d("WebSocket", "Byte message received: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        Log.d("WebSocket", "Connection closing: " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e("WebSocket", "Error: " + t.getMessage());
    }



    public abstract void showNotification(String message);
}
