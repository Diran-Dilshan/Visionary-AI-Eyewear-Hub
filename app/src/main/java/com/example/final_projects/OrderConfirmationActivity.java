package com.example.final_projects;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_projects.network.FrameRequest;
import com.example.final_projects.network.OrderRequest;
import com.example.final_projects.network.SupabaseManager;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderConfirmationActivity extends AppCompatActivity {

    private String generatedOrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager.init(this);
        setContentView(R.layout.activity_order_confirmation);

        generatedOrderId = generateOrderId();
        TextView orderIdDisplay = findViewById(R.id.confirm_order_id);
        if (orderIdDisplay != null) {
            orderIdDisplay.setText("Order Ref: " + generatedOrderId);
        }

        String name = getIntent().getStringExtra("DELIVERY_NAME");
        String email = getIntent().getStringExtra("DELIVERY_EMAIL");
        String phone = getIntent().getStringExtra("DELIVERY_PHONE");
        String address = getIntent().getStringExtra("DELIVERY_ADDRESS");

        LinearLayout itemsContainer = findViewById(R.id.confirm_items_container);
        TextView subtotalText = findViewById(R.id.confirm_subtotal);
        TextView totalText = findViewById(R.id.confirm_total);
        
        ((TextView)findViewById(R.id.confirm_name)).setText("Name: " + name);
        ((TextView)findViewById(R.id.confirm_email)).setText("Email: " + email);
        ((TextView)findViewById(R.id.confirm_phone)).setText("Phone: " + phone);
        ((TextView)findViewById(R.id.confirm_address)).setText("Address: " + address);

        double totalAmount = CartManager.getInstance().getTotalAmount();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (CartManager.CartItem item : CartManager.getInstance().getCartItems()) {
            View itemView = inflater.inflate(R.layout.item_cart, itemsContainer, false);
            ImageView img = itemView.findViewById(R.id.item_image);
            loadImage(img, item.imageSource);
            ((TextView)itemView.findViewById(R.id.item_name)).setText(item.name);
            ((TextView)itemView.findViewById(R.id.item_price_qty)).setText(String.format(Locale.US, "Qty: %d | $%.2f", item.quantity, item.price));
            cleanBackground(img);
            itemsContainer.addView(itemView);
            itemView.findViewById(R.id.btn_remove_item).setVisibility(View.GONE);
        }

        subtotalText.setText(String.format(Locale.US, "$%.2f", totalAmount));
        totalText.setText(String.format(Locale.US, "$%.2f", totalAmount));

        findViewById(R.id.btn_back_edit).setOnClickListener(v -> finish());

        findViewById(R.id.btn_confirm_order).setOnClickListener(v -> {
            processFinalOrder(name, email, phone, address, totalAmount);
        });
    }

    private void processFinalOrder(String name, String email, String phone, String address, double totalAmount) {
        findViewById(R.id.btn_confirm_order).setEnabled(false);
        
        StringBuilder summary = new StringBuilder();
        for (CartManager.CartItem item : CartManager.getInstance().getCartItems()) {
            summary.append(item.name).append(" (x").append(item.quantity).append("), ");
        }
        if (summary.length() > 2) summary.setLength(summary.length() - 2);

        OrderRequest dbRequest = new OrderRequest(
                generatedOrderId, name, email, phone, address, totalAmount, 
                summary.toString(), SessionManager.getInstance().getUserId(), "Pending"
        );

        SupabaseManager.getInstance().getAuthService().insertOrder(dbRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    updateProductInventory();
                } else {
                    Toast.makeText(OrderConfirmationActivity.this, "Order Failed", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.btn_confirm_order).setEnabled(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(OrderConfirmationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                findViewById(R.id.btn_confirm_order).setEnabled(true);
            }
        });
    }

    private void updateProductInventory() {
        List<CartManager.CartItem> cartItems = CartManager.getInstance().getCartItems();
        for (CartManager.CartItem item : cartItems) {
            fetchAndUpdateStock(item.id, item.quantity);
        }

        com.example.final_projects.network.EmailSender.sendOrderConfirmation(this, 
                getIntent().getStringExtra("DELIVERY_EMAIL"), generatedOrderId, CartManager.getInstance().getTotalAmount());

        Intent intent = new Intent(this, OrderSuccessActivity.class);
        intent.putExtra("TOTAL_AMOUNT", CartManager.getInstance().getTotalAmount());
        intent.putExtra("DELIVERY_EMAIL", getIntent().getStringExtra("DELIVERY_EMAIL"));
        intent.putExtra("ORDER_ID", generatedOrderId);
        startActivity(intent);
        finish();
    }

    private void fetchAndUpdateStock(int frameId, int orderedQty) {
        String filter = "eq." + frameId;
        SupabaseManager.getInstance().getAuthService().getFrameById(filter).enqueue(new Callback<List<FrameRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<FrameRequest>> call, @NonNull Response<List<FrameRequest>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    FrameRequest frame = response.body().get(0);
                    int newStock = frame.getStockCount() - orderedQty;
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("stock_count", Math.max(0, newStock));
                    
                    SupabaseManager.getInstance().getAuthService().updateFrame(filter, updates).enqueue(new Callback<Void>() {
                        @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            Log.d("StockUpdate", "Frame " + frameId + " stock updated to " + newStock);
                        }
                        @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
                    });
                }
            }
            @Override public void onFailure(@NonNull Call<List<FrameRequest>> call, @NonNull Throwable t) {}
        });
    }

    private void loadImage(ImageView img, String source) {
        if (source == null || source.isEmpty()) {
            img.setImageResource(R.drawable.glass1);
            return;
        }
        if (source.length() > 100) {
            try {
                byte[] decoded = Base64.decode(source, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                img.setImageBitmap(bitmap);
            } catch (Exception e) { img.setImageResource(R.drawable.glass1); }
        } else {
            int resId = getResources().getIdentifier(source, "drawable", getPackageName());
            if (resId != 0) img.setImageResource(resId);
            else img.setImageResource(R.drawable.glass1);
        }
    }

    private String generateOrderId() {
        String datePart = new java.text.SimpleDateFormat("yyyyMMdd", Locale.US).format(new java.util.Date());
        int randomPart = new java.util.Random().nextInt(9000) + 1000;
        return "ORD-" + datePart + "-" + randomPart;
    }

    private void cleanBackground(ImageView imageView) {
        float[] aggressive = { 2.5f, 0, 0, 0, -150f, 0, 2.5f, 0, 0, -150f, 0, 0, 2.5f, 0, -150f, 0, 0, 0, 1, 0 };
        imageView.setColorFilter(new ColorMatrixColorFilter(aggressive));
    }
}
