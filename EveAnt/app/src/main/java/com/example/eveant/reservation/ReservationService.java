package com.example.eveant.reservation;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;
public interface ReservationService {
    @GET("api/reservations")
    Call<List<Reservation>> getReservations();

    @GET("api/reservations/filter")
    Call<List<Reservation>> getReservationsByServiceAndDate(
            @Query("service") String service,
            @Query("date") String date
    );
}
