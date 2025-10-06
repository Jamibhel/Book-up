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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectSelectionActivity extends AppCompatActivity implements SubjectAdapter.OnItemRemoveListener{

    private static final String TAG = "SubjectSelectionActivity";
    private EditText edtTextDepartment;
    private EditText edtTextCourseCode;
    private EditText edtTextCourseName;
    private EditText edt_topics;
    private RadioGroup studentGroupSubjectRole;
    private Button  addSubject;
    private RecyclerView recyclerViewSubjects;
    private SubjectAdapter subjectAdapter;
    private List<Map<String, Object>> subjectsList;
    private Button saveAllSubjects;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;



    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_selection);
    //initialization
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();

    if (mAuth.getCurrentUser() == null) {
        startActivity(new Intent (SubjectSelectionActivity.this, SignInActivity.class));
        finish();
        return;
    } //checking whether my user is logged in

    edtTextDepartment = findViewById(R.id.edit_text_department);
    edtTextCourseCode = findViewById(R.id.edit_text_course_code);
    edtTextCourseName = findViewById(R.id.edit_text_course_name);                   //adding subjects
    edt_topics = findViewById(R.id.edit_text_topics);
    studentGroupSubjectRole = findViewById(R.id.radio_group_subject_role);
    addSubject = findViewById(R.id.button_add_subject);

    recyclerViewSubjects = findViewById(R.id.recycler_view_subjects);
    recyclerViewSubjects.setLayoutManager(new LinearLayoutManager(this));
    subjectsList = new ArrayList<>();
    subjectAdapter = new SubjectAdapter(subjectsList);

    //setting up listener for removing subject frm the recyclerview
        subjectAdapter.setOnItemRemoveListener(new SubjectAdapter.OnItemRemoveListener() {
            public void onItemRemove(int position) {
                subjectsList.remove(position);
                subjectAdapter.notifyItemRemoved(position);
                // No need to save to Firestore immediately, wait for 'Save All Subjects'
                Toast.makeText(SubjectSelectionActivity.this, "Subject removed from list.", Toast.LENGTH_SHORT).show();
            }
        });
        saveAllSubjects = findViewById(R.id.button_save_all_subjects);

       saveAllSubjects.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
                saveAllSubjectsToFirestore();
           }
       });

       addSubject.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               addSubjectToList();
           }
       });
       loadExistingSubjects();
    }

    @Override
    public void onItemRemove(int position) {
        subjectsList.remove(position);
        subjectAdapter.notifyItemRemoved(position);
        Toast.makeText(SubjectSelectionActivity.this, "Subject removed from List", Toast.LENGTH_SHORT).show();
    }

    private void loadExistingSubjects() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return; // Should not happen if onCreate check works

        String userId = user.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Firestore can return List<Map<String, Object>> directly if structured correctly
                            List<Map<String, Object>> existingSubjects = (List<Map<String, Object>>) documentSnapshot.get("subjects");
                            if (existingSubjects != null && !existingSubjects.isEmpty()) {
                                subjectsList.clear(); // Clear any initial empty list
                                subjectsList.addAll(existingSubjects); // Add fetched subjects
                                subjectAdapter.notifyDataSetChanged(); // Tell adapter to re-draw
                                Log.d(TAG, "Loaded " + existingSubjects.size() + " existing subjects.");
                            } else {
                                Log.d(TAG, "No existing subjects found for user.");
                            }
                        } else {
                            Log.d(TAG, "User document does not exist, no subjects to load.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error loading existing subjects: " + e.getMessage(), e);
                        Toast.makeText(SubjectSelectionActivity.this, "Heyyyy ,we can't load your subjects.", Toast.LENGTH_SHORT).show();
                    }
                });

    }



    private void saveAllSubjectsToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SubjectSelectionActivity.this, SignInActivity.class));
            finish();
            return;
        }
        String userId = user.getUid();
        db.collection("users").document(userId)
                .update("subjects", subjectsList) // Use 'update' to modify a specific field
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "All subjects saved successfully for user: " + userId);
                        Toast.makeText(SubjectSelectionActivity.this, "Subjects saved and profile updated!", Toast.LENGTH_SHORT).show();

                        // Redirect to the main app activity or Dashboard
                        Intent intent = new Intent(SubjectSelectionActivity.this, HomePageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error saving subjects for user: " + userId, e);
                        Toast.makeText(SubjectSelectionActivity.this, "Heyyyy ,we can't save your subjects, try again ", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addSubjectToList() {
        String department = edtTextDepartment.getText().toString().trim();
        String courseCode = edtTextCourseCode.getText().toString().trim();
        String courseName = edtTextCourseName.getText().toString().trim();
        String topics = edt_topics.getText().toString().trim();
        int selectedRadioButtonId = studentGroupSubjectRole.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(department)) {
            edtTextDepartment.setError("Department is required.");
            edtTextDepartment.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(courseCode)) {
            edtTextCourseCode.setError("Course Code is required.");
            edtTextCourseCode.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(courseName)) {
            edtTextCourseName.setError("Course Name is required.");
            edtTextCourseName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(topics)) {
            edt_topics.setError("Topics are required (e.g., comma-separated).");
            edt_topics.requestFocus();
            return;
        }
        if (selectedRadioButtonId == -1) {
            Toast.makeText(this, "Please select if you are Learning or Tutoring this subject.", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        String role = selectedRadioButton.getText().toString();


        Map<String, Object> newSubject = new HashMap<>();
        newSubject.put("department", department);
        newSubject.put("courseCode", courseCode);
        newSubject.put("courseName", courseName);
        newSubject.put("topics", topics);
        newSubject.put("role", role);

        subjectsList.add(newSubject);
        subjectAdapter.notifyItemInserted(subjectsList.size() - 1);
        edtTextDepartment.setText("");
        edtTextCourseCode.setText("");
        edtTextCourseName.setText("");

        studentGroupSubjectRole.check(R.id.radio_learning); //Defaulted to learning
        Toast.makeText(SubjectSelectionActivity.this, "Subject added to the list, Click 'Save All' to confirm", Toast.LENGTH_LONG).show();
    }


}
