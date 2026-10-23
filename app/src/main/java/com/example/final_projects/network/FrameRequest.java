package com.example.final_projects.network;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FrameRequest implements Serializable {
    @SerializedName("id")
    private Integer id;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("price")
    private double price;
    @SerializedName("status")
    private String status;
    @SerializedName("rating")
    private double rating;
    @SerializedName("review_count")
    private int reviewCount;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("stock_count")
    private int stockCount;

    public FrameRequest(String name, String description, double price, String status, double rating, int reviewCount, String imageUrl, int stockCount) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.imageUrl = imageUrl;
        this.stockCount = stockCount;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public String getImageUrl() { return imageUrl; }
    public int getStockCount() { return stockCount; }
}
