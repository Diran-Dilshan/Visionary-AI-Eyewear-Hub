package com.example.final_projects;

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

import com.example.final_projects.network.AuthResponse;
import com.example.final_projects.network.SupabaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageUsersActivity extends AppCompatActivity {

    private View progressBar;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_list);

        TextView title = findViewById(R.id.tv_manage_title);
        title.setText("Manage Users");

        findViewById(R.id.btn_back_manage).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.rv_manage_list);
        progressBar = findViewById(R.id.progress_manage);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(new ArrayList<>(), new UserAdapter.OnUserActionListener() {
            @Override
            public void onEdit(AuthResponse.User user) {
                showEditUserDialog(user);
            }

            @Override
            public void onDelete(AuthResponse.User user) {
                confirmDeleteUser(user);
            }
        });
        recyclerView.setAdapter(adapter);

        fetchUsers();
    }

    private void showEditUserDialog(AuthResponse.User user) {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(user.getFullName());
        new AlertDialog.Builder(this)
                .setTitle("Edit User Name")
                .setView(input)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) updateUserName(user, newName);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUserName(AuthResponse.User user, String newName) {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> updates = new HashMap<>();
        updates.put("full_name", newName);
        
        String filter = "eq." + user.getId();
        SupabaseManager.getInstance().getAuthService().updateUserData(filter, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ManageUsersActivity.this, "User updated", Toast.LENGTH_SHORT).show();
                    fetchUsers();
                } else {
                    Toast.makeText(ManageUsersActivity.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteUser(AuthResponse.User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getFullName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(AuthResponse.User user) {
        progressBar.setVisibility(View.VISIBLE);
        String filter = "eq." + user.getId();
        SupabaseManager.getInstance().getAuthService().deleteUserData(filter).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ManageUsersActivity.this, "User record removed", Toast.LENGTH_SHORT).show();
                    fetchUsers();
                } else {
                    Toast.makeText(ManageUsersActivity.this, "Deletion failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUsers() {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseManager.getInstance().getAuthService().getAllUsers("*").enqueue(new Callback<List<AuthResponse.User>>() {
            @Override
            public void onResponse(@NonNull Call<List<AuthResponse.User>> call, @NonNull Response<List<AuthResponse.User>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<AuthResponse.User> userList = response.body();
                    // Always update the adapter so the screen clears if the list is empty
                    adapter.setUsers(userList);
                    
                    if (userList.isEmpty()) {
                        Toast.makeText(ManageUsersActivity.this, "No users found in database.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManageUsersActivity.this, "Access Denied: Check RLS Policies (Error " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(@NonNull Call<List<AuthResponse.User>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private List<AuthResponse.User> users;
        private final OnUserActionListener listener;

        interface OnUserActionListener {
            void onEdit(AuthResponse.User user);
            void onDelete(AuthResponse.User user);
        }

        UserAdapter(List<AuthResponse.User> users, OnUserActionListener listener) {
            this.users = users;
            this.listener = listener;
        }

        void setUsers(List<AuthResponse.User> users) {
            this.users = users;
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
            AuthResponse.User user = users.get(position);
            holder.tvTitle.setText(user.getFullName());
            holder.tvSubtitle.setText(user.getEmail());
            holder.tvDetails.setText("ID: " + user.getId());

            holder.imgPreview.setVisibility(View.GONE); // No image for users

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(user));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
        }

        @Override
        public int getItemCount() {
            return users.size();
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
