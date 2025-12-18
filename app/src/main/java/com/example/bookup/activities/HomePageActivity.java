package com.example.bookup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

// Import all fragments used in bottom nav
import com.example.bookup.R;
import com.example.bookup.fragments.AIChatFragment;
import com.example.bookup.fragments.DashboardFragment;
import com.example.bookup.fragments.RequestsFragment;
import com.example.bookup.fragments.ChatListFragment;
import com.example.bookup.fragments.SearchFragment;
import com.example.bookup.fragments.ProfileFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.bookup.utils.NetworkConnectivityManager;

public class HomePageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private NetworkConnectivityManager connectivityManager;

    // Added TAG for logging
    private static final String TAG = "HomePageActivity";

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Initialize Network Connectivity Manager
        connectivityManager = new NetworkConnectivityManager(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null){
            startActivity(new Intent(HomePageActivity.this, SignInActivity.class));
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Home"); // Default title

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String title = "";

                int itemId = item.getItemId(); // Get the ID once

                if (itemId == R.id.navigation_dashboard) {
                    selectedFragment = new DashboardFragment();
                    title = "Home";
                } else if (itemId == R.id.navigation_search) {
                    selectedFragment = new SearchFragment();
                    title = "Search";
                } else if (itemId == R.id.navigation_ai_chat) {
                    selectedFragment = new AIChatFragment();
                    title = "AI Tutor";
                } else if (itemId == R.id.navigation_chat) {
                    selectedFragment = new ChatListFragment();
                    title = "Chat";
                } else if (itemId == R.id.navigation_requests) {
                    selectedFragment = new RequestsFragment();
                    title = "Requests";
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment();
                    title = "Profile";
                }

                if (selectedFragment != null){
                    // Pass the actual selectedFragment to loadFragment
                    loadFragment(selectedFragment);
                    getSupportActionBar().setTitle(title);
                    return true;
                }
                return false;
            }
        });

        //This loads the default fragment home when app is started
        if (savedInstanceState == null){
            loadFragment(new DashboardFragment());
            bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        
        // Use replace with a tag to enable proper fragment caching
        // Each fragment type gets a unique tag for retrieval
        String tag = fragment.getClass().getSimpleName();
        
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (connectivityManager != null) {
            connectivityManager.startMonitoring(this::onNetworkStateChanged);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connectivityManager != null) {
            connectivityManager.stopMonitoring();
        }
    }

    private void onNetworkStateChanged(boolean isConnected, String status) {
        // Update UI based on network state
        if (!isConnected) {
            // Show offline indicator in toolbar
            if (toolbar != null) {
                int offlineColor = ContextCompat.getColor(this, android.R.color.holo_red_light);
                toolbar.setBackgroundColor(offlineColor);
                toolbar.setTitle("Book Up - OFFLINE");
            }
        } else {
            // Restore normal toolbar with blue color
            if (toolbar != null) {
                int onlineColor = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
                toolbar.setBackgroundColor(onlineColor);
                toolbar.setTitle("Book Up");
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_homepage, menu); // Ensure this menu exists
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout){ // Ensure action_logout is in menu_homepage.xml
            mAuth.signOut();
            Toast.makeText(HomePageActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomePageActivity.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectBottomNavItem(int itemId) {
        bottomNavigationView.setSelectedItemId(itemId);
    }
}
