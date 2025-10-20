package com.example.bookup;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

// Import ONLY the fragments you are now using in the bottom nav
import com.example.bookup.fragments.DashboardFragment;
import com.example.bookup.fragments.RequestsFragment; // NEW
import com.example.bookup.fragments.ChatListFragment; // NEW
import com.example.bookup.fragments.SearchFragment;   // NEW
import com.example.bookup.fragments.ProfileFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomePageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    // Added TAG for logging
    private static final String TAG = "HomePageActivity";

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

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

                if (itemId == R.id.nav_home){
                    selectedFragment = new DashboardFragment();
                    title = "Home";
                } else if (itemId == R.id.nav_requests) { // NEW ID
                    selectedFragment = new RequestsFragment();
                    title = "Requests";
                } else if (itemId == R.id.nav_chat) { // NEW ID
                    selectedFragment = new ChatListFragment();
                    title = "Chat";
                } else if (itemId == R.id.nav_search) { // NEW ID
                    selectedFragment = new SearchFragment();
                    title = "Search";
                } else if (itemId == R.id.nav_profile) {
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
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment); // Ensure R.id.fragment_container is correct
        fragmentTransaction.commit();
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
