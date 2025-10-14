package com.example.eveant.event.agenda;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;


public interface ActivityService {

    @GET("activities")
    Call<List<Activity>> getAll();

    @GET("activities/event/{eventId}")
    Call<List<Activity>> getByEventId(@Path("eventId") int eventId);

    @GET("activities/{id}")
    Call<Activity> getById(@Path("id") int id);

    @POST("activities")
    Call<ActivityDTO> create(@Body ActivityDTO dto);

    @PUT("activities/{id}")
    Call<Activity> update(@Path("id") int id, @Body Activity dto);

    @DELETE("activities/{id}")
    Call<Void> delete(@Path("id") int id);
}

