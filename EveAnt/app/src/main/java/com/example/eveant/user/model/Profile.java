package com.example.eveant.user.model;

import java.util.List;

public class Profile {
    private Integer id;
    private String username;
    private String email;
    private boolean isActivated;
    private String profilePhoto;
    private String suspendedUntil; // Use String for simplicity; parse to Date if needed

    private Event[] favouriteEvents;
    private Offer[] favouriteOffers;
    private List<Profile> blockedProfiles;

    // Default constructor
    public Profile() {}

    public Integer getId() { return id;}
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActivated() { return isActivated; }
    public void setActivated(boolean activated) { isActivated = activated; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getSuspendedUntil() { return suspendedUntil; }
    public void setSuspendedUntil(String suspendedUntil) { this.suspendedUntil = suspendedUntil; }

    public Event[] getFavouriteEvents() { return favouriteEvents; }
    public void setFavouriteEvents(Event[] favouriteEvents) { this.favouriteEvents = favouriteEvents; }

    public Offer[] getFavouriteOffers() { return favouriteOffers; }
    public void setFavouriteOffers(Offer[] favouriteOffers) { this.favouriteOffers = favouriteOffers; }

    public List<Profile> getBlockedProfiles() { return blockedProfiles; }
    public void setBlockedProfiles(List<Profile> blockedProfiles) { this.blockedProfiles = blockedProfiles; }
}
