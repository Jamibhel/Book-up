package com.example.bookup.fragments;

import android.os.Bundle;
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
import com.example.bookup.adapters.NewsFeedAdapter;
import com.example.bookup.databinding.FragmentCommunityBinding;
import com.example.bookup.models.NewsItem;
import com.example.bookup.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommunityFragment extends Fragment {
    private FragmentCommunityBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private NewsFeedAdapter adapter;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        loadUserInfo();
        loadFeed();

        binding.layoutCreatePost.btnSubmitPost.setOnClickListener(v -> submitPost());
    }

    private void setupRecyclerView() {
        String uid = mAuth.getUid() != null ? mAuth.getUid() : "";
        adapter = new NewsFeedAdapter(uid, new NewsFeedAdapter.OnNewsItemClickListener() {
            @Override public void onNewsItemClick(NewsItem item) { openNewsDetail(item); }
            @Override public void onLikeClick(NewsItem item) { toggleLike(item); }
            @Override public void onCommentClick(NewsItem item) { openNewsDetail(item); }
            @Override public void onShareClick(NewsItem item) {}
        });
        binding.recyclerCommunityFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerCommunityFeed.setAdapter(adapter);
    }

    private void openNewsDetail(NewsItem item) {
        android.content.Intent intent = new android.content.Intent(getContext(), com.example.bookup.activities.NewsDetailActivity.class);
        intent.putExtra("news_item", item);
        intent.putExtra("news_id", item.getId());
        startActivity(intent);
    }

    private void toggleLike(NewsItem item) {
        if (currentUser == null || item.getId() == null) return;
        
        String uid = currentUser.getId();
        boolean isLiking = !item.isLikedByUser(uid);
        
        List<String> likedBy = item.getLikedBy() != null ? new ArrayList<>(item.getLikedBy()) : new ArrayList<>();
        if (isLiking) likedBy.add(uid);
        else likedBy.remove(uid);
        
        // Optimistic UI Update
        item.setLikedBy(likedBy);
        item.setLikesCount(likedBy.size());
        adapter.notifyDataSetChanged();

        db.collection("newsFeed").document(item.getId()).update(
                "likedBy", isLiking ? FieldValue.arrayUnion(uid) : FieldValue.arrayRemove(uid),
                "likesCount", FieldValue.increment(isLiking ? 1 : -1)
        );
    }

    private void loadUserInfo() {
        String uid = mAuth.getUid();
        if (uid == null) return;
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (binding != null) {
                currentUser = doc.toObject(User.class);
                if (currentUser != null && isAdded()) {
                    currentUser.setId(doc.getId());
                    Glide.with(this)
                         .load(currentUser.getPhotoUrl())
                         .placeholder(R.drawable.ic_user_placeholder)
                         .into(binding.layoutCreatePost.currentUserAvatar);
                }
            }
        });
    }

    private void loadFeed() {
        db.collection("newsFeed")
                .orderBy("priority", Query.Direction.DESCENDING)
                .orderBy("timestamp", Query.Direction.DESCENDING)
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
                        adapter.setItems(items);
                    }
                });
    }

    private void submitPost() {
        String content = binding.layoutCreatePost.editPostContent.getText().toString().trim();
        if (content.isEmpty() || currentUser == null) return;

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
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding != null && isAdded()) {
                        binding.layoutCreatePost.btnSubmitPost.setEnabled(true);
                        Toast.makeText(getContext(), "Error posting", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
