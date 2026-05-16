package com.example.bookup.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.adapters.DashboardNewsAdapter;
import com.example.bookup.databinding.FragmentDashboardBinding;
import com.example.bookup.models.NewsItem;
import com.example.bookup.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DashboardNewsAdapter newsAdapter;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        setupNewsFeed();
        loadUserInfo();
        
        binding.layoutCreatePost.btnSubmitPost.setOnClickListener(v -> submitPost());
        binding.btnViewAllNews.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.navigation_community);
        });

        binding.cardDashboardSearch.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.navigation_search);
        });

        binding.btnQuickRequests.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigate(R.id.navigation_requests));
        binding.btnQuickAi.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigate(R.id.navigation_ai_chat));
    }

    private void setupNewsFeed() {
        String uid = mAuth.getUid() != null ? mAuth.getUid() : "";
        newsAdapter = new DashboardNewsAdapter(uid, new DashboardNewsAdapter.OnItemClickListener() {
            @Override public void onItemClick(NewsItem item) {
                if (isAdded()) {
                    android.content.Intent intent = new android.content.Intent(getContext(), com.example.bookup.activities.NewsDetailActivity.class);
                    intent.putExtra("news_item", item);
                    intent.putExtra("news_id", item.getId());
                    startActivity(intent);
                }
            }

            @Override public void onLikeClick(NewsItem item) { toggleLike(item); }
        });
        binding.recyclerNewsFeed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerNewsFeed.setAdapter(newsAdapter);

        db.collection("newsFeed")
                .orderBy("priority", Query.Direction.DESCENDING)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((value, error) -> {
                    if (binding != null && value != null && isAdded()) {
                        List<NewsItem> items = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            NewsItem item = doc.toObject(NewsItem.class);
                            if (item != null) {
                                item.setId(doc.getId());
                                items.add(item);
                            }
                        }
                        newsAdapter.setItems(items);
                    }
                });
    }

    private void toggleLike(NewsItem item) {
        if (currentUser == null || item.getId() == null) return;
        
        String uid = currentUser.getId();
        boolean isLiking = !item.isLikedByUser(uid);
        
        db.collection("newsFeed").document(item.getId()).update(
                "likedBy", isLiking ? FieldValue.arrayUnion(uid) : FieldValue.arrayRemove(uid),
                "likesCount", FieldValue.increment(isLiking ? 1 : -1)
        );
    }

    private void loadUserInfo() {
        FirebaseUser fu = mAuth.getCurrentUser();
        if (fu == null) return;
        
        db.collection("users").document(fu.getUid()).get().addOnSuccessListener(doc -> {
            if (binding != null && isAdded()) {
                currentUser = doc.toObject(User.class);
                if (currentUser != null && binding != null) {
                    currentUser.setId(doc.getId());
                    binding.textWelcomeTitle.setText("Hello, " + currentUser.getFirstName() + "!");
                    Glide.with(this)
                         .load(currentUser.getPhotoUrl())
                         .placeholder(R.drawable.ic_user_placeholder)
                         .into(binding.layoutCreatePost.currentUserAvatar);
                }
            }
        });
    }

    private void submitPost() {
        String content = binding.layoutCreatePost.editPostContent.getText().toString().trim();
        if (content.isEmpty()) return;

        if (currentUser == null) {
            Toast.makeText(getContext(), "User profile loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.layoutCreatePost.btnSubmitPost.setEnabled(false);
        
        NewsItem item = new NewsItem();
        item.setContent(content);
        item.setAuthorId(currentUser.getId());
        item.setAuthorName(currentUser.getDisplayName());
        item.setAuthorRole(currentUser.getRole());
        item.setPriority(currentUser.isAdmin());
        item.setLikesCount(0L);
        item.setLikedBy(new ArrayList<>());
        item.setTimestamp(new Date());

        db.collection("newsFeed").add(item)
                .addOnSuccessListener(ref -> {
                    if (binding != null && isAdded()) {
                        binding.layoutCreatePost.editPostContent.setText("");
                        binding.layoutCreatePost.btnSubmitPost.setEnabled(true);
                        Toast.makeText(getContext(), "Post published!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding != null && isAdded()) {
                        binding.layoutCreatePost.btnSubmitPost.setEnabled(true);
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
