# FIRESTORE & STORAGE PERMISSIONS AUDIT & FIX

## Critical Issues Found

Your current Firestore rules have **DUPLICATE RULES** that conflict with each other:
1. **Detailed rules** at the top (conversations, chat, users, materials, etc.)
2. **Conflicting generic rules** at the bottom that override everything
3. **Result**: All operations fail with PERMISSION_DENIED

### The Problem in Your Rules
```firestore
// These are your good rules at top
match /reviews/{reviewId} {
  allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
}

// Then at the BOTTOM, you have a catch-all that denies everything:
match /{document=**} {
  allow read, write: if false;  // <-- THIS DENIES EVERYTHING FIRST
}

// This generic allow rule comes AFTER the deny, so it never executes:
match /{document=**} {
  allow read: if request.auth != null;  // <-- DEAD CODE, never reached
}
```

Firestore rules **stop at the FIRST match**, so the catch-all `allow if false` blocks everything before your good rules can execute.

## Solution: Reorganized Firestore Rules

Replace your entire `firestore.rules` file with this corrected version:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // ==================== HELPER FUNCTIONS ====================
    function isSignedIn() {
      return request.auth != null;
    }

    function isAdmin() {
      return isSignedIn() && 
             exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true;
    }

    // ==================== CONVERSATIONS (CHAT) ====================
    match /conversations/{conversationId} {
      allow read: if isSignedIn() && request.auth.uid in resource.data.participantIds;
      allow create: if isSignedIn() && request.auth.uid in request.resource.data.participantIds;
      allow update: if isSignedIn() && request.auth.uid in resource.data.participantIds;
      allow delete: if isSignedIn() && request.auth.uid in resource.data.participantIds;

      match /messages/{messageId} {
        allow read: if isSignedIn() && request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participantIds;
        allow create: if isSignedIn() && request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participantIds && request.resource.data.senderId == request.auth.uid;
        allow update, delete: if isSignedIn() && resource.data.senderId == request.auth.uid;
      }
    }

    // ==================== CHAT CHANNELS (LEGACY) ====================
    match /chatChannels/{channelId} {
      allow read: if isSignedIn() && request.auth.uid in resource.data.participantIds;
      allow create: if isSignedIn() && request.auth.uid in request.resource.data.participantIds;
      allow update: if isSignedIn() && request.auth.uid in resource.data.participantIds;
      allow delete: if false;

      match /messages/{messageId} {
        allow read: if isSignedIn() && get(/databases/$(database)/documents/chatChannels/$(channelId)).data.participantIds.hasAny([request.auth.uid]);
        allow write: if isSignedIn() && get(/databases/$(database)/documents/chatChannels/$(channelId)).data.participantIds.hasAny([request.auth.uid]);
      }
    }

    // ==================== USERS ====================
    match /users/{userId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn() && userId == request.auth.uid;
      allow update: if isSignedIn() && userId == request.auth.uid && (!request.resource.data.diff(resource.data).affectedKeys().hasAny(['isAdmin']) || request.resource.data.isAdmin == false);
      allow delete: if false;
    }

    // ==================== REVIEWS ====================
    match /reviews/{reviewId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn() && request.resource.data.userId == request.auth.uid;
      allow update: if isSignedIn() && resource.data.userId == request.auth.uid;
      allow delete: if isSignedIn() && resource.data.userId == request.auth.uid;
    }

    // ==================== MATERIALS ====================
    match /materials/{materialId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn() && request.resource.data.uploadedBy == request.auth.uid;
      allow update, delete: if isSignedIn() && (resource.data.uploadedBy == request.auth.uid || isAdmin());
    }

    match /studyMaterials/{materialId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn() && request.resource.data.uploaderUid == request.auth.uid;
      allow update, delete: if isSignedIn() && (resource.data.uploaderUid == request.auth.uid || isAdmin());
    }

    // ==================== TUTORS ====================
    match /tutors/{tutorId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn() && (tutorId == request.auth.uid || isAdmin());
      allow update: if isSignedIn() && (tutorId == request.auth.uid || isAdmin());
      allow delete: if isAdmin();
    }

    // ==================== NEWS FEED ====================
    match /newsFeed/{newsId} {
      allow read: if isSignedIn();
      allow create, update, delete: if isAdmin();
    }

    // ==================== HELP REQUESTS ====================
    match /helpRequests/{requestId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn() && request.resource.data.userId == request.auth.uid;
      allow update, delete: if isSignedIn() && (resource.data.userId == request.auth.uid || isAdmin());
    }

    // ==================== APP SETTINGS ====================
    match /appSettings/{document=**} {
      allow read: if isSignedIn();
      allow create, update, delete: if isAdmin();
    }

    // ==================== AI CHAT ====================
    match /aiChat/{userId}/{messageId} {
      allow read: if isSignedIn() && userId == request.auth.uid;
      allow create: if isSignedIn() && userId == request.auth.uid && request.resource.data.userId == request.auth.uid;
      allow update, delete: if isSignedIn() && userId == request.auth.uid;
    }

    // ==================== NOTIFICATIONS ====================
    match /notifications/{userId}/{notificationId} {
      allow read: if isSignedIn() && userId == request.auth.uid;
      allow create: if isSignedIn();
      allow update, delete: if isSignedIn() && userId == request.auth.uid;
    }

    // ==================== USER ACTIVITY ====================
    match /userActivity/{userId}/{activityId} {
      allow read: if isSignedIn() && userId == request.auth.uid;
      allow create: if isSignedIn();
      allow update, delete: if false;
    }

    // ==================== DEFAULT DENY ====================
    // MUST BE LAST: Deny everything not explicitly allowed above
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

## How to Apply These Rules

1. Go to **Firebase Console** → Your Project
2. Navigate to **Firestore Database** → **Rules** tab
3. **Select ALL** the existing rules (Ctrl+A / Cmd+A)
4. **Delete** them
5. **Paste** the corrected rules above
6. Click **Publish**

## For Image/File Uploads (Cloud Storage)

You also need **separate rules for Cloud Storage**. Create/update `storage.rules`:

```firestore
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // User uploads folder - user can read/write their own files
    match /users/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }

    // Profile pictures
    match /profilePictures/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }

    // Audio recordings
    match /audioRecordings/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }

    // Material attachments
    match /materials/{materialId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }

    // Review images
    match /reviews/{reviewId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }

    // Default deny
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

### How to Apply Storage Rules

1. Go to **Firebase Console** → Your Project
2. Navigate to **Storage** → **Rules** tab
3. Replace with the rules above
4. Click **Publish**

## Permissions Checklist After Applying Rules

- ✅ **Send Messages** - Requires conversation in `participantIds`
- ✅ **Upload Images** - Requires `users/{userId}/` storage path access
- ✅ **Audio Recording** - Requires `audioRecordings/{userId}/` path access
- ✅ **Write Reviews** - Requires `userId == request.auth.uid` in review data
- ✅ **Upload Materials** - Requires `uploadedBy == request.auth.uid` in materials

## Testing the Fix

After applying rules:

1. **Test Message Sending**:
   - Open a conversation
   - Send a message
   - Should succeed without PERMISSION_DENIED

2. **Test Review Writing**:
   - Go to tutor profile
   - Click "Write Review"
   - Submit review
   - Should succeed

3. **Test Image Upload**:
   - Upload profile picture or material
   - Should succeed without PERMISSION_DENIED

4. **Test Audio Recording**:
   - Record audio and send
   - Should succeed

## Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| Can't create reviews | `userId` field missing from review object | Verify ReviewsBottomSheetFragment sets `review.setUserId()` |
| Can't send messages | Not in `participantIds` | Ensure conversation creation adds user to participants |
| Can't upload images | Storage path mismatch | Use `users/{uid}/filename` structure in code |
| Can't record audio | Storage rules missing audio path | Add `audioRecordings/{uid}/` rule |

## Next Steps

1. **Apply the Firestore rules** above
2. **Apply the Storage rules** above
3. **Test each feature** (messages, reviews, uploads, audio)
4. **Check logcat** for any remaining PERMISSION_DENIED errors
5. **Share error details** if any persist

## Important Notes

- The **order of rules matters** - more specific rules come first
- **Default deny at bottom** is essential for security
- **Storage rules are separate** from Firestore rules
- **Remove duplicate rules** - you had 3 conflicting rules blocks

---

**Your previous issue**: You had good detailed rules, but they were overridden by a catch-all `allow if false` rule at the bottom, followed by unreachable rules trying to allow access. This corrected version removes duplicates and puts the catch-all at the very end.
