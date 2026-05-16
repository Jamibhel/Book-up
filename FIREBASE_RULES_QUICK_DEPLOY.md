# 🔐 Firebase Rules - QUICK DEPLOYMENT GUIDE

## ⚡ TL;DR - Deploy in 2 Minutes

### Copy This Code to Firestore Rules

**Go to:** https://console.firebase.google.com → Select Project → Firestore Database → Rules Tab

**Replace everything with:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /conversations/{conversationId} {
      allow read: if request.auth != null && request.auth.uid in resource.data.participantIds;
      allow create: if request.auth != null && request.auth.uid in request.resource.data.participantIds;
      allow update: if request.auth != null && request.auth.uid in resource.data.participantIds;
      allow delete: if request.auth != null && request.auth.uid in resource.data.participantIds;
      
      match /messages/{messageId} {
        allow read: if request.auth != null && request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participantIds;
        allow create: if request.auth != null && request.auth.uid == request.resource.data.senderId && request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participantIds;
        allow update: if request.auth != null && request.auth.uid == resource.data.senderId;
        allow delete: if request.auth != null && request.auth.uid == resource.data.senderId;
      }
    }
    
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

Click **Publish** ✅

---

### Copy This Code to Storage Rules

**Go to:** https://console.firebase.google.com → Select Project → Storage → Rules Tab

**Replace everything with:**

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /chat_media/{conversationId}/{userId}/{allPaths=**} {
      allow create: if request.auth != null && request.auth.uid == userId && request.resource.size < 50 * 1024 * 1024;
      allow read: if request.auth != null;
      allow delete: if request.auth != null && request.auth.uid == userId;
    }
    
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

Click **Publish** ✅

---

## ✅ Why These Rules Work

| What | Rule | Result |
|------|------|--------|
| Create conversation | Any authenticated user | ✅ Works |
| Read own conversation | Must be in participantIds | ✅ Works |
| Send message | Must be sender + in conversation | ✅ Works |
| Edit message | Must be message sender | ✅ Works |
| Delete message | Must be message sender | ✅ Works |
| Upload media | To own folder only | ✅ Works |
| Read media | Any authenticated user | ✅ Works |

---

## 🧪 Quick Test (30 seconds)

1. In Firebase Console, scroll down to **Rules Simulator**
2. Click **Simulate**
3. Test these:

### ✅ Should PASS
```
Operation: write
Path: conversations/test123
Auth UID: user123
Data: {"participantIds": ["user123", "user456"]}
```

### ❌ Should FAIL
```
Operation: read
Path: conversations/test123
Auth UID: user789 (NOT in participantIds)
```

---

## 🚨 If Rules Still Deny Access

Check these 3 things:

### 1. Is Authentication Enabled?
```
Firebase Console → Authentication → Enable Email/Password or Google Sign-In
```

### 2. Does Your Data Have Required Fields?
```json
{
  "conversations": {
    "id": {
      "participantIds": ["uid1", "uid2"]  // ← MUST have this
    }
  }
}
```

### 3. Are UIDs Correct?
```java
String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
Log.d("UID", uid);  // Check this matches participantIds
```

---

## 📊 Rule Structure Explained

```
conversations/
├── {conversationId}
│   ├── participantIds: ["uid1", "uid2"]  ← Users must be here
│   └── messages/
│       └── {messageId}
│           ├── senderId: "uid1"  ← Sender UID
│           └── content: "Hello!"
```

**Rules check:**
- ✅ User is authenticated (request.auth != null)
- ✅ User is in participantIds (read/write)
- ✅ User is message sender (message edit/delete)

---

## 🎯 Common Errors & Fixes

### Error: "Permission denied"
**Cause:** User not in participantIds  
**Fix:** Check conversation document has your UID in participantIds array

### Error: "Cannot read property of undefined"
**Cause:** Field name is wrong  
**Fix:** Verify field names exactly match (case-sensitive):
- `participantIds` ✅
- `participantids` ❌
- `participantIdz` ❌

### Error: "Subcollection read denied"
**Cause:** Parent conversation doesn't exist  
**Fix:** Create conversation first, then add messages

---

## 📋 Deployment Checklist

- [ ] Copy Firestore Rules
- [ ] Paste in Firebase Console
- [ ] Click Publish
- [ ] Wait for "Rules updated successfully"
- [ ] Copy Storage Rules
- [ ] Paste in Firebase Storage Rules
- [ ] Click Publish
- [ ] Test with Rules Simulator
- [ ] Test in app
- [ ] All working ✅

---

## 🚀 You're Done!

Your Firebase is now secure and working.

Next:
1. Run your app
2. Test sending messages
3. Check that both users see messages
4. Verify images upload
5. Verify audio works

**Status: 🟢 READY**

---

*Setup Time: ~2 minutes*  
*Security Level: Production Ready*  
*Last Updated: December 22, 2025*
