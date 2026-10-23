package com.example.final_projects.network;

import com.google.gson.annotations.SerializedName;

public class OrderRequest {
    @SerializedName("order_id")
    private String orderId;

    @SerializedName("customer_name")
    private String customerName;

    @SerializedName("customer_email")
    private String customerEmail;

    @SerializedName("customer_phone")
    private String customerPhone;

    @SerializedName("delivery_address")
    private String deliveryAddress;

    @SerializedName("total_amount")
    private double totalAmount;

    @SerializedName("items_summary")
    private String itemsSummary;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    public OrderRequest(String orderId, String customerName, String customerEmail, 
                        String customerPhone, String deliveryAddress, double totalAmount, 
                        String itemsSummary, String userId, String status) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.deliveryAddress = deliveryAddress;
        this.totalAmount = totalAmount;
        this.itemsSummary = itemsSummary;
        this.userId = userId;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public double getTotalAmount() { return totalAmount; }
    public String getItemsSummary() { return itemsSummary; }
    public String getUserId() { return userId; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
