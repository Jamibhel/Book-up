# Firestore Security Rules Fix

## Problem
The app is getting PERMISSION_DENIED errors when trying to read reviews:
```
PERMISSION_DENIED: Missing or insufficient permissions.
```

This occurs when TutorDetailsActivity tries to load reviews from the "reviews" collection.

## Solution

Your Firestore security rules need to allow authenticated users to read reviews. Go to Firebase Console and update your rules:

### Firebase Console Path
1. Go to **Firebase Console** → Your Project
2. Navigate to **Firestore Database** → **Rules** tab
3. Replace the rules with the following:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Allow authenticated users to read all collections
    match /{document=**} {
      allow read: if request.auth != null;
    }
    
    // Users collection - allow users to read/write their own data
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    
    // Reviews collection - allow authenticated users to read
    match /reviews/{reviewId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update: if request.auth.uid == resource.data.userId;
      allow delete: if request.auth.uid == resource.data.userId;
    }
    
    // Materials collection - allow authenticated users to read
    match /materials/{materialId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.uploadedBy == request.auth.uid;
      allow update: if request.auth.uid == resource.data.uploadedBy;
      allow delete: if request.auth.uid == resource.data.uploadedBy;
    }
    
    // Tutors collection - allow authenticated users to read
    match /tutors/{tutorId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == tutorId;
    }
  }
}
```

4. Click **Publish** to apply the rules

## What These Rules Do

- ✅ Allows **authenticated users** to **read** reviews, materials, tutors, and user data
- ✅ Allows users to **create reviews** if they're authenticated
- ✅ Allows users to **edit/delete** only their own reviews and materials
- ✅ Prevents **unauthenticated** access to any collection
- ✅ Prevents users from editing other users' data

## Testing

After updating the rules:
1. Run the app
2. Log in with your test account
3. Navigate to a tutor profile
4. The "Write Review" button should now display reviews correctly
5. The error "PERMISSION_DENIED" should no longer appear in logcat

## Alternative (Permissive - Development Only)

For **development/testing only**, you can use:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

⚠️ **DO NOT** use this in production - always properly restrict permissions!

## References
- [Firebase Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Common Rules Patterns](https://firebase.google.com/docs/firestore/security/rules-structure)
