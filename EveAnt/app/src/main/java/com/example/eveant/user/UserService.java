package com.example.eveant.user;

import com.example.eveant.user.model.User;
import com.example.eveant.user.model.UserProfileRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @POST("/api/users")
    Call<ResponseBody> registerUser(@Body UserProfileRequest userProfileRequest);
}
