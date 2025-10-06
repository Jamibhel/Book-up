package com.example.bookup;

import android.app.ComponentCaller;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity {

    private EditText getEmail;
    private EditText getPassword;
    private EditText getConfirmPassword;
    private EditText getPhone;

    private TextView signin;

    private Button signup;

    private FirebaseAuth mAuth;

    private static final String TAG = "SignUpActivity";

    // Google Sign-In related variables
    private MaterialButton googleSignUpButton;
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_GOOGLE_SIGN_UP = 1002;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        mAuth = FirebaseAuth.getInstance(); //Initialising my firebase

        getEmail = findViewById(R.id.getEmail);
        getPassword = findViewById(R.id.getPassword);
        getConfirmPassword = findViewById(R.id.confirm_Password);

        signin = findViewById(R.id.signin);

        signup = findViewById(R.id.signup);


        // Initializing Google Signup Button and client
        googleSignUpButton = findViewById(R.id.googleSignUpButton);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to start the Google Sign-In flow
                googleSignup();
            }
        });


        //Setting onClickListener for signup Button
    
    signup.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            createAccount(); //calling the method for signup

        }
        //Setting onClickListener for signin link
    });
    signin.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    });
    }

    //configuring the create aco0unt method
    private void createAccount() {
        String email = getEmail.getText().toString().trim();
        String password = getPassword.getText().toString().trim();
        String confirmPassword = getConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            getEmail.setError("Email is required");
            getEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)){
            getPassword.setError("Password is required");
            getPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)){
            getConfirmPassword.setError("Password must be confirmed!");
            getConfirmPassword.requestFocus();
            return;
        }

        if (email.isEmpty()||password.isEmpty()||confirmPassword.isEmpty()){
        Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
        return;
         }

         if (!password.equals(confirmPassword)){
        getConfirmPassword.setError("Password don't match");
        getConfirmPassword.requestFocus();
        return;
         }

        Toast.makeText(SignUpActivity.this, "Registering User .......", Toast.LENGTH_SHORT).show();

         mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
             @Override
             public void onComplete( Task<AuthResult> task) {
                 if (task.isSuccessful()){
                     Log.d(TAG, "CreateUserWithEmail:success");
                     Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                     redirectToProfileSetup();
                 }
                 else{
                     Log.d(TAG, "CreateUserWithEmail:failure", task.getException());
                     String errorMessage = "Authentication failed";

                     if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                         errorMessage = "Password is too weak. Please use a stronger password";
                     }
                     if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                         errorMessage = "Invalid email format.";
                     }
                     if (task.getException() instanceof FirebaseAuthUserCollisionException){
                         errorMessage = "An account with this email already exists";
                     }
                     if (task.getException() != null){
                         errorMessage = "Error" +task.getException().getMessage();
                     }
                     Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                 }

             }
         });

    }

    //Initiating google signup flow with this method
    private void googleSignup(){
        Intent signinIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signinIntent, RC_GOOGLE_SIGN_UP);
    }
    //using this method to handle results from google sign in
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_UP){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google sign in was successful, authenticate with the firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Firebase Authentication Successful");
                Toast.makeText(SignUpActivity.this, "Google Sign Up Successful", Toast.LENGTH_SHORT).show();
                FirebaseAuthWithGoogle(account.getIdToken());
            }
            catch (ApiException e) {
                Log.w(TAG, "Firebase Authentication Failed");
                Toast.makeText(SignUpActivity.this, "Google SignUp Failed, Check connection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //New method to authenticate with Firebase using Google's ID token
    private void FirebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    //Update the Ui with signed information
                    Log.d(TAG, "firebaseAuthWithGoogle: Successful");
                    Toast.makeText(SignUpActivity.this, "Google SignUp is successful", Toast.LENGTH_SHORT).show();
                    redirectToProfileSetup();
                }
                else {
                    Log.w(TAG, "firebaseAuthWithGoogle:failure");
                    Toast.makeText(SignUpActivity.this, "Sign Up Failed, Check your internet connection", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void redirectToProfileSetup() {
        Intent intent = new Intent(SignUpActivity.this, ProfileSetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
