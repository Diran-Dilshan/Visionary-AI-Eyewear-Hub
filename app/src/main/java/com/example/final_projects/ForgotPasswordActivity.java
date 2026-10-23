package com.example.final_projects;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_projects.network.RecoverRequest;
import com.example.final_projects.network.SupabaseManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        TextInputEditText emailInput = findViewById(R.id.email_edit_text);
        Button resetButton = findViewById(R.id.reset_button);
        TextView backToLogin = findViewById(R.id.back_to_login_text);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendResetLink(email, resetButton);
            }
        });

        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void sendResetLink(String email, Button resetButton) {
        resetButton.setEnabled(false);
        RecoverRequest request = new RecoverRequest(email);
        
        SupabaseManager.getInstance().getAuthService().recoverPassword(request)
                .enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                resetButton.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Reset link sent to your email!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                resetButton.setEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
