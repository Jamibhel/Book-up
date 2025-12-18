package com.example.bookup.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bookup.R;
import com.example.bookup.adapters.UserAdapter;
import com.example.bookup.models.User;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {
    private static final String TAG = "ManageUsersActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI Components
    private RecyclerView recyclerUsers;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CircularProgressIndicator progressIndicator;
    private View emptyStateView;

    // Adapter
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_manage_users);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.manage_users_title);
        }

        // Check authentication and admin status
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to manage users.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        checkAdminStatus();

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        fetchUsers(); // Initial fetch
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews() {
        recyclerUsers = findViewById(R.id.recycler_users);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_users);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyStateView = findViewById(R.id.empty_state_view);
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(new ArrayList<>(), this::showUserOptionsDialog);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(userAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchUsers);
    }

    private void checkAdminStatus() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
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
                Toast.makeText(this, "Error checking admin status.", Toast.LENGTH_LONG).show();
                finish();
            });
    }

    private void fetchUsers() {
        setLoading(true);
        db.collection("users")
            .get()
            .addOnCompleteListener(task -> {
                setLoading(false);
                swipeRefreshLayout.setRefreshing(false);

                if (task.isSuccessful()) {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        user.setId(document.getId());
                        users.add(user);
                    }
                    updateUsersList(users);
                } else {
                    Log.e(TAG, "Error fetching users", task.getException());
                    Toast.makeText(this, "Error fetching users: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void updateUsersList(List<User> users) {
        userAdapter.updateUsers(users);
        updateEmptyState(users.isEmpty());
    }

    private void showUserOptionsDialog(User user) {
        String[] options;
        if (user.isBlocked()) {
            options = new String[]{"Unblock User", "Toggle Admin Status", "Delete User"};
        } else {
            options = new String[]{"Block User", "Toggle Admin Status", "Delete User"};
        }

        new AlertDialog.Builder(this)
            .setTitle("Manage User: " + user.getDisplayName())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Block/Unblock
                        toggleUserBlock(user);
                        break;
                    case 1: // Toggle Admin
                        toggleAdminStatus(user);
                        break;
                    case 2: // Delete
                        confirmDeleteUser(user);
                        break;
                }
            })
            .show();
    }

    private void toggleUserBlock(User user) {
        db.collection("users").document(user.getId())
            .update("blocked", !user.isBlocked())
            .addOnSuccessListener(aVoid -> {
                String message = user.isBlocked() ? "User unblocked" : "User blocked";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                fetchUsers();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error toggling user block status", e);
                Toast.makeText(this, "Error updating user status", Toast.LENGTH_SHORT).show();
            });
    }

    private void toggleAdminStatus(User user) {
        if (user.getId().equals(mAuth.getCurrentUser().getUid())) {
            Toast.makeText(this, "Cannot modify your own admin status", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(user.getId())
            .update("isAdmin", !user.isAdmin())
            .addOnSuccessListener(aVoid -> {
                String message = user.isAdmin() ? "Admin privileges removed" : "Admin privileges granted";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                fetchUsers();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error toggling admin status", e);
                Toast.makeText(this, "Error updating admin status", Toast.LENGTH_SHORT).show();
            });
    }

    private void confirmDeleteUser(User user) {
        if (user.getId().equals(mAuth.getCurrentUser().getUid())) {
            Toast.makeText(this, "Cannot delete your own account", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete " + user.getDisplayName() + "? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteUser(User user) {
        db.collection("users").document(user.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                fetchUsers();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting user", e);
                Toast.makeText(this, "Error deleting user", Toast.LENGTH_SHORT).show();
            });
    }

    private void setLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerUsers.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
