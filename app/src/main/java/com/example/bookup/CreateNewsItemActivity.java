package com.example.bookup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookup.models.NewsItem; // Import your NewsItem model
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue; // For ServerTimestamp
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateNewsItemActivity extends AppCompatActivity {

    private static final String TAG = "CreateNewsItemActivity";

    // UI Elements
    private TextInputEditText editTextNewsTitle;
    private TextInputEditText editTextNewsDescription;
    private TextInputEditText editTextNewsImageUrl;
    private TextInputEditText editTextNewsSource;
    private MaterialButton btnPublishNews;
    private MaterialButton btnCancelNewsCreation;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_news_item);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_create_news);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.create_news_item_title);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Basic check for admin user. More robust checks should be server-side.
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to create news items.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        checkAdminStatus(); // Check if current user is admin

        initViews();
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
        btnPublishNews = findViewById(R.id.btn_publish_news);
        btnCancelNewsCreation = findViewById(R.id.btn_cancel_news_creation);
        progressBar = findViewById(R.id.progress_bar_create_news);
    }

    private void setupClickListeners() {
        btnPublishNews.setOnClickListener(v -> publishNewsItem());
        btnCancelNewsCreation.setOnClickListener(v -> finish());
    }

    /**
     * Checks if the current authenticated user has admin privileges.
     * If not, finishes the activity to prevent unauthorized access.
     * This is a client-side check; server-side security rules are also crucial.
     */
    private void checkAdminStatus() {
        if (currentUser == null) {
            Toast.makeText(this, "Authentication required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                        if (isAdmin == null || !isAdmin) { // If isAdmin is null or false
                            Toast.makeText(this, "Access denied: Not an admin.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        // Else, user is admin, proceed
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

    /**
     * Publishes a new news item to the "newsFeed" Firestore collection.
     * Performs basic client-side validation before attempting to save.
     */
    private void publishNewsItem() {
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
        // Optional: Add URL validation for imageUrl if desired

        setLoading(true);

        // Create a NewsItem object (ID and Timestamp handled by Firestore/ServerTimestamp)
        NewsItem newsItem = new NewsItem();
        newsItem.setTitle(title);
        newsItem.setDescription(description);
        newsItem.setImageUrl(imageUrl);
        newsItem.setSource(source);
        // The timestamp field in NewsItem model has @ServerTimestamp, so Firestore will populate it automatically

        db.collection("newsFeed").add(newsItem) // Directly save the NewsItem object
                .addOnSuccessListener(documentReference -> {
                    setLoading(false);
                    Toast.makeText(CreateNewsItemActivity.this, "News item published successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to ManageNewsActivity
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Error publishing news item: " + e.getMessage(), e);
                    Toast.makeText(CreateNewsItemActivity.this, "Failed to publish news item: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Manages the visibility of the ProgressBar and enables/disables UI elements
     * to prevent user interaction during an asynchronous operation.
     * @param isLoading True to show progress bar and disable UI, false otherwise.
     */
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnPublishNews.setEnabled(!isLoading);
        btnCancelNewsCreation.setEnabled(!isLoading);
        editTextNewsTitle.setEnabled(!isLoading);
        editTextNewsDescription.setEnabled(!isLoading);
        editTextNewsImageUrl.setEnabled(!isLoading);
        editTextNewsSource.setEnabled(!isLoading);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
    }
}
