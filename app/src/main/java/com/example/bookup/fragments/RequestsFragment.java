package com.example.bookup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookup.adapters.HelpRequestAdapter;
import com.example.bookup.databinding.FragmentRequestsBinding;
import com.example.bookup.models.HelpRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class RequestsFragment extends Fragment {
    private FragmentRequestsBinding binding;
    private HelpRequestAdapter adapter;
    private final List<HelpRequest> requestsList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRequestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadHelpRequests();
    }

    private void setupRecyclerView() {
        adapter = new HelpRequestAdapter(requestsList);
        binding.recyclerRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerRequests.setAdapter(adapter);
    }

    private void loadHelpRequests() {
        db.collection("helpRequests")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null) return;
                    if (error != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        requestsList.clear();
                        requestsList.addAll(value.toObjects(HelpRequest.class));
                        adapter.notifyDataSetChanged();
                        binding.textEmptyRequests.setVisibility(requestsList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
