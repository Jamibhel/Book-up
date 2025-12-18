package com.example.bookup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookup.R;
import com.example.bookup.activities.RequestDetailsActivity;
import com.example.bookup.adapters.RequestAdapter;
import com.example.bookup.models.Request;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageRequestsActivity extends AppCompatActivity implements RequestAdapter.OnRequestClickListener {
    private static final String TAG = "ManageRequestsActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI Components
    private RecyclerView recyclerRequests;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CircularProgressIndicator progressIndicator;
    private TextInputEditText searchInput;
    private ChipGroup filterChipGroup;
    private MaterialTextView textNoRequests;

    // Adapter
    private RequestAdapter requestAdapter;
    private List<Request> allRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_requests);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_manage_requests);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Check authentication and admin status
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to manage requests.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        checkAdminStatus();

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        setupFilters();
        fetchRequests(); // Initial fetch
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        recyclerRequests = findViewById(R.id.recycler_requests);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        progressIndicator = findViewById(R.id.progress_indicator);
        searchInput = findViewById(R.id.search_input);
        filterChipGroup = findViewById(R.id.filter_chip_group);
        textNoRequests = findViewById(R.id.text_no_requests);
    }

    private void setupRecyclerView() {
        allRequests = new ArrayList<>();
        requestAdapter = new RequestAdapter(new ArrayList<>(), this);
        recyclerRequests.setLayoutManager(new LinearLayoutManager(this));
        recyclerRequests.setAdapter(requestAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchRequests);
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterRequests();
            }
        });
    }

    private void setupFilters() {
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> filterRequests());
    }

    private void checkAdminStatus() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                    if (isAdmin == null || !isAdmin) {
                        Toast.makeText(this, "Access denied: Not an admin.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, "User profile not found. Access denied.", Toast.LENGTH_LONG).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to check admin status: " + e.getMessage(), e);
                Toast.makeText(this, "Error checking admin status.", Toast.LENGTH_LONG).show();
                finish();
            });
    }

    private void fetchRequests() {
        setLoading(true);
        db.collection("helpRequests")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                setLoading(false);
                swipeRefreshLayout.setRefreshing(false);

                if (task.isSuccessful()) {
                    allRequests.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Request request = document.toObject(Request.class);
                        request.setId(document.getId());
                        allRequests.add(request);
                    }
                    filterRequests();
                } else {
                    Log.e(TAG, "Error fetching requests", task.getException());
                    Toast.makeText(this, "Error fetching requests: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void filterRequests() {
        String searchQuery = searchInput.getText().toString().toLowerCase(Locale.ROOT).trim();
        String status = getSelectedStatus();

        List<Request> filteredRequests = new ArrayList<>();
        for (Request request : allRequests) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    request.getDescription().toLowerCase(Locale.ROOT).contains(searchQuery);
            
            boolean matchesStatus = status.equals("all") || request.getStatus().equals(status);

            if (matchesSearch && matchesStatus) {
                filteredRequests.add(request);
            }
        }

        updateRequestsList(filteredRequests);
    }

    private String getSelectedStatus() {
        int checkedId = filterChipGroup.getCheckedChipId();
        if (checkedId == R.id.chip_pending) return "pending";
        if (checkedId == R.id.chip_accepted) return "accepted";
        if (checkedId == R.id.chip_rejected) return "rejected";
        return "all";
    }

    private void updateRequestsList(List<Request> requests) {
        requestAdapter.updateRequests(requests);
        updateEmptyState(requests.isEmpty());
    }

    @Override
    public void onRequestClick(Request request) {
        Intent intent = new Intent(this, RequestDetailsActivity.class);
        intent.putExtra(RequestDetailsActivity.EXTRA_REQUEST_ID, request.getId());
        intent.putExtra(RequestDetailsActivity.EXTRA_IS_ADMIN, true);
        startActivity(intent);
    }

    private void setLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerRequests.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState(boolean isEmpty) {
        textNoRequests.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerRequests.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}