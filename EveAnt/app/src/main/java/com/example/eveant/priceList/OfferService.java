package com.example.eveant.priceList;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface OfferService {

    @GET("/api/offers/priceList/{providerUsername}")
    Call<List<PriceListItem>> getPriceList(@Path("providerUsername") String providerUsername);


    @PUT("/api/offers/priceList/{id}")
    Call<PriceListItem> updateOfferPriceAndDiscount(@Path("id") int id, @Body PriceListItem priceListItem);

    @GET("/api/offers/priceList/pdf/{providerUsername}")
    Call<ResponseBody> downloadPriceListPdf(@Path("providerUsername") String providerUsername);

}
