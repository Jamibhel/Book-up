# Firebase Storage Rules Deployment Guide

## Problem
Audio recording upload fails with "object does not exists at location" because Firebase Storage Rules are not configured.

## Solution
Deploy the Storage Rules to your Firebase project.

---

## Step 1: Install Firebase CLI (if not already installed)

```bash
npm install -g firebase-tools
```

## Step 2: Login to Firebase

```bash
firebase login
```

This will open a browser window to authenticate with your Google account.

## Step 3: Verify Firebase Configuration

From the BookUp project root, verify your firebase.json is correct:

```bash
cat firebase.json
```

You should see:
```json
{
  "firestore": {
    "rules": "firebase.rules"
  },
  "storage": {
    "rules": "storage.rules"
  }
}
```

## Step 4: Deploy Rules

```bash
firebase deploy --project book-up-ishola
```

This will:
1. ✅ Deploy Firestore rules from `firebase.rules`
2. ✅ Deploy Storage rules from `storage.rules`

Expected output:
```
=== Deploying to 'book-up-ishola' ===

i  deploying firestore
✔  firestore: rules updated successfully
i  deploying storage
✔  storage: rules updated successfully

✔  Deploy complete!
```

## Step 5: Verify in Firebase Console

1. Go to https://console.firebase.google.com/
2. Select project `book-up-ishola`
3. Go to **Storage** (left sidebar)
4. Click **Rules** tab
5. Verify you see the storage rules (should allow authenticated users to upload to `/chats/`, `/userProfiles/`, `/materials/`)

## Step 6: Test Audio Upload

1. Reinstall the app: `./gradlew installDebug`
2. Record 3-5 seconds of audio
3. Release to upload
4. **Expected**: Audio appears in chat within 2-3 seconds
5. **Expected**: Playback works without errors

---

## What the Storage Rules Allow

✅ **Authenticated users** can:
- Read chat media (audio/images/videos)
- Upload files to `/chats/{chatChannelId}/`
- Upload profile pictures to `/userProfiles/{userId}/`
- Upload study materials to `/materials/{materialId}/`

❌ **Rules prevent**:
- Unauthenticated access
- Files larger than size limits (50MB for chat, 10MB for profiles, 100MB for materials)
- Access to other users' private data

---

## Troubleshooting

If deployment fails:

1. **"Project not found"**:
   ```bash
   firebase projects:list
   ```
   Verify `book-up-ishola` is in the list. If not, check your Google account permissions.

2. **"Permission denied"**:
   Make sure you're logged in: `firebase login`

3. **"Rules file not found"**:
   Verify you're in the BookUp project root and both files exist:
   ```bash
   ls firebase.rules
   ls storage.rules
   ```

4. **Still getting upload errors after deployment**:
   - Check app permissions: Settings > Apps > BookUp > Permissions > Storage ✅
   - Check Firebase Console > Storage > Rules are deployed
   - Verify user is authenticated (logged in)
   - Check logs: `firebase functions:log --project book-up-ishola`

---

## One-Line Deployment (if you're in a hurry)

```bash
firebase deploy --project book-up-ishola
```

That's it! 🚀
