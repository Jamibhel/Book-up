package com.example.bookup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Required for View.VISIBLE/GONE
import android.widget.ProgressBar;

import com.google.android.material.button.MaterialButton; // Explicit import for MaterialButton
import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseUser; // Not needed if auto-sign-in check is removed

public class SplashScreenActivity extends AppCompatActivity {

    // FirebaseAuth instance can still be initialized here if needed for other checks later,
    // but the auto-redirection logic for existing users is removed.
    private FirebaseAuth mAuth;

    private MaterialButton signupButton;
    private MaterialButton signinButton;
    private ProgressBar progressBar; // Declared for potential future use or if present in XML

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth (can be useful for other checks later)

        // Initialize UI components using IDs from activity_splash_screen.xml
        signupButton = findViewById(R.id.signup_button);
        signinButton = findViewById(R.id.signin_button);
        progressBar = findViewById(R.id.progress_bar_splash); // Assuming this ID is in your XML

        setupClickListeners();

        // Initially, the progressBar should be hidden (as set in XML visibility="gone")
        // and buttons visible. If you add a loading phase later, you'd manage visibility here.
        // For now, it shows the buttons immediately.
    }

    // IMPORTANT: The onStart() method (which typically handles auto-redirection for existing users)
    // is intentionally omitted or its content removed. This ensures that the user ALWAYS
    // sees the interactive welcome screen with "Log In" and "Get Started" buttons.
    // If you ever want auto-login behavior, you would re-implement onStart() to check mAuth.getCurrentUser().

    private void setupClickListeners() {
        if (signupButton != null) {
            signupButton.setOnClickListener(v -> navigateToSignUpActivity());
        } else {
            // Log a warning if the button is not found, indicating a layout mismatch
            // This shouldn't happen if activity_splash_screen.xml is correct
            android.util.Log.e("SplashScreenActivity", "Sign Up Button (R.id.signup_button) not found in layout.");
        }

        if (signinButton != null) {
            signinButton.setOnClickListener(v -> navigateToSignInActivity());
        } else {
            android.util.Log.e("SplashScreenActivity", "Sign In Button (R.id.signin_button) not found in layout.");
        }
    }

    // Helper method for navigation (unchanged)
    private void navigateToHomePage() {
        Intent intent = new Intent(SplashScreenActivity.this, HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Helper method for navigation (unchanged)
    private void navigateToSignUpActivity() {
        Intent intent = new Intent(SplashScreenActivity.this, SignUpActivity.class);
        startActivity(intent);
        // finish() is optional here; typically you don't finish this screen if you expect
        // the user to return to it after sign-up/sign-in attempts from other activities.
    }

    // Helper method for navigation (unchanged)
    private void navigateToSignInActivity() {
        Intent intent = new Intent(SplashScreenActivity.this, SignInActivity.class);
        startActivity(intent);
        // finish() is optional here.
    }

    // You can add a setLoading(boolean isLoading) method here if you introduce a loading phase
    // (e.g., fetching remote config or initial data) that temporarily hides buttons.
    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (signupButton != null) {
            signupButton.setEnabled(!isLoading);
        }
        if (signinButton != null) {
            signinButton.setEnabled(!isLoading);
        }
    }
}
