package com.example.bookup;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText getEmail;
    private Button sendLink;
    private TextView backToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        mAuth = FirebaseAuth.getInstance();

        getEmail = findViewById(R.id.getEmail);
        sendLink = findViewById(R.id.sendLink);
        backToLogin = findViewById(R.id.text_back_to_login);

        sendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              sendPasswordResetEmail();
            }
        });
        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to SignInActivity
                Intent intent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
                startActivity(intent);
                finish(); // Finish this activity so they don't return to it
            }
        });

    }

    private void sendPasswordResetEmail() {
        String email = getEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            getEmail.setError("Email is required to reset password.");
            getEmail.requestFocus();
            return;
        }

        Toast.makeText(this, "Sending password reset email...", Toast.LENGTH_SHORT).show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password reset email sent to " + email);
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Password reset email sent to " + email + ". Check your inbox (and spam folder)!",
                                    Toast.LENGTH_LONG).show();
                            // Optionally navigate back to login after showing success
                            Intent intent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.w(TAG, "Failed to send password reset email.", task.getException());
                            String errorMessage = "Failed to send reset email. Please try again.";

                            // Check for specific exceptions
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                // Important for security: do not reveal whether the email exists.
                                // Just say "If an account exists..." or a generic failure.
                                // This is especially true if you have Email Enumeration Protection enabled.
                                errorMessage = "If an account exists for this email, a reset link has been sent.";
                                Log.w(TAG, "Password reset for non-existent or invalid user attempted: " + email);
                            } else if (task.getException() != null) {
                                errorMessage = "Error: " + task.getException().getMessage();
                            }

                            Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
