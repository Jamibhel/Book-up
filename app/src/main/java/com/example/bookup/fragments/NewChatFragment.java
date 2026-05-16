package com.example.bookup.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.bookup.R;
import com.example.bookup.adapters.UserSelectionAdapter;
import com.example.bookup.databinding.FragmentNewChatBinding;
import com.example.bookup.models.User;
import com.example.bookup.repositories.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NewChatFragment extends Fragment {
    private FragmentNewChatBinding binding;
    private UserSelectionAdapter adapter;
    private final ChatRepository chatRepository = new ChatRepository();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private User currentUser;
    private List<User> allUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        loadCurrentUser();
        setupRecyclerView();
        setupSearch();
        setupGroupAction();
        loadAllUsers();
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

    private void setupGroupAction() {
        binding.btnNewGroupHeader.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.action_newChat_to_createGroup);
        });
    }

    private void setupRecyclerView() {
        adapter = new UserSelectionAdapter(getContext());
        adapter.setOnUserClickListener(user -> {
            if (currentUser != null) {
                binding.progressBar.setVisibility(View.VISIBLE);
                chatRepository.getOrCreateChatChannel(currentUser, user)
                        .addOnSuccessListener(channelId -> {
                            Bundle args = new Bundle();
                            args.putString("channelId", channelId);
                            args.putString("channelName", user.getDisplayName());
                            Navigation.findNavController(requireView()).navigate(R.id.action_newChat_to_chat, args);
                        });
            }
        });
        binding.usersRecyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAllUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").get().addOnSuccessListener(querySnapshot -> {
            if (binding != null) {
                allUsers.clear();
                String myId = FirebaseAuth.getInstance().getUid();
                for (var doc : querySnapshot.getDocuments()) {
                    if (!doc.getId().equals(myId)) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());
                            allUsers.add(user);
                        }
                    }
                }
                adapter.submitList(new ArrayList<>(allUsers));
                binding.progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            if (binding != null) binding.progressBar.setVisibility(View.GONE);
        });
    }

    private void filterUsers(String query) {
        if (query.isEmpty()) {
            adapter.submitList(new ArrayList<>(allUsers));
        } else {
            List<User> filtered = new ArrayList<>();
            for (User u : allUsers) {
                if (u.getDisplayName().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(u);
                }
            }
            adapter.submitList(filtered);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
