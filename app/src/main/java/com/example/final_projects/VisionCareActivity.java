package com.example.final_projects;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class VisionCareActivity extends AppCompatActivity implements OnMapReadyCallback {

    private RecyclerView recyclerView;
    private View mapFragmentContainer;
    private ImageButton btnToggle;
    private boolean isMapVisible = false;
    private List<VisionCenter> centers = new ArrayList<>();
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision_care);

        findViewById(R.id.btn_back_vision).setOnClickListener(v -> finish());
        btnToggle = findViewById(R.id.btn_toggle_view);
        recyclerView = findViewById(R.id.rv_vision_care);
        mapFragmentContainer = findViewById(R.id.vision_care_map);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        centers.add(new VisionCenter("Visionary Elite Main Hub", "123 Eye Care St, Medical District, NY", "+1 800-555-0101", 40.7128, -74.0060));
        centers.add(new VisionCenter("ClearSight Vision Center", "456 Lens Ave, Downtown Area, CA", "+1 800-555-0102", 34.0522, -118.2437));
        centers.add(new VisionCenter("Elite Optical Services", "789 Frame Blvd, Suburban Center, TX", "+1 800-555-0103", 29.7604, -95.3698));
        centers.add(new VisionCenter("Perfect Vision Care", "101 Sight Road, Uptown Plaza, FL", "+1 800-555-0104", 25.7617, -80.1918));

        VisionCareAdapter adapter = new VisionCareAdapter(centers);
        recyclerView.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.vision_care_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnToggle.setOnClickListener(v -> toggleView());
    }

    private void toggleView() {
        if (isMapVisible) {
            recyclerView.setVisibility(View.VISIBLE);
            mapFragmentContainer.setVisibility(View.GONE);
            btnToggle.setImageResource(R.drawable.ic_map);
        } else {
            recyclerView.setVisibility(View.GONE);
            mapFragmentContainer.setVisibility(View.VISIBLE);
            btnToggle.setImageResource(R.drawable.ic_list);
        }
        isMapVisible = !isMapVisible;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        for (VisionCenter center : centers) {
            LatLng pos = new LatLng(center.lat, center.lng);
            googleMap.addMarker(new MarkerOptions().position(pos).title(center.name).snippet(center.address));
        }
        if (!centers.isEmpty()) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(centers.get(0).lat, centers.get(0).lng), 10));
        }
    }

    private static class VisionCenter {
        String name, address, phone;
        double lat, lng;
        VisionCenter(String name, String address, String phone, double lat, double lng) {
            this.name = name;
            this.address = address;
            this.phone = phone;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private class VisionCareAdapter extends RecyclerView.Adapter<VisionCareViewHolder> {
        private final List<VisionCenter> centers;

        VisionCareAdapter(List<VisionCenter> centers) {
            this.centers = centers;
        }

        @NonNull
        @Override
        public VisionCareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vision_care, parent, false);
            return new VisionCareViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VisionCareViewHolder holder, int position) {
            VisionCenter center = centers.get(position);
            holder.tvName.setText(center.name);
            holder.tvAddress.setText(center.address);
            holder.tvPhone.setText("Phone: " + center.phone);

            holder.btnMap.setOnClickListener(v -> {
                // Focus on map for this specific location
                recyclerView.setVisibility(View.GONE);
                mapFragmentContainer.setVisibility(View.VISIBLE);
                isMapVisible = true;
                btnToggle.setImageResource(R.drawable.ic_list);
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(center.lat, center.lng), 15));
                }
            });
        }

        @Override
        public int getItemCount() {
            return centers.size();
        }
    }

    private static class VisionCareViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvPhone;
        View btnMap;
        VisionCareViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_center_name);
            tvAddress = itemView.findViewById(R.id.tv_center_address);
            tvPhone = itemView.findViewById(R.id.tv_center_contact);
            btnMap = itemView.findViewById(R.id.btn_view_on_map);
        }
    }
}
