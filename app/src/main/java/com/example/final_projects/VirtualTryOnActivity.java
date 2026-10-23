package com.example.final_projects;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.slider.Slider;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;

public class VirtualTryOnActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private PreviewView cameraPreview;
    private ImageView glassesOverlay;
    private int currentColor = Color.BLACK;
    private int currentFrameResId = R.drawable.glass1;
    private int selectedPatternResId = -1; // -1 means solid color

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_try_on);

        cameraPreview = findViewById(R.id.camera_preview);
        glassesOverlay = findViewById(R.id.tryon_glasses_overlay);
        ImageButton btnBack = findViewById(R.id.btn_back_tryon);
        btnBack.setOnClickListener(v -> finish());

        if (checkPermission()) {
            startCamera();
        } else {
            requestPermission();
        }

        setupCustomization();
        
        // Initial state
        refreshGlasses();
    }

    private void setupCustomization() {
        // --- Sliders ---
        Slider scaleSlider = findViewById(R.id.slider_scale);
        if (scaleSlider != null) {
            scaleSlider.addOnChangeListener((slider, value, fromUser) -> {
                float scale = 0.5f + (value * 2.0f);
                glassesOverlay.setScaleX(scale);
                glassesOverlay.setScaleY(scale);
            });
        }

        Slider pdSlider = findViewById(R.id.slider_pd);
        if (pdSlider != null) {
            pdSlider.addOnChangeListener((slider, value, fromUser) -> {
                float translationX = (value - 0.5f) * 400f;
                glassesOverlay.setTranslationX(translationX);
            });
        }

        Slider tiltSlider = findViewById(R.id.slider_tilt);
        if (tiltSlider != null) {
            tiltSlider.addOnChangeListener((slider, value, fromUser) -> {
                float rotation = (value - 0.5f) * 60f;
                glassesOverlay.setRotation(rotation);
            });
        }

        // --- Pattern Selection ---
        findViewById(R.id.card_pattern_mosaic).setOnClickListener(v -> { 
            selectedPatternResId = R.drawable.mosaic;
            updatePatternUI(R.id.card_pattern_mosaic);
            refreshGlasses();
        });
        findViewById(R.id.card_pattern_checker).setOnClickListener(v -> { 
            selectedPatternResId = R.drawable.checkbox;
            updatePatternUI(R.id.card_pattern_checker);
            refreshGlasses();
        });
        findViewById(R.id.card_pattern_tortoise).setOnClickListener(v -> { 
            selectedPatternResId = R.drawable.tortoise;
            updatePatternUI(R.id.card_pattern_tortoise);
            refreshGlasses();
        });

        // --- Color Selection ---
        findViewById(R.id.color_matte_black).setOnClickListener(v -> { 
            currentColor = Color.BLACK; 
            selectedPatternResId = -1;
            updatePatternUI(-1);
            refreshGlasses(); 
        });
        findViewById(R.id.color_rose_gold).setOnClickListener(v -> { 
            currentColor = Color.parseColor("#B76E79"); 
            selectedPatternResId = -1;
            updatePatternUI(-1);
            refreshGlasses(); 
        });
        findViewById(R.id.color_gold_elite).setOnClickListener(v -> { 
            currentColor = Color.parseColor("#D4AF37"); 
            selectedPatternResId = -1;
            updatePatternUI(-1);
            refreshGlasses(); 
        });

        // --- Frame Type Selection ---
        findViewById(R.id.card_shape_cateye).setOnClickListener(v -> { 
            currentFrameResId = R.drawable.glass1;
            updateShapeUI(R.id.card_shape_cateye);
            refreshGlasses(); 
        });
        findViewById(R.id.card_shape_square).setOnClickListener(v -> { 
            currentFrameResId = R.drawable.square;
            updateShapeUI(R.id.card_shape_square);
            refreshGlasses(); 
        });
        findViewById(R.id.card_shape_round).setOnClickListener(v -> { 
            currentFrameResId = R.drawable.aviator;
            updateShapeUI(R.id.card_shape_round);
            refreshGlasses(); 
        });

        // --- Action Buttons ---
        findViewById(R.id.btn_add_to_cart).setOnClickListener(v -> 
            Toast.makeText(this, "Added to Cart!", Toast.LENGTH_SHORT).show()
        );
        findViewById(R.id.btn_save_design).setOnClickListener(v -> 
            Toast.makeText(this, "Design Saved!", Toast.LENGTH_SHORT).show()
        );
    }

    private void updatePatternUI(int selectedId) {
        int[] ids = {R.id.card_pattern_mosaic, R.id.card_pattern_checker, R.id.card_pattern_tortoise};
        for (int id : ids) {
            com.google.android.material.card.MaterialCardView card = findViewById(id);
            if (card != null) {
                card.setStrokeWidth(id == selectedId ? 6 : 0);
                card.setStrokeColor(ContextCompat.getColor(this, R.color.brand_medium_teal));
            }
        }
    }

    private void updateShapeUI(int selectedId) {
        int[] ids = {R.id.card_shape_cateye, R.id.card_shape_square, R.id.card_shape_round};
        for (int id : ids) {
            com.google.android.material.card.MaterialCardView card = findViewById(id);
            if (card != null) {
                card.setStrokeWidth(id == selectedId ? 6 : 0);
                card.setStrokeColor(ContextCompat.getColor(this, R.color.brand_medium_teal));
            }
        }
    }

    private void refreshGlasses() {
        if (selectedPatternResId != -1) {
            applyPattern(selectedPatternResId);
        } else {
            applyProperTint(currentColor);
        }
    }

    /**
     * Applies a solid color to the frames while ensuring the background is 100% removed.
     */
    private void applyProperTint(int color) {
        Bitmap original = BitmapFactory.decodeResource(getResources(), currentFrameResId);
        Bitmap cleaned = createCleanMask(original);
        
        Bitmap result = Bitmap.createBitmap(cleaned.getWidth(), cleaned.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        
        // Fill the canvas with the target color
        canvas.drawColor(color);
        
        // Mask it so the color only stays on the frame area
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(cleaned, 0, 0, paint);
        
        glassesOverlay.setImageBitmap(result);
        glassesOverlay.clearColorFilter();
    }

    /**
     * Applies a pattern to the frames while ensuring the background is 100% removed.
     */
    private void applyPattern(int patternResId) {
        Bitmap originalFrame = BitmapFactory.decodeResource(getResources(), currentFrameResId);
        Bitmap cleanMask = createCleanMask(originalFrame);
        Bitmap patternBitmap = BitmapFactory.decodeResource(getResources(), patternResId);

        Bitmap result = Bitmap.createBitmap(cleanMask.getWidth(), cleanMask.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // Draw the pattern
        Paint patternPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        BitmapShader shader = new BitmapShader(patternBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        patternPaint.setShader(shader);
        canvas.drawRect(0, 0, cleanMask.getWidth(), cleanMask.getHeight(), patternPaint);

        // Mask it so the pattern only stays on the frame area
        Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(cleanMask, 0, 0, maskPaint);

        glassesOverlay.setImageBitmap(result);
        glassesOverlay.clearColorFilter();
    }

    /**
     * CRITICAL: Background Removal Logic
     * Uses a ColorMatrix to turn light/checkered pixels into fully transparent pixels (Alpha 0)
     * and dark frame pixels into solid pixels (Alpha 255).
     */
    private Bitmap createCleanMask(Bitmap source) {
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        
        Paint paint = new Paint();
        // Aggressive threshold: anything lighter than dark gray becomes transparent
        float thres = -10.0f; 
        float offset = 255f * 10.0f;
        
        float[] matrix = {
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            thres, thres, thres, 1, offset
        };
        
        paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(matrix)));
        canvas.drawBitmap(source, 0, 0, paint);
        return output;
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("VirtualTryOn", "Error starting camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();
        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }
}
