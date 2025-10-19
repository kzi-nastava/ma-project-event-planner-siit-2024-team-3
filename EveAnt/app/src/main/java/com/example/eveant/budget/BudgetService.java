package com.example.eveant.budget;

import com.example.eveant.service.model.OfferDTO;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface BudgetService {
    @POST("budgets/create/{eventId}")
    Call<Budget> createBudget(@Path("eventId") int eventId);

    @GET("budgets/{budgetId}/items")
    Call<List<Item>> getItems(@Path("budgetId") int budgetId);

    @POST("budgets/{budgetId}/items")
    Call<Item> addItem(@Path("budgetId") int budgetId, @Body Item item);

    @PUT("budgets/{budgetId}/items")
    Call<Item> updateItem(@Path("budgetId") int budgetId, @Body Item item);


    @DELETE("budgets/items/{itemId}")
    Call<Void> deleteItem(@Path("itemId") int itemId);
    @GET("offers/category/{categoryId}")
    Call<List<OfferDTO>> getOffersByCategory(@Path("categoryId") int categoryId);
}
