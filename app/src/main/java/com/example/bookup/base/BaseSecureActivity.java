package com.example.bookup.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bookup.R;
import com.example.bookup.activities.SignInActivity;
import com.example.bookup.utils.AuthManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced base activity class that provides comprehensive security features
 * and standardized error handling across the application.
 */
public abstract class BaseSecureActivity extends AppCompatActivity {
    private static final String TAG = "BaseSecureActivity";
    
    // Authentication components
    protected FirebaseAuth mAuth;
    protected AuthManager authManager;
    
    // Rate limiting
    private static final Map<String, Long> lastActionTimestamp = new HashMap<>();
    private static final Map<String, Integer> actionAttempts = new HashMap<>();
    
    // Session tracking
    private long lastInteractionTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        authManager = AuthManager.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkAuthentication()) {
            redirectToLogin();
            return;
        }
        authManager.refreshTokenIfNeeded();
    }

    protected boolean checkAuthentication() {
        if (!authManager.isSessionValid()) {
            Log.w(TAG, "Session invalid, redirecting to login");
            return false;
        }
        return true;
    }

    protected void redirectToLogin() {
        // Check if we're already on the login screen to prevent loops
        if (getClass().getSimpleName().equals("SignInActivity")) {
            return;
        }
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    protected void handleFirebaseError(Exception e) {
        String message;
        if (e instanceof FirebaseAuthInvalidUserException) {
            message = "Your session has expired. Please sign in again.";
            redirectToLogin();
        } else if (e instanceof FirebaseNetworkException) {
            message = "Network error. Please check your connection and try again.";
        } else if (e instanceof FirebaseFirestoreException) {
            message = "Database error. Please try again later.";
        } else {
            message = "An error occurred. Please try again.";
            Log.e(TAG, "Unhandled error: " + e.getMessage(), e);
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void setLoading(boolean isLoading) {
        // To be implemented by child classes
    }
}