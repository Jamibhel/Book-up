package com.example.bookup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {

    private static final String TAG = "ProfileSetupActivity";

    // UI elements
    private TextInputEditText editTextFirstName, editTextLastName, editTextPhoneNumber;
    private AutoCompleteTextView autoCompleteGender;
    private ChipGroup chipGroupRole;
    private Chip chipStudent, chipTutor;
    private Button btnContinue, btnSkip;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_setup);

        // Initialize UI components
        editTextFirstName = findViewById(R.id.edit_text_first_name);
        editTextLastName = findViewById(R.id.edit_text_last_name);
        editTextPhoneNumber = findViewById(R.id.edit_text_phone_number);
        autoCompleteGender = findViewById(R.id.auto_complete_gender);
        chipGroupRole = findViewById(R.id.chip_group_role);
        chipStudent = findViewById(R.id.chip_student);
        chipTutor = findViewById(R.id.chip_tutor);
        btnContinue = findViewById(R.id.btn_continue);
        btnSkip = findViewById(R.id.btn_skip);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupGenderDropdown();
        setupClickListeners();
        loadExistingProfileData(); // Attempt to load existing data if any
    }

    private void setupGenderDropdown() {
        String[] genders = {"Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        autoCompleteGender.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnContinue.setOnClickListener(v -> saveProfileData());
        btnSkip.setOnClickListener(v -> navigateToHomePage());
    }

    private void loadExistingProfileData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No current user for profile setup.");
            Toast.makeText(this, "Please sign in first.", Toast.LENGTH_SHORT).show();
            navigateToSignIn();
            return;
        }

        setLoading(true);
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        editTextFirstName.setText(documentSnapshot.getString("firstName"));
                        editTextLastName.setText(documentSnapshot.getString("lastName"));
                        editTextPhoneNumber.setText(documentSnapshot.getString("phoneNumber"));
                        autoCompleteGender.setText(documentSnapshot.getString("gender"), false);
                        Boolean isTutor = documentSnapshot.getBoolean("isTutor");
                        if (isTutor != null) {
                            if (isTutor) {
                                chipTutor.setChecked(true);
                            } else {
                                chipStudent.setChecked(true);
                            }
                        }
                    } else {
                        Log.d(TAG, "No existing profile data for user: " + currentUser.getUid());
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Error loading existing profile data: " + e.getMessage(), e);
                    Toast.makeText(this, "Error loading existing profile data.", Toast.LENGTH_SHORT).show();
                });
    }


    private void saveProfileData() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String gender = autoCompleteGender.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            navigateToSignIn();
            return;
        }

        boolean isTutor = chipTutor.isChecked();

        setLoading(true);

        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("phoneNumber", phoneNumber);
        user.put("gender", gender);
        user.put("isTutor", isTutor);
        // Also add email and UID from FirebaseUser for completeness
        user.put("email", currentUser.getEmail());
        user.put("uid", currentUser.getUid());
        // For profilePicUrl and shareLocation, we'll set defaults if not already present
        user.put("profilePicUrl", ""); // Default empty URL for now
        user.put("shareLocation", false); // Default to false

        db.collection("users").document(currentUser.getUid())
                .set(user, SetOptions.merge()) // Use merge to avoid overwriting other fields if they exist
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(ProfileSetupActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    navigateToHomePage();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(ProfileSetupActivity.this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToHomePage() {
        Intent intent = new Intent(ProfileSetupActivity.this, HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(ProfileSetupActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnContinue.setEnabled(!isLoading);
        btnSkip.setEnabled(!isLoading);
        editTextFirstName.setEnabled(!isLoading);
        editTextLastName.setEnabled(!isLoading);
        editTextPhoneNumber.setEnabled(!isLoading);
        autoCompleteGender.setEnabled(!isLoading);
        chipStudent.setEnabled(!isLoading);
        chipTutor.setEnabled(!isLoading);
    }
}
