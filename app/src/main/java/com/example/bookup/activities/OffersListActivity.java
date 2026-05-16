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
import com.example.bookup.adapters.TutorOfferAdapter;
import com.example.bookup.models.TutorOffer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display all offers received for a specific help request
 */
public class OffersListActivity extends AppCompatActivity {

    public static final String EXTRA_REQUEST_ID = "request_id";
    public static final String EXTRA_REQUEST_TITLE = "request_title";

    private static final String TAG = "OffersListActivity";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textNoOffers;
    private Toolbar toolbar;

    private TutorOfferAdapter adapter;
    private List<TutorOffer> offersList = new ArrayList<>();
    private String requestId;
    private String requestTitle;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers_list);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Get data from intent
        requestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);
        requestTitle = getIntent().getStringExtra(EXTRA_REQUEST_TITLE);

        // Initialize views
        toolbar = findViewById(R.id.toolbar_offers);
        recyclerView = findViewById(R.id.recycler_offers);
        progressBar = findViewById(R.id.progress_bar_offers);
        textNoOffers = findViewById(R.id.text_no_offers);

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Offers for: " + (requestTitle != null ? requestTitle : "Request"));
        }

        // Set up RecyclerView
        setupRecyclerView();

        // Load offers from Firestore
        if (requestId != null) {
            loadOffers();
        } else {
            Toast.makeText(this, "Request ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new TutorOfferAdapter(offersList, new TutorOfferAdapter.OnOfferClickListener() {
            @Override
            public void onOfferClick(TutorOffer offer, int position) {
                // TODO: Show offer details in a dialog
                Log.d(TAG, "Offer clicked: " + offer.getId());
            }

            @Override
            public void onAcceptClick(TutorOffer offer, int position) {
                acceptOffer(offer, position);
            }

            @Override
            public void onRejectClick(TutorOffer offer, int position) {
                rejectOffer(offer, position);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadOffers() {
        setLoading(true);

        db.collection("helpRequests")
                .document(requestId)
                .collection("offers")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    offersList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            TutorOffer offer = document.toObject(TutorOffer.class);
                            offer.setId(document.getId());
                            offersList.add(offer);
                        } catch (Exception e) {
                            Log.e(TAG, "Error deserializing offer: " + e.getMessage());
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    setLoading(false);

                    Log.d(TAG, "Loaded " + offersList.size() + " offers");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading offers: " + e.getMessage());
                    Toast.makeText(OffersListActivity.this, "Error loading offers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    private void acceptOffer(TutorOffer offer, int position) {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Update offer status to "accepted"
        db.collection("helpRequests")
                .document(requestId)
                .collection("offers")
                .document(offer.getId())
                .update(
                        "status", "accepted",
                        "acceptedByStudent", currentUser.getUid(),
                        "acceptedAt", com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(aVoid -> {
                    // Also reject all other offers for this request
                    rejectOtherOffers(offer.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error accepting offer: " + e.getMessage());
                    Toast.makeText(OffersListActivity.this, "Error accepting offer", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    private void rejectOtherOffers(String acceptedOfferId) {
        // Get all offers and reject the ones that weren't accepted
        db.collection("helpRequests")
                .document(requestId)
                .collection("offers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int rejectedCount = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (!document.getId().equals(acceptedOfferId) && 
                            "pending".equals(document.get("status"))) {
                            
                            db.collection("helpRequests")
                                    .document(requestId)
                                    .collection("offers")
                                    .document(document.getId())
                                    .update("status", "rejected")
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error rejecting other offer: " + e.getMessage());
                                    });
                            
                            rejectedCount++;
                        }
                    }
                    
                    Toast.makeText(OffersListActivity.this, "Offer accepted! (" + rejectedCount + " other offers rejected)", Toast.LENGTH_SHORT).show();
                    loadOffers(); // Reload to show updated statuses
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching offers: " + e.getMessage());
                    setLoading(false);
                });
    }

    private void rejectOffer(TutorOffer offer, int position) {
        setLoading(true);

        db.collection("helpRequests")
                .document(requestId)
                .collection("offers")
                .document(offer.getId())
                .update("status", "rejected")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OffersListActivity.this, "Offer rejected", Toast.LENGTH_SHORT).show();
                    loadOffers(); // Reload to show updated status
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error rejecting offer: " + e.getMessage());
                    Toast.makeText(OffersListActivity.this, "Error rejecting offer", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    private void updateEmptyState() {
        if (offersList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textNoOffers.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textNoOffers.setVisibility(View.GONE);
        }
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
