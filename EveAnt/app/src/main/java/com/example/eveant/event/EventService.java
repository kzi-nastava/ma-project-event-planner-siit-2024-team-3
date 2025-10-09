package com.example.eveant.event;

import com.example.eveant.event.createEvent.BasicInformationFragment;

import java.util.Collection;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface EventService {

    // === READ ===
    @GET("/api/events/{id}")
    Call<Event> getEventById(@Path("id") int id);

    @GET("/api/events")
    Call<List<Event>> getAllEvents();

    @GET("/api/events/organizer/{username}")
    Call<List<Event>> getAllByOrganizer(@Path("username") String username);

    @GET("/api/events/attended/{username}")
    Call<List<Event>> getAttended(@Path("username") String username);

    @GET("/api/events/invited/{token}")
    Call<Event> getInvitedEvent(@Path("token") String token);

    // /api/events/search?search=&eventType=&status=&startDate=&endDate=&city=&userEmail=&sortBy=&order=
    @GET("/api/events/search")
    Call<List<Event>> searchEvents(
            @Query("search") String search,
            @Query("eventType") List<String> eventType, // e.g. ["Conference","Workshop"]
            @Query("status") String status,
            @Query("startDate") String startDateIso,   // yyyy-MM-dd
            @Query("endDate") String endDateIso,       // yyyy-MM-dd
            @Query("city") String city,
            @Query("userEmail") String userEmail,
            @Query("sortBy") String sortBy,
            @Query("order") String order
    );

    @GET("/api/events/distinct-cities")
    Call<List<String>> getDistinctCities();

    @GET("/api/events/top5")
    Call<Collection<Event>> getTop5();


    // === CRUD EVENT ===
    @POST("/api/events")
    Call<Event> createEvent(@Body BasicInformationFragment.CreateEventRequest body);

    @PUT("/api/events/{id}")
    Call<Event> updateEvent(@Path("id") int id, @Body Event body);

    @DELETE("/api/events/{id}")
    Call<ResponseBody> deleteEvent(@Path("id") int id);
}
