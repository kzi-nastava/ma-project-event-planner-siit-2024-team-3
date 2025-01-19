package com.example.eveant.communication;

import com.example.eveant.communication.ChatSession;
import com.example.eveant.communication.Message;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatSessionService {

    @POST("/api/chat/start")
    Call<ChatSession> startChat(@Body StartChatRequest request);

    @POST("/api/chat/send")
    Call<Void> sendMessage(@Body SendMessageRequest request);

    @GET("/api/chat/messages")
    Call<List<Message>> getMessages(@Query("chatSessionId")Integer chatSessionId);

    @POST("/api/chat/read")
    Call<Void> markAsRead(@Path("chatSessionId") Integer chatSessionId);


}
