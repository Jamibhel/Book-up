package com.example.bookup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bookup.R;
import com.example.bookup.models.Review;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReviewsBottomSheetFragment extends BottomSheetDialogFragment {

    private String tutorId;
    private String tutorName;
    private RatingBar ratingBar;
    private EditText commentEditText;
    private Button submitButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

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

        ratingBar = view.findViewById(R.id.rating_bar);
        commentEditText = view.findViewById(R.id.edit_comment);
        submitButton = view.findViewById(R.id.btn_submit_review);

        submitButton.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String comment = commentEditText.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(getContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

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
        review.setRating(rating);
        review.setComment(comment);

        // Save review
        db.collection("tutorReviews").add(review)
                .addOnSuccessListener(documentReference -> {
                    review.setId(documentReference.getId());
                    updateTutorRating();
                    Toast.makeText(getContext(), "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to submit review: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    private void updateTutorRating() {
        // Recalculate average rating and update tutor document
        db.collection("tutorReviews")
                .whereEqualTo("tutorId", tutorId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        float totalRating = 0;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            Review review = doc.toObject(Review.class);
                            if (review != null) {
                                totalRating += review.getRating();
                            }
                        }
                        float averageRating = totalRating / queryDocumentSnapshots.size();

                        db.collection("users").document(tutorId)
                                .update("rating", averageRating, "reviewCount", queryDocumentSnapshots.size())
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to update rating", 
                                        Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }
}
