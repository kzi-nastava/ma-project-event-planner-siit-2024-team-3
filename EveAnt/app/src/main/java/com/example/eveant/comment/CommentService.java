// CommentService.java
package com.example.eveant.comment;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface CommentService {

    @POST("api/comments")
    Call<Comment> addComment(@Body CommentRequest comment);

    @GET("api/comments/event/{eventId}")
    Call<List<Comment>> getApprovedComments(@Path("eventId") Integer eventId);

    @GET("api/comments/pending")
    Call<List<Comment>> getPendingComments();

    @POST("api/comments/{id}/approve")
    Call<Void> approveComment(@Path("id") Integer id);

    @DELETE("api/comments/{id}")
    Call<Void> deleteComment(@Path("id") Integer id);

    // Request class for adding comments
    class CommentRequest {
        private String content;
        private Integer eventId;
        private String userEmail;

        public CommentRequest(String content, Integer eventId, String userEmail) {
            this.content = content;
            this.eventId = eventId;
            this.userEmail = userEmail;
        }

        // Getters and setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public Integer getEventId() { return eventId; }
        public void setEventId(Integer eventId) { this.eventId = eventId; }

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    }
}