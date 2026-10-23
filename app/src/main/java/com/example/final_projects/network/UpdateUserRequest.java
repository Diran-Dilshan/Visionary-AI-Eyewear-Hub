package com.example.final_projects.network;

import com.google.gson.annotations.SerializedName;

public class UpdateUserRequest {
    @SerializedName("password")
    private String password;

    public UpdateUserRequest(String password) {
        this.password = password;
    }
}
