package com.example.eveant.comment;

import com.example.eveant.event.Event;
import com.example.eveant.user.model.Profile;
import com.google.gson.annotations.SerializedName;

public class Comment {
    @SerializedName("id")
    private Integer id;

    @SerializedName("event")
    private Event event;

    @SerializedName("profile")
    private Profile profile;

    @SerializedName("content")
    private String content;

    @SerializedName("status")
    private CommentStatus status;

    @SerializedName("createdAt")
    private String createdAt;

    // Constructors
    public Comment() {}

    public Comment(String content, Integer eventId, String userEmail) {
        this.content = content;
        // You might need to set event and profile differently based on your API
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public CommentStatus getStatus() { return status; }
    public void setStatus(CommentStatus status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}