package com.example.final_projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_projects.network.AuthResponse;
import com.example.final_projects.network.SupabaseManager;
import com.example.final_projects.network.UserRequest;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize SessionManager
        SessionManager.init(this);
        // Force light mode to fix potential black screen rendering issues
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        TextInputEditText emailInput = findViewById(R.id.email_edit_text);
        TextInputEditText passwordInput = findViewById(R.id.password_edit_text);
        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check for Admin Credentials
                if (email.equals("admin@gmail.com") && password.equals("Diran@0513")) {
                    Toast.makeText(MainActivity.this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                
                loginButton.setEnabled(false);
                performLogin(email, password, loginButton);
            }
        });

        TextView forgotPassword = findViewById(R.id.forgot_password_text);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        TextView createAccount = findViewById(R.id.create_account_text);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin(String email, String password, Button loginButton) {
        UserRequest request = new UserRequest(email, password);
        SupabaseManager.getInstance().getAuthService().login(
                "password",
                request
        ).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                loginButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    
                    String userId = response.body().getUser().getId();
                    SessionManager.getInstance().setUserId(userId);

                    Intent intent = new Intent(MainActivity.this, LandingActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                    finish();
                } else if (response.code() == 400) {
                    Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Login Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loginButton.setEnabled(true);
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
