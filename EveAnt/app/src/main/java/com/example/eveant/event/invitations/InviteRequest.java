package com.example.eveant.event.invitations;

public class InviteRequest {
    public String email;
    public String message;
    public int eventId;

    public InviteRequest(String email, String message, int eventId) {
        this.email = email;
        this.message = message;
        this.eventId = eventId;
    }
}
