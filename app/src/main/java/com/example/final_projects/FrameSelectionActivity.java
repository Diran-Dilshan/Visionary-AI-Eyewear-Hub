package com.example.final_projects;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FrameSelectionActivity extends AppCompatActivity {

    private ImageView mainPreview;
    private ImageView thumbSquare, thumbTortoise, thumbAviator;
    private View checkSquare, checkTortoise, checkAviator;
    private float currentScale = 1.0f;
    private float currentRotationY = 0f;
    private float lastTouchX;
    private int currentColor = Color.BLACK;
    private int selectedFrameType = 0; // 0: Square, 1: Tortoise, 2: Aviator
    private int currentlyShowingResId = -1;
    private boolean isAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_selection);

        // UI Initialization
        mainPreview = findViewById(R.id.main_frame_preview);
        thumbSquare = findViewById(R.id.thumb_square);
        thumbTortoise = findViewById(R.id.thumb_tortoise);
        thumbAviator = findViewById(R.id.thumb_aviator);
        
        checkSquare = findViewById(R.id.check_square);
        checkTortoise = findViewById(R.id.check_tortoise);
        checkAviator = findViewById(R.id.check_aviator);
        
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Setup 3D Depth
        mainPreview.setCameraDistance(15000 * getResources().getDisplayMetrics().density);

        // --- GRANULAR ROTATION BUTTON (Bit-by-Bit) ---
        findViewById(R.id.btn_rotate).setOnClickListener(v -> {
            if (isAnimating) return;
            // Nudge by 10 degrees per click
            float nextAngle = currentRotationY + 10f;
            if (nextAngle > 45f) nextAngle = -45f; // Cycle loop
            animateToAngle(nextAngle);
        });

        // --- GESTURE CONTROL ---
        mainPreview.setOnTouchListener((v, event) -> {
            if (isAnimating) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = event.getX();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getX() - lastTouchX;
                    float newAngle = currentRotationY + (deltaX / 6f);
                    updateUIAccordingToAngle(Math.max(-45f, Math.min(45f, newAngle)));
                    lastTouchX = event.getX();
                    return true;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    return true;
            }
            return false;
        });

        // Zoom Controls
        findViewById(R.id.btn_zoom_in).setOnClickListener(v -> {
            currentScale = Math.min(currentScale + 0.2f, 3.0f);
            mainPreview.animate().scaleX(currentScale).scaleY(currentScale).setDuration(200).start();
        });

        findViewById(R.id.btn_zoom_out).setOnClickListener(v -> {
            currentScale = Math.max(currentScale - 0.2f, 0.5f);
            mainPreview.animate().scaleX(currentScale).scaleY(currentScale).setDuration(200).start();
        });

        // Frame Type Selection
        setupFrameTypeListeners();

        // Check if a specific frame type was passed from catalog
        int initialType = getIntent().getIntExtra("SELECTED_FRAME_TYPE", 0);
        selectedFrameType = initialType;
        MaterialCardView f1 = findViewById(R.id.frame_square), f2 = findViewById(R.id.frame_tortoise), f3 = findViewById(R.id.frame_aviator);
        if (selectedFrameType == 1) updateFrameTypeUI(f2, f1, f3);
        else if (selectedFrameType == 2) updateFrameTypeUI(f3, f1, f2);
        else updateFrameTypeUI(f1, f2, f3);

        // Color Selection
        setupColorListeners();
        
        // Initial Refresh
        updateUIAccordingToAngle(0);
        setupMiscUI();
    }

    private void animateToAngle(float target) {
        isAnimating = true;
        ValueAnimator animator = ValueAnimator.ofFloat(currentRotationY, target);
        animator.setDuration(250);
        animator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            updateUIAccordingToAngle((float) animation.getAnimatedValue());
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isAnimating = false;
            }
        });
        animator.start();
    }

    private void updateUIAccordingToAngle(float angle) {
        currentRotationY = angle;
        mainPreview.setRotationY(angle);

        // Determine which asset to show based on angle
        int resId;
        if (angle > 15) {
            resId = R.drawable.glass2; // Right view
        } else if (angle < -15) {
            resId = R.drawable.glass3; // Left view
        } else {
            // Front view specific to model
            if (selectedFrameType == 1) resId = R.drawable.tortoiseshell;
            else if (selectedFrameType == 2) resId = R.drawable.aviator;
            else resId = R.drawable.square;
        }

        if (resId != currentlyShowingResId) {
            mainPreview.setImageResource(resId);
            currentlyShowingResId = resId;
        }
        
        // Re-apply the perfect tint to the active image
        applyPerfectTint(mainPreview, currentColor, true);
        
        // Ensure thumbnails stay tinted correctly
        applyPerfectTint(thumbSquare, selectedFrameType == 0 ? currentColor : Color.BLACK, false);
        applyPerfectTint(thumbTortoise, selectedFrameType == 1 ? currentColor : Color.BLACK, false);
        applyPerfectTint(thumbAviator, selectedFrameType == 2 ? currentColor : Color.BLACK, false);
    }

    /**
     * MATHEMATICALLY CORRECT TINTING:
     * 1. Grayscales the image to remove original asset colors.
     * 2. Uses contrast/brightness to make frames black and background white.
     * 3. Tints black areas to the target color while keeping white areas white.
     */
    private void applyPerfectTint(ImageView imageView, int color, boolean isMain) {
        if (imageView == null) return;

        ColorMatrix cm = new ColorMatrix();
        
        // Step 1: Grayscale (important since assets like 'aviator' or 'square' might be pre-colored)
        cm.setSaturation(0);

        // Step 2: High Contrast to isolate the frame (Black) from the background (White)
        // This helps 'wash out' checkered artifacts into pure white.
        float contrast = 2.0f;
        float translate = -120f; // Threshold to push dark frames towards zero

        float[] contrastMatrix = {
            contrast, 0, 0, 0, translate,
            0, contrast, 0, 0, translate,
            0, 0, contrast, 0, translate,
            0, 0, 0, 1, 0
        };
        cm.postConcat(new ColorMatrix(contrastMatrix));

        // Step 3: Tinting
        // We map Black (0) to the Target Color and White (255) to White.
        // Formula: Output = Input * (255 - TargetColor)/255 + TargetColor
        float r = Color.red(color);
        float g = Color.green(color);
        float b = Color.blue(color);

        float[] tintMatrix = {
            (255 - r) / 255f, 0, 0, 0, r,
            0, (255 - g) / 255f, 0, 0, g,
            0, 0, (255 - b) / 255f, 0, b,
            0, 0, 0, 1, 0
        };
        cm.postConcat(new ColorMatrix(tintMatrix));

        imageView.setColorFilter(new ColorMatrixColorFilter(cm));
    }

    private void setupFrameTypeListeners() {
        MaterialCardView f1 = findViewById(R.id.frame_square), f2 = findViewById(R.id.frame_tortoise), f3 = findViewById(R.id.frame_aviator);
        f1.setOnClickListener(v -> { selectedFrameType = 0; updateFrameTypeUI(f1, f2, f3); updateUIAccordingToAngle(currentRotationY); });
        f2.setOnClickListener(v -> { selectedFrameType = 1; updateFrameTypeUI(f2, f1, f3); updateUIAccordingToAngle(currentRotationY); });
        f3.setOnClickListener(v -> { selectedFrameType = 2; updateFrameTypeUI(f3, f1, f2); updateUIAccordingToAngle(currentRotationY); });
    }

    private void setupColorListeners() {
        MaterialCardView[] cards = { findViewById(R.id.color_black), findViewById(R.id.color_tortoise), findViewById(R.id.color_gray), findViewById(R.id.color_gold), findViewById(R.id.color_silver) };
        int[] colors = { Color.BLACK, Color.parseColor("#8B4513"), Color.parseColor("#9E9E9E"), Color.parseColor("#FFD700"), Color.parseColor("#E0E0E0") };
        int activeColor = ContextCompat.getColor(this, R.color.brand_medium_teal);

        for (int i = 0; i < cards.length; i++) {
            final int index = i;
            cards[i].setOnClickListener(v -> {
                for (MaterialCardView c : cards) c.setStrokeWidth(0);
                cards[index].setStrokeWidth(4);
                cards[index].setStrokeColor(activeColor);
                currentColor = colors[index];
                updateUIAccordingToAngle(currentRotationY);
            });
        }
    }

    private void updateFrameTypeUI(MaterialCardView sel, MaterialCardView... others) {
        sel.setStrokeWidth(4);
        sel.setStrokeColor(ContextCompat.getColor(this, R.color.brand_medium_teal));
        for (MaterialCardView o : others) o.setStrokeWidth(0);
        
        // Update checkmark visibility
        if (checkSquare != null) checkSquare.setVisibility(selectedFrameType == 0 ? View.VISIBLE : View.GONE);
        if (checkTortoise != null) checkTortoise.setVisibility(selectedFrameType == 1 ? View.VISIBLE : View.GONE);
        if (checkAviator != null) checkAviator.setVisibility(selectedFrameType == 2 ? View.VISIBLE : View.GONE);
    }

    private void setupMiscUI() {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_shop);
        nav.setOnItemSelectedListener(item -> { if (item.getItemId() == R.id.nav_home) finish(); return true; });

        Spinner s = findViewById(R.id.spinner_material);
        String[] m = {"Acetate", "Titanium", "Metal", "Polycarbonate"};
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, m);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);
    }
}
