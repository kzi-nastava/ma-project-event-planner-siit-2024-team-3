package com.example.eveant.eventType;

import com.example.eveant.service.model.Category;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface EventTypeService {

    @GET("event-types")
    Call<List<EventType>> getAll();

    @GET("event-types/activated")
    Call<List<EventType>> getAllActivated();

    @GET("event-types/{id}")
    Call<EventType> getById(@Path("id") int id);

    @POST("event-types")
    Call<EventType> create(@Body EventType dto);

    @PUT("event-types/{id}")
    Call<EventType> update(@Path("id") int id, @Body EventType dto);

    @GET("event-types/{id}/categories")
    Call<List<EventType>> getCategories(@Path("id") int id);

    // Opcione rute po tvom servisu:
    @POST("event-types/all-with-categories")
    Call<EventType> createAllEventTypeWithAllCategories();

    @POST("event-types/resolve")
    Call<EventType> resolve(@Body EventType dto);

    @GET("event-types/by-name/{name}")
    Call<EventType> findByName(@Path("name") String name);

    @PATCH("event-types/{id}")
    Call<EventType> patch(@Path("id") int id,
                          @Body com.example.eveant.eventType.EventTypeFragment.EventTypePatch dto);

    @GET("/api/event-types/{id}/categories")
    Call<List<Category>> getEventTypeCategories(@Path("id") int eventTypeId);
}
