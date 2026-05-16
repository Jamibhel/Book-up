package com.example.bookup.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.R;
import com.example.bookup.adapters.BookingAdapter;
import com.example.bookup.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TutorBookingsActivity extends AppCompatActivity {
    private static final String TAG = "TutorBookingsActivity";
    private RecyclerView bookingsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingsList = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_bookings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        bookingsRecyclerView = findViewById(R.id.recycler_bookings);
        progressBar = findViewById(R.id.progress_bar_bookings);
        emptyStateText = findViewById(R.id.text_empty_bookings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bookingAdapter = new BookingAdapter(this, bookingsList);
        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingsRecyclerView.setAdapter(bookingAdapter);

        loadTutorBookings(currentUser.getUid());
    }

    private void loadTutorBookings(String uid) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("bookings")
                .whereEqualTo("tutorId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, e) -> {
                    progressBar.setVisibility(View.GONE);
                    if (e != null) return;
                    if (value != null) {
                        bookingsList.clear();
                        bookingsList.addAll(value.toObjects(Booking.class));
                        bookingAdapter.notifyDataSetChanged();
                        emptyStateText.setVisibility(bookingsList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
