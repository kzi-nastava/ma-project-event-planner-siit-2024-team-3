package com.example.eveant.user;

import com.example.eveant.user.model.User;
import com.example.eveant.user.model.UserProfileRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @POST("/api/users")
    Call<ResponseBody> registerUser(@Body UserProfileRequest userProfileRequest);

    @GET("/api/users/check-email")
    Call<Boolean> checkEmailExists(@Query("email") String email);

    @GET("/api/users/check-username")
    Call<Boolean> checkUsernameExists(@Query("username") String username);
}
