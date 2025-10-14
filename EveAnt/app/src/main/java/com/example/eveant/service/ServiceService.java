package com.example.eveant.service;

import com.example.eveant.service.model.Category;
import com.example.eveant.service.model.Service;
import com.example.eveant.service.model.ServiceDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServiceService {
    @GET("/api/services")
    Call<ArrayList<Service>> getAllServices();

    @POST("/api/services")
    Call<Void> createService(@Body ServiceDTO service);
    @DELETE("/api/services/{id}")
    Call<Void> deleteService(@Path("id") int serviceId);

    @GET("/api/categories")
    Call<List<Category>> getCategories();

    @PUT("/api/services/{id}")
    Call<Void> updateService(@Path("id") int id, @Body ServiceDTO serviceDTO);
    @GET("/api/services/search")
    Call<List<Service>> searchServices(
            @Query("search") String search,
            @Query("category") List<String> categories,
            @Query("city") String city,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("sortBy") String sortBy,
            @Query("order") String order
    );
    @GET("/api/services/search")
    Call<List<Service>> getServices(
            @Query("search") String search,
            @Query("status") String status,
            @Query("city") String city,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("minPrice") Integer minPrice,
            @Query("maxPrice") Integer maxPrice,
            @Query("eventTypes") List<String> eventTypes,
            @Query("categories") List<String> categories,
            @Query("userEmail") String userEmail,
            @Query("sortBy") String sortBy,
            @Query("order") String order
    );

}
