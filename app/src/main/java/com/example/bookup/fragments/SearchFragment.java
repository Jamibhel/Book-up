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
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    private SearchBar searchBar;
    private SearchView searchView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;

    private FirebaseFirestore db;

    private SearchFragmentStateAdapter viewPagerFragmentAdapter;

    // To hold all data once fetched from Firestore for client-side filtering
    private List<StudyMaterial> allStudyMaterials = new ArrayList<>();
    private List<Tutor> allTutors = new ArrayList<>();

    // Keep track of the currently displayed search results in case a new search happens
    private List<StudyMaterial> currentFilteredMaterials = new ArrayList<>();
    private List<Tutor> currentFilteredTutors = new ArrayList<>();

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
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

    // Fetches all study materials and tutors from Firestore initially
    private void fetchAllDataForSearch() {
        setLoading(true);

        // Fetch Study Materials
        db.collection("studyMaterials")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allStudyMaterials.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        StudyMaterial material = document.toObject(StudyMaterial.class);
                        if (material != null) {
                            material.setId(document.getId());
                            allStudyMaterials.add(material);
                        }
                    }
                    Log.d(TAG, "Fetched " + allStudyMaterials.size() + " study materials for search.");
                    // After fetching, immediately perform initial search (empty query)
                    performSearch(searchView.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching all study materials: " + e.getMessage(), e);
                    if (getContext() != null) Toast.makeText(getContext(), "Failed to load materials for search.", Toast.LENGTH_SHORT).show();
                });

        // Fetch Tutors
        db.collection("tutors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allTutors.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Tutor tutor = document.toObject(Tutor.class);
                        if (tutor != null) {
                            tutor.setUid(document.getId());
                            allTutors.add(tutor);
                        }
                    }
                    Log.d(TAG, "Fetched " + allTutors.size() + " tutors for search.");
                    // After fetching, immediately perform initial search (empty query)
                    performSearch(searchView.getText().toString());
                    setLoading(false); // Only set loading to false after both fetches complete
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching all tutors: " + e.getMessage(), e);
                    if (getContext() != null) Toast.makeText(getContext(), "Failed to load tutors for search.", Toast.LENGTH_SHORT).show();
                    setLoading(false); // Ensure loading is turned off even on failure
                });
    }


    /**
     * Performs a client-side search on the fetched data and updates the current fragment.
     * @param query The search string.
     */
    private void performSearch(String query) {
        if (!isAdded() || getContext() == null) return;

        String lowerCaseQuery = query.toLowerCase(Locale.getDefault()).trim();

        // Filter materials
        currentFilteredMaterials.clear();
        if (!lowerCaseQuery.isEmpty()) {
            for (StudyMaterial material : allStudyMaterials) {
                boolean match = (material.getTitle() != null && material.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                        (material.getDescription() != null && material.getDescription().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                        (material.getSubject() != null && material.getSubject().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                        (material.getMaterialType() != null && material.getMaterialType().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                        (material.getUploaderName() != null && material.getUploaderName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery));
                if (match) {
                    currentFilteredMaterials.add(material);
                }
            }
        } else {
            // If query is empty, show all materials (or none, depending on desired behavior)
            // For search, usually show none if query is empty.
            // If you want to show all: currentFilteredMaterials.addAll(allStudyMaterials);
        }


        // Filter tutors
        currentFilteredTutors.clear();
        if (!lowerCaseQuery.isEmpty()) {
            for (Tutor tutor : allTutors) {
                boolean match = (tutor.getName() != null && tutor.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                        (tutor.getBio() != null && tutor.getBio().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery));

                if (!match && tutor.getSubjects() != null) {
                    for (String subject : tutor.getSubjects()) {
                        if (subject != null && subject.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                            match = true;
                            break;
                        }
                    }
                }
                if (match) {
                    currentFilteredTutors.add(tutor);
                }
            }
        } else {
            // If query is empty, show all tutors (or none)
            // If you want to show all: currentFilteredTutors.addAll(allTutors);
        }


        // Update the currently visible fragment with the filtered results
        Fragment currentFragment = viewPagerFragmentAdapter.createFragment(viewPager.getCurrentItem()); // Get the active fragment instance
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
}
