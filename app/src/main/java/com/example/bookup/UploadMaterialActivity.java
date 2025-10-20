package com.example.bookup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat; // For tinting ImageView icon
import androidx.core.content.PackageManagerCompat;
import androidx.core.app.ActivityCompat; // For checking permissions

import android.Manifest; // NEW: For Manifest.permission constants
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager; // NEW: For PackageManager
import android.net.Uri;
import android.os.Build; // NEW: For Build.VERSION.SDK_INT
import android.os.Bundle;
import android.provider.OpenableColumns; // For getting file name from URI
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookup.models.StudyMaterial;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;


public class UploadMaterialActivity extends AppCompatActivity {

    private static final String TAG = "UploadMaterialActivity";

    // UI elements
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextSubject;
    private AutoCompleteTextView autoCompleteMaterialType;
    private TextView textSelectedFileName;
    private ImageView imageFileIcon;
    private ShapeableImageView imageThumbnailPreview;
    private TextView textSelectedThumbnailName;

    private MaterialButton btnSelectFile;
    private MaterialButton btnSelectThumbnail;
    private MaterialButton btnUploadMaterialFinal;
    private ProgressBar progressBarUpload;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // File URIs
    private Uri selectedFileUri = null;
    private Uri selectedThumbnailUri = null;

    // Activity Result Launchers for picking files/images
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher; // NEW: For runtime permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_material);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Define currentUser here
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to upload materials.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupAdapters();
        setupClickListeners();
        setupFilePickers(); // This will register the launchers

        // NEW: Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Storage permission granted, proceeding with file picker.");
                        // Determine which picker to launch based on a flag or last attempted action
                        // For simplicity, we'll assume the context of the last clicked button
                        // In a real app, you might use a temporary flag to know which picker to re-launch
                        Toast.makeText(this, "Permission granted, please tap select file/image again.", Toast.LENGTH_LONG).show();
                        // openFilePicker(); // Or openImagePicker() if that was the context
                    } else {
                        Log.w(TAG, "Storage permission denied. Cannot open file picker.");
                        Toast.makeText(this, "Permission to access storage denied. Cannot select files.", Toast.LENGTH_LONG).show();
                    }
                });

        // Initial state
        btnUploadMaterialFinal.setEnabled(false); // Disabled until a file is selected
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        editTextTitle = findViewById(R.id.text_input_title); // Updated ID from your XML
        editTextDescription = findViewById(R.id.text_input_description); // Updated ID from your XML
        editTextSubject = findViewById(R.id.text_input_subject); // Updated ID from your XML
        autoCompleteMaterialType = findViewById(R.id.auto_complete_material_type);
        textSelectedFileName = findViewById(R.id.text_selected_file_name);
        imageFileIcon = findViewById(R.id.image_file_icon);
        imageThumbnailPreview = findViewById(R.id.image_thumbnail_preview);
        textSelectedThumbnailName = findViewById(R.id.text_selected_thumbnail_name);

        btnSelectFile = findViewById(R.id.btn_select_file);
        btnSelectThumbnail = findViewById(R.id.btn_select_thumbnail);
        btnUploadMaterialFinal = findViewById(R.id.btn_upload_material_final); // Updated ID from your XML
        progressBarUpload = findViewById(R.id.progress_bar_upload);

        // Reset file icon color
        imageFileIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorOnSurfaceVariant), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void setupAdapters() {
        // Subjects (ideally fetched from Firestore or a common list)
        List<String> subjects = Arrays.asList(
                "Mathematics", "Physics", "Chemistry", "Biology", "Computer Science",
                "History", "English", "Economics", "Psychology", "Philosophy",
                "Algebra", "Calculus", "Thermodynamics", "Electromagnetism", "Genetics",
                "Web Development", "Literature", "Microeconomics", "Ethics", "Art History"
        );
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects);
        // Using `editTextSubject` as a plain input for now, but you could make it an AutoCompleteTextView too.
        // For now, if you want subject suggestions:
        // ((AutoCompleteTextView) editTextSubject).setAdapter(subjectAdapter); // This would require editTextSubject to be AutoCompleteTextView in XML

        // Material Types
        String[] materialTypes = {"Notes", "Past Paper", "Cheatsheet", "Study Guide", "Flashcards", "Video Lecture", "Audio Lecture", "Textbook Chapter", "Research Paper", "Tutorial", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, materialTypes);
        autoCompleteMaterialType.setAdapter(typeAdapter);
        autoCompleteMaterialType.setText(materialTypes[0], false); // Set default value
    }

    private void setupClickListeners() {
        btnSelectFile.setOnClickListener(v -> checkAndOpenFilePicker()); // NEW call
        btnSelectThumbnail.setOnClickListener(v -> checkAndOpenImagePicker()); // NEW call
        btnUploadMaterialFinal.setOnClickListener(v -> uploadMaterial());
    }

    // NEW: Check and request permission before opening file picker
    private void checkAndOpenFilePicker() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            // For general files, READ_MEDIA_IMAGES covers many common document types implicitly or will let system handle
            permission = Manifest.permission.READ_MEDIA_IMAGES; // Assuming documents might also be in Images/Downloads
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openFilePicker();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    // NEW: Check and request permission before opening image picker
    private void checkAndOpenImagePicker() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void setupFilePickers() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            // Grant persistable URI permissions - CRITICAL FIX for "permission denied"
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            try {
                                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            } catch (SecurityException e) {
                                Log.e(TAG, "Failed to take persistable URI permission for file: " + e.getMessage());
                                Toast.makeText(this, "Permission denied for selected file. Please ensure app has access.", Toast.LENGTH_LONG).show();
                                // Do not proceed if permission cannot be taken
                                selectedFileUri = null;
                                textSelectedFileName.setText(R.string.no_file_selected);
                                imageFileIcon.setImageResource(R.drawable.ic_document_black_24dp);
                                imageFileIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorOnSurfaceVariant), android.graphics.PorterDuff.Mode.SRC_IN);
                                btnUploadMaterialFinal.setEnabled(false);
                                return;
                            }

                            selectedFileUri = uri;
                            String fileName = getFileName(selectedFileUri);
                            textSelectedFileName.setText(fileName);
                            imageFileIcon.setImageResource(getFileIcon(fileName));
                            imageFileIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                            btnUploadMaterialFinal.setEnabled(true); // Enable if file is selected
                        }
                    } else {
                        selectedFileUri = null;
                        textSelectedFileName.setText(R.string.no_file_selected);
                        imageFileIcon.setImageResource(R.drawable.ic_document_black_24dp);
                        imageFileIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorOnSurfaceVariant), android.graphics.PorterDuff.Mode.SRC_IN);
                        btnUploadMaterialFinal.setEnabled(false);
                        Toast.makeText(this, "No file selected.", Toast.LENGTH_SHORT).show();
                    }
                });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            // Grant persistable URI permissions - CRITICAL FIX for "permission denied"
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            try {
                                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            } catch (SecurityException e) {
                                Log.e(TAG, "Failed to take persistable URI permission for thumbnail: " + e.getMessage());
                                Toast.makeText(this, "Permission denied for selected thumbnail. Please ensure app has access.", Toast.LENGTH_LONG).show();
                                // Do not proceed if permission cannot be taken
                                selectedThumbnailUri = null;
                                textSelectedThumbnailName.setText(R.string.no_image_selected);
                                imageThumbnailPreview.setImageResource(R.drawable.ic_image_placeholder);
                                return;
                            }

                            selectedThumbnailUri = uri;
                            textSelectedThumbnailName.setText(getFileName(selectedThumbnailUri));
                            Glide.with(this).load(selectedThumbnailUri).placeholder(R.drawable.ic_image_placeholder).into(imageThumbnailPreview);
                        }
                    } else {
                        selectedThumbnailUri = null;
                        textSelectedThumbnailName.setText(R.string.no_image_selected);
                        imageThumbnailPreview.setImageResource(R.drawable.ic_image_placeholder);
                        Toast.makeText(this, "No thumbnail selected.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(intent);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Only image files
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(intent);
    }

    private void uploadMaterial() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String subject = editTextSubject.getText().toString().trim();
        String materialType = autoCompleteMaterialType.getText().toString().trim();

        // Basic validation
        if (title.isEmpty()) {
            editTextTitle.setError("Title is required.");
            editTextTitle.requestFocus();
            return;
        }
        if (subject.isEmpty()) {
            editTextSubject.setError("Subject is required.");
            editTextSubject.requestFocus();
            return;
        }
        if (materialType.isEmpty()) {
            autoCompleteMaterialType.setError("Material type is required.");
            autoCompleteMaterialType.requestFocus();
            return;
        }
        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file to upload.", Toast.LENGTH_SHORT).show();
            return;
        }

        // FirebaseUser currentUser must be defined
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to upload material.", Toast.LENGTH_SHORT).show();
            // Redirect to sign in activity if current user is null
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        setLoading(true);

        final String uploaderUid = currentUser.getUid();
        final String uploaderName = currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty() ? currentUser.getDisplayName() : "Anonymous";

        // 1. Upload file to Firebase Storage using InputStream
        String fileExtension = getFileExtension(selectedFileUri);
        if (fileExtension == null || fileExtension.isEmpty()) {
            fileExtension = "bin"; // Fallback to a generic binary extension
        }
        String fileStoragePath = "materials/" + UUID.randomUUID().toString() + "." + fileExtension;
        StorageReference fileRef = storage.getReference().child(fileStoragePath);

        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
            if (inputStream == null) {
                setLoading(false);
                Toast.makeText(this, "Failed to open selected file for upload. Input stream is null.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "InputStream for selectedFileUri is null: " + selectedFileUri);
                return;
            }

            fileRef.putStream(inputStream)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return fileRef.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        // Make sure to close the input stream
                        try {
                            if (inputStream != null) inputStream.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Error closing file input stream: " + e.getMessage(), e);
                        }

                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String fileUrl = downloadUri.toString();

                            if (selectedThumbnailUri != null) {
                                uploadThumbnailAndSaveMetadata(fileUrl, title, description, subject, materialType, uploaderUid, uploaderName);
                            } else {
                                // Default thumbnail logic (replace with a real URL from your Firebase Storage)
                                String defaultThumbnailUrl = "https://firebasestorage.googleapis.com/v0/b/book-up-ishola.appspot.com/o/thumbnails%2Fdocument_default.png?alt=media&token=YOUR_IMAGE_TOKEN_HERE";
                                saveMaterialMetadata(fileUrl, defaultThumbnailUrl, title, description, subject, materialType, uploaderUid, uploaderName);
                            }
                        } else {
                setLoading(false);
                Log.e(TAG, "File upload failed: " + task.getException().getMessage(), task.getException());
                String errorMessage = "File upload failed. Ensure Firebase Storage is configured and accessible (check billing/rules). Error: " + task.getException().getMessage();
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }

        });
        } catch (FileNotFoundException e) {
            setLoading(false);
            Log.e(TAG, "File not found for URI: " + selectedFileUri, e);
            Toast.makeText(this, "Selected file not found. Please try again.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            setLoading(false);
            Log.e(TAG, "IOException during file upload setup: " + e.getMessage(), e);
            Toast.makeText(this, "Error processing selected file. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadThumbnailAndSaveMetadata(String fileUrl, String title, String description, String subject, String materialType, String uploaderUid, String uploaderName) {
        String thumbnailExtension = getFileExtension(selectedThumbnailUri);
        if (thumbnailExtension == null || thumbnailExtension.isEmpty()) {
            thumbnailExtension = "jpg"; // Default if cannot determine
        }
        String thumbnailStoragePath = "thumbnails/" + UUID.randomUUID().toString() + "." + thumbnailExtension;
        StorageReference thumbnailRef = storage.getReference().child(thumbnailStoragePath);

        try {
            InputStream thumbnailInputStream = getContentResolver().openInputStream(selectedThumbnailUri);
            if (thumbnailInputStream == null) {
                throw new FileNotFoundException("Thumbnail InputStream is null");
            }

            thumbnailRef.putStream(thumbnailInputStream)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return thumbnailRef.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        try {
                            if (thumbnailInputStream != null) thumbnailInputStream.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Error closing thumbnail input stream: " + e.getMessage(), e);
                        }

                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String thumbnailUrl = downloadUri.toString();
                            saveMaterialMetadata(fileUrl, thumbnailUrl, title, description, subject, materialType, uploaderUid, uploaderName);
                        } else {
                Log.e(TAG, "Thumbnail upload failed: " + task.getException().getMessage(), task.getException());
                String errorMessage = "Thumbnail upload failed. Ensure Firebase Storage is configured and accessible (check billing/rules). Error: " + task.getException().getMessage();
                Toast.makeText(this, errorMessage + " Using default thumbnail.", Toast.LENGTH_LONG).show();
                String defaultThumbnailUrl = "https://firebasestorage.googleapis.com/v0/b/book-up-ishola.appspot.com/o/thumbnails%2Fdocument_default.png?alt=media&token=YOUR_IMAGE_TOKEN_HERE"; // IMPORTANT: REPLACE
                saveMaterialMetadata(fileUrl, defaultThumbnailUrl, title, description, subject, materialType, uploaderUid, uploaderName);
            }

        });
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Thumbnail file not found for URI: " + selectedThumbnailUri, e);
            Toast.makeText(this, "Selected thumbnail not found. Using default.", Toast.LENGTH_LONG).show();
            String defaultThumbnailUrl = "https://firebasestorage.googleapis.com/v0/b/book-up-ishola.appspot.com/o/thumbnails%2Fdocument_default.png?alt=media&token=YOUR_IMAGE_TOKEN_HERE"; // IMPORTANT: REPLACE
            saveMaterialMetadata(fileUrl, defaultThumbnailUrl, title, description, subject, materialType, uploaderUid, uploaderName);
        } catch (IOException e) {
            Log.e(TAG, "IOException during thumbnail upload setup: " + e.getMessage(), e);
            Toast.makeText(this, "Error processing selected thumbnail. Using default.", Toast.LENGTH_LONG).show();
            String defaultThumbnailUrl = "https://firebasestorage.googleapis.com/v0/b/book-up-ishola.appspot.com/o/thumbnails%2Fdocument_default.png?alt=media&token=YOUR_IMAGE_TOKEN_HERE"; // IMPORTANT: REPLACE
            saveMaterialMetadata(fileUrl, defaultThumbnailUrl, title, description, subject, materialType, uploaderUid, uploaderName);
        }
    }

    private void saveMaterialMetadata(String fileUrl, String thumbnailUrl, String title, String description, String subject, String materialType, String uploaderUid, String uploaderName) {
        // Prepare data for Firestore
        Map<String, Object> materialData = new HashMap<>();
        materialData.put("title", title);
        materialData.put("description", description);
        materialData.put("subject", subject);
        materialData.put("type", materialType);
        materialData.put("downloadUrl", fileUrl);
        materialData.put("thumbnailUrl", thumbnailUrl);
        materialData.put("uploaderUid", uploaderUid);
        materialData.put("uploaderName", uploaderName);
        materialData.put("uploadDate", FieldValue.serverTimestamp());
        materialData.put("downloads", 0);
        materialData.put("averageRating", 0.0);

        db.collection("studyMaterials")
                .add(materialData)
                .addOnSuccessListener(documentReference -> {
                    // Update the material with its Firestore auto-generated ID
                    documentReference.update("id", documentReference.getId())
                            .addOnSuccessListener(aVoid -> {
                                setLoading(false);
                                Toast.makeText(this, "Material uploaded successfully!", Toast.LENGTH_LONG).show();
                                finish(); // Go back to the previous activity (ProfileFragment)
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                Log.e(TAG, "Failed to update material ID in Firestore: " + e.getMessage(), e);
                                Toast.makeText(this, "Material uploaded, but failed to set ID.", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Failed to save material metadata to Firestore: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to upload material: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBarUpload.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnUploadMaterialFinal.setEnabled(!isLoading);
        btnSelectFile.setEnabled(!isLoading);
        btnSelectThumbnail.setEnabled(!isLoading);
        editTextTitle.setEnabled(!isLoading);
        editTextDescription.setEnabled(!isLoading);
        editTextSubject.setEnabled(!isLoading);
        autoCompleteMaterialType.setEnabled(!isLoading);
        // Disable back button on toolbar too
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
    }

    // Helper to get file name from Uri
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name from content URI: " + e.getMessage());
            }
        }
        if (result == null) {
            // Fallback for file:// or other URIs
            result = uri.getLastPathSegment();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "Unknown File";
    }

    // Helper to get file extension from Uri
    private String getFileExtension(Uri uri) {
        String extension = null;
        if (uri != null) {
            String type = getContentResolver().getType(uri);
            if (type != null) {
                // For content URIs, try to get extension from MIME type
                String[] parts = type.split("/");
                if (parts.length > 1) {
                    extension = parts[1]; // e.g., "pdf", "jpeg"
                }
            }
            if (extension == null && uri.getLastPathSegment() != null) {
                // Fallback to path segment
                int dot = uri.getLastPathSegment().lastIndexOf('.');
                if (dot >= 0) {
                    extension = uri.getLastPathSegment().substring(dot + 1);
                }
            }
        }
        return extension;
    }

    // Helper to determine file icon based on extension/type
    private int getFileIcon(String fileName) {
        if (fileName == null) return R.drawable.ic_document_black_24dp; // Default icon

        String lowerCaseFileName = fileName.toLowerCase();
        if (lowerCaseFileName.endsWith(".pdf")) {
            return R.drawable.ic_pdf_black_24dp;
        } else if (lowerCaseFileName.endsWith(".doc") || lowerCaseFileName.endsWith(".docx")) {
            return R.drawable.ic_word_black_24dp;
        } else if (lowerCaseFileName.endsWith(".ppt") || lowerCaseFileName.endsWith(".pptx")) {
            return R.drawable.ic_powerpoint_black_24dp;
        } else if (lowerCaseFileName.endsWith(".xls") || lowerCaseFileName.endsWith(".xlsx")) {
            return R.drawable.ic_excel_black_24dp;
        } else if (lowerCaseFileName.endsWith(".mp4") || lowerCaseFileName.endsWith(".avi") || lowerCaseFileName.endsWith(".mov")) {
            return R.drawable.ic_video_black_24dp;
        } else if (lowerCaseFileName.endsWith(".mp3") || lowerCaseFileName.endsWith(".wav") || lowerCaseFileName.endsWith(".aac")) {
            return R.drawable.ic_audio_black_24dp;
        } else if (lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg") || lowerCaseFileName.endsWith(".png") || lowerCaseFileName.endsWith(".gif")) {
            return R.drawable.ic_image_black_24dp;
        }
        return R.drawable.ic_document_black_24dp;
    }
}
