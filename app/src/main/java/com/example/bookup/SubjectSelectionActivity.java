package com.example.bookup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap; // Import for HashMap
import java.util.HashSet;
import java.util.List;
import java.util.Map; // Import for Map
import java.util.Set;

public class SubjectSelectionActivity extends AppCompatActivity {

    private static final String TAG = "SubjectSelectionAct";

    // UI Elements
    private ChipGroup chipGroupCurrentSubjects;
    private TextView textNoCurrentSubjects;
    private ChipGroup chipGroupAvailableSubjects;
    private TextView textLoadingSubjects;
    private MaterialButton btnSaveSubjects;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Data
    private List<String> allAvailableSubjects;
    private Set<String> userSelectedSubjects; // Still using a Set for efficient in-memory management

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_selection);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Select Subjects");
        }

        // Initialize UI components
        chipGroupCurrentSubjects = findViewById(R.id.chip_group_current_subjects);
        textNoCurrentSubjects = findViewById(R.id.text_no_current_subjects);
        chipGroupAvailableSubjects = findViewById(R.id.chip_group_available_subjects);
        textLoadingSubjects = findViewById(R.id.text_loading_subjects);
        btnSaveSubjects = findViewById(R.id.btn_save_subjects);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to manage subjects.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userSelectedSubjects = new HashSet<>();
        initializeAllAvailableSubjects();
        loadUserSubjects();
        setupClickListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initializeAllAvailableSubjects() {
        allAvailableSubjects = Arrays.asList(
                "Mathematics", "Algebra", "Calculus", "Geometry", "Statistics",
                "Physics", "Mechanics", "Thermodynamics", "Electromagnetism", "Quantum Physics",
                "Chemistry", "Organic Chemistry", "Inorganic Chemistry", "Biochemistry", "Physical Chemistry",
                "Biology", "Genetics", "Ecology", "Anatomy", "Physiology",
                "Computer Science", "Programming", "Data Structures", "Algorithms", "Web Development",
                "History", "World History", "European History", "American History", "Ancient Civilizations",
                "English", "Literature", "Writing", "Grammar", "Creative Writing",
                "Economics", "Microeconomics", "Macroeconomics", "Econometrics",
                "Psychology", "Cognitive Psychology", "Social Psychology", "Developmental Psychology",
                "Philosophy", "Ethics", "Logic", "Metaphysics",
                "Art History", "Music Theory", "Political Science", "Sociology", "Environmental Science"
        );
    }

    private void loadUserSubjects() {
        setLoading(true);
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        Object subjectsObject = documentSnapshot.get("subjects");

                        userSelectedSubjects.clear(); // Clear existing selections

                        if (subjectsObject instanceof Map) {
                            // Expected format: Map<String, Boolean>
                            Map<String, Boolean> subjectsMap = (Map<String, Boolean>) subjectsObject;
                            for (Map.Entry<String, Boolean> entry : subjectsMap.entrySet()) {
                                if (entry.getValue() != null && entry.getValue()) { // Add if value is true
                                    userSelectedSubjects.add(entry.getKey());
                                }
                            }
                        } else if (subjectsObject instanceof List) {
                            // Fallback for old data format: List<String>
                            Log.w(TAG, "Subjects field is a List. Converting to Map format for future saves.");
                            List<?> rawSubjectsList = (List<?>) subjectsObject;
                            for (Object item : rawSubjectsList) {
                                if (item instanceof String) {
                                    userSelectedSubjects.add((String) item);
                                }
                            }
                        } else if (subjectsObject != null) {
                            Log.e(TAG, "Subjects field is neither a Map nor a List. Type: " + subjectsObject.getClass().getName());
                            Toast.makeText(this, "Unexpected data format for subjects.", Toast.LENGTH_LONG).show();
                        }
                    }

                    displayCurrentSubjects();
                    populateAvailableSubjectsChips();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Error loading user subjects: " + e.getMessage());
                    Toast.makeText(this, "Failed to load your subjects.", Toast.LENGTH_SHORT).show();
                    displayCurrentSubjects();
                    populateAvailableSubjectsChips();
                });
    }


    private void displayCurrentSubjects() {
        chipGroupCurrentSubjects.removeAllViews();
        if (userSelectedSubjects.isEmpty()) {
            textNoCurrentSubjects.setVisibility(View.VISIBLE);
        } else {
            textNoCurrentSubjects.setVisibility(View.GONE);
            for (String subject : userSelectedSubjects) {
                Chip chip = createRemovableChip(subject);
                chipGroupCurrentSubjects.addView(chip);
            }
        }
    }

    private void populateAvailableSubjectsChips() {
        chipGroupAvailableSubjects.removeAllViews();
        // textLoadingSubjects.setVisibility(View.GONE); // No longer needed here as data is loaded.

        for (String subject : allAvailableSubjects) {
            Chip chip = createSelectableChip(subject);
            chipGroupAvailableSubjects.addView(chip);
        }
    }

    private Chip createRemovableChip(String subjectName) {
        Chip chip = new Chip(this);
        chip.setText(subjectName);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setClickable(false);
        chip.setChipBackgroundColorResource(R.color.colorPrimaryContainer);
        chip.setTextColor(getResources().getColor(R.color.colorOnPrimaryContainer, getTheme()));

        chip.setOnCloseIconClickListener(v -> {
            userSelectedSubjects.remove(subjectName);
            displayCurrentSubjects();
            populateAvailableSubjectsChips(); // Update to reflect removal in available chips
        });
        return chip;
    }

    private Chip createSelectableChip(String subjectName) {
        Chip chip = new Chip(this);
        chip.setText(subjectName);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        chip.setChecked(userSelectedSubjects.contains(subjectName));

        chip.setChipBackgroundColorResource(R.color.chip_background_selector);
        chip.setTextColor(getResources().getColorStateList(R.color.chip_text_selector, getTheme()));


        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                userSelectedSubjects.add(subjectName);
            } else {
                userSelectedSubjects.remove(subjectName);
            }
            displayCurrentSubjects(); // Refresh current subjects immediately
        });
        return chip;
    }

    private void setupClickListeners() {
        btnSaveSubjects.setOnClickListener(v -> saveSelectedSubjects());
    }

    private void saveSelectedSubjects() {
        if (currentUser == null) {
            Toast.makeText(this, "Not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Convert the local Set<String> to a Map<String, Boolean> for Firestore
        Map<String, Boolean> subjectsToSave = new HashMap<>();
        for (String subject : userSelectedSubjects) {
            subjectsToSave.put(subject, true); // Mark selected subjects as true
        }

        db.collection("users").document(currentUser.getUid())
                .update("subjects", subjectsToSave) // Save the Map to Firestore
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(SubjectSelectionActivity.this, "Subjects updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Error updating subjects: " + e.getMessage(), e);
                    Toast.makeText(SubjectSelectionActivity.this, "Failed to save subjects: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveSubjects.setEnabled(!isLoading);
        chipGroupAvailableSubjects.setEnabled(!isLoading);
        chipGroupCurrentSubjects.setEnabled(!isLoading);
    }
}
