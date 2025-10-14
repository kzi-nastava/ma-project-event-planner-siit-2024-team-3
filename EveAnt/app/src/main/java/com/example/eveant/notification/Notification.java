package com.example.eveant.notification;

public class Notification {
    private int id;
    private String message;
    private String timestamp;
    private boolean read;

    // Empty constructor (required for Gson / Retrofit)
    public Notification() {}

    // Full constructor
    public Notification(int id, String message, String timestamp, boolean read) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return read;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    // Optional: nice string representation for debugging
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", read=" + read +
                '}';
    }
}
