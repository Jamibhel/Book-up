# Android Code Requirements for New Firebase Rules

Now that you're updating Firebase rules, your **Android code must set the correct field names** in Firestore documents.

## 1. ReviewsBottomSheetFragment - SET userId FIELD

**File**: `app/src/main/java/com/example/bookup/fragments/ReviewsBottomSheetFragment.java`

**Location**: In the `submitReview()` method, around line 155

**Current code** (you may already have this):
```java
Review review = new Review(
    tutorId,
    currentUser.getUid(),
    currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous",
    currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : ""
);
review.setRating(currentRating);
review.setComment(comment);
```

**CRITICAL**: Ensure your `Review` model has these fields:
```java
public class Review {
    public String id;
    public String tutorId;
    public String userId;        // <-- MUST BE SET for rules to allow
    public String userName;
    public String userPhotoUrl;
    public float rating;
    public String comment;
    public long timestamp;

    public Review() {}

    public Review(String tutorId, String userId, String userName, String userPhotoUrl) {
        this.tutorId = tutorId;
        this.userId = userId;      // <-- THIS IS SET IN CONSTRUCTOR
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public void setId(String id) { this.id = id; }
    public void setRating(float rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public String getUserId() { return userId; }
}
```

**Verify in `submitReview()`**:
```java
// After creating review object, verify userId is set:
Review review = new Review(
    tutorId,
    currentUser.getUid(),  // This becomes userId
    currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous",
    currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : ""
);

// Debug: Log to verify
Log.d("ReviewSubmit", "UserId being saved: " + review.userId + ", CurrentUser: " + currentUser.getUid());

review.setRating(currentRating);
review.setComment(comment);

// Save - Firestore rules will check: request.resource.data.userId == request.auth.uid
db.collection("reviews").add(review)...
```

---

## 2. Message Sending - VERIFY participantIds

**File**: `app/src/main/java/com/example/bookup/fragments/ChatFragment.java` (or your chat implementation)

When creating a conversation:
```java
Map<String, Object> conversationData = new HashMap<>();
conversationData.put("participantIds", Arrays.asList(
    currentUser.getUid(),
    recipientId
));
conversationData.put("lastMessage", "");
conversationData.put("lastMessageTime", System.currentTimeMillis());

db.collection("conversations").add(conversationData)
    .addOnSuccessListener(ref -> {
        // Create message in conversation
        Map<String, Object> message = new HashMap<>();
        message.put("senderId", currentUser.getUid());
        message.put("text", messageText);
        message.put("timestamp", System.currentTimeMillis());
        
        // This will only work if senderId is in participantIds
        db.collection("conversations")
            .document(ref.getId())
            .collection("messages")
            .add(message);
    });
```

**Rules check**: 
- `request.auth.uid in get(/databases/.../conversations/$(conversationId)).data.participantIds`

Ensure `participantIds` is an **array** with user IDs as strings.

---

## 3. Material Upload - SET uploadedBy FIELD

**File**: Upload materials code (wherever you handle uploads)

```java
Map<String, Object> material = new HashMap<>();
material.put("title", titleText);
material.put("description", descriptionText);
material.put("uploadedBy", FirebaseAuth.getInstance().getCurrentUser().getUid()); // <-- REQUIRED
material.put("uploadDate", System.currentTimeMillis());
material.put("fileUrl", downloadUrl);
material.put("fileType", "PDF"); // or image type

db.collection("materials").add(material)
    .addOnSuccessListener(ref -> {
        Toast.makeText(context, "Material uploaded", Toast.LENGTH_SHORT).show();
    })
    .addOnFailureListener(e -> {
        // If PERMISSION_DENIED, check:
        // 1. uploadedBy field is set to current user ID
        // 2. Storage rules allow writing to users/{uid}/ path
        Toast.makeText(context, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
    });
```

**Rules check**:
- `request.resource.data.uploadedBy == request.auth.uid`

---

## 4. Cloud Storage Upload - CORRECT PATH

**File**: Your image/audio upload code

**BEFORE (likely wrong path)**:
```java
// DON'T DO THIS - storage path doesn't match rules
storageRef.child("files").child(filename).putFile(fileUri);
```

**AFTER (correct path)**:
```java
String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
String path;

if (isProfilePicture) {
    path = "profilePictures/" + userId + "/" + System.currentTimeMillis() + ".jpg";
} else if (isAudioRecording) {
    path = "audioRecordings/" + userId + "/" + System.currentTimeMillis() + ".m4a";
} else {
    path = "users/" + userId + "/" + filename;
}

FirebaseStorage.getInstance()
    .getReference(path)
    .putFile(fileUri)
    .addOnSuccessListener(taskSnapshot -> {
        Log.d("Upload", "Success: " + path);
    })
    .addOnFailureListener(e -> {
        Log.e("Upload", "Failed: " + e.getMessage());
    });
```

**Storage rules check**:
- Profile: `profilePictures/{userId}/`
- Audio: `audioRecordings/{userId}/`
- Other: `users/{userId}/`

---

## Checklist: Before Testing

- [ ] Review model has `userId` field
- [ ] ReviewsBottomSheetFragment sets userId in Review constructor
- [ ] Conversation creation adds both users to `participantIds` array
- [ ] Material uploads set `uploadedBy` field
- [ ] Storage paths use `users/{uid}/` or specific folders
- [ ] Audio paths use `audioRecordings/{uid}/`
- [ ] Firebase rules published (see PERMISSIONS_FIX_QUICK_START.md)
- [ ] Storage rules published
- [ ] App rebuilt after any code changes

---

## Debugging: Check Firestore Documents

Go to Firebase Console → Firestore Database → Collections:

### Verify Reviews Document
```json
{
  "id": "...",
  "tutorId": "tutor123",
  "userId": "user456",      // <-- MUST EXIST and match request.auth.uid
  "userName": "John Doe",
  "rating": 4.5,
  "comment": "Great tutor!",
  "timestamp": 1234567890
}
```

### Verify Conversation Document
```json
{
  "participantIds": ["user1", "user2"],  // <-- MUST BE ARRAY
  "lastMessage": "Hello",
  "lastMessageTime": 1234567890
}
```

### Verify Material Document
```json
{
  "title": "Study Guide",
  "uploadedBy": "user456",  // <-- MUST EQUAL request.auth.uid
  "uploadDate": 1234567890,
  "fileUrl": "gs://bucket/..."
}
```

---

## If Still Getting PERMISSION_DENIED

1. **Check field names** - must exactly match rules expectations
2. **Check field values** - must match `request.auth.uid`
3. **Check field types** - `participantIds` must be array, not string
4. **Check rules published** - reload rules tab to confirm
5. **Check user logged in** - `request.auth.uid` must not be null
6. **Check logcat** - share exact error message

```bash
# Run app and check logs
adb logcat | grep -E "PERMISSION|firestore|reviews|messages"
```

---

## Summary

| Operation | Field to Set | Rules Check |
|-----------|--------------|-------------|
| Create Review | `userId` | `request.resource.data.userId == request.auth.uid` |
| Send Message | none (parent conversation) | user in `participantIds` |
| Upload Material | `uploadedBy` | `request.resource.data.uploadedBy == request.auth.uid` |
| Upload Image | none (storage path) | path is `users/{uid}/` |
| Record Audio | none (storage path) | path is `audioRecordings/{uid}/` |

All of these must work **AFTER** you apply the new Firebase rules.
