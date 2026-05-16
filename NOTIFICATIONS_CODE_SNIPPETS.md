# BookUp Notifications - Code Snippets for Integration

Use these code snippets to quickly integrate the notification system into your existing activities.

## Snippet 1: Device Token Saving (MainActivity or SplashActivity)

**Location:** `MainActivity.java` or `SplashActivity.java`

**Replace/Add in onCreate() method:**
```java
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import android.util.Log;

private static final String TAG = "DeviceTokenSetup";

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    // ... existing code ...
    
    // Initialize device token (call this after user is logged in)
    initializeDeviceToken();
}

private void initializeDeviceToken() {
    // Get FCM token from Firebase
    FirebaseMessaging.getInstance().getToken()
        .addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "getToken failed", task.getException());
                return;
            }

            // Get new FCM token
            String token = task.getResult();
            
            // Get current user
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                Log.d(TAG, "User not logged in yet");
                return;
            }

            // Save token to Firestore
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("deviceTokens", FieldValue.arrayUnion(token))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Device token saved to Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save device token", e);
                    // Retry logic can be added here
                });
        });
}
```

---

## Snippet 2: Notification Initialization (HomePageActivity)

**Location:** `HomePageActivity.java` (or wherever user goes after login)

**Add at class level:**
```java
private NotificationListener notificationListener;
private static final String TAG = "NotificationSetup";
```

**Add in onCreate():**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_page);
    
    // ... existing code ...
    
    // Initialize notification channels (required for Android 8.0+)
    NotificationService.initializeNotificationChannels(this);
    
    // Start listening for real-time notifications
    notificationListener = new NotificationListener(this);
    notificationListener.startListening();
    
    Log.d(TAG, "Notification system initialized");
}
```

**Add cleanup in onDestroy():**
```java
@Override
protected void onDestroy() {
    // Stop listening for notifications to prevent memory leaks
    if (notificationListener != null) {
        notificationListener.stopListening();
        Log.d(TAG, "Notification listener stopped");
    }
    
    super.onDestroy();
}
```

---

## Snippet 3: Handle Device Token Refresh (Optional - Advanced)

**Add to MainActivity if you want to refresh token on app startup:**

```java
import com.google.firebase.messaging.FirebaseMessaging;

// Optional: Listen for token refresh
private void initializeTokenRefreshListener() {
    FirebaseMessaging.getInstance().getToken()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String newToken = task.getResult();
                Log.d(TAG, "FCM Token: " + newToken);
                // Token is automatically saved by initializeDeviceToken()
            }
        });
    
    // When token is refreshed by Firebase, save it
    // (Firebase SDK handles this automatically)
}
```

---

## Snippet 4: Manual Notification Trigger (For Testing)

**Add this method to HomePageActivity for testing without waiting for tutor action:**

```java
import com.example.bookup.notifications.NotificationService;

// Test method: call from a button click to verify notifications work
private void testNotification() {
    NotificationService.showBookingNotification(
        this,
        "confirmed",
        "Mathematics"
    );
    Log.d(TAG, "Test notification displayed");
}

// Add this to a button in your layout for testing:
// android:onClick="onTestNotificationClick"

public void onTestNotificationClick(View view) {
    testNotification();
}
```

**Layout (activity_home_page.xml):**
```xml
<Button
    android:id="@+id/btn_test_notification"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Test Notification"
    android:onClick="onTestNotificationClick"
    android:visibility="gone" />
```

---

## Snippet 5: Firestore Rules Update

**File:** `firestore.rules` (in Firebase Console or local file)

**Add/Update the notifications section:**
```javascript
// Allow users to create and read their own notifications
match /notifications/{document=**} {
  allow read: if isSignedIn();
  allow create: if isSignedIn();
  allow update, delete: if isSignedIn();
}

// More restrictive version (if desired):
match /notifications/{userId}/{document=**} {
  allow read: if isSignedIn() && userId == request.auth.uid;
  allow create: if isSignedIn();
  allow delete: if isSignedIn() && userId == request.auth.uid;
}
```

**Deploy rules:**
```bash
firebase deploy --only firestore:rules
```

---

## Snippet 6: Check if App Has Notification Permission (Android 13+)

**Add to MainActivity:**

```java
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

private void requestNotificationPermission() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        // Android 13+ requires explicit permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                1001
            );
        }
    }
}

// Call in onCreate() after user logs in:
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    // ... existing code ...
    
    requestNotificationPermission();
    initializeDeviceToken();
}

// Handle permission result:
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    
    if (requestCode == 1001) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Notification permission granted");
            initializeDeviceToken();
        } else {
            Log.d(TAG, "Notification permission denied");
        }
    }
}
```

**AndroidManifest.xml:**
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

---

## Snippet 7: Verify Build.gradle Has Required Dependencies

**File:** `build.gradle` (app level)

**Ensure these dependencies exist:**
```gradle
dependencies {
    // Firebase
    implementation 'com.google.firebase:firebase-messaging:23.4.0'
    implementation 'com.google.firebase:firebase-firestore:24.10.0'
    implementation 'com.google.firebase:firebase-auth:22.3.0'
    
    // AndroidX
    implementation 'androidx.core:core:1.13.0'  // For NotificationCompat
    
    // For UI Tests
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
}
```

---

## Snippet 8: Logging for Debugging

**Add to NotificationListener or NotificationService for detailed logging:**

```java
import android.util.Log;

private static final String TAG = "BookUpNotifications";

// In NotificationListener.startListening():
@Override
public void startListening() {
    String currentUserId = auth.getCurrentUser().getUid();
    Log.d(TAG, "Starting notification listener for user: " + currentUserId);
    
    // ... rest of code ...
    
    notificationListener = firestore.collection("notifications")
            .document(currentUserId)
            .collection("messages")
            .addSnapshotListener((querySnapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Listener error: " + error.getMessage(), error);
                    return;
                }
                
                if (querySnapshot != null) {
                    Log.d(TAG, "Received " + querySnapshot.getDocumentChanges().size() + " changes");
                    
                    querySnapshot.getDocumentChanges().forEach(docChange -> {
                        Log.d(TAG, "Document change type: " + docChange.getType());
                        // ... rest of code ...
                    });
                }
            });
}
```

---

## Snippet 9: Graceful Error Handling

**Add to MainActivity for handling token save failures:**

```java
private static final int MAX_TOKEN_RETRIES = 3;
private int tokenRetryCount = 0;

private void initializeDeviceToken() {
    FirebaseMessaging.getInstance().getToken()
        .addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "getToken failed", task.getException());
                return;
            }

            String token = task.getResult();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            
            if (auth.getCurrentUser() == null) {
                // Retry after delay if user not ready
                if (tokenRetryCount < MAX_TOKEN_RETRIES) {
                    tokenRetryCount++;
                    new Handler(Looper.getMainLooper()).postDelayed(
                        this::initializeDeviceToken,
                        2000 // Retry after 2 seconds
                    );
                }
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("deviceTokens", FieldValue.arrayUnion(token))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Device token saved successfully");
                    tokenRetryCount = 0; // Reset retry count on success
                })
                .addOnFailureListener(e -> {
                    if (tokenRetryCount < MAX_TOKEN_RETRIES) {
                        tokenRetryCount++;
                        Log.w(TAG, "Token save failed, retrying... (attempt " + tokenRetryCount + ")");
                        
                        // Retry after exponential backoff
                        new Handler(Looper.getMainLooper()).postDelayed(
                            this::initializeDeviceToken,
                            (long) Math.pow(2, tokenRetryCount) * 1000
                        );
                    } else {
                        Log.e(TAG, "Failed to save device token after " + MAX_TOKEN_RETRIES + " retries", e);
                    }
                });
        });
}
```

---

## Snippet 10: View Firestore Notifications in Logcat

**Run this command to filter logs for notification events:**

```bash
# Watch for notification saves
adb logcat | grep -i "device token"

# Watch for notification displays
adb logcat | grep -i "notification"

# Watch for Firestore listener events
adb logcat | grep -i "listener"

# Combined: all notification-related logs
adb logcat | grep -E -i "(notification|token|listener)"
```

---

## Complete Integration Checklist with Snippets

- [ ] **Snippet 1** added to MainActivity (device token saving)
- [ ] **Snippet 2** added to HomePageActivity (initialization)
- [ ] **Snippet 3** (optional) added for token refresh
- [ ] **Snippet 4** (optional) added for testing
- [ ] **Snippet 5** applied to firestore.rules
- [ ] **Snippet 6** (Android 13+) added for permissions
- [ ] **Snippet 7** verified in build.gradle
- [ ] **Snippet 8** added for debugging
- [ ] **Snippet 9** (optional) added for error handling
- [ ] **Snippet 10** used to verify logs in Android Studio

---

## Quick Integration Order

1. Add Snippet 1 to MainActivity.java
2. Add Snippet 2 to HomePageActivity.java
3. Update build.gradle with Snippet 7
4. Update firestore.rules with Snippet 5
5. Deploy changes: `firebase deploy --only firestore:rules`
6. Build app: `./gradlew assembleDebug`
7. Test with Snippet 4 (manual notification test)
8. Verify logs with Snippet 10

**Total time:** ~15 minutes

---

## Reference Quick Links

| File | Location | Purpose |
|------|----------|---------|
| NotificationService.java | `app/src/main/java/com/example/bookup/notifications/` | Local notification display |
| NotificationListener.java | `app/src/main/java/com/example/bookup/notifications/` | Firestore real-time listener |
| BookingAdapter.java | `app/src/main/java/com/example/bookup/adapters/` | Creates notification documents |
| index.ts | `functions/src/` | Cloud Function (FCM sender) |

---

**Status:** 🟢 Ready to integrate. Copy snippets above into your files.
