package com.example.eveant.service.model;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("/api/services")
    Call<ArrayList<Service>> getAllServices();
}
