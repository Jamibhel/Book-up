package com.example.bookup.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bookup.R;
import com.example.bookup.adapters.SearchFragmentStateAdapter;
import com.example.bookup.viewmodels.SearchViewModel;
import com.example.bookup.databinding.BottomSheetSearchFilterBinding;
import com.example.bookup.models.StudyMaterial;
import com.example.bookup.models.Tutor;
import com.example.bookup.models.User;
import com.example.bookup.utils.FirebaseErrorHandler;
import com.example.bookup.utils.NetworkConnectivityManager;
import com.example.bookup.utils.PaginationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final int PAGE_SIZE = 20;

    private EditText searchInput;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private ImageButton btnFilter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseErrorHandler errorHandler;
    private NetworkConnectivityManager connectivityManager;

    private SearchFragmentStateAdapter viewPagerFragmentAdapter;
    private SearchViewModel viewModel;

    private List<StudyMaterial> currentFilteredMaterials = new ArrayList<>();
    private List<Tutor> currentFilteredTutors = new ArrayList<>();
    
    private String lastSearchQuery = "";
    private float minRatingFilter = 0.0f;
    private List<String> selectedSubjects = new ArrayList<>();

    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private static final long SEARCH_DEBOUNCE_DELAY = 300;

    public SearchFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        errorHandler = new FirebaseErrorHandler();
        connectivityManager = new NetworkConnectivityManager(getContext());
        viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        viewModel.loadInitialData();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        initViews(view);
        setupViewPagerAndTabs();
        setupSearchView();
        
        btnFilter.setOnClickListener(v -> showFilterBottomSheet());

        return view;
    }

    private void initViews(View view) {
        searchInput = view.findViewById(R.id.search_input);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        progressBar = view.findViewById(R.id.progress_bar);
        btnFilter = view.findViewById(R.id.btn_search_filter);
    }

    private void setupViewPagerAndTabs() {
        viewPagerFragmentAdapter = new SearchFragmentStateAdapter(getChildFragmentManager(), getLifecycle());
        viewPager.setAdapter(viewPagerFragmentAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Materials"); break;
                case 1: tab.setText("Tutors"); break;
                case 2: tab.setText("Students"); break;
            }
        }).attach();
    }

    private void setupSearchView() {
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    Log.d(TAG, "Filtering real-time for: " + s);
                    viewModel.filter(s.toString().trim());
                };
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::setLoading);
        
        // Observe and update internal lists to ensure state consistency
        viewModel.getFilteredTutors().observe(getViewLifecycleOwner(), tutors -> {
            currentFilteredTutors = tutors;
        });
        viewModel.getFilteredMaterials().observe(getViewLifecycleOwner(), materials -> {
            currentFilteredMaterials = materials;
        });
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        BottomSheetSearchFilterBinding sheetBinding = BottomSheetSearchFilterBinding.inflate(getLayoutInflater());
        bottomSheet.setContentView(sheetBinding.getRoot());

        sheetBinding.ratingSlider.setValue(minRatingFilter);
        // Pre-select chips if needed logic could go here

        sheetBinding.btnApplyFilter.setOnClickListener(v -> {
            minRatingFilter = sheetBinding.ratingSlider.getValue();
            selectedSubjects.clear();
            for (int i = 0; i < sheetBinding.subjectsChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) sheetBinding.subjectsChipGroup.getChildAt(i);
                if (chip.isChecked()) selectedSubjects.add(chip.getText().toString());
            }
            performSearch(searchInput.getText().toString().trim());
            bottomSheet.dismiss();
        });

        bottomSheet.show();
    }

    private void performSearch(String query) {
        lastSearchQuery = query;
        setLoading(true);
        searchMaterials(query);
        searchTutors(query);
    }

    private void searchMaterials(String query) {
        Query dbQuery = db.collection("studyMaterials");
        if (!query.isEmpty()) {
            dbQuery = dbQuery.whereGreaterThanOrEqualTo("title", query)
                             .whereLessThanOrEqualTo("title", query + "\uf8ff");
        }

        dbQuery.limit(PAGE_SIZE).get().addOnSuccessListener(snapshots -> {
            currentFilteredMaterials.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                StudyMaterial material = doc.toObject(StudyMaterial.class);
                material.setId(doc.getId());
                currentFilteredMaterials.add(material);
            }
            updateSearchResultsInFragments();
        });
    }

    private void searchTutors(String query) {
        Query dbQuery = db.collection("users").whereEqualTo("role", "tutor");
        
        dbQuery.get().addOnSuccessListener(snapshots -> {
            currentFilteredTutors.clear();
            String lowerQuery = query.toLowerCase();
            for (QueryDocumentSnapshot doc : snapshots) {
                User user = doc.toObject(User.class);
                if (user.getDisplayName() != null && user.getDisplayName().toLowerCase().contains(lowerQuery)) {
                    // Filter by rating
                    if (user.getRating() < minRatingFilter) continue;
                    
                    // Filter by subjects
                    if (!selectedSubjects.isEmpty()) {
                        boolean match = false;
                        if (user.getTutoringSubjects() != null) {
                            for (String sub : selectedSubjects) {
                                if (user.getTutoringSubjects().contains(sub)) {
                                    match = true;
                                    break;
                                }
                            }
                        }
                        if (!match) continue;
                    }

                    Tutor tutor = new Tutor();
                    tutor.setUid(doc.getId());
                    tutor.setName(user.getDisplayName());
                    tutor.setProfileImageUrl(user.getPhotoUrl());
                    tutor.setBio(user.getBio());
                    tutor.setSubjects(user.getTutoringSubjects());
                    tutor.setRating(user.getRating());
                    tutor.setReviewCount(user.getReviewCount());
                    tutor.setAvailable(user.isAvailable());
                    currentFilteredTutors.add(tutor);
                }
            }
            setLoading(false);
            updateSearchResultsInFragments();
        });
    }

    private void updateSearchResultsInFragments() {
        if (!isAdded()) return;
        
        // Loop through all possible fragment tags in ViewPager2
        for (int i = 0; i < 3; i++) {
            Fragment fragment = getChildFragmentManager().findFragmentByTag("f" + i);
            if (fragment instanceof TutorSearchResultsFragment) {
                if (i == 1) { // Tutors tab only
                    ((TutorSearchResultsFragment) fragment).updateSearchResults(currentFilteredTutors);
                }
                // We don't overwrite Students tab (i == 2) with tutor results
            } else if (fragment instanceof MaterialSearchResultsFragment) {
                if (i == 0) { // Materials tab
                    ((MaterialSearchResultsFragment) fragment).updateSearchResults(currentFilteredMaterials);
                }
            }
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchHandler.removeCallbacks(searchRunnable);
    }
}
