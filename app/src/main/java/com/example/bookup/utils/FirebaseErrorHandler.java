package com.example.bookup.utils;

import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.FileNotFoundException;

/**
 * FirebaseErrorHandler - Centralized Firebase error handling
 * 
 * Categorizes Firebase errors and provides user-friendly messages.
 * Replaces scattered error handling throughout the app with consistent,
 * professional error messages.
 * 
 * Usage:
 * db.collection("items").get()
 *   .addOnFailureListener(e -> FirebaseErrorHandler.handleError(e, getRootView()));
 * 
 * @author Senior Developer
 * @version 1.0
 */
public class FirebaseErrorHandler {
    
    private static final String TAG = "FirebaseErrorHandler";
    
    /**
     * Enumeration of error types returned by Firebase
     */
    public enum ErrorType {
        NETWORK_ERROR("Network connectivity issue"),
        PERMISSION_DENIED("Access denied"),
        NOT_FOUND("Data not found"),
        INVALID_DATA("Invalid data format"),
        AUTHENTICATION_FAILED("Authentication failed"),
        SERVER_ERROR("Server error"),
        TIMEOUT("Request timed out"),
        CONFLICT("Data conflict"),
        UNKNOWN("An unexpected error occurred");
        
        private final String description;
        
        ErrorType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Categorize a Firebase exception into an ErrorType
     * 
     * @param e The exception to categorize
     * @return ErrorType classification
     */
    public static ErrorType categorizeError(Exception e) {
        if (e == null) {
            return ErrorType.UNKNOWN;
        }
        
        // Check for authentication errors
        if (e instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) e;
            String code = authException.getErrorCode();
            if (code != null) {
                switch (code) {
                    case "ERROR_INVALID_CREDENTIALS":
                    case "ERROR_INVALID_EMAIL":
                    case "ERROR_WEAK_PASSWORD":
                        return ErrorType.AUTHENTICATION_FAILED;
                    case "ERROR_USER_NOT_FOUND":
                    case "ERROR_USER_DISABLED":
                        return ErrorType.AUTHENTICATION_FAILED;
                }
            }
            return ErrorType.AUTHENTICATION_FAILED;
        }
        
        // Check for network errors
        String errorMessage = e.getMessage();
        if (errorMessage != null) {
            if (errorMessage.contains("offline") || 
                errorMessage.contains("network") ||
                errorMessage.contains("unable to resolve")) {
                return ErrorType.NETWORK_ERROR;
            }
        }
        
        // Check for not found errors
        if (e instanceof FileNotFoundException) {
            return ErrorType.NOT_FOUND;
        }
        
        // Check for Firestore-specific errors
        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
            switch (firestoreException.getCode()) {
                case PERMISSION_DENIED:
                    return ErrorType.PERMISSION_DENIED;
                case UNAVAILABLE:
                case RESOURCE_EXHAUSTED:
                    return ErrorType.NETWORK_ERROR;
                case NOT_FOUND:
                    return ErrorType.NOT_FOUND;
                case INVALID_ARGUMENT:
                case FAILED_PRECONDITION:
                    return ErrorType.INVALID_DATA;
                case DEADLINE_EXCEEDED:
                    return ErrorType.TIMEOUT;
                case ALREADY_EXISTS:
                case ABORTED:
                    return ErrorType.CONFLICT;
                case INTERNAL:
                case DATA_LOSS:
                case UNKNOWN:
                case UNAUTHENTICATED:
                case OUT_OF_RANGE:
                case CANCELLED:
                default:
                    return ErrorType.SERVER_ERROR;
            }
        }
        
        return ErrorType.UNKNOWN;
    }
    
    /**
     * Get user-friendly error message for an error type
     * 
     * @param type The ErrorType
     * @return User-friendly message to display
     */
    public static String getUserMessage(ErrorType type) {
        switch (type) {
            case NETWORK_ERROR:
                return "Network error. Please check your connection and try again.";
            case PERMISSION_DENIED:
                return "You don't have permission to access this resource.";
            case NOT_FOUND:
                return "The requested data could not be found.";
            case INVALID_DATA:
                return "The data format is invalid. Please try again.";
            case AUTHENTICATION_FAILED:
                return "Authentication failed. Please log in again.";
            case SERVER_ERROR:
                return "Server error. Please try again later.";
            case TIMEOUT:
                return "Request timed out. Please check your connection and try again.";
            case CONFLICT:
                return "Data conflict. The resource may have been modified.";
            case UNKNOWN:
            default:
                return "An unexpected error occurred. Please try again.";
        }
    }
    
    /**
     * Get action-oriented text for retry/help buttons
     * 
     * @param type The ErrorType
     * @return Action button text
     */
    public static String getActionText(ErrorType type) {
        switch (type) {
            case NETWORK_ERROR:
                return "Retry";
            case PERMISSION_DENIED:
                return "Contact Support";
            case NOT_FOUND:
                return "Go Back";
            case TIMEOUT:
                return "Retry";
            case AUTHENTICATION_FAILED:
                return "Re-login";
            default:
                return "Dismiss";
        }
    }
    
    /**
     * Handle a Firebase error and display user message
     * 
     * @param e The exception to handle
     * @param rootView The view to show Snackbar on
     */
    public static void handleError(Exception e, View rootView) {
        if (rootView == null) {
            handleErrorLogging(e);
            return;
        }
        
        ErrorType type = categorizeError(e);
        String message = getUserMessage(type);
        
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        
        // Color code based on error severity
        int backgroundColor = getBackgroundColor(type);
        snackbar.setBackgroundTint(backgroundColor);
        
        snackbar.show();
        
        handleErrorLogging(e);
    }
    
    /**
     * Handle error with custom action button
     * 
     * @param e The exception
     * @param rootView The view to show Snackbar on
     * @param action Callback for action button click
     */
    public static void handleErrorWithAction(Exception e, View rootView, Runnable action) {
        if (rootView == null) {
            handleErrorLogging(e);
            return;
        }
        
        ErrorType type = categorizeError(e);
        String message = getUserMessage(type);
        String actionText = getActionText(type);
        
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getBackgroundColor(type));
        snackbar.setAction(actionText, v -> action.run());
        snackbar.show();
        
        handleErrorLogging(e);
    }
    
    /**
     * Log error to console and analytics
     * 
     * @param e The exception to log
     */
    private static void handleErrorLogging(Exception e) {
        if (e == null) {
            return;
        }
        
        ErrorType type = categorizeError(e);
        String message = String.format("[%s] %s - %s", 
            type.name(), 
            type.getDescription(),
            e.getMessage());
        
        Log.e(TAG, message, e);
        
        // TODO: Send to analytics/crash reporting service
        // FirebaseCrashlytics.getInstance().recordException(e);
    }
    
    /**
     * Get background color for Snackbar based on error type
     * 
     * @param type The ErrorType
     * @return Color integer for background
     */
    private static int getBackgroundColor(ErrorType type) {
        switch (type) {
            case NETWORK_ERROR:
            case TIMEOUT:
                return android.graphics.Color.parseColor("#FF9800");  // Orange
            case PERMISSION_DENIED:
            case AUTHENTICATION_FAILED:
                return android.graphics.Color.parseColor("#F44336");  // Red
            case NOT_FOUND:
                return android.graphics.Color.parseColor("#9C27B0");  // Purple
            case SERVER_ERROR:
            case UNKNOWN:
                return android.graphics.Color.parseColor("#F44336");  // Red
            default:
                return android.graphics.Color.parseColor("#616161");  // Gray
        }
    }
    
    /**
     * Check if error is retryable
     * 
     * @param e The exception
     * @return true if operation should be retried
     */
    public static boolean isRetryable(Exception e) {
        ErrorType type = categorizeError(e);
        switch (type) {
            case NETWORK_ERROR:
            case TIMEOUT:
            case SERVER_ERROR:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Calculate retry delay with exponential backoff
     * 
     * @param attemptNumber The retry attempt number (1st, 2nd, 3rd attempt)
     * @return Delay in milliseconds
     */
    public static long calculateBackoffDelay(int attemptNumber) {
        // Formula: 2^attemptNumber * 1000ms, capped at 30 seconds
        long delay = (long) Math.pow(2, attemptNumber) * 1000;
        return Math.min(delay, 30000);  // Max 30 seconds
    }
}
