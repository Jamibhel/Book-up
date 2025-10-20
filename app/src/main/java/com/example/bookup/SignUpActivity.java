package com.example.bookup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
// import android.widget.EditText; // Removed as we use TextInputEditText
import android.widget.ProgressBar; // Add ProgressBar import
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText; // Correct import for Material text input
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;

    private TextView btnSignInLink;
    private MaterialButton btnSignUp; // This refers to the "Create Account" button
    private MaterialButton btnGoogleSignUp;
    private ProgressBar progressBar; // <-- You also need to declare and find this in initViews

    private FirebaseAuth mAuth;

    private static final String TAG = "SignUpActivity";

    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_GOOGLE_SIGN_UP = 1002;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // CRITICAL: You are using R.layout.signup. Ensure this is the correct XML file.
        // If it was supposed to be R.layout.activity_sign_up, change it here.
        setContentView(R.layout.signup); // Keep this as R.layout.signup based on your provided XML

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupGoogleSignIn();
        setupClickListeners();
    }

    private void initViews() {
        // IDs from your provided signup.xml
        editTextEmail = findViewById(R.id.getEmail);
        editTextPassword = findViewById(R.id.getPassword);
        editTextConfirmPassword = findViewById(R.id.confirm_Password);

        btnSignInLink = findViewById(R.id.signInLink); // ID of the "Sign In" TextView
        btnSignUp = findViewById(R.id.signup); // CRITICAL FIX: Changed from signup_button to signup
        btnGoogleSignUp = findViewById(R.id.googleSignUpButton); // ID of the Google MaterialButton
        progressBar = findViewById(R.id.progress_bar); // Add this if you have a ProgressBar in signup.xml
        // If R.id.progress_bar does not exist in signup.xml, this will still be null.
        // If you don't have a progress bar in signup.xml, either add one or remove this line and related setLoading calls.
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        if (btnSignUp != null) { // Added null check for safety
            btnSignUp.setOnClickListener(v -> createAccount());
        } else {
            Log.e(TAG, "btnSignUp (R.id.signup) is null. Check signup.xml for ID.");
        }

        if (btnSignInLink != null) { // Added null check for safety
            btnSignInLink.setOnClickListener(v -> {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            });
        } else {
            Log.e(TAG, "btnSignInLink (R.id.signInLink) is null. Check signup.xml for ID.");
        }

        if (btnGoogleSignUp != null) { // Added null check for safety
            btnGoogleSignUp.setOnClickListener(v -> googleSignup());
        } else {
            Log.e(TAG, "btnGoogleSignUp (R.id.googleSignUpButton) is null. Check signup.xml for ID.");
        }
    }

    private void createAccount() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)){
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)){
            editTextConfirmPassword.setError("Password must be confirmed!");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)){
            editTextConfirmPassword.setError("Passwords don't match");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if (password.length() < 6) { // Firebase requires at least 6 characters for password
            editTextPassword.setError("Password must be at least 6 characters long.");
            editTextPassword.requestFocus();
            return;
        }

        Toast.makeText(SignUpActivity.this, "Registering User .......", Toast.LENGTH_SHORT).show();
        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            setLoading(false);
            if (task.isSuccessful()){
                Log.d(TAG, "CreateUserWithEmail:success");
                Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                redirectToProfileSetup();
            }
            else{
                Log.e(TAG, "CreateUserWithEmail:failure", task.getException());
                String errorMessage = "Authentication failed";

                if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                    errorMessage = "Password is too weak. Please use a stronger password (min 6 characters).";
                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                    errorMessage = "Invalid email format.";
                } else if (task.getException() instanceof FirebaseAuthUserCollisionException){
                    errorMessage = "An account with this email already exists. Try signing in.";
                } else if (task.getException() != null){
                    errorMessage = "Error: " + task.getException().getMessage();
                }
                Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void googleSignup(){
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signinIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signinIntent, RC_GOOGLE_SIGN_UP);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_UP){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google Sign In Successful, authenticating with Firebase...");
                firebaseAuthWithGoogle(account.getIdToken());
            }
            catch (ApiException e) {
                Log.w(TAG, "Google sign in failed: " + e.getStatusCode(), e);
                Toast.makeText(SignUpActivity.this, "Google Sign Up Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                setLoading(false);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken){
        setLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            setLoading(false);
            if (task.isSuccessful()){
                Log.d(TAG, "firebaseAuthWithGoogle: Successful");
                FirebaseUser user = mAuth.getCurrentUser();
                Toast.makeText(SignUpActivity.this, "Google Sign Up successful!", Toast.LENGTH_SHORT).show();
                redirectToProfileSetup();
            }
            else {
                Log.e(TAG, "firebaseAuthWithGoogle:failure", task.getException());
                String errorMessage = "Google sign up failed.";

                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    errorMessage = "An account with this Google email already exists. Try signing in with Google.";
                } else if (task.getException() != null) {
                    errorMessage = "Error: " + task.getException().getMessage();
                }
                Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void redirectToProfileSetup() {
        Intent intent = new Intent(SignUpActivity.this, ProfileSetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        // Only try to set visibility if progressBar exists in the layout.
        // Assuming your signup.xml currently does NOT have a ProgressBar,
        // so I will comment out the progressBar line here too.
        // If you add one, uncomment this line and ensure the ID is R.id.progress_bar.
        // progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (btnSignUp != null) btnSignUp.setEnabled(!isLoading);
        if (btnGoogleSignUp != null) btnGoogleSignUp.setEnabled(!isLoading);
        if (btnSignInLink != null) btnSignInLink.setEnabled(!isLoading);
        if (editTextEmail != null) editTextEmail.setEnabled(!isLoading);
        if (editTextPassword != null) editTextPassword.setEnabled(!isLoading);
        if (editTextConfirmPassword != null) editTextConfirmPassword.setEnabled(!isLoading);
    }
}
