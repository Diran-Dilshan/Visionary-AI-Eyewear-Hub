package com.example.final_projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_projects.network.AuthResponse;
import com.example.final_projects.network.OrderRequest;
import com.example.final_projects.network.SupabaseManager;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvUsersCount, tvOrdersCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvUsersCount = findViewById(R.id.tv_total_users_count);
        tvOrdersCount = findViewById(R.id.tv_total_orders_count);

        findViewById(R.id.btn_admin_logout).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        MaterialCardView cardUsers = findViewById(R.id.card_manage_users);
        MaterialCardView cardOrders = findViewById(R.id.card_manage_orders);
        MaterialCardView cardFrames = findViewById(R.id.card_manage_frames);
        MaterialCardView cardCust = findViewById(R.id.card_customize_options);

        setupCardAction(cardUsers, ManageUsersActivity.class);
        setupCardAction(cardOrders, ManageOrdersActivity.class);
        setupCardAction(cardFrames, ManageFramesActivity.class);
        setupCardAction(cardCust, ManageCustomizationsActivity.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchQuickStats();
    }

    private void fetchQuickStats() {
        // Fetch Users Count
        SupabaseManager.getInstance().getAuthService().getAllUsers("id").enqueue(new Callback<List<AuthResponse.User>>() {
            @Override
            public void onResponse(Call<List<AuthResponse.User>> call, Response<List<AuthResponse.User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvUsersCount.setText(String.valueOf(response.body().size()));
                }
            }
            @Override public void onFailure(Call<List<AuthResponse.User>> call, Throwable t) {}
        });

        // Fetch Orders Count
        SupabaseManager.getInstance().getAuthService().getAllOrders("order_id").enqueue(new Callback<List<OrderRequest>>() {
            @Override
            public void onResponse(Call<List<OrderRequest>> call, Response<List<OrderRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvOrdersCount.setText(String.valueOf(response.body().size()));
                }
            }
            @Override public void onFailure(Call<List<OrderRequest>> call, Throwable t) {}
        });
    }

    private void setupCardAction(MaterialCardView card, Class<?> targetActivity) {
        Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);

        card.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(scaleDown);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.startAnimation(scaleUp);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                    startActivity(new Intent(this, targetActivity));
                }
            }
            return true;
        });
    }
}
