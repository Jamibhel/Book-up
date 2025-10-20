package com.example.bookup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookup.models.HelpRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CreateRequestActivity extends AppCompatActivity {

    private static final String TAG = "CreateRequestActivity";

    // UI Elements
    private TextInputEditText editTextRequestTitle;
    private AutoCompleteTextView autoCompleteRequestSubject;
    private TextInputEditText editTextRequestDescription;
    private MaterialButton btnPostRequest;
    private MaterialButton btnCancelRequestCreation;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_create_request);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.create_request_title);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to post a request.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupSubjectDropdown();
        setupClickListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        editTextRequestTitle = findViewById(R.id.edit_text_request_title);
        autoCompleteRequestSubject = findViewById(R.id.auto_complete_request_subject);
        editTextRequestDescription = findViewById(R.id.edit_text_request_description);
        btnPostRequest = findViewById(R.id.btn_post_request);
        btnCancelRequestCreation = findViewById(R.id.btn_cancel_request_creation);
        progressBar = findViewById(R.id.progress_bar_create_request);
    }

    private void setupSubjectDropdown() {
        // Subjects (ideally fetched dynamically from Firestore or a global config)
        List<String> subjects = Arrays.asList(
                "Mathematics", "Physics", "Chemistry", "Biology", "Computer Science",
                "History", "English", "Economics", "Psychology", "Philosophy",
                "Algebra", "Calculus", "Thermodynamics", "Electromagnetism", "Genetics",
                "Web Development", "Literature", "Microeconomics", "Ethics", "Art History"
        );
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects);
        autoCompleteRequestSubject.setAdapter(subjectAdapter);
    }

    private void setupClickListeners() {
        btnPostRequest.setOnClickListener(v -> postHelpRequest());
        btnCancelRequestCreation.setOnClickListener(v -> finish());
    }

    private void postHelpRequest() {
        String title = editTextRequestTitle.getText().toString().trim();
        String subject = autoCompleteRequestSubject.getText().toString().trim();
        String description = editTextRequestDescription.getText().toString().trim();

        // --- Validation ---
        if (TextUtils.isEmpty(title)) {
            editTextRequestTitle.setError("Request title is required.");
            editTextRequestTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(subject)) {
            autoCompleteRequestSubject.setError("Subject is required.");
            autoCompleteRequestSubject.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            editTextRequestDescription.setError("Description is required.");
            editTextRequestDescription.requestFocus();
            return;
        }

        setLoading(true);

        // Get current user's name
        String requestedByName = currentUser.getDisplayName();
        if (requestedByName == null || requestedByName.isEmpty()) {
            requestedByName = currentUser.getEmail(); // Fallback to email
            if (requestedByName != null && requestedByName.contains("@")) {
                requestedByName = requestedByName.split("@")[0]; // Use part before @
            } else {
                requestedByName = "Anonymous User";
            }
        }

        // Prepare data for Firestore
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", title);
        requestData.put("description", description);
        requestData.put("subject", subject);
        requestData.put("requestedByUid", currentUser.getUid());
        requestData.put("requestedByName", requestedByName);
        requestData.put("status", "Open"); // Initial status
        requestData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("helpRequests").add(requestData)
                .addOnSuccessListener(documentReference -> {
                    setLoading(false);
                    Toast.makeText(CreateRequestActivity.this, "Help request posted successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to RequestsFragment
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Error posting help request: " + e.getMessage(), e);
                    Toast.makeText(CreateRequestActivity.this, "Failed to post request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnPostRequest.setEnabled(!isLoading);
        btnCancelRequestCreation.setEnabled(!isLoading);
        editTextRequestTitle.setEnabled(!isLoading);
        autoCompleteRequestSubject.setEnabled(!isLoading);
        editTextRequestDescription.setEnabled(!isLoading);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
    }
}
