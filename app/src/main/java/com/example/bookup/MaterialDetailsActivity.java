package com.example.bookup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookup.models.StudyMaterial;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth; // Needed for current user checks

import java.util.Locale;

public class MaterialDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_MATERIAL = "extra_material"; // Key for passing StudyMaterial object

    // UI Elements
    private ShapeableImageView materialDetailThumbnail;
    private TextView materialDetailTitle;
    private TextView materialDetailSubjectType;
    private TextView materialDetailRatingDownloads;
    private TextView materialDetailDescription;
    private TextView materialDetailUploaderName;
    private TextView materialDetailUploadDate;
    private Button btnViewMaterial;
    private Button btnShareMaterial;
    private ProgressBar progressBarLoadingMaterial;

    // Data
    private StudyMaterial currentMaterial;
    private FirebaseAuth mAuth; // To check if current user is the uploader

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_details);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize UI components
        materialDetailThumbnail = findViewById(R.id.material_detail_thumbnail);
        materialDetailTitle = findViewById(R.id.material_detail_title);
        materialDetailSubjectType = findViewById(R.id.material_detail_subject_type);
        materialDetailRatingDownloads = findViewById(R.id.material_detail_rating_downloads);
        materialDetailDescription = findViewById(R.id.material_detail_description);
        materialDetailUploaderName = findViewById(R.id.material_detail_uploader_name);
        materialDetailUploadDate = findViewById(R.id.material_detail_upload_date);
        btnViewMaterial = findViewById(R.id.btn_view_material);
        btnShareMaterial = findViewById(R.id.btn_share_material);
        progressBarLoadingMaterial = findViewById(R.id.progress_bar_loading_material);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        // Get StudyMaterial object from Intent
        if (getIntent().hasExtra(EXTRA_MATERIAL)) {
            currentMaterial = (StudyMaterial) getIntent().getSerializableExtra(EXTRA_MATERIAL);
            if (currentMaterial != null) {
                displayMaterialDetails();
            } else {
                Toast.makeText(this, "Error loading material details.", Toast.LENGTH_SHORT).show();
                finish(); // Go back if no material found
            }
        } else {
            Toast.makeText(this, "No material provided.", Toast.LENGTH_SHORT).show();
            finish(); // Go back if no material provided
        }

        setupClickListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void displayMaterialDetails() {
        if (currentMaterial == null) return;

        materialDetailTitle.setText(currentMaterial.getTitle());
        materialDetailSubjectType.setText(String.format(Locale.getDefault(), "%s • %s", currentMaterial.getSubject(), currentMaterial.getMaterialType()));
        materialDetailRatingDownloads.setText(String.format(Locale.getDefault(), "%.1f • %d downloads", currentMaterial.getAverageRating(), currentMaterial.getDownloadCount()));
        materialDetailDescription.setText(currentMaterial.getDescription());
        materialDetailUploaderName.setText(currentMaterial.getUploaderName());

        if (currentMaterial.getTimestamp() != null) {
            String date = DateFormat.format("MMM dd, yyyy", currentMaterial.getTimestamp()).toString();
            materialDetailUploadDate.setText(String.format("Uploaded on: %s", date));
        } else {
            materialDetailUploadDate.setText("Upload date: N/A");
        }

        // Load thumbnail using Glide
        if (currentMaterial.getThumbnailUrl() != null && !currentMaterial.getThumbnailUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentMaterial.getThumbnailUrl())
                    .placeholder(R.drawable.ic_document_placeholder)
                    .error(R.drawable.ic_document_placeholder)
                    .into(materialDetailThumbnail);
        } else {
            materialDetailThumbnail.setImageResource(R.drawable.ic_document_placeholder);
        }

        // Check if fileUrl is available for "View Material"
        if (currentMaterial.getFileUrl() == null || currentMaterial.getFileUrl().isEmpty()) {
            btnViewMaterial.setEnabled(false);
            btnViewMaterial.setText("Material Not Available");
        }
    }

    private void setupClickListeners() {
        btnViewMaterial.setOnClickListener(v -> {
            if (currentMaterial != null && currentMaterial.getFileUrl() != null && !currentMaterial.getFileUrl().isEmpty()) {
                viewMaterialFile(currentMaterial.getFileUrl());
            } else {
                Toast.makeText(this, "Material file is not available.", Toast.LENGTH_SHORT).show();
            }
        });

        btnShareMaterial.setOnClickListener(v -> {
            if (currentMaterial != null) {
                shareMaterial(currentMaterial.getTitle(), currentMaterial.getFileUrl());
            }
        });
    }

    private void viewMaterialFile(String fileUrl) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
            // Try to specify package for better PDF viewing experience (e.g., Google Chrome, Adobe Acrobat)
            // This is optional and might vary by device/app availability
            // browserIntent.setPackage("com.android.chrome");
            startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found to view this type of file. Please install a viewer.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not open file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void shareMaterial(String title, String fileUrl) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain"); // Can be changed to a more specific type if the file is embedded
        String shareMessage = String.format("Check out this study material: %s\nDownload here: %s\n(Shared via BookUp App)", title, fileUrl);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share Material Via"));
    }
}
