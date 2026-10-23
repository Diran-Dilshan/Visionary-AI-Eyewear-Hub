package com.example.final_projects.network;

import java.util.Map;

public class UserRequest {
    private String email;
    private String password;
    private Map<String, Object> data;

    public UserRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public UserRequest(String email, String password, Map<String, Object> data) {
        this.email = email;
        this.password = password;
        this.data = data;
    }
}
