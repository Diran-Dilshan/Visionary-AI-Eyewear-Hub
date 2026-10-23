package com.example.final_projects.network;

import com.google.gson.annotations.SerializedName;

public class RecoverRequest {
    @SerializedName("email")
    private String email;

    public RecoverRequest(String email) {
        this.email = email;
    }
}
