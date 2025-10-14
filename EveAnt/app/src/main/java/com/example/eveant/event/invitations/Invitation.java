package com.example.eveant.event.invitations;

// com.example.eveant.invitation.Invitation.java

public class Invitation {
    public long id;
    public String email;
    public boolean accepted;   // maps to { accepted: true/false }
    public String message;
    public int eventId;
}


class StatusUpdate {
    public boolean accepted;
    public StatusUpdate(boolean accepted) { this.accepted = accepted; }
}
