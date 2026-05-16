# 📚 Complete User Search Guide: NewChatFragment

## Quick Answer

### How Users Search After Clicking "Start Chat" FAB:

1. **FAB clicked** → NewChatFragment (BottomSheetDialog) opens
2. **All users load** → `loadAllUsers()` method fetches all users from Firestore
3. **User types in search field** → `TextWatcher` detects input (no delay)
4. **Real-time filtering** → `searchUsers(query)` called immediately
5. **Results update** → Adapter refreshes with matching users
6. **User taps result** → Selection callback fires → Chat opens

---

## 🔧 Methods I Implemented in NewChatFragment

### 1. **setupSearch()** - Text Input Listener
```java
private void setupSearch() {
    binding.editSearchUsers.addTextChangedListener(new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            currentSearchQuery = s.toString().trim();
            if (currentSearchQuery.isEmpty()) {
                loadAllUsers();           // Show all users
            } else {
                searchUsers(currentSearchQuery);  // Filter users
            }
        }
        // ... other methods
    });
}
```
**Purpose**: Listens to text input and triggers search immediately

---

### 2. **searchUsers(String query)** - Fragment-level Search Handler
```java
private void searchUsers(String query) {
    Log.d("NewChatFragment", "🔍 Searching users for: '" + query + "'");
    
    chatRepository.searchUsers(query, (users, error) -> {
        if (binding == null) return;
        
        if (error != null) {
            showEmptyState(true, "Error: " + error);
            return;
        }

        if (users != null && !users.isEmpty()) {
            binding.recyclerUsers.setVisibility(View.VISIBLE);
            binding.layoutEmptyUsers.setVisibility(View.GONE);
            adapter.submitList(users);  // Update adapter with results
        } else {
            showEmptyState(true, "No users found for \"" + query + "\"");
        }
    });
}
```
**Purpose**: Calls repository method and updates UI with results

---

### 3. **loadAllUsers()** - Load All Users Initially
```java
private void loadAllUsers() {
    Log.d("NewChatFragment", "📋 Loading all users");
    
    chatRepository.getAllUsers((users, error) -> {
        if (binding == null) return;
        
        if (error != null) {
            showEmptyState(true, "Error loading users: " + error);
            return;
        }

        if (users != null && !users.isEmpty()) {
            binding.recyclerUsers.setVisibility(View.VISIBLE);
            binding.layoutEmptyUsers.setVisibility(View.GONE);
            adapter.submitList(users);  // Show all users
        } else {
            showEmptyState(true, "No users available");
        }
    });
}
```
**Purpose**: Loads and displays all users when dialog first opens

---

### 4. **setupChipFilters()** - User Type Filtering
```java
private void setupChipFilters() {
    // All users filter
    binding.chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isChecked) {
            binding.chipStudents.setChecked(false);
            binding.chipTutors.setChecked(false);
            loadAllUsers();
        }
    });

    // Students filter (TODO: implement filtering logic)
    binding.chipStudents.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isChecked) {
            binding.chipAll.setChecked(false);
            binding.chipTutors.setChecked(false);
            // TODO: Filter for students only
        }
    });

    // Tutors filter (TODO: implement filtering logic)
    binding.chipTutors.setOnCheckedChangeListener((buttonView, isChecked) -> {
        if (isChecked) {
            binding.chipAll.setChecked(false);
            binding.chipStudents.setChecked(false);
            // TODO: Filter for tutors only
        }
    });

    binding.chipAll.setChecked(true);  // Set "All" as default
}
```
**Purpose**: Allows filtering users by type (Students/Tutors) - currently only "All" works

---

### 5. **showEmptyState(boolean show, String message)** - UI State Management
```java
private void showEmptyState(boolean show, String message) {
    Log.d("NewChatFragment", "📝 showEmptyState(" + show + ", \"" + message + "\")");
    binding.layoutEmptyUsers.setVisibility(show ? View.VISIBLE : View.GONE);
    binding.recyclerUsers.setVisibility(show ? View.GONE : View.VISIBLE);
}
```
**Purpose**: Shows/hides empty state message and adjusts RecyclerView visibility

---

## 📊 Backend Implementation: ChatRepository.searchUsers()

```java
public void searchUsers(String query, OnUsersFoundListener listener) {
    // 1. Normalize query
    query = query.toLowerCase().trim();
    final String finalQuery = query;

    // 2. Fetch ALL users from Firestore
    db.collection("users")
            .get()  // ← Gets ALL documents
            .addOnSuccessListener(querySnapshot -> {
                java.util.List<com.example.bookup.models.User> users = 
                    new java.util.ArrayList<>();

                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    // 3. Loop through each user document
                    for (int i = 0; i < querySnapshot.getDocuments().size(); i++) {
                        try {
                            // Deserialize to User object
                            com.example.bookup.models.User user = querySnapshot
                                .getDocuments()
                                .get(i)
                                .toObject(com.example.bookup.models.User.class);

                            if (user != null) {
                                // Set ID from document ID
                                if (user.getId() == null) {
                                    user.setId(querySnapshot.getDocuments().get(i).getId());
                                }
                                
                                // Get display name and email for comparison
                                String displayName = user.getDisplayName() != null ? 
                                    user.getDisplayName().toLowerCase() : "";
                                String userEmail = user.getEmail() != null ? 
                                    user.getEmail().toLowerCase() : "";

                                // 4. CLIENT-SIDE FILTERING
                                // Check if display name OR email contains query
                                if (displayName.contains(finalQuery) || 
                                    userEmail.contains(finalQuery)) {
                                    users.add(user);  // Add matching user
                                }
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "⚠️ Error parsing user: " + e.getMessage());
                        }
                    }
                }

                // 5. Return filtered results via callback
                listener.onUsersFound(users, null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Error searching users: " + e.getMessage());
                listener.onUsersFound(null, e.getMessage());
            });
}
```

---

## 🔄 Comparison: NewChatFragment vs SearchFragment

| Feature | NewChatFragment | SearchFragment |
|---------|-----------------|----------------|
| **Search Trigger** | Immediate (TextWatcher) | Debounced (500ms) |
| **Database Query** | `.get()` (all users) | `.where()` queries |
| **Filtering** | Client-side only | Server-side + Client-side |
| **Collections** | Users only | Users, Materials |
| **Search Type** | Contains (substring) | StartsWith (prefix) |
| **Use Case** | "Find person to chat with" | "Find anything" |
| **Performance** | Good for small user bases | Better for large datasets |
| **User Experience** | Real-time feedback | 500ms delay before search |

### Key Differences:

**NewChatFragment Search:**
- ✅ **IMMEDIATE**: Searches as you type
- ✅ **SIMPLE**: Just load all users, filter on client
- ✅ **CONTEXTUAL**: Only searches users (chat contacts)
- ✅ **EFFICIENT**: Users likely have <1000 contacts
- ✅ **RESPONSIVE**: No delay = better UX

**SearchFragment Search:**
- ✅ **DEBOUNCED**: Waits 500ms for user to stop typing
- ✅ **SOPHISTICATED**: Multiple collections, server-side filtering
- ✅ **GENERAL**: Searches materials, tutors, and students
- ✅ **SCALABLE**: Works with millions of documents
- ✅ **EFFICIENT**: Server filters first, then client filters

---

## 🎯 Method Calls & Flow

### When User Types in NewChatFragment:

```
1. User types "john" in search field
   ↓
2. TextWatcher.onTextChanged() fires
   ↓
3. setupSearch() → searchUsers("john")
   ↓
4. NewChatFragment.searchUsers() calls:
   chatRepository.searchUsers("john", callback)
   ↓
5. ChatRepository.searchUsers() executes:
   db.collection("users").get()
   → For each user: check if displayName.contains("john") 
                    or email.contains("john")
   → Build list of matches
   ↓
6. Callback receives results
   ↓
7. adapter.submitList(users)
   ↓
8. DiffUtil compares old list with new list
   ↓
9. RecyclerView animates changes
   ↓
10. User sees updated results
```

---

## 🏗️ Why Two Different Search Methods?

### NewChatFragment uses SIMPLE search because:
1. **Limited dataset**: Usually <1000 users in app
2. **Context matters**: Only searching for chat contacts
3. **UX priority**: User wants immediate feedback
4. **Simplicity**: Less code, easier to debug

### SearchFragment uses SOPHISTICATED search because:
1. **Larger dataset**: Could have millions of materials, tutors, students
2. **Multiple types**: Need to search different collections
3. **Performance priority**: Debouncing prevents server overload
4. **Production-ready**: Scales with app growth

---

## 💡 Pro Tips for Using the Search

### For Users:
- **Start typing a name** → See matching users in real-time
- **Type an email** → Search by email address too
- **Use chips** → Filter by type (Students/Tutors) [TODO: implement]
- **No results?** → User might not exist or not yet created profile

### For Developers:
- **To add Tutor/Student filtering**: Modify `searchUsers()` to filter by `isTutor` field
- **To make search faster**: Implement server-side filtering in ChatRepository
- **To add debounce**: Wrap TextWatcher with Handler like SearchFragment does
- **To show user status**: Add online indicator from presence system

---

## 🐛 Known Limitations & TODOs

### Current Limitations:
1. ❌ **Student/Tutor filters don't work** - Chips are set up but filtering logic is TODO
2. ❌ **No debouncing** - Every keystroke triggers a database query
3. ❌ **Searches all users** - No pagination for large user bases
4. ❌ **No online status** - Can't see who's currently available

### TODOs for Improvement:
```java
// TODO 1: Implement student filtering
private void filterStudents(String query) {
    // Similar to searchUsers but with: .where("isTutor", "==", false)
}

// TODO 2: Implement tutor filtering  
private void filterTutors(String query) {
    // Similar to searchUsers but with: .where("isTutor", "==", true)
}

// TODO 3: Add debouncing (like SearchFragment)
private Handler searchHandler = new Handler(Looper.getMainLooper());
private Runnable searchRunnable;
searchHandler.postDelayed(() -> searchUsers(query), 300);

// TODO 4: Show online/offline status
// Add presence system to track active users
```

---

## 📱 UI Components

### Layout File: `fragment_new_chat.xml`
- Search EditText: `edit_search_users`
- Filter Chips: `chip_all`, `chip_students`, `chip_tutors`
- RecyclerView: `recycler_users` (uses UserSelectionAdapter)
- Empty State: `layout_empty_users`

### Item Layout: `item_user_selection.xml`
- Profile image: `image_user_profile`
- User name: `text_user_name`
- User email: `text_user_email`
- User role badge: `chip_user_role`
- Online indicator: `indicator_online`

---

## 🔐 Security Considerations

### Current Implementation:
- ✅ Loads users from Firestore (requires authentication)
- ✅ User IDs are set from document IDs (secure)
- ✅ Only searchable fields are name and email

### Best Practices Applied:
- ✅ Validate user ID before using it
- ✅ Handle null values gracefully
- ✅ Log errors for debugging
- ✅ Show user-friendly error messages

---

## 🎓 Summary

**NewChatFragment Search** is designed to be:
- **Simple**: Load users, filter on client-side
- **Direct**: Immediate response to typing
- **Contextual**: Only search for chat contacts
- **Reliable**: No complex server queries

It uses a **TextWatcher** for input, a **ChatRepository method** for Firestore access, and a **ListAdapter** with DiffUtil for efficient UI updates.

The main differences from SearchFragment are:
- **No debouncing** (immediate vs 500ms delay)
- **Client-side only filtering** (vs server-side + client-side)
- **Single collection search** (vs multi-collection)
- **Substring matching** (vs prefix matching)

Both approaches are correct for their use cases!

