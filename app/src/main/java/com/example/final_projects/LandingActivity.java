package com.example.final_projects;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_projects.network.AuthResponse;
import com.example.final_projects.network.SupabaseManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandingActivity extends AppCompatActivity {

    private boolean isMenuOpen = false;
    private View fabBackdrop;
    private View layoutAi, layoutPost, layoutFeedback;
    private FloatingActionButton fabMain;
    private ScrollView mainScrollView;
    private TextView userNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager.init(this);
        setContentView(R.layout.activity_landing);

        // UI References
        mainScrollView = findViewById(R.id.main_scroll_view);
        userNameText = findViewById(R.id.user_name_text);
        View header = findViewById(R.id.avatar_card);
        View greeting = findViewById(R.id.greeting_layout);
        View search = findViewById(R.id.search_container);
        ImageButton btnLogout = findViewById(R.id.btn_logout);


        // Fetch User Data if available
        String userId = getIntent().getStringExtra("USER_ID");
        if (userId != null) {
            SessionManager.getInstance().setUserId(userId);
            fetchUserProfile(userId);
        } else {
            userId = SessionManager.getInstance().getUserId();
            if (userId != null) fetchUserProfile(userId);
        }

        MaterialCardView card1 = findViewById(R.id.card_select_frame);
        MaterialCardView card2 = findViewById(R.id.card_upload_report);
        MaterialCardView card3 = findViewById(R.id.card_virtual_tryon);
        MaterialCardView card4 = findViewById(R.id.card_my_orders);
        MaterialCardView card5 = findViewById(R.id.card_find_store);
        View scanningLine = findViewById(R.id.scanning_line);

        // FAB Menu References
        fabMain = findViewById(R.id.fab_main);
        fabBackdrop = findViewById(R.id.fab_backdrop);
        layoutAi = findViewById(R.id.layout_ai);
        layoutPost = findViewById(R.id.layout_post);
        layoutFeedback = findViewById(R.id.layout_feedback);

        FloatingActionButton fabAi = findViewById(R.id.fab_ai);
        FloatingActionButton fabPost = findViewById(R.id.fab_post);
        FloatingActionButton fabFeedback = findViewById(R.id.fab_feedback);

        // Initial FAB state
        layoutAi.setAlpha(0f);
        layoutPost.setAlpha(0f);
        layoutFeedback.setAlpha(0f);
        fabBackdrop.setAlpha(0f);

        // FAB Logic
        fabMain.setOnClickListener(v -> toggleMenu());
        fabBackdrop.setOnClickListener(v -> toggleMenu());

        View.OnClickListener menuOptionListener = v -> {
            if (v.getId() == R.id.fab_ai || v.getId() == R.id.layout_ai) {
                toggleMenu();
                startActivity(new Intent(LandingActivity.this, AiAssistantActivity.class));
            } else if (v.getId() == R.id.fab_post) {
                Toast.makeText(this, "Post feature coming soon!", Toast.LENGTH_SHORT).show();
                toggleMenu();
            } else if (v.getId() == R.id.fab_feedback) {
                Toast.makeText(this, "Feedback submitted! Thank you.", Toast.LENGTH_SHORT).show();
                toggleMenu();
            }
        };

        fabAi.setOnClickListener(menuOptionListener);
        layoutAi.setOnClickListener(menuOptionListener);
        fabPost.setOnClickListener(menuOptionListener);
        fabFeedback.setOnClickListener(menuOptionListener);

        // Group views for animation
        View[] staggeredViews = {header, greeting, search, card1, card2, card3, card4, card5};
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.card_slide_up);
        for (View v : staggeredViews) {
            if (v != null) v.startAnimation(slideUp);
        }

        // Setup Touch Feedback & Click Listeners
        View[] interactiveViews = {header, btnLogout, fabMain, card1, card2, card3, card4, card5};
        for (View view : interactiveViews) {
            if (view != null) {
                setupTouchFeedback(view);
            }
        }

        MaterialCardView[] cards = {card1, card2, card3, card4, card5};
        for (MaterialCardView card : cards) {
            if (card != null) {
                card.setOnClickListener(v -> {
                    if (v.getId() == R.id.card_select_frame) {
                        Intent intent = new Intent(LandingActivity.this, GlassCatalogActivity.class);
                        startActivity(intent);
                    } else if (v.getId() == R.id.card_virtual_tryon) {
                        Intent intent = new Intent(LandingActivity.this, VirtualTryOnActivity.class);
                        startActivity(intent);
                    } else if (v.getId() == R.id.card_my_orders) {
                        Intent intent = new Intent(LandingActivity.this, OrderTrackingActivity.class);
                        startActivity(intent);
                    } else if (v.getId() == R.id.card_find_store) {
                        Intent intent = new Intent(LandingActivity.this, VisionCareActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Feature coming soon!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // Logout functionality
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> finish());
        }

        // Promo card opens Offers page
        View promoCard = findViewById(R.id.promo_text_container);
        if (promoCard != null && promoCard.getParent() != null && promoCard.getParent().getParent() instanceof View) {
            ((View) promoCard.getParent().getParent()).setOnClickListener(v -> 
                startActivity(new Intent(LandingActivity.this, OffersActivity.class))
            );
        }

        // Virtual Try-On Scanning Animation
        if (scanningLine != null) {
            startScanningAnimation(scanningLine);
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        View navHome = findViewById(R.id.nav_home);
        View navOffers = findViewById(R.id.nav_offers);
        View navOrders = findViewById(R.id.nav_orders);
        View navProfile = findViewById(R.id.nav_profile);

        View[] tabs = {navHome, navOffers, navOrders, navProfile};

        for (View tab : tabs) {
            if (tab != null) {
                setupTouchFeedback(tab);
            }
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> selectTab(navHome, "home"));
        }

        if (navOffers != null) {
            navOffers.setOnClickListener(v -> {
                selectTab(navOffers, "offers");
                startActivity(new Intent(LandingActivity.this, OffersActivity.class));
            });
        }

        if (navOrders != null) {
            navOrders.setOnClickListener(v -> {
                selectTab(navOrders, "orders");
                startActivity(new Intent(LandingActivity.this, OrderTrackingActivity.class));
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                selectTab(navProfile, "profile");
                String name = userNameText != null ? userNameText.getText().toString() : "User Profile";
                Toast.makeText(LandingActivity.this, "Profile: " + name, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void selectTab(View selectedTab, String tabName) {
        int[] tabIds = {R.id.nav_home, R.id.nav_offers, R.id.nav_orders, R.id.nav_profile};
        int[] iconIds = {R.id.icon_nav_home, R.id.icon_nav_offers, R.id.icon_nav_orders, R.id.icon_nav_profile};
        int[] textIds = {R.id.text_nav_home, R.id.text_nav_offers, R.id.text_nav_orders, R.id.text_nav_profile};

        for (int i = 0; i < tabIds.length; i++) {
            View tab = findViewById(tabIds[i]);
            android.widget.ImageView icon = findViewById(iconIds[i]);
            TextView text = findViewById(textIds[i]);

            if (tab != null) {
                if (tabIds[i] == selectedTab.getId()) {
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

    private void fetchUserProfile(String userId) {
        String filter = "eq." + userId;
        SupabaseManager.getInstance().getAuthService().getUserData(filter, "full_name").enqueue(new Callback<List<AuthResponse.User>>() {
            @Override
            public void onResponse(Call<List<AuthResponse.User>> call, Response<List<AuthResponse.User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String name = response.body().get(0).getFullName();
                    if (name != null) userNameText.setText(name);
                }
            }

            @Override
            public void onFailure(Call<List<AuthResponse.User>> call, Throwable t) {
                // Ignore failure
            }
        });
    }

    private void toggleMenu() {
        if (!isMenuOpen) {
            isMenuOpen = true;
            fabBackdrop.setVisibility(View.VISIBLE);
            layoutAi.setVisibility(View.VISIBLE);
            layoutPost.setVisibility(View.VISIBLE);
            layoutFeedback.setVisibility(View.VISIBLE);

            fabMain.animate().rotation(45f).setDuration(300).start();
            fabBackdrop.animate().alpha(1f).setDuration(300).start();
            layoutAi.animate().translationY(-20f).alpha(1f).setDuration(300).start();
            layoutPost.animate().translationY(-20f).alpha(1f).setDuration(300).start();
            layoutFeedback.animate().translationY(-20f).alpha(1f).setDuration(300).start();
        } else {
            isMenuOpen = false;
            fabMain.animate().rotation(0f).setDuration(300).start();
            fabBackdrop.animate().alpha(0f).setDuration(300).withEndAction(() -> fabBackdrop.setVisibility(View.GONE)).start();
            layoutAi.animate().translationY(0f).alpha(0f).setDuration(300).withEndAction(() -> layoutAi.setVisibility(View.GONE)).start();
            layoutPost.animate().translationY(0f).alpha(0f).setDuration(300).withEndAction(() -> layoutPost.setVisibility(View.GONE)).start();
            layoutFeedback.animate().translationY(0f).alpha(0f).setDuration(300).withEndAction(() -> layoutFeedback.setVisibility(View.GONE)).start();
        }
    }

    private void setupTouchFeedback(View view) {
        Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(scaleDown);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.startAnimation(scaleUp);
            }
            return false; // Allow standard click handling
        });
    }

    private void startScanningAnimation(View view) {
        view.post(() -> {
            view.setVisibility(View.VISIBLE);
            float containerHeight = 100;
            if (view.getParent() instanceof View) {
                containerHeight = ((View)view.getParent()).getHeight();
            }
            ObjectAnimator scanAnim = ObjectAnimator.ofFloat(view, "translationY", 0f, containerHeight - view.getHeight());
            scanAnim.setDuration(2000);
            scanAnim.setRepeatCount(ValueAnimator.INFINITE);
            scanAnim.setRepeatMode(ValueAnimator.REVERSE);
            scanAnim.start();
        });
    }
}
