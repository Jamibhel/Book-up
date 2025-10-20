package com.example.bookup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity; // For setting toolbar title (optional)
import androidx.appcompat.widget.Toolbar; // For toolbar (optional)
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookup.ChatActivity;
import com.example.bookup.ChatChannelAdapter;
import com.example.bookup.R;
import com.example.bookup.models.ChatChannel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private static final String TAG = "ChatListFragment";

    // UI Elements
    private RecyclerView recyclerChatChannels;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyChatList;
    private TextView textEmptyChatListTitle;
    private TextView textEmptyChatListDescription;
    private MaterialButton btnStartNewChat;
    private FloatingActionButton fabStartNewChat;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration chatChannelsListener; // Use a real-time listener for chat lists

    // Adapter and Data
    private ChatChannelAdapter chatChannelAdapter;
    private List<ChatChannel> chatChannelList;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupClickListeners();

        // If the parent activity manages the toolbar, you might set the title here
        if (getActivity() instanceof AppCompatActivity) {
            // ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.chat_list_title);
            // If you have a custom toolbar in your HomePageActivity for fragments,
            // you might get a reference to it and set its title here.
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to view chats.", Toast.LENGTH_LONG).show();
            // Consider navigating to login/registration if not logged in
            // For now, just show empty state
            updateEmptyState(true);
        } else {
            // Start listening for real-time updates when the fragment becomes visible
            listenForChatChannels();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop listening for real-time updates when the fragment is no longer visible
        if (chatChannelsListener != null) {
            chatChannelsListener.remove();
        }
    }

    private void initViews(View view) {
        recyclerChatChannels = view.findViewById(R.id.recycler_chat_channels);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_chat_list);
        progressBar = view.findViewById(R.id.progress_bar_chat_list);
        layoutEmptyChatList = view.findViewById(R.id.layout_empty_chat_list);
        textEmptyChatListTitle = view.findViewById(R.id.text_empty_chat_list_title);
        textEmptyChatListDescription = view.findViewById(R.id.text_empty_chat_list_description);
        btnStartNewChat = view.findViewById(R.id.btn_start_new_chat);
        fabStartNewChat = view.findViewById(R.id.fab_start_new_chat);
    }

    private void setupRecyclerView() {
        chatChannelList = new ArrayList<>();
        chatChannelAdapter = new ChatChannelAdapter(chatChannelList);
        recyclerChatChannels.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChatChannels.setAdapter(chatChannelAdapter);

        chatChannelAdapter.setOnChatChannelClickListener(channel -> {
            if (getContext() != null && currentUser != null) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_CHAT_CHANNEL_ID, channel.getId());
                intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, channel.getOtherParticipantId(currentUser.getUid()));
                intent.putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, channel.getOtherParticipantName(currentUser.getUid()));
                intent.putExtra(ChatActivity.EXTRA_IS_GROUP_CHAT, channel.getIsGroupChat());
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Please sign in to open chats.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::listenForChatChannels); // Refresh now triggers the listener
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorSecondary);
    }

    private void setupClickListeners() {
        View.OnClickListener startNewChatListener = v -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Select a user to chat with (Coming Soon!)", Toast.LENGTH_SHORT).show();
                // TODO: Implement logic to select another user to chat with.
                // This might involve launching an activity like UserSelectionForChatActivity,
                // which then passes the selected user's ID/name back to ChatActivity
                // by calling ChatActivity directly with otherUserId and otherUserName
                // (and chatChannelId as null).
            }
        };
        btnStartNewChat.setOnClickListener(startNewChatListener);
        fabStartNewChat.setOnClickListener(startNewChatListener);
    }

    private void listenForChatChannels() {
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated. Cannot listen for chat channels.");
            updateEmptyState(true);
            setLoading(false);
            if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
            return;
        }

        setLoading(true);

        chatChannelsListener = db.collection("chatChannels")
                .whereArrayContains("participantIds", currentUser.getUid())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed for chat channels.", e);
                        Toast.makeText(getContext(), "Failed to load chats.", Toast.LENGTH_SHORT).show();
                        updateEmptyState(true);
                        setLoading(false);
                        if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        chatChannelList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ChatChannel channel = document.toObject(ChatChannel.class);
                            channel.setId(document.getId());
                            chatChannelList.add(channel);
                        }
                        chatChannelAdapter.notifyDataSetChanged();
                        updateEmptyState(chatChannelList.isEmpty());
                    } else {
                        Log.d(TAG, "Current chat channel list is empty.");
                        updateEmptyState(true);
                    }
                    setLoading(false);
                    if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (!isAdded() || getContext() == null) return; // Fragment not attached

        if (isEmpty) {
            recyclerChatChannels.setVisibility(View.GONE);
            layoutEmptyChatList.setVisibility(View.VISIBLE);
            fabStartNewChat.setVisibility(View.VISIBLE); // Always show FAB
        } else {
            recyclerChatChannels.setVisibility(View.VISIBLE);
            layoutEmptyChatList.setVisibility(View.GONE);
            fabStartNewChat.setVisibility(View.VISIBLE); // Always show FAB
        }
    }

    private void setLoading(boolean isLoading) {
        if (!isAdded() || getContext() == null) return; // Fragment not attached
        progressBar.setVisibility(isLoading && !swipeRefreshLayout.isRefreshing() ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setEnabled(!isLoading);
        fabStartNewChat.setEnabled(!isLoading); // Disable FAB during loading
        btnStartNewChat.setEnabled(!isLoading); // Disable empty state button
    }
}
