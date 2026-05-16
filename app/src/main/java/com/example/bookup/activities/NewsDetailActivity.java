package com.example.bookup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.adapters.CommentsAdapter;
import com.example.bookup.models.Comment;
import com.example.bookup.models.NewsItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class NewsDetailActivity extends AppCompatActivity {
    public static final String EXTRA_NEWS_ID = "news_id";
    public static final String EXTRA_NEWS_ITEM = "news_item";

    private ImageView imageNewDetail;
    private TextView headline, source, timestamp, content;
    private MaterialButton btnLike, commentCount;
    private TextInputEditText editComment;
    private MaterialButton btnPostComment;
    private RecyclerView recyclerComments;
    private LinearLayout emptyCommentsState, replyPreviewLayout;
    private TextView textReplyToName, textReplyToContent;
    private ImageButton btnCancelReply;
    private Toolbar toolbar;

    private NewsItem newsItem;
    private String newsId;
    private CommentsAdapter commentsAdapter;
    private List<Comment> commentsList = new ArrayList<>();
    private Comment replyingTo = null;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        initializeViews();
        setupToolbar();
        setupRecyclerView();

        if (getIntent() != null) {
            newsId = getIntent().getStringExtra(EXTRA_NEWS_ID);
            newsItem = (NewsItem) getIntent().getSerializableExtra(EXTRA_NEWS_ITEM);
            if (newsItem != null) {
                if (newsId == null) newsId = newsItem.getId();
                displayNewsItem(newsItem);
                if (newsId != null) setupRealtimeListener();
            } else if (newsId != null) {
                loadNewsItemFromFirestore(newsId);
            }
        }
        setupListeners();
    }

    private void initializeViews() {
        imageNewDetail = findViewById(R.id.image_news_detail);
        headline = findViewById(R.id.headline);
        source = findViewById(R.id.source);
        timestamp = findViewById(R.id.timestamp);
        content = findViewById(R.id.content);
        btnLike = findViewById(R.id.btn_like);
        commentCount = findViewById(R.id.comment_count);
        editComment = findViewById(R.id.edit_comment);
        btnPostComment = findViewById(R.id.btn_post_comment);
        recyclerComments = findViewById(R.id.recycler_comments);
        emptyCommentsState = findViewById(R.id.empty_comments_state);
        toolbar = findViewById(R.id.toolbar);
        
        replyPreviewLayout = findViewById(R.id.reply_preview_layout);
        textReplyToName = findViewById(R.id.text_reply_to_name);
        textReplyToContent = findViewById(R.id.text_reply_to_content);
        btnCancelReply = findViewById(R.id.btn_cancel_reply);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        commentsAdapter = new CommentsAdapter(this);
        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerComments.setAdapter(commentsAdapter);
        commentsAdapter.setOnCommentActionListener(new CommentsAdapter.OnCommentActionListener() {
            @Override public void onUserProfileClick(String userId) { navigateToUserProfile(userId); }
            @Override public void onLikeCommentClick(Comment comment) { toggleCommentLike(comment); }
            @Override public void onReplyCommentClick(Comment comment) { setReplyingTo(comment); }
            @Override public void onDeleteCommentClick(Comment comment) { deleteComment(comment); }
        });
    }

    private void setupListeners() {
        btnLike.setOnClickListener(v -> togglePostLike());
        btnPostComment.setOnClickListener(v -> {
            String text = editComment.getText().toString().trim();
            if (!text.isEmpty()) postComment(text);
        });
        btnCancelReply.setOnClickListener(v -> cancelReply());
    }

    private void displayNewsItem(NewsItem item) {
        if (item == null) return;
        
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(this).load(item.getImageUrl()).placeholder(R.drawable.ic_dashboard_banner_placeholder).into(imageNewDetail);
            imageNewDetail.setVisibility(View.VISIBLE);
        } else {
            imageNewDetail.setVisibility(View.GONE);
        }

        headline.setText(item.getTitle() != null && !item.getTitle().isEmpty() ? item.getTitle() : "Community Discussion");
        source.setText(item.getAuthorName() != null ? item.getAuthorName() : "Admin");
        
        if (item.getTimestamp() != null) {
            timestamp.setText(android.text.format.DateUtils.getRelativeTimeSpanString(item.getTimestamp().getTime(), System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS));
        }

        content.setText(item.getContent());
        
        updatePostLikeUI();
        commentsList = item.getComments() != null ? item.getComments() : new ArrayList<>();
        commentsAdapter.setComments(new ArrayList<>(commentsList));
        commentCount.setText(commentsList.size() + (commentsList.size() == 1 ? " reply" : " replies"));
        emptyCommentsState.setVisibility(commentsList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupRealtimeListener() {
        db.collection("newsFeed").document(newsId).addSnapshotListener((doc, e) -> {
            if (doc != null && doc.exists()) {
                newsItem = doc.toObject(NewsItem.class);
                if (newsItem != null) { newsItem.setId(doc.getId()); displayNewsItem(newsItem); }
            }
        });
    }

    private void togglePostLike() {
        if (currentUser == null || newsItem == null || newsId == null) return;
        
        String uid = currentUser.getUid();
        boolean isLiking = !newsItem.isLikedByUser(uid);
        
        List<String> likedBy = newsItem.getLikedBy() != null ? new ArrayList<>(newsItem.getLikedBy()) : new ArrayList<>();
        if (isLiking) likedBy.add(uid);
        else likedBy.remove(uid);
        
        // Optimistic UI
        newsItem.setLikedBy(likedBy);
        newsItem.setLikesCount(isLiking ? newsItem.getLikesCount() + 1 : newsItem.getLikesCount() - 1);
        updatePostLikeUI();

        db.collection("newsFeed").document(newsId).update(
                "likedBy", com.google.firebase.firestore.FieldValue.arrayUnion(uid),
                "likesCount", com.google.firebase.firestore.FieldValue.increment(isLiking ? 1 : -1)
        );
        
        if (!isLiking) {
            db.collection("newsFeed").document(newsId).update("likedBy", com.google.firebase.firestore.FieldValue.arrayRemove(uid));
        }
    }

    private void updatePostLikeUI() {
        if (newsItem == null || currentUser == null) return;
        boolean liked = newsItem.isLikedByUser(currentUser.getUid());
        btnLike.setIconResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        btnLike.setIconTintResource(liked ? R.color.error : R.color.colorOnSurfaceVariant);
        btnLike.setText(String.valueOf(newsItem.getLikesCount()));
    }

    private void postComment(String text) {
        if (currentUser == null || newsItem == null || newsId == null) return;
        Comment comment = new Comment(currentUser.getUid(), currentUser.getDisplayName(), text);
        comment.setUserImageUrl(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");
        
        if (replyingTo != null) {
            comment.setReplyToId(replyingTo.getId());
            comment.setReplyToName(replyingTo.getUserName());
            comment.setReplyToText(replyingTo.getText());
        }
        
        List<Comment> currentComments = newsItem.getComments() != null ? new ArrayList<>(newsItem.getComments()) : new ArrayList<>();
        currentComments.add(comment);
        db.collection("newsFeed").document(newsId).update("comments", currentComments).addOnSuccessListener(v -> {
            editComment.setText("");
            cancelReply();
        });
    }

    private void setReplyingTo(Comment comment) {
        replyingTo = comment;
        replyPreviewLayout.setVisibility(View.VISIBLE);
        textReplyToName.setText("Replying to " + comment.getUserName());
        textReplyToContent.setText(comment.getText());
        editComment.requestFocus();
    }

    private void cancelReply() {
        replyingTo = null;
        replyPreviewLayout.setVisibility(View.GONE);
    }

    private void toggleCommentLike(Comment comment) {
        if (currentUser == null || newsItem == null) return;
        List<Comment> comments = new ArrayList<>(newsItem.getComments());
        for (Comment c : comments) {
            if (c.getId().equals(comment.getId())) {
                List<String> lb = c.getLikedBy() != null ? new ArrayList<>(c.getLikedBy()) : new ArrayList<>();
                if (lb.contains(currentUser.getUid())) {
                    lb.remove(currentUser.getUid());
                    c.setLikeCount(c.getLikeCount() - 1);
                } else {
                    lb.add(currentUser.getUid());
                    c.setLikeCount(c.getLikeCount() + 1);
                }
                c.setLikedBy(lb);
                break;
            }
        }
        db.collection("newsFeed").document(newsId).update("comments", comments);
    }

    private void deleteComment(Comment comment) {
        if (newsItem == null) return;
        List<Comment> comments = new ArrayList<>(newsItem.getComments());
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(comment.getId())) {
                comments.remove(i);
                break;
            }
        }
        db.collection("newsFeed").document(newsId).update("comments", comments);
    }

    private void loadNewsItemFromFirestore(String id) {
        db.collection("newsFeed").document(id).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                newsItem = doc.toObject(NewsItem.class);
                if (newsItem != null) { newsItem.setId(doc.getId()); displayNewsItem(newsItem); setupRealtimeListener(); }
            }
        });
    }

    private void navigateToUserProfile(String uid) {
        Intent intent = new Intent(this, TutorDetailsActivity.class);
        intent.putExtra("tutorId", uid);
        startActivity(intent);
    }
}
