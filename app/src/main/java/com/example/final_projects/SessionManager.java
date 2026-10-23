package com.example.final_projects;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "visionary_session";
    private static final String KEY_USER_ID = "user_id";
    
    private static SessionManager instance;
    private SharedPreferences sharedPreferences;

    private SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("SessionManager must be initialized with context before use.");
        }
        return instance;
    }

    public void setUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public void logout() {
        sharedPreferences.edit().clear().apply();
    }
}
