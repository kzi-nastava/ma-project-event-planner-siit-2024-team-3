package com.example.eveant.invitation;

public class Invitation {
    private int id;
    private String email;
    private boolean accepted;
    private boolean declined;
    private String message;

    public Invitation() {}

    public Invitation(int id, String email, boolean accepted, boolean declined, String message) {
        this.id = id;
        this.email = email;
        this.accepted = accepted;
        this.declined = declined;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isDeclined() {
        return declined;
    }

    public void setDeclined(boolean declined) {
        this.declined = declined;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Invitation{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", accepted=" + accepted +
                ", declined=" + declined +
                ", message='" + message + '\'' +
                '}';
    }
}
