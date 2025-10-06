package com.example.bookup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileSetupActivity extends AppCompatActivity {
    private static final String TAG = "ProfileSetUpActivty";
    private EditText firstname;
    private EditText lastname;
    private EditText phoneNumber;
    private RadioGroup genderSelect;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button saveProfile;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_setup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null){
            Intent intent = new Intent(ProfileSetupActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        firstname = findViewById(R.id.edit_text_first_name);
        lastname = findViewById(R.id.edit_text_last_name);
        phoneNumber = findViewById(R.id.edit_text_phone);
        genderSelect = findViewById(R.id.radio_group_gender);
        saveProfile = findViewById(R.id.button_save_profile);

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

    private void saveUserProfile() {
        Log.d(TAG, "Entering saveUserProfile() method.");

        String Firstname = firstname.getText().toString().trim();
        String Lastname = lastname.getText().toString().trim();
        String Phone = phoneNumber.getText().toString().trim();

        //Grtting my selected gender
        int selectedGenderId = genderSelect.getCheckedRadioButtonId();
        RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
        String gender = (selectedGenderRadioButton != null) ? selectedGenderRadioButton.getText().toString(): "";

        //Validation
        if (TextUtils.isEmpty(Firstname)){
            firstname.setError("This field is required");
            firstname.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(Lastname)){
            lastname.setError("This field is required");
            lastname.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(Phone)){
            phoneNumber.setError("This field is required");
            phoneNumber.requestFocus();
            return;
        }
        if (selectedGenderId == -1){
            Toast.makeText(ProfileSetupActivity.this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();  //getting the current user

        if (user == null){
            Toast.makeText(ProfileSetupActivity.this, "User is not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileSetupActivity.this, SignInActivity.class));
            return;
        }

        Log.d(TAG, "All validation passed. Attempting to save to Firestore.");

        String userId = user.getUid();

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("Firstname", Firstname);
        userProfile.put("Lastname", Lastname);
        userProfile.put("Phone", Phone);
        userProfile.put("Gender", gender);

        userProfile.put("ProfileCompleted", true);
        userProfile.put("timestamp", System.currentTimeMillis());
        db.collection("users").document(userId).set(userProfile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                     public void onSuccess(Void avoid) {
                     Log.d(TAG, "Basic user Profile saved successfully for user: " +userId);
                     Toast.makeText(ProfileSetupActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();

                      Intent intent = new Intent(ProfileSetupActivity.this, SubjectSelectionActivity.class);
                      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                      startActivity(intent);
                      finish();
                      return;
                     }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error saving profile for user: " +userId, e);
                Toast.makeText(ProfileSetupActivity.this, "Omoo, sorry we can't save your profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

