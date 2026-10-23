package com.example.final_projects;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_projects.network.FrameRequest;

import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<ChatMessage> messages;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.TYPE_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvUserMessage.setText(message.getMessage());
        } else if (holder instanceof AiViewHolder) {
            AiViewHolder aiHolder = (AiViewHolder) holder;
            aiHolder.tvAiMessage.setText(message.getMessage());

            FrameRequest frame = message.getRecommendedFrame();
            if (frame != null) {
                aiHolder.cardRecommendation.setVisibility(View.VISIBLE);
                aiHolder.tvRecName.setText(frame.getName());
                aiHolder.tvRecPrice.setText(String.format(Locale.US, "$%.2f", frame.getPrice()));
                aiHolder.tvRecRating.setText(String.format(Locale.US, "⭐ %.1f", frame.getRating()));

                loadImage(aiHolder.imgRecFrame, frame.getImageUrl());

                aiHolder.btnRecTryOn.setOnClickListener(v -> {
                    Intent intent = new Intent(context, VirtualTryOnActivity.class);
                    context.startActivity(intent);
                });

                aiHolder.btnRecView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, GlassCatalogActivity.class);
                    context.startActivity(intent);
                });
            } else {
                aiHolder.cardRecommendation.setVisibility(View.GONE);
            }
        }
    }

    private void loadImage(ImageView img, String source) {
        if (source == null || source.isEmpty()) {
            img.setImageResource(R.drawable.square);
            return;
        }

        if (source.length() > 100) { // Base64
            try {
                byte[] decoded = Base64.decode(source, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                img.setImageBitmap(bitmap);
            } catch (Exception e) {
                img.setImageResource(R.drawable.square);
            }
        } else { // Resource Name
            int resId = context.getResources().getIdentifier(source, "drawable", context.getPackageName());
            if (resId != 0) img.setImageResource(resId);
            else img.setImageResource(R.drawable.square);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserMessage;
        UserViewHolder(View itemView) {
            super(itemView);
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView tvAiMessage, tvRecName, tvRecPrice, tvRecRating;
        ImageView imgRecFrame;
        View cardRecommendation, btnRecTryOn, btnRecView;

        AiViewHolder(View itemView) {
            super(itemView);
            tvAiMessage = itemView.findViewById(R.id.tv_ai_message);
            cardRecommendation = itemView.findViewById(R.id.card_frame_recommendation);
            imgRecFrame = itemView.findViewById(R.id.img_rec_frame);
            tvRecName = itemView.findViewById(R.id.tv_rec_name);
            tvRecPrice = itemView.findViewById(R.id.tv_rec_price);
            tvRecRating = itemView.findViewById(R.id.tv_rec_rating);
            btnRecTryOn = itemView.findViewById(R.id.btn_rec_tryon);
            btnRecView = itemView.findViewById(R.id.btn_rec_view);
        }
    }
}
