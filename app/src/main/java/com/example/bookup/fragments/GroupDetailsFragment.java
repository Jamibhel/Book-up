package com.example.bookup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.adapters.GroupParticipantAdapter;
import com.example.bookup.adapters.MultiUserSelectionAdapter;
import com.example.bookup.databinding.FragmentGroupDetailsBinding;
import com.example.bookup.models.ChatChannel;
import com.example.bookup.models.User;
import com.example.bookup.models.UserSearchItem;
import com.example.bookup.viewmodels.ChatViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupDetailsFragment extends Fragment {

    private FragmentGroupDetailsBinding binding;
    private ChatViewModel viewModel;
    private String channelId;
    private GroupParticipantAdapter adapter;
    private final String currentUserId = FirebaseAuth.getInstance().getUid();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        if (getArguments() != null) {
            channelId = getArguments().getString("channelId");
        }

        setupRecyclerView();
        observeViewModel();
        setupListeners();

        if (channelId != null) {
            viewModel.loadMessages(channelId);
        }
    }

    private void setupRecyclerView() {
        adapter = new GroupParticipantAdapter(currentUserId, userId -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remove Member")
                    .setMessage("Are you sure you want to remove this member from the group?")
                    .setPositiveButton("Remove", (d, w) -> viewModel.removeMember(channelId, userId))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        binding.recyclerParticipants.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerParticipants.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getCurrentChannel().observe(getViewLifecycleOwner(), channel -> {
            if (channel != null && isAdded()) {
                updateUI(channel);
            }
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUI(ChatChannel channel) {
        binding.textGroupName.setText(channel.getGroupName());
        binding.textGroupDescription.setText(channel.getGroupDescription() != null && !channel.getGroupDescription().isEmpty() 
                ? channel.getGroupDescription() : "No description provided.");
        
        Glide.with(this)
                .load(channel.getGroupImage())
                .placeholder(R.drawable.ic_user_placeholder)
                .into(binding.groupImage);

        List<UserSearchItem> participants = new ArrayList<>();
        Map<String, String> names = channel.getParticipantNames();
        Map<String, String> photos = channel.getParticipantPhotos();

        if (names != null && channel.getParticipantIds() != null) {
            for (String uid : channel.getParticipantIds()) {
                String name = names.get(uid);
                String photo = photos != null ? photos.get(uid) : null;
                participants.add(new UserSearchItem(uid, name != null ? name : "Unknown User", photo != null ? photo : ""));
            }
        }

        adapter.setParticipants(participants, channel.getAdminId());
        binding.textParticipantCount.setText(participants.size() + " Participants");

        boolean isAdmin = currentUserId != null && currentUserId.equals(channel.getAdminId());
        binding.btnAddMember.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        binding.btnAddMember.setOnClickListener(v -> showAddMemberDialog());

        binding.btnLeaveGroup.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Leave Group")
                    .setMessage("Are you sure you want to leave this group?")
                    .setPositiveButton("Leave", (d, w) -> {
                        if (channelId != null) {
                            viewModel.deleteChannelForMe(channelId);
                            Navigation.findNavController(requireView()).popBackStack(R.id.navigation_chat, false);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showAddMemberDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        // Using fragment_new_chat layout for user selection
        View view = getLayoutInflater().inflate(R.layout.fragment_new_chat, null);
        dialog.setContentView(view);
        
        androidx.recyclerview.widget.RecyclerView rv = view.findViewById(R.id.usersRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        MultiUserSelectionAdapter userAdapter = new MultiUserSelectionAdapter();
        rv.setAdapter(userAdapter);
        
        View loader = view.findViewById(R.id.progressBar);
        if (loader != null) loader.setVisibility(View.VISIBLE);
        
        FirebaseFirestore.getInstance().collection("users").get().addOnSuccessListener(querySnapshot -> {
            if (loader != null) loader.setVisibility(View.GONE);
            List<User> users = new ArrayList<>();
            for (var doc : querySnapshot.getDocuments()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    user.setId(doc.getId());
                    // Filter out already members
                    ChatChannel current = viewModel.getCurrentChannel().getValue();
                    if (current != null && !current.getParticipantIds().contains(user.getId())) {
                         users.add(user);
                    }
                }
            }
            userAdapter.setUsers(users);
        });

        MaterialButton btnAdd = view.findViewById(R.id.btnNewGroupHeader);
        if (btnAdd != null) {
            btnAdd.setText("Add Selected Members");
            btnAdd.setOnClickListener(v -> {
                List<User> selected = userAdapter.getSelectedUsers();
                for (User u : selected) {
                    viewModel.addMember(channelId, u);
                }
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
