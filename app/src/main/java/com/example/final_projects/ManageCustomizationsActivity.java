package com.example.final_projects;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_projects.network.CustomizationRequest;
import com.example.final_projects.network.SupabaseManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageCustomizationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View progressBar;
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_list);

        TextView title = findViewById(R.id.tv_manage_title);
        title.setText("Customizations");

        findViewById(R.id.btn_back_manage).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_manage_list);
        progressBar = findViewById(R.id.progress_manage);
        FloatingActionButton fab = findViewById(R.id.fab_add_new);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddCustomizationActivity.class)));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomAdapter(new ArrayList<>(), new CustomAdapter.OnCustActionListener() {
            @Override
            public void onEdit(CustomizationRequest item) {
                showEditValueDialog(item);
            }

            @Override
            public void onDelete(CustomizationRequest item) {
                confirmDeleteCust(item);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void showEditValueDialog(CustomizationRequest item) {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(item.getValue());
        new AlertDialog.Builder(this)
                .setTitle("Update Value: " + item.getName())
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newValue = input.getText().toString().trim();
                    if (!newValue.isEmpty()) updateCustValue(item, newValue);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateCustValue(CustomizationRequest item, String newValue) {
        progressBar.setVisibility(View.VISIBLE);
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("value", newValue);
        
        String filter = "eq." + item.getId();
        SupabaseManager.getInstance().getAuthService().updateCustomization(filter, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ManageCustomizationsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    fetchCustomizations();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void confirmDeleteCust(CustomizationRequest item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Customization")
                .setMessage("Are you sure you want to delete " + item.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCust(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCust(CustomizationRequest item) {
        progressBar.setVisibility(View.VISIBLE);
        String filter = "eq." + item.getId();
        SupabaseManager.getInstance().getAuthService().deleteCustomization(filter).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ManageCustomizationsActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    fetchCustomizations();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCustomizations();
    }

    private void fetchCustomizations() {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseManager.getInstance().getAuthService().getAllCustomizations("*").enqueue(new Callback<List<CustomizationRequest>>() {
            @Override
            public void onResponse(Call<List<CustomizationRequest>> call, Response<List<CustomizationRequest>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Toast.makeText(ManageCustomizationsActivity.this, "Failed to load", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CustomizationRequest>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageCustomizationsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private List<CustomizationRequest> items;
        private final OnCustActionListener listener;

        interface OnCustActionListener {
            void onEdit(CustomizationRequest item);
            void onDelete(CustomizationRequest item);
        }

        CustomAdapter(List<CustomizationRequest> items, OnCustActionListener listener) {
            this.items = items;
            this.listener = listener;
        }

        void setItems(List<CustomizationRequest> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_record, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CustomizationRequest item = items.get(position);
            holder.tvTitle.setText(item.getName());
            holder.tvSubtitle.setText("Type: " + item.getType());
            holder.tvDetails.setText("Value: " + item.getValue());

            holder.imgPreview.setVisibility(View.GONE); // No image for customizations

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSubtitle, tvDetails;
            ImageView imgPreview;
            ImageButton btnEdit, btnDelete;
            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_record_title);
                tvSubtitle = itemView.findViewById(R.id.tv_record_subtitle);
                tvDetails = itemView.findViewById(R.id.tv_record_details);
                imgPreview = itemView.findViewById(R.id.img_record_preview);
                btnEdit = itemView.findViewById(R.id.btn_record_edit);
                btnDelete = itemView.findViewById(R.id.btn_record_delete);
            }
        }
    }
}
