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

import com.example.bookup.adapters.BookingAdapter;
import com.example.bookup.databinding.FragmentBookingListBinding;
import com.example.bookup.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class BookingListFragment extends Fragment {
    private FragmentBookingListBinding binding;
    private BookingAdapter adapter;
    private final List<Booking> bookingsList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        loadBookings();
        
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new BookingAdapter(requireContext(), bookingsList);
        binding.recyclerBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerBookings.setAdapter(adapter);
    }

    private void loadBookings() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("bookings")
                .whereArrayContains("participantIds", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null) return;
                    if (value != null) {
                        bookingsList.clear();
                        bookingsList.addAll(value.toObjects(Booking.class));
                        adapter.notifyDataSetChanged();
                        binding.textEmptyBookings.setVisibility(bookingsList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
