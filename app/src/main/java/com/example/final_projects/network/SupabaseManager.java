package com.example.final_projects.network;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseManager {
    // Guaranteed clean URL string
    public static final String SUPABASE_URL = "https://sdjiwnwkhnvyqkpppjog.supabase.co/";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNkaml3bndraG52eXFrcHBwam9nIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI1NTIzMTIsImV4cCI6MjA5ODEyODMxMn0.8AA4jMnAXNqPCGminFhGucOT0E6Pj9Nya-Qe4iIjw10";

    private static SupabaseManager instance;
    private AuthService authService;

    private SupabaseManager() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("apikey", SUPABASE_ANON_KEY)
                            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        authService = retrofit.create(AuthService.class);
    }

    public static synchronized SupabaseManager getInstance() {
        if (instance == null) {
            instance = new SupabaseManager();
        }
        return instance;
    }

    public AuthService getAuthService() {
        return authService;
    }
}
