package com.example.bookup.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.bookup.R;
import com.example.bookup.adapters.ChatChannelAdapter;
import com.example.bookup.databinding.BottomSheetChatOptionsBinding;
import com.example.bookup.databinding.FragmentChatListBinding;
import com.example.bookup.models.ChatChannel;
import com.example.bookup.viewmodels.ChatViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {
    private static final String TAG = "ChatListFragment";
    private FragmentChatListBinding binding;
    private ChatViewModel viewModel;
    private ChatChannelAdapter adapter;
    private List<ChatChannel> allChannels = new ArrayList<>();
    private String currentFilter = "All";
    private final String currentUserId = FirebaseAuth.getInstance().getUid();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        
        setupRecyclerView();
        setupSearch();
        observeViewModel();
        
        binding.newChatFab.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_chatList_to_newChat);
        });

        binding.btnCreateGroupAction.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_chatList_to_createGroup);
        });

        setupFilterChips();
        
        viewModel.loadChannels();
    }

    private void setupFilterChips() {
        binding.filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) currentFilter = "All";
            else if (id == R.id.chipUnread) currentFilter = "Unread";
            else if (id == R.id.chipPersonal) currentFilter = "Personal";
            else if (id == R.id.chipGroups) currentFilter = "Groups";
            
            filterChannels(binding.searchView.getQuery().toString());
        });
    }

    private void setupRecyclerView() {
        binding.channelsRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        adapter = new ChatChannelAdapter(new ChatChannelAdapter.OnChannelClickListener() {
            @Override
            public void onChannelClick(ChatChannel channel) {
                Bundle args = new Bundle();
                args.putString("channelId", channel.getId());
                String name = "Chat";
                if (channel.isGroup()) {
                    name = channel.getGroupName();
                } else if (channel.getParticipantNames() != null) {
                    for (String id : channel.getParticipantIds()) {
                        if (!id.equals(currentUserId)) {
                            name = channel.getParticipantNames().get(id);
                            break;
                        }
                    }
                }
                args.putString("channelName", name);
                Navigation.findNavController(requireView()).navigate(R.id.action_chatList_to_chat, args);
            }

            @Override
            public void onChannelLongClick(ChatChannel channel, View view) {
                showOptionsBottomSheet(channel);
            }
        });
        binding.channelsRecyclerView.setAdapter(adapter);
    }

    private void showOptionsBottomSheet(ChatChannel channel) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        BottomSheetChatOptionsBinding sheetBinding = BottomSheetChatOptionsBinding.inflate(getLayoutInflater());
        bottomSheet.setContentView(sheetBinding.getRoot());

        boolean isPinned = channel.getPinnedBy() != null && Boolean.TRUE.equals(channel.getPinnedBy().get(currentUserId));
        sheetBinding.btnPinChat.setText(isPinned ? "Unpin Chat" : "Pin Chat");
        sheetBinding.btnPinChat.setOnClickListener(v -> {
            viewModel.pinChannel(channel.getId(), !isPinned);
            bottomSheet.dismiss();
        });

        sheetBinding.btnDeleteChat.setOnClickListener(v -> {
            viewModel.deleteChannelForMe(channel.getId());
            bottomSheet.dismiss();
        });

        bottomSheet.show();
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterChannels(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterChannels(newText);
                return true;
            }
        });
    }

    private void observeViewModel() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getChannels().observe(getViewLifecycleOwner(), channels -> {
            if (channels == null) {
                Log.d(TAG, "observeViewModel: channels list is null");
            } else {
                Log.d(TAG, "observeViewModel: received " + channels.size() + " channels");
            }
            allChannels = channels != null ? channels : new ArrayList<>();
            filterChannels(binding.searchView.getQuery().toString());
            binding.progressBar.setVisibility(View.GONE);
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            Log.e(TAG, "observeViewModel: error received: " + error);
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
        });
    }

    private void filterChannels(String query) {
        List<ChatChannel> filtered = new ArrayList<>();
        String lowerQuery = (query != null) ? query.toLowerCase() : "";

        for (ChatChannel channel : allChannels) {
            // Apply Category Filter
            boolean categoryMatch = true;
            if (currentFilter.equals("Unread")) {
                com.google.firebase.Timestamp lastRead = channel.getLastRead() != null ? channel.getLastRead().get(currentUserId) : null;
                com.google.firebase.Timestamp lastMsg = channel.getLastMessageTimestamp();
                if (lastMsg != null && lastRead != null && lastRead.compareTo(lastMsg) >= 0) categoryMatch = false;
                else if (lastMsg == null) categoryMatch = false;
            } else if (currentFilter.equals("Personal")) {
                if (channel.isGroup()) categoryMatch = false;
            } else if (currentFilter.equals("Groups")) {
                if (!channel.isGroup()) categoryMatch = false;
            }

            if (!categoryMatch) continue;

            // Apply Search Query
            if (lowerQuery.isEmpty()) {
                filtered.add(channel);
                continue;
            }

            boolean searchMatch = false;
            if (channel.isGroup()) {
                if (channel.getGroupName() != null && channel.getGroupName().toLowerCase().contains(lowerQuery)) searchMatch = true;
            } else {
                if (channel.getParticipantNames() != null) {
                    for (String name : channel.getParticipantNames().values()) {
                        if (name != null && name.toLowerCase().contains(lowerQuery)) {
                            searchMatch = true;
                            break;
                        }
                    }
                }
            }
            if (searchMatch) filtered.add(channel);
        }
        adapter.setChannels(filtered);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
