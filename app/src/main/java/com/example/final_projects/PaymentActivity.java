package com.example.final_projects;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;

public class PaymentActivity extends AppCompatActivity {

    private TextInputEditText editCardName, editCardNumber, editExpiry, editCvv;
    private View processingOverlay;
    private double totalAmount;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);
        email = getIntent().getStringExtra("DELIVERY_EMAIL");
        
        TextView amountText = findViewById(R.id.payment_total_amount);
        amountText.setText(String.format(Locale.US, "$%.2f", totalAmount));

        editCardName = findViewById(R.id.edit_card_name);
        editCardNumber = findViewById(R.id.edit_card_number);
        editExpiry = findViewById(R.id.edit_expiry);
        editCvv = findViewById(R.id.edit_cvv);
        processingOverlay = findViewById(R.id.processing_overlay);

        ImageButton btnBack = findViewById(R.id.btn_back_payment);
        btnBack.setOnClickListener(v -> finish());

        setupInputFormatters();

        Button btnComplete = findViewById(R.id.btn_complete_payment);
        btnComplete.setOnClickListener(v -> validateAndPay());
    }

    private void setupInputFormatters() {
        // Card Number Formatter (XXXX XXXX XXXX XXXX)
        editCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isDeleting = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { isDeleting = count > after; }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (isDeleting) return;
                String text = s.toString().replace(" ", "");
                if (text.length() > 0 && text.length() % 4 == 0 && text.length() < 16) {
                    s.append(" ");
                }
            }
        });

        // Expiry Formatter (MM/YY)
        editExpiry.addTextChangedListener(new TextWatcher() {
            private boolean isDeleting = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { isDeleting = count > after; }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (isDeleting) return;
                if (s.length() == 2) s.append("/");
            }
        });
    }

    private void validateAndPay() {
        boolean isValid = true;

        if (editCardName.getText() == null || editCardName.getText().toString().trim().isEmpty()) {
            shakeView((View) editCardName.getParent().getParent());
            isValid = false;
        }

        String cardNum = editCardNumber.getText().toString().replace(" ", "");
        if (cardNum.length() < 16) {
            shakeView((View) editCardNumber.getParent().getParent());
            isValid = false;
        }

        if (editExpiry.getText().toString().length() < 5) {
            shakeView((View) editExpiry.getParent().getParent());
            isValid = false;
        }

        if (editCvv.getText().toString().length() < 3) {
            shakeView((View) editCvv.getParent().getParent());
            isValid = false;
        }

        if (isValid) {
            processPayment();
        }
    }

    private void shakeView(View view) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        view.startAnimation(shake);
        if (view instanceof TextInputLayout) {
            ((TextInputLayout) view).setError("Required");
        }
    }

    private void processPayment() {
        processingOverlay.setVisibility(View.VISIBLE);
        
        // Mocking payment gateway processing (2.5 seconds delay)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            processingOverlay.setVisibility(View.GONE);
            navigateToSuccess();
        }, 2500);
    }

    private void navigateToSuccess() {
        Intent intent = new Intent(this, OrderSuccessActivity.class);
        intent.putExtra("TOTAL_AMOUNT", totalAmount);
        intent.putExtra("DELIVERY_EMAIL", email);
        startActivity(intent);
        finish();
    }
}
