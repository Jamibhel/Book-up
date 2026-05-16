package com.example.bookup.activities;

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
import com.example.bookup.R;
import com.example.bookup.models.Purchase;
import com.example.bookup.models.StudyMaterial;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class MaterialDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_MATERIAL = "extra_material";

    private ShapeableImageView materialDetailThumbnail;
    private TextView materialDetailTitle, materialDetailSubjectType, materialDetailRatingDownloads;
    private TextView materialDetailDescription, materialDetailUploaderName, materialDetailUploadDate;
    private Button btnViewMaterial, btnShareMaterial, btnBuyMaterial;
    private ProgressBar progressBarLoadingMaterial;

    private StudyMaterial currentMaterial;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isPurchased = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_details);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupToolbar();

        if (getIntent().hasExtra(EXTRA_MATERIAL)) {
            currentMaterial = (StudyMaterial) getIntent().getSerializableExtra(EXTRA_MATERIAL);
            if (currentMaterial != null) {
                checkOwnership();
            } else {
                Toast.makeText(this, "Error loading material", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        setupClickListeners();
    }

    private void initializeViews() {
        materialDetailThumbnail = findViewById(R.id.material_detail_thumbnail);
        materialDetailTitle = findViewById(R.id.material_detail_title);
        materialDetailSubjectType = findViewById(R.id.material_detail_subject_type);
        materialDetailRatingDownloads = findViewById(R.id.material_detail_rating_downloads);
        materialDetailDescription = findViewById(R.id.material_detail_description);
        materialDetailUploaderName = findViewById(R.id.material_detail_uploader_name);
        materialDetailUploadDate = findViewById(R.id.material_detail_upload_date);
        btnViewMaterial = findViewById(R.id.btn_view_material);
        btnShareMaterial = findViewById(R.id.btn_share_material);
        btnBuyMaterial = findViewById(R.id.btn_buy_material); // Ensure you add this ID to XML
        progressBarLoadingMaterial = findViewById(R.id.progress_bar_loading_material);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void checkOwnership() {
        if (mAuth.getUid() == null || currentMaterial == null) return;
        
        // Always owned if uploader
        if (mAuth.getUid().equals(currentMaterial.getUploaderUid())) {
            isPurchased = true;
            displayMaterialDetails();
            return;
        }

        if (!currentMaterial.isPremium()) {
            isPurchased = true;
            displayMaterialDetails();
            return;
        }

        // Check purchases collection
        db.collection("purchases")
                .whereEqualTo("userId", mAuth.getUid())
                .whereEqualTo("materialId", currentMaterial.getId())
                .get()
                .addOnSuccessListener(snapshots -> {
                    isPurchased = !snapshots.isEmpty();
                    displayMaterialDetails();
                });
    }

    private void displayMaterialDetails() {
        materialDetailTitle.setText(currentMaterial.getTitle());
        materialDetailSubjectType.setText(String.format(Locale.getDefault(), "%s • %s", currentMaterial.getSubject(), currentMaterial.getMaterialType()));
        materialDetailRatingDownloads.setText(String.format(Locale.getDefault(), "%.1f • %d downloads", currentMaterial.getAverageRating(), currentMaterial.getDownloadCount()));
        materialDetailDescription.setText(currentMaterial.getDescription());
        materialDetailUploaderName.setText(currentMaterial.getUploaderName());

        if (currentMaterial.getTimestamp() != null) {
            String date = DateFormat.format("MMM dd, yyyy", currentMaterial.getTimestamp()).toString();
            materialDetailUploadDate.setText("Uploaded on: " + date);
        }

        Glide.with(this).load(currentMaterial.getThumbnailUrl())
                .placeholder(R.drawable.ic_document_placeholder).into(materialDetailThumbnail);

        // Visibility Logic
        if (isPurchased) {
            btnViewMaterial.setVisibility(View.VISIBLE);
            btnBuyMaterial.setVisibility(View.GONE);
            btnViewMaterial.setText(currentMaterial.isPremium() ? "View Purchased Material" : "View Material");
        } else {
            btnViewMaterial.setVisibility(View.GONE);
            btnBuyMaterial.setVisibility(View.VISIBLE);
            btnBuyMaterial.setText(String.format(Locale.getDefault(), "Buy for ₦%.0f", currentMaterial.getPrice()));
        }
    }

    private void setupClickListeners() {
        btnViewMaterial.setOnClickListener(v -> viewMaterialFile(currentMaterial.getFileUrl()));
        btnShareMaterial.setOnClickListener(v -> shareMaterial(currentMaterial.getTitle(), currentMaterial.getFileUrl()));
        
        btnBuyMaterial.setOnClickListener(v -> {
            // SKELETON: In a real app, integrate Stripe/PayPal/Google Pay here
            Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();
            
            Purchase p = new Purchase(mAuth.getUid(), currentMaterial.getId(), currentMaterial.getTitle(), currentMaterial.getPrice());
            db.collection("purchases").add(p).addOnSuccessListener(ref -> {
                Toast.makeText(this, "Purchase Successful!", Toast.LENGTH_LONG).show();
                isPurchased = true;
                displayMaterialDetails();
            });
        });
    }

    private void viewMaterialFile(String fileUrl) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl)));
        } catch (Exception e) {
            Toast.makeText(this, "Could not open file", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareMaterial(String title, String fileUrl) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Check out " + title + " on BookUp: " + fileUrl);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
