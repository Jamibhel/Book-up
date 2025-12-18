package com.example.bookup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookup.adapters.ChatChannelAdapter;
import com.example.bookup.R;
import com.example.bookup.models.ChatChannel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = "ChatListActivity";

    // UI Elements
    private RecyclerView recyclerChatChannels;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyChatList;
    private MaterialButton btnStartNewChat;
    private FloatingActionButton fabStartNewChat;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Adapter and Data
    private ChatChannelAdapter chatChannelAdapter;
    private List<ChatChannel> chatChannelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_chat_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.chat_list_title);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to view chats.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        setupClickListeners();

        fetchChatChannels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            fetchChatChannels();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        recyclerChatChannels = findViewById(R.id.recycler_chat_channels);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_chat_list);
        progressBar = findViewById(R.id.progress_bar_chat_list);
        layoutEmptyChatList = findViewById(R.id.layout_empty_chat_list);
        btnStartNewChat = findViewById(R.id.btn_start_new_chat);
        fabStartNewChat = findViewById(R.id.fab_start_new_chat);
    }

    private void setupRecyclerView() {
        chatChannelList = new ArrayList<>();
        chatChannelAdapter = new ChatChannelAdapter(chatChannelList);
        recyclerChatChannels.setLayoutManager(new LinearLayoutManager(this));
        recyclerChatChannels.setAdapter(chatChannelAdapter);

        chatChannelAdapter.setOnChatChannelClickListener(channel -> {
            if (currentUser != null) {
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_CHAT_CHANNEL_ID, channel.getId());
                intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, channel.getOtherParticipantId(currentUser.getUid()));
                intent.putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, channel.getOtherParticipantName(currentUser.getUid()));
                intent.putExtra(ChatActivity.EXTRA_IS_GROUP_CHAT, channel.getIsGroupChat());
                startActivity(intent);
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchChatChannels);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorSecondary);
    }

    private void setupClickListeners() {
        View.OnClickListener startNewChatListener = v -> startNewChatActivity();
        btnStartNewChat.setOnClickListener(startNewChatListener);
        fabStartNewChat.setOnClickListener(startNewChatListener);
    }

    private void startNewChatActivity() {
        Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
        intent.putExtra("startNewChat", true);
        startActivity(intent);
    }

    private void fetchChatChannels() {
        setLoading(true);

        db.collection("chatChannels")
                .whereArrayContains("participantIds", currentUser.getUid())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatChannelList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ChatChannel channel = document.toObject(ChatChannel.class);
                        channel.setId(document.getId());
                        chatChannelList.add(channel);
                    }
                    chatChannelAdapter.notifyDataSetChanged();
                    updateEmptyState(chatChannelList.isEmpty());
                    setLoading(false);
                    if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching chat channels: " + e.getMessage(), e);
                    Toast.makeText(ChatListActivity.this, "Failed to load chats.", Toast.LENGTH_SHORT).show();
                    updateEmptyState(true);
                    setLoading(false);
                    if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerChatChannels.setVisibility(View.GONE);
            layoutEmptyChatList.setVisibility(View.VISIBLE);
            fabStartNewChat.setVisibility(View.VISIBLE);
        } else {
            recyclerChatChannels.setVisibility(View.VISIBLE);
            layoutEmptyChatList.setVisibility(View.GONE);
            fabStartNewChat.setVisibility(View.VISIBLE);
        }
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading && !swipeRefreshLayout.isRefreshing() ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setEnabled(!isLoading);
        fabStartNewChat.setEnabled(!isLoading);
        btnStartNewChat.setEnabled(!isLoading);
    }
}
