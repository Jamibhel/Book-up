package com.example.bookup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookup.adapters.NewsItemManagerAdapter;
import com.example.bookup.databinding.ActivityManageNewsBinding;
import com.example.bookup.models.NewsItem;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ManageNewsActivity extends AppCompatActivity {
    private ActivityManageNewsBinding binding;
    private FirebaseFirestore db;
    private NewsItemManagerAdapter adapter;
    private final List<NewsItem> newsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        loadNews();

        binding.fabAddNewNews.setOnClickListener(v -> startActivity(new Intent(this, CreateNewsItemActivity.class)));
        binding.swipeRefreshLayoutManageNews.setOnRefreshListener(this::loadNews);
    }

    private void setupRecyclerView() {
        adapter = new NewsItemManagerAdapter(newsList);
        adapter.setOnNewsItemActionListener(new NewsItemManagerAdapter.OnNewsItemActionListener() {
            @Override public void onEditClick(NewsItem newsItem) {
                if (newsItem.getId() == null) {
                    Toast.makeText(ManageNewsActivity.this, "Error: news item has no ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(ManageNewsActivity.this, EditNewsItemActivity.class);
                intent.putExtra(EditNewsItemActivity.EXTRA_NEWS_ITEM, newsItem);
                startActivity(intent);
            }
            @Override public void onDeleteClick(NewsItem newsItem) { confirmAndDeleteNews(newsItem); }
        });
        binding.recyclerManageNewsItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerManageNewsItems.setAdapter(adapter);
    }

    private void loadNews() {
        binding.progressBarManageNews.setVisibility(View.VISIBLE);
        db.collection("newsFeed").orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, e) -> {
                    binding.progressBarManageNews.setVisibility(View.GONE);
                    binding.swipeRefreshLayoutManageNews.setRefreshing(false);
                    if (value != null) {
                        newsList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            NewsItem item = doc.toObject(NewsItem.class);
                            if (item != null) {
                                item.setId(doc.getId()); // CRITICAL: Set Firestore document ID
                                newsList.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.layoutEmptyNews.setVisibility(newsList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void confirmAndDeleteNews(NewsItem item) {
        if (item.getId() == null) {
            Toast.makeText(this, "Error: Cannot delete - missing document ID", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete News")
                .setMessage("Are you sure you want to delete \"" + (item.getTitle() != null ? item.getTitle() : "this item") + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("newsFeed").document(item.getId()).delete()
                            .addOnSuccessListener(v -> Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(ex -> Toast.makeText(this, "Delete failed: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
