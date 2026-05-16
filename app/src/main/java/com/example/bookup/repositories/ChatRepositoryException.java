package com.example.bookup.repositories;

import android.util.Log;

/**
 * Custom exception for Chat Repository operations with user-friendly error messages.
 * Provides specific error types and helpful messages for different failure scenarios.
 */
public class ChatRepositoryException extends Exception {
    private static final String TAG = "ChatRepositoryException";
    
    /**
     * Enum of error types with user-friendly messages
     */
    public enum ErrorType {
        NETWORK_ERROR("Network Error", "Check your internet connection"),
        PERMISSION_DENIED("Permission Denied", "You don't have permission to access this chat"),
        INVALID_DATA("Invalid Data", "The message data is invalid"),
        QUERY_FAILED("Query Failed", "Failed to load messages. Please try again"),
        UPLOAD_FAILED("Upload Failed", "Failed to upload file. Try a smaller file"),
        TIMEOUT("Request Timeout", "Request took too long. Please try again"),
        FILE_TOO_LARGE("File Too Large", "File size exceeds 50MB limit"),
        UNSUPPORTED_FORMAT("Unsupported Format", "This file format is not supported"),
        STORAGE_ERROR("Storage Error", "Unable to access device storage"),
        AUTH_ERROR("Authentication Error", "Please sign in again"),
        SYNC_ERROR("Sync Error", "Failed to sync messages"),
        CONVERSATION_NOT_FOUND("Not Found", "This conversation no longer exists"),
        UNKNOWN("Unknown Error", "An unexpected error occurred");
        
        public final String title;
        public final String message;
        
        ErrorType(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }
    
    public ErrorType errorType;
    public String userMessage;
    public String technicalDetails;
    public int httpStatusCode;
    
    /**
     * Constructor with just error type
     */
    public ChatRepositoryException(ErrorType type) {
        super(type.title + ": " + type.message);
        this.errorType = type;
        this.userMessage = type.message;
        this.technicalDetails = "";
        this.httpStatusCode = 0;
        logError();
    }
    
    /**
     * Constructor with error type and additional details
     */
    public ChatRepositoryException(ErrorType type, String details) {
        super(type.title + ": " + type.message + " (" + details + ")");
        this.errorType = type;
        this.userMessage = type.message;
        this.technicalDetails = details;
        this.httpStatusCode = 0;
        logError();
    }
    
    /**
     * Constructor with error type, message, and HTTP status code
     */
    public ChatRepositoryException(ErrorType type, String details, int statusCode) {
        super(type.title + ": " + type.message + " (" + details + ")");
        this.errorType = type;
        this.userMessage = type.message;
        this.technicalDetails = details;
        this.httpStatusCode = statusCode;
        logError();
    }
    
    /**
     * Convert common exceptions to ChatRepositoryException
     * Maps generic exceptions to specific ChatRepositoryException types
     */
    public static ChatRepositoryException from(Exception e) {
        if (e instanceof ChatRepositoryException) {
            return (ChatRepositoryException) e;
        }
        
        String message = e.getMessage() != null ? e.getMessage() : e.toString();
        
        // Network errors
        if (message.contains("Network") || message.contains("UNAVAILABLE") 
            || message.contains("No internet") || message.contains("timeout")) {
            return new ChatRepositoryException(ErrorType.NETWORK_ERROR, message);
        }
        
        // Permission errors
        if (message.contains("Permission") || message.contains("PERMISSION_DENIED")
            || message.contains("Access denied")) {
            return new ChatRepositoryException(ErrorType.PERMISSION_DENIED, message);
        }
        
        // Timeout errors
        if (message.contains("timeout") || message.contains("Timeout")
            || message.contains("DEADLINE_EXCEEDED")) {
            return new ChatRepositoryException(ErrorType.TIMEOUT, message);
        }
        
        // Upload/Storage errors
        if (message.contains("upload") || message.contains("Upload")
            || message.contains("Storage") || message.contains("storage")) {
            return new ChatRepositoryException(ErrorType.UPLOAD_FAILED, message);
        }
        
        // Query errors
        if (message.contains("query") || message.contains("Query")
            || message.contains("FAILED_PRECONDITION")) {
            return new ChatRepositoryException(ErrorType.QUERY_FAILED, message);
        }
        
        // Auth errors
        if (message.contains("auth") || message.contains("Auth")
            || message.contains("UNAUTHENTICATED")) {
            return new ChatRepositoryException(ErrorType.AUTH_ERROR, message);
        }
        
        // Sync errors
        if (message.contains("sync") || message.contains("Sync")) {
            return new ChatRepositoryException(ErrorType.SYNC_ERROR, message);
        }
        
        // Default
        return new ChatRepositoryException(ErrorType.UNKNOWN, message);
    }
    
    /**
     * Get user-facing error message (short, clear)
     * Safe to display directly in UI
     */
    public String getUserMessage() {
        return userMessage;
    }
    
    /**
     * Get error title for dialog/alert
     */
    public String getErrorTitle() {
        return errorType.title;
    }
    
    /**
     * Check if this is a retryable error
     * Errors that should show a "Retry" button
     */
    public boolean isRetryable() {
        return errorType == ErrorType.NETWORK_ERROR ||
               errorType == ErrorType.TIMEOUT ||
               errorType == ErrorType.QUERY_FAILED ||
               errorType == ErrorType.UPLOAD_FAILED ||
               errorType == ErrorType.SYNC_ERROR;
    }
    
    /**
     * Check if user needs to take action
     * Errors that require user intervention
     */
    public boolean requiresUserAction() {
        return errorType == ErrorType.PERMISSION_DENIED ||
               errorType == ErrorType.AUTH_ERROR ||
               errorType == ErrorType.FILE_TOO_LARGE ||
               errorType == ErrorType.UNSUPPORTED_FORMAT ||
               errorType == ErrorType.STORAGE_ERROR;
    }
    
    /**
     * Log error with appropriate level based on severity
     */
    private void logError() {
        if (requiresUserAction()) {
            Log.w(TAG, "⚠️ User action required: " + getMessage());
        } else if (isRetryable()) {
            Log.d(TAG, "♻️ Retryable error: " + getMessage());
        } else {
            Log.e(TAG, "❌ Error: " + getMessage());
        }
    }
}
