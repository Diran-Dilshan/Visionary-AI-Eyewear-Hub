package com.example.final_projects;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_projects.network.FrameRequest;
import com.example.final_projects.network.SupabaseManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GlassCatalogActivity extends AppCompatActivity {

    private TextView cartBadge;
    private LinearLayout productListContainer;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass_catalog);

        cartBadge = findViewById(R.id.cart_badge);
        productListContainer = findViewById(R.id.product_list);
        progressBar = findViewById(R.id.progress_catalog);

        findViewById(R.id.btn_back_catalog).setOnClickListener(v -> finish());
        findViewById(R.id.btn_cart).setOnClickListener(v -> showCartSheet());
        
        updateCartBadge();
        fetchFramesFromDatabase();
    }

    private void fetchFramesFromDatabase() {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseManager.getInstance().getAuthService().getAllFrames("*").enqueue(new Callback<List<FrameRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<FrameRequest>> call, @NonNull Response<List<FrameRequest>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayFrames(response.body());
                } else {
                    Toast.makeText(GlassCatalogActivity.this, "Database error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FrameRequest>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GlassCatalogActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayFrames(List<FrameRequest> frames) {
        productListContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (FrameRequest frame : frames) {
            View card = inflater.inflate(R.layout.item_catalog_frame, productListContainer, false);
            
            ImageView img = card.findViewById(R.id.img_frame);
            TextView name = card.findViewById(R.id.tv_frame_name);
            TextView ratingText = card.findViewById(R.id.tv_frame_rating_text);
            TextView desc = card.findViewById(R.id.tv_frame_desc);
            TextView price = card.findViewById(R.id.tv_frame_price);
            TextView status = card.findViewById(R.id.tv_frame_status);

            name.setText(frame.getName());
            ratingText.setText(String.format(Locale.US, "%.1f (%d)", frame.getRating(), frame.getReviewCount()));
            desc.setText(frame.getDescription());
            price.setText(String.format(Locale.US, "$%.2f", frame.getPrice()));
            
            // Show Stock Count
            if (frame.getStockCount() > 0) {
                status.setText(String.format(Locale.US, "Available: %d", frame.getStockCount()));
                status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E8F5E9")));
                status.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
            } else {
                status.setText("Out of Stock");
                status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEE")));
                status.setTextColor(android.graphics.Color.parseColor("#C62828"));
            }

            loadImage(img, frame.getImageUrl());
            img.clearColorFilter();

            com.google.android.material.button.MaterialButton btnCart = card.findViewById(R.id.btn_card_cart);
            if (frame.getStockCount() > 0) {
                btnCart.setText("Add to Cart");
                btnCart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#5CBDB9")));
                btnCart.setIconResource(R.drawable.ic_cart);
                btnCart.setOnClickListener(v -> addToCart(frame));
            } else {
                btnCart.setText("Notify Me");
                btnCart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#CBD3D2")));
                btnCart.setIconResource(R.drawable.ic_notification);
                btnCart.setOnClickListener(v -> 
                    Toast.makeText(GlassCatalogActivity.this, "Notification set for " + frame.getName() + " when back in stock!", Toast.LENGTH_SHORT).show()
                );
            }

            card.findViewById(R.id.btn_card_tryon).setOnClickListener(v -> navigateToTryOn());
            card.findViewById(R.id.btn_card_customize).setOnClickListener(v -> navigateToCustomization());

            productListContainer.addView(card);
        }
    }

    private void loadImage(ImageView img, String source) {
        if (source == null || source.isEmpty()) {
            img.setImageResource(R.drawable.glass1);
            return;
        }

        if (source.length() > 100) { // Assume Base64
            try {
                byte[] decoded = Base64.decode(source, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                img.setImageBitmap(bitmap);
            } catch (Exception e) {
                img.setImageResource(R.drawable.glass1);
            }
        } else { // Assume Resource Name
            int resId = getResources().getIdentifier(source, "drawable", getPackageName());
            if (resId != 0) img.setImageResource(resId);
            else img.setImageResource(R.drawable.glass1);
        }
    }

    private void addToCart(FrameRequest frame) {
        // Check if stock is available
        int currentInCart = CartManager.getInstance().getItemQuantity(frame.getId());
        if (currentInCart < frame.getStockCount()) {
            CartManager.getInstance().addItem(frame.getId(), frame.getName(), frame.getPrice(), frame.getImageUrl());
            updateCartBadge();
            showCartSheet();
        } else {
            Toast.makeText(this, "Sorry, that product quantity is unavailable", Toast.LENGTH_LONG).show();
        }
    }

    private void updateCartBadge() {
        int count = CartManager.getInstance().getItemsCount();
        if (count > 0) {
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisibility(View.VISIBLE);
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    }

    private void showCartSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_cart_sheet, null);
        dialog.setContentView(view);

        LinearLayout container = view.findViewById(R.id.cart_items_container);
        TextView totalText = view.findViewById(R.id.cart_total);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (CartManager.CartItem item : CartManager.getInstance().getCartItems()) {
            View itemRow = inflater.inflate(R.layout.item_cart, container, false);
            ImageView img = itemRow.findViewById(R.id.item_image);
            loadImage(img, item.imageSource);
            ((TextView)itemRow.findViewById(R.id.item_name)).setText(item.name);
            ((TextView)itemRow.findViewById(R.id.item_price_qty)).setText(String.format(Locale.US, "Qty: %d | $%.2f", item.quantity, item.price));
            container.addView(itemRow);
        }

        totalText.setText(String.format(Locale.US, "$%.2f", CartManager.getInstance().getTotalAmount()));
        view.findViewById(R.id.btn_checkout).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, CheckoutActivity.class));
        });
        dialog.show();
    }

    private void cleanBackground(ImageView imageView) {
        ColorMatrix cm = new ColorMatrix();
        float[] aggressive = { 2.5f, 0, 0, 0, -150f, 0, 2.5f, 0, 0, -150f, 0, 0, 2.5f, 0, -150f, 0, 0, 0, 1, 0 };
        imageView.setColorFilter(new ColorMatrixColorFilter(aggressive));
    }

    private void navigateToCustomization() {
        startActivity(new Intent(this, FrameSelectionActivity.class));
    }

    private void navigateToTryOn() {
        startActivity(new Intent(this, VirtualTryOnActivity.class));
    }
}
