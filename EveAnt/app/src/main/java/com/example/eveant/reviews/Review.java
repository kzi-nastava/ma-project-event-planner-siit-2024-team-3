package com.example.eveant.reviews;

public class Review {
    private int rating;     // 1..5
    private String comment; // optional
    private String createdAt;
    public int getRating() { return rating; }
    public String getCreatedAt() { return createdAt; }
}
