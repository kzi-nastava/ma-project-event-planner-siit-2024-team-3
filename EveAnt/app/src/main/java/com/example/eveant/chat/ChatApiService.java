package com.example.eveant.chat;

import com.example.eveant.user.model.Profile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ChatApiService {
    @GET("/api/users/contacts/{userEmail}")
    Call<List<Profile>> getUsersChattedWith(@Path("userEmail") String email);

    @GET("/api/chat/messages/{chatId}")
    Call<List<ChatMessage>> getMessagesByChatId(@Path("chatId") String chatId);
}
