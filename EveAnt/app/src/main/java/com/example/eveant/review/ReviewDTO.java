package com.example.eveant.review;

public class ReviewDTO {

    private Integer offerId;
    private int rating;
    private String comment;
    private Integer reviewerId;

    // Prazan konstruktor
    public ReviewDTO() {
    }

    // Konstruktor sa svim poljima
    public ReviewDTO(Integer offerId, int rating, String comment, Integer reviewerId) {
        this.offerId = offerId;
        this.rating = rating;
        this.comment = comment;
        this.reviewerId = reviewerId;
    }

    // Getters and Setters
    public Integer getOfferId() {
        return offerId;
    }

    public void setOfferId(Integer offerId) {
        this.offerId = offerId;
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

    public Integer getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }
}
