# Error Handling Integration Guide - Week 2 Phase 6
**Quick Reference for Integrating FirebaseErrorHandler**

---

## 📋 Fragments to Update

### 1. ChatListFragment
**File:** `/app/src/main/java/com/example/bookup/fragments/ChatListFragment.java`

**Find & Replace Pattern:**
```java
// FIND:
.addOnFailureListener(e -> {
    Log.e(TAG, "Error: " + e.getMessage());
    Toast.makeText(getContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
});

// REPLACE:
.addOnFailureListener(e -> {
    Log.e(TAG, "Error: " + e.getMessage());
    if (errorHandler != null) {
        errorHandler.handleError(e, recyclerView);
    } else {
        Toast.makeText(getContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
    }
});
```

**Add to onCreate():**
```java
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    errorHandler = new FirebaseErrorHandler();  // ← ADD THIS
}
```

**Add import:**
```java
import com.example.bookup.utils.FirebaseErrorHandler;
```

---

### 2. DashboardFragment
**File:** `/app/src/main/java/com/example/bookup/fragments/DashboardFragment.java`

**Locations to Update:**
- Fetch news feed failures
- Fetch dashboardStats failures
- Fetch featured tutors failures

**Pattern (same as above):**
```java
.addOnFailureListener(e -> {
    if (errorHandler != null) {
        errorHandler.handleError(e, rootView);
    }
});
```

---

### 3. ProfileFragment
**File:** `/app/src/main/java/com/example/bookup/fragments/ProfileFragment.java`

**Locations to Update:**
- Fetch user profile failures
- Update profile failures
- Delete account failures

**With Retry Logic:**
```java
.addOnFailureListener(e -> {
    if (errorHandler != null) {
        if (errorHandler.isRetryable(errorHandler.categorizeError(e))) {
            // Auto-retry with backoff
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                retryFetchProfile();
            }, errorHandler.calculateBackoffDelay(1));
        } else {
            errorHandler.handleError(e, rootView);
        }
    }
});
```

---

### 4. RequestsFragment ✅ (Already Updated)
**Status:** FirebaseErrorHandler placeholder added, ready to activate

**Current State:**
```java
private FirebaseErrorHandler errorHandler;  // ✅ Already declared

// In loadMoreRequests():
.addOnFailureListener(e -> {
    if (errorHandler != null) {
        errorHandler.handleError(e, recyclerView);  // ✅ Already using
    }
});
```

---

### 5. SearchFragment ✅ (Already Updated)
**Status:** FirebaseErrorHandler placeholder added, ready to activate

**Current State:**
```java
private FirebaseErrorHandler errorHandler;  // ✅ Already declared

// In searchMaterials():
.addOnFailureListener(e -> {
    if (errorHandler != null) {
        errorHandler.handleError(e, progressBar);  // ✅ Already using
    }
});

// In searchTutors():
.addOnFailureListener(e -> {
    if (errorHandler != null) {
        errorHandler.handleError(e, progressBar);  // ✅ Already using
    }
});
```

---

## 🔄 Integration Checklist

### For Each Fragment:

- [ ] Add `private FirebaseErrorHandler errorHandler;` field
- [ ] Add `errorHandler = new FirebaseErrorHandler();` in onCreate()
- [ ] Add `import com.example.bookup.utils.FirebaseErrorHandler;`
- [ ] Find all `.addOnFailureListener()` blocks
- [ ] Replace Toast with `errorHandler.handleError(e, viewRef)`
- [ ] Test: Trigger error manually (disable internet, use wrong credentials)
- [ ] Verify: Snackbar appears with appropriate message and color

---

## 🎨 Error Type → User Message Mapping

| Error Type | User Message | Action |
|---|---|---|
| NETWORK_ERROR | "No connection. Retrying..." | Retry automatically |
| PERMISSION_DENIED | "You don't have access" | Show error, no retry |
| NOT_FOUND | "Resource not found" | Clear UI gracefully |
| INVALID_DATA | "Invalid data format" | Show error, no retry |
| AUTHENTICATION_FAILED | "Sign in required" | Redirect to login |
| SERVER_ERROR | "Server error. Retrying..." | Retry automatically |
| TIMEOUT | "Request timeout" | Retry automatically |
| CONFLICT | "Conflict detected" | Refresh data |
| UNKNOWN | "Something went wrong" | Show error, log analytics |

---

## 🔧 Code Snippets Ready to Copy

### Full ChatListFragment Update
```java
package com.example.bookup.fragments;

import com.example.bookup.utils.FirebaseErrorHandler;

public class ChatListFragment extends Fragment {
    private FirebaseErrorHandler errorHandler;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        errorHandler = new FirebaseErrorHandler();
    }
    
    private void loadChats() {
        db.collection("chats")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Success logic
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching chats: " + e.getMessage());
                if (errorHandler != null) {
                    errorHandler.handleError(e, recyclerView);
                }
            });
    }
}
```

### Full DashboardFragment Update
```java
private void fetchNewsFeed() {
    db.collection("newsFeeds")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(10)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
            // Success logic
        })
        .addOnFailureListener(e -> {
            if (errorHandler != null) {
                errorHandler.handleError(e, rootView);
            }
        });
}
```

---

## ⏱️ Time Estimate

**Per Fragment:**
- Find failure listeners: 2 min
- Replace with errorHandler: 3 min
- Test: 2 min
- **Total per fragment: ~7 minutes**

**All 5 fragments: ~35 minutes**

---

## ✅ Verification Steps

After each fragment update:

1. **Compile Check:**
   ```bash
   ./gradlew compileDebugJavaWithJavac
   ```

2. **Manual Test - Network Error:**
   - Disable internet
   - Trigger data load
   - Verify: Orange Snackbar with "No connection" message

3. **Manual Test - Auth Error:**
   - Sign out
   - Try to load protected data
   - Verify: Red Snackbar with "Sign in required" message

4. **Manual Test - Success:**
   - Enable internet
   - Sign in
   - Data loads smoothly
   - No error Snackbar shown

---

## 🎯 Production Quality Criteria

After completing all 5 fragments:
- ✅ All failures handled with user-friendly messages
- ✅ Appropriate retry logic for network errors
- ✅ No generic Toasts showing
- ✅ Consistent Snackbar styling throughout app
- ✅ Error analytics data being logged
- ✅ Zero uncaught exceptions in error handling

---

**Status:** Ready to implement  
**Estimated Time:** 35-45 minutes for all 5 fragments  
**Next Phase:** Network Connectivity Integration
