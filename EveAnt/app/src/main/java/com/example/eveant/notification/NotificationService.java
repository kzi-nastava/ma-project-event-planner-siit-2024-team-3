package com.example.eveant.notification;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface NotificationService {

    @GET("notifications/{userEmail}")
    Call<List<Notification>> getAllNotifications(@Path("userEmail") String userEmail);

    @POST("notifications/{id}/read")
    Call<Void> markAsRead(@Path("id") int id);

    @GET("users/{userEmail}/mute")
    Call<Boolean> getMuteStatus(@Path("userEmail") String userEmail);

    @POST("users/{userEmail}/toggle-mute")
    Call<Void> toggleMute(@Path("userEmail") String userEmail);

}
