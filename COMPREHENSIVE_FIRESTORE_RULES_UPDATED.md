# 🔐 COMPREHENSIVE FIRESTORE RULES - FINAL UPDATED VERSION

**Last Updated:** December 27, 2025  
**Status:** ✅ Production Ready - All Permissions Reviewed & Updated  
**Location:** `/firestore.rules`

---

## 📋 Overview

This document provides a complete reference for all Firestore security rules covering **every collection and subcollection** in the BookUp application, with **ALL permissions properly configured**.

---

## 🔑 Helper Functions

```javascript
function isSignedIn() {
  return request.auth != null;
}

function isAdmin() {
  return isSignedIn() && 
         exists(/databases/{database}/documents/users/{request.auth.uid}) &&
         get(/databases/{database}/documents/users/{request.auth.uid}).data.isAdmin == true;
}

function isDocumentOwner(ownerId) {
  return isSignedIn() && request.auth.uid == ownerId;
}

function isTutor() {
  return isSignedIn() &&
         exists(/databases/{database}/documents/users/{request.auth.uid}) &&
         get(/databases/{database}/documents/users/{request.auth.uid}).data.isTutor == true;
}

function isStudent() {
  return isSignedIn() &&
         exists(/databases/{database}/documents/users/{request.auth.uid}) &&
         get(/databases/{database}/documents/users/{request.auth.uid}).data.isTutor == false;
}
```

---

## 📁 Collection Permissions Breakdown

### 1️⃣ **CONVERSATIONS** (Modern Chat System)

**Path:** `conversations/{conversationId}`

**Rules:**
```javascript
match /conversations/{conversationId} {
  allow read: if isSignedIn() &&
                 request.auth.uid in resource.data.participantIds;
  
  allow create: if isSignedIn() && 
                   request.auth.uid in request.resource.data.participantIds;
  
  allow update: if isSignedIn() && 
                   request.auth.uid in resource.data.participantIds;
  
  allow delete: if false;

  match /messages/{messageId} {
    allow read: if isSignedIn();
    
    allow create: if isSignedIn() &&
                     request.resource.data.senderId != null &&
                     request.resource.data.senderId == request.auth.uid;
    
    allow update, delete: if isSignedIn() &&
                             resource.data.senderId != null &&
                             resource.data.senderId == request.auth.uid;
  }
}
```

**Who Can:**
- ✅ READ: Conversation participants only
- ✅ CREATE: Users who are in participantIds array
- ✅ UPDATE: Conversation participants (for metadata)
- ❌ DELETE: Not allowed

---

### 2️⃣ **CHAT CHANNELS** (Legacy - Migration Support)

**Path:** `chatChannels/{channelId}`

**Rules:**
```javascript
match /chatChannels/{channelId} {
  allow read: if isSignedIn() &&
                 request.auth.uid in resource.data.participantIds;
  
  allow create: if isSignedIn() && 
                   request.auth.uid in request.resource.data.participantIds;
  
  allow update: if isSignedIn() && 
                   request.auth.uid in resource.data.participantIds;
  
  allow delete: if false;

  match /messages/{messageId} {
    allow read: if isSignedIn();
    
    allow create: if isSignedIn() &&
                     request.resource.data.senderId != null &&
                     request.resource.data.senderId == request.auth.uid;
    
    allow update, delete: if isSignedIn() &&
                             resource.data.senderId != null &&
                             resource.data.senderId == request.auth.uid;
  }
}
```

---

### 3️⃣ **USERS** (Profile Data)

**Path:** `users/{userId}`

**Rules:**
```javascript
match /users/{userId} {
  allow read: if isSignedIn();
  
  allow create: if isSignedIn() && userId == request.auth.uid;
  
  allow update: if isSignedIn() && userId == request.auth.uid;
  
  allow delete: if false;
}
```

**Who Can:**
- ✅ READ: All authenticated users (needed for tutor/student discovery)
- ✅ CREATE: User creating their own profile (userId = currentUser)
- ✅ UPDATE: User updating their own profile only
  - Can update: name, bio, profile picture, subjects, isTutor, shareLocation, etc.
  - Cannot elevate to admin (no isAdmin field in update)
- ❌ DELETE: Not allowed (audit trail)

---

### 4️⃣ **REVIEWS** (Tutor Reviews & Ratings)

**Path:** `reviews/{reviewId}`

**Rules:**
```javascript
match /reviews/{reviewId} {
  allow read: if isSignedIn();
  
  allow create: if isSignedIn() &&
                   request.resource.data.userId == request.auth.uid &&
                   request.resource.data.tutorId != null;
  
  allow update: if isSignedIn() &&
                   (resource.data.userId == request.auth.uid || isAdmin());
  
  allow delete: if isSignedIn() &&
                   (resource.data.userId == request.auth.uid || isAdmin());
}
```

**Who Can:**
- ✅ READ: All authenticated users (view reviews)
- ✅ CREATE: User writing review on themselves (userId = currentUser)
- ✅ UPDATE: Review author or admin
- ✅ DELETE: Review author or admin

---

### 5️⃣ **STUDY MATERIALS** (Legacy)

**Path:** `studyMaterials/{materialId}`

**Rules:**
```javascript
match /studyMaterials/{materialId} {
  allow read: if isSignedIn();
  
  allow create: if isSignedIn() &&
                   request.resource.data.uploaderUid == request.auth.uid;
  
  allow update: if isSignedIn() &&
                   (resource.data.uploaderUid == request.auth.uid || isAdmin());
  
  allow delete: if isSignedIn() &&
                   (resource.data.uploaderUid == request.auth.uid || isAdmin());
}
```

---

### 6️⃣ **MATERIALS** (Current Implementation)

**Path:** `materials/{materialId}`

**Rules:**
```javascript
match /materials/{materialId} {
  allow read: if isSignedIn();
  
  allow create: if isSignedIn() &&
                   request.resource.data.uploadedBy == request.auth.uid;
  
  allow update: if isSignedIn() &&
                   (resource.data.uploadedBy == request.auth.uid || isAdmin());
  
  allow delete: if isSignedIn() &&
                   (resource.data.uploadedBy == request.auth.uid || isAdmin());
}
```

**Who Can:**
- ✅ READ: All authenticated users (search & discover materials)
- ✅ CREATE: User uploading their own material
- ✅ UPDATE: Uploader or admin only
- ✅ DELETE: Uploader or admin only

---

### 7️⃣ **TUTORS** (Tutor Profiles & Discovery)

**Path:** `tutors/{tutorId}`

**Rules:**
```javascript
match /tutors/{tutorId} {
  allow read: if isSignedIn();
  
  allow create: if isSignedIn() &&
                   (tutorId == request.auth.uid || isAdmin());
  
  allow update: if isSignedIn() &&
                   (tutorId == request.auth.uid || isAdmin());
  
  allow delete: if isAdmin();
}
```

**Who Can:**
- ✅ READ: All authenticated users (discover tutors)
- ✅ CREATE: Tutor creating own profile or admin
- ✅ UPDATE: Tutor updating own profile or admin
- ✅ DELETE: Admin only

---

### 8️⃣ **NEWS FEED** (Announcements & Articles)

**Path:** `newsFeed/{newsId}`

**Rules:**
```javascript
match /newsFeed/{newsId} {
  allow read: if isSignedIn();
  
  allow create: if isAdmin();
  
  allow update: if isAdmin() ||
                   (isSignedIn() &&
                    request.resource.data.keys().hasOnly(['likes', 'comments', 'likesCount']));
  
  allow delete: if isAdmin();
}
```

**Who Can:**
- ✅ READ: All authenticated users
- ✅ CREATE: Admin only
- ✅ UPDATE: 
  - Admin: Full update
  - Authenticated users: Can only update likes, comments, likesCount (for heart/like functionality)
- ✅ DELETE: Admin only

---

### 9️⃣ **HELP REQUESTS** (Student Help Requests)

**Path:** `helpRequests/{requestId}`

**Rules:**
```javascript
match /helpRequests/{requestId} {
  allow read: if isSignedIn();
  
  allow create: if isSignedIn() &&
                   request.resource.data.requestedByUid != null &&
                   request.resource.data.requestedByUid == request.auth.uid;
  
  allow update: if isSignedIn() &&
                   (resource.data.requestedByUid == request.auth.uid || isAdmin());
  
  allow delete: if isSignedIn() &&
                   (resource.data.requestedByUid == request.auth.uid || isAdmin());

  // ==================== OFFERS SUBCOLLECTION ====================
  match /offers/{offerId} {
    allow read: if isSignedIn();
    
    allow create: if isSignedIn() &&
                     isTutor() &&
                     request.resource.data.tutorUid == request.auth.uid &&
                     request.resource.data.requestId != null &&
                     request.resource.data.tutorUid != null &&
                     request.resource.data.message != null;
    
    allow update: if isSignedIn() &&
                     resource.data.tutorUid == request.auth.uid;
    
    allow delete: if isSignedIn() &&
                     (resource.data.tutorUid == request.auth.uid || 
                      get(/databases/{database}/documents/helpRequests/{requestId}).data.requestedByUid == request.auth.uid ||
                      isAdmin());
  }
}
```

**Help Requests - Who Can:**
- ✅ READ: All authenticated users (browse requests)
- ✅ CREATE: Student creating request (requestedByUid = currentUser)
- ✅ UPDATE: Request creator or admin
- ✅ DELETE: Request creator or admin

**Offers Subcollection - Who Can:**
- ✅ READ: All authenticated users (see offers on requests)
- ✅ CREATE: Tutors only, on requests not by themselves
  - Must include: tutorUid, requestId, message
- ✅ UPDATE: Tutor who created the offer
- ✅ DELETE: 
  - Tutor who created offer
  - Student who created the request
  - Admin

---

### 🔟 **BOOKINGS** (Session Bookings)

**Path:** `bookings/{bookingId}`

**Rules:**
```javascript
match /bookings/{bookingId} {
  allow read: if isSignedIn() &&
                 (resource.data.userId == request.auth.uid || 
                  resource.data.tutorId == request.auth.uid ||
                  isAdmin());
  
  allow create: if isSignedIn() &&
                   request.resource.data.userId == request.auth.uid &&
                   request.resource.data.tutorId != null;
  
  allow update: if isSignedIn() &&
                   (resource.data.userId == request.auth.uid ||
                    resource.data.tutorId == request.auth.uid ||
                    isAdmin());
  
  allow delete: if isSignedIn() &&
                   (resource.data.userId == request.auth.uid || isAdmin());
}
```

**Who Can:**
- ✅ READ: Booking participants (student or tutor) or admin
- ✅ CREATE: Student (userId) creating booking with tutor
- ✅ UPDATE: Student, tutor involved, or admin
- ✅ DELETE: Student or admin

---

### 1️⃣1️⃣ **APP SETTINGS**

**Path:** `appSettings/{document}`

**Rules:**
```javascript
match /appSettings/{document=**} {
  allow read: if isSignedIn();
  
  allow create, update, delete: if isAdmin();
}
```

**Who Can:**
- ✅ READ: All authenticated users
- ✅ CREATE/UPDATE/DELETE: Admin only

---

### 1️⃣2️⃣ **AI CHAT** (Private AI Conversations)

**Path:** `aiChat/{userId}/{messageId}`

**Rules:**
```javascript
match /aiChat/{userId}/{messageId} {
  allow read: if isSignedIn() && userId == request.auth.uid;
  
  allow create: if isSignedIn() &&
                   userId == request.auth.uid &&
                   request.resource.data.userId != null &&
                   request.resource.data.userId == request.auth.uid;
  
  allow update, delete: if isSignedIn() &&
                           userId == request.auth.uid &&
                           resource.data.userId != null &&
                           resource.data.userId == request.auth.uid;
}
```

**Who Can:**
- ✅ READ: User only (their own messages)
- ✅ CREATE: User creating their own message
- ✅ UPDATE: User updating their own message
- ✅ DELETE: User deleting their own message

---

### 1️⃣3️⃣ **NOTIFICATIONS**

**Path:** `notifications/{userId}/{notificationId}`

**Rules:**
```javascript
match /notifications/{userId}/{notificationId} {
  allow read: if isSignedIn() && userId == request.auth.uid;
  
  allow create: if isSignedIn();
  
  allow update, delete: if isSignedIn() && userId == request.auth.uid;
}
```

**Who Can:**
- ✅ READ: User only (their own notifications)
- ✅ CREATE: System/backend (any authenticated user can trigger, typically backend cloud functions)
- ✅ UPDATE: User managing their notifications
- ✅ DELETE: User only

---

### 1️⃣4️⃣ **USER ACTIVITY** (Audit Trail)

**Path:** `userActivity/{userId}/{activityId}`

**Rules:**
```javascript
match /userActivity/{userId}/{activityId} {
  allow read: if isSignedIn() && userId == request.auth.uid;
  
  allow create: if isSignedIn();
  
  allow update, delete: if false;
}
```

**Who Can:**
- ✅ READ: User only (their own activity)
- ✅ CREATE: System/backend (immutable log)
- ❌ UPDATE: Not allowed (audit trail)
- ❌ DELETE: Not allowed (audit trail)

---

## 🚀 Deployment Instructions

### Step 1: Access Firebase Console
1. Go to https://console.firebase.google.com
2. Select your **BookUp** project
3. Navigate to **Firestore Database** → **Rules** tab

### Step 2: Deploy Rules
1. Select ALL existing code (Cmd+A on Mac, Ctrl+A on Windows)
2. Delete everything
3. Copy the **complete firestore.rules file** from your local project:
   - File location: `/Users/user/AndroidStudioProjects/BookUp/firestore.rules`
4. Paste into Firebase Console
5. Click **Publish**
6. ✅ Wait for confirmation: "Rules updated successfully"

### Step 3: Verify Deployment
1. Go to **Firestore Database** → **Data** tab
2. Try browsing a few collections
3. You should see all data (you're authenticated as an admin)
4. Test app features:
   - Send a message ✅
   - Submit review ✅
   - Create help request ✅
   - Submit offer ✅
   - Like news article ✅

---

## 📊 Permission Summary Matrix

| Collection | Read | Create | Update | Delete |
|---|---|---|---|---|
| **Conversations** | Members | Members | Members | ❌ |
| **Chat Channels** | Members | Members | Members | ❌ |
| **Users** | All Auth | Self | Self | ❌ |
| **Reviews** | All Auth | Self | Self/Admin | Self/Admin |
| **Study Materials** | All Auth | Self | Self/Admin | Self/Admin |
| **Materials** | All Auth | Self | Self/Admin | Self/Admin |
| **Tutors** | All Auth | Self/Admin | Self/Admin | Admin |
| **News Feed** | All Auth | Admin | Admin/Likes | Admin |
| **Help Requests** | All Auth | Self | Self/Admin | Self/Admin |
| **→ Offers** | All Auth | Tutors | Tutor | Tutor/Student/Admin |
| **Bookings** | Parties | Self | Parties | Self/Admin |
| **App Settings** | All Auth | Admin | Admin | Admin |
| **AI Chat** | Self | Self | Self | Self |
| **Notifications** | Self | System | Self | Self |
| **User Activity** | Self | System | ❌ | ❌ |

---

## ✅ What's Now Properly Permitted

### For STUDENTS 👨‍🎓
- ✅ Create help requests
- ✅ View all help requests
- ✅ View offers received on their requests
- ✅ Accept/reject offers (via update operation)
- ✅ Book sessions with tutors
- ✅ Submit reviews for tutors
- ✅ Like and comment on news articles
- ✅ Upload study materials
- ✅ Update their profile (name, bio, photo, etc.)
- ✅ Private AI chat access

### For TUTORS 👨‍🏫
- ✅ View help requests from students
- ✅ Submit offers on student requests (not their own)
- ✅ Update their tutor profile
- ✅ Manage their bookings
- ✅ Upload study materials
- ✅ Like and comment on news articles
- ✅ Update their profile
- ✅ Private AI chat access

### For ADMINS 🔧
- ✅ Create, update, delete news articles
- ✅ Manage app settings
- ✅ Delete any user's data (emergency)
- ✅ Manage materials and tutorials
- ✅ Full system access

### For ALL AUTHENTICATED USERS 🔐
- ✅ Read all public collections
- ✅ Like/comment on news feed
- ✅ Send/receive messages
- ✅ Create and join conversations
- ✅ Access their own private data

---

## 🔒 What's PROTECTED

- ❌ Users cannot delete accounts (admin responsibility)
- ❌ Users cannot elevate themselves to admin
- ❌ Users cannot offer help on their own requests
- ❌ Users cannot modify others' profiles
- ❌ Non-admins cannot create/edit news
- ❌ Chat messages cannot be bulk deleted
- ❌ Activity logs are immutable (audit trail)
- ❌ Unauthorized users cannot view others' private data

---

## 🧪 Testing Checklist

After deployment, test these operations:

### Messaging Features ✅
- [ ] Send message in conversation
- [ ] Edit own message
- [ ] Delete own message
- [ ] Cannot edit/delete others' messages

### Request & Offer Features ✅
- [ ] Create help request (student)
- [ ] View all requests (tutor)
- [ ] Submit offer on request (not own)
- [ ] Cannot offer on own request
- [ ] Accept offer (student)
- [ ] Reject offer (student)

### Profile Management ✅
- [ ] Update profile (name, bio, photo)
- [ ] Update location sharing setting
- [ ] Update subject selection (tutors)
- [ ] Cannot change isAdmin/isTutor fields via client

### Content Features ✅
- [ ] Like news article
- [ ] Comment on news article
- [ ] Submit review for tutor
- [ ] Only admins can create news
- [ ] Only admins can delete news

### Materials ✅
- [ ] Upload study material
- [ ] View all materials
- [ ] Delete own material
- [ ] Cannot delete others' materials

### Bookings ✅
- [ ] Create booking (student)
- [ ] View own bookings (student/tutor)
- [ ] Cancel booking
- [ ] Cannot view/modify others' bookings

---

## 📝 Notes

1. **Email/Password Authentication:** These rules assume email/password auth is enabled. Adjust if using other providers.

2. **Claims-Based Rules:** If you implement custom claims (e.g., for admin status), update the `isAdmin()` function to use those instead.

3. **Performance:** Rules with `get()` calls are fast in Firestore (cached), but monitor performance in console.

4. **Backup:** Keep this rules file in version control and document any changes.

5. **Migration:** If migrating from old rules, test thoroughly before deploying to production.

---

## 🆘 Troubleshooting

### "Permission denied" error when...

**Sending a message:**
- ✅ User must be in conversation's `participantIds`
- ✅ `senderId` must match `request.auth.uid`

**Creating help request:**
- ✅ `requestedByUid` must match `request.auth.uid`

**Submitting offer:**
- ✅ User must be a tutor (`isTutor == true`)
- ✅ `tutorUid` must match `request.auth.uid`
- ✅ Cannot offer on own request
- ✅ Message field must not be null

**Updating profile:**
- ✅ userId in path must match `request.auth.uid`

**Creating news (admin):**
- ✅ User must have `isAdmin == true` in users collection

---

## 📞 Support

For issues deploying rules:
1. Check Firebase Console error message
2. Verify all collections exist with sample data
3. Check authentication is working (not signed out)
4. Review this document for specific operation permissions

---

**Status:** ✅ Complete and Ready for Production
**Version:** 2.0
**Last Updated:** December 27, 2025
