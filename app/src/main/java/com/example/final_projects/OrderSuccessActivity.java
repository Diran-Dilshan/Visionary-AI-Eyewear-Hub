package com.example.final_projects;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import java.util.Random;

public class OrderSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        double totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);
        String email = getIntent().getStringExtra("DELIVERY_EMAIL");
        String orderId = getIntent().getStringExtra("ORDER_ID"); // Get generated ID
        
        TextView orderIdText = findViewById(R.id.success_order_id);
        TextView totalText = findViewById(R.id.success_total_amount);
        TextView emailMsgText = findViewById(R.id.success_email_msg);

        // Display the passed Order ID
        if (orderId == null) orderId = "ORD-ERROR";
        orderIdText.setText(orderId);
        
        totalText.setText(String.format(Locale.US, "$%.2f", totalAmount));
        
        if (email != null && !email.isEmpty()) {
            emailMsgText.setText(getString(R.string.msg_email_sent) + email);
            // Redundant call removed - Email triggered in OrderConfirmationActivity
        }

        findViewById(R.id.btn_continue_shopping_success).setOnClickListener(v -> {
            CartManager.getInstance().clear();
            Intent intent = new Intent(this, GlassCatalogActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Removed generateOrderId() as it's now in OrderConfirmationActivity
}
