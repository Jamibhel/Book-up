package com.example.bookup;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.models.ChatChannel;
import com.example.bookup.models.ChatMessage;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    public static final String EXTRA_CHAT_CHANNEL_ID = "extra_chat_channel_id";
    public static final String EXTRA_OTHER_USER_ID = "extra_other_user_id";
    public static final String EXTRA_OTHER_USER_NAME = "extra_other_user_name";
    public static final String EXTRA_IS_GROUP_CHAT = "extra_is_group_chat"; // For future group chat support

    // UI Elements
    private RecyclerView recyclerChatMessages;
    private TextInputEditText editTextMessageInput;
    private ImageButton buttonSendMessage;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration chatMessagesListener;

    // Chat Data
    private String chatChannelId;
    private String otherUserId;
    private String otherUserName;
    private boolean isGroupChat = false; // For now, default to false
    private String currentUserName; // Current user's display name
    private Map<String, String> participantNames = new HashMap<>(); // All participant names for this channel

    // Adapter and Data
    private MessageAdapter messageAdapter;
    private List<ChatMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to chat.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Get chat channel info from Intent
        chatChannelId = getIntent().getStringExtra(EXTRA_CHAT_CHANNEL_ID);
        otherUserId = getIntent().getStringExtra(EXTRA_OTHER_USER_ID);
        otherUserName = getIntent().getStringExtra(EXTRA_OTHER_USER_NAME);
        isGroupChat = getIntent().getBooleanExtra(EXTRA_IS_GROUP_CHAT, false); // Default to false

        if (chatChannelId == null && otherUserId == null) {
            Toast.makeText(this, "Chat details missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        // Fetch current user's display name once
        fetchCurrentUserName();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null && chatChannelId != null) {
            listenForMessages();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (chatMessagesListener != null) {
            chatMessagesListener.remove(); // Stop listening for messages
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        recyclerChatMessages = findViewById(R.id.recycler_chat_messages);
        editTextMessageInput = findViewById(R.id.edit_text_message_input);
        buttonSendMessage = findViewById(R.id.button_send_message);
        progressBar = findViewById(R.id.progress_bar_chat);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(otherUserName); // Set toolbar title to other participant's name
        }
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        // Pass isGroupChat to adapter to determine if sender names should be shown
        messageAdapter = new MessageAdapter(messageList, currentUser.getUid(), isGroupChat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Important: stackFromEnd for chat apps
        layoutManager.setStackFromEnd(true);
        recyclerChatMessages.setLayoutManager(layoutManager);
        recyclerChatMessages.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        buttonSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void fetchCurrentUserName() {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            currentUserName = name;
                        } else {
                            currentUserName = "You"; // Fallback
                        }
                    } else {
                        currentUserName = "You"; // Fallback
                    }
                    // Add current user to participant names map
                    participantNames.put(currentUser.getUid(), currentUserName);
                    // Add other user to participant names map (if not already there)
                    if (otherUserId != null && otherUserName != null && !participantNames.containsKey(otherUserId)) {
                        participantNames.put(otherUserId, otherUserName);
                    }

                    // After getting current user's name, fetch initial channel data if not existing
                    if (chatChannelId == null) {
                        findOrCreateChatChannel();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching current user name: " + e.getMessage());
                    currentUserName = "You"; // Fallback on error
                    // Add current user to participant names map
                    participantNames.put(currentUser.getUid(), currentUserName);
                    // Add other user to participant names map (if not already there)
                    if (otherUserId != null && otherUserName != null && !participantNames.containsKey(otherUserId)) {
                        participantNames.put(otherUserId, otherUserName);
                    }
                    if (chatChannelId == null) {
                        findOrCreateChatChannel();
                    }
                });
    }

    private void findOrCreateChatChannel() {
        // This method is called if chatChannelId is null, meaning a new 1-to-1 chat is initiated
        // We need to check if a channel already exists between these two users
        setLoading(true);

        List<String> participants = new ArrayList<>();
        participants.add(currentUser.getUid());
        participants.add(otherUserId);

        // Firestore query to find an existing channel between these two specific participants
        // This works reliably for 1-to-1 chats as participantIds array will have the same two UIDs
        db.collection("chatChannels")
                .whereArrayContains("participantIds", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String foundChannelId = null;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ChatChannel channel = doc.toObject(ChatChannel.class);
                        if (channel.getParticipantIds().size() == 2 && channel.getParticipantIds().contains(otherUserId)) {
                            foundChannelId = doc.getId();
                            break;
                        }
                    }

                    if (foundChannelId != null) {
                        chatChannelId = foundChannelId;
                        Log.d(TAG, "Found existing chat channel: " + chatChannelId);
                        listenForMessages();
                    } else {
                        // No existing channel, create a new one
                        Log.d(TAG, "No existing channel, creating new one with " + otherUserId);
                        createChatChannel(participants);
                    }
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding chat channel: " + e.getMessage());
                    Toast.makeText(this, "Failed to find/create chat. Try again.", Toast.LENGTH_LONG).show();
                    setLoading(false);
                });
    }

    private void createChatChannel(List<String> participants) {
        DocumentReference newChannelRef = db.collection("chatChannels").document();
        chatChannelId = newChannelRef.getId();

        ChatChannel newChannel = new ChatChannel();
        newChannel.setParticipantIds(participants);
        newChannel.setParticipantNames(participantNames); // Store names for easy display
        newChannel.setLastMessage("Say hello!");
        newChannel.setLastMessageTimestamp(new Date()); // Current time
        newChannel.setIsGroupChat(false); // For 1-to-1 chat

        newChannelRef.set(newChannel)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New chat channel created: " + chatChannelId);
                    listenForMessages();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating chat channel: " + e.getMessage());
                    Toast.makeText(this, "Failed to create chat channel.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }


    private void listenForMessages() {
        if (chatChannelId == null) {
            Log.e(TAG, "Cannot listen for messages: chatChannelId is null.");
            return;
        }

        setLoading(true);
        CollectionReference messagesRef = db.collection("chatChannels").document(chatChannelId).collection("messages");

        chatMessagesListener = messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        setLoading(false);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        messageList.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            if (message != null) {
                                messageList.add(message);
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                        recyclerChatMessages.scrollToPosition(messageList.size() - 1); // Scroll to last message
                        setLoading(false);
                    } else {
                        Log.d(TAG, "Current chat has no messages.");
                        messageList.clear();
                        messageAdapter.notifyDataSetChanged();
                        setLoading(false);
                    }
                });
    }

    private void sendMessage() {
        String messageText = editTextMessageInput.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (chatChannelId == null) {
            Toast.makeText(this, "Chat not ready. Please wait or try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextMessageInput.getWindowToken(), 0);

        setLoading(true);

        DocumentReference messageRef = db.collection("chatChannels").document(chatChannelId).collection("messages").document();
        DocumentReference channelRef = db.collection("chatChannels").document(chatChannelId);

        // Create ChatMessage object
        ChatMessage chatMessage = new ChatMessage(
                currentUser.getUid(),
                currentUserName, // Use the fetched currentUserName
                messageText,
                new Date(),
                false // Not yet read by other participant
        );

        // Use a Firestore WriteBatch to update both the message and the chat channel atomically
        WriteBatch batch = db.batch();
        batch.set(messageRef, chatMessage); // Add new message
        // Update lastMessage and lastMessageTimestamp in the parent ChatChannel
        batch.update(channelRef,
                "lastMessage", messageText,
                "lastMessageTimestamp", FieldValue.serverTimestamp()
        );

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Message sent and channel updated successfully.");
                    editTextMessageInput.setText(""); // Clear input field
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message: " + e.getMessage());
                    Toast.makeText(ChatActivity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false);
                });
    }


    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonSendMessage.setEnabled(!isLoading);
        editTextMessageInput.setEnabled(!isLoading);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
    }
}
