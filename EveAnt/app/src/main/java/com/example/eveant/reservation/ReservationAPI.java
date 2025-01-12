package com.example.eveant.reservation;
import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;
public interface ReservationAPI {
    @GET("api/reservations")
    Call<List<Reservation>> getReservations();
}
