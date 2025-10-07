package com.example.eveant.communication;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface ChatService {
    @GET("/api/users/{user1}/blocked/{user2}")
    Call<Boolean> checkBlockStatus(@Path("user1") String user1, @Path("user2") String user2);

    @GET("/api/users/username")
    Call<String> getUsernameByEmail(@Query("email") String email);

    @GET("/api/chat/messages/{chatId}")
    Call<List<ChatMessageDTO>> getMessages(@Path("chatId") String chatId);

    @GET("/api/chat/{senderId}/{recipientId}")
    Call<List<ChatMessageDTO>> getMessagesHome(@Path("senderId") String senderId, @Path("recipientId") String recipientId);

    @GET("chat/room/{senderId}/{recipientId}")
    Call<ResponseBody> getChatId(
            @Path("senderId") String senderId,
            @Path("recipientId") String recipientId
    );

}
