package com.example.final_projects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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

import com.example.final_projects.network.OrderRequest;
import com.example.final_projects.network.SupabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View progressBar;
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_list);

        TextView title = findViewById(R.id.tv_manage_title);
        title.setText("Manage Orders");

        findViewById(R.id.btn_back_manage).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_manage_list);
        progressBar = findViewById(R.id.progress_manage);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(new ArrayList<>(), new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onEdit(OrderRequest order) {
                showUpdateStatusDialog(order);
            }

            @Override
            public void onDelete(OrderRequest order) {
                confirmDeleteOrder(order);
            }
        });
        recyclerView.setAdapter(adapter);

        fetchOrders();
    }

    private void showUpdateStatusDialog(OrderRequest order) {
        String[] statuses = {"Pending", "Shipped", "Delivered", "Cancelled"};
        new AlertDialog.Builder(this)
                .setTitle("Update Order Status")
                .setItems(statuses, (dialog, which) -> {
                    updateOrderStatus(order, statuses[which]);
                })
                .show();
    }

    private void updateOrderStatus(OrderRequest order, String newStatus) {
        progressBar.setVisibility(View.VISIBLE);
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", newStatus); 
        
        String filter = "eq." + order.getOrderId();
        SupabaseManager.getInstance().getAuthService().updateOrder(filter, updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ManageOrdersActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                    fetchOrders();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void confirmDeleteOrder(OrderRequest order) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Order")
                .setMessage("Are you sure you want to delete order " + order.getOrderId() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteOrder(order))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteOrder(OrderRequest order) {
        progressBar.setVisibility(View.VISIBLE);
        String filter = "eq." + order.getOrderId();
        SupabaseManager.getInstance().getAuthService().deleteOrder(filter).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ManageOrdersActivity.this, "Order deleted", Toast.LENGTH_SHORT).show();
                    fetchOrders();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchOrders() {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseManager.getInstance().getAuthService().getAllOrders("*").enqueue(new Callback<List<OrderRequest>>() {
            @Override
            public void onResponse(Call<List<OrderRequest>> call, Response<List<OrderRequest>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setOrders(response.body());
                } else {
                    Toast.makeText(ManageOrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderRequest>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageOrdersActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private List<OrderRequest> orders;
        private final OnOrderActionListener listener;

        interface OnOrderActionListener {
            void onEdit(OrderRequest order);
            void onDelete(OrderRequest order);
        }

        OrderAdapter(List<OrderRequest> orders, OnOrderActionListener listener) {
            this.orders = orders;
            this.listener = listener;
        }

        void setOrders(List<OrderRequest> orders) {
            this.orders = orders;
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
            OrderRequest order = orders.get(position);
            holder.tvTitle.setText(order.getOrderId());
            holder.tvSubtitle.setText(String.format(Locale.US, "Status: %s | Total: $%.2f", order.getStatus(), order.getTotalAmount()));
            holder.tvDetails.setText("Customer: " + order.getCustomerName() + "\nItems: " + order.getItemsSummary());
            
            holder.imgPreview.setVisibility(View.GONE); // No default image for orders

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(order));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(order));
        }

        @Override
        public int getItemCount() {
            return orders.size();
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
