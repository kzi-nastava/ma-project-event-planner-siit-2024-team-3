package com.example.eveant.chat;


import java.util.Date;

public class ChatMessage {
    private String senderUsername;
    private String recipientUsername;
    private String content;
    private Date timestamp;

    public ChatMessage(String senderUsername, String recipientUsername, String content, Date timestamp) {
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public String getContent() {
        return content;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}

