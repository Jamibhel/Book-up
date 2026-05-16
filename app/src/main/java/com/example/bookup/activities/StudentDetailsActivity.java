package com.example.bookup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "user_id";
    
    private ImageView studentImage;
    private TextView studentName, studentEmail, studentBio;
    private ChipGroup chipGroupLearning;
    private MaterialButton btnMessage;
    
    private String studentId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);

        db = FirebaseFirestore.getInstance();
        studentId = getIntent().getStringExtra(EXTRA_USER_ID);

        initViews();
        if (studentId != null) loadStudentData();
        else finish();
    }

    private void initViews() {
        studentImage = findViewById(R.id.student_profile_image);
        studentName = findViewById(R.id.student_name);
        studentEmail = findViewById(R.id.student_email);
        studentBio = findViewById(R.id.student_bio);
        chipGroupLearning = findViewById(R.id.chip_group_learning_subjects);
        btnMessage = findViewById(R.id.btn_message_student);
        
        findViewById(R.id.toolbar).setOnClickListener(v -> onBackPressed());
    }

    private void loadStudentData() {
        db.collection("users").document(studentId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) displayData(user);
            }
        });
    }

    private void displayData(User user) {
        studentName.setText(user.getDisplayName());
        studentEmail.setText(user.getEmail());
        studentBio.setText(user.getBio() != null ? user.getBio() : "No bio available.");

        Glide.with(this).load(user.getPhotoUrl()).placeholder(R.drawable.ic_profile_placeholder).into(studentImage);

        chipGroupLearning.removeAllViews();
        if (user.getTutoringSubjects() != null) {
            for (String s : user.getTutoringSubjects()) {
                Chip chip = new Chip(this);
                chip.setText(s);
                chip.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.primary_faded));
                chipGroupLearning.addView(chip);
            }
        }
        
        btnMessage.setOnClickListener(v -> {
            FirebaseUser cu = FirebaseAuth.getInstance().getCurrentUser();
            if (cu == null || user == null) return;

            User me = new User();
            me.setId(cu.getUid());
            me.setDisplayName(cu.getDisplayName());
            me.setPhotoUrl(cu.getPhotoUrl() != null ? cu.getPhotoUrl().toString() : "");

            new com.example.bookup.repositories.ChatRepository().getOrCreateChatChannel(me, user)
                .addOnSuccessListener(id -> {
                    Intent intent = new Intent(this, HomePageActivity.class);
                    intent.putExtra("channelId", id);
                    intent.putExtra("channelName", user.getDisplayName());
                    intent.putExtra("tabIndex", 4);
                    startActivity(intent);
                });
        });
    }
}
