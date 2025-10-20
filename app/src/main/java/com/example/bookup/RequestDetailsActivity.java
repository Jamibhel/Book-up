package com.example.bookup;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.bookup.models.HelpRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class RequestDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST = "extra_request"; // Key for passing HelpRequest object

    private static final String TAG = "RequestDetailsActivity";

    // UI Elements
    private TextView requestDetailTitle;
    private TextView requestDetailSubject;
    private Chip requestDetailStatusChip;
    private TextView requestDetailRequestedBy;
    private TextView requestDetailDate;
    private TextView requestDetailDescription;
    private MaterialButton btnOfferHelp; // Renamed for clarity
    private ProgressBar progressBar;

    // Data
    private HelpRequest currentRequest;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private boolean isCurrentUserTutor = false; // Flag to determine button text

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_request_details);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.request_details_title);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to view request details.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();

        // Get HelpRequest object from Intent
        if (getIntent().hasExtra(EXTRA_REQUEST)) {
            currentRequest = (HelpRequest) getIntent().getSerializableExtra(EXTRA_REQUEST);
            if (currentRequest != null) {
                // Determine if current user is a tutor
                checkUserRoleAndDisplayDetails();
            } else {
                Toast.makeText(this, "Error loading request details.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No request provided.", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupClickListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        requestDetailTitle = findViewById(R.id.request_detail_title);
        requestDetailSubject = findViewById(R.id.request_detail_subject);
        requestDetailStatusChip = findViewById(R.id.request_detail_status_chip);
        requestDetailRequestedBy = findViewById(R.id.request_detail_requested_by);
        requestDetailDate = findViewById(R.id.request_detail_date);
        requestDetailDescription = findViewById(R.id.request_detail_description);
        btnOfferHelp = findViewById(R.id.btn_offer_help);
        progressBar = findViewById(R.id.progress_bar_request_details);
    }

    private void checkUserRoleAndDisplayDetails() {
        if (currentUser == null || currentRequest == null) return;

        setLoading(true);

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        Boolean isTutor = documentSnapshot.getBoolean("isTutor");
                        if (isTutor != null && isTutor) {
                            isCurrentUserTutor = true;
                            btnOfferHelp.setText(R.string.offer_help_button_text);
                            btnOfferHelp.setVisibility(View.VISIBLE);
                        } else {
                            // Student viewing their own request, or another student's request
                            // Might change this later to "View Offers" if it's their own request
                            btnOfferHelp.setText(R.string.view_offers_button_text); // For students to see tutor offers
                            btnOfferHelp.setVisibility(View.GONE); // For now, hide it for students, unless it's their own
                            if (currentRequest.getRequestedByUid().equals(currentUser.getUid())) {
                                btnOfferHelp.setText(R.string.view_offers_button_text);
                                btnOfferHelp.setVisibility(View.VISIBLE); // Show if student viewing their own
                            }
                        }
                        displayRequestDetails();
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                        btnOfferHelp.setVisibility(View.GONE);
                        displayRequestDetails(); // Still display what we have
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Failed to get user role: " + e.getMessage(), e);
                    Toast.makeText(this, "Error checking user role.", Toast.LENGTH_SHORT).show();
                    btnOfferHelp.setVisibility(View.GONE);
                    displayRequestDetails(); // Still display what we have
                });
    }


    private void displayRequestDetails() {
        if (currentRequest == null) return;

        requestDetailTitle.setText(currentRequest.getTitle());
        requestDetailSubject.setText(String.format(Locale.getDefault(), "Subject: %s", currentRequest.getSubject()));
        requestDetailDescription.setText(currentRequest.getDescription());
        requestDetailRequestedBy.setText(String.format(Locale.getDefault(), "Requested by %s", currentRequest.getRequestedByName()));

        // Set status chip text and color
        String status = currentRequest.getStatus();
        requestDetailStatusChip.setText(status);
        if ("Open".equalsIgnoreCase(status)) {
            requestDetailStatusChip.setChipBackgroundColorResource(R.color.colorPrimaryContainer);
            requestDetailStatusChip.setTextColor(ContextCompat.getColor(this, R.color.colorOnPrimaryContainer));
        } else if ("Resolved".equalsIgnoreCase(status)) {
            // Assuming md_theme_light_tertiaryContainer maps to a green/success color
            requestDetailStatusChip.setChipBackgroundColorResource(R.color.md_theme_light_tertiaryContainer);
            requestDetailStatusChip.setTextColor(ContextCompat.getColor(this, R.color.md_theme_light_onTertiaryContainer));
        } else { // Other statuses or default
            requestDetailStatusChip.setChipBackgroundColorResource(R.color.colorSurfaceVariant);
            requestDetailStatusChip.setTextColor(ContextCompat.getColor(this, R.color.colorOnSurfaceVariant));
        }


        if (currentRequest.getTimestamp() != null) {
            String date = DateFormat.format("MMM dd, yyyy", currentRequest.getTimestamp()).toString();
            requestDetailDate.setText(date);
        } else {
            requestDetailDate.setText("N/A");
        }
    }

    private void setupClickListeners() {
        btnOfferHelp.setOnClickListener(v -> {
            if (currentRequest == null) return;

            if (isCurrentUserTutor) {
                // Logic for tutor offering help
                Toast.makeText(this, "Tutor " + currentUser.getDisplayName() + " offers help for: " + currentRequest.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Implement logic to record tutor's offer (e.g., in a subcollection under the request)
                // TODO: Navigate to chat with the requestedByUid
            } else {
                // Logic for student viewing offers (if it's their own request)
                Toast.makeText(this, "Student " + currentUser.getDisplayName() + " views offers for: " + currentRequest.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Implement navigation to a screen listing tutors who offered help for this request
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // Disable content interaction while loading
        requestDetailTitle.setEnabled(!isLoading);
        requestDetailSubject.setEnabled(!isLoading);
        requestDetailDescription.setEnabled(!isLoading);
        btnOfferHelp.setEnabled(!isLoading);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
    }
}
