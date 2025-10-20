package com.example.bookup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookup.models.NewsItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditNewsItemActivity extends AppCompatActivity {

    public static final String EXTRA_NEWS_ITEM = "extra_news_item";

    private static final String TAG = "EditNewsItemActivity";

    // UI Elements
    private TextInputEditText editTextNewsTitle;
    private TextInputEditText editTextNewsDescription;
    private TextInputEditText editTextNewsImageUrl;
    private TextInputEditText editTextNewsSource;
    private MaterialButton btnUpdateNews;
    private MaterialButton btnCancelEdit;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Data
    private NewsItem currentNewsItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_news_item);

        Toolbar toolbar = findViewById(R.id.toolbar_edit_news);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_news_item_title);
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to edit news items.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        checkAdminStatus(); // Client-side admin check

        initViews();

        if (getIntent().hasExtra(EXTRA_NEWS_ITEM)) {
            currentNewsItem = (NewsItem) getIntent().getSerializableExtra(EXTRA_NEWS_ITEM);
            if (currentNewsItem != null) {
                displayNewsItemForEditing();
            } else {
                Toast.makeText(this, "Error: News item not found.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No news item provided for editing.", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupClickListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        editTextNewsTitle = findViewById(R.id.edit_text_news_title);
        editTextNewsDescription = findViewById(R.id.edit_text_news_description);
        editTextNewsImageUrl = findViewById(R.id.edit_text_news_image_url);
        editTextNewsSource = findViewById(R.id.edit_text_news_source);
        btnUpdateNews = findViewById(R.id.btn_update_news);
        btnCancelEdit = findViewById(R.id.btn_cancel_edit);
        progressBar = findViewById(R.id.progress_bar_edit_news);
    }

    private void setupClickListeners() {
        btnUpdateNews.setOnClickListener(v -> updateNewsItem());
        btnCancelEdit.setOnClickListener(v -> finish());
    }

    private void checkAdminStatus() {
        if (currentUser == null) return; // Already checked in onCreate

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                        if (isAdmin == null || !isAdmin) {
                            Toast.makeText(this, "Access denied: Not an admin.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "User profile not found. Access denied.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check admin status: " + e.getMessage(), e);
                    Toast.makeText(this, "Error checking admin status. Access denied.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void displayNewsItemForEditing() {
        if (currentNewsItem == null) return;

        editTextNewsTitle.setText(currentNewsItem.getTitle());
        editTextNewsDescription.setText(currentNewsItem.getDescription());
        editTextNewsImageUrl.setText(currentNewsItem.getImageUrl());
        editTextNewsSource.setText(currentNewsItem.getSource());
    }

    private void updateNewsItem() {
        String title = editTextNewsTitle.getText().toString().trim();
        String description = editTextNewsDescription.getText().toString().trim();
        String imageUrl = editTextNewsImageUrl.getText().toString().trim();
        String source = editTextNewsSource.getText().toString().trim();

        // --- Validation ---
        if (TextUtils.isEmpty(title)) {
            editTextNewsTitle.setError("Title is required.");
            editTextNewsTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            editTextNewsDescription.setError("Description is required.");
            editTextNewsDescription.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(source)) {
            editTextNewsSource.setError("Source is required.");
            editTextNewsSource.requestFocus();
            return;
        }

        setLoading(true);

        // Update the currentNewsItem object
        currentNewsItem.setTitle(title);
        currentNewsItem.setDescription(description);
        currentNewsItem.setImageUrl(imageUrl);
        currentNewsItem.setSource(source);

        db.collection("newsFeed").document(currentNewsItem.getId()).set(currentNewsItem)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(EditNewsItemActivity.this, "News item updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to ManageNewsActivity
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Error updating news item: " + e.getMessage(), e);
                    Toast.makeText(EditNewsItemActivity.this, "Failed to update news item: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnUpdateNews.setEnabled(!isLoading);
        btnCancelEdit.setEnabled(!isLoading);
        editTextNewsTitle.setEnabled(!isLoading);
        editTextNewsDescription.setEnabled(!isLoading);
        editTextNewsImageUrl.setEnabled(!isLoading);
        editTextNewsSource.setEnabled(!isLoading);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
    }
}
