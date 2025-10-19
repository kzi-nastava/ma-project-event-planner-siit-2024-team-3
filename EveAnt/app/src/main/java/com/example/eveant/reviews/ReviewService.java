package com.example.eveant.reviews;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ReviewService {
    @GET("/api/events/{eventId}/reviews")
    Call<List<Review>> getReviews(@Path("eventId") int eventId);
}
