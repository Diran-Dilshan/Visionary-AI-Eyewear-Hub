package com.example.final_projects;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final_projects.network.AuthResponse;
import com.example.final_projects.network.SupabaseManager;
import com.example.final_projects.network.UpdateUserRequest;
import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        handleIntent(getIntent());

        TextInputEditText newPasswordInput = findViewById(R.id.new_password_edit_text);
        TextInputEditText confirmPasswordInput = findViewById(R.id.confirm_password_edit_text);
        Button updateButton = findViewById(R.id.update_button);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = newPasswordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                if (password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(ResetPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (accessToken == null) {
                    Toast.makeText(ResetPasswordActivity.this, "Invalid or missing session token", Toast.LENGTH_LONG).show();
                    return;
                }

                updatePassword(password, updateButton);
            }
        });
    }

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            String fragment = data.getFragment(); // access_token=...&type=recovery
            if (fragment != null) {
                Pattern pattern = Pattern.compile("access_token=([^&]+)");
                Matcher matcher = pattern.matcher(fragment);
                if (matcher.find()) {
                    accessToken = matcher.group(1);
                }
            }
        }
    }

    private void updatePassword(String newPassword, Button updateButton) {
        updateButton.setEnabled(false);
        UpdateUserRequest request = new UpdateUserRequest(newPassword);
        String authHeader = "Bearer " + accessToken;

        SupabaseManager.getInstance().getAuthService().updateUser(authHeader, request)
                .enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                updateButton.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ResetPasswordActivity.this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Failed to update password: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                updateButton.setEnabled(true);
                Toast.makeText(ResetPasswordActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
