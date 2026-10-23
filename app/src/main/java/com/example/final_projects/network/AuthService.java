package com.example.final_projects.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.DELETE;

import com.example.final_projects.network.FrameRequest;
import com.example.final_projects.network.CustomizationRequest;

import java.util.Map;

public interface AuthService {
    
    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(
            @Body UserRequest request
    );

    @POST("auth/v1/token")
    Call<AuthResponse> login(
            @Query("grant_type") String grantType,
            @Body UserRequest request
    );

    @POST("auth/v1/recover")
    Call<okhttp3.ResponseBody> recoverPassword(
            @Body RecoverRequest request
    );

    @PATCH("auth/v1/user")
    Call<AuthResponse> updateUser(
            @Header("Authorization") String token,
            @Body UpdateUserRequest request
    );

    @GET("rest/v1/users")
    Call<List<AuthResponse.User>> getUserData(
            @Query("id") String filter,
            @Query("select") String select
    );

    @GET("rest/v1/users")
    Call<List<AuthResponse.User>> getAllUsers(
            @Query("select") String select
    );

    @POST("rest/v1/users")
    Call<Void> insertUserRecord(
            @Body java.util.Map<String, Object> userRecord
    );

    @PATCH("rest/v1/users")
    Call<Void> updateUserData(
            @Query("id") String filter,
            @Body Map<String, Object> updates
    );

    @DELETE("rest/v1/users")
    Call<Void> deleteUserData(
            @Query("id") String filter
    );

    @GET("rest/v1/orders")
    Call<List<OrderRequest>> getAllOrders(
            @Query("select") String select
    );

    @GET("rest/v1/orders")
    Call<List<OrderRequest>> getOrdersByUserId(
            @Query("user_id") String userIdFilter,
            @Query("select") String select
    );

    @PATCH("rest/v1/orders")
    Call<Void> updateOrder(
            @Query("order_id") String filter,
            @Body Map<String, Object> updates
    );

    @DELETE("rest/v1/orders")
    Call<Void> deleteOrder(
            @Query("order_id") String filter
    );

    @GET("rest/v1/frames")
    Call<List<FrameRequest>> getAllFrames(
            @Query("select") String select
    );

    @GET("rest/v1/frames")
    Call<List<FrameRequest>> getFrameById(
            @Query("id") String filter
    );

    @POST("rest/v1/frames")
    Call<Void> insertFrame(
            @Body FrameRequest request
    );

    @PATCH("rest/v1/frames")
    Call<Void> updateFrame(
            @Query("id") String filter,
            @Body Map<String, Object> updates
    );

    @DELETE("rest/v1/frames")
    Call<Void> deleteFrame(
            @Query("id") String filter
    );

    @GET("rest/v1/customizations")
    Call<List<CustomizationRequest>> getAllCustomizations(
            @Query("select") String select
    );

    @POST("rest/v1/customizations")
    Call<Void> insertCustomization(
            @Body CustomizationRequest request
    );

    @PATCH("rest/v1/customizations")
    Call<Void> updateCustomization(
            @Query("id") String filter,
            @Body Map<String, Object> updates
    );

    @DELETE("rest/v1/customizations")
    Call<Void> deleteCustomization(
            @Query("id") String filter
    );

    @POST("rest/v1/orders")
    Call<Void> insertOrder(
            @Body OrderRequest request
    );
}
