# Quick Troubleshooting Guide - Chat Features

## Issue 1: Conversations Not Showing in Chat List

### Quick Fix Checklist:
- [ ] Open Firestore Console
- [ ] Go to `conversations` collection
- [ ] Check if any documents exist
- [ ] Verify document has:
  - ✅ `participantIds` array containing your user ID
  - ✅ `conversationName` field (not empty)
  - ✅ `lastMessageTimestamp` Date field

### If Empty:
1. **Create a conversation first**:
   - Click "Start chat" FAB in ChatListFragment
   - Select a user
   - Send a message
   - Conversation will be created automatically

2. **Check Firestore Rules** are allowing reads:
   ```
   match /conversations/{conversationId} {
     allow read: if request.auth.uid in resource.data.participantIds;
   }
   ```

### Logcat Debugging:
Look for these messages:
```
✅ SUCCESS: Loaded X conversations
❌ ERROR loading conversations
📭 Empty state: No conversations found
```

---

## Issue 2: Timestamps Not Showing

### Quick Fix:
Check that conversation documents have:
```
lastMessageTimestamp: Timestamp (Date object, NOT String!)
```

### If Showing Wrong Format:
1. Verify `lastMessageTimestamp` is a Firestore Timestamp
2. Not a String like "2025-12-25"
3. Check `ConversationAdapter.formatTimestamp()` method

### Logcat Debugging:
```
⏰ Set timestamp: 2:30 PM
⚠️ No timestamp for: conv_123
```

---

## Issue 3: Card Colors Look Wrong

### Verification:
✅ Cards should have:
- Light gray background (not white)
- Thin border (subtle outline)
- No shadow/elevation (flat design)

### If Colors Still Look Off:
1. Verify using theme attributes in XML:
   ```xml
   app:cardBackgroundColor="?attr/colorSurfaceContainer"
   app:strokeColor="?attr/colorOutline"
   ```

2. Check you're using a Material Design 3 theme
3. Verify not in custom theme that overrides colors

### In Dark Mode:
- Background should be darker gray
- Border should be lighter
- Automatically adapts to system theme

---

## Issue 4: User Search Shows No Results

### Step 1: Verify Users Exist
```
Logcat Output:
D/ChatRepository: 📊 Total documents in users collection: 5
```

If it says 0:
1. Go to Firestore Console
2. Create test users in `users` collection
3. Each user needs:
   - `displayName` (String)
   - `email` (String)
   - `id` (String, should be document ID)

### Step 2: Verify Search Works
Open NewChatFragment and check Logcat:
```
📋 Loading all users
✅ Loaded X users
  - Alice Johnson (user_001)
  - Bob Smith (user_002)
```

### Step 3: Test Search
Type "alice" in search box:
```
🔍 Searching users for: 'alice'
✅ Found 1 matching users
  ✓ Alice Johnson (alice@email.com)
```

### Step 4: Check Empty State
If no results:
```
⚠️ No users found for query: 'xyz'
(Should show "No users found for xyz" message)
```

---

## Complete User Setup

### Add Test Users to Firestore:
```javascript
// Firestore document: /users/user_001
{
  id: "user_001",
  displayName: "Alice Johnson",
  email: "alice@example.com",
  photoUrl: "https://...",  // optional
  bio: "Student"  // optional
}
```

### Add Test Conversation:
```javascript
// Firestore document: /conversations/conv_001
{
  conversationId: "conv_001",
  conversationName: "Alice Johnson",
  participantIds: ["YOUR_USER_ID", "user_001"],
  lastMessage: "Hello, how are you?",
  lastMessageContent: "Hello, how are you?",
  lastMessageTimestamp: Timestamp.now(),
  participantNames: {
    "YOUR_USER_ID": "You",
    "user_001": "Alice Johnson"
  },
  conversationImage: "https://...",  // optional
  unreadCount: 0
}
```

---

## Common Error Messages

### ❌ "Binding is null - fragment may be destroyed"
- Fragment closed before callback returned
- Normal if dialog closes quickly
- Not a critical error

### ❌ "Current user ID is EMPTY!"
- Not logged in
- FirebaseAuth not initialized
- Solution: Log in first

### ❌ "No last message for: conv_123"
- Conversation has no messages yet
- Display shows "No messages yet"
- Normal for new conversations

### ❌ "No timestamp for: conv_123"
- lastMessageTimestamp field is null
- Solution: Send a message to create timestamp

### ❌ "No name available for conversation"
- conversationName is empty
- Fallback uses participantNames
- If both empty, shows "Conversation"

### ❌ "Error parsing user: ..."
- User document in Firestore is malformed
- displayName or email might be null
- Check Firestore document structure

---

## Firestore Queries Being Used

### Get Conversations:
```java
db.collection("conversations")
  .whereArrayContains("participantIds", userId)
  .addSnapshotListener(...)
```

### Search Users:
```java
db.collection("users").get()  // Client-side filtering
// Filters by displayName.contains() or email.contains()
```

### Get All Users:
```java
db.collection("users").get()  // Returns all users
```

---

## Network & Performance

### Real-Time Updates:
- Uses `addSnapshotListener()` (real-time)
- Auto-updates when Firestore data changes
- No need to refresh manually

### Search Performance:
- Client-side filtering (gets all users, filters locally)
- OK for <1000 users
- For >1000 users, implement server-side search

### Optimization Tips:
- Limit participant names in participantNames map
- Archive old conversations
- Use pagination for large lists

---

## Development Mode Settings

### Enable Verbose Logging:
In relevant Fragment classes, set log level:
```java
Log.setLevel(Log.DEBUG);  // Enable debug logs
```

### Check Real-Time Updates:
1. Open app in emulator
2. Modify document in Firestore Console
3. App should update automatically
4. Check Logcat for snapshot updates

---

## Testing Commands (Firestore Console)

### Create Test User:
```javascript
// Run in Firestore Console - Add document
db.collection("users").add({
  displayName: "Test User",
  email: "test@example.com",
  photoUrl: "",
  id: "test_user_001"
})
```

### Create Test Conversation:
```javascript
db.collection("conversations").add({
  conversationId: "conv_test",
  conversationName: "Test User",
  participantIds: ["YOUR_UID", "test_user_001"],
  lastMessage: "Test message",
  lastMessageTimestamp: new Date(),
  participantNames: {
    "YOUR_UID": "You",
    "test_user_001": "Test User"
  }
})
```

---

## Final Checklist Before Deployment

- [ ] Firebase project is properly configured
- [ ] Firestore rules allow authenticated users to read collections
- [ ] At least one test user exists in `/users` collection
- [ ] At least one conversation exists with proper structure
- [ ] Real-time listeners are working (check with Firestore updates)
- [ ] All colors display correctly in light AND dark mode
- [ ] Timestamps format correctly (shows time for today)
- [ ] Search finds users by name and email
- [ ] No red errors in Logcat
- [ ] Build completes successfully

---

## Emergency Fixes

### If App Crashes on ChatListFragment:
1. Check Firestore rules
2. Verify getUserConversations() is called
3. Check if binding is null
4. Clear app data and retry

### If Search Always Shows "No Users":
1. Verify users exist: `db.collection("users").count()`
2. Check user displayName is not null
3. Run search without text (should show all)
4. Check Logcat for query errors

### If Timestamps Show as "null":
1. Delete and recreate conversation
2. Send a new message
3. Timestamp will be created automatically

### If Cards Look Invisible:
1. Verify theme is properly initialized
2. Check if `colorSurfaceContainer` exists in your theme
3. Fallback: use hardcoded color if theme not available

---

## Advanced Debugging

### Firestore Connection Check:
```java
db.collection("test").document("test").get()
  .addOnSuccessListener(doc -> Log.d("DEBUG", "✅ Firestore connected"))
  .addOnFailureListener(e -> Log.e("DEBUG", "❌ Error: " + e));
```

### List All Collections:
Can't do directly in Firestore SDK - check Console

### Monitor Real-Time Updates:
```java
db.collection("conversations")
  .addSnapshotListener((snapshot, error) -> {
    if (error != null) {
      Log.e("DEBUG", "Snapshot error: " + error);
      return;
    }
    Log.d("DEBUG", "Documents: " + snapshot.size());
  });
```

### Check Current User:
```java
FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
if (user != null) {
  Log.d("DEBUG", "User: " + user.getUid() + " | " + user.getEmail());
}
```
