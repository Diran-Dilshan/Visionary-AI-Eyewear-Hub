package com.example.final_projects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_projects.network.FrameRequest;
import com.example.final_projects.network.OrderRequest;
import com.example.final_projects.network.SupabaseManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderTrackingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View progressBar;
    private TextView tvNoOrders, tvSummary;
    private TrackingAdapter adapter;
    private List<OrderRequest> allOrders = new ArrayList<>();
    private List<FrameRequest> catalogFrames = new ArrayList<>();
    
    private TextView chipAll, chipInProgress, chipDelivered, chipCancelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager.init(this);
        setContentView(R.layout.activity_order_tracking);

        // UI References
        tvSummary = findViewById(R.id.tv_order_summary);
        recyclerView = findViewById(R.id.rv_order_tracking);
        progressBar = findViewById(R.id.progress_tracking);
        tvNoOrders = findViewById(R.id.tv_no_orders);
        
        chipAll = findViewById(R.id.chip_all);
        chipInProgress = findViewById(R.id.chip_in_progress);
        chipDelivered = findViewById(R.id.chip_delivered);
        chipCancelled = findViewById(R.id.chip_cancelled);

        findViewById(R.id.btn_back_tracking).setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrackingAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        setupFilters();
        fetchCatalogAndOrders();
    }

    private void setupFilters() {
        View.OnClickListener filterListener = v -> {
            resetChips();
            v.setBackgroundResource(R.drawable.category_bg_selected);
            ((TextView)v).setTextColor(ContextCompat.getColor(this, R.color.white));
            
            if (v.getId() == R.id.chip_all) filterOrders("All");
            else if (v.getId() == R.id.chip_in_progress) filterOrders("Pending");
            else if (v.getId() == R.id.chip_delivered) filterOrders("Delivered");
            else if (v.getId() == R.id.chip_cancelled) filterOrders("Cancelled");
        };

        chipAll.setOnClickListener(filterListener);
        chipInProgress.setOnClickListener(filterListener);
        chipDelivered.setOnClickListener(filterListener);
        chipCancelled.setOnClickListener(filterListener);
    }

    private void resetChips() {
        TextView[] chips = {chipAll, chipInProgress, chipDelivered, chipCancelled};
        for (TextView chip : chips) {
            chip.setBackgroundResource(R.drawable.category_bg);
            chip.setTextColor(0xFF666666);
        }
    }

    private void filterOrders(String status) {
        if (status.equals("All")) {
            updateList(allOrders);
        } else {
            List<OrderRequest> filtered = new ArrayList<>();
            for (OrderRequest o : allOrders) {
                if (status.equalsIgnoreCase("Pending")) {
                    if (o.getStatus().equalsIgnoreCase("Pending") || o.getStatus().equalsIgnoreCase("Shipped")) {
                        filtered.add(o);
                    }
                } else if (o.getStatus().equalsIgnoreCase(status)) {
                    filtered.add(o);
                }
            }
            updateList(filtered);
        }
    }

    private void updateList(List<OrderRequest> list) {
        if (list.isEmpty()) {
            tvNoOrders.setVisibility(View.VISIBLE);
        } else {
            tvNoOrders.setVisibility(View.GONE);
        }
        adapter.setOrders(list);
    }

    private void fetchCatalogAndOrders() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Step 1: Fetch Catalog Frames to match frame images
        SupabaseManager.getInstance().getAuthService().getAllFrames("*").enqueue(new Callback<List<FrameRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<FrameRequest>> call, @NonNull Response<List<FrameRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    catalogFrames = response.body();
                    adapter.setCatalogFrames(catalogFrames);
                }
                fetchMyOrders();
            }

            @Override
            public void onFailure(@NonNull Call<List<FrameRequest>> call, @NonNull Throwable t) {
                fetchMyOrders();
            }
        });
    }

    private void fetchMyOrders() {
        String userId = SessionManager.getInstance().getUserId();
        if (userId == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String filter = "eq." + userId;
        SupabaseManager.getInstance().getAuthService().getOrdersByUserId(filter, "*").enqueue(new Callback<List<OrderRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<OrderRequest>> call, @NonNull Response<List<OrderRequest>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body();
                    updateHeaderSummary();
                    filterOrders("All");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<OrderRequest>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateHeaderSummary() {
        int active = 0;
        for (OrderRequest o : allOrders) {
            if (!"Delivered".equalsIgnoreCase(o.getStatus()) && !"Cancelled".equalsIgnoreCase(o.getStatus())) {
                active++;
            }
        }
        tvSummary.setText(active + " frames on the way");
    }

    private static class TrackingAdapter extends RecyclerView.Adapter<TrackingAdapter.ViewHolder> {
        private List<OrderRequest> orders;
        private List<FrameRequest> catalogFrames = new ArrayList<>();
        private int expandedPosition = -1;

        TrackingAdapter(List<OrderRequest> orders) {
            this.orders = orders;
        }

        void setOrders(List<OrderRequest> orders) {
            this.orders = orders;
            notifyDataSetChanged();
        }

        void setCatalogFrames(List<FrameRequest> frames) {
            if (frames != null) {
                this.catalogFrames = frames;
                notifyDataSetChanged();
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_tracking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OrderRequest order = orders.get(position);
            
            // Text values
            holder.tvItems.setText(order.getItemsSummary());
            holder.tvIdPrice.setText(String.format(Locale.US, "%s • Total: $%.2f", order.getOrderId(), order.getTotalAmount()));
            
            String status = order.getStatus();
            if (status == null) status = "Pending";
            holder.tvStatus.setText(status);

            // Load Ordered Frame Image
            loadOrderFrameImage(holder.imgOrderFrame, order.getItemsSummary());

            // Status Badge
            int color = 0xFF35858B;
            int bgColor = 0xFFE0F2F1;
            
            if ("Shipped".equalsIgnoreCase(status)) {
                color = 0xFF2E7D32; bgColor = 0xFFE8F5E9;
            } else if ("Delivered".equalsIgnoreCase(status)) {
                color = 0xFF1565C0; bgColor = 0xFFE3F2FD;
            } else if ("Cancelled".equalsIgnoreCase(status)) {
                color = 0xFFC62828; bgColor = 0xFFFFEBEE;
            } else if ("Pending".equalsIgnoreCase(status)) {
                color = 0xFFEF6C00; bgColor = 0xFFFFF3E0;
            }

            holder.tvStatus.setTextColor(color);
            holder.tvStatus.getBackground().setTint(bgColor);
            
            // Expansion Logic
            final boolean isExpanded = position == expandedPosition;
            holder.layoutJourney.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.ivArrow.setRotation(isExpanded ? 180f : 0f);
            
            holder.itemView.setOnClickListener(v -> {
                int previousExpandedPosition = expandedPosition;
                expandedPosition = isExpanded ? -1 : position;
                notifyItemChanged(previousExpandedPosition);
                notifyItemChanged(expandedPosition);
            });

            // Date logic
            if (order.getCreatedAt() != null) {
                String date = order.getCreatedAt().substring(0, 10);
                holder.tvStatusDate.setText(status + " on " + date);
                holder.tvDatePlaced.setText(date);
                holder.tvDateQuality.setText(date);
                holder.tvDateShipped.setText(date);
            }

            // Update Progress Dots
            updateProgressDots(holder, status);
        }

        private void loadOrderFrameImage(ImageView img, String itemsSummary) {
            if (itemsSummary == null) {
                img.setImageResource(R.drawable.glass);
                return;
            }

            String summaryLower = itemsSummary.toLowerCase(Locale.US);

            // 1. Try matching against live catalog frames from DB
            if (catalogFrames != null) {
                for (FrameRequest frame : catalogFrames) {
                    if (frame.getName() != null && summaryLower.contains(frame.getName().toLowerCase(Locale.US))) {
                        String source = frame.getImageUrl();
                        if (source != null && !source.isEmpty()) {
                            if (source.length() > 100) { // Base64 String
                                try {
                                    byte[] decoded = Base64.decode(source, Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                    if (bitmap != null) {
                                        img.setImageBitmap(bitmap);
                                        return;
                                    }
                                } catch (Exception ignored) {}
                            } else { // Resource name
                                int resId = img.getContext().getResources().getIdentifier(source, "drawable", img.getContext().getPackageName());
                                if (resId != 0) {
                                    img.setImageResource(resId);
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            // 2. Keyword Fallback to local drawable images
            if (summaryLower.contains("square")) {
                img.setImageResource(R.drawable.square);
            } else if (summaryLower.contains("tortoise")) {
                img.setImageResource(R.drawable.tortoiseshell);
            } else if (summaryLower.contains("aviator")) {
                img.setImageResource(R.drawable.aviator);
            } else if (summaryLower.contains("glass1") || summaryLower.contains("cat")) {
                img.setImageResource(R.drawable.glass1);
            } else if (summaryLower.contains("glass2")) {
                img.setImageResource(R.drawable.glass2);
            } else if (summaryLower.contains("glass3")) {
                img.setImageResource(R.drawable.glass3);
            } else {
                img.setImageResource(R.drawable.glass);
            }
        }

        private void updateProgressDots(ViewHolder holder, String status) {
            // Reset all to RED first
            holder.dotPlaced.setBackgroundResource(R.drawable.dot_red);
            holder.dotQuality.setBackgroundResource(R.drawable.dot_red);
            holder.dotShipped.setBackgroundResource(R.drawable.dot_red);
            holder.dotDelivered.setBackgroundResource(R.drawable.dot_red);

            // Placed is always complete once it appears here
            holder.dotPlaced.setBackgroundResource(R.drawable.dot_green);

            if ("Pending".equalsIgnoreCase(status)) {
                // Next step is Quality Check
                holder.dotQuality.setBackgroundResource(R.drawable.dot_yellow);
                // Shipped and Delivered stay Red
            } else if ("Shipped".equalsIgnoreCase(status)) {
                // Quality is done
                holder.dotQuality.setBackgroundResource(R.drawable.dot_green);
                // Next step is Shipping/Delivering
                holder.dotShipped.setBackgroundResource(R.drawable.dot_yellow);
                // Delivered stays Red
            } else if ("Delivered".equalsIgnoreCase(status)) {
                // All steps are complete
                holder.dotPlaced.setBackgroundResource(R.drawable.dot_green);
                holder.dotQuality.setBackgroundResource(R.drawable.dot_green);
                holder.dotShipped.setBackgroundResource(R.drawable.dot_green);
                holder.dotDelivered.setBackgroundResource(R.drawable.dot_green);
            }
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItems, tvIdPrice, tvStatus, tvStatusDate;
            TextView tvDatePlaced, tvDateQuality, tvDateShipped, tvDateDelivered;
            ImageView imgOrderFrame, ivArrow;
            View layoutJourney, dotPlaced, dotQuality, dotShipped, dotDelivered;

            ViewHolder(View itemView) {
                super(itemView);
                imgOrderFrame = itemView.findViewById(R.id.img_order_frame);
                tvItems = itemView.findViewById(R.id.tv_order_items);
                tvIdPrice = itemView.findViewById(R.id.tv_order_id_price);
                tvStatus = itemView.findViewById(R.id.tv_order_status);
                tvStatusDate = itemView.findViewById(R.id.tv_status_date);
                ivArrow = itemView.findViewById(R.id.iv_expand_arrow);
                layoutJourney = itemView.findViewById(R.id.layout_journey);
                
                dotPlaced = itemView.findViewById(R.id.dot_placed);
                dotQuality = itemView.findViewById(R.id.dot_quality);
                dotShipped = itemView.findViewById(R.id.dot_shipped);
                dotDelivered = itemView.findViewById(R.id.dot_delivered);

                tvDatePlaced = itemView.findViewById(R.id.tv_date_placed);
                tvDateQuality = itemView.findViewById(R.id.tv_date_quality);
                tvDateShipped = itemView.findViewById(R.id.tv_date_shipped);
                tvDateDelivered = itemView.findViewById(R.id.tv_date_delivered);
            }
        }
    }
}
