package com.example.bookup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.bookup.R;
import com.example.bookup.notifications.NotificationListener;
import com.example.bookup.notifications.NotificationService;
import com.example.bookup.repositories.ChatRepository;
import com.example.bookup.utils.NetworkConnectivityManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomePageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;
    private NetworkConnectivityManager connectivityManager;
    private NavController navController;
    private static final String TAG = "HomePageActivity";
    private NotificationListener notificationListener;
    private final ChatRepository chatRepository = new ChatRepository();
    private com.google.firebase.firestore.ListenerRegistration callListener;

    private String lastCallId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        connectivityManager = new NetworkConnectivityManager(this);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(HomePageActivity.this, SignInActivity.class));
            finish();
            return;
        }

        updatePresence(true);
        NotificationService.initializeNotificationChannels(this);
        notificationListener = new NotificationListener(this);
        notificationListener.startListening();
        listenForCalls();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        handleIntentExtras(getIntent());
    }

    private void handleIntentExtras(Intent intent) {
        if (intent == null || navController == null) return;

        int tabIndex = intent.getIntExtra("tabIndex", -1);
        String channelId = intent.getStringExtra("channelId");
        String channelName = intent.getStringExtra("channelName");

        if (tabIndex >= 0) {
            int navItemId = -1;
            switch (tabIndex) {
                case 0: navItemId = R.id.navigation_dashboard; break;
                case 1: navItemId = R.id.navigation_community; break;
                case 2: navItemId = R.id.navigation_requests; break;
                case 3: navItemId = R.id.navigation_ai_chat; break;
                case 4: navItemId = R.id.navigation_chat; break;
                case 5: navItemId = R.id.navigation_profile; break;
            }
            if (navItemId >= 0) {
                navController.navigate(navItemId);
            }
        }

        if (channelId != null) {
            Bundle args = new Bundle();
            args.putString("channelId", channelId);
            args.putString("channelName", channelName);
            navController.navigate(R.id.chat, args);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentExtras(intent);
    }

    public void selectBottomNavItem(int itemId) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(itemId);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updatePresence(true);
        if (connectivityManager != null) {
            connectivityManager.startMonitoring((isConnected, status) -> {});
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        updatePresence(false);
        if (connectivityManager != null) {
            connectivityManager.stopMonitoring();
        }
    }

    private void updatePresence(boolean isOnline) {
        if (mAuth.getCurrentUser() != null) {
            chatRepository.updatePresence(mAuth.getCurrentUser().getUid(), isOnline);
        }
    }

    private void listenForCalls() {
        if (mAuth.getCurrentUser() == null) return;
        callListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("calls")
                .whereEqualTo("receiverId", mAuth.getUid())
                .whereEqualTo("status", "DIALING")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }
                    if (value != null && !value.isEmpty()) {
                        com.google.firebase.firestore.DocumentSnapshot doc = value.getDocuments().get(0);
                        com.example.bookup.models.Call call = doc.toObject(com.example.bookup.models.Call.class);
                        
                        // FIX: Only trigger ringing if I am NOT the one who started the call
                        if (call != null && !doc.getId().equals(lastCallId)) {
                            if (call.getCallerId() != null && call.getCallerId().equals(mAuth.getUid())) {
                                Log.d(TAG, "Outgoing call detected in listener, ignoring self-ring");
                                return;
                            }

                            lastCallId = doc.getId();
                            call.setId(doc.getId());
                            Log.d(TAG, "Incoming call detected: " + call.getId());
                            Intent intent = new Intent(this, CallActivity.class);
                            intent.putExtra(CallActivity.EXTRA_CALL, call);
                            intent.putExtra(CallActivity.EXTRA_IS_INCOMING, true);
                            startActivity(intent);
                        }
                    }
                });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_homepage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            Toast.makeText(HomePageActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomePageActivity.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            if (navController != null) {
                navController.navigate(R.id.navigation_search);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (notificationListener != null) {
            notificationListener.stopListening();
        }
        super.onDestroy();
    }
}
