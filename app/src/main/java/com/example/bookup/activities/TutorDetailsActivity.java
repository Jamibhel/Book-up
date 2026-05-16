package com.example.bookup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.adapters.AvailabilityAdapter;
import com.example.bookup.adapters.ReviewAdapter;
import com.example.bookup.models.Review;
import com.example.bookup.models.Tutor;
import com.example.bookup.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.Locale;

public class TutorDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_TUTOR = "extra_tutor";
    private ImageView tutorProfileImage;
    private TextView tutorName, tutorRating, tutorBio, textNoReviews, textTutorHourlyRate;
    private TextView textTutorLocation, textTutorWorkPreference;
    private ChipGroup chipGroupTutorSubjects;
    private MaterialButton btnBookSession, btnMessageTutor;
    private RecyclerView recyclerAvailability, recyclerReviews;
    
    private Tutor currentTutor;
    private ReviewAdapter reviewAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration tutorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_details);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        
        String tutorId = getIntent().getStringExtra("tutorId");
        if (tutorId != null) loadTutorById(tutorId);
        else finish();

        setupClickListeners();
    }

    private void initViews() {
        tutorProfileImage = findViewById(R.id.tutor_profile_image);
        tutorName = findViewById(R.id.tutor_name);
        tutorRating = findViewById(R.id.tutor_rating);
        tutorBio = findViewById(R.id.tutor_bio);
        textTutorLocation = findViewById(R.id.text_tutor_location);
        textTutorWorkPreference = findViewById(R.id.text_tutor_work_preference);
        chipGroupTutorSubjects = findViewById(R.id.chip_group_tutor_subjects);
        btnBookSession = findViewById(R.id.btn_book_session);
        btnMessageTutor = findViewById(R.id.btn_message_tutor);
        recyclerAvailability = findViewById(R.id.recycler_tutor_availability);
        recyclerReviews = findViewById(R.id.recycler_tutor_reviews);
        textNoReviews = findViewById(R.id.text_no_reviews);
        textTutorHourlyRate = findViewById(R.id.text_tutor_hourly_rate);
        
        reviewAdapter = new ReviewAdapter();
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setAdapter(reviewAdapter);

        findViewById(R.id.toolbar).setOnClickListener(v -> onBackPressed());
    }

    private void loadTutorById(String id) {
        tutorListener = db.collection("users").document(id).addSnapshotListener((doc, e) -> {
            if (doc != null && doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    currentTutor = new Tutor();
                    currentTutor.setUid(id);
                    currentTutor.setName(user.getDisplayName());
                    currentTutor.setProfileImageUrl(user.getPhotoUrl());
                    currentTutor.setBio(user.getBio());
                    currentTutor.setSubjects(user.getTutoringSubjects());
                    currentTutor.setRating(user.getRating());
                    currentTutor.setReviewCount(user.getReviewCount());
                    
                    displayTutorDetails(user);
                    loadReviews(id);
                }
            }
        });
    }

    private void displayTutorDetails(User user) {
        tutorName.setText(user.getDisplayName());
        tutorRating.setText(String.format(Locale.getDefault(), "%.1f (%d reviews)", user.getRating(), user.getReviewCount()));
        tutorBio.setText(user.getBio() != null && !user.getBio().isEmpty() ? user.getBio() : "No bio available.");
        textTutorHourlyRate.setText(String.format(Locale.getDefault(), "₦%.0f / hr", user.getHourlyRate()));

        // Location Info
        String loc = user.getLocationName();
        textTutorLocation.setText(loc != null && !loc.isEmpty() ? loc : "No specific location set");
        
        String pref = user.getWorkPreference();
        String prefText = "Offers Online & In-Person";
        if ("online".equals(pref)) prefText = "Online Sessions Only";
        else if ("in_person".equals(pref)) prefText = "In-Person Sessions Only";
        textTutorWorkPreference.setText(prefText);

        Glide.with(this).load(user.getPhotoUrl()).placeholder(R.drawable.ic_profile_placeholder).into(tutorProfileImage);

        chipGroupTutorSubjects.removeAllViews();
        if (user.getTutoringSubjects() != null) {
            for (String s : user.getTutoringSubjects()) {
                Chip chip = new Chip(this);
                chip.setText(s);
                chip.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.primary_faded));
                chipGroupTutorSubjects.addView(chip);
            }
        }

        if (user.getAvailability() != null) {
            recyclerAvailability.setLayoutManager(new LinearLayoutManager(this));
            recyclerAvailability.setAdapter(new AvailabilityAdapter(user.getAvailability(), false));
        }
    }

    private void loadReviews(String tutorId) {
        db.collection("reviews")
                .whereEqualTo("tutorId", tutorId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        List<Review> reviews = value.toObjects(Review.class);
                        reviewAdapter.setReviews(reviews);
                        textNoReviews.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void setupClickListeners() {
        btnMessageTutor.setOnClickListener(v -> {
            FirebaseUser cu = mAuth.getCurrentUser();
            if (cu == null || currentTutor == null) return;
            
            User me = new User();
            me.setId(cu.getUid());
            me.setDisplayName(cu.getDisplayName());
            me.setPhotoUrl(cu.getPhotoUrl() != null ? cu.getPhotoUrl().toString() : "");

            User other = new User();
            other.setId(currentTutor.getUid());
            other.setDisplayName(currentTutor.getName());
            other.setPhotoUrl(currentTutor.getProfileImageUrl());

            new com.example.bookup.repositories.ChatRepository().getOrCreateChatChannel(me, other)
                .addOnSuccessListener(id -> {
                    Intent intent = new Intent(this, HomePageActivity.class);
                    intent.putExtra("channelId", id);
                    intent.putExtra("channelName", other.getDisplayName());
                    intent.putExtra("tabIndex", 4); // Index for Chat tab
                    startActivity(intent);
                });
        });

        btnBookSession.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingSessionActivity.class);
            intent.putExtra("tutorId", currentTutor.getUid());
            intent.putExtra("tutorName", currentTutor.getName());
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tutorListener != null) tutorListener.remove();
    }
}
