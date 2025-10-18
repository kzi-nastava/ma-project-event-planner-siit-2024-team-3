package com.example.eveant.chat;

public class ChatUser {
    private String username;
    private String profilePhotoUrl;

    public ChatUser(String username) {
        this.username = username;
    }

    public ChatUser(String username, String profilePhotoUrl) {
        this.username = username;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }
}
