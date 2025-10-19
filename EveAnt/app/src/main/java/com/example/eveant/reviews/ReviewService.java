package com.example.eveant.reviews;


import com.example.eveant.event.Event;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReviewService {
    @GET("/api/reviews/pending")
    Call<List<Event>> getEventsToReview(@Query("username") String username);

    // GET /api/reviews/event/{id}
    @GET("/api/reviews/event/{id}")
    Call<List<Review>> getEventReviews(@Path("id") int eventId);

    // GET /api/reviews/event/{eventId}/{username}
    @GET("/api/reviews/event/{eventId}/{username}")
    Call<Review> getUsersReviewForEvent(@Path("eventId") int eventId, @Path("username") String username);

    // POST /api/reviews
    @POST("/api/reviews")
    Call<CreateReview.Created> createReview(@Body CreateReview body);
}
