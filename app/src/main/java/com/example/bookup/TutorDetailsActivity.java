package com.example.bookup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.bookup.models.Tutor;
import com.google.android.material.button.MaterialButton; // Ensure this is MaterialButton
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;

public class TutorDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_TUTOR = "extra_tutor"; // Key for passing Tutor object

    private static final String TAG = "TutorDetailsActivity";

    // UI Elements
    private ShapeableImageView tutorProfileImage;
    private TextView tutorName;
    private TextView tutorRating;
    private TextView tutorBio;
    private ChipGroup chipGroupTutorSubjects; // Changed to ChipGroup
    private TextView textNoTutorSubjects; // NEW for empty subjects
    private MaterialButton btnBookSession; // Changed to MaterialButton
    private MaterialButton btnMessageTutor; // Changed to MaterialButton
    private ProgressBar progressBarTutorDetails;

    // Data
    private Tutor currentTutor;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_details);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.tutor_details_title); // Use string resource
        }

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        initViews();

        // Get Tutor object from Intent
        if (getIntent().hasExtra(EXTRA_TUTOR)) {
            currentTutor = (Tutor) getIntent().getSerializableExtra(EXTRA_TUTOR);
            if (currentTutor != null) {
                displayTutorDetails();
            } else {
                Toast.makeText(this, "Error loading tutor details.", Toast.LENGTH_SHORT).show();
                finish(); // Go back if no tutor found
            }
        } else {
            Toast.makeText(this, "No tutor provided.", Toast.LENGTH_SHORT).show();
            finish(); // Go back if no tutor provided
        }

        setupClickListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Handle toolbar back button
        return true;
    }

    private void initViews() {
        tutorProfileImage = findViewById(R.id.tutor_profile_image);
        tutorName = findViewById(R.id.tutor_name);
        tutorRating = findViewById(R.id.tutor_rating);
        tutorBio = findViewById(R.id.tutor_bio);
        chipGroupTutorSubjects = findViewById(R.id.chip_group_tutor_subjects); // Initialize ChipGroup
        textNoTutorSubjects = findViewById(R.id.text_no_tutor_subjects); // Initialize TextView for no subjects
        btnBookSession = findViewById(R.id.btn_book_session);
        btnMessageTutor = findViewById(R.id.btn_message_tutor);
        progressBarTutorDetails = findViewById(R.id.progress_bar_tutor_details);
    }

    private void displayTutorDetails() {
        if (currentTutor == null) return;

        setLoading(true); // Show progress bar initially

        tutorName.setText(currentTutor.getName());
        tutorRating.setText(String.format(Locale.getDefault(), "%.1f (%d %s)",
                currentTutor.getRating(),
                currentTutor.getReviewCount(),
                currentTutor.getReviewCount() == 1 ? "review" : "reviews")); // Pluralization

        tutorBio.setText(TextUtils.isEmpty(currentTutor.getBio()) ? getString(R.string.no_bio_available) : currentTutor.getBio());

        // Display subjects using ChipGroup
        chipGroupTutorSubjects.removeAllViews();
        List<String> subjects = currentTutor.getSubjects();
        if (subjects != null && !subjects.isEmpty()) {
            textNoTutorSubjects.setVisibility(View.GONE);
            for (String subject : subjects) {
                Chip chip = createSubjectChip(subject);
                chipGroupTutorSubjects.addView(chip);
            }
        } else {
            textNoTutorSubjects.setVisibility(View.VISIBLE);
        }

        // Load profile image using Glide
        if (currentTutor.getProfileImageUrl() != null && !currentTutor.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentTutor.getProfileImageUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_black_24dp) // Fallback to a generic profile icon on error
                    .into(tutorProfileImage);
        } else {
            tutorProfileImage.setImageResource(R.drawable.ic_profile_black_24dp); // Default if no URL
        }

        // Adjust buttons based on whether the current user is this tutor
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(currentTutor.getUid())) {
            // If current user is viewing their OWN tutor profile
            btnBookSession.setText(R.string.edit_my_profile_button);
            btnMessageTutor.setText(R.string.view_my_materials_button);
            btnBookSession.setIconResource(R.drawable.ic_edit_black_24dp); // Assuming edit icon exists
            btnMessageTutor.setIconResource(R.drawable.ic_book_black_24dp); // Assuming book icon exists
            // Hide message button or change its functionality
        } else {
            // Normal view for other users
            btnBookSession.setText(R.string.book_session_button);
            btnMessageTutor.setText(R.string.message_button);
            btnBookSession.setIconResource(R.drawable.ic_event_black_24dp); // Assuming event icon exists
            btnMessageTutor.setIconResource(R.drawable.ic_chat_black_24dp); // Assuming chat icon exists
        }

        setLoading(false); // Hide progress bar after displaying details
    }

    private void setupClickListeners() {
        // These listeners will be dynamic based on the currentUser, set in displayTutorDetails()
        // For general users:
        btnBookSession.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                if (mAuth.getCurrentUser().getUid().equals(currentTutor.getUid())) {
                    // This case is handled in displayTutorDetails, but as a fallback
                    Toast.makeText(this, "Navigating to your profile for editing.", Toast.LENGTH_SHORT).show();
                    // startActivity(new Intent(this, ProfileEditActivity.class)); // TODO: Add ProfileEditActivity import and launch
                } else {
                    Toast.makeText(this, "Book session with " + currentTutor.getName() + " (Coming Soon!)", Toast.LENGTH_SHORT).show();
                    // TODO: Implement session booking functionality
                }
            } else {
                Toast.makeText(this, "Please sign in to book a session.", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to sign-in activity
            }
        });

        btnMessageTutor.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                if (mAuth.getCurrentUser().getUid().equals(currentTutor.getUid())) {
                    // This case is handled in displayTutorDetails, but as a fallback
                    Toast.makeText(this, "Navigating to your uploaded materials.", Toast.LENGTH_SHORT).show();
                    // TODO: Launch MyMaterialsActivity or similar
                } else {
                    Toast.makeText(this, "Message " + currentTutor.getName() + " (Coming Soon!)", Toast.LENGTH_SHORT).show();
                    // TODO: Implement messaging functionality
                }
            } else {
                Toast.makeText(this, "Please sign in to message tutors.", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to sign-in activity
            }
        });
    }

    /**
     * Creates a Chip view for displaying a tutor's teaching subject.
     * @param subjectName The name of the subject.
     * @return Configured Chip view.
     */
    private Chip createSubjectChip(String subjectName) {
        Chip chip = new Chip(this);
        chip.setText(subjectName);
        chip.setCheckable(false);
        chip.setClickable(false);
        // Using ContextCompat.getColorStateList for theme attributes
        chip.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.colorSecondaryContainer));
        chip.setTextColor(ContextCompat.getColorStateList(this, R.color.colorOnSecondaryContainer));
        return chip;
    }

    private void setLoading(boolean isLoading) {
        progressBarTutorDetails.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // Disable interaction with main content if loading
        tutorProfileImage.setEnabled(!isLoading);
        tutorName.setEnabled(!isLoading);
        tutorRating.setEnabled(!isLoading);
        tutorBio.setEnabled(!isLoading);
        chipGroupTutorSubjects.setEnabled(!isLoading);
        btnBookSession.setEnabled(!isLoading);
        btnMessageTutor.setEnabled(!isLoading);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
        // Could also hide/show other cards as a whole during initial load
    }
}
