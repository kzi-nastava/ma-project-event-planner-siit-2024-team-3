package com.example.eveant.reviews;

public class Review {
    private int rating;     // 1..5
    private String comment; // optional
    private String createdAt;
    private String author;

    public Review(String who, String txt, String when, int what) {
        this.author = who;
        this.comment = txt;
        this.createdAt = when;
        this.rating = what;
    }

    public String getAuthor() {
        return author;
    }

    public String getComment() {
        return comment;
    }

    public int getRating() { return rating; }
    public String getCreatedAt() { return createdAt; }
}
