package com.example.eveant.review;

import com.example.eveant.service.model.Offer;
import com.example.eveant.user.model.User;

import java.time.LocalDateTime;

public class Review {
    private Offer offer;
    private int rating;
    private String comment;
    private User reviewer;

    // Default constructor
    public Review() {
    }

    // Parameterized constructor
    public Review(Offer offer, int rating, String comment, User reviewer) {
        this.offer = offer;
        this.rating = rating;
        this.comment = comment;
        this.reviewer = reviewer;
    }

    // Getters and Setters
    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

}
