# Cloud Functions Setup for BookUp Notifications

## Overview

This guide explains how to deploy the Cloud Function that sends FCM (Firebase Cloud Messaging) notifications when booking status changes.

## Prerequisites

1. Firebase project initialized with CLI (`firebase init`)
2. Node.js 18+ installed
3. TypeScript compiler (npm will install)
4. Admin SDK credentials configured

## Files

- `functions/src/index.ts` - Cloud Function source code
- `functions/package.json` - Dependencies and scripts

## Function Details

### Function Name
`sendNotificationOnBookingStatusChange`

### Trigger
- **Type:** Firestore onCreate
- **Collection Path:** `notifications/{docId}`
- **When:** A new notification document is created

### Behavior
1. Reads notification document from Firestore
2. Fetches target user's device tokens from `users/{userId}` document
3. Constructs FCM message with title, body, and metadata
4. Sends multicast message to all device tokens
5. Logs success/failure counts

## Required Firestore Data Structure

### Notifications Collection
```
/notifications/{notificationId}
├── toUserId: string (student's UID)
├── fromUserId: string (tutor's UID)
├── type: string ("booking_status_changed")
├── status: string ("confirmed" | "cancelled")
├── bookingId: string (booking document ID)
├── message: string (user-facing message)
└── createdAt: timestamp
```

### Users Collection (must have deviceTokens)
```
/users/{userId}
├── name: string
├── email: string
├── ...other fields...
└── deviceTokens: array<string> (FCM registration tokens)
```

## Deployment Steps

### 1. Install Dependencies
```bash
cd functions
npm install
```

### 2. Build TypeScript
```bash
cd functions
npm run build
# or just deploy (Firebase CLI auto-builds)
```

### 3. Deploy Function
```bash
firebase deploy --only functions
```

**Output should show:**
```
✔  functions[sendNotificationOnBookingStatusChange(us-central1)]: Successful create operation.
Deploy complete!
```

### 4. Verify Deployment
```bash
firebase functions:log
```

Check logs in Firebase Console:
1. Go to Firebase Console → Project → Functions
2. Click `sendNotificationOnBookingStatusChange`
3. View logs in "Logs" tab

## Local Testing with Emulator

### Start Emulator
```bash
firebase emulators:start --import=./emulator-data
```

### Test Function
1. Use Firebase Emulator UI (localhost:4000)
2. Manually create a document in `notifications` collection
3. Function should trigger automatically
4. Check function logs in emulator terminal

## Environment Variables (if needed)

Currently, no additional environment variables are required. The function uses:
- `process.env.GCLOUD_PROJECT` (automatically set)
- Firebase Admin SDK credentials (from `GOOGLE_APPLICATION_CREDENTIALS`)

## Error Handling

### Common Issues

**1. Device Tokens Empty**
```
User {userId} has no device tokens. Skipping.
```
**Solution:** Ensure app saves device token when user logs in (see NotificationSetup.md)

**2. Missing Fields in Notification Doc**
```
Missing required fields. Skipping notification.
```
**Solution:** Ensure `toUserId` and `type` are populated when creating notification

**3. User Not Found**
```
User {userId} not found. Skipping.
```
**Solution:** Verify the user exists in Firestore before creating notification

**4. FCM Token Invalid**
```
Failed to send {n} notifications.
```
**Solution:** Device tokens may be stale. App should refresh token on startup

## Integration with Android App

### 1. Save Device Token on Login (MainActivity.java)

```java
FirebaseMessaging.getInstance().getToken()
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            String token = task.getResult();
            saveDeviceTokenToFirestore(FirebaseAuth.getInstance().getCurrentUser().getUid(), token);
        }
    });

private void saveDeviceTokenToFirestore(String userId, String token) {
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .update("deviceTokens", FieldValue.arrayUnion(token));
}
```

### 2. Start Notification Listener (HomePageActivity.java)

```java
private NotificationListener notificationListener;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    
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

### 3. BookingAdapter Triggers Notification (existing code)

When tutor accepts/rejects:
```java
private void updateBookingStatus(String bookingId, String newStatus) {
    // Update booking status
    booking.setStatus(newStatus);
    
    // Create notification document for student
    Map<String, Object> notification = new HashMap<>();
    notification.put("toUserId", booking.getStudentId());
    notification.put("fromUserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
    notification.put("type", "booking_status_changed");
    notification.put("status", newStatus);
    notification.put("bookingId", bookingId);
    notification.put("createdAt", FieldValue.serverTimestamp());
    
    // This write triggers the Cloud Function!
    FirebaseFirestore.getInstance()
        .collection("notifications")
        .add(notification);
}
```

## Monitoring in Production

### Firebase Console
1. **Functions tab:** View deployments, versions, and metrics
2. **Logs tab:** Real-time function execution logs
3. **Error reporting:** Automatic error tracking

### Metrics to Monitor
- **Invocations:** Total function calls
- **Errors:** Failed function executions
- **Duration:** Execution time
- **Execution count by status:** Success/failure rates

## Rollback

If issues occur:

```bash
# List deployed versions
firebase functions:list

# Rollback to previous version
firebase deploy --only functions:sendNotificationOnBookingStatusChange
```

## Next Steps

1. ✅ Deploy Cloud Function
2. ✅ Update Android app to save device tokens on login
3. ✅ Start NotificationListener in HomePageActivity
4. ✅ Run UI tests to verify full notification flow
5. ✅ Monitor logs for 24 hours post-deployment

## References

- [Firebase Cloud Functions Documentation](https://firebase.google.com/docs/functions)
- [Firebase Cloud Messaging (FCM)](https://firebase.google.com/docs/cloud-messaging)
- [Firestore Triggers](https://firebase.google.com/docs/functions/firestore-events)
