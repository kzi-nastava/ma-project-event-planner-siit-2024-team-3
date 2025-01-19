package com.example.eveant.communication;

import com.example.eveant.user.model.User;

import java.time.LocalDateTime;

public class Message {

    private Integer id;
    private ChatSession chatSession;
    private User sender;
    private String content;
    private LocalDateTime timestamp;
    private Boolean isRead;

    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }
    public ChatSession getChatSession() {
        return chatSession;
    }

    public void setChatSession(ChatSession chatSession) {
        this.chatSession = chatSession;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
