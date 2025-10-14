package com.example.eveant.service.model;

import com.example.eveant.service.model.Service;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServiceService {
    @GET("services")
    Call<ArrayList<Service>> getAllServices();

    @GET("service/{id}")
    Call<Service> getById(@Path("id") int id);

    @POST("service")
    Call<Service> add(@Body Service service);
    @DELETE("service/{id}")
    Call<ResponseBody> deleteById(@Path("id") Long id);

    @PUT("service/")
    Call<Service> edit(@Body Service product);

    @GET("/api/services/search")
    Call<List<Service>> searchServices(
            @Query("search") String search,
            @Query("status") String status,
            @Query("city") String city,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("eventTypes") List<String> eventTypes,
            @Query("categories") List<String> categories,
            @Query("userEmail") String userEmail,
            @Query("sortBy") String sortBy,
            @Query("order") String order
    );
}