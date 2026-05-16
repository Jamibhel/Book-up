package com.example.bookup.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.activities.AdminPanelActivity;
import com.example.bookup.activities.ChangePasswordActivity;
import com.example.bookup.activities.DeleteAccountActivity;
import com.example.bookup.activities.HomePageActivity;
import com.example.bookup.activities.MaterialDetailsActivity;
import com.example.bookup.activities.ProfileEditActivity;
import com.example.bookup.activities.SignInActivity;
import com.example.bookup.activities.SubjectSelectionActivity;
import com.example.bookup.activities.UploadMaterialActivity;
import com.example.bookup.adapters.AvailabilityAdapter;
import com.example.bookup.adapters.ReviewAdapter;
import com.example.bookup.adapters.StudyMaterialOverviewAdapter;
import com.example.bookup.databinding.DialogEditAvailabilityBinding;
import com.example.bookup.databinding.DialogEditLocationBinding;
import com.example.bookup.databinding.FragmentProfileBinding;
import com.example.bookup.models.Availability;
import com.example.bookup.models.Review;
import com.example.bookup.models.StudyMaterial;
import com.example.bookup.models.User;
import com.example.bookup.utils.FirebaseErrorHandler;
import com.example.bookup.utils.NetworkConnectivityManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";

    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private User currentUser;
    
    private ReviewAdapter reviewAdapter;
    private StudyMaterialOverviewAdapter myMaterialsAdapter;
    private List<StudyMaterial> myMaterialsList = new ArrayList<>();
    
    private NetworkConnectivityManager connectivityManager;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        connectivityManager = new NetworkConnectivityManager(getContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerViews();
        setupClickListeners();
        setupPreferenceToggles();
        loadUserData();
    }

    private void setupRecyclerViews() {
        if (binding == null) return;
        reviewAdapter = new ReviewAdapter();
        binding.recyclerMyReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerMyReviews.setAdapter(reviewAdapter);

        myMaterialsAdapter = new StudyMaterialOverviewAdapter(myMaterialsList);
        binding.recyclerMyMaterials.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerMyMaterials.setAdapter(myMaterialsAdapter);
        myMaterialsAdapter.setOnMaterialClickListener(material -> {
            Intent intent = new Intent(getContext(), MaterialDetailsActivity.class);
            intent.putExtra(MaterialDetailsActivity.EXTRA_MATERIAL, material);
            startActivity(intent);
        });
    }

    private void setupClickListeners() {
        if (binding == null) return;
        binding.btnChangePhoto.setOnClickListener(v -> startActivity(new Intent(getContext(), ProfileEditActivity.class)));
        binding.btnEditBio.setOnClickListener(v -> {
            if (currentUser != null) showBioEditDialog();
            else Toast.makeText(getContext(), "Profile still loading...", Toast.LENGTH_SHORT).show();
        });
        binding.btnManageMySubjects.setOnClickListener(v -> startActivity(new Intent(getContext(), SubjectSelectionActivity.class)));
        binding.btnChangePassword.setOnClickListener(v -> startActivity(new Intent(getContext(), ChangePasswordActivity.class)));
        binding.btnAdminPanel.setOnClickListener(v -> startActivity(new Intent(getContext(), AdminPanelActivity.class)));
        binding.btnEditAvailability.setOnClickListener(v -> {
            if (currentUser != null) showAvailabilityDialog();
            else Toast.makeText(getContext(), "Profile still loading...", Toast.LENGTH_SHORT).show();
        });
        binding.btnEditLocationStatus.setOnClickListener(v -> {
            if (currentUser != null) showLocationDialog();
            else Toast.makeText(getContext(), "Profile still loading...", Toast.LENGTH_SHORT).show();
        });
        binding.btnUploadMaterial.setOnClickListener(v -> startActivity(new Intent(getContext(), UploadMaterialActivity.class)));
        binding.btnDeleteAccount.setOnClickListener(v -> startActivity(new Intent(getContext(), DeleteAccountActivity.class)));
        binding.btnMyBookings.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(requireView()).navigate(R.id.action_profile_to_bookings));
        
        binding.textHourlyRate.setOnClickListener(v -> {
            if (currentUser != null) showHourlyRateDialog();
            else Toast.makeText(getContext(), "Profile still loading...", Toast.LENGTH_SHORT).show();
        });

        binding.btnLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Sign Out", (d, w) -> {
                        // Google Sign Out
                        try {
                            com.google.android.gms.auth.api.signin.GoogleSignInOptions gso = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(getString(R.string.default_web_client_id))
                                    .requestEmail()
                                    .build();
                            com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireContext(), gso).signOut();
                        } catch (Exception e) {
                            Log.e(TAG, "Google Sign Out failed", e);
                        }

                        mAuth.signOut();
                        startActivity(new Intent(getContext(), SignInActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupPreferenceToggles() {
        if (binding == null) return;
        binding.switchNotifications.setChecked(sharedPreferences.getBoolean(PREF_NOTIFICATIONS_ENABLED, true));
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(PREF_NOTIFICATIONS_ENABLED, isChecked).apply();
            if (isChecked) {
                FirebaseMessaging.getInstance().subscribeToTopic("global_notifications");
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("global_notifications");
            }
        });

        binding.switchLocationSharing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentUser != null) {
                db.collection("users").document(currentUser.getId()).update("shareLocation", isChecked);
            }
        });
    }

    private void loadUserData() {
        FirebaseUser fu = mAuth.getCurrentUser();
        if (fu == null) return;
        
        if (binding != null) binding.progressBarProfile.setVisibility(View.VISIBLE);
        db.collection("users").document(fu.getUid()).get().addOnSuccessListener(doc -> {
            if (isAdded() && binding != null) {
                binding.progressBarProfile.setVisibility(View.GONE);
                currentUser = doc.toObject(User.class);
                if (currentUser != null) {
                    updateUI(doc);
                    loadMyUploadedMaterials(fu.getUid());
                    if ("tutor".equals(currentUser.getRole())) {
                        loadMyReviews(fu.getUid());
                    }
                }
            }
        }).addOnFailureListener(e -> {
            if (isAdded() && binding != null) {
                binding.progressBarProfile.setVisibility(View.GONE);
                FirebaseErrorHandler.handleError(e, binding.getRoot());
            }
        });
    }

    private void updateUI(DocumentSnapshot doc) {
        if (binding == null || currentUser == null) return;
        binding.textFullName.setText(currentUser.getDisplayName());
        binding.textEmail.setText(currentUser.getEmail());
        binding.textUserRole.setText(currentUser.getRole().toUpperCase(Locale.ROOT));
        
        Glide.with(this)
             .load(currentUser.getPhotoUrl())
             .placeholder(R.drawable.ic_profile_placeholder)
             .circleCrop()
             .into(binding.imgProfilePicture);

        binding.textPhoneNumber.setText(doc.getString("phoneNumber") != null ? doc.getString("phoneNumber") : "N/A");
        binding.textGender.setText(doc.getString("gender") != null ? doc.getString("gender") : "N/A");

        if (currentUser.getBio() != null && !currentUser.getBio().isEmpty()) {
            binding.textUserBio.setText(currentUser.getBio());
            binding.textUserBio.setVisibility(View.VISIBLE);
            binding.textNoBio.setVisibility(View.GONE);
        } else {
            binding.textUserBio.setVisibility(View.GONE);
            binding.textNoBio.setVisibility(View.VISIBLE);
        }

        boolean isTutor = "tutor".equals(currentUser.getRole());
        binding.cardAvailability.setVisibility(isTutor ? View.VISIBLE : View.GONE);
        binding.cardMyReviews.setVisibility(isTutor ? View.VISIBLE : View.GONE);
        binding.layoutUserRating.setVisibility(isTutor ? View.VISIBLE : View.GONE);
        binding.cardLocationStatus.setVisibility(isTutor ? View.VISIBLE : View.GONE);
        binding.layoutLocationSharing.setVisibility(isTutor ? View.VISIBLE : View.GONE);
        binding.textHourlyRate.setVisibility(isTutor ? View.VISIBLE : View.GONE);
        
        if (isTutor) {
            binding.textUserRating.setText(String.format(Locale.getDefault(), "%.1f (%d reviews)", currentUser.getRating(), currentUser.getReviewCount()));
            binding.textLocationName.setText(currentUser.getLocationName() != null ? currentUser.getLocationName() : "No location set");
            binding.textHourlyRate.setText(String.format(Locale.getDefault(), "₦%.0f / hr", currentUser.getHourlyRate()));

            String pref = currentUser.getWorkPreference();
            String prefText = "Offers Online & In-Person";
            if ("online".equals(pref)) prefText = "Online Only";
            else if ("in_person".equals(pref)) prefText = "In-Person Only";
            binding.textWorkPreference.setText(prefText);
            
            Boolean shareLoc = doc.getBoolean("shareLocation");
            binding.switchLocationSharing.setChecked(shareLoc != null && shareLoc);
        }

        binding.btnAdminPanel.setVisibility(currentUser.isAdmin() ? View.VISIBLE : View.GONE);

        binding.chipGroupUserSubjects.removeAllViews();
        List<String> subjects = currentUser.getTutoringSubjects();
        if (subjects != null && !subjects.isEmpty()) {
            for (String s : subjects) {
                Chip chip = new Chip(requireContext());
                chip.setText(s);
                chip.setChipBackgroundColor(ContextCompat.getColorStateList(requireContext(), R.color.primary_faded));
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
                binding.chipGroupUserSubjects.addView(chip);
            }
        }

        if (currentUser.getAvailability() != null && !currentUser.getAvailability().isEmpty()) {
            binding.recyclerAvailabilityPreview.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerAvailabilityPreview.setAdapter(new AvailabilityAdapter(currentUser.getAvailability(), false));
            binding.textAvailabilitySummary.setVisibility(View.GONE);
        } else {
            binding.textAvailabilitySummary.setVisibility(View.VISIBLE);
        }
    }

    private void loadMyUploadedMaterials(String userId) {
        db.collection("studyMaterials")
                .whereEqualTo("uploaderUid", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded() || binding == null) return;
                    myMaterialsList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        StudyMaterial material = doc.toObject(StudyMaterial.class);
                        if (material != null) {
                            material.setId(doc.getId());
                            myMaterialsList.add(material);
                        }
                    }
                    myMaterialsAdapter.notifyDataSetChanged();
                    binding.textNoUploadedMaterials.setVisibility(myMaterialsList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void loadMyReviews(String tutorId) {
        db.collection("reviews")
                .whereEqualTo("tutorId", tutorId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null && isAdded() && binding != null) {
                        List<Review> reviews = value.toObjects(Review.class);
                        reviewAdapter.setReviews(reviews);
                        binding.textNoMyReviews.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void showLocationDialog() {
        if (currentUser == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        DialogEditLocationBinding lBinding = DialogEditLocationBinding.inflate(getLayoutInflater());
        dialog.setContentView(lBinding.getRoot());

        lBinding.editLocationName.setText(currentUser.getLocationName());
        
        String pref = currentUser.getWorkPreference();
        if ("online".equals(pref)) lBinding.chipOnline.setChecked(true);
        else if ("in_person".equals(pref)) lBinding.chipInPerson.setChecked(true);
        else lBinding.chipBoth.setChecked(true);

        lBinding.btnSaveLocation.setOnClickListener(v -> {
            String newLoc = lBinding.editLocationName.getText() != null ? lBinding.editLocationName.getText().toString().trim() : "";
            String newPref = "both";
            if (lBinding.chipOnline.isChecked()) newPref = "online";
            else if (lBinding.chipInPerson.isChecked()) newPref = "in_person";

            db.collection("users").document(currentUser.getId()).update("locationName", newLoc, "workPreference", newPref)
                    .addOnSuccessListener(aVoid -> {
                        loadUserData();
                        dialog.dismiss();
                    });
        });
        dialog.show();
    }

    private void showHourlyRateDialog() {
        if (currentUser == null) return;
        android.widget.EditText input = new android.widget.EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf((int)currentUser.getHourlyRate()));
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Set Hourly Rate (₦)")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        db.collection("users").document(currentUser.getId()).update("hourlyRate", Double.parseDouble(val))
                                .addOnSuccessListener(v -> loadUserData());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAvailabilityDialog() {
        if (currentUser == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        DialogEditAvailabilityBinding dialogBinding = DialogEditAvailabilityBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        List<Availability> list = currentUser.getAvailability();
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            for (String day : days) list.add(new Availability(day, true, "09:00", "17:00"));
        }

        dialogBinding.recyclerAvailabilityEdit.setLayoutManager(new LinearLayoutManager(getContext()));
        dialogBinding.recyclerAvailabilityEdit.setAdapter(new AvailabilityAdapter(list, true));

        List<Availability> finalList = list;
        dialogBinding.btnSaveAvailability.setOnClickListener(v -> {
            db.collection("users").document(currentUser.getId()).update("availability", finalList)
                    .addOnSuccessListener(aVoid -> {
                        loadUserData();
                        dialog.dismiss();
                    });
        });
        dialog.show();
    }

    private void showBioEditDialog() {
        if (currentUser == null) return;
        android.widget.EditText input = new android.widget.EditText(getContext());
        input.setText(currentUser.getBio());
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Edit About Me").setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String bio = input.getText().toString().trim();
                    db.collection("users").document(currentUser.getId()).update("bio", bio).addOnSuccessListener(v -> loadUserData());
                }).setNegativeButton("Cancel", null).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (connectivityManager != null) {
            connectivityManager.startMonitoring((isConnected, status) -> {});
        }
        loadUserData();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (connectivityManager != null) {
            connectivityManager.stopMonitoring();
        }
    }

    @Override
    public void onDestroyView() { 
        super.onDestroyView(); 
        binding = null; 
    }
}
