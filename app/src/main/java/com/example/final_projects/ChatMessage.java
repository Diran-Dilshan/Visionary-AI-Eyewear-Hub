package com.example.final_projects;

import com.example.final_projects.network.FrameRequest;

public class ChatMessage {
    public static final int TYPE_USER = 1;
    public static final int TYPE_AI = 2;

    private String message;
    private int type;
    private FrameRequest recommendedFrame;

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
        this.recommendedFrame = null;
    }

    public ChatMessage(String message, int type, FrameRequest recommendedFrame) {
        this.message = message;
        this.type = type;
        this.recommendedFrame = recommendedFrame;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public FrameRequest getRecommendedFrame() {
        return recommendedFrame;
    }
}
