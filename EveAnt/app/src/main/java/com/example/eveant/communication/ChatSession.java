package com.example.eveant.communication;

import com.google.gson.annotations.SerializedName;

public class ChatSession {

    private Integer id;

    private Integer user;
    private Integer organiser;

    private String createdAt;

    private String updatedAt;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user= user;
    }

    public Integer getOrganiser() {
        return organiser;
    }

    public void setOrganiser(Integer organiser) {
        this.organiser = organiser;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
