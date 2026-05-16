package com.example.bookup.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookup.R;
import com.example.bookup.adapters.StudyMaterialAdapter;
import com.example.bookup.models.StudyMaterial;
import com.example.bookup.utils.FirebaseErrorHandler;
import com.example.bookup.utils.PaginationHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ManageMaterialsActivity extends AppCompatActivity {
    private static final String TAG = "ManageMaterialsActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseErrorHandler errorHandler;

    private RecyclerView recyclerMaterials;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExtendedFloatingActionButton fabRefresh;
    private View layoutEmptyMaterials;

    private StudyMaterialAdapter adapter;
    private PaginationHelper paginationHelper;
    private List<StudyMaterial> materialsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_materials);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        errorHandler = new FirebaseErrorHandler();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Authentication required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkAdminStatus();
        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        loadMaterials();
    }

    private void checkAdminStatus() {
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");
                        if (isAdmin == null || !isAdmin) {
                            Toast.makeText(this, "Access denied: Not an admin", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "User profile not found. Access denied.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check admin status", e);
                    finish();
                });
    }

    private void initViews() {
        recyclerMaterials = findViewById(R.id.recycler_manage_materials);
        progressBar = findViewById(R.id.progress_bar_manage_materials);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_manage_materials);
        fabRefresh = findViewById(R.id.fab_refresh_materials);
        layoutEmptyMaterials = findViewById(R.id.layout_empty_materials);

        if (fabRefresh != null) {
            fabRefresh.setOnClickListener(v -> loadMaterials());
        }
    }

    private void setupRecyclerView() {
        materialsList = new ArrayList<>();
        adapter = new StudyMaterialAdapter(materialsList);
        recyclerMaterials.setLayoutManager(new LinearLayoutManager(this));
        recyclerMaterials.setAdapter(adapter);
        adapter.setOnDeleteClickListener(material -> confirmDeleteMaterial(material));
        paginationHelper = new PaginationHelper();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadMaterials);
    }

    private void loadMaterials() {
        setLoading(true);

        db.collection("studyMaterials")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PaginationHelper.DEFAULT_PAGE_SIZE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    materialsList.clear();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        updateEmptyState(true);
                        setLoading(false);
                        return;
                    }

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        try {
                            StudyMaterial material = doc.toObject(StudyMaterial.class);
                            if (material != null) {
                                material.setId(doc.getId());
                                materialsList.add(material);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Failed to parse material document", e);
                        }
                    }

                    updateEmptyState(materialsList.isEmpty());
                    adapter.notifyDataSetChanged();
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load materials", e);
                    setLoading(false);
                    Toast.makeText(this, "Error loading materials", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDeleteMaterial(StudyMaterial material) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Material")
                .setMessage("Are you sure you want to delete \"" + material.getTitle() + "\"?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteMaterial(material))
                .show();
    }

    private void deleteMaterial(StudyMaterial material) {
        setLoading(true);

        db.collection("studyMaterials").document(material.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Material deleted successfully", Toast.LENGTH_SHORT).show();
                    materialsList.remove(material);
                    adapter.notifyDataSetChanged();
                    updateEmptyState(materialsList.isEmpty());
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete material", e);
                    setLoading(false);
                    Toast.makeText(this, "Error deleting material", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerMaterials.setVisibility(View.GONE);
            layoutEmptyMaterials.setVisibility(View.VISIBLE);
        } else {
            recyclerMaterials.setVisibility(View.VISIBLE);
            layoutEmptyMaterials.setVisibility(View.GONE);
        }
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setEnabled(!isLoading);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        materialsList = null;
        adapter = null;
    }
}
