package com.example.eveant.event;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface EventService {

    // === READ ===
    @GET("/api/events/{id}")
    Call<GetEventDTO> getEventById(@Path("id") int id);

    @GET("/api/events")
    Call<List<GetEventDTO>> getAllEvents();

    @GET("/api/events/organizer/{username}")
    Call<List<GetEventDTO>> getAllByOrganizer(@Path("username") String username);

    @GET("/api/events/attended/{username}")
    Call<List<GetEventDTO>> getAttended(@Path("username") String username);

    @GET("/api/events/invited/{token}")
    Call<GetEventDTO> getInvitedEvent(@Path("token") String token);

    // /api/events/search?search=&eventType=&status=&startDate=&endDate=&city=&userEmail=&sortBy=&order=
    @GET("/api/events/search")
    Call<List<GetEventDTO>> searchEvents(
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
    Call<Collection<GetEventDTO>> getTop5();

    // === INVITATIONS ===
    @GET("/api/events/{eventId}/invitations")
    Call<List<GetInvitationDTO>> getInvitations(@Path("eventId") int eventId);

    @POST("/api/events/{eventId}/invitations")
    Call<CreatedInvitationDTO> createInvitation(@Path("eventId") int eventId, @Body CreateInvitationDTO body);

    @DELETE("/api/events/{eventId}/invitations/{email}")
    Call<ResponseBody> declineInvitation(@Path("eventId") int eventId, @Path("email") String email);

    // Accept invitation by token (redirect backend) – we just trigger it:
    @GET("/api/events/invitation")
    Call<ResponseBody> acceptInvitation(@Query("token") String token);

    // === CRUD EVENT ===
    @POST("/api/events")
    Call<CreatedEventDTO> createEvent(@Body CreateEventDTO body);

    @PUT("/api/events/{id}")
    Call<UpdatedEventDTO> updateEvent(@Path("id") int id, @Body UpdatedEventDTO body);

    @DELETE("/api/events/{id}")
    Call<ResponseBody> deleteEvent(@Path("id") int id);

    // === EventType via Event ===
    @GET("/api/events/event-type/by-event/{eventId}")
    Call<EventTypeDTO> getEventTypeByEvent(@Path("eventId") int eventId);

}
