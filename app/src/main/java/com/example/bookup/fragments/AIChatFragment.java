package com.example.bookup.fragments;

// Android imports
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

// AndroidX imports
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Material Design imports
import com.example.bookup.helpers.MarkdownHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

// Firebase imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

// App imports
import com.example.bookup.R;
import com.example.bookup.adapters.AIChatAdapter;
import com.example.bookup.models.AIChatMessage;

// Java imports
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for handling AI-powered chat functionality.
 * This fragment provides a chat interface where users can interact with an AI,
 * sending messages and receiving responses in a chat-like format.
 */
public class AIChatFragment extends Fragment {
    private RecyclerView chatRecyclerView;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private AIChatAdapter chatAdapter;
    private List<AIChatMessage> messageList;
    private FirebaseFirestore db;
    private String userId;
    private final String currentSubject = "General"; // Default subject

    /**
     * Required empty public constructor
     */
    public AIChatFragment() {
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return Return the View for the fragment's UI, or null
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        MarkdownHelper markdownHelper = new MarkdownHelper();
        chatAdapter = new AIChatAdapter(messageList, markdownHelper);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        // Send button click listener
        sendButton.setOnClickListener(v -> sendMessage());

        // Load existing messages
        loadMessages();

        return view;
    }

    /**
     * Sends the message currently in the message input field.
     * If the message is not empty, creates a new message object
     * and saves it to Firestore, then generates an AI response.
     */
    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        // Create and save user message
        AIChatMessage userMessage = new AIChatMessage(message, false, currentSubject, userId);
        saveMessage(userMessage);

        // Clear input
        messageInput.setText("");

        // Create AI response
        generateAIResponse(message);
    }

    /**
     * Generates an AI response to the user's message.
     * Currently returns a placeholder response, but will be updated
     * with actual AI integration in the future.
     *
     * @param prompt The user's message to respond to
     */
    private void generateAIResponse(String prompt) {
        // TODO: Implement actual AI integration
        AIChatMessage aiMessage = new AIChatMessage(
            "This is a placeholder AI response. The actual AI integration will be implemented soon.", 
            true, 
            currentSubject, 
            "AI"
        );
        saveMessage(aiMessage);
    }

    /**
     * Saves a message to Firestore and updates the UI.
     * On success, adds the message to the local list and updates the RecyclerView.
     * On failure, shows a toast message to the user.
     *
     * @param message The message to save
     */
    private void saveMessage(AIChatMessage message) {
        db.collection("ai_chat_messages")
            .add(message)
            .addOnSuccessListener(documentReference -> {
                message.setId(documentReference.getId());
                messageList.add(message);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
            })
            .addOnFailureListener(e -> 
                Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
            );
    }

    /**
     * Loads all messages for the current user from Firestore.
     * Messages are ordered by timestamp and filtered by userId.
     * On success, updates the RecyclerView with the loaded messages.
     * On failure, shows a toast message to the user.
     */
    private void loadMessages() {
        db.collection("ai_chat_messages")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                messageList.clear();
                messageList.addAll(queryDocumentSnapshots.toObjects(AIChatMessage.class));
                chatAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(requireContext(), "Failed to load messages", Toast.LENGTH_SHORT).show()
            );
    }
}
