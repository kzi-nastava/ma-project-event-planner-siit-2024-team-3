package com.example.eveant.communication;

import com.example.eveant.user.model.User;

public class SendMessageRequest {

    private ChatSession chatSession;
    private Integer sender;
    private String content;

    public ChatSession getChatSession() {
        return chatSession;
    }

    public void setChatSession(ChatSession chatSession) {
        this.chatSession = chatSession;
    }

    public Integer getSender() {
        return sender;
    }

    public void setSender(Integer sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
