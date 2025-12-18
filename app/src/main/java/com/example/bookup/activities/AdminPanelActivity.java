package com.example.bookup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.bookup.R;
import com.example.bookup.base.BaseSecureActivity;
import com.example.bookup.utils.AdminSecurityManager;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Administrative panel activity that provides access to various management features.
 * This activity implements comprehensive security checks and audit logging.
 */
public class AdminPanelActivity extends BaseSecureActivity {
    private static final String TAG = "AdminPanelActivity";

    // Firebase and Security
    private FirebaseFirestore db;
    private AdminSecurityManager adminSecurityManager;

    // UI Components
    private CircularProgressIndicator progressIndicator;
    private ConstraintLayout rootLayout;

    // UI Elements
    private MaterialButton btnManageNews;
    private MaterialButton btnManageMaterials;
    private MaterialButton btnManageUsers;
    private MaterialButton btnManageRequests;
    private MaterialButton btnViewStats;
    private MaterialButton btnAppSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        // Initialize Security
        db = FirebaseFirestore.getInstance();
        adminSecurityManager = AdminSecurityManager.getInstance();
        
        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_admin_panel);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.admin_panel_title);
        }

        initViews();
        verifyAdminAccess();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        verifyAdminAccess();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        progressIndicator = findViewById(R.id.progress_indicator);
//        rootLayout = findViewById(R.id.root_layout);
        btnManageNews = findViewById(R.id.btn_manage_news);
        btnManageMaterials = findViewById(R.id.btn_manage_materials);
        btnManageUsers = findViewById(R.id.btn_manage_users);
        btnManageRequests = findViewById(R.id.btn_manage_requests);
        btnViewStats = findViewById(R.id.btn_view_stats);
        btnAppSettings = findViewById(R.id.btn_app_settings);
    }

    private void verifyAdminAccess() {
        setLoading(true);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        adminSecurityManager.verifyAdminStatus(currentUser.getUid())
            .addOnSuccessListener(isAdmin -> {
                if (!isAdmin) {
                    Toast.makeText(this, "Access denied: Not an admin", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                setupClickListeners();
                setLoading(false);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error verifying admin status", e);
                handleFirebaseError(e);
                finish();
            });
    }

    private void setupClickListeners() {
        btnManageNews.setOnClickListener(v -> launchSecureActivity(ManageNewsActivity.class, "news_management"));
        btnManageMaterials.setOnClickListener(v -> launchSecureActivity(ManageMaterialsActivity.class, "materials_management"));
        btnManageUsers.setOnClickListener(v -> launchSecureActivity(ManageUsersActivity.class, "user_management"));
        btnManageRequests.setOnClickListener(v -> launchSecureActivity(ManageRequestsActivity.class, "request_management"));
        btnViewStats.setOnClickListener(v -> launchSecureActivity(AnalyticsActivity.class, "analytics_view"));
        btnAppSettings.setOnClickListener(v -> launchSecureActivity(AppSettingsActivity.class, "settings_management"));
    }

    private void launchSecureActivity(Class<?> activityClass, String operationType) {
        if (adminSecurityManager.checkRateLimit(this, operationType)) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                adminSecurityManager.logAdminAction(
                    currentUser.getUid(),
                    "access_" + operationType,
                    "Accessed " + activityClass.getSimpleName()
                );
                startActivity(new Intent(AdminPanelActivity.this, activityClass));
            }
        }
    }

    @Override
    protected void setLoading(boolean isLoading) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (rootLayout != null) {
            rootLayout.setAlpha(isLoading ? 0.5f : 1.0f);
            rootLayout.setEnabled(!isLoading);
        }
    }
}
