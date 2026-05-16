package com.example.bookup.services;

import android.util.Log;
import com.example.bookup.models.User;
import com.example.bookup.models.Tutor;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class MigrationService {
    private static final String TAG = "MigrationService";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void migrateTutorsToUsers() {
        Log.d(TAG, "Starting migration: Tutors -> Users");

        db.collection("tutors").get().addOnSuccessListener(querySnapshot -> {
            WriteBatch batch = db.batch();
            int count = 0;

            for (QueryDocumentSnapshot tutorDoc : querySnapshot) {
                String tutorId = tutorDoc.getId(); // Assuming document ID is the User ID
                Tutor tutor = tutorDoc.toObject(Tutor.class);

                Map<String, Object> updates = new HashMap<>();
                updates.put("role", "tutor");
                updates.put("tutoringSubjects", tutor.getSubjects());
                updates.put("rating", tutor.getRating());
                updates.put("reviewCount", tutor.getReviewCount());
                updates.put("isAvailable", tutor.isAvailable());
                updates.put("bio", tutor.getBio());

                // Update the user document
                batch.update(db.collection("users").document(tutorId), updates);
                count++;
                
                // Firestore batches are limited to 500 operations
                if (count >= 450) {
                    batch.commit();
                    batch = db.batch();
                    count = 0;
                }
            }

            batch.commit().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Successfully migrated tutors to users collection.");
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Migration failed", e);
            });
        });
    }
}
