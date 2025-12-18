package com.example.bookup.activities;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.bookup.R;
import com.example.bookup.adapters.MessageAdapter;
import com.example.bookup.adapters.UserSearchAdapter;
import com.example.bookup.base.BaseSecureActivity;
import com.example.bookup.models.ChatChannel;
import com.example.bookup.models.ChatMessage;
import com.example.bookup.models.UserSearchItem;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends BaseSecureActivity {

    private static final String TAG = "ChatActivity";
    private static final int PAGE_SIZE = 20;
    private static final int LOAD_MORE_THRESHOLD = 3;
    private static final long TYPING_TIMEOUT = 1000L; // 1 second timeout for typing indicator

    public static final String EXTRA_CHAT_CHANNEL_ID = "extra_chat_channel_id";
    public static final String EXTRA_OTHER_USER_ID = "extra_other_user_id";
    public static final String EXTRA_OTHER_USER_NAME = "extra_other_user_name";
    public static final String EXTRA_IS_GROUP_CHAT = "extra_is_group_chat"; // For future group chat support

    private boolean isLoadingMore = false;
    private boolean hasMoreMessages = true;
    private DocumentSnapshot lastVisible;
    private long lastTypingTime = 0;

    // UI Elements
    private RecyclerView recyclerChatMessages;
    private TextInputEditText editTextMessageInput;
    private ImageButton buttonSendMessage;
    private ImageButton buttonSendImage;
    private ImageButton buttonSendVideo;
    private ImageButton buttonSendAudio;
    private ProgressBar progressBar;
    
    // Media-related fields
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> pickVideoLauncher;
    private StorageReference storageReference;
    
    // Audio recording fields
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;
    private long recordingStartTime;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration chatMessagesListener;
    private ListenerRegistration typingListener;

    // Chat Data
    private String chatChannelId;
    private String otherUserId;
    private String otherUserName;
    private boolean isGroupChat = false;
    private String currentUserName;
    private Map<String, String> participantNames = new HashMap<>();
    private Map<String, Boolean> typingUsers = new HashMap<>();

    // Adapter and Data
    private MessageAdapter messageAdapter;
    private List<ChatMessage> messageList;

    private final Runnable typingTimeout = new Runnable() {
        @Override
        public void run() {
            updateTypingStatus(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        currentUser = mAuth.getCurrentUser(); // ensure mAuth exists in BaseSecureActivity
        
        // Initialize media pickers
        initializeMediaLaunchers();

        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        // Get chat channel info from Intent
        chatChannelId = getIntent().getStringExtra(EXTRA_CHAT_CHANNEL_ID);
        otherUserId = getIntent().getStringExtra(EXTRA_OTHER_USER_ID);
        otherUserName = getIntent().getStringExtra(EXTRA_OTHER_USER_NAME);
        isGroupChat = getIntent().getBooleanExtra(EXTRA_IS_GROUP_CHAT, false);

        if (chatChannelId == null && otherUserId == null) {
            showError("Error", "Chat details missing");
            finish();
            return;
        }

        // Initialize views and setup UI components
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        // Typing listener will be started in onStart if chatChannelId exists.
        verifyChatAccess(); // left empty intentionally in original - keep for future checks

        // Fetch current user's display name once
        fetchCurrentUserName();

        // Initial empty state check
        updateEmptyState();

        // Start listening for messages if we have a channel ID
        if (chatChannelId != null) {
            listenForMessages();
            listenForTypingStatus();
        }
    }

    private void verifyChatAccess() {
        // Implement access/permission checks if needed; left intentionally blank
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null && chatChannelId != null) {
            listenForMessages();
            listenForTypingStatus();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (chatMessagesListener != null) {
            chatMessagesListener.remove();
            chatMessagesListener = null;
        }
        if (typingListener != null) {
            typingListener.remove();
            typingListener = null;
        }
        // Set user as not typing when leaving
        updateTypingStatus(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (editTextMessageInput != null) {
            editTextMessageInput.removeCallbacks(typingTimeout);
        }
        // Clean up audio recorder if still recording
        if (mediaRecorder != null) {
            try {
                if (isRecording) {
                    mediaRecorder.stop();
                }
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaRecorder: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new_chat) {
            showNewChatDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showNewChatDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_chat_user, null);
        TextInputEditText userSearchInput = dialogView.findViewById(R.id.edit_text_user_search);
        RecyclerView userList = dialogView.findViewById(R.id.recycler_user_list);
        ProgressBar searchProgressBar = dialogView.findViewById(R.id.progress_bar_user_search);

        // Set up user list RecyclerView
        UserSearchAdapter adapter = new UserSearchAdapter();
        userList.setLayoutManager(new LinearLayoutManager(this));
        userList.setAdapter(adapter);

        // Set up user search
        userSearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 3) {
                    searchUsers(query, adapter, searchProgressBar);
                } else {
                    adapter.submitList(new ArrayList<>());
                }
            }
        });

        // Build dialog and keep a reference so we can dismiss it inside the adapter click
        builder.setTitle(R.string.new_chat_dialog_title)
                .setView(dialogView)
                .setNegativeButton(R.string.button_cancel, null);

        AlertDialog dialog = builder.show();

        // Set up click listener for user selection
        adapter.setOnUserClickListener(user -> {
            // start new chat activity and dismiss dialog
            startNewChat(user.getUserId(), user.getDisplayName());
            dialog.dismiss();
        });
    }

    private void searchUsers(String query, UserSearchAdapter adapter, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users")
                .orderBy("displayName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserSearchItem> users = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId();
                        if (!userId.equals(currentUser.getUid())) {
                            String displayName = document.getString("displayName");
                            String photoUrl = document.getString("photoUrl");
                            users.add(new UserSearchItem(userId, displayName, photoUrl));
                        }
                    }
                    adapter.submitList(users);
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showError("Search Error", e.getMessage());
                });
    }

    private void startNewChat(String otherUserId, String otherUserName) {
        String channelId = createChatChannelId(currentUser.getUid(), otherUserId);

        // Check if chat channel already exists (normalized collection name "chatChannels")
        db.collection("chatChannels")
                .document(channelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Create new chat channel
                        createNewChatChannel(channelId, otherUserId, otherUserName);
                    }
                    // Start chat activity
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra(EXTRA_CHAT_CHANNEL_ID, channelId);
                    intent.putExtra(EXTRA_OTHER_USER_ID, otherUserId);
                    intent.putExtra(EXTRA_OTHER_USER_NAME, otherUserName);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> showError("Error", e.getMessage()));
    }

    private String createChatChannelId(String uid1, String uid2) {
        // Create a consistent channel ID by sorting user IDs
        String[] sortedIds = new String[]{uid1, uid2};
        Arrays.sort(sortedIds);
        return sortedIds[0] + "_" + sortedIds[1];
    }

    private void createNewChatChannel(String channelId, String otherUserId, String otherUserName) {
        ChatChannel channel = new ChatChannel();
        channel.setChannelId(channelId);
        channel.setParticipantIds(Arrays.asList(currentUser.getUid(), otherUserId));
        Map<String, String> names = new HashMap<>();
        names.put(currentUser.getUid(), currentUserName != null ? currentUserName : "You");
        names.put(otherUserId, otherUserName);
        channel.setParticipantNames(names);
        channel.setLastMessageTimestamp(new Date());

        db.collection("chatChannels")
                .document(channelId)
                .set(channel)
                .addOnFailureListener(e -> showError("Error", e.getMessage()));
    }

    private void initViews() {
        recyclerChatMessages = findViewById(R.id.recycler_chat_messages);
        editTextMessageInput = findViewById(R.id.edit_text_message_input);
        buttonSendMessage = findViewById(R.id.button_send_message);
        buttonSendImage = findViewById(R.id.button_send_image);
        buttonSendVideo = findViewById(R.id.button_send_video);
        buttonSendAudio = findViewById(R.id.button_send_audio);
        progressBar = findViewById(R.id.progress_bar_chat);

        View emptyStateLayout = findViewById(R.id.layout_empty_state);
        if (emptyStateLayout != null) {
            TextView titleView = emptyStateLayout.findViewById(R.id.text_empty_chat_title);
            TextView descriptionView = emptyStateLayout.findViewById(R.id.text_empty_chat_description);

            if (titleView != null && descriptionView != null) {
                titleView.setText(R.string.empty_chat_title);
                descriptionView.setText(R.string.empty_chat_description);
            }
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(otherUserName != null ? otherUserName : getString(R.string.app_name));
        }

        // Make toolbar title clickable to view tutor profile
        toolbar.setOnClickListener(v -> {
            if (otherUserId != null && !isGroupChat) {
                openTutorProfile();
            }
        });
    }

    private void openTutorProfile() {
        Intent intent = new Intent(ChatActivity.this, TutorDetailsActivity.class);
        intent.putExtra("tutorId", otherUserId);
        startActivity(intent);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUser.getUid(), isGroupChat);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChatMessages.setLayoutManager(layoutManager);
        recyclerChatMessages.setAdapter(messageAdapter);

        // Setup empty state observer using fully qualified AdapterDataObserver
        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                updateEmptyState();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                updateEmptyState();
                // If the newly inserted items made the list start from 0, scroll to bottom
                if (messageList.size() == itemCount) {
                    recyclerChatMessages.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                updateEmptyState();
            }
        });

        // Add scroll listener for pagination
        recyclerChatMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (!isLoadingMore && hasMoreMessages && firstVisibleItem <= LOAD_MORE_THRESHOLD) {
                    loadMessages();
                }
            }
        });

        // Typing status update on text change
        editTextMessageInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTypingStatus(true);
                // Reset timer
                editTextMessageInput.removeCallbacks(typingTimeout);
                editTextMessageInput.postDelayed(typingTimeout, TYPING_TIMEOUT);
            }
        });
    }

    private void listenForTypingStatus() {
        if (chatChannelId == null) return;

        // normalized collection name: chatChannels
        typingListener = db.collection("chatChannels")
                .document(chatChannelId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen for typing failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        // get a map of typing users
                        Object raw = snapshot.get("typing");
                        if (raw instanceof Map) {
                            //noinspection unchecked
                            Map<String, Boolean> typing = (Map<String, Boolean>) raw;
                            typingUsers.clear();
                            typingUsers.putAll(typing);
                            updateTypingIndicator();
                        } else {
                            typingUsers.clear();
                            updateTypingIndicator();
                        }
                    }
                });
    }

    private void updateTypingStatus(boolean isTyping) {
        if (chatChannelId == null || currentUser == null) return;

        Boolean currentStatus = typingUsers.get(currentUser.getUid());
        if (currentStatus != null && currentStatus == isTyping) {
            // no change
        } else {
            typingUsers.put(currentUser.getUid(), isTyping);

            // Update Firestore typing sub-field (map) on the channel document
            db.collection("chatChannels")
                    .document(chatChannelId)
                    .update("typing." + currentUser.getUid(), isTyping)
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating typing status", e));
        }

        // Schedule typing timeout locally
        if (isTyping) {
            lastTypingTime = System.currentTimeMillis();
            editTextMessageInput.removeCallbacks(typingTimeout);
            editTextMessageInput.postDelayed(typingTimeout, TYPING_TIMEOUT);
        }
    }

    private void updateTypingIndicator() {
        // Count typing users except current user
        int typingCount = 0;
        for (Map.Entry<String, Boolean> entry : typingUsers.entrySet()) {
            if (!entry.getKey().equals(currentUser.getUid()) && Boolean.TRUE.equals(entry.getValue())) {
                typingCount++;
            }
        }

        if (typingCount > 0) {
            String typingText = typingCount == 1 ? (otherUserName != null ? otherUserName + " is typing..." : "Someone is typing...")
                    : typingCount + " people are typing...";
            showTypingIndicator(typingText);
        } else {
            hideTypingIndicator();
        }
    }

    private void showTypingIndicator(String text) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(text);
        }
    }

    private void hideTypingIndicator() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(null);
        }
    }

    private void setupClickListeners() {
        buttonSendMessage.setOnClickListener(v -> sendMessage());
        buttonSendImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        buttonSendVideo.setOnClickListener(v -> pickVideoLauncher.launch("video/*"));
        buttonSendAudio.setOnClickListener(v -> startAudioRecording());
    }

    private void updateEmptyState() {
        View emptyStateLayout = findViewById(R.id.layout_empty_state);
        if (emptyStateLayout != null && recyclerChatMessages != null) {
            boolean isEmpty = messageList == null || messageList.isEmpty();

            recyclerChatMessages.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

            if (isEmpty) {
                TextView titleView = emptyStateLayout.findViewById(R.id.text_empty_chat_title);
                TextView descriptionView = emptyStateLayout.findViewById(R.id.text_empty_chat_description);
                if (titleView != null && descriptionView != null) {
                    titleView.setText(R.string.empty_chat_title);
                    descriptionView.setText(R.string.empty_chat_description);
                }
            }
        }
    }
    private void showError(String title, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void fetchCurrentUserName() {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        currentUserName = (name != null && !name.isEmpty()) ? name : "You";
                    } else {
                        currentUserName = "You";
                    }

                    participantNames.put(currentUser.getUid(), currentUserName);
                    if (otherUserId != null && otherUserName != null && !participantNames.containsKey(otherUserId)) {
                        participantNames.put(otherUserId, otherUserName);
                    }

                    if (chatChannelId == null) {
                        findOrCreateChatChannel();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching current user name: " + e.getMessage());
                    currentUserName = "You";
                    participantNames.put(currentUser.getUid(), currentUserName);
                    if (otherUserId != null && otherUserName != null && !participantNames.containsKey(otherUserId)) {
                        participantNames.put(otherUserId, otherUserName);
                    }
                    if (chatChannelId == null) {
                        findOrCreateChatChannel();
                    }
                });
    }

    private void findOrCreateChatChannel() {
        setLoading(true);

        List<String> participants = new ArrayList<>();
        participants.add(currentUser.getUid());
        participants.add(otherUserId);

        // Query channels where current user is present, then find the one with the other user
        db.collection("chatChannels")
                .whereArrayContains("participantIds", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String foundChannelId = null;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ChatChannel channel = doc.toObject(ChatChannel.class);
                        if (channel != null && channel.getParticipantIds() != null
                                && channel.getParticipantIds().size() == 2
                                && channel.getParticipantIds().contains(otherUserId)) {
                            foundChannelId = doc.getId();
                            break;
                        }
                    }

                    if (foundChannelId != null) {
                        chatChannelId = foundChannelId;
                        Log.d(TAG, "Found existing chat channel: " + chatChannelId);
                        listenForMessages();
                        listenForTypingStatus();
                    } else {
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
        newChannel.setParticipantNames(participantNames);
        newChannel.setLastMessage("Say hello!");
        newChannel.setLastMessageTimestamp(new Date());
        newChannel.setIsGroupChat(false);

        newChannelRef.set(newChannel)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New chat channel created: " + chatChannelId);
                    listenForMessages();
                    listenForTypingStatus();
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

        if (isLoadingMore) return;

        isLoadingMore = true;
        setLoading(true);
        CollectionReference messagesRef = db.collection("chatChannels").document(chatChannelId).collection("messages");

        Query query = messagesRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.get().addOnCompleteListener(task -> {
            isLoadingMore = false;
            setLoading(false);

            if (task.isSuccessful() && task.getResult() != null) {
                QuerySnapshot snapshots = task.getResult();

                if (!snapshots.isEmpty()) {
                    lastVisible = snapshots.getDocuments().get(snapshots.size() - 1);

                    List<ChatMessage> newMessages = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        if (message != null) {
                            newMessages.add(0, message); // reverse order
                        }
                    }

                    if (messageList.isEmpty()) {
                        messageList.addAll(newMessages);
                        messageAdapter.notifyDataSetChanged();
                        recyclerChatMessages.scrollToPosition(messageList.size() - 1);
                    } else {
                        int oldSize = messageList.size();
                        messageList.addAll(0, newMessages);
                        messageAdapter.notifyItemRangeInserted(0, newMessages.size());
                        // attempt to maintain scroll position (approximate)
                        recyclerChatMessages.scrollBy(0, -getResources().getDimensionPixelSize(R.dimen.message_height) * newMessages.size());
                    }

                    hasMoreMessages = snapshots.size() == PAGE_SIZE;
                } else {
                    hasMoreMessages = false;
                    if (messageList.isEmpty()) {
                        Log.d(TAG, "Current chat has no messages.");
                        messageAdapter.notifyDataSetChanged();
                    }
                }
            } else {
                Toast.makeText(ChatActivity.this, "Error loading messages.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error getting messages.", task.getException());
            }
        });
    }

    private void sendMessage() {
        String messageText = editTextMessageInput.getText() != null ? editTextMessageInput.getText().toString().trim() : "";

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
        if (imm != null) {
            imm.hideSoftInputFromWindow(editTextMessageInput.getWindowToken(), 0);
        }

        setLoading(true);

        DocumentReference messageRef = db.collection("chatChannels").document(chatChannelId).collection("messages").document();
        DocumentReference channelRef = db.collection("chatChannels").document(chatChannelId);

        // Create ChatMessage object
        ChatMessage chatMessage = new ChatMessage(
                currentUser.getUid(),
                currentUserName != null ? currentUserName : "You",
                messageText,
                new Date(),
                false
        );

        // Use a Firestore WriteBatch to update both the message and the chat channel atomically
        WriteBatch batch = db.batch();
        batch.set(messageRef, chatMessage);
        batch.update(channelRef,
                "lastMessage", messageText,
                "lastMessageTimestamp", FieldValue.serverTimestamp()
        );

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Message sent and channel updated successfully.");
                    editTextMessageInput.setText("");
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message: " + e.getMessage());
                    Toast.makeText(ChatActivity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false);
                });
    }
    
    /**
     * Initialize media picker launchers
     */
    private void initializeMediaLaunchers() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Log.d(TAG, "Image selected: " + uri.toString());
                        uploadMediaFile(uri, "image");
                    } else {
                        Log.w(TAG, "Image selection cancelled by user");
                    }
                });

        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Log.d(TAG, "Video selected: " + uri.toString());
                        uploadMediaFile(uri, "video");
                    } else {
                        Log.w(TAG, "Video selection cancelled by user");
                    }
                });
    }

    /**
     * Upload media file to Firebase Storage and send as message
     */
    private void uploadMediaFile(Uri fileUri, String mediaType) {
        setLoading(true);
        
        // Log file details for debugging
        Log.d(TAG, "Starting media upload - Type: " + mediaType + ", URI: " + fileUri);
        
        try {
            byte[] fileBytes = null;
            
            // For local files (from audio recording), read directly
            if (fileUri.getScheme().equals("file")) {
                java.io.File file = new java.io.File(fileUri.getPath());
                if (!file.exists()) {
                    throw new Exception("File does not exist: " + fileUri.getPath());
                }
                
                fileBytes = new byte[(int) file.length()];
                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                int bytesRead = fis.read(fileBytes);
                fis.close();
                Log.d(TAG, "Read " + bytesRead + " bytes from local file: " + file.getAbsolutePath());
            }
            // For content:// URIs (from picker), read via ContentResolver
            else {
                java.io.InputStream inputStream = getContentResolver().openInputStream(fileUri);
                if (inputStream == null) {
                    throw new Exception("Could not open input stream for URI: " + fileUri);
                }
                
                // Read all bytes
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                byte[] data = new byte[16384];
                int nRead;
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                inputStream.close();
                fileBytes = buffer.toByteArray();
                Log.d(TAG, "Read " + fileBytes.length + " bytes from content URI");
            }
            
            if (fileBytes == null || fileBytes.length == 0) {
                throw new Exception("File is empty or could not be read");
            }
            
            String fileName = System.currentTimeMillis() + "_" + mediaType;
            StorageReference fileRef = storageReference
                    .child("chats")
                    .child(chatChannelId)
                    .child(fileName);

            Log.d(TAG, "Upload path: " + fileRef.getPath() + ", Size: " + fileBytes.length + " bytes");

            // Use putBytes instead of putFile to avoid FileProvider URI issues
            fileRef.putBytes(fileBytes)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "File uploaded successfully. Getting download URL...");
                        fileRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                            Log.d(TAG, "Download URL obtained: " + downloadUrl.toString());
                            sendMediaMessage(downloadUrl.toString(), mediaType);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to get download URL: " + e.getMessage(), e);
                            Toast.makeText(ChatActivity.this, "Failed to get " + mediaType + " URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            setLoading(false);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to upload media: " + e.getMessage(), e);
                        Log.e(TAG, "Error code: " + e.getClass().getSimpleName());
                        Toast.makeText(ChatActivity.this, "Failed to upload " + mediaType + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error reading file for upload: " + e.getMessage(), e);
            Toast.makeText(this, "Error reading " + mediaType + " file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            setLoading(false);
        }
    }

    /**
     * Send media message to Firestore
     */
    private void sendMediaMessage(String mediaUrl, String mediaType) {
        if (chatChannelId == null) {
            Log.e(TAG, "Cannot send media - chatChannelId is null");
            Toast.makeText(this, "Chat not ready. Please wait or try again.", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        Log.d(TAG, "Sending " + mediaType + " message to channel: " + chatChannelId);

        DocumentReference messageRef = db.collection("chatChannels").document(chatChannelId).collection("messages").document();
        DocumentReference channelRef = db.collection("chatChannels").document(chatChannelId);

        ChatMessage chatMessage = new ChatMessage(
                currentUser.getUid(),
                currentUserName != null ? currentUserName : "You",
                mediaUrl,
                mediaType
        );

        Log.d(TAG, "Message object created - Type: " + mediaType + ", URL length: " + mediaUrl.length());

        WriteBatch batch = db.batch();
        batch.set(messageRef, chatMessage);
        batch.update(channelRef,
                "lastMessage", "[" + mediaType.toUpperCase() + "]",
                "lastMessageTimestamp", FieldValue.serverTimestamp()
        );

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, mediaType + " message sent successfully.");
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending " + mediaType + " message: " + e.getMessage(), e);
                    Toast.makeText(ChatActivity.this, "Failed to send " + mediaType + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false);
                });
    }

    /**
     * Start or stop audio recording
     */
    private void startAudioRecording() {
        // Check permission on Android 6.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "RECORD_AUDIO permission not granted, requesting...");
                requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 200);
                return;
            }
        }
        
        if (!isRecording) {
            startRecording();
        } else {
            stopAndUploadRecording();
        }
    }

    /**
     * Start recording audio
     */
    private void startRecording() {
        try {
            audioFilePath = getExternalCacheDir().getAbsolutePath() + "/voice_" + System.currentTimeMillis() + ".m4a";
            
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setOutputFile(audioFilePath);
            
            mediaRecorder.prepare();
            mediaRecorder.start();
            
            isRecording = true;
            recordingStartTime = System.currentTimeMillis();
            
            // Update button appearance to show "stop" state
            buttonSendAudio.setAlpha(1.0f);
            Toast.makeText(this, "Recording... (tap to stop)", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Recording started: " + audioFilePath);
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting audio recording: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to start recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isRecording = false;
        }
    }

    /**
     * Stop recording and upload audio file
     */
    private void stopAndUploadRecording() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
            
            isRecording = false;
            long recordingDuration = System.currentTimeMillis() - recordingStartTime;
            
            // Only upload if recording is at least 1 second
            if (recordingDuration < 1000) {
                Toast.makeText(this, "Recording too short (minimum 1 second)", Toast.LENGTH_SHORT).show();
                new java.io.File(audioFilePath).delete();
                return;
            }
            
            Log.d(TAG, "Recording stopped. Duration: " + (recordingDuration / 1000) + "s");
            Log.d(TAG, "Audio file path: " + audioFilePath);
            
            // Verify file exists before uploading
            java.io.File audioFile = new java.io.File(audioFilePath);
            if (!audioFile.exists()) {
                Log.e(TAG, "Audio file does not exist at path: " + audioFilePath);
                Toast.makeText(this, "Audio file not found. Recording may have failed.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Log.d(TAG, "Audio file verified - Size: " + audioFile.length() + " bytes");
            
            // Reset button appearance
            buttonSendAudio.setAlpha(1.0f);
            
            // Upload the recorded file using proper URI handling
            // For Android 7+, use FileProvider instead of deprecated Uri.fromFile()
            Uri audioUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                audioUri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        audioFile
                );
            } else {
                audioUri = Uri.fromFile(audioFile);
            }
            
            Log.d(TAG, "Audio URI created: " + audioUri);
            uploadMediaFile(audioUri, "audio");
            
        } catch (Exception e) {
            Log.e(TAG, "Error stopping audio recording: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(this, "Failed to save recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isRecording = false;
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "RECORD_AUDIO permission granted");
                startRecording();
            } else {
                Log.w(TAG, "RECORD_AUDIO permission denied");
                Toast.makeText(this, "Microphone permission is required to record voice notes", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void loadMessages() {
        if (chatChannelId == null || isLoadingMore || !hasMoreMessages) return;

        isLoadingMore = true;
        CollectionReference messagesRef = db.collection("chatChannels")
                .document(chatChannelId)
                .collection("messages");

        Query query = messagesRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.get().addOnCompleteListener(task -> {
            isLoadingMore = false;
            if (task.isSuccessful() && task.getResult() != null) {
                QuerySnapshot snapshots = task.getResult();
                if (!snapshots.isEmpty()) {
                    lastVisible = snapshots.getDocuments().get(snapshots.size() - 1);
                    List<ChatMessage> newMessages = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) newMessages.add(0, msg);
                    }
                    int oldSize = messageList.size();
                    messageList.addAll(0, newMessages);
                    messageAdapter.notifyItemRangeInserted(0, newMessages.size());
                    recyclerChatMessages.scrollToPosition(newMessages.size());
                    hasMoreMessages = snapshots.size() == PAGE_SIZE;
                } else {
                    hasMoreMessages = false;
                }
            } else {
                Toast.makeText(this, "Failed to load more messages", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading more messages", task.getException());
            }
        });
    }


    public void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (buttonSendMessage != null) {
            buttonSendMessage.setEnabled(!isLoading);
        }
        if (editTextMessageInput != null) {
            editTextMessageInput.setEnabled(!isLoading);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
    }
}
