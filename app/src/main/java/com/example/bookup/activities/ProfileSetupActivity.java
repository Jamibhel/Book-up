package com.example.bookup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookup.R;
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

    private TextInputEditText editTextFirstName, editTextLastName, editTextPhoneNumber;
    private AutoCompleteTextView autoCompleteGender;
    private ChipGroup chipGroupRole;
    private Chip chipStudent, chipTutor;
    private Button btnContinue;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_setup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupGenderDropdown();
        
        btnContinue.setOnClickListener(v -> saveProfileData());
    }

    private void initViews() {
        editTextFirstName = findViewById(R.id.edit_text_first_name);
        editTextLastName = findViewById(R.id.edit_text_last_name);
        editTextPhoneNumber = findViewById(R.id.edit_text_phone_number);
        autoCompleteGender = findViewById(R.id.auto_complete_gender);
        chipGroupRole = findViewById(R.id.chip_group_role);
        chipStudent = findViewById(R.id.chip_student);
        chipTutor = findViewById(R.id.chip_tutor);
        btnContinue = findViewById(R.id.btn_continue);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupGenderDropdown() {
        String[] genders = {"Male", "Female", "Other"};
        autoCompleteGender.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders));
    }

    private void saveProfileData() {
        String fName = editTextFirstName.getText().toString().trim();
        String lName = editTextLastName.getText().toString().trim();
        String phone = editTextPhoneNumber.getText().toString().trim();
        String gender = autoCompleteGender.getText().toString().trim();

        if (fName.isEmpty() || lName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser cu = mAuth.getCurrentUser();
        if (cu == null) return;

        setLoading(true);
        boolean isTutor = chipTutor.isChecked();

        Map<String, Object> user = new HashMap<>();
        user.put("id", cu.getUid());
        user.put("firstName", fName);
        user.put("lastName", lName);
        user.put("displayName", fName + " " + lName);
        user.put("email", cu.getEmail());
        user.put("phoneNumber", phone);
        user.put("gender", gender);
        user.put("role", isTutor ? "tutor" : "student");
        user.put("isTutor", isTutor); // Keeping for internal logic
        user.put("isOnline", true);
        user.put("lastSeen", com.google.firebase.Timestamp.now());
        user.put("profileCompleted", true);
        user.put("rating", 0.0);
        user.put("reviewCount", 0);
        user.put("tutoringSubjects", new java.util.ArrayList<String>());

        // Save device token for push notifications
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    user.put("fcmToken", token);
                    user.put("deviceTokens", java.util.Arrays.asList(token));
                }
                
                db.collection("users").document(cu.getUid())
                        .set(user, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Welcome to BookUp!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, HomePageActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            setLoading(false);
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnContinue.setEnabled(!loading);
    }
}
