package com.example.bookup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout; // For empty state
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookup.models.NewsItem; // Import your NewsItem model
import com.google.android.material.dialog.MaterialAlertDialogBuilder; // For delete confirmation
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageNewsActivity extends AppCompatActivity {

    private static final String TAG = "ManageNewsActivity";

    // UI Elements
    private RecyclerView recyclerNewsItems;
    private ExtendedFloatingActionButton fabAddNewNews;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyNews; // NEW: for empty state
    private TextView textEmptyNewsTitle; // NEW
    private TextView textEmptyNewsDescription; // NEW

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth; // For admin check

    // Adapter and Data
    private NewsItemManagerAdapter newsAdapter; // Use the new manager adapter
    private List<NewsItem> newsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_news);

        Toolbar toolbar = findViewById(R.id.toolbar_manage_news);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.manage_news_title);
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to manage news.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        checkAdminStatus(); // Ensure only admins can access

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupSwipeRefresh();

        fetchNewsItems(); // Initial fetch
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        recyclerNewsItems = findViewById(R.id.recycler_manage_news_items);
        fabAddNewNews = findViewById(R.id.fab_add_new_news);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_manage_news); // NEW ID
        progressBar = findViewById(R.id.progress_bar_manage_news); // NEW ID
        layoutEmptyNews = findViewById(R.id.layout_empty_news); // NEW
        textEmptyNewsTitle = findViewById(R.id.text_empty_news_title); // NEW
        textEmptyNewsDescription = findViewById(R.id.text_empty_news_description); // NEW
    }

    private void setupRecyclerView() {
        newsList = new ArrayList<>();
        newsAdapter = new NewsItemManagerAdapter(newsList);
        recyclerNewsItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerNewsItems.setAdapter(newsAdapter);

        newsAdapter.setOnNewsItemActionListener(new NewsItemManagerAdapter.OnNewsItemActionListener() {
            @Override
            public void onEditClick(NewsItem newsItem) {
                // Launch EditNewsItemActivity
                Intent intent = new Intent(ManageNewsActivity.this, EditNewsItemActivity.class);
                intent.putExtra(EditNewsItemActivity.EXTRA_NEWS_ITEM, newsItem);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(NewsItem newsItem) {
                // Show confirmation dialog before deleting
                confirmDeleteNewsItem(newsItem);
            }
        });
    }

    private void setupClickListeners() {
        fabAddNewNews.setOnClickListener(v -> {
            startActivity(new Intent(ManageNewsActivity.this, CreateNewsItemActivity.class));
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchNewsItems);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorSecondary);
    }

    private void checkAdminStatus() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Authentication required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
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
                    Toast.makeText(this, "Error checking admin status. Access denied.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void fetchNewsItems() {
        setLoading(true);

        db.collection("newsFeed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    newsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        NewsItem newsItem = document.toObject(NewsItem.class);
                        newsItem.setId(document.getId()); // Set the document ID
                        newsList.add(newsItem);
                    }
                    newsAdapter.notifyDataSetChanged();
                    updateEmptyState(newsList.isEmpty());
                    setLoading(false);
                    if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching news items: " + e.getMessage(), e);
                    Toast.makeText(ManageNewsActivity.this, "Failed to load news items.", Toast.LENGTH_SHORT).show();
                    updateEmptyState(true); // Show empty state on error
                    setLoading(false);
                    if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void confirmDeleteNewsItem(NewsItem newsItem) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete News Item?")
                .setMessage("Are you sure you want to delete \"" + newsItem.getTitle() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteNewsItem(newsItem))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNewsItem(NewsItem newsItem) {
        if (newsItem.getId() == null || newsItem.getId().isEmpty()) {
            Toast.makeText(this, "Error: News item ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        db.collection("newsFeed").document(newsItem.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ManageNewsActivity.this, "News item deleted successfully!", Toast.LENGTH_SHORT).show();
                    fetchNewsItems(); // Refresh list
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Error deleting news item: " + e.getMessage(), e);
                    Toast.makeText(ManageNewsActivity.this, "Failed to delete news item: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerNewsItems.setVisibility(View.GONE);
            layoutEmptyNews.setVisibility(View.VISIBLE);
            textEmptyNewsTitle.setText(R.string.no_news_to_manage_title);
            textEmptyNewsDescription.setText(R.string.no_news_to_manage_description);
        } else {
            recyclerNewsItems.setVisibility(View.VISIBLE);
            layoutEmptyNews.setVisibility(View.GONE);
        }
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading && !swipeRefreshLayout.isRefreshing() ? View.VISIBLE : View.GONE);
        fabAddNewNews.setEnabled(!isLoading);
        swipeRefreshLayout.setEnabled(!isLoading); // Disable pull-to-refresh during loading
    }
}
