package com.example.final_projects.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface EmailApiService {
    @POST("v3/smtp/email")
    Call<Void> sendEmail(
        @Header("api-key") String apiKey,
        @Body EmailRequest request
    );
}
