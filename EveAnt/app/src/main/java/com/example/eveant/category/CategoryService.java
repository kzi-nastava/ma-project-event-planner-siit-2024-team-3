package com.example.eveant.category;

import com.example.eveant.service.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoryService {
    @GET("/api/categories")
    Call<List<Category>> getCategories();

    @PUT("/api/categories/{id}")
    Call<Category> updateCategory(@Path("id") int id, @Body Category category);

    @DELETE("/api/categories/{id}")
    Call<Void> deleteCategory(@Path("id") int id);

    @POST("/api/categories")
    Call<Category> createCategory(@Body Category category);
}
