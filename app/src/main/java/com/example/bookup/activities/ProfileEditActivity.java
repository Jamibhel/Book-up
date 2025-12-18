package com.example.bookup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView; // Import for ShapeableImageView
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "ProfileEditActivity";

    // UI Elements
    private ShapeableImageView imageProfilePictureEdit; // New: For profile picture
    private MaterialButton btnChangePictureInEdit; // New: Button to pick image
    private TextInputEditText editTextFirstName;
    private TextInputEditText editTextLastName;
    private TextInputEditText editTextPhoneNumber;
    private RadioGroup radioGroupGender;
    private RadioButton radioMale;
    private RadioButton radioFemale;
    private RadioGroup radioGroupRole;
    private RadioButton radioStudent;
    private RadioButton radioTutor;
    private MaterialButton btnSaveProfile;
    private MaterialButton btnCancelEdit;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage; // New: For Firebase Storage
    private StorageReference storageRef; // New: For Firebase Storage

    // Image URI
    private Uri selectedImageUri; // New: To hold the URI of the selected image

    // ActivityResultLauncher for picking images
    private ActivityResultLauncher<String> pickImageLauncher; // New

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_profile_edit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance(); // Initialize Firebase Storage
        storageRef = storage.getReference(); // Get root reference for Storage

        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to edit your profile.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        loadUserProfile();
        setupImagePickerLauncher(); // Setup the image picker
        setupClickListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        imageProfilePictureEdit = findViewById(R.id.image_profile_picture_edit); // New
        btnChangePictureInEdit = findViewById(R.id.btn_change_picture_in_edit); // New
        editTextFirstName = findViewById(R.id.edit_text_first_name);
        editTextLastName = findViewById(R.id.edit_text_last_name);
        editTextPhoneNumber = findViewById(R.id.edit_text_phone_number);
        radioGroupGender = findViewById(R.id.radio_group_gender);
        radioMale = findViewById(R.id.radio_male);
        radioFemale = findViewById(R.id.radio_female);
        radioGroupRole = findViewById(R.id.radio_group_role);
        radioStudent = findViewById(R.id.radio_student);
        radioTutor = findViewById(R.id.radio_tutor);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnCancelEdit = findViewById(R.id.btn_cancel_edit);
        progressBar = findViewById(R.id.progress_bar_edit_profile);
    }

    private void loadUserProfile() {
        if (currentUser == null) return;

        setLoading(true);

        // Load current profile picture preview
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_default_profile_picture)
                    .error(R.drawable.ic_profile_black_24dp)
                    .into(imageProfilePictureEdit);
            selectedImageUri = currentUser.getPhotoUrl(); // Set as current image
        } else {
            imageProfilePictureEdit.setImageResource(R.drawable.ic_profile_black_24dp);
        }

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        String gender = documentSnapshot.getString("gender");
                        String role = documentSnapshot.getString("role");

                        editTextFirstName.setText(firstName);
                        editTextLastName.setText(lastName);
                        editTextPhoneNumber.setText(phoneNumber);

                        if ("Male".equals(gender)) {
                            radioMale.setChecked(true);
                        } else if ("Female".equals(gender)) {
                            radioFemale.setChecked(true);
                        }

                        if ("Student".equals(role)) {
                            radioStudent.setChecked(true);
                        } else if ("Tutor".equals(role)) {
                            radioTutor.setChecked(true);
                        }
                    } else {
                        Log.d(TAG, "User document does not exist, creating new default profile.");
                        String displayName = currentUser.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            String[] parts = displayName.split(" ", 2);
                            if (parts.length > 0) editTextFirstName.setText(parts[0]);
                            if (parts.length > 1) editTextLastName.setText(parts[1]);
                        }
                        Toast.makeText(this, "Profile not found, please set your details.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Image upload failed: " + e.getMessage(), e);
                    String errorMessage = "Image upload failed. Ensure Firebase Storage is configured and accessible (check billing/rules). Error: " + e.getMessage();
                    Toast.makeText(ProfileEditActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
    }

    private void setupImagePickerLauncher() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Grant persistable URI permissions
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);

                        selectedImageUri = uri;
                        imageProfilePictureEdit.setImageURI(uri); // Display selected image
                        // No need for a separate btnUploadImage.setEnabled(true); here if we are saving everything at once
                    } else {
                        // If no image is selected, revert to the current profile picture
                        // or clear the preview if it was a new selection attempt.
                        if (currentUser.getPhotoUrl() != null) {
                            selectedImageUri = currentUser.getPhotoUrl();
                            Glide.with(this).load(currentUser.getPhotoUrl()).into(imageProfilePictureEdit);
                        } else {
                            selectedImageUri = null;
                            imageProfilePictureEdit.setImageResource(R.drawable.ic_profile_black_24dp);
                        }
                        Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void setupClickListeners() {
        btnChangePictureInEdit.setOnClickListener(v -> pickImageLauncher.launch("image/*")); // Launch picker
        btnSaveProfile.setOnClickListener(v -> saveUserProfile());
        btnCancelEdit.setOnClickListener(v -> finish());
    }

    private void saveUserProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "Not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String gender = "";
        String role = "";

        if (radioMale.isChecked()) {
            gender = "Male";
        } else if (radioFemale.isChecked()) {
            gender = "Female";
        }

        if (radioStudent.isChecked()) {
            role = "Student";
        } else if (radioTutor.isChecked()) {
            role = "Tutor";
        }

        // --- Basic Validation ---
        if (TextUtils.isEmpty(firstName)) {
            editTextFirstName.setError("First name is required.");
            editTextFirstName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(lastName)) {
            editTextLastName.setError("Last name is required.");
            editTextLastName.requestFocus();
            return;
        }
        // Assuming 10 digits after prefix for Nigeria
        if (!phoneNumber.isEmpty() && phoneNumber.length() != 10) {
            editTextPhoneNumber.setError("Phone number must be 10 digits.");
            editTextPhoneNumber.requestFocus();
            return;
        }
        if (gender.isEmpty()) {
            Toast.makeText(this, "Please select a gender.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (role.isEmpty()) {
            Toast.makeText(this, "Please select a role.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Handle profile picture update first if a new one was selected
        if (selectedImageUri != null && !selectedImageUri.equals(currentUser.getPhotoUrl())) {
            uploadImageAndSaveProfile(firstName, lastName, phoneNumber, gender, role);
        } else {
            // No new image selected or current image unchanged, just save other profile data
            updateFirestoreAndAuthProfile(firstName, lastName, phoneNumber, gender, role, currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null);
        }
    }

    private void uploadImageAndSaveProfile(String firstName, String lastName, String phoneNumber, String gender, String role) {
        if (selectedImageUri == null) {
            // This case should ideally not happen if check was done before calling
            updateFirestoreAndAuthProfile(firstName, lastName, phoneNumber, gender, role, currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null);
            return;
        }

        final String userId = currentUser.getUid();
        StorageReference profilePicRef = storageRef.child("profile_pictures/" + userId + ".jpg");

        profilePicRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> profilePicRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            updateFirestoreAndAuthProfile(firstName, lastName, phoneNumber, gender, role, downloadUrl);
                        })
                        .addOnFailureListener(e -> {
                            setLoading(false);
                            Log.e(TAG, "Failed to get download URL: " + e.getMessage(), e);
                            Toast.makeText(ProfileEditActivity.this, "Failed to get image URL.", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Image upload failed: " , e);
                    Toast.makeText(ProfileEditActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                });
    }


    private void updateFirestoreAndAuthProfile(String firstName, String lastName, String phoneNumber, String gender, String role, String photoUrl) {
        // 1. Update Firestore document
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("firstName", firstName);
        userUpdates.put("lastName", lastName);
        userUpdates.put("fullName", firstName + " " + lastName);
        userUpdates.put("phoneNumber", phoneNumber);
        userUpdates.put("gender", gender);
        userUpdates.put("role", role);
        userUpdates.put("isTutor", "Tutor".equals(role));
        if (photoUrl != null) {
            userUpdates.put("profilePicUrl", photoUrl); // Only update if a photo URL is provided
        }

        db.collection("users").document(currentUser.getUid())
                .set(userUpdates) // Using set with default (merge=false), ensures full overwrite of these fields
                .addOnSuccessListener(aVoid -> {
                    // 2. Update Firebase Auth profile (display name and photo URL)
                    UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder()
                            .setDisplayName(firstName + " " + lastName);

                    if (photoUrl != null) {
                        profileUpdatesBuilder.setPhotoUri(Uri.parse(photoUrl));
                    }

                    currentUser.updateProfile(profileUpdatesBuilder.build())
                            .addOnCompleteListener(task -> {
                                setLoading(false);
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User Auth profile updated.");
                                } else {
                                    Log.e(TAG, "Failed to update Auth profile: " + task.getException().getMessage());
                                }
                                Toast.makeText(ProfileEditActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Error updating Firestore document: " + e.getMessage(), e);
                    Toast.makeText(ProfileEditActivity.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!isLoading);
        btnCancelEdit.setEnabled(!isLoading);
        imageProfilePictureEdit.setEnabled(!isLoading);
        btnChangePictureInEdit.setEnabled(!isLoading);
        editTextFirstName.setEnabled(!isLoading);
        editTextLastName.setEnabled(!isLoading);
        editTextPhoneNumber.setEnabled(!isLoading);
        radioGroupGender.setEnabled(!isLoading);
        radioMale.setEnabled(!isLoading);
        radioFemale.setEnabled(!isLoading);
        radioGroupRole.setEnabled(!isLoading);
        radioStudent.setEnabled(!isLoading);
        radioTutor.setEnabled(!isLoading);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
    }
}
