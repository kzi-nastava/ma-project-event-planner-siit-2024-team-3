package com.example.eveant.communication;

import com.example.eveant.user.model.User;

public class StartChatRequest {
    private User user;
    private User organiser;

    public void setOrganiser(User organiser) {
        this.organiser = organiser;
    }

    public User getOrganiser() {
        return organiser;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}