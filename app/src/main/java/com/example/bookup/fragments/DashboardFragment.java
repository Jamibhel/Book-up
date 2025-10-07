package com.example.bookup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.HomePageActivity;
import com.example.bookup.ProfileSetupActivity;
import com.example.bookup.R;
import com.example.bookup.SignInActivity;
import com.example.bookup.SubjectAdapter;
import com.example.bookup.SubjectSelectionActivity;
import com.example.bookup.StudyMaterialOverviewAdapter; // New adapter import
import com.example.bookup.TutorOverviewAdapter;     // New adapter import
import com.example.bookup.models.StudyMaterialOverview; // New model import
import com.example.bookup.models.TutorOverview;     // New model import
import com.google.android.material.button.MaterialButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    // UI elements
    private TextView textWelcomeMessage;
    private MaterialButton buttonFindTutorDashboard;
    private MaterialButton buttonManageSubjectsDashboard;

    // New: RecyclerViews for recommendations and materials showcase
    private RecyclerView recyclerTopTutors;
    private RecyclerView recyclerPicksForYou;
    private RecyclerView recyclerStudyMaterials;

    // Adapters for new recommendation RecyclerViews
    private TutorOverviewAdapter topTutorsAdapter;
    private TutorOverviewAdapter picksForYouAdapter;
    private StudyMaterialOverviewAdapter studyMaterialsAdapter; // New adapter

    // Dummy data lists for new recommendation RecyclerViews
    private List<TutorOverview> topTutorsList = new ArrayList<>();
    private List<TutorOverview> picksForYouList = new ArrayList<>();
    private List<StudyMaterialOverview> studyMaterialsList = new ArrayList<>(); // New list for materials

    // Existing: RecyclerViews for user's subjects
    private RecyclerView recyclerLearningSubjects;
    private RecyclerView recyclerTutoringSubjects;
    private TextView textNoLearningSubjects;
    private TextView textNoTutoringSubjects;

    // Existing: Adapters for user's subjects
    private SubjectAdapter learningSubjectAdapter;
    private SubjectAdapter tutoringSubjectAdapter;

    // Existing: Data lists for user's subjects
    private List<Map<String, Object>> learningSubjectsList = new ArrayList<>();
    private List<Map<String, Object>> tutoringSubjectsList = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase instances here, they persist through Fragment lifecycle
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // --- Initialize UI elements ---
        initViews(view);

        // --- Setup RecyclerViews ---
        setupRecyclerViews();

        // --- Load Dummy Data for recommendations and materials ---
        loadDummyData();

        // --- Set up click listeners for buttons ---
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load user data (name and subjects) every time the fragment becomes visible
        loadUserData();
    }

    /**
     * Initializes all UI components from the inflated view.
     * @param view The inflated view of the fragment.
     */
    private void initViews(View view) {
        textWelcomeMessage = view.findViewById(R.id.text_welcome_message);
        buttonFindTutorDashboard = view.findViewById(R.id.button_find_tutor_dashboard);
        buttonManageSubjectsDashboard = view.findViewById(R.id.button_manage_subjects_dashboard);

        recyclerTopTutors = view.findViewById(R.id.recycler_top_tutors);
        recyclerPicksForYou = view.findViewById(R.id.recycler_picks_for_you);
        recyclerStudyMaterials = view.findViewById(R.id.recycler_study_materials); // New RecyclerView

        recyclerLearningSubjects = view.findViewById(R.id.recycler_learning_subjects);
        recyclerTutoringSubjects = view.findViewById(R.id.recycler_tutoring_subjects);
        textNoLearningSubjects = view.findViewById(R.id.text_no_learning_subjects);
        textNoTutoringSubjects = view.findViewById(R.id.text_no_tutoring_subjects);
    }

    /**
     * Configures and sets up all RecyclerViews with their adapters and layout managers.
     */
    private void setupRecyclerViews() {
        // Top Tutors RecyclerView (Horizontal)
        recyclerTopTutors.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        topTutorsAdapter = new TutorOverviewAdapter(topTutorsList);
        recyclerTopTutors.setAdapter(topTutorsAdapter);
        topTutorsAdapter.setOnItemClickListener(tutor -> {
            Toast.makeText(getContext(), "Clicked Top Tutor: " + tutor.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Future: Navigate to TutorProfileActivity/Fragment for 'tutor.getUid()'
        });

        // Picks For You RecyclerView (Horizontal)
        recyclerPicksForYou.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        picksForYouAdapter = new TutorOverviewAdapter(picksForYouList);
        recyclerPicksForYou.setAdapter(picksForYouAdapter);
        picksForYouAdapter.setOnItemClickListener(tutor -> {
            Toast.makeText(getContext(), "Clicked Pick For You: " + tutor.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Future: Navigate to TutorProfileActivity/Fragment for 'tutor.getUid()'
        });

        // Study Materials Showcase RecyclerView (Horizontal)
        recyclerStudyMaterials.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        studyMaterialsAdapter = new StudyMaterialOverviewAdapter(studyMaterialsList);
        recyclerStudyMaterials.setAdapter(studyMaterialsAdapter);
        studyMaterialsAdapter.setOnItemClickListener(material -> {
            Toast.makeText(getContext(), "Clicked Material: " + material.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Future: Navigate to MaterialDetailsActivity/Fragment for 'material.getMaterialId()'
        });

        // Learning Subjects RecyclerView (Vertical)
        recyclerLearningSubjects.setLayoutManager(new LinearLayoutManager(getContext()));
        learningSubjectAdapter = new SubjectAdapter(learningSubjectsList);
        recyclerLearningSubjects.setAdapter(learningSubjectAdapter);

        // Tutoring Subjects RecyclerView (Vertical)
        recyclerTutoringSubjects.setLayoutManager(new LinearLayoutManager(getContext()));
        tutoringSubjectAdapter = new SubjectAdapter(tutoringSubjectsList);
        recyclerTutoringSubjects.setAdapter(tutoringSubjectAdapter);
    }

    /**
     * Sets up click listeners for action buttons on the Dashboard.
     */
    private void setupClickListeners() {
        buttonFindTutorDashboard.setOnClickListener(v -> {
            // Programmatically select the 'Find Tutor' tab in HomepageActivity's BottomNav
            if (getActivity() instanceof HomePageActivity) {
                ((HomePageActivity) getActivity()).selectBottomNavItem(R.id.nav_find_tutor);
            }
        });

        buttonManageSubjectsDashboard.setOnClickListener(v -> {
            // Navigate to SubjectSelectionActivity to manage subjects
            Intent intent = new Intent(getContext(), SubjectSelectionActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Loads dummy data for recommendation sections (Top Tutors, Picks for You, Study Materials).
     * This data will later be replaced by actual Firestore queries.
     */
    private void loadDummyData() {
        // Clear previous dummy data
        topTutorsList.clear();
        picksForYouList.clear();
        studyMaterialsList.clear();

        // Add dummy data for Top Tutors
        topTutorsList.add(new TutorOverview("Dr. Adaobi Nwachukwu", "", 4.9f, "Organic Chemistry", "tutor_uid_1"));
        topTutorsList.add(new TutorOverview("Prof. Emeka Okafor", "", 4.8f, "Linear Algebra", "tutor_uid_2"));
        topTutorsList.add(new TutorOverview("Miss Fatima Bello", "", 4.7f, "African History", "tutor_uid_3"));
        topTutorsList.add(new TutorOverview("Mr. Tunde Adewale", "", 4.9f, "Computer Science", "tutor_uid_4"));
        topTutorsAdapter.notifyDataSetChanged();

        // Add dummy data for Picks for You
        picksForYouList.add(new TutorOverview("Bisi Adeyemi", "", 4.5f, "Thermodynamics", "tutor_uid_5"));
        picksForYouList.add(new TutorOverview("Chidi Obiora", "", 4.6f, "Data Structures", "tutor_uid_6"));
        picksForYouList.add(new TutorOverview("Aisha Abubakar", "", 4.4f, "Microeconomics", "tutor_uid_7"));
        picksForYouList.add(new TutorOverview("Obioma Okoro", "", 4.7f, "Art History", "tutor_uid_8"));
        picksForYouAdapter.notifyDataSetChanged();

        // Add dummy data for Study Materials Showcase
        studyMaterialsList.add(new StudyMaterialOverview("CompSci 101 Past Q", "CS101", "", "mat_id_1"));
        studyMaterialsList.add(new StudyMaterialOverview("Calc II Study Guide", "MATH202", "", "mat_id_2"));
        studyMaterialsList.add(new StudyMaterialOverview("Orgo Lab Manual", "CHEM305", "", "mat_id_3"));
        studyMaterialsList.add(new StudyMaterialOverview("Hist 300 Essay Tips", "HIST300", "", "mat_id_4"));
        studyMaterialsAdapter.notifyDataSetChanged();
    }


    /**
     * Loads the authenticated user's profile data and subjects from Firestore.
     * Updates the UI accordingly.
     */
    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // This case should ideally be handled by HomepageActivity's initial check
            startActivity(new Intent(getContext(), SignInActivity.class));
            requireActivity().finish(); // Finish hosting activity
            return;
        }

        String userId = currentUser.getUid();

        // Update welcome message with user's display name if available from Auth
        String currentUserName = currentUser.getDisplayName();
        if (currentUserName != null && !currentUserName.isEmpty()) {
            // Use only the first name for a more friendly greeting
            textWelcomeMessage.setText(String.format("Hello, %s!", currentUserName.split(" ")[0]));
        } else {
            textWelcomeMessage.setText("Hello, Book Up user!"); // Generic fallback
        }


        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> { // Using lambda for brevity
                    if (documentSnapshot.exists()) {
                        // Refine welcome message if we have specific first name from profile
                        String firstName = documentSnapshot.getString("firstName");
                        if (firstName != null && !firstName.isEmpty()) {
                            textWelcomeMessage.setText(String.format("Hello, %s!", firstName));
                        }

                        // Load subjects
                        List<Map<String, Object>> allSubjects = (List<Map<String, Object>>) documentSnapshot.get("subjects");
                        if (allSubjects != null && !allSubjects.isEmpty()) {
                            learningSubjectsList.clear();
                            tutoringSubjectsList.clear();

                            for (Map<String, Object> subject : allSubjects) {
                                String role = (String) subject.get("role");
                                if ("Learning".equals(role)) {
                                    learningSubjectsList.add(subject);
                                } else if ("Tutoring".equals(role)) {
                                    tutoringSubjectsList.add(subject);
                                }
                            }

                            learningSubjectAdapter.notifyDataSetChanged();
                            tutoringSubjectAdapter.notifyDataSetChanged();

                            // Show/hide "No subjects" text based on list content
                            textNoLearningSubjects.setVisibility(learningSubjectsList.isEmpty() ? View.VISIBLE : View.GONE);
                            textNoTutoringSubjects.setVisibility(tutoringSubjectsList.isEmpty() ? View.VISIBLE : View.GONE);

                        } else {
                            Log.d(TAG, "No subjects found for user " + userId);
                            textNoLearningSubjects.setVisibility(View.VISIBLE);
                            textNoTutoringSubjects.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.d(TAG, "User document does not exist for " + userId);
                        Toast.makeText(getContext(), "Profile not fully set up. Please complete your profile.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getContext(), ProfileSetupActivity.class));
                        requireActivity().finish(); // Finish hosting activity if user MUST complete profile
                    }
                })
                .addOnFailureListener(e -> { // Using lambda for brevity
                    Log.e(TAG, "Error loading user data: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error loading profile data.", Toast.LENGTH_SHORT).show();
                });
    }
}
