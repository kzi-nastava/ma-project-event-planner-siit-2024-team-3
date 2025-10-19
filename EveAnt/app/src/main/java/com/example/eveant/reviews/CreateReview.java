package com.example.eveant.reviews;

public class CreateReview{
    // server expects exactly these names:
    // reviewer (String username), rating (int), eventId (Integer?), offerId (Integer?)
    public String reviewer;
    public Integer eventId; // one of eventId or offerId must be set
    public Integer offerId; // keep null when reviewing event
    public int rating;
    public String comment;  // optional

    public CreateReview(String reviewer, Integer eventId, Integer offerId, int rating, String comment) {
        this.reviewer = reviewer;
        this.eventId  = eventId;
        this.offerId  = offerId;
        this.rating   = rating;
        this.comment  = comment;
    }

    // Response DTO (CreatedReviewDTO)
    public static class Created {
        public int id;
        public int rating;
        public String comment;
        public String timestamp;
    }
}
