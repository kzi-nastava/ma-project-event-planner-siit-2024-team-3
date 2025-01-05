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

public interface ApiService {
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


}
