package com.example.bookup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // For empty state icon
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView; // For empty state message
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookup.CreateRequestActivity; // For launching new request activity
import com.example.bookup.HelpRequestAdapter;
import com.example.bookup.R;
import com.example.bookup.RequestDetailsActivity; // For launching request details
import com.example.bookup.models.HelpRequest;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet; // For user subjects

public class RequestsFragment extends Fragment {

    private static final String TAG = "RequestsFragment";

    // UI Elements
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutNoRequests; // Existing
    private ImageView imgNoRequestsIcon; // NEW
    private TextView textNoRequestsTitle; // NEW
    private TextView textNoRequestsDescription; // NEW
    private ExtendedFloatingActionButton fabNewRequest;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Adapter and Data
    private HelpRequestAdapter requestAdapter;
    private List<HelpRequest> requestList;

    // User-specific data for personalized queries
    private boolean isCurrentUserTutor = false; // Flag to determine role
    private List<String> currentUserSubjects = new ArrayList<>(); // Tutor's teaching subjects or Student's learning subjects

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        requestList = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        setupSwipeRefresh();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUser != null) {
            fetchUserRoleAndSubjects(); // Fetch user role and then requests
        } else {
            Log.e(TAG, "User not authenticated. Cannot fetch requests.");
            if (getContext() != null) Toast.makeText(getContext(), "Please sign in to view requests.", Toast.LENGTH_SHORT).show();
            updateUI(true); // Show empty state for unauthenticated
            if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_requests);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        layoutNoRequests = view.findViewById(R.id.layout_no_requests);
        imgNoRequestsIcon = view.findViewById(R.id.img_no_requests_icon); // NEW
        textNoRequestsTitle = view.findViewById(R.id.text_no_requests_title); // NEW
        textNoRequestsDescription = view.findViewById(R.id.text_no_requests_description); // NEW
        fabNewRequest = view.findViewById(R.id.fab_new_request);
        progressBar = view.findViewById(R.id.progress_bar_requests);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        requestAdapter = new HelpRequestAdapter(requestList);
        recyclerView.setAdapter(requestAdapter);
        // Note: onRequestClickListener is now handled directly in adapter for navigation
    }

    private void setupClickListeners() {
        fabNewRequest.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), CreateRequestActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (currentUser != null) {
                fetchUserRoleAndSubjects(); // Re-fetch all data on refresh
            } else {
                swipeRefreshLayout.setRefreshing(false); // Stop refreshing if no user
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorSecondary);
    }

    // NEW: Fetch user's role and subjects before fetching requests
    private void fetchUserRoleAndSubjects() {
        if (currentUser == null) return;

        setLoading(true);

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getContext() == null) return;

                    if (documentSnapshot.exists()) {
                        Boolean isTutorBool = documentSnapshot.getBoolean("isTutor");
                        isCurrentUserTutor = (isTutorBool != null && isTutorBool);
                        Log.d(TAG, "Current user isTutor: " + isCurrentUserTutor);

                        currentUserSubjects.clear();
                        Object subjectsObject = documentSnapshot.get("subjects");
                        if (subjectsObject instanceof Map) {
                            Map<String, Boolean> subjectsMap = (Map<String, Boolean>) subjectsObject;
                            for (Map.Entry<String, Boolean> entry : subjectsMap.entrySet()) {
                                if (entry.getValue() != null && entry.getValue()) {
                                    currentUserSubjects.add(entry.getKey());
                                }
                            }
                        } else if (subjectsObject instanceof List) { // Fallback for old List<String> format
                            List<?> rawSubjectsList = (List<?>) subjectsObject;
                            for (Object item : rawSubjectsList) {
                                if (item instanceof String) {
                                    currentUserSubjects.add((String) item);
                                }
                            }
                        }
                        Log.d(TAG, "User subjects: " + currentUserSubjects.size());
                    } else {
                        Log.w(TAG, "User profile not found for " + currentUser.getUid());
                        isCurrentUserTutor = false; // Default to student
                    }
                    // Now that role and subjects are known, fetch the relevant requests
                    fetchHelpRequests();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    setLoading(false); // Stop loading on failure
                    Log.e(TAG, "Error fetching user role and subjects: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Failed to get user details.", Toast.LENGTH_SHORT).show();
                    isCurrentUserTutor = false; // Default to student on error
                    fetchHelpRequests(); // Still try to fetch requests, maybe general ones
                });
    }


    private void fetchHelpRequests() {
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated for fetchHelpRequests.");
            if (getContext() != null) Toast.makeText(getContext(), "Please sign in to view requests.", Toast.LENGTH_SHORT).show();
            updateUI(true);
            if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
            setLoading(false);
            return;
        }

        setLoading(true); // Show progress bar

        Query query = db.collection("helpRequests");

        if (isCurrentUserTutor && !currentUserSubjects.isEmpty()) {
            // Tutor view: Show open requests that match their teaching subjects
            List<String> querySubjects = currentUserSubjects.size() > 10 ? currentUserSubjects.subList(0, 10) : currentUserSubjects; // Firestore limit for whereIn
            query = query.whereEqualTo("status", "Open")
                    .whereIn("subject", querySubjects);
            Log.d(TAG, "Fetching requests for tutor. Subjects: " + querySubjects);
        } else if (!isCurrentUserTutor) {
            // Student view: Show their own requests, and potentially other open requests
            // For simplicity, let's show ALL open requests, and also their own if they are resolved.
            // Or, you might want two separate lists: "My Requests" and "Other Open Requests"
            // For now, let's fetch all open requests that are not by the current user
            // and then separately fetch the user's own requests.
            // A more complex setup might filter this into two tabs/sections within the fragment.

            // Simplest approach: fetch all open requests that are not by the current user
            query = query.whereEqualTo("status", "Open");
            Log.d(TAG, "Fetching general open requests for student.");
            // We could add an additional query for requests posted by the student,
            // even if they are 'Assigned' or 'Resolved', and merge the lists.
            // For this iteration, let's just show general 'Open' requests to all students.
            // A dedicated "My Requests" tab would be better for personal requests.
        } else {
            // Fallback for tutors without subjects, or if isCurrentUserTutor is false for other reasons
            query = query.whereEqualTo("status", "Open");
            Log.d(TAG, "Fetching general open requests (fallback).");
        }


        query.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || getContext() == null) return;

                    requestList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        HelpRequest request = document.toObject(HelpRequest.class);
                        if (request != null) {
                            request.setId(document.getId());
                            requestList.add(request);
                        }
                    }
                    requestAdapter.notifyDataSetChanged();
                    updateUI(requestList.isEmpty());
                    setLoading(false);
                    if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;

                    Log.e(TAG, "Error fetching help requests: " + e.getMessage(), e);
                    if (getContext() != null) Toast.makeText(getContext(), "Failed to load requests.", Toast.LENGTH_SHORT).show();
                    updateUI(true); // Show empty state on failure
                    setLoading(false);
                    if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void updateUI(boolean isEmpty) {
        if (!isAdded() || getContext() == null) return;

        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            layoutNoRequests.setVisibility(View.VISIBLE);
            // Customize empty state message based on user role
            if (isCurrentUserTutor && currentUserSubjects.isEmpty()) {
                imgNoRequestsIcon.setImageResource(R.drawable.ic_subject_black_24dp);
                textNoRequestsTitle.setText(R.string.no_teaching_subjects_title);
                textNoRequestsDescription.setText(R.string.no_teaching_subjects_description);
            } else if (isCurrentUserTutor) {
                imgNoRequestsIcon.setImageResource(R.drawable.ic_help_black_24dp);
                textNoRequestsTitle.setText(R.string.no_matching_requests_title_tutor);
                textNoRequestsDescription.setText(R.string.no_matching_requests_description_tutor);
            } else { // Student
                imgNoRequestsIcon.setImageResource(R.drawable.ic_empty_requests_24dp);
                textNoRequestsTitle.setText(R.string.no_open_requests_title_student);
                textNoRequestsDescription.setText(R.string.no_open_requests_description_student);
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutNoRequests.setVisibility(View.GONE);
        }
    }

    private void setLoading(boolean isLoading) {
        if (!isAdded() || getContext() == null) return;
        // Only show main progress bar if not currently refreshing
        progressBar.setVisibility(isLoading && !swipeRefreshLayout.isRefreshing() ? View.VISIBLE : View.GONE);
        fabNewRequest.setEnabled(!isLoading);
    }
}
