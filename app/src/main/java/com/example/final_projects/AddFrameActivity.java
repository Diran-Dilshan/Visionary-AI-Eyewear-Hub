package com.example.final_projects;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_projects.network.FrameRequest;
import com.example.final_projects.network.SupabaseManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFrameActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private TextView tvUploadHint;
    private String encodedImage = "";
    private ActivityResultLauncher<String> imagePickerLauncher;
    
    private FrameRequest existingFrame;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_frame);

        findViewById(R.id.btn_back_add_frame).setOnClickListener(v -> finish());

        TextInputEditText nameEdit = findViewById(R.id.edit_frame_name);
        TextInputEditText descEdit = findViewById(R.id.edit_frame_desc);
        TextInputEditText priceEdit = findViewById(R.id.edit_frame_price);
        TextInputEditText stockEdit = findViewById(R.id.edit_frame_stock);
        TextInputEditText ratingEdit = findViewById(R.id.edit_frame_rating);
        TextInputEditText reviewCountEdit = findViewById(R.id.edit_frame_review_count);
        imgPreview = findViewById(R.id.img_frame_preview);
        tvUploadHint = findViewById(R.id.tv_upload_hint);
        MaterialButton btnSave = findViewById(R.id.btn_save_frame);

        // Check if we are editing
        existingFrame = (FrameRequest) getIntent().getSerializableExtra("EDIT_FRAME");
        if (existingFrame != null) {
            isEditMode = true;
            TextView toolbarTitle = findViewById(R.id.tv_add_frame_title);
            if (toolbarTitle != null) toolbarTitle.setText("Edit Frame");
            btnSave.setText("Update Frame");
            
            nameEdit.setText(existingFrame.getName());
            descEdit.setText(existingFrame.getDescription());
            priceEdit.setText(String.valueOf(existingFrame.getPrice()));
            stockEdit.setText(String.valueOf(existingFrame.getStockCount()));
            ratingEdit.setText(String.valueOf(existingFrame.getRating()));
            reviewCountEdit.setText(String.valueOf(existingFrame.getReviewCount()));
            
            encodedImage = existingFrame.getImageUrl();
            loadImagePreview(encodedImage);
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleImageSelection(uri);
                    }
                }
        );

        View.OnClickListener openPickerListener = v -> {
            // Hide keyboard first if open
            View currentFocus = getCurrentFocus();
            if (currentFocus != null) {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                }
            }
            try {
                imagePickerLauncher.launch("image/*");
            } catch (Exception e) {
                // Fallback for older intent handling
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1001);
            }
        };

        findViewById(R.id.card_image_picker).setOnClickListener(openPickerListener);
        imgPreview.setOnClickListener(openPickerListener);
        tvUploadHint.setOnClickListener(openPickerListener);

        btnSave.setOnClickListener(v -> {
            String name = nameEdit.getText().toString().trim();
            String desc = descEdit.getText().toString().trim();
            String priceStr = priceEdit.getText().toString().trim();
            String stockStr = stockEdit.getText().toString().trim();
            String ratingStr = ratingEdit.getText().toString().trim();
            String reviewCountStr = reviewCountEdit.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(this, "Name, Price and Stock are required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                double rating = ratingStr.isEmpty() ? 0.0 : Double.parseDouble(ratingStr);
                int reviewCount = reviewCountStr.isEmpty() ? 0 : Integer.parseInt(reviewCountStr);
                
                if (isEditMode) {
                    performUpdate(existingFrame.getId(), name, desc, price, stock, rating, reviewCount, btnSave);
                } else {
                    if (encodedImage.isEmpty()) {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    performInsert(name, desc, price, stock, rating, reviewCount, btnSave);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid numeric input", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performInsert(String name, String desc, double price, int stock, double rating, int reviewCount, View btn) {
        FrameRequest request = new FrameRequest(name, desc, price, "In Stock", rating, reviewCount, encodedImage, stock);
        btn.setEnabled(false);
        SupabaseManager.getInstance().getAuthService().insertFrame(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                btn.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(AddFrameActivity.this, "Frame added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                btn.setEnabled(true);
            }
        });
    }

    private void performUpdate(int id, String name, String desc, double price, int stock, double rating, int reviewCount, View btn) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("description", desc);
        updates.put("price", price);
        updates.put("stock_count", stock);
        updates.put("rating", rating);
        updates.put("review_count", reviewCount);
        updates.put("image_url", encodedImage);

        btn.setEnabled(false);
        String filter = "eq." + id;
        SupabaseManager.getInstance().getAuthService().updateFrame(filter, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                btn.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(AddFrameActivity.this, "Frame updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                btn.setEnabled(true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            handleImageSelection(data.getData());
        }
    }

    private void handleImageSelection(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) {
                imgPreview.setImageBitmap(bitmap);
                imgPreview.setPadding(0, 0, 0, 0);
                tvUploadHint.setVisibility(View.GONE);
                encodedImage = encodeImageToBase64(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadImagePreview(String source) {
        if (source == null || source.isEmpty()) return;
        if (source.length() > 100) {
            byte[] decoded = Base64.decode(source, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            imgPreview.setImageBitmap(bitmap);
            imgPreview.setPadding(0, 0, 0, 0);
            tvUploadHint.setVisibility(View.GONE);
        } else {
            int resId = getResources().getIdentifier(source, "drawable", getPackageName());
            if (resId != 0) {
                imgPreview.setImageResource(resId);
                imgPreview.setPadding(0, 0, 0, 0);
                tvUploadHint.setVisibility(View.GONE);
            }
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        int maxWidth = 350;
        int maxHeight = (int) (bitmap.getHeight() * ((float) maxWidth / bitmap.getWidth()));
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 55, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }
}
