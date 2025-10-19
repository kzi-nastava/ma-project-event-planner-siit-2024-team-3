package com.example.eveant.admin.adminReports;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReportService {
    @GET("reports")
    Call<List<com.example.eveant.report.Report>> getReports();

    @POST("users/{email}/suspend")
    Call<Void> suspendUser(@Path("email") String email);

    @DELETE("reports/{id}")
    Call<Void> deleteReport(@Path("id") int reportId);
}