# BookUp Notifications System - Complete Integration Guide

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Notification Flow                         │
└─────────────────────────────────────────────────────────────┘

1. Tutor Action (Android App)
   ↓
   Tutor clicks "Accept" or "Reject" on booking
   └─→ BookingAdapter.updateBookingStatus()
       └─→ Writes to /bookings/{bookingId}
       └─→ Writes to /notifications/{notificationId}

2. Cloud Function (Firebase Backend)
   ↓
   onCreate trigger for /notifications
   └─→ Reads notification document
   └─→ Fetches student's device tokens
   └─→ Sends FCM multicast message

3. FCM Delivery (Google Cloud)
   ↓
   Routes message to student's device(s)
   └─→ Delivered to Android app
   
4. Student App (Android)
   ↓
   NotificationListener detects new notification
   └─→ NotificationService.showNotification()
   └─→ Display local notification

5. User Experience
   ↓
   Student sees notification in notification tray
   └─→ Tap to view booking status update
```

## Component Breakdown

### 1. Cloud Function (`functions/src/index.ts`)

**Purpose:** Trigger FCM delivery on Firestore notification document creation

**Key Functions:**
- `sendNotificationOnBookingStatusChange()` - Main function
  - Triggered: When document added to `/notifications/{docId}`
  - Reads: Notification document fields (toUserId, status, bookingId)
  - Fetches: Student's device tokens from `users/{studentId}.deviceTokens`
  - Sends: FCM multicast to all tokens
  - Logs: Success/failure counts

**Deployment:**
```bash
cd functions
npm install
firebase deploy --only functions
```

### 2. NotificationService (`app/src/main/java/com/example/bookup/notifications/NotificationService.java`)

**Purpose:** Display local notifications on Android device

**Key Methods:**
- `initializeNotificationChannels(Context)` - Create notification channels (Android 8.0+)
- `showNotification(Context, title, message, id)` - Display generic notification
- `showBookingNotification(Context, status, subject)` - Display booking-specific notification

**Usage in HomePageActivity:**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    NotificationService.initializeNotificationChannels(this); // Call once on app startup
}
```

### 3. NotificationListener (`app/src/main/java/com/example/bookup/notifications/NotificationListener.java`)

**Purpose:** Real-time listener for student's notification documents in Firestore

**Key Methods:**
- `startListening()` - Begin listening for new notifications
- `stopListening()` - Clean up listener (prevents memory leaks)

**Usage in HomePageActivity or MainActivity:**
```java
private NotificationListener notificationListener;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    NotificationService.initializeNotificationChannels(this);
    notificationListener = new NotificationListener(this);
    notificationListener.startListening();
}

@Override
protected void onDestroy() {
    if (notificationListener != null) {
        notificationListener.stopListening();
    }
    super.onDestroy();
}
```

### 4. BookingAdapter (Existing - Enhanced)

**What It Does:**
- User interaction: Tutor clicks "Accept" or "Reject"
- Firestore write: Updates `/bookings/{bookingId}` with new status
- **Notification write:** Creates doc at `/notifications/{notificationId}`
- This write triggers the Cloud Function

**Current Code Location:**
`app/src/main/java/com/example/bookup/adapters/BookingAdapter.java`

**Key Code Section:**
```java
private void updateBookingStatus(String bookingId, String newStatus) {
    Map<String, Object> notification = new HashMap<>();
    notification.put("toUserId", booking.getStudentId());
    notification.put("fromUserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
    notification.put("type", "booking_status_changed");
    notification.put("status", newStatus);
    notification.put("bookingId", bookingId);
    notification.put("createdAt", FieldValue.serverTimestamp());
    
    // This write triggers the Cloud Function
    FirebaseFirestore.getInstance()
        .collection("notifications")
        .add(notification);
}
```

### 5. Device Token Management

**On App Startup (MainActivity or Login):**

```java
private void saveDeviceTokenToFirestore() {
    FirebaseMessaging.getInstance().getToken()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("deviceTokens", FieldValue.arrayUnion(token))
                    .addOnSuccessListener(v -> Log.d(TAG, "Device token saved"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save token", e));
            }
        });
}
```

**Firestore Structure:**
```
/users/{userId}
├── name: "John Student"
├── email: "john@example.com"
├── rating: 4.5
├── deviceTokens: [
│   "token1_abc123...",
│   "token2_def456...",
│   "token3_ghi789..."
│ ]
```

## Setup Checklist

### Step 1: Deploy Cloud Function
- [ ] Navigate to `functions/` directory
- [ ] Run `npm install` to install dependencies
- [ ] Run `firebase deploy --only functions`
- [ ] Verify in Firebase Console → Functions
- [ ] Check logs for successful deployment

### Step 2: Update Android App - Device Token Saving

**File to Modify:** `MainActivity.java` or `SplashActivity.java`

**Add this code in `onCreate()`:**
```java
private void initializeDeviceToken() {
    FirebaseMessaging.getInstance().getToken()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                saveTokenToFirestore(token);
            } else {
                Log.e(TAG, "Failed to get FCM token", task.getException());
            }
        });
}

private void saveTokenToFirestore(String token) {
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    if (userId != null) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update("deviceTokens", FieldValue.arrayUnion(token))
            .addOnFailureListener(e -> Log.e(TAG, "Error saving token", e));
    }
}
```

### Step 3: Update Android App - Notification Initialization

**File to Modify:** `HomePageActivity.java` or `MainActivity.java`

**Add this in `onCreate()`:**
```java
// Initialize notification channels (required for Android 8.0+)
NotificationService.initializeNotificationChannels(this);

// Start listening for real-time notifications
notificationListener = new NotificationListener(this);
notificationListener.startListening();
```

**Add cleanup in `onDestroy()`:**
```java
@Override
protected void onDestroy() {
    if (notificationListener != null) {
        notificationListener.stopListening();
    }
    super.onDestroy();
}
```

### Step 4: Verify Firestore Rules

**File:** `firestore.rules`

**Ensure notifications collection is readable/writable:**
```javascript
match /notifications/{document=**} {
  allow read: if isSignedIn();
  allow create: if isSignedIn();
  allow update, delete: if isSignedIn();
}
```

### Step 5: Build and Test

```bash
# Build the app
./gradlew assembleDebug

# Run unit tests (if available)
./gradlew testDebugUnitTest

# Run UI tests (requires emulator)
./gradlew connectedAndroidTest
```

## Testing the Full Flow

### Manual Test (Requires 2 Devices/Emulators)

**Device 1: Tutor**
1. Log in as a tutor
2. Navigate to "View Booking Requests"
3. Find a pending booking
4. Click "Accept"
5. Observe the Firebase Console logs for function execution

**Device 2: Student**
1. Ensure NotificationListener is active
2. Wait for notification from Device 1
3. Verify notification appears in notification tray
4. Tap notification to verify it navigates to booking details

**Logs to Check:**

*Cloud Function logs:*
```
firebase functions:log
> Sent 1 notifications.
```

*Android logs:*
```
adb logcat | grep BookUp
D/NotificationService: Showing notification: Booking Accepted!
```

### Automated Test (Espresso UI Test)

**File:** `app/src/androidTest/java/com/example/bookup/ui/BookingAndReviewUITest.java`

**Run tests:**
```bash
./gradlew connectedAndroidTest
```

**Test cases included:**
1. `testTutorCanAcceptBooking()` - Accept flow
2. `testTutorCanRejectBooking()` - Reject flow
3. `testStudentCanSubmitReviewAndSeeRatingUpdate()` - Review + rating
4. `testEmptyStateForNoBookings()` - Edge case
5. `testNotificationDisplaysOnBookingStatusChange()` - Notification verification

## Troubleshooting

### Issue 1: Notifications Not Appearing

**Symptoms:** User accepts booking, but student doesn't see notification

**Debugging Steps:**
1. Check Cloud Function logs:
   ```bash
   firebase functions:log | grep sendNotificationOnBookingStatusChange
   ```
2. Check if notification document was created:
   - Open Firebase Console → Firestore
   - Look for document in `/notifications` collection
3. Verify student's device tokens:
   - Check `/users/{studentId}.deviceTokens` array
   - Should have at least one token

**Common Causes:**
- Student not logged in (can't save device token)
- Student didn't accept notification permissions
- Device token is stale

**Fix:**
- Prompt user to enable notifications when they first login
- Implement token refresh (Firebase SDK does this automatically)

### Issue 2: Cloud Function Errors

**Check logs:**
```bash
firebase functions:log --limit 100
```

**Common errors:**
- `"toUserId" not found` → notification doc missing field
- `User not found` → student ID doesn't exist in users collection
- `No device tokens` → student hasn't opened app yet

### Issue 3: App Crashes After Integration

**Cause:** Missing dependencies or import errors

**Check:**
```bash
./gradlew assembleDebug 2>&1 | grep -i error
```

**Fix:**
- Ensure all imports are present in NotificationService.java
- Verify gradle dependencies include firebase-messaging

## Performance Considerations

### Function Cold Starts
- First invocation may take 3-5 seconds
- Subsequent calls within ~15 minutes: <500ms

### Firestore Reads/Writes
- Each notification write: 1 Firestore write
- Each function execution: 1 read (fetch user + device tokens)
- **Cost:** Minimal (includes free tier)

### Device Token Management
- Store tokens per user in array (auto-deduplicates)
- Tokens expire if app not used for 60+ days
- Firebase SDK auto-refreshes on app startup

## Security Considerations

### Firestore Rules
Ensure only authenticated users can:
- Create notifications (only tutors for other users)
- Read their own notifications
- NOT modify others' notifications

**Recommended Rules:**
```javascript
match /notifications/{document=**} {
  allow read: if isSignedIn() && 
              (get(/databases/$(database)/documents/notifications/$(document)).data.toUserId == request.auth.uid ||
               get(/databases/$(database)/documents/notifications/$(document)).data.fromUserId == request.auth.uid);
  allow create: if isSignedIn();
  allow update, delete: if isSignedIn() && 
                        get(/databases/$(database)/documents/notifications/$(document)).data.fromUserId == request.auth.uid;
}
```

### Cloud Function Security
- Function is triggered by Firestore (internal)
- No public HTTP endpoint exposed
- All operations scoped to authenticated user

## Next Steps After Deployment

1. **Monitor Function Health**
   - Check Firebase Console daily for errors
   - Set up alerts for function failures

2. **User Feedback**
   - Ask users to report missing notifications
   - Collect feedback on notification timing

3. **Optimize**
   - Add rich notification with images/buttons
   - Implement notification grouping
   - Add notification dismissal tracking

4. **Expand**
   - Add notifications for new reviews
   - Add notifications for chat messages
   - Add notifications for new student signups

## References

- [Cloud Functions Documentation](https://firebase.google.com/docs/functions)
- [FCM Android Documentation](https://firebase.google.com/docs/cloud-messaging/android/client)
- [Firestore Triggers](https://firebase.google.com/docs/functions/firestore-events)
- [Notification Channels (Android)](https://developer.android.com/training/notify-user/channels)

---

**Last Updated:** 2024
**Status:** 🟢 Ready for Deployment
