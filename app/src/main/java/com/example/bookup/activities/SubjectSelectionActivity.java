package com.example.bookup.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import com.example.bookup.R;
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
import java.util.LinkedHashMap;
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
    private Map<String, List<String>> categorizedSubjects;
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
        categorizedSubjects = new LinkedHashMap<>();
        
        categorizedSubjects.put("Technology & Coding", Arrays.asList(
            "Web Development", "Python Programming", "Mobile App Development", "Algorithms & Data Structures", 
            "Cybersecurity", "Cloud Computing", "Database Management", "Game Development", "Artificial Intelligence",
            "Blockchain & Web3"
        ));
        
        categorizedSubjects.put("Design & Creative", Arrays.asList(
            "UI/UX Design", "Graphic Design", "3D Modeling & Rendering", "Digital Photography", 
            "Video Editing & VFX", "Motion Graphics & Animation", "Fashion & Apparel Design", "Interior Architecture"
        ));
        
        categorizedSubjects.put("Business & Strategy", Arrays.asList(
            "Entrepreneurship & Startups", "Digital Marketing & Growth", "SEO & Content Strategy", 
            "Public Speaking & Pitching", "Financial Literacy & Investing", "Agile Project Management", "Business Strategy & Consulting"
        ));
        
        categorizedSubjects.put("Languages & Culture", Arrays.asList(
            "Spanish Language", "French Language", "Mandarin Chinese", "German Language", "Japanese Language", 
            "English Literature", "Creative Writing"
        ));
        
        categorizedSubjects.put("Advanced Academics & Sciences", Arrays.asList(
            "Calculus & Real Analysis", "Statistics & Data Science", "Physics & Quantum Mechanics", 
            "Organic Chemistry", "Biochemistry", "Genetics & Molecular Biology", "Anatomy & Physiology", "Environmental Science & Policy"
        ));
        
        categorizedSubjects.put("Music, Arts & Lifestyle", Arrays.asList(
            "Piano Performance", "Guitar & Strings", "Vocal Training & Singing", "Music Theory & Production", 
            "Culinary Arts & Gastronomy", "Nutrition & Personal Fitness"
        ));
        
        categorizedSubjects.put("Test Prep & Coaching", Arrays.asList(
            "SAT/ACT Standardized Prep", "IELTS Exam Training", "TOEFL Exam Prep", "Resume Writing & Interview Coaching"
        ));

        // Flatten all subjects for general searches
        allAvailableSubjects = new ArrayList<>();
        for (List<String> list : categorizedSubjects.values()) {
            allAvailableSubjects.addAll(list);
        }
    }

    private void loadUserSubjects() {
        setLoading(true);
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        userSelectedSubjects.clear(); // Clear existing selections
                        
                        Object subjectsObject = documentSnapshot.get("subjects");
                        Object tutoringSubjectsObject = documentSnapshot.get("tutoringSubjects");

                        if (subjectsObject instanceof Map) {
                            // Expected format: Map<String, Boolean>
                            Map<String, Boolean> subjectsMap = (Map<String, Boolean>) subjectsObject;
                            for (Map.Entry<String, Boolean> entry : subjectsMap.entrySet()) {
                                if (entry.getValue() != null && entry.getValue()) { // Add if value is true
                                    userSelectedSubjects.add(entry.getKey());
                                }
                            }
                        } else if (tutoringSubjectsObject instanceof List) {
                            // Fall back to tutoringSubjects List
                            List<?> rawList = (List<?>) tutoringSubjectsObject;
                            for (Object item : rawList) {
                                if (item instanceof String) {
                                    userSelectedSubjects.add((String) item);
                                }
                            }
                        } else if (subjectsObject instanceof List) {
                            // Fallback for old data format: List<String>
                            List<?> rawSubjectsList = (List<?>) subjectsObject;
                            for (Object item : rawSubjectsList) {
                                if (item instanceof String) {
                                    userSelectedSubjects.add((String) item);
                                }
                            }
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
        LinearLayout container = (LinearLayout) chipGroupAvailableSubjects.getParent();
        
        // Find chipGroupAvailableSubjects index inside the container
        int chipGroupIndex = container.indexOfChild(chipGroupAvailableSubjects);
        
        // Remove all views starting from the chipGroupAvailableSubjects
        int childCount = container.getChildCount();
        if (childCount > chipGroupIndex) {
            container.removeViews(chipGroupIndex, childCount - chipGroupIndex);
        }
        
        // Populate categorized chips dynamically
        for (Map.Entry<String, List<String>> entry : categorizedSubjects.entrySet()) {
            String category = entry.getKey();
            List<String> subjects = entry.getValue();
            
            // Create a luxurious Subheader TextView
            TextView subheader = new TextView(this);
            subheader.setText(category);
            subheader.setTextSize(14);
            subheader.setPadding(4, 32, 4, 12);
            subheader.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
            subheader.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL));
            container.addView(subheader);
            
            // Create a new Flow-based ChipGroup for this category
            ChipGroup categoryGroup = new ChipGroup(this);
            categoryGroup.setChipSpacingHorizontal(12);
            categoryGroup.setChipSpacingVertical(12);
            
            for (String subject : subjects) {
                Chip chip = createSelectableChip(subject);
                categoryGroup.addView(chip);
            }
            container.addView(categoryGroup);
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

        // Convert the local Set<String> to a Map<String, Boolean> for Firestore compatibility
        Map<String, Boolean> subjectsToSave = new HashMap<>();
        List<String> tutoringSubjectsList = new ArrayList<>();
        
        for (String subject : userSelectedSubjects) {
            subjectsToSave.put(subject, true); // Mark selected subjects as true
            tutoringSubjectsList.add(subject); // Add to the standard List representation
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("subjects", subjectsToSave);
        updates.put("tutoringSubjects", tutoringSubjectsList);

        db.collection("users").document(currentUser.getUid())
                .update(updates) // Save both fields concurrently to Firestore
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
