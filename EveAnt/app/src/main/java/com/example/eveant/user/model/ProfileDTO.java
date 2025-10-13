package com.example.eveant.user.model;

public class ProfileDTO {
    private String username;
    private String email;
    private String password;

    private boolean isActivated;
    private String profilePhoto;

    // Default constructor
    public ProfileDTO() {}

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActivated() { return isActivated; }
    public void setActivated(boolean activated) { isActivated = activated; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public void setPassword(String password){
        this.password = password;
    }
}



