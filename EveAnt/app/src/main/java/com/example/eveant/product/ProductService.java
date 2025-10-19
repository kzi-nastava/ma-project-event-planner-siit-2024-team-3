package com.example.eveant.product;

import com.example.eveant.service.model.OfferStatus;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductService {

    @GET("/api/products")
    Call<List<Product>> searchProducts(
            @Query("search") String search,
            @Query("categories") List<String> categories,
            @Query("status") OfferStatus status,
            @Query("minPrice") Long minPrice,
            @Query("maxPrice") Long maxPrice,
            @Query("eventTypes") List<String> eventTypes,
            @Query("city") String city,
            @Query("userEmail") String userEmail,
            @Query("sortBy") String sortBy,
            @Query("order") String order,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    @GET("/api/products/{id}")
    Call<Product> getProduct(@Path("id") int id);

    @POST("/api/products")
    Call<Product> createProduct(@Body Product body);

    @PUT("/api/products/{id}")
    Call<Product> updateProduct(@Path("id") int id, @Body Product body);

    @DELETE("/api/products/{id}")
    Call<Void> deleteProduct(@Path("id") int id);
}
