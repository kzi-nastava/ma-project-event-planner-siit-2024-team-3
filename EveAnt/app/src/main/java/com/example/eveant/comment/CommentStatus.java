// CommentStatus.java
package com.example.eveant.comment;

public enum CommentStatus {
    PENDING("PENDING"),
    RESOLVED("RESOLVED");

    private final String value;

    CommentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CommentStatus fromString(String value) {
        for (CommentStatus status : CommentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown comment status: " + value);
    }
}