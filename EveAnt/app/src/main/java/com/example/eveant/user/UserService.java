package com.example.eveant.user;

import com.example.eveant.user.model.LoginRequest;
import com.example.eveant.user.model.Profile;
import com.example.eveant.user.model.User;
import com.example.eveant.user.model.UserProfileRequest;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
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

    @GET("/api/users/profile")
    Call<Profile> getProfile(@Query("username") String username);

    @POST("/api/auth/login")
    Call<Map<String, String>> login(@Body LoginRequest loginRequest);

    @GET("/api/users/profile")
    Call<Profile> getProfile(@Query("username") String username);

    @GET("/api/users/user")
    Call<User> getUser(@Query("username") String username);

    @PUT("users/{id}")
    Call<Void> updateUser(@Path("id") int id, @Body User user);
  
    @PUT("profiles/{id}")
    Call<Void> updateProfile(@Path("id") int id, @Body Profile profile);
  
    @GET("/api/auth/check-activation")
    Call<Boolean> checkActivationStatus(@Query("email") String email);
  
    @POST("/api/auth/send-activation-email")
    @Headers("Content-Type: text/plain")
    Call<Map<String, String>> sendActivationEmail(@Body String email);

}
