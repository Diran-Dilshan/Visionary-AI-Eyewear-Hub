package com.example.final_projects;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        double totalAmount = CartManager.getInstance().getTotalAmount();
        int itemsCount = CartManager.getInstance().getItemsCount();

        TextView itemsText = findViewById(R.id.checkout_items_count);
        TextView totalText = findViewById(R.id.checkout_total_amount);
        TextInputEditText nameEdit = findViewById(R.id.checkout_name);
        TextInputEditText emailEdit = findViewById(R.id.checkout_email);
        TextInputEditText phoneEdit = findViewById(R.id.checkout_phone);
        TextInputEditText addressEdit = findViewById(R.id.checkout_address);

        itemsText.setText(String.format(Locale.US, "Items (%d)", itemsCount));
        totalText.setText(String.format(Locale.US, "Total: $%.2f", totalAmount));

        // Close button
        findViewById(R.id.btn_close_checkout).setOnClickListener(v -> finish());
        
        // Cancel button
        findViewById(R.id.btn_cancel_checkout).setOnClickListener(v -> finish());

        // Proceed button
        findViewById(R.id.btn_proceed_confirm).setOnClickListener(v -> {
            if (validateForm(nameEdit, emailEdit, phoneEdit, addressEdit)) {
                Intent intent = new Intent(this, OrderConfirmationActivity.class);
                intent.putExtra("DELIVERY_NAME", nameEdit.getText().toString().trim());
                intent.putExtra("DELIVERY_EMAIL", emailEdit.getText().toString().trim());
                intent.putExtra("DELIVERY_PHONE", phoneEdit.getText().toString().trim());
                intent.putExtra("DELIVERY_ADDRESS", addressEdit.getText().toString().trim());
                startActivity(intent);
            }
        });
    }

    private boolean validateForm(TextInputEditText name, TextInputEditText email, TextInputEditText phone, TextInputEditText address) {
        if (name.getText() == null || name.getText().toString().trim().isEmpty()) {
            name.setError("Full name is required");
            return false;
        }
        
        String emailStr = email.getText() != null ? email.getText().toString().trim() : "";
        if (emailStr.isEmpty()) {
            email.setError("Email address is required");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            email.setError("Please enter a valid email address");
            return false;
        }

        if (phone.getText() == null || phone.getText().toString().trim().isEmpty()) {
            phone.setError("Phone number is required");
            return false;
        }
        if (address.getText() == null || address.getText().toString().trim().isEmpty()) {
            address.setError("Delivery address is required");
            return false;
        }
        return true;
    }
}
