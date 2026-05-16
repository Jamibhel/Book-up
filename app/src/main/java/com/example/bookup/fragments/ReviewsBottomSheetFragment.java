package com.example.bookup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.bookup.R;
import com.example.bookup.models.Review;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReviewsBottomSheetFragment extends BottomSheetDialogFragment {

    private String tutorId;
    private String tutorName;
    private EditText commentEditText;
    private Button submitButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private float currentRating = 0f;
    private ImageButton[] starButtons = new ImageButton[5];
    private TextView ratingPreview;

    public static ReviewsBottomSheetFragment newInstance(String tutorId, String tutorName) {
        ReviewsBottomSheetFragment fragment = new ReviewsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("tutorId", tutorId);
        args.putString("tutorName", tutorName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tutorId = getArguments().getString("tutorId");
            tutorName = getArguments().getString("tutorName");
        }
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commentEditText = view.findViewById(R.id.edit_comment);
        submitButton = view.findViewById(R.id.btn_submit_review);
        ratingPreview = view.findViewById(R.id.rating_preview);

        // Wire up star buttons for interactive selection
        starButtons[0] = view.findViewById(R.id.star_1);
        starButtons[1] = view.findViewById(R.id.star_2);
        starButtons[2] = view.findViewById(R.id.star_3);
        starButtons[3] = view.findViewById(R.id.star_4);
        starButtons[4] = view.findViewById(R.id.star_5);

        setupStarRating();

        submitButton.setOnClickListener(v -> submitReview());

        // Configure bottom sheet behavior
        if (getDialog() instanceof com.google.android.material.bottomsheet.BottomSheetDialog) {
            com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                    (com.google.android.material.bottomsheet.BottomSheetDialog) getDialog();
            bottomSheetDialog.setOnShowListener(dialog -> {
                com.google.android.material.bottomsheet.BottomSheetBehavior<?> behavior =
                        bottomSheetDialog.getBehavior();
                behavior.setPeekHeight(800); // Set initial peek height to 800dp
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            });
        }
    }

    private void setupStarRating() {
        for (int i = 0; i < starButtons.length; i++) {
            final int ratingValue = i + 1;
            starButtons[i].setOnClickListener(v -> {
                setRating(ratingValue);
                animateStar(starButtons[ratingValue - 1]);
            });
        }
    }

    private void setRating(int rating) {
        currentRating = rating;
        updateStarDisplay();
        updateRatingPreview();
    }

    private void updateStarDisplay() {
        int filledColor = ContextCompat.getColor(requireContext(), R.color.primary);
        int outlineColor = ContextCompat.getColor(requireContext(), R.color.black);

        for (int i = 0; i < starButtons.length; i++) {
            ImageButton star = starButtons[i];
            if (i < currentRating) {
                // Filled star
                star.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_filled_24dp));
                star.setColorFilter(filledColor);
            } else {
                // Outline star
                star.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_outline_24dp));
                star.setColorFilter(outlineColor);
            }
        }
    }

    private void updateRatingPreview() {
        if (ratingPreview != null) {
            ratingPreview.setText(String.format("Rating: %.0f / 5", currentRating));
        }
    }

    private void animateStar(ImageButton star) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(300);
        scaleAnimation.setRepeatCount(0);
        star.startAnimation(scaleAnimation);
    }

    private void submitReview() {
        if (currentRating == 0) {
            Toast.makeText(getContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = commentEditText.getText().toString().trim();

        if (comment.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a review comment", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        Review review = new Review(
                tutorId,
                currentUser.getUid(),
                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous",
                currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : ""
        );
        review.setRating(currentRating);
        review.setComment(comment);

        // Save review
        db.collection("reviews").add(review)
                .addOnSuccessListener(documentReference -> {
                    review.setId(documentReference.getId());
                    updateTutorRating();
                    Toast.makeText(getContext(), "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    // Give clearer feedback for permission issues
                    String message = "Failed to submit review";
                    if (e instanceof com.google.firebase.firestore.FirebaseFirestoreException) {
                        com.google.firebase.firestore.FirebaseFirestoreException ffe = (com.google.firebase.firestore.FirebaseFirestoreException) e;
                        if (ffe.getCode() == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            message = "Permission denied: you don't have permission to submit reviews. Please sign in or contact support.";
                        } else {
                            message = "Failed to submit review: " + ffe.getMessage();
                        }
                    } else {
                        message = "Failed to submit review: " + e.getMessage();
                    }

                    if (getContext() != null) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    }
                    android.util.Log.e("ReviewsBottomSheet", "Error submitting review", e);
                });
    }

    private void updateTutorRating() {
        // Recalculate average rating and update tutor document
        db.collection("reviews")
                .whereEqualTo("tutorId", tutorId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        java.util.List<Float> ratings = new java.util.ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            Review review = doc.toObject(Review.class);
                            if (review != null) {
                                ratings.add(review.getRating());
                            }
                        }
                        double averageRating = com.example.bookup.utils.RatingUtils.computeAverage(ratings);

                        db.collection("users").document(tutorId)
                                .update("rating", averageRating, "reviewCount", queryDocumentSnapshots.size())
                                .addOnFailureListener(e -> {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "Failed to update rating",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }
}
