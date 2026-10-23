package com.example.final_projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_projects.network.AuthResponse;
import com.example.final_projects.network.SupabaseManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OffersActivity extends AppCompatActivity {

    private TextView userNameText;
    private View secHero, dotsContainer, secTrending, secSummer, secHealth, secStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager.init(this);
        setContentView(R.layout.activity_offers);

        userNameText = findViewById(R.id.tv_user_name_offers);

        // Section Views for Interactive Filtering
        secHero = findViewById(R.id.section_hero_promo);
        dotsContainer = findViewById(R.id.dots_container);
        secTrending = findViewById(R.id.section_trending);
        secSummer = findViewById(R.id.section_summer_collection);
        secHealth = findViewById(R.id.section_health_tip);
        secStore = findViewById(R.id.section_store_update);

        String userId = SessionManager.getInstance().getUserId();
        if (userId != null) {
            fetchUserProfile(userId);
        }

        // Action Buttons
        findViewById(R.id.btn_shop_now).setOnClickListener(v -> 
            startActivity(new Intent(this, GlassCatalogActivity.class))
        );

        findViewById(R.id.btn_see_all_trending).setOnClickListener(v -> 
            startActivity(new Intent(this, GlassCatalogActivity.class))
        );

        findViewById(R.id.btn_browse_summer).setOnClickListener(v -> 
            startActivity(new Intent(this, GlassCatalogActivity.class))
        );

        findViewById(R.id.btn_read_health).setOnClickListener(v -> 
            Toast.makeText(this, "Eye Care Tip: Remember the 20-20-20 rule to rest your eyes!", Toast.LENGTH_LONG).show()
        );

        findViewById(R.id.btn_book_slot).setOnClickListener(v -> 
            startActivity(new Intent(this, VisionCareActivity.class))
        );

        // Bottom Navigation Bar Setup
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            selectTab(v);
            Intent intent = new Intent(this, LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.nav_offers).setOnClickListener(v -> 
            selectTab(v)
        );

        findViewById(R.id.nav_orders).setOnClickListener(v -> {
            selectTab(v);
            startActivity(new Intent(this, OrderTrackingActivity.class));
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            selectTab(v);
            Toast.makeText(this, "Profile: " + (userNameText != null ? userNameText.getText() : "User"), Toast.LENGTH_SHORT).show();
        });

        // Apply Touch Scaling & Morphing Feedback
        View[] interactiveViews = {
            findViewById(R.id.btn_shop_now),
            findViewById(R.id.btn_browse_summer),
            findViewById(R.id.btn_read_health),
            findViewById(R.id.btn_book_slot),
            findViewById(R.id.btn_bell_offers),
            findViewById(R.id.nav_home),
            findViewById(R.id.nav_offers),
            findViewById(R.id.nav_orders),
            findViewById(R.id.nav_profile),
            secHero,
            secSummer,
            secHealth,
            secStore
        };

        for (View view : interactiveViews) {
            if (view != null) {
                setupTouchFeedback(view);
            }
        }

        setupFilterChips();
    }

    private void setupTouchFeedback(View view) {
        android.view.animation.Animation scaleDown = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_down);
        android.view.animation.Animation scaleUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_up);
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.startAnimation(scaleDown);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.startAnimation(scaleUp);
            }
            return false;
        });
    }

    private void fetchUserProfile(String userId) {
        String filter = "eq." + userId;
        SupabaseManager.getInstance().getAuthService().getUserData(filter, "full_name").enqueue(new Callback<List<AuthResponse.User>>() {
            @Override
            public void onResponse(Call<List<AuthResponse.User>> call, Response<List<AuthResponse.User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String name = response.body().get(0).getFullName();
                    if (name != null && userNameText != null) {
                        userNameText.setText(name);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AuthResponse.User>> call, Throwable t) {
                // Ignore failure
            }
        });
    }

    private void selectTab(View selectedTab) {
        int[] tabIds = {R.id.nav_home, R.id.nav_offers, R.id.nav_orders, R.id.nav_profile};

        for (int id : tabIds) {
            View tab = findViewById(id);
            if (tab instanceof android.view.ViewGroup) {
                android.view.ViewGroup group = (android.view.ViewGroup) tab;
                ImageView icon = null;
                TextView text = null;

                for (int i = 0; i < group.getChildCount(); i++) {
                    View child = group.getChildAt(i);
                    if (child instanceof ImageView) icon = (ImageView) child;
                    else if (child instanceof TextView) text = (TextView) child;
                }

                if (id == selectedTab.getId()) {
                    tab.setBackgroundResource(R.drawable.category_bg_selected);
                    tab.setPadding(0, 16, 0, 16);
                    if (icon != null) icon.setColorFilter(0xFFFFFFFF);
                    if (text != null) {
                        text.setTextColor(0xFFFFFFFF);
                        text.setTypeface(null, android.graphics.Typeface.BOLD);
                    }
                } else {
                    tab.setBackground(null);
                    tab.setPadding(0, 0, 0, 0);
                    if (icon != null) icon.setColorFilter(0xFF666666);
                    if (text != null) {
                        text.setTextColor(0xFF666666);
                        text.setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                }
            }
        }
    }

    private void setupFilterChips() {
        View chipAll = findViewById(R.id.chip_all_offers);
        View chipOffers = findViewById(R.id.chip_offers_only);
        View chipCollections = findViewById(R.id.chip_collections);
        View chipHealth = findViewById(R.id.chip_health_tips);

        View[] chips = {chipAll, chipOffers, chipCollections, chipHealth};

        View.OnClickListener chipListener = v -> {
            for (View chip : chips) {
                if (chip != null) {
                    chip.setBackgroundResource(R.drawable.category_bg);
                    if (chip instanceof TextView) {
                        ((TextView) chip).setTextColor(0xFF666666);
                    }
                }
            }
            v.setBackgroundResource(R.drawable.category_bg_selected);
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(0xFFFFFFFF);
            }

            // Filter visible sections dynamically
            filterSections(v.getId());
        };

        for (View chip : chips) {
            if (chip != null) chip.setOnClickListener(chipListener);
        }
    }

    private void filterSections(int selectedId) {
        if (selectedId == R.id.chip_offers_only) {
            secHero.setVisibility(View.VISIBLE);
            dotsContainer.setVisibility(View.VISIBLE);
            secTrending.setVisibility(View.GONE);
            secSummer.setVisibility(View.VISIBLE);
            secHealth.setVisibility(View.GONE);
            secStore.setVisibility(View.GONE);
        } else if (selectedId == R.id.chip_collections) {
            secHero.setVisibility(View.GONE);
            dotsContainer.setVisibility(View.GONE);
            secTrending.setVisibility(View.VISIBLE);
            secSummer.setVisibility(View.VISIBLE);
            secHealth.setVisibility(View.GONE);
            secStore.setVisibility(View.GONE);
        } else if (selectedId == R.id.chip_health_tips) {
            secHero.setVisibility(View.GONE);
            dotsContainer.setVisibility(View.GONE);
            secTrending.setVisibility(View.GONE);
            secSummer.setVisibility(View.GONE);
            secHealth.setVisibility(View.VISIBLE);
            secStore.setVisibility(View.VISIBLE);
        } else { // "All" selected
            secHero.setVisibility(View.VISIBLE);
            dotsContainer.setVisibility(View.VISIBLE);
            secTrending.setVisibility(View.VISIBLE);
            secSummer.setVisibility(View.VISIBLE);
            secHealth.setVisibility(View.VISIBLE);
            secStore.setVisibility(View.VISIBLE);
        }
    }
}
