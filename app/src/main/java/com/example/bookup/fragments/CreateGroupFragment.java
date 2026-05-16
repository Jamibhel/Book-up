package com.example.bookup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.bookup.R;
import com.example.bookup.adapters.MultiUserSelectionAdapter;
import com.example.bookup.databinding.FragmentCreateGroupBinding;
import com.example.bookup.models.User;
import com.example.bookup.repositories.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupFragment extends Fragment {
    private FragmentCreateGroupBinding binding;
    private MultiUserSelectionAdapter adapter;
    private final ChatRepository chatRepository = new ChatRepository();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        loadCurrentUser();
        setupRecyclerView();
        loadAllUsers();
        
        binding.createGroupButton.setOnClickListener(v -> createGroup());
    }

    private void loadCurrentUser() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                currentUser = doc.toObject(User.class);
                if (currentUser != null) currentUser.setId(doc.getId());
            });
        }
    }

    private void setupRecyclerView() {
        adapter = new MultiUserSelectionAdapter();
        binding.usersRecyclerView.setAdapter(adapter);
    }

    private void loadAllUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").get().addOnSuccessListener(querySnapshot -> {
            List<User> users = new ArrayList<>();
            String myId = FirebaseAuth.getInstance().getUid();
            for (var doc : querySnapshot.getDocuments()) {
                if (!doc.getId().equals(myId)) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        user.setId(doc.getId());
                        users.add(user);
                    }
                }
            }
            adapter.setUsers(users);
            binding.progressBar.setVisibility(View.GONE);
        });
    }

    private void createGroup() {
        String groupName = binding.groupNameEditText.getText().toString().trim();
        List<User> selected = adapter.getSelectedUsers();
        
        if (groupName.isEmpty()) {
            Toast.makeText(getContext(), "Enter group name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selected.isEmpty()) {
            Toast.makeText(getContext(), "Select at least one participant", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentUser == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        chatRepository.createGroupChannel(groupName, selected, currentUser)
                .addOnSuccessListener(channelId -> {
                    Bundle args = new Bundle();
                    args.putString("channelId", channelId);
                    args.putString("channelName", groupName);
                    Navigation.findNavController(requireView()).navigate(R.id.action_createGroup_to_chat, args);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to create group", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
