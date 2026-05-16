package com.example.bookup.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookup.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.messaging.FirebaseMessaging;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001; // Request code for Google Sign-In

    // UI elements
    private TextInputEditText editTextEmail, editTextPassword;
    private Button btnSignIn, btnForgotPassword, btnGoogleSignIn, btnSignUpLink;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        btnForgotPassword = findViewById(R.id.btn_forgot_password);
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);
        btnSignUpLink = findViewById(R.id.btn_sign_up_link);
        progressBar = findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        setupClickListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setupClickListeners() {
        btnSignIn.setOnClickListener(v -> signInWithEmail());
        btnForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        btnSignUpLink.setOnClickListener(v -> navigateToSignUp());
    }

    private void signInWithEmail() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignInActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                        navigateToHomePage();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(SignInActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(SignInActivity.this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                setLoading(false); // Ensure loading is off if it was set during sign-in attempt
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "firebaseAuthWithGoogle:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignInActivity.this, "Google sign-in successful.", Toast.LENGTH_SHORT).show();
                        navigateToHomePage();
                    } else {
                        Log.w(TAG, "firebaseAuthWithGoogle:failure", task.getException());
                        Toast.makeText(SignInActivity.this, "Firebase authentication with Google failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToHomePage() {
        // First check if the user actually exists
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // Something went wrong
            Toast.makeText(this, "Authentication error. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save device token for push notifications
        saveDeviceToken(user.getUid());

        try {
            // Create intent for HomePage using the correct activity package
            Intent intent = new Intent(SignInActivity.this, com.example.bookup.activities.HomePageActivity.class);
            // Clear entire task stack to prevent going back
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            
            // Add a small delay before finishing to ensure smooth transition
            new android.os.Handler().postDelayed(() -> {
                finish();
            }, 100);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to HomePage: " + e.getMessage());
            Toast.makeText(this, "Error starting HomePage. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save FCM device token to Firestore for push notifications
     */
    private void saveDeviceToken(String userId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .update("deviceTokens", FieldValue.arrayUnion(token))
                                .addOnSuccessListener(v -> {
                                    Log.d(TAG, "Device token saved successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to save device token", e);
                                });
                    } else {
                        Log.e(TAG, "Failed to get FCM token", task.getException());
                    }
                });
    }

    private void navigateToForgotPassword() {
        startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class));
    }

    private void navigateToSignUp() {
        startActivity(new Intent(SignInActivity.this, com.example.bookup.activities.SignUpActivity.class));
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSignIn.setEnabled(false);
            btnGoogleSignIn.setEnabled(false);
            btnSignUpLink.setEnabled(false);
            btnForgotPassword.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSignIn.setEnabled(true);
            btnGoogleSignIn.setEnabled(true);
            btnSignUpLink.setEnabled(true);
            btnForgotPassword.setEnabled(true);
        }
    }
}
