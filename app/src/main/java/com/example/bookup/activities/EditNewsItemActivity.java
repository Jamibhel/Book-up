package com.example.bookup.activities;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.models.NewsItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditNewsItemActivity extends AppCompatActivity {

    public static final String EXTRA_NEWS_ITEM = "extra_news_item";

    private static final String TAG = "EditNewsItemActivity";

    // UI Elements
    private ImageView imagePreview;
    private TextInputEditText editTextHeadline;
    private TextInputEditText editTextContent;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextNewsSource;
    private MaterialButton btnPickImage;
    private MaterialButton btnUpdateNews;
    private MaterialButton btnCancelEdit;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    // Image handling
    private Uri selectedImageUri;
    private String uploadedImageUrl;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;

    // Activity result launcher for image picker
    private ActivityResultLauncher<String> imagePicker;

    // Data
    private NewsItem currentNewsItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_news_item);

        toolbar = findViewById(R.id.toolbar_edit_news);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Edit News");
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to edit news items.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        checkAdminStatus();

        // Setup image picker launcher
        setupImagePicker();

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

    private void setupImagePicker() {
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(this)
                                .load(uri)
                                .centerCrop()
                                .into(imagePreview);
                        btnPickImage.setText("Change Image");
                    }
                }
        );
    }

    private void initViews() {
        imagePreview = findViewById(R.id.image_preview);
        editTextHeadline = findViewById(R.id.edit_headline);
        editTextContent = findViewById(R.id.edit_content);
        editTextDescription = findViewById(R.id.edit_description);
        editTextNewsSource = findViewById(R.id.edit_news_source);
        btnPickImage = findViewById(R.id.btn_pick_image);
        btnUpdateNews = findViewById(R.id.btn_update_news);
        btnCancelEdit = findViewById(R.id.btn_cancel_edit);
        progressBar = findViewById(R.id.loading_indicator);
    }

    private void setupClickListeners() {
        btnPickImage.setOnClickListener(v -> imagePicker.launch("image/*"));
        btnUpdateNews.setOnClickListener(v -> updateNewsItem());
        btnCancelEdit.setOnClickListener(v -> finish());
    }

    private void checkAdminStatus() {
        if (currentUser == null) return;

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

        editTextHeadline.setText(currentNewsItem.getHeadline() != null ? currentNewsItem.getHeadline() : currentNewsItem.getTitle());
        editTextContent.setText(currentNewsItem.getContent());
        editTextDescription.setText(currentNewsItem.getDescription());
        editTextNewsSource.setText(currentNewsItem.getSource());
        uploadedImageUrl = currentNewsItem.getImageUrl();

        // Display current image
        if (currentNewsItem.getImageUrl() != null && !currentNewsItem.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentNewsItem.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_dashboard_banner_placeholder)
                    .into(imagePreview);
        }
    }

    private void updateNewsItem() {
        String headline = editTextHeadline.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String source = editTextNewsSource.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(headline)) {
            editTextHeadline.setError("Headline is required.");
            editTextHeadline.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(content)) {
            editTextContent.setError("Content is required.");
            editTextContent.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(source)) {
            editTextNewsSource.setError("Source is required.");
            editTextNewsSource.requestFocus();
            return;
        }

        setLoading(true);

        // If new image selected, upload it; otherwise keep existing image
        if (selectedImageUri != null) {
            uploadImageToFirebase();
        } else {
            updateNewsItemInFirestore(headline, content, description, source);
        }
    }

    private void uploadImageToFirebase() {
        String filename = "news_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storage.getReference().child("news_images/" + filename);

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            uploadedImageUrl = uri.toString();
                            String headline = editTextHeadline.getText().toString().trim();
                            String content = editTextContent.getText().toString().trim();
                            String description = editTextDescription.getText().toString().trim();
                            String source = editTextNewsSource.getText().toString().trim();
                            updateNewsItemInFirestore(headline, content, description, source);
                        })
                )
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Image upload failed: " + e.getMessage());
                    Toast.makeText(EditNewsItemActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateNewsItemInFirestore(String headline, String content, String description, String source) {
        currentNewsItem.setHeadline(headline);
        currentNewsItem.setTitle(headline);
        currentNewsItem.setContent(content);
        currentNewsItem.setDescription(description);
        currentNewsItem.setImageUrl(uploadedImageUrl);
        currentNewsItem.setSource(source);

        db.collection("newsFeed").document(currentNewsItem.getId()).set(currentNewsItem)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(EditNewsItemActivity.this, "News item updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
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
        btnPickImage.setEnabled(!isLoading);
        editTextHeadline.setEnabled(!isLoading);
        editTextContent.setEnabled(!isLoading);
        editTextDescription.setEnabled(!isLoading);
        editTextNewsSource.setEnabled(!isLoading);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
    }
}

