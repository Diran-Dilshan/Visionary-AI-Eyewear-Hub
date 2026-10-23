package com.example.final_projects;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();

    public static class CartItem {
        public int id;
        public String name;
        public double price;
        public String imageSource; // Resource name or Base64
        public int quantity;

        public CartItem(int id, String name, double price, String imageSource) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.imageSource = imageSource;
            this.quantity = 1;
        }
    }

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void addItem(int id, String name, double price, String imageSource) {
        for (CartItem item : cartItems) {
            if (item.id == id) {
                item.quantity++;
                return;
            }
        }
        cartItems.add(new CartItem(id, name, price, imageSource));
    }

    public int getItemQuantity(int id) {
        for (CartItem item : cartItems) {
            if (item.id == id) {
                return item.quantity;
            }
        }
        return 0;
    }

    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.price * item.quantity;
        }
        return total;
    }

    public int getItemsCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.quantity;
        }
        return count;
    }

    public void clear() {
        cartItems.clear();
    }
}
