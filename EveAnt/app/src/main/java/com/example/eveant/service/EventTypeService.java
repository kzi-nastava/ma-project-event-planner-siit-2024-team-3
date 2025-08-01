package com.example.eveant.service;


import com.example.eveant.service.model.EventType;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface EventTypeService {
    @GET("/api/event-types")
    Call<List<EventType>> getEventTypes();

    @GET("/api/event-types/{id}")
    Call<EventType> getEventType(@Path("id") int id);

    @GET("/api/event-types/name/{name}")
    Call<EventType> getEventTypeByName(@Path("name") String name);



}
