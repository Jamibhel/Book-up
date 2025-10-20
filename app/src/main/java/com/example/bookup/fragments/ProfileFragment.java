package com.example.bookup.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Original Button import (MaterialButton replaces these in XML but code might reference Button)
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.AdminPanelActivity; // NEW: For launching Admin Panel
import com.example.bookup.ChangePasswordActivity;
import com.example.bookup.DeleteAccountActivity; // NEW: For launching Delete Account
import com.example.bookup.MaterialDetailsActivity;
import com.example.bookup.ProfileEditActivity;
import com.example.bookup.ProfileSetupActivity;
import com.example.bookup.R;
import com.example.bookup.SignInActivity;
import com.example.bookup.StudyMaterialOverviewAdapter;
import com.example.bookup.SubjectSelectionActivity;
import com.example.bookup.UploadMaterialActivity;
import com.example.bookup.models.StudyMaterial;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder; // NEW: For Logout confirmation
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // UI Elements
    private ShapeableImageView imgProfilePicture;
    private TextView textFullName;
    private TextView textEmail;
    // Removed btnChangeProfilePicture as it's handled by ProfileEditActivity
    private MaterialButton btnEditProfile; // General edit profile button
    private TextView textUserRole;
    private TextView textPhoneNumberValue;
    private TextView textGenderValue;
    private MaterialButton btnEditPersonalInfo; // Specific edit personal info button
    private ChipGroup chipGroupUserSubjects;
    private TextView textNoSubjects;
    private MaterialButton btnManageMySubjects;
    private LinearLayout layoutLocationSharing;
    private SwitchMaterial switchLocationSharing;
    private SwitchMaterial switchNotifications;
    private MaterialButton btnAdminPanel; // NEW: Admin Panel button
    private MaterialButton btnChangePassword;
    private MaterialButton btnDeleteAccount;
    private RecyclerView recyclerMyMaterials;
    private TextView textNoUploadedMaterials;
    private MaterialButton btnUploadMaterial;
    private MaterialButton btnLogout;
    private ProgressBar progressBarProfile;

    // Adapters
    private StudyMaterialOverviewAdapter myMaterialsAdapter;
    private List<StudyMaterial> myMaterialsList = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser; // Declared here for broader scope if needed

    // Preferences for Notifications
    private SharedPreferences sharedPreferences;
    private static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();

        // Load notification preference state
        switchNotifications.setChecked(sharedPreferences.getBoolean(PREF_NOTIFICATIONS_ENABLED, true));

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
        btnEditProfile = view.findViewById(R.id.btn_edit_profile); // General edit button

        // Personal Information Card
        textUserRole = view.findViewById(R.id.text_user_role);
        textPhoneNumberValue = view.findViewById(R.id.text_phone_number_value);
        textGenderValue = view.findViewById(R.id.text_gender_value);
        btnEditPersonalInfo = view.findViewById(R.id.btn_edit_personal_info);

        // My Subjects Card
        chipGroupUserSubjects = view.findViewById(R.id.chip_group_user_subjects);
        textNoSubjects = view.findViewById(R.id.text_no_subjects);
        btnManageMySubjects = view.findViewById(R.id.btn_manage_my_subjects);

        // Account & Preferences Card
        layoutLocationSharing = view.findViewById(R.id.layout_location_sharing);
        switchLocationSharing = view.findViewById(R.id.switch_location_sharing);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        btnAdminPanel = view.findViewById(R.id.btn_admin_panel); // NEW: Init Admin Panel button
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);

        // My Uploaded Materials Section
        recyclerMyMaterials = view.findViewById(R.id.recycler_my_materials);
        textNoUploadedMaterials = view.findViewById(R.id.text_no_uploaded_materials);
        btnUploadMaterial = view.findViewById(R.id.btn_upload_material);

        // Logout & ProgressBar
        btnLogout = view.findViewById(R.id.btn_logout);
        progressBarProfile = view.findViewById(R.id.progress_bar_profile);
    }

    /**
     * Sets up the RecyclerView for displaying user's uploaded materials.
     */
    private void setupRecyclerView() {
        recyclerMyMaterials.setLayoutManager(new LinearLayoutManager(getContext()));
        myMaterialsAdapter = new StudyMaterialOverviewAdapter(myMaterialsList);
        recyclerMyMaterials.setAdapter(myMaterialsAdapter);
        myMaterialsAdapter.setOnMaterialClickListener(material -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), MaterialDetailsActivity.class);
                intent.putExtra(MaterialDetailsActivity.EXTRA_MATERIAL, material); // Pass the StudyMaterial object
                startActivity(intent);
            }
        });
    }

    /**
     * Sets up click listeners for all buttons and switches in the Profile Fragment.
     */
    private void setupClickListeners() {
        // General Edit Profile Button (from header card)
        btnEditProfile.setOnClickListener(v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), ProfileEditActivity.class));
            }
        });

        // Edit Personal Info Button (from personal info card)
        btnEditPersonalInfo.setOnClickListener(v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), ProfileEditActivity.class));
            }
        });

        // Location Sharing Toggle
        switchLocationSharing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Location Sharing " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
            }
            updateLocationSharingPreference(isChecked);
        });

        // Notifications Toggle
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (getContext() != null) {
                // Save preference
                sharedPreferences.edit().putBoolean(PREF_NOTIFICATIONS_ENABLED, isChecked).apply();
                Toast.makeText(getContext(), "Notifications " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();

                // Manage FCM subscription based on user preference
                if (isChecked) {
                    FirebaseMessaging.getInstance().subscribeToTopic("global_notifications")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) Log.d(TAG, "Subscribed to global_notifications");
                                else Log.e(TAG, "Failed to subscribe to global_notifications", task.getException());
                            });
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("global_notifications")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) Log.d(TAG, "Unsubscribed from global_notifications");
                                else Log.e(TAG, "Failed to unsubscribe from global_notifications", task.getException());
                            });
                }
            }
        });

        // NEW: Admin Panel button click listener
        btnAdminPanel.setOnClickListener(v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), AdminPanelActivity.class));
            }
        });

        // Upload Material
        btnUploadMaterial.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), UploadMaterialActivity.class);
                startActivity(intent);
            } else {
                Log.e(TAG, "Context is null, cannot launch UploadMaterialActivity.");
            }
        });

        // Manage My Subjects
        btnManageMySubjects.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), SubjectSelectionActivity.class);
                startActivity(intent);
            } else {
                Log.e(TAG, "Context is null, cannot launch SubjectSelectionActivity.");
            }
        });

        // Change Password
        btnChangePassword.setOnClickListener(v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), ChangePasswordActivity.class));
            }
        });

        // Delete Account
        btnDeleteAccount.setOnClickListener(v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), DeleteAccountActivity.class));
            }
        });

        // Logout with confirmation dialog
        btnLogout.setOnClickListener(v -> {
            if (getContext() != null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Confirm Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            mAuth.signOut();
                            Toast.makeText(getContext(), "Logged out.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getContext(), SignInActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish(); // Finish the hosting HomepageActivity
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss(); // Just close the dialog
                        })
                        .show();
            }
        });
    }

    /**
     * Loads the authenticated user's profile data and their uploaded materials from Firestore.
     * Updates the UI accordingly.
     */
    private void loadUserData() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user is logged in when loading profile data. Redirecting to sign-in.");
            if (getContext() != null) {
                startActivity(new Intent(getContext(), SignInActivity.class));
            }
            requireActivity().finish();
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Logged-in user UID: " + userId);
        setLoading(true);

        // Display basic Auth data first
        textFullName.setText(currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty() ? currentUser.getDisplayName() : "User Name");
        textEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "No Email");

        if (currentUser.getPhotoUrl() != null) {
            Glide.with(requireContext())
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(imgProfilePicture);
        } else {
            imgProfilePicture.setImageResource(R.drawable.ic_profile_placeholder);
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getContext() == null) {
                        Log.d(TAG, "Fragment not attached, skipping UI update for user data.");
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        // Load basic profile data
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        String gender = documentSnapshot.getString("gender");
                        String role = documentSnapshot.getString("role");
                        Boolean isTutor = documentSnapshot.getBoolean("isTutor");
                        Boolean shareLocation = documentSnapshot.getBoolean("shareLocation");
                        Boolean isAdmin = documentSnapshot.getBoolean("isAdmin"); // NEW: Get isAdmin flag

                        textFullName.setText(String.format("%s %s",
                                firstName != null ? firstName : "",
                                lastName != null ? lastName : "").trim());
                        textPhoneNumberValue.setText(phoneNumber != null ? phoneNumber : "N/A");
                        textGenderValue.setText(gender != null ? gender : "N/A");
                        textUserRole.setText(role != null ? role : "N/A");

                        // Show/hide location sharing UI if the user is a tutor
                        if (isTutor != null && isTutor) {
                            layoutLocationSharing.setVisibility(View.VISIBLE);
                            switchLocationSharing.setChecked(shareLocation != null && shareLocation);
                        } else {
                            layoutLocationSharing.setVisibility(View.GONE);
                        }

                        // NEW: Show/hide Admin Panel button
                        if (isAdmin != null && isAdmin) {
                            btnAdminPanel.setVisibility(View.VISIBLE);
                        } else {
                            btnAdminPanel.setVisibility(View.GONE);
                        }

                        // --- Load User Subjects ---
                        chipGroupUserSubjects.removeAllViews();
                        Set<String> userSubjects = new HashSet<>();

                        Object subjectsObject = documentSnapshot.get("subjects");
                        if (subjectsObject instanceof Map) {
                            Map<String, Boolean> subjectsMap = (Map<String, Boolean>) subjectsObject;
                            for (Map.Entry<String, Boolean> entry : subjectsMap.entrySet()) {
                                if (entry.getValue() != null && entry.getValue()) {
                                    userSubjects.add(entry.getKey());
                                }
                            }
                        } else if (subjectsObject instanceof List) { // Fallback for old List<String> format
                            List<?> rawSubjectsList = (List<?>) subjectsObject;
                            for (Object item : rawSubjectsList) {
                                if (item instanceof String) {
                                    userSubjects.add((String) item);
                                }
                            }
                        }

                        if (!userSubjects.isEmpty()) {
                            textNoSubjects.setVisibility(View.GONE);
                            for (String subject : userSubjects) {
                                Chip chip = createSubjectChip(subject);
                                chipGroupUserSubjects.addView(chip);
                            }
                        } else {
                            textNoSubjects.setVisibility(View.VISIBLE);
                        }

                        // --- Load My Uploaded Materials ---
                        loadMyUploadedMaterials(userId);

                    } else {
                        Log.d(TAG, "User document does not exist for " + userId + ". Redirecting to setup.");
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Please complete your profile first.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getContext(), ProfileSetupActivity.class));
                        }
                        setLoading(false); // Hide progress bar even if redirecting
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) {
                        Log.d(TAG, "Fragment not attached, skipping Toast for profile data error.");
                        return;
                    }
                    setLoading(false);
                    Log.e(TAG, "Error loading user profile data: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error loading profile data.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates location sharing preference in Firestore.
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
     * Creates a Chip view for displaying a user's selected subject.
     * @param subjectName The name of the subject.
     * @return Configured Chip view.
     */
    private Chip createSubjectChip(String subjectName) {
        Chip chip = new Chip(requireContext());
        chip.setText(subjectName);
        chip.setCheckable(false);
        chip.setClickable(false);
        // Using ContextCompat.getColor for compatibility and theme attributes
        chip.setChipBackgroundColor(ContextCompat.getColorStateList(requireContext(), R.color.colorSecondaryContainer));
        chip.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.colorOnSecondaryContainer));
        return chip;
    }

    /**
     * Fetches materials uploaded by the current user from Firestore.
     * @param userId The UID of the current user.
     */
    private void loadMyUploadedMaterials(String userId) {
        db.collection("studyMaterials")
                .whereEqualTo("uploaderUid", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || getContext() == null) {
                        Log.d(TAG, "Fragment not attached, skipping UI update for uploaded materials.");
                        return;
                    }

                    myMaterialsList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            StudyMaterial material = doc.toObject(StudyMaterial.class);
                            if (material != null) {
                                material.setId(doc.getId());
                                myMaterialsList.add(material);
                            }
                        }
                        myMaterialsAdapter.notifyDataSetChanged();
                        textNoUploadedMaterials.setVisibility(View.GONE);
                    } else {
                        myMaterialsList.clear();
                        myMaterialsAdapter.notifyDataSetChanged();
                        textNoUploadedMaterials.setVisibility(View.VISIBLE);
                    }
                    setLoading(false); // Make sure loading is turned off after materials too
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) {
                        Log.d(TAG, "Fragment not attached, skipping Toast for uploaded materials error.");
                        return;
                    }
                    setLoading(false); // Hide progress bar on failure
                    Log.e(TAG, "Error loading uploaded materials: " + e.getMessage(), e);
                    textNoUploadedMaterials.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Failed to load your uploaded materials.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Manages the visibility of the ProgressBar and enables/disables UI elements.
     * @param isLoading True to show progress bar and disable UI, false otherwise.
     */
    private void setLoading(boolean isLoading) {
        if (!isAdded() || getContext() == null) return;

        progressBarProfile.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        // Disable/enable key UI elements
        imgProfilePicture.setEnabled(!isLoading);
        btnEditProfile.setEnabled(!isLoading);
        btnEditPersonalInfo.setEnabled(!isLoading);
        btnManageMySubjects.setEnabled(!isLoading);
        switchLocationSharing.setEnabled(!isLoading);
        switchNotifications.setEnabled(!isLoading);
        btnAdminPanel.setEnabled(!isLoading); // Control Admin Panel button
        btnChangePassword.setEnabled(!isLoading);
        btnDeleteAccount.setEnabled(!isLoading);
        btnUploadMaterial.setEnabled(!isLoading);
        btnLogout.setEnabled(!isLoading);
        recyclerMyMaterials.setEnabled(!isLoading);
        // Add more elements as needed
    }
}
