package com.example.final_projects.network;

import com.google.gson.annotations.SerializedName;

public class CustomizationRequest {
    @SerializedName("id")
    private Integer id;
    @SerializedName("type")
    private String type;
    @SerializedName("name")
    private String name;
    @SerializedName("value")
    private String value;

    public CustomizationRequest(String type, String name, String value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public Integer getId() { return id; }
    public String getType() { return type; }
    public String getName() { return name; }
    public String getValue() { return value; }
}
