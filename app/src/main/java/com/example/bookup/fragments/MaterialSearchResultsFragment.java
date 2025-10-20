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

import com.example.bookup.MaterialDetailsActivity; // NEW import
import com.example.bookup.R;
import com.example.bookup.StudyMaterialOverviewAdapter;
import com.example.bookup.models.StudyMaterial;

import java.util.ArrayList;
import java.util.List;

public class MaterialSearchResultsFragment extends Fragment {

    private RecyclerView recyclerView;
    private StudyMaterialOverviewAdapter adapter;
    private List<StudyMaterial> materialList = new ArrayList<>();
    private TextView textEmptyResults; // Changed to textEmptyResults

    public MaterialSearchResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_search_results);
        textEmptyResults = view.findViewById(R.id.text_empty_search_results); // Use new ID

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StudyMaterialOverviewAdapter(materialList);
        recyclerView.setAdapter(adapter);

        // Set click listener for the adapter (this will now navigate to MaterialDetailsActivity)
        adapter.setOnMaterialClickListener(material -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), MaterialDetailsActivity.class);
                intent.putExtra(MaterialDetailsActivity.EXTRA_MATERIAL, material);
                startActivity(intent);
            }
        });

        updateUI(); // Initial UI update based on empty list
        return view;
    }

    public void updateSearchResults(List<StudyMaterial> results) {
        materialList.clear();
        if (results != null) {
            materialList.addAll(results);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            updateUI();
        }
    }

    private void updateUI() {
        if (materialList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textEmptyResults.setVisibility(View.VISIBLE);
            textEmptyResults.setText(R.string.no_materials_search_results); // Use string resource
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textEmptyResults.setVisibility(View.GONE);
        }
    }
}
