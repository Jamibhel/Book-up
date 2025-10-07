package com.example.bookup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout; // For location sharing layout
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.HomePageActivity; // For Logout redirection
import com.example.bookup.ProfileSetupActivity; // For initial profile setup or editing
import com.example.bookup.R;
import com.example.bookup.SignInActivity; // For Logout redirection
import com.example.bookup.StudyMaterialOverviewAdapter;
import com.example.bookup.SubjectSelectionActivity; // For managing subjects
import com.example.bookup.models.StudyMaterialOverview;
import com.google.android.material.switchmaterial.SwitchMaterial; // For location sharing switch

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // UI Elements
    private ImageView imgProfilePicture;
    private TextView textFullName;
    private TextView textEmail;
    private Button btnChangeProfilePicture;
    private TextView textPhoneNumber;
    private TextView textGender;
    private Button btnEditPersonalInfo;
    private LinearLayout layoutLocationSharing; // Container for location sharing UI
    private SwitchMaterial switchLocationSharing;
    private RecyclerView recyclerMyMaterials;
    private TextView textNoUploadedMaterials;
    private Button btnUploadMaterial;
    private Button btnManageMySubjects;
    private Button btnLogout;

    // Adapters
    private StudyMaterialOverviewAdapter myMaterialsAdapter;
    private List<StudyMaterialOverview> myMaterialsList = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load user data every time the fragment becomes visible
        loadUserData();
    }

    /**
     * Initializes all UI components from the inflated view.
     * @param view The inflated view of the fragment.
     */
    private void initViews(View view) {
        imgProfilePicture = view.findViewById(R.id.img_profile_picture);
        textFullName = view.findViewById(R.id.text_full_name);
        textEmail = view.findViewById(R.id.text_email);
        btnChangeProfilePicture = view.findViewById(R.id.btn_change_profile_picture);
        textPhoneNumber = view.findViewById(R.id.text_phone_number);
        textGender = view.findViewById(R.id.text_gender);
        btnEditPersonalInfo = view.findViewById(R.id.btn_edit_personal_info);
        layoutLocationSharing = view.findViewById(R.id.layout_location_sharing);
        switchLocationSharing = view.findViewById(R.id.switch_location_sharing);
        recyclerMyMaterials = view.findViewById(R.id.recycler_my_materials);
        textNoUploadedMaterials = view.findViewById(R.id.text_no_uploaded_materials);
        btnUploadMaterial = view.findViewById(R.id.btn_upload_material);
        btnManageMySubjects = view.findViewById(R.id.btn_manage_my_subjects);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    /**
     * Sets up the RecyclerView for displaying user's uploaded materials.
     */
    private void setupRecyclerView() {
        recyclerMyMaterials.setLayoutManager(new LinearLayoutManager(getContext()));
        myMaterialsAdapter = new StudyMaterialOverviewAdapter(myMaterialsList);
        recyclerMyMaterials.setAdapter(myMaterialsAdapter);
        myMaterialsAdapter.setOnItemClickListener(material -> {
            Toast.makeText(getContext(), "Clicked My Material: " + material.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to MaterialDetailsActivity for 'material.getMaterialId()'
        });
    }

    /**
     * Sets up click listeners for all buttons in the Profile Fragment.
     */
    private void setupClickListeners() {
        btnChangeProfilePicture.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change Profile Picture Coming Soon!", Toast.LENGTH_SHORT).show();
            // TODO: Implement image picker and upload to Firebase Storage
        });

        btnEditPersonalInfo.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit Personal Info Coming Soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to an EditProfileActivity (possibly reusing ProfileSetupActivity layout with pre-filled data)
        });

        switchLocationSharing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Implement logic to update user's location sharing preference in Firestore
            Toast.makeText(getContext(), "Location Sharing " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
            updateLocationSharingPreference(isChecked);
        });

        btnUploadMaterial.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Upload Material Coming Soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to UploadMaterialActivity
        });

        btnManageMySubjects.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SubjectSelectionActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getContext(), "Logged out.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish(); // Finish the hosting HomepageActivity
        });
    }

    /**
     * Loads the authenticated user's profile data and their uploaded materials from Firestore.
     * Updates the UI accordingly.
     */
    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Should not happen if HomepageActivity's auth check is correct
            startActivity(new Intent(getContext(), SignInActivity.class));
            requireActivity().finish();
            return;
        }

        String userId = currentUser.getUid();
        textEmail.setText(currentUser.getEmail()); // Email always comes from FirebaseAuth

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Load basic profile data
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        String gender = documentSnapshot.getString("gender");
                        Boolean isTutor = documentSnapshot.getBoolean("isTutor"); // Assuming a field for this
                        Boolean shareLocation = documentSnapshot.getBoolean("shareLocation"); // For tutor location sharing

                        textFullName.setText(String.format("%s %s", firstName, lastName));
                        textPhoneNumber.setText("Phone: " + (phoneNumber != null ? phoneNumber : "N/A"));
                        textGender.setText("Gender: " + (gender != null ? gender : "N/A"));

                        // Show/hide location sharing UI if the user is a tutor
                        if (isTutor != null && isTutor) {
                            layoutLocationSharing.setVisibility(View.VISIBLE);
                            switchLocationSharing.setChecked(shareLocation != null && shareLocation);
                        } else {
                            layoutLocationSharing.setVisibility(View.GONE);
                        }

                        // TODO: Load and display profile picture from Cloud Storage
                        // String profilePicUrl = documentSnapshot.getString("profilePicUrl");
                        // if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        //     Glide.with(this).load(profilePicUrl).placeholder(R.drawable.ic_default_profile_picture).into(imgProfilePicture);
                        // } else {
                        //     imgProfilePicture.setImageResource(R.drawable.ic_default_profile_picture);
                        // }

                        // --- Load My Uploaded Materials ---
                        loadMyUploadedMaterials(userId);

                    } else {
                        Log.d(TAG, "User document does not exist for " + userId + ". Redirecting to setup.");
                        Toast.makeText(getContext(), "Please complete your profile first.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getContext(), ProfileSetupActivity.class));
                        requireActivity().finish(); // Ensure HomepageActivity is finished
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user profile data: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error loading profile data.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Placeholder for updating location sharing preference in Firestore.
     * @param isChecked New state of the switch.
     */
    private void updateLocationSharingPreference(boolean isChecked) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .update("shareLocation", isChecked)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Location sharing preference updated successfully."))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating location sharing preference", e));
    }

    /**
     * Fetches materials uploaded by the current user from Firestore.
     * @param userId The UID of the current user.
     */
    private void loadMyUploadedMaterials(String userId) {
        // This assumes a 'studyMaterials' collection exists where each document
        // has a field 'uploaderUid' that matches the userId.
        // We will implement the upload process later.
        db.collection("studyMaterials")
                .whereEqualTo("uploaderUid", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myMaterialsList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            // Map Firestore document to StudyMaterialOverview object
                            StudyMaterialOverview material = doc.toObject(StudyMaterialOverview.class);
                            if (material != null) {
                                material.setMaterialId(doc.getId()); // Store document ID
                                myMaterialsList.add(material);
                            }
                        }
                        myMaterialsAdapter.notifyDataSetChanged();
                        textNoUploadedMaterials.setVisibility(View.GONE);
                    } else {
                        textNoUploadedMaterials.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading uploaded materials: " + e.getMessage(), e);
                    textNoUploadedMaterials.setVisibility(View.VISIBLE);
                });
    }
}
