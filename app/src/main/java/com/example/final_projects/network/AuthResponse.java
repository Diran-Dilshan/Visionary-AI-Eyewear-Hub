package com.example.final_projects.network;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;
    
    @SerializedName("user")
    private User user;

    public String getAccessToken() {
        return accessToken;
    }

    public User getUser() {
        return user;
    }

    public static class User {
        @SerializedName("id")
        private String id;
        
        @SerializedName("email")
        private String email;

        @SerializedName("full_name")
        private String fullName;

        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getFullName() {
            return fullName;
        }
    }
}
