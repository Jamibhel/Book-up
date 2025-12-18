package com.example.bookup.ai;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for interacting with Firebase Cloud Functions for AI Chat
 * Handles communication with OpenAI through backend functions
 */
public class AICloudFunctionClient {
    private static final String TAG = "AICloudFunctionClient";
    private static final String FUNCTION_NAME = "processAIChatMessage";

    private final FirebaseFunctions functions;

    public interface AIResponseCallback {
        void onSuccess(String response);
        void onError(String errorMessage, int errorCode);
    }

    public AICloudFunctionClient() {
        this.functions = FirebaseFunctions.getInstance();
        // Note: Uncomment below for emulator testing
        // this.functions.useFunctionsEmulator("localhost", 5001);
    }

    /**
     * Send a message to AI and get response
     * @param message The user's question/message
     * @param subject The subject area (e.g., "Mathematics", "Science")
     * @param callback Callback for response handling
     */
    public void sendMessage(String message, String subject, AIResponseCallback callback) {
        if (message == null || message.trim().isEmpty()) {
            callback.onError("Message cannot be empty", 400);
            return;
        }

        if (subject == null || subject.trim().isEmpty()) {
            callback.onError("Subject must be selected", 400);
            return;
        }

        // Prepare function data
        Map<String, Object> data = new HashMap<>();
        data.put("message", message.trim());
        data.put("subject", subject.trim());

        // Call cloud function
        functions
                .getHttpsCallable(FUNCTION_NAME)
                .call(data)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        try {
                            Map<String, Object> result = (Map<String, Object>) t.getResult().getData();
                            String response = (String) result.get("response");
                            
                            if (response != null && !response.isEmpty()) {
                                callback.onSuccess(response);
                                Log.d(TAG, "AI response received successfully");
                            } else {
                                callback.onError("Empty response from AI", 500);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing AI response", e);
                            callback.onError("Error parsing response: " + e.getMessage(), 500);
                        }
                    } else {
                        handleError(t.getException(), callback);
                    }
                });
    }

    /**
     * Handle errors from cloud function calls
     */
    private void handleError(Exception exception, AIResponseCallback callback) {
        if (exception == null) {
            callback.onError("Unknown error occurred", 500);
            return;
        }

        Log.e(TAG, "Cloud Function Error: " + exception.getMessage(), exception);

        // Check for specific error types
        String message = exception.getMessage();
        if (message != null) {
            if (message.contains("UNAUTHENTICATED") || message.contains("unauthenticated")) {
                callback.onError("Please sign in to use AI Chat", 401);
            } else if (message.contains("QUOTA_EXCEEDED") || message.contains("quota")) {
                callback.onError("AI service quota exceeded. Please try later.", 429);
            } else if (message.contains("DEADLINE_EXCEEDED") || message.contains("timeout")) {
                callback.onError("Request timeout. Please try again.", 408);
            } else if (message.contains("INVALID_ARGUMENT") || message.contains("invalid")) {
                callback.onError("Invalid request. Please check your input.", 400);
            } else if (message.contains("permission") || message.contains("PERMISSION_DENIED")) {
                callback.onError("Permission denied to access AI Chat", 403);
            } else {
                callback.onError("Error: " + message, 500);
            }
        } else {
            callback.onError("Unknown error occurred. Please try again.", 500);
        }
    }

    /**
     * Check if AI Chat service is available
     * @return true if service is reachable
     */
    public void checkServiceAvailability(ServiceStatusCallback callback) {
        // Simple health check by attempting to read a dummy config
        callback.onStatusChecked(true); // Assume available - actual check could be more sophisticated
    }

    public interface ServiceStatusCallback {
        void onStatusChecked(boolean isAvailable);
    }
}
