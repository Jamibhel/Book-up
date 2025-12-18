package com.example.bookup.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bookup.R;
import com.example.bookup.adapters.SearchFragmentStateAdapter;
import com.example.bookup.models.StudyMaterial;
import com.example.bookup.models.Tutor;
import com.example.bookup.utils.PaginationHelper;
import com.example.bookup.utils.FirebaseErrorHandler;
import com.example.bookup.utils.NetworkConnectivityManager;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final int PAGE_SIZE = 20;

    private SearchBar searchBar;
    private SearchView searchView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private PaginationHelper paginationHelper;
    private FirebaseErrorHandler errorHandler;
    private NetworkConnectivityManager connectivityManager;

    private SearchFragmentStateAdapter viewPagerFragmentAdapter;

    // Cloud-based search with pagination
    private List<StudyMaterial> currentFilteredMaterials = new ArrayList<>();
    private List<Tutor> currentFilteredTutors = new ArrayList<>();
    
    // Track last search query
    private String lastSearchQuery = "";
    private boolean isSearching = false;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        paginationHelper = new PaginationHelper();
        errorHandler = new FirebaseErrorHandler();
        connectivityManager = new NetworkConnectivityManager(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start monitoring network connectivity
        if (connectivityManager != null) {
            connectivityManager.startMonitoring(this::onNetworkStateChanged);
        }
    }

    private void onNetworkStateChanged(boolean isConnected, String status) {
        // Update UI based on network state
        Log.d(TAG, "Network state changed: " + (isConnected ? "CONNECTED" : "OFFLINE"));
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop monitoring network connectivity
        if (connectivityManager != null) {
            connectivityManager.stopMonitoring();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initViews(view);
        setupViewPagerAndTabs();
        setupSearchView();

        fetchAllDataForSearch(); // Fetch all data once

        return view;
    }

    private void initViews(View view) {
        searchBar = view.findViewById(R.id.search_bar);
        searchView = view.findViewById(R.id.search_view);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupViewPagerAndTabs() {
        viewPagerFragmentAdapter = new SearchFragmentStateAdapter(getChildFragmentManager(), getLifecycle());
        viewPager.setAdapter(viewPagerFragmentAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.materials_tab_title); // Use string resource
                            break;
                        case 1:
                            tab.setText(R.string.tutors_tab_title); // Use string resource
                            break;
                    }
                }
        ).attach();

        // Listener for tab changes to update results immediately
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // When tab changes, re-apply the current search query to the new tab's fragment
                performSearch(searchView.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional: Scroll to top of list when re-selecting, if applicable
            }
        });

        // Ensure fragments are ready to receive data when they come into view
        // This is a more robust way to handle passing data to ViewPager2 fragments
        getChildFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);
                // When a fragment is resumed (visible), update it with the current filtered data
                if (f instanceof MaterialSearchResultsFragment && viewPager.getCurrentItem() == 0) {
                    ((MaterialSearchResultsFragment) f).updateSearchResults(currentFilteredMaterials);
                } else if (f instanceof TutorSearchResultsFragment && viewPager.getCurrentItem() == 1) {
                    ((TutorSearchResultsFragment) f).updateSearchResults(currentFilteredTutors);
                }
            }
        }, false);
    }

    private void setupSearchView() {
        searchView.setupWithSearchBar(searchBar);

        // When user submits query via keyboard
        searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchView.getText().toString());
                searchView.hide(); // Hide search view after search
                return true;
            }
            return false;
        });

        // Listen for changes in search query as user types in the search view
        searchView.getEditText().addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Perform search live as user types, or debounce for performance
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Listen for search view closing (e.g., via back button)
        searchView.addTransitionListener((searchView, previousState, newState) -> {
            if (newState == SearchView.TransitionState.HIDDEN) {
                searchBar.setText(searchView.getText()); // Update searchBar text to reflect last search
                performSearch(searchView.getText().toString()); // Re-apply search to ensure correct display
            }
        });
    }

    // Performs cloud-based search using Firestore queries with pagination
    private void fetchAllDataForSearch() {
        // This method now performs cloud-based search instead of loading all data
        // The actual search will be triggered when user types in SearchView
    }

    /**
     * Performs cloud-based search using Firestore range queries for efficiency.
     * @param query The search string.
     */
    private void performSearch(String query) {
        if (!isAdded() || getContext() == null) return;

        lastSearchQuery = query.toLowerCase(Locale.getDefault()).trim();
        
        if (lastSearchQuery.isEmpty()) {
            // Empty query - clear results
            currentFilteredMaterials.clear();
            currentFilteredTutors.clear();
            updateCurrentFragmentWithResults();
            return;
        }

        isSearching = true;
        setLoading(true);

        // Search materials using cloud query with pagination
        searchMaterials(lastSearchQuery);
        
        // Search tutors using cloud query
        searchTutors(lastSearchQuery);
    }

    /** Search study materials using cloud-based Firestore query with range constraints */
    private void searchMaterials(String searchTerm) {
        // Query by title range: title >= searchTerm AND title < searchTerm + '~'
        Query query = db.collection("studyMaterials")
                .whereGreaterThanOrEqualTo("title", searchTerm)
                .whereLessThan("title", searchTerm + "\uffff") // Using max Unicode character for range
                .limit(PAGE_SIZE);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || getContext() == null) return;

                    currentFilteredMaterials.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        StudyMaterial material = document.toObject(StudyMaterial.class);
                        if (material != null) {
                            material.setId(document.getId());
                            currentFilteredMaterials.add(material);
                        }
                    }
                    Log.d(TAG, "Cloud search found " + currentFilteredMaterials.size() + " materials");
                    updateCurrentFragmentWithResults();
                    isSearching = false;
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) {
                        isSearching = false;
                        return;
                    }
                    Log.e(TAG, "Error searching materials: " + e.getMessage(), e);
                    if (errorHandler != null) {
                        errorHandler.handleError(e, progressBar);
                    } else {
                        Toast.makeText(getContext(), "Search failed", Toast.LENGTH_SHORT).show();
                    }
                    isSearching = false;
                    setLoading(false);
                });
    }

    /** Search tutors using cloud-based Firestore query */
    private void searchTutors(String searchTerm) {
        // Query by name range: name >= searchTerm AND name < searchTerm + '~'
        Query query = db.collection("tutors")
                .whereGreaterThanOrEqualTo("name", searchTerm)
                .whereLessThan("name", searchTerm + "\uffff")
                .limit(PAGE_SIZE);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || getContext() == null) return;

                    currentFilteredTutors.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tutor tutor = document.toObject(Tutor.class);
                        if (tutor != null) {
                            tutor.setUid(document.getId());
                            currentFilteredTutors.add(tutor);
                        }
                    }
                    Log.d(TAG, "Cloud search found " + currentFilteredTutors.size() + " tutors");
                    updateCurrentFragmentWithResults();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    Log.e(TAG, "Error searching tutors: " + e.getMessage(), e);
                    if (errorHandler != null) {
                        errorHandler.handleError(e, progressBar);
                    } else {
                        Toast.makeText(getContext(), "Search failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Update the currently visible fragment with search results */
    private void updateCurrentFragmentWithResults() {
        if (!isAdded() || viewPagerFragmentAdapter == null) return;

        Fragment currentFragment = viewPagerFragmentAdapter.createFragment(viewPager.getCurrentItem());
        if (currentFragment instanceof MaterialSearchResultsFragment) {
            ((MaterialSearchResultsFragment) currentFragment).updateSearchResults(currentFilteredMaterials);
        } else if (currentFragment instanceof TutorSearchResultsFragment) {
            ((TutorSearchResultsFragment) currentFragment).updateSearchResults(currentFilteredTutors);
        }
    }

    private void setLoading(boolean isLoading) {
        if (!isAdded() || getContext() == null) return;
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        searchBar.setEnabled(!isLoading);
        tabLayout.setEnabled(!isLoading);
        viewPager.setUserInputEnabled(!isLoading); // Prevent swiping while loading
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Null out view references
        searchBar = null;
        tabLayout = null;
        viewPager = null;
        progressBar = null;
        viewPagerFragmentAdapter = null;
    }
}
