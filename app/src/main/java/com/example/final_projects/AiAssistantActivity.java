package com.example.final_projects;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_projects.network.FrameRequest;
import com.example.final_projects.network.SupabaseManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiAssistantActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText editInput;
    private ImageButton btnSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private EyewearAiModel aiModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager.init(this);
        setContentView(R.layout.activity_ai_assistant);

        findViewById(R.id.btn_back_ai).setOnClickListener(v -> finish());

        rvChat = findViewById(R.id.rv_chat_messages);
        editInput = findViewById(R.id.edit_chat_input);
        btnSend = findViewById(R.id.btn_send_chat);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(this, messageList);
        rvChat.setAdapter(chatAdapter);

        // Fetch live database frames to train AI Knowledge Base
        fetchFramesAndInitializeAi();

        btnSend.setOnClickListener(v -> sendMessage());

        setupSuggestionChips();
    }

    private void fetchFramesAndInitializeAi() {
        SupabaseManager.getInstance().getAuthService().getAllFrames("*").enqueue(new Callback<List<FrameRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<FrameRequest>> call, @NonNull Response<List<FrameRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    aiModel = new EyewearAiModel(response.body());
                } else {
                    aiModel = new EyewearAiModel(null);
                }
                addInitialGreeting();
            }

            @Override
            public void onFailure(@NonNull Call<List<FrameRequest>> call, @NonNull Throwable t) {
                aiModel = new EyewearAiModel(null);
                addInitialGreeting();
            }
        });
    }

    private void addInitialGreeting() {
        if (messageList.isEmpty()) {
            messageList.add(new ChatMessage(
                    "Hello! 👋 I am your Visionary AI Eyewear Specialist. Ask me about frames for your face shape, top-rated glasses, prices, or Virtual Try-On!",
                    ChatMessage.TYPE_AI
            ));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
        }
    }

    private void setupSuggestionChips() {
        View.OnClickListener chipListener = v -> {
            String text = "";
            if (v.getId() == R.id.chip_round_face) text = "What frames are best for a round face?";
            else if (v.getId() == R.id.chip_square_frames) text = "Show me Classic Square frames";
            else if (v.getId() == R.id.chip_best_value) text = "What are your best value frames?";
            else if (v.getId() == R.id.chip_try_on) text = "How does Virtual Try-On work?";

            if (!text.isEmpty()) {
                processUserQuery(text);
            }
        };

        findViewById(R.id.chip_round_face).setOnClickListener(chipListener);
        findViewById(R.id.chip_square_frames).setOnClickListener(chipListener);
        findViewById(R.id.chip_best_value).setOnClickListener(chipListener);
        findViewById(R.id.chip_try_on).setOnClickListener(chipListener);
    }

    private void sendMessage() {
        String input = editInput.getText().toString().trim();
        if (input.isEmpty()) return;

        editInput.setText("");
        processUserQuery(input);
    }

    private void processUserQuery(String query) {
        // Add User Message
        messageList.add(new ChatMessage(query, ChatMessage.TYPE_USER));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChat.smoothScrollToPosition(messageList.size() - 1);

        // Process with AI Model
        if (aiModel == null) {
            aiModel = new EyewearAiModel(null);
        }

        EyewearAiModel.AiResponse response = aiModel.processQuery(query);

        // Add AI Response after slight delay for natural chat feel
        rvChat.postDelayed(() -> {
            messageList.add(new ChatMessage(response.replyText, ChatMessage.TYPE_AI, response.matchedFrame));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            rvChat.smoothScrollToPosition(messageList.size() - 1);
        }, 300);
    }
}
