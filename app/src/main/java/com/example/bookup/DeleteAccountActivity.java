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
import com.google.firebase.auth.UserInfo;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeleteAccountActivity extends AppCompatActivity {

    private static final String TAG = "DeleteAccountActivity";

    // UI Elements
    private TextInputEditText editTextDeletePassword;
    private MaterialButton btnConfirmDeleteAccount;
    private MaterialButton btnCancelDeleteAccount;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_delete_account);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.delete_account_title);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to delete your account.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        // --- CRITICAL FIX: Replaced stream().noneMatch() with a compatible loop ---
        if (currentUser.isAnonymous()) {
            Toast.makeText(this, "Anonymous accounts cannot be deleted this way.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        boolean hasPasswordProvider = false;
        // Iterate through the list of providers linked to the user
        for (UserInfo profile : currentUser.getProviderData()) {
            if (EmailAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                hasPasswordProvider = true;
                break; // Found an email/password provider, no need to check further
            }
        }

        if (!hasPasswordProvider) {
            Toast.makeText(this, "Account deletion requires reauthentication. This method is for email/password users. For other providers (Google, etc.), please delete via that provider's settings or link an email/password first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // --- END CRITICAL FIX ---

        initViews();
        setupClickListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        editTextDeletePassword = findViewById(R.id.edit_text_delete_password);
        btnConfirmDeleteAccount = findViewById(R.id.btn_confirm_delete_account);
        btnCancelDeleteAccount = findViewById(R.id.btn_cancel_delete_account);
        progressBar = findViewById(R.id.progress_bar_delete_account);
    }

    private void setupClickListeners() {
        btnConfirmDeleteAccount.setOnClickListener(v -> confirmAndDeleteAccount());
        btnCancelDeleteAccount.setOnClickListener(v -> finish());
    }

    private void confirmAndDeleteAccount() {
        String password = editTextDeletePassword.getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            editTextDeletePassword.setError("Password is required to confirm deletion.");
            editTextDeletePassword.requestFocus();
            return;
        }

        setLoading(true);

        // Reauthenticate the user
        // We already checked that currentUser has an email/password provider in onCreate
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        Log.d(TAG, "User re-authenticated successfully.");
                        // Reauthentication successful, now proceed with deletion

                        // First, delete Firestore user document
                        db.collection("users").document(currentUser.getUid()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User document deleted from Firestore.");
                                    // Then, delete the Firebase Auth user
                                    currentUser.delete()
                                            .addOnCompleteListener(deleteAuthTask -> {
                                                setLoading(false);
                                                if (deleteAuthTask.isSuccessful()) {
                                                    Log.d(TAG, "User account deleted from Firebase Auth.");
                                                    Toast.makeText(DeleteAccountActivity.this, "Your account has been deleted.", Toast.LENGTH_LONG).show();
                                                    // Navigate to sign-in screen
                                                    Intent intent = new Intent(DeleteAccountActivity.this, SignInActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Log.e(TAG, "Failed to delete Firebase Auth user: " + deleteAuthTask.getException().getMessage(), deleteAuthTask.getException());
                                                    Toast.makeText(DeleteAccountActivity.this, "Failed to delete account: " + deleteAuthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    setLoading(false);
                                    Log.e(TAG, "Failed to delete Firestore document: " + e.getMessage(), e);
                                    Toast.makeText(DeleteAccountActivity.this, "Failed to delete account data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        setLoading(false);
                        Log.e(TAG, "Reauthentication failed for deletion: " + reauthTask.getException().getMessage(), reauthTask.getException());
                        Toast.makeText(DeleteAccountActivity.this, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnConfirmDeleteAccount.setEnabled(!isLoading);
        btnCancelDeleteAccount.setEnabled(!isLoading);
        editTextDeletePassword.setEnabled(!isLoading);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isLoading);
        }
    }
}
