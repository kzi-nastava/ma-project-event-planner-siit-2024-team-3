package com.example.eveant.budget;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface BudgetService {

    @POST("/api/budgets/{id}/items")
    Call<Item> addItemToBudget(@Path("id") int id, @Body ItemDTO itemDTO);

    @GET("/api/budgets/{id}/items")
    Call<ArrayList<Item>> getAllItemsForBudget(@Path("id") int id);

    @PUT("/api/budgets/items/{itemId}")
    Call<Item> updateItem(@Path("itemId") int itemId, @Body ItemDTO itemDTO);

    @DELETE("/api/budgets/items/{itemId}")
    Call<Void> deleteItem(@Path("itemId") int itemId);

}
