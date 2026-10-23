package com.example.final_projects.network;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.example.final_projects.BuildConfig;
import java.util.Collections;
import java.util.Locale;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EmailSender {
    private static final String BREVO_API_KEY = BuildConfig.BREVO_API_KEY;
    
    private static EmailApiService service;

    private static EmailApiService getService() {
        if (service == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.brevo.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            service = retrofit.create(EmailApiService.class);
        }
        return service;
    }

    public static void sendOrderConfirmation(Context context, String userEmail, String orderId, double amount) {
        if (BREVO_API_KEY.isEmpty()) {
            Log.e("EmailSender", "Brevo API key is not configured.");
            Toast.makeText(context, "Email service is not configured.", Toast.LENGTH_SHORT).show();
            return;
        }

        EmailRequest request = new EmailRequest();
        // IMPORTANT: The sender email MUST be verified in your Brevo dashboard.
        // Usually, this is the email address you used to sign up for Brevo.
        request.sender = new EmailRequest.Person("Visionary Elite", "dirandilshanmax@gmail.com");
        request.to = Collections.singletonList(new EmailRequest.Person("Customer", userEmail));
        request.subject = "Order Confirmed - " + orderId;
        
        String totalFormatted = String.format(Locale.US, "%.2f", amount);
        
        request.htmlContent = "<html><body style='font-family: sans-serif; padding: 20px;'>" +
                "<div style='border-bottom: 2px solid #008080; padding-bottom: 10px;'>" +
                "<h1 style='color: #008080;'>Order Placed Successfully!</h1>" +
                "</div>" +
                "<p>Thank you for choosing <b>Visionary Elite</b>. Your order is confirmed.</p>" +
                "<div style='background: #f4f4f4; padding: 15px; border-radius: 10px;'>" +
                "<p><strong>Order ID:</strong> " + orderId + "</p>" +
                "<p><strong>Total Amount:</strong> $" + totalFormatted + "</p>" +
                "</div>" +
                "<p>Stay Stylish,<br>The Visionary Elite Team</p>" +
                "</body></html>";

        getService().sendEmail(BREVO_API_KEY, request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (response.isSuccessful()) {
                        Log.d("EmailSender", "Email Sent Successfully!");
                        Toast.makeText(context, "Confirmation sent to inbox!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("EmailSender", "Failed: " + response.code());
                        if (response.code() == 401) {
                            Toast.makeText(context, "Email Service API Key expired (401).", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, "Email delivery pending (Error " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Log.e("EmailSender", "Network Failure: " + t.getMessage());
                    Toast.makeText(context, "Check internet connection for email.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
