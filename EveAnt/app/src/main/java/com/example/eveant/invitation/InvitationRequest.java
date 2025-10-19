package com.example.eveant.invitation;

public class InvitationRequest {
    private String email;
    private String message;
    private int eventId;

    public InvitationRequest(String email, String message, int eventId) {
        this.email = email;
        this.message = message;
        this.eventId = eventId;
    }
}
