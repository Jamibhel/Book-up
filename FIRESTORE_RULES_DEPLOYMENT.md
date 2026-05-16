# Firestore Security Rules Deployment Guide

## Overview
The `firestore.rules` file contains security rules that:
- ✅ Allow authenticated users to submit reviews for tutors
- ✅ Allow authenticated users to create and access chat channels
- ✅ Prevent unauthorized access to chats and reviews
- ✅ Allow users to only edit/delete their own content

## Deployment Methods

### Method 1: Firebase Console (Easiest for Quick Testing)

1. **Open Firebase Console**
   - Go to [firebase.google.com](https://firebase.google.com)
   - Select your BookUp project

2. **Navigate to Firestore Rules**
   - Click **Firestore Database** in the left sidebar
   - Click the **Rules** tab at the top

3. **Copy & Paste the Rules**
   - Copy all content from `firestore.rules`
   - Paste into the Firebase Console editor
   - Click **Publish**

4. **Verify Deployment**
   - You'll see "Rules updated" confirmation
   - Rules take effect immediately

### Method 2: Firebase CLI (Recommended for Production)

#### Prerequisites
```bash
# Install Firebase CLI if not already installed
npm install -g firebase-tools

# Login to Firebase
firebase login
```

#### Deploy Steps
```bash
# Navigate to project root
cd /Users/user/AndroidStudioProjects/BookUp

# Set your Firebase project (if not already set)
firebase use --add
# Select your BookUp project from the list

# Deploy only Firestore rules
firebase deploy --only firestore:rules

# OR deploy all Firebase resources
firebase deploy
```

#### Expected Output
```
⚡ Firestore Rules have been deployed.
✔  Deploy complete!
```

## Rule Breakdown

### 1. Tutorial Reviews Collection
```firestore
match /tutorReviews/{reviewId}
```
- **Read**: Public (anyone can see reviews)
- **Create**: Only authenticated users; must include:
  - `tutorId` (string)
  - `userId` = logged-in user's UID
  - `rating` = integer between 1-5
- **Update/Delete**: Only by the review author

### 2. Chat Channels Collection
```firestore
match /chatChannels/{channelId}
```
- **Read**: Only participants in the channel
- **Create**: Only authenticated users who are in the `participantIds` list
- **Update**: Only participants
- **Delete**: Disabled (prevent accidental deletion)
- **Messages**: Only participants can read/write messages

### 3. Conversations Collection
```firestore
match /conversations/{conversationId}
```
- Same rules as chatChannels (supports alternative naming)

### 4. Users Collection
```firestore
match /users/{userId}
```
- **Read**: Public
- **Write**: Only by the user themselves

## Testing the Rules

### Test Case 1: Submit a Review (Should Succeed)
1. Sign in as User A
2. Navigate to a Tutor's profile
3. Tap "Write Review"
4. Select a rating (e.g., 5 stars)
5. Enter review text
6. Tap "Submit Review"
7. **Expected**: Success toast, review appears in Firestore

### Test Case 2: Submit Without Authentication (Should Fail)
1. Sign out
2. Try to submit a review
3. **Expected**: Permission denied error toast
   - "Permission denied: you don't have permission to submit reviews. Please sign in or contact support."

### Test Case 3: Send a Message in Chat (Should Succeed)
1. Sign in as User A
2. Open a conversation with User B
3. Type a message
4. Tap Send
5. **Expected**: Message appears in chat

### Test Case 4: Access Another User's Chat (Should Fail)
1. User A and User B have a chat
2. User C tries to access that chat directly (via URL/database)
3. **Expected**: Firestore denies read access

## Troubleshooting

### Error: "Permission denied"
- Ensure you're signed in
- Check that the authenticated user's UID is in the `participantIds` array for chats
- Verify review submission includes `userId == request.auth.uid`

### Error: "Document does not exist" (on message read)
- Confirm the parent conversation/channel exists
- Check that the user is a participant

### Rules Not Updating
- Clear browser cache (if using Console)
- Wait 1-2 minutes for cache invalidation
- Try deploying again with CLI

### CLI Deployment Issues

**Issue**: "Cannot find module 'firebase-tools'"
```bash
# Solution: Install globally
npm install -g firebase-tools
firebase login
```

**Issue**: "Project ID not found"
```bash
# Solution: Set project explicitly
firebase use --add
firebase deploy --only firestore:rules
```

## Monitoring Rules Performance

### In Firebase Console:
1. Go to **Firestore Database** → **Rules**
2. Scroll to **Rules Playground** (if available)
3. Test sample read/write operations

### In Android Studio Logs:
When a rule blocks an operation, you'll see:
```
FirebaseFirestoreException: PERMISSION_DENIED
```

Check ReviewsBottomSheetFragment logs:
```
Log.e("ReviewsBottomSheet", "Error submitting review", e);
```

## Summary

✅ **Created**: `firestore.rules` with complete security rules  
✅ **Updated**: `firebase.json` to reference `firestore.rules`  
✅ **Included**: Rules for reviews, chats, and user profiles  
✅ **Ready**: Deploy using Firebase Console or CLI  

**Next Steps**:
1. Deploy rules using either method above
2. Run test case 1 to verify review submission works
3. Run test case 2 to verify unauthenticated users get clear error message
4. Monitor Android logs for any issues

---

**Questions?**
- Check Firebase docs: https://firebase.google.com/docs/firestore/security/start
- Review rule syntax: https://firebase.google.com/docs/firestore/security/rules-query
