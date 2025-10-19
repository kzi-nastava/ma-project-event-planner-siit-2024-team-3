package com.example.eveant.event.invitations;

// com.example.eveant.invitation.InvitationEventService.java

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface InvitationEventService {

    // GET /api/events/{eventId}/invitations
    @GET("/api/events/{eventId}/invitations")
    Call<List<Invitation>> getInvitations(@Path("eventId") int eventId);

    // POST /api/events/{eventId}/invitations { email, message, eventId }
    @POST("/api/events/{eventId}/invitations")
    Call<Void> sendInvitation(@Path("eventId") int eventId, @Body InviteRequest body);

    // DELETE /api/events/{eventId}/invitations/{email}
    // NOTE: keep encoded=true so emails with '+' or '@' are accepted
    @DELETE("/api/events/{eventId}/invitations/{email}")
    Call<Void> declineInvitation(@Path("eventId") int eventId,
                                 @Path(value = "email", encoded = true) String email);

    // PUT /api/invitations/{invitationId} { accepted: boolean }
    @PUT("/api/invitations/{invitationId}")
    Call<Invitation> updateInvitationStatus(@Path("invitationId") long invitationId,
                                            @Body StatusUpdate body);
}
