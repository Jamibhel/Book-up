package com.example.bookup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";

    // UI Elements
    private TextInputEditText editTextCurrentPassword;
    private TextInputEditText editTextNewPassword;
    private TextInputEditText editTextConfirmNewPassword;
    private MaterialButton btnSavePassword;
    private MaterialButton btnCancelChangePassword;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_change_password);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.change_password_title);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to change your password.", Toast.LENGTH_LONG).show();
            // Redirect to sign-in or finish activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        // IMPORTANT: Update password requires reauthentication if user hasn't signed in recently.
        // If the user's last sign-in was too long ago, reauthentication will fail.
        // We handle this by asking for their current password and using it for reauthentication.
        if (currentUser.isAnonymous() || currentUser.getProviderData().stream().noneMatch(info -> info.getProviderId().equals("password"))) {
            // User is anonymous or signed in via a federated provider (Google, etc.) without a password.
            // For federated users, they typically change password via their provider.
            Toast.makeText(this, "Password change not applicable for this sign-in method.", Toast.LENGTH_LONG).show();
            finish();
            return;
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
        editTextCurrentPassword = findViewById(R.id.edit_text_current_password);
        editTextNewPassword = findViewById(R.id.edit_text_new_password);
        editTextConfirmNewPassword = findViewById(R.id.edit_text_confirm_new_password);
        btnSavePassword = findViewById(R.id.btn_save_password);
        btnCancelChangePassword = findViewById(R.id.btn_cancel_change_password);
        progressBar = findViewById(R.id.progress_bar_change_password);
    }

    private void setupClickListeners() {
        btnSavePassword.setOnClickListener(v -> changePassword());
        btnCancelChangePassword.setOnClickListener(v -> finish());
    }

    private void changePassword() {
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmNewPassword = editTextConfirmNewPassword.getText().toString().trim();

        // --- Validation ---
        if (TextUtils.isEmpty(currentPassword)) {
            editTextCurrentPassword.setError("Current password is required.");
            editTextCurrentPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            editTextNewPassword.setError("New password is required.");
            editTextNewPassword.requestFocus();
            return;
        }
        if (newPassword.length() < 6) {
            editTextNewPassword.setError("New password must be at least 6 characters long.");
            editTextNewPassword.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            editTextConfirmNewPassword.setError("New passwords do not match.");
            editTextConfirmNewPassword.requestFocus();
            return;
        }
        if (newPassword.equals(currentPassword)) {
            editTextNewPassword.setError("New password cannot be the same as current password.");
            editTextNewPassword.requestFocus();
            return;
        }

        setLoading(true);

        // Reauthenticate user with their current password
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User re-authenticated.");
                        // Reauthentication successful, now update the password
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    setLoading(false);
                                    if (updateTask.isSuccessful()) {
                                        Log.d(TAG, "User password updated.");
                                        Toast.makeText(ChangePasswordActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                        finish(); // Go back to profile
                                    } else {
                                        Log.e(TAG, "Error updating password: " + updateTask.getException().getMessage(), updateTask.getException());
                                        Toast.makeText(ChangePasswordActivity.this, "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        setLoading(false);
                        Log.e(TAG, "Reauthentication failed: " + task.getException().getMessage(), task.getException());
                        Toast.makeText(ChangePasswordActivity.this, "Authentication failed. Please check your current password.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSavePassword.setEnabled(!isLoading);
        btnCancelChangePassword.setEnabled(!isLoading);
        editTextCurrentPassword.setEnabled(!isLoading);
        editTextNewPassword.setEnabled(!isLoading);
        editTextConfirmNewPassword.setEnabled(!isLoading);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
    }
}
