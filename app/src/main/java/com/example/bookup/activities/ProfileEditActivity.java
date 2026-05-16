package com.example.bookup.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileEditActivity extends AppCompatActivity {
    private ShapeableImageView profileImage;
    private TextInputEditText editFirstName, editLastName, editPhone, editBio, editGrade, editExp;
    private RadioGroup radioGroupGender, radioGroupRole;
    private RadioButton radioMale, radioFemale, radioStudent, radioTutor;
    private MaterialButton btnSave, btnChangePhoto;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User currentUser;
    private Uri imageUri;

    private final ActivityResultLauncher<String> imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    profileImage.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        loadData();
    }

    private void initViews() {
        profileImage = findViewById(R.id.image_profile_picture_edit);
        btnChangePhoto = findViewById(R.id.btn_change_picture_in_edit);
        editFirstName = findViewById(R.id.edit_text_first_name);
        editLastName = findViewById(R.id.edit_text_last_name);
        editPhone = findViewById(R.id.edit_text_phone_number);
        editBio = findViewById(R.id.edit_text_bio);
        editGrade = findViewById(R.id.edit_text_grade_level);
        editExp = findViewById(R.id.edit_text_experience);
        
        radioGroupGender = findViewById(R.id.radio_group_gender);
        radioMale = findViewById(R.id.radio_male);
        radioFemale = findViewById(R.id.radio_female);
        
        radioGroupRole = findViewById(R.id.radio_group_role);
        radioStudent = findViewById(R.id.radio_student);
        radioTutor = findViewById(R.id.radio_tutor);
        
        btnSave = findViewById(R.id.btn_save_profile);
        progressBar = findViewById(R.id.progress_bar_edit_profile);

        btnChangePhoto.setOnClickListener(v -> imagePicker.launch("image/*"));
        btnSave.setOnClickListener(v -> saveProfile());
        findViewById(R.id.btn_cancel_edit).setOnClickListener(v -> finish());
    }

    private void loadData() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            currentUser = doc.toObject(User.class);
            if (currentUser != null) {
                editFirstName.setText(currentUser.getFirstName());
                editLastName.setText(currentUser.getLastName());
                editPhone.setText(doc.getString("phoneNumber"));
                editBio.setText(currentUser.getBio());
                editGrade.setText(doc.getString("academicLevel"));
                editExp.setText(doc.getString("experience"));

                String gender = doc.getString("gender");
                if ("Male".equalsIgnoreCase(gender)) radioMale.setChecked(true);
                else if ("Female".equalsIgnoreCase(gender)) radioFemale.setChecked(true);

                if ("tutor".equalsIgnoreCase(currentUser.getRole())) radioTutor.setChecked(true);
                else radioStudent.setChecked(true);

                Glide.with(this).load(currentUser.getPhotoUrl()).placeholder(R.drawable.ic_profile_placeholder).into(profileImage);
            }
        });
    }

    private void saveProfile() {
        if (currentUser == null) return;
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        if (imageUri != null) uploadImage();
        else updateFirestore(currentUser.getPhotoUrl());
    }

    private void uploadImage() {
        StorageReference ref = FirebaseStorage.getInstance().getReference("profile_pics/" + UUID.randomUUID().toString());
        ref.putFile(imageUri).addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(uri -> updateFirestore(uri.toString())));
    }

    private void updateFirestore(String photoUrl) {
        String gender = radioMale.isChecked() ? "Male" : (radioFemale.isChecked() ? "Female" : "Other");
        String role = radioTutor.isChecked() ? "tutor" : "student";

        Map<String, Object> map = new HashMap<>();
        map.put("firstName", editFirstName.getText().toString());
        map.put("lastName", editLastName.getText().toString());
        map.put("displayName", editFirstName.getText().toString() + " " + editLastName.getText().toString());
        map.put("phoneNumber", editPhone.getText().toString());
        map.put("bio", editBio.getText().toString());
        map.put("gender", gender);
        map.put("role", role);
        map.put("isTutor", "tutor".equals(role));
        map.put("photoUrl", photoUrl);
        map.put("experience", editExp.getText().toString());
        map.put("academicLevel", editGrade.getText().toString());

        db.collection("users").document(currentUser.getId()).update(map).addOnSuccessListener(v -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
