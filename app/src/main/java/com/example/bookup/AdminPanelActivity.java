package com.example.bookup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class AdminPanelActivity extends AppCompatActivity {

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

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_admin_panel);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.admin_panel_title);
        }

        initViews();
        setupClickListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        btnManageNews = findViewById(R.id.btn_manage_news);
        btnManageMaterials = findViewById(R.id.btn_manage_materials);
        btnManageUsers = findViewById(R.id.btn_manage_users);
        btnManageRequests = findViewById(R.id.btn_manage_requests);
        btnViewStats = findViewById(R.id.btn_view_stats);
        btnAppSettings = findViewById(R.id.btn_app_settings);
    }

    private void setupClickListeners() {
        btnManageNews.setOnClickListener(v -> {
            // Launch the activity to manage news items (list, edit, create new)
            startActivity(new Intent(AdminPanelActivity.this, ManageNewsActivity.class));
        });

        btnManageMaterials.setOnClickListener(v -> {
            // Launch the activity for admin to view, edit, delete any material
            startActivity(new Intent(AdminPanelActivity.this, ManageMaterialsActivity.class));
        });

        btnManageUsers.setOnClickListener(v -> {
            // Launch the activity for admin to view, modify roles, block users
            startActivity(new Intent(AdminPanelActivity.this, ManageUsersActivity.class));
        });

        btnManageRequests.setOnClickListener(v -> {
            // Launch the activity for admin to moderate/manage all help requests
            startActivity(new Intent(AdminPanelActivity.this, ManageRequestsActivity.class));
        });

        btnViewStats.setOnClickListener(v -> {
            // Launch the activity to show app usage, user statistics, etc.
            startActivity(new Intent(AdminPanelActivity.this, AnalyticsActivity.class));
        });

        btnAppSettings.setOnClickListener(v -> {
            // Launch the activity for global app configurations
            startActivity(new Intent(AdminPanelActivity.this, AppSettingsActivity.class));
        });
    }
}
