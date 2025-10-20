package com.example.bookup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.MaterialDetailsActivity;
import com.example.bookup.NewsFeedAdapter;
import com.example.bookup.R;
import com.example.bookup.SubjectSelectionActivity;
import com.example.bookup.StudyMaterialOverviewAdapter;
import com.example.bookup.TutorOverviewAdapter;
import com.example.bookup.models.NewsItem;
import com.example.bookup.models.StudyMaterial;
import com.example.bookup.models.Tutor;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    // UI Elements
    private TextView textWelcomeTitle;
    private TextView textMotivationQuote;
    private MaterialButton btnPostRequest;
    private MaterialButton btnManageSubjects;
    private ProgressBar progressBarDashboard;
    private LinearLayout layoutNewsFeedEmpty; // Empty state for news

    // News Feed
    private RecyclerView recyclerNewsFeed;
    private NewsFeedAdapter newsFeedAdapter;
    private List<NewsItem> newsList;

    // Picks For You Tutors
    private TextView textPicksForYouTitle; // NEW: To conditionally show/hide
    private RecyclerView recyclerPicksForYou;
    private TutorOverviewAdapter picksForYouAdapter;
    private List<Tutor> picksForYouTutorsList;
    private LinearLayout layoutPicksForYouEmpty; // NEW: Empty state for tutors

    // Top-Rated Tutors
    private RecyclerView recyclerTopTutors;
    private TutorOverviewAdapter topTutorsAdapter;
    private List<Tutor> topTutorsList;

    // Study Materials
    private TextView textStudyMaterialsTitle; // NEW: To conditionally show/hide
    private RecyclerView recyclerStudyMaterials;
    private StudyMaterialOverviewAdapter studyMaterialsAdapter;
    private List<StudyMaterial> studyMaterialsList;
    private LinearLayout layoutStudyMaterialsEmpty; // NEW: Empty state for materials

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // User data for personalization
    private List<String> userSelectedSubjects = new ArrayList<>(); // NEW: To store user's subjects

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        newsList = new ArrayList<>();
        picksForYouTutorsList = new ArrayList<>();
        topTutorsList = new ArrayList<>();
        studyMaterialsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        initViews(view);
        setupNewsFeedRecyclerView();
        setupPicksForYouRecyclerView();
        setupTopTutorsRecyclerView();
        setupStudyMaterialsRecyclerView();
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData(); // Loads user display data, then triggers subject fetch
        fetchNewsFeedFromFirestore();
        // Tutors and Study materials will be fetched after user subjects are known
    }

    private void initViews(View view) {
        textWelcomeTitle = view.findViewById(R.id.text_welcome_title);
        textMotivationQuote = view.findViewById(R.id.text_motivation_quote);
        btnPostRequest = view.findViewById(R.id.btn_dashboard_post_request);
        btnManageSubjects = view.findViewById(R.id.btn_dashboard_manage_subjects);
        progressBarDashboard = view.findViewById(R.id.progress_bar_dashboard);

        // News Feed
        recyclerNewsFeed = view.findViewById(R.id.recycler_news_feed);
        layoutNewsFeedEmpty = view.findViewById(R.id.layout_news_feed_empty);

        // Picks For You Tutors
        textPicksForYouTitle = view.findViewById(R.id.text_picks_for_you_title); // NEW ID
        recyclerPicksForYou = view.findViewById(R.id.recycler_picks_for_you);
        layoutPicksForYouEmpty = view.findViewById(R.id.layout_picks_for_you_empty); // NEW ID

        // Top-Rated Tutors
        recyclerTopTutors = view.findViewById(R.id.recycler_top_tutors);

        // Study Materials
        textStudyMaterialsTitle = view.findViewById(R.id.text_study_materials_title); // NEW ID
        recyclerStudyMaterials = view.findViewById(R.id.recycler_study_materials);
        layoutStudyMaterialsEmpty = view.findViewById(R.id.layout_study_materials_empty); // NEW ID
    }

    private void setupNewsFeedRecyclerView() {
        newsFeedAdapter = new NewsFeedAdapter(newsList);
        recyclerNewsFeed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerNewsFeed.setAdapter(newsFeedAdapter);

        newsFeedAdapter.setOnNewsItemClickListener(item -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Clicked on News: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
            // TODO: Implement navigation to a NewsDetailActivity or web link
        });
    }

    private void setupPicksForYouRecyclerView() {
        picksForYouAdapter = new TutorOverviewAdapter(picksForYouTutorsList);
        recyclerPicksForYou.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerPicksForYou.setAdapter(picksForYouAdapter);

        picksForYouAdapter.setOnTutorClickListener(tutor -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Clicked on Pick: " + tutor.getName(), Toast.LENGTH_SHORT).show();
            }
            // TODO: Navigate to TutorProfileActivity (Pass tutor.getUid())
        });
    }

    private void setupTopTutorsRecyclerView() {
        topTutorsAdapter = new TutorOverviewAdapter(topTutorsList);
        recyclerTopTutors.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerTopTutors.setAdapter(topTutorsAdapter);

        topTutorsAdapter.setOnTutorClickListener(tutor -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Clicked on Top Tutor: " + tutor.getName(), Toast.LENGTH_SHORT).show();
            }
            // TODO: Navigate to TutorProfileActivity (Pass tutor.getUid())
        });
    }

    private void setupStudyMaterialsRecyclerView() {
        studyMaterialsAdapter = new StudyMaterialOverviewAdapter(studyMaterialsList);
        recyclerStudyMaterials.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerStudyMaterials.setAdapter(studyMaterialsAdapter);

        studyMaterialsAdapter.setOnMaterialClickListener(material -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), MaterialDetailsActivity.class);
                intent.putExtra(MaterialDetailsActivity.EXTRA_MATERIAL, material);
                startActivity(intent);
            } else {
                Log.e(TAG, "Context is null, cannot launch MaterialDetailsActivity.");
            }
        });
    }

    private void setupClickListeners() {
        btnPostRequest.setOnClickListener(v -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Post Help Request (Coming Soon!)", Toast.LENGTH_SHORT).show();
            }
            // Example: Navigate to RequestsFragment directly
            // if (getActivity() instanceof HomePageActivity) {
            //     ((HomePageActivity) getActivity()).selectBottomNavItem(R.id.nav_requests);
            // }
        });

        btnManageSubjects.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), SubjectSelectionActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserData() {
        if (currentUser != null) {
            String userName = "User";
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                userName = currentUser.getDisplayName().split(" ")[0];
            } else if (currentUser.getEmail() != null) {
                userName = currentUser.getEmail().split("@")[0];
            }
            textWelcomeTitle.setText(String.format("Hello, %s!", userName));
            fetchUserSubjects(currentUser.getUid()); // Fetch subjects after user data is loaded
        } else {
            textWelcomeTitle.setText("Hello!");
            textMotivationQuote.setText("“Education is the most powerful weapon which you can use to change the world.” - Nelson Mandela");
            // If no user, show generic recommendations
            fetchTutorsFromFirestore(new ArrayList<>());
            fetchStudyMaterialsFromFirestore(new ArrayList<>());
        }
    }

    // NEW: Fetch user's selected subjects for personalization
    private void fetchUserSubjects(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getContext() == null) return;

                    userSelectedSubjects.clear();
                    Object subjectsObject = documentSnapshot.get("subjects");
                    if (subjectsObject instanceof Map) {
                        Map<String, Boolean> subjectsMap = (Map<String, Boolean>) subjectsObject;
                        for (Map.Entry<String, Boolean> entry : subjectsMap.entrySet()) {
                            if (entry.getValue() != null && entry.getValue()) {
                                userSelectedSubjects.add(entry.getKey());
                            }
                        }
                    } else if (subjectsObject instanceof List) { // Fallback for old List<String> format
                        List<?> rawSubjectsList = (List<?>) subjectsObject;
                        for (Object item : rawSubjectsList) {
                            if (item instanceof String) {
                                userSelectedSubjects.add((String) item);
                            }
                        }
                    }

                    Log.d(TAG, "User subjects fetched: " + userSelectedSubjects.size());
                    // Now that subjects are known, fetch personalized data
                    fetchTutorsFromFirestore(userSelectedSubjects);
                    fetchStudyMaterialsFromFirestore(userSelectedSubjects);

                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    Log.e(TAG, "Error fetching user subjects: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Failed to load user subjects.", Toast.LENGTH_SHORT).show();
                    // Fallback to generic recommendations if subjects can't be fetched
                    fetchTutorsFromFirestore(new ArrayList<>());
                    fetchStudyMaterialsFromFirestore(new ArrayList<>());
                });
    }


    // Fetches news feed data from Firestore
    private void fetchNewsFeedFromFirestore() {
        setLoading(true);

        db.collection("newsFeed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) {
                        Log.d(TAG, "Fragment not attached, skipping Toast/UI update for news feed.");
                        return;
                    }

                    newsList.clear();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            NewsItem newsItem = document.toObject(NewsItem.class);
                            newsList.add(newsItem);
                        }
                        Log.d(TAG, "News fetched from Firestore successfully: " + newsList.size());
                    } else {
                        Log.w(TAG, "Error getting news from Firestore: ", task.getException());
                        Toast.makeText(getContext(), "Failed to load news.", Toast.LENGTH_SHORT).show();
                    }

                    newsFeedAdapter.notifyDataSetChanged();
                    updateNewsFeedUI(newsList.isEmpty());
                    setLoading(false); // Hide overall dashboard loading
                });
    }

    private void updateNewsFeedUI(boolean isEmpty) {
        if (isEmpty) {
            recyclerNewsFeed.setVisibility(View.GONE);
            layoutNewsFeedEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerNewsFeed.setVisibility(View.VISIBLE);
            layoutNewsFeedEmpty.setVisibility(View.GONE);
        }
    }


    // UPDATED: Fetches tutor data, now with optional subject filtering
    private void fetchTutorsFromFirestore(List<String> subjects) {
        picksForYouTutorsList.clear();
        topTutorsList.clear();

        Query query = db.collection("tutors");

        if (!subjects.isEmpty()) {
            // Prioritize tutors who teach at least one of the user's subjects
            // Note: Firestore 'array-contains-any' can only check up to 10 items.
            // If user has more than 10 subjects, you might need a different strategy.
            // For now, if subjects > 10, it will only use the first 10.
            List<String> querySubjects = subjects.size() > 10 ? subjects.subList(0, 10) : subjects;
            query = query.whereArrayContainsAny("subjects", querySubjects);
        }

        query.limit(10) // Limit the number of tutors fetched for performance
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) {
                        Log.d(TAG, "Fragment not attached, skipping Toast/UI update for tutors.");
                        return;
                    }

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Tutor tutor = document.toObject(Tutor.class);
                            tutor.setUid(document.getId());
                            picksForYouTutorsList.add(tutor);
                            // For top-rated, you might filter/sort picksForYouTutorsList by rating or have a separate query
                            topTutorsList.add(tutor); // For simplicity, adding to both for now
                        }
                        picksForYouAdapter.notifyDataSetChanged();
                        topTutorsAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Tutors fetched from Firestore successfully: " + picksForYouTutorsList.size());
                    } else {
                        Log.w(TAG, "Error getting tutors from Firestore: ", task.getException());
                        Toast.makeText(getContext(), "Failed to load tutors.", Toast.LENGTH_SHORT).show();
                    }
                    updatePicksForYouUI(picksForYouTutorsList.isEmpty());
                });
    }

    private void updatePicksForYouUI(boolean isEmpty) {
        if (isEmpty) {
            recyclerPicksForYou.setVisibility(View.GONE);
            layoutPicksForYouEmpty.setVisibility(View.VISIBLE);
            textPicksForYouTitle.setVisibility(View.GONE); // Hide title if empty
        } else {
            recyclerPicksForYou.setVisibility(View.VISIBLE);
            layoutPicksForYouEmpty.setVisibility(View.GONE);
            textPicksForYouTitle.setVisibility(View.VISIBLE);
        }
    }


    // UPDATED: Fetches study materials data, now with optional subject filtering
    private void fetchStudyMaterialsFromFirestore(List<String> subjects) {
        studyMaterialsList.clear();

        Query query = db.collection("studyMaterials");

        if (!subjects.isEmpty()) {
            // Filter materials by subjects, similar to tutors
            List<String> querySubjects = subjects.size() > 10 ? subjects.subList(0, 10) : subjects;
            query = query.whereIn("subject", querySubjects); // Use whereIn for exact subject match
        }

        query.orderBy("timestamp", Query.Direction.DESCENDING) // Order by newest first
                .limit(10) // Limit the number of materials fetched
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) {
                        Log.d(TAG, "Fragment not attached, skipping Toast/UI update for study materials.");
                        return;
                    }

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            StudyMaterial material = document.toObject(StudyMaterial.class);
                            if (material != null) {
                                material.setId(document.getId());
                                studyMaterialsList.add(material);
                            }
                        }
                        studyMaterialsAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Study materials fetched from Firestore successfully: " + studyMaterialsList.size());
                    } else {
                        Log.w(TAG, "Error getting study materials from Firestore: ", task.getException());
                        Toast.makeText(getContext(), "Failed to load study materials.", Toast.LENGTH_SHORT).show();
                    }
                    updateStudyMaterialsUI(studyMaterialsList.isEmpty());
                });
    }

    private void updateStudyMaterialsUI(boolean isEmpty) {
        if (isEmpty) {
            recyclerStudyMaterials.setVisibility(View.GONE);
            layoutStudyMaterialsEmpty.setVisibility(View.VISIBLE);
            textStudyMaterialsTitle.setVisibility(View.GONE); // Hide title if empty
        } else {
            recyclerStudyMaterials.setVisibility(View.VISIBLE);
            layoutStudyMaterialsEmpty.setVisibility(View.GONE);
            textStudyMaterialsTitle.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Manages the visibility of the overall dashboard ProgressBar and potentially other elements.
     * Use this for initial loading of the entire dashboard.
     */
    private void setLoading(boolean isLoading) {
        if (!isAdded() || getContext() == null) return;
        progressBarDashboard.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // Optionally disable other UI elements during full load:
        // recyclerNewsFeed.setEnabled(!isLoading);
        // recyclerPicksForYou.setEnabled(!isLoading);
        // recyclerTopTutors.setEnabled(!isLoading);
        // recyclerStudyMaterials.setEnabled(!isLoading);
        // btnPostRequest.setEnabled(!isLoading);
        // btnManageSubjects.setEnabled(!isLoading);
    }
}
