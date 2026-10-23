package com.example.final_projects;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.final_projects.network.CustomizationRequest;
import com.example.final_projects.network.SupabaseManager;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCustomizationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customization);

        findViewById(R.id.btn_back_add_cust).setOnClickListener(v -> finish());

        Spinner typeSpinner = findViewById(R.id.spinner_cust_type);
        String[] types = {"Pattern", "Shape", "Color"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        TextInputEditText nameEdit = findViewById(R.id.edit_cust_name);
        TextInputEditText valueEdit = findViewById(R.id.edit_cust_value);
        Button btnSave = findViewById(R.id.btn_save_cust);

        btnSave.setOnClickListener(v -> {
            String type = typeSpinner.getSelectedItem().toString();
            String name = nameEdit.getText().toString().trim();
            String value = valueEdit.getText().toString().trim();

            if (name.isEmpty() || value.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            CustomizationRequest request = new CustomizationRequest(type, name, value);

            btnSave.setEnabled(false);
            SupabaseManager.getInstance().getAuthService().insertCustomization(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    btnSave.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(AddCustomizationActivity.this, "Customization Added!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddCustomizationActivity.this, "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    btnSave.setEnabled(true);
                    Toast.makeText(AddCustomizationActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
