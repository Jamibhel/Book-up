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

import com.bumptech.glide.Glide;
import com.example.bookup.databinding.ActivityCreateNewsItemBinding;
import com.example.bookup.models.NewsItem;
import com.example.bookup.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;

public class CreateNewsItemActivity extends AppCompatActivity {
    private static final String TAG = "CreateNewsItemActivity";
    private ActivityCreateNewsItemBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;

    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNewsItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupImagePicker();

        binding.btnPublishNews.setOnClickListener(v -> publishNews());
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.btnPickImage.setOnClickListener(v -> imagePicker.launch("image/*"));
        binding.btnCancelNewsCreation.setOnClickListener(v -> finish());
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
                                .into(binding.imagePreview);
                        binding.btnPickImage.setText("Change Image");
                    }
                }
        );
    }

    private void publishNews() {
        String title = getText(binding.editTextNewsHeadline);
        String content = getText(binding.editTextNewsContent);
        String description = getText(binding.editTextNewsDescription);
        String source = getText(binding.editTextNewsSource);

        if (TextUtils.isEmpty(title)) {
            binding.editTextNewsHeadline.setError("Headline is required");
            binding.editTextNewsHeadline.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(content)) {
            binding.editTextNewsContent.setError("Content is required");
            binding.editTextNewsContent.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(source)) {
            source = "Admin";
        }

        setLoading(true);

        if (selectedImageUri != null) {
            uploadImageThenPublish(title, content, description, source);
        } else {
            createNewsDocument(title, content, description, source, null);
        }
    }

    private void uploadImageThenPublish(String title, String content, String description, String source) {
        String filename = "news_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storage.getReference().child("news_images/" + filename);

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            uploadedImageUrl = uri.toString();
                            createNewsDocument(title, content, description, source, uploadedImageUrl);
                        })
                )
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Image upload failed", e);
                    Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createNewsDocument(String title, String content, String description, String source, String imageUrl) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            setLoading(false);
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = firebaseUser.getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            String authorName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Admin";
            String authorRole = "admin";
            boolean isAdminUser = false;

            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    authorName = user.getDisplayName() != null ? user.getDisplayName() : authorName;
                    authorRole = user.getRole() != null ? user.getRole() : authorRole;
                    isAdminUser = user.isAdmin();
                }
            }

            NewsItem item = new NewsItem();
            item.setTitle(title);
            item.setHeadline(title);
            item.setContent(content);
            item.setDescription(description);
            item.setSource(source);
            item.setImageUrl(imageUrl);
            item.setAuthorId(uid); // Use FirebaseAuth UID directly — always non-null
            item.setAuthorName(authorName);
            item.setAuthorRole(authorRole);
            item.setPriority(isAdminUser);
            item.setTimestamp(new Date());
            item.setLikesCount(0L);
            item.setLikedBy(new ArrayList<>());
            item.setComments(new ArrayList<>());

            db.collection("newsFeed").add(item).addOnSuccessListener(ref -> {
                setLoading(false);
                Toast.makeText(this, "Published!", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                setLoading(false);
                Log.e(TAG, "Failed to publish", e);
                Toast.makeText(this, "Publish failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            setLoading(false);
            Log.e(TAG, "Failed to fetch user profile", e);
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBarCreateNews.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnPublishNews.setEnabled(!loading);
        binding.btnPickImage.setEnabled(!loading);
        binding.btnCancelNewsCreation.setEnabled(!loading);
    }

    private String getText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
