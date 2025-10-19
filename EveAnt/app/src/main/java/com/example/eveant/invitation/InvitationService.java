package com.example.eveant.invitation;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface InvitationService {

    @GET("events/{eventId}/invitations")
    Call<List<Invitation>> getInvitations(@Path("eventId") int eventId);

    @POST("events/{eventId}/invitations")
    Call<Void> sendInvitation(@Path("eventId") int eventId, @Body InvitationRequest request);

    @PUT("events/{invitationId}")
    Call<Void> updateInvitationStatus(@Path("invitationId") int invitationId, @Body InvitationStatusRequest request);

    @DELETE("events/{eventId}/invitations/{email}")
    Call<Void> declineInvitation(@Path("eventId") int eventId, @Path("email") String email);
}
