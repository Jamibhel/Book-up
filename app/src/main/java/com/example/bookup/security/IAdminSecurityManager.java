package com.example.bookup.security;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

/**
 * Interface defining security operations for admin features.
 * Provides a standardized way to implement admin-level security checks and audit logging.
 */
public interface IAdminSecurityManager {
    /**
     * Verifies if a user has admin privileges.
     * @param userId The ID of the user to check.
     * @return A Task that resolves to true if the user is an admin.
     */
    Task<Boolean> verifyAdminStatus(@NonNull String userId);

    /**
     * Checks rate limiting for admin operations.
     * @param context Context for showing messages.
     * @param operationType The type of operation being performed.
     * @return true if the operation is allowed, false if rate limited.
     */
    boolean checkRateLimit(@NonNull Context context, @NonNull String operationType);

    /**
     * Validates and sanitizes input for security.
     * @param input The input to validate.
     * @return Sanitized string or null if invalid.
     */
    String validateAndSanitizeInput(String input);

    /**
     * Logs admin actions for audit purposes.
     * @param userId The ID of the admin performing the action.
     * @param action The action being performed.
     * @param details Additional details about the action.
     */
    void logAdminAction(@NonNull String userId, @NonNull String action, String details);
}