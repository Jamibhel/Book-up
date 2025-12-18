package com.example.bookup.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookup.R;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppSettingsActivity extends AppCompatActivity {
    private static final String TAG = "AppSettingsActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private MaterialSwitch switchEnableNotifications;
    private MaterialSwitch switchEnableOfflineMode;
    private MaterialSwitch switchEnableAnalytics;
    private MaterialSwitch switchEnableDataCollection;
    private ProgressBar progressBar;
    private LinearLayout settingsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar_app_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.app_settings_title);
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Authentication required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkAdminStatus();
        initViews();
        loadSettings();
    }

    private void checkAdminStatus() {
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                        if (isAdmin == null || !isAdmin) {
                            Toast.makeText(this, "Access denied: Not an admin", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "User profile not found. Access denied.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check admin status", e);
                    finish();
                });
    }

    private void initViews() {
        progressBar = findViewById(R.id.progress_bar_app_settings);
        settingsContainer = findViewById(R.id.settings_container);

        switchEnableNotifications = findViewById(R.id.switch_enable_notifications);
        switchEnableOfflineMode = findViewById(R.id.switch_enable_offline_mode);
        switchEnableAnalytics = findViewById(R.id.switch_enable_analytics);
        switchEnableDataCollection = findViewById(R.id.switch_enable_data_collection);

        setupSwitchListeners();
    }

    private void setupSwitchListeners() {
        switchEnableNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSetting("enableNotifications", isChecked));

        switchEnableOfflineMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSetting("enableOfflineMode", isChecked));

        switchEnableAnalytics.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSetting("enableAnalytics", isChecked));

        switchEnableDataCollection.setOnCheckedChangeListener((buttonView, isChecked) ->
                saveSetting("enableDataCollection", isChecked));
    }

    private void loadSettings() {
        setLoading(true);

        db.collection("appSettings").document("global").get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);

                    if (documentSnapshot.exists()) {
                        Boolean enableNotifications = documentSnapshot.getBoolean("enableNotifications");
                        Boolean enableOfflineMode = documentSnapshot.getBoolean("enableOfflineMode");
                        Boolean enableAnalytics = documentSnapshot.getBoolean("enableAnalytics");
                        Boolean enableDataCollection = documentSnapshot.getBoolean("enableDataCollection");

                        switchEnableNotifications.setChecked(enableNotifications != null && enableNotifications);
                        switchEnableOfflineMode.setChecked(enableOfflineMode != null && enableOfflineMode);
                        switchEnableAnalytics.setChecked(enableAnalytics != null && enableAnalytics);
                        switchEnableDataCollection.setChecked(enableDataCollection != null && enableDataCollection);
                    } else {
                        // Initialize with default values
                        switchEnableNotifications.setChecked(true);
                        switchEnableOfflineMode.setChecked(false);
                        switchEnableAnalytics.setChecked(true);
                        switchEnableDataCollection.setChecked(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load settings", e);
                    setLoading(false);
                    Toast.makeText(this, "Error loading settings", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveSetting(String key, boolean value) {
        db.collection("appSettings").document("global")
                .update(key, value)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Setting saved: " + key + " = " + value);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save setting: " + key, e);
                    Toast.makeText(this, "Error saving setting", Toast.LENGTH_SHORT).show();
                    // Revert the UI
                    loadSettings();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        settingsContainer.setEnabled(!isLoading);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
