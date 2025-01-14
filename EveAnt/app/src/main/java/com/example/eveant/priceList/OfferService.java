package com.example.eveant.priceList;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface OfferService {

    @GET("/api/offers/priceList/{id}")
    Call<List<PriceListItem>> getPriceList(@Path("id") int id);

    @PUT("/api/offers/priceList/{id}")
    Call<PriceListItem> updateOfferPriceAndDiscount(@Path("id") int id, @Body PriceListItem priceListItem);
}
