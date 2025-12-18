package com.example.bookup.utils;

import androidx.annotation.NonNull;
import android.util.Log;
import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AdminSecurityManager {
    private static final String TAG = "AdminSecurityManager";
    private static final int MAX_OPERATIONS_PER_WINDOW = 50; // Maximum operations allowed in time window
    private static final long TIME_WINDOW_MS = TimeUnit.MINUTES.toMillis(15); // 15 minute window
    
    private static AdminSecurityManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private final Map<String, RateLimit> rateLimits = new HashMap<>();
    
    private static class RateLimit {
        long windowStart;
        int operationCount;
        
        RateLimit() {
            this.windowStart = System.currentTimeMillis();
            this.operationCount = 0;
        }
    }
    
    private AdminSecurityManager() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    
    public static synchronized AdminSecurityManager getInstance() {
        if (instance == null) {
            instance = new AdminSecurityManager();
        }
        return instance;
    }
    
    /**
     * Checks if an admin operation should be allowed based on rate limits
     * @param context Android context for showing messages
     * @param operationType Type of operation being performed
     * @return true if operation is allowed, false if rate limited
     */
    public boolean checkRateLimit(@NonNull Context context, @NonNull String operationType) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return false;
        
        String userId = currentUser.getUid();
        String key = userId + ":" + operationType;
        
        RateLimit limit = rateLimits.get(key);
        long currentTime = System.currentTimeMillis();
        
        if (limit == null || currentTime - limit.windowStart > TIME_WINDOW_MS) {
            limit = new RateLimit();
            rateLimits.put(key, limit);
        }
        
        limit.operationCount++;
        
        if (limit.operationCount > MAX_OPERATIONS_PER_WINDOW) {
            Toast.makeText(context, 
                "Too many operations. Please wait before trying again.", 
                Toast.LENGTH_LONG).show();
            return false;
        }
        
        return true;
    }
    
    /**
     * Verifies admin status with additional security checks
     * @param userId User ID to check
     * @return Task that resolves to true if user is admin
     */
    public Task<Boolean> verifyAdminStatus(String userId) {
        return db.collection("users").document(userId).get()
            .continueWith(task -> {
                if (!task.isSuccessful() || !task.getResult().exists()) {
                    return false;
                }
                
                Boolean isAdmin = task.getResult().getBoolean("isAdmin");
                if (isAdmin == null || !isAdmin) {
                    Log.w(TAG, "Unauthorized admin access attempt by user: " + userId);
                    return false;
                }
                
                return true;
            });
    }
    
    /**
     * Validates and sanitizes input for admin operations
     * @param input String to validate
     * @return Sanitized string or null if invalid
     */
    public String validateAndSanitizeInput(String input) {
        if (input == null) return null;
        
        // Remove any potentially harmful characters
        return input.replaceAll("[<>\"'&]", "")
                   .replaceAll("(?i)javascript:", "")
                   .trim();
    }
    
    /**
     * Records an admin action for audit purposes
     * @param userId Admin user ID
     * @param action Action performed
     * @param details Additional details
     */
    public void logAdminAction(String userId, String action, String details) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("userId", userId);
        logEntry.put("action", action);
        logEntry.put("details", details);
        logEntry.put("timestamp", System.currentTimeMillis());
        
        db.collection("admin_logs")
            .add(logEntry)
            .addOnFailureListener(e -> 
                Log.e(TAG, "Failed to log admin action: " + e.getMessage(), e));
    }
}