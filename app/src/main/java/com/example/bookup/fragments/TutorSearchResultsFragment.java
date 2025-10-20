package com.example.bookup.fragments;

import android.content.Intent; // NEW import
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.R;
import com.example.bookup.TutorDetailsActivity; // NEW import
import com.example.bookup.TutorOverviewAdapter;
import com.example.bookup.models.Tutor;

import java.util.ArrayList;
import java.util.List;

public class TutorSearchResultsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TutorOverviewAdapter adapter;
    private List<Tutor> tutorList = new ArrayList<>();
    private TextView textEmptyResults; // Changed to textEmptyResults

    public TutorSearchResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_search_results);
        textEmptyResults = view.findViewById(R.id.text_empty_search_results); // Use new ID

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TutorOverviewAdapter(tutorList);
        recyclerView.setAdapter(adapter);

        // Set click listener for the adapter (this will now navigate to TutorDetailsActivity)
        adapter.setOnTutorClickListener(tutor -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), TutorDetailsActivity.class);
                intent.putExtra(TutorDetailsActivity.EXTRA_TUTOR, tutor);
                startActivity(intent);
            }
        });


        updateUI(); // Initial UI update based on empty list
        return view;
    }

    public void updateSearchResults(List<Tutor> results) {
        tutorList.clear();
        if (results != null) {
            tutorList.addAll(results);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            updateUI();
        }
    }

    private void updateUI() {
        if (tutorList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textEmptyResults.setVisibility(View.VISIBLE);
            textEmptyResults.setText(R.string.no_tutors_search_results); // Use string resource
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textEmptyResults.setVisibility(View.GONE);
        }
    }
}
