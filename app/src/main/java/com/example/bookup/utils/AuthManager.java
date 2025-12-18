package com.example.bookup.utils;

import android.util.Log;
import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {
    private static final String TAG = "AuthManager";
    private static final long TOKEN_REFRESH_INTERVAL = 45 * 60 * 1000; // 45 minutes

    private static AuthManager instance;
    private final FirebaseAuth mAuth;
    private long lastTokenRefresh = 0;

    private AuthManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public void refreshTokenIfNeeded() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTokenRefresh > TOKEN_REFRESH_INTERVAL) {
            currentUser.getIdToken(true)
                .addOnSuccessListener(result -> {
                    lastTokenRefresh = currentTime;
                    Log.d(TAG, "Token refreshed successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to refresh token", e);
                    // Force re-authentication if token refresh fails
                    if (e.getMessage() != null && e.getMessage().contains("ERROR_REQUIRES_RECENT_LOGIN")) {
                        mAuth.signOut();
                    }
                });
        }
    }

    public boolean isSessionValid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null && !currentUser.isAnonymous();
    }

    public void signOut() {
        mAuth.signOut();
        lastTokenRefresh = 0;
    }
}