package com.example.final_projects;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.example.final_projects.network.FrameRequest;
import com.example.final_projects.network.SupabaseManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageFramesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View progressBar;
    private FrameAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_list);

        TextView title = findViewById(R.id.tv_manage_title);
        title.setText("Manage Frames");

        findViewById(R.id.btn_back_manage).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_manage_list);
        progressBar = findViewById(R.id.progress_manage);
        FloatingActionButton fab = findViewById(R.id.fab_add_new);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddFrameActivity.class)));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FrameAdapter(new ArrayList<>(), new FrameAdapter.OnFrameActionListener() {
            @Override
            public void onEdit(FrameRequest frame) {
                Intent intent = new Intent(ManageFramesActivity.this, AddFrameActivity.class);
                intent.putExtra("EDIT_FRAME", frame);
                startActivity(intent);
            }

            @Override
            public void onDelete(FrameRequest frame) {
                confirmDeleteFrame(frame);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void confirmDeleteFrame(FrameRequest frame) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Frame")
                .setMessage("Are you sure you want to delete " + frame.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteFrame(frame))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFrame(FrameRequest frame) {
        progressBar.setVisibility(View.VISIBLE);
        String filter = "eq." + frame.getId();
        SupabaseManager.getInstance().getAuthService().deleteFrame(filter).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ManageFramesActivity.this, "Frame deleted", Toast.LENGTH_SHORT).show();
                    fetchFrames();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchFrames();
    }

    private void fetchFrames() {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseManager.getInstance().getAuthService().getAllFrames("*").enqueue(new Callback<List<FrameRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<FrameRequest>> call, @NonNull Response<List<FrameRequest>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setFrames(response.body());
                } else {
                    Toast.makeText(ManageFramesActivity.this, "Failed to load (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FrameRequest>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageFramesActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.ViewHolder> {
        private List<FrameRequest> frames;
        private final OnFrameActionListener listener;

        interface OnFrameActionListener {
            void onEdit(FrameRequest frame);
            void onDelete(FrameRequest frame);
        }

        FrameAdapter(List<FrameRequest> frames, OnFrameActionListener listener) {
            this.frames = frames;
            this.listener = listener;
        }

        void setFrames(List<FrameRequest> frames) {
            this.frames = frames;
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
            FrameRequest frame = frames.get(position);
            holder.tvTitle.setText(frame.getName());
            holder.tvSubtitle.setText(String.format(Locale.US, "$%.2f | Stock: %d", frame.getPrice(), frame.getStockCount()));
            holder.tvDetails.setText(frame.getDescription());

            // Load Preview Image
            holder.imgPreview.setVisibility(View.VISIBLE);
            loadImage(holder.imgPreview, frame.getImageUrl());

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(frame));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(frame));
        }

        private void loadImage(ImageView img, String source) {
            if (source == null || source.isEmpty()) {
                img.setImageResource(R.drawable.glass1);
                return;
            }

            if (source.length() > 100) { // Assume Base64
                try {
                    byte[] decoded = Base64.decode(source, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    img.setImageBitmap(bitmap);
                } catch (Exception e) {
                    img.setImageResource(R.drawable.glass1);
                }
            } else { // Assume Resource Name
                int resId = img.getContext().getResources().getIdentifier(source, "drawable", img.getContext().getPackageName());
                if (resId != 0) img.setImageResource(resId);
                else img.setImageResource(R.drawable.glass1);
            }
        }

        @Override
        public int getItemCount() {
            return frames.size();
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
