package com.example.final_projects;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_projects.network.AuthResponse;
import com.example.final_projects.network.SupabaseManager;
import com.example.final_projects.network.UserRequest;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager.init(this);
        setContentView(R.layout.activity_signup);

        TextInputEditText nameInput = findViewById(R.id.name_edit_text);
        TextInputEditText emailInput = findViewById(R.id.email_edit_text);
        TextInputEditText passwordInput = findViewById(R.id.password_edit_text);
        TextInputEditText confirmPasswordInput = findViewById(R.id.confirm_password_edit_text);
        Button signupButton = findViewById(R.id.signup_button);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();
                
                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(SignupActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                signupButton.setEnabled(false);
                performSignup(name, email, password, signupButton);
            }
        });

        TextView loginLink = findViewById(R.id.login_link_text);
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performSignup(String name, String email, String password, Button signupButton) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("full_name", name);

        UserRequest request = new UserRequest(email, password, metadata);
        
        SupabaseManager.getInstance().getAuthService().signUp(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                signupButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    String userId = response.body().getUser().getId();
                    SessionManager.getInstance().setUserId(userId);

                    // --- NEW: Save user to public.users table for Admin Dashboard ---
                    java.util.Map<String, Object> userRecord = new java.util.HashMap<>();
                    userRecord.put("id", userId);
                    userRecord.put("email", email);
                    userRecord.put("full_name", name);

                    SupabaseManager.getInstance().getAuthService().insertUserRecord(userRecord).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                android.util.Log.d("UserDB", "User record saved successfully.");
                            }
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {}
                    });
                    // ---------------------------------------------------------------

                    Toast.makeText(SignupActivity.this, "Signup Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, LandingActivity.class);
                    startActivity(intent);
                    finish();
                } else if (response.code() == 422) {
                    Toast.makeText(SignupActivity.this, "An account with this email already exists. Please login.", Toast.LENGTH_LONG).show();
                } else if (response.code() == 429) {
                    Toast.makeText(SignupActivity.this, "Too many requests. Please wait a moment.", Toast.LENGTH_LONG).show();
                } else if (response.code() == 500) {
                    Toast.makeText(SignupActivity.this, "Supabase Database Error (500). Please check SQL triggers or disable Email Confirmation in Supabase.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SignupActivity.this, "Signup Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                signupButton.setEnabled(true);
                Toast.makeText(SignupActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
