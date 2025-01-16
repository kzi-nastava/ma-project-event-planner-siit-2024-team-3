package com.example.eveant.review;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ReviewService {
    @POST("/api/reviews")
    Call<Void> createReview(@Body ReviewDTO review);
}
