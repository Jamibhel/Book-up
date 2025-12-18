package com.example.bookup.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookup.R;
import com.example.bookup.adapters.AIChatAdapter;
import com.example.bookup.ai.AIChatMessage;
import com.example.bookup.ai.AICloudFunctionClient;
import com.example.bookup.utils.FirebaseErrorHandler;
import com.example.bookup.utils.NetworkConnectivityManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for AI-powered tutoring chat
 * Allows students to ask questions about various subjects
 * Responses powered by OpenAI GPT-4 via Firebase Cloud Functions
 */
public class AIChatFragment extends Fragment {

    private static final String TAG = "AIChatFragment";
    private static final int MESSAGES_PAGE_SIZE = 50;
    private static final String AI_CHAT_COLLECTION = "ai_chat_messages";

    // UI Components
    private RecyclerView messagesRecyclerView;
    private AIChatAdapter chatAdapter;
    private EditText messageInput;
    private ImageButton sendButton;
    private Spinner subjectSpinner;
    private MaterialButton clearButton;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyStateLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateText;

    // Firebase and AI
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private AICloudFunctionClient aiClient;
    private FirebaseErrorHandler errorHandler;
    private NetworkConnectivityManager connectivityManager;

    // State
    private List<AIChatMessage> messages = new ArrayList<>();
    private String currentSubject = "General";
    private boolean isLoadingResponse = false;
    private String currentUserId;

    public AIChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        aiClient = new AICloudFunctionClient();
        errorHandler = new FirebaseErrorHandler();
        connectivityManager = new NetworkConnectivityManager(getContext());

        FirebaseUser currentUser = auth.getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getUid() : "";
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        initializeViews(view);
        loadPreviousMessages();
        setupListeners();
        setupRecyclerView();
    }

    private void initializeViews(View view) {
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        subjectSpinner = view.findViewById(R.id.subjectSpinner);
        clearButton = view.findViewById(R.id.clearButton);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true); // Show latest messages at bottom
        messagesRecyclerView.setLayoutManager(layoutManager);

        chatAdapter = new AIChatAdapter(messages, getContext());
        messagesRecyclerView.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        clearButton.setOnClickListener(v -> clearConversation());

        subjectSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                currentSubject = parent.getItemAtPosition(position).toString();
                // Clear messages when subject changes
                clearConversation();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            messages.clear();
            loadPreviousMessages();
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user message to chat
        AIChatMessage userMessage = new AIChatMessage(
                currentUserId,
                currentSubject,
                message,
                AIChatMessage.ROLE_USER
        );
        messages.add(0, userMessage); // Add to top (reversed layout)
        chatAdapter.notifyItemInserted(0);
        saveMessageToFirestore(userMessage);

        // Clear input
        messageInput.setText("");
        messagesRecyclerView.scrollToPosition(0);

        // Show loading state
        isLoadingResponse = true;
        loadingIndicator.setVisibility(View.VISIBLE);

        // Send to AI
        aiClient.sendMessage(message, currentSubject, new AICloudFunctionClient.AIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                handleAIResponse(response);
            }

            @Override
            public void onError(String errorMessage, int errorCode) {
                handleAIError(errorMessage, errorCode);
            }
        });
    }

    private void handleAIResponse(String aiResponse) {
        isLoadingResponse = false;
        loadingIndicator.setVisibility(View.GONE);

        // Create AI message
        AIChatMessage aiMessage = new AIChatMessage(
                currentUserId,
                currentSubject,
                aiResponse,
                AIChatMessage.ROLE_AI
        );
        aiMessage.setMarkdown(true); // AI responses use markdown

        messages.add(0, aiMessage); // Add to top (reversed layout)
        chatAdapter.notifyItemInserted(0);
        saveMessageToFirestore(aiMessage);

        messagesRecyclerView.scrollToPosition(0);
        updateEmptyState();
    }

    private void handleAIError(String errorMessage, int errorCode) {
        isLoadingResponse = false;
        loadingIndicator.setVisibility(View.GONE);

        Log.e(TAG, "AI Error: " + errorMessage + " (Code: " + errorCode + ")");
        
        // Show appropriate error messages
        String displayMessage = errorMessage;
        if (!isNetworkConnected()) {
            displayMessage = "No internet connection. Please check your network.";
        }

        Snackbar.make(messagesRecyclerView, displayMessage, Snackbar.LENGTH_LONG).show();
    }

    private void loadPreviousMessages() {
        if (currentUserId.isEmpty()) {
            return;
        }

        db.collection(AI_CHAT_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("subject", currentSubject)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MESSAGES_PAGE_SIZE)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    messages.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        AIChatMessage msg = document.toObject(AIChatMessage.class);
                        msg.setMessageId(document.getId());
                        messages.add(0, msg); // Add in reverse order
                    }
                    chatAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    swipeRefreshLayout.setRefreshing(false);
                    Log.d(TAG, "Loaded " + messages.size() + " previous messages");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading messages", e);
                    swipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(messagesRecyclerView, "Failed to load messages", Snackbar.LENGTH_SHORT).show();
                });
    }

    private void saveMessageToFirestore(AIChatMessage message) {
        db.collection(AI_CHAT_COLLECTION)
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    message.setMessageId(documentReference.getId());
                    Log.d(TAG, "Message saved: " + message.getMessageId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving message", e);
                    Toast.makeText(getContext(), "Failed to save message", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearConversation() {
        if (messages.isEmpty()) {
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Clear Conversation")
                .setMessage("Are you sure you want to clear this conversation? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    messages.clear();
                    chatAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    // Note: In production, you might also delete from Firestore
                    // For now, we just clear the local list
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmptyState() {
        if (messages.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.GONE);
            
            // Set welcome message with examples
            String welcomeMessage = "Welcome to AI Tutor! 👋\n\n" +
                    "Ask me anything about " + currentSubject + "\n\n" +
                    "📝 Try asking:\n" +
                    "• Explain [topic] simply\n" +
                    "• Give me an example of...\n" +
                    "• What is the difference between...?\n" +
                    "• How do I solve this problem?";
            
            emptyStateText.setText(welcomeMessage);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            messagesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkConnected() {
        return connectivityManager != null && connectivityManager.isOnline();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (connectivityManager != null) {
            connectivityManager.startMonitoring(this::onNetworkStateChanged);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (connectivityManager != null) {
            connectivityManager.stopMonitoring();
        }
    }

    private void onNetworkStateChanged(boolean isConnected, String status) {
        Log.d(TAG, "Network state: " + (isConnected ? "CONNECTED" : "OFFLINE"));
        sendButton.setEnabled(isConnected && !isLoadingResponse);
    }
}
