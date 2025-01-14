package com.example.eveant.budget;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface BudgetService {

    @POST("/api/budgets/{id}/items")
    Call<ItemDTO> addItemToBudget(@Path("id") int id, @Body ItemDTO itemDTO);

    @GET("/api/budgets/{id}/items")
    Call<ArrayList<Item>> getAllItemsForBudget(@Path("id") int id);
}
