# SEARCH SYSTEM - ROOT CAUSE ANALYSIS
**Status**: Root Causes Identified  
**Date**: December 24, 2025

---

## 🔴 THE REAL PROBLEM: COLLECTION NAME MISMATCHES

Search has **NEVER** worked because the app saves data to different collections than where search queries look.

---

## 🔎 SEARCH FLOW ANALYSIS

### **What SearchFragment Does:**

#### **Materials Search** (SearchFragment.java, line 244-265)
```java
Query query = db.collection("studyMaterials")  // ← Queries HERE
    .whereGreaterThanOrEqualTo("title", searchTerm)
    .whereLessThan("title", searchTerm + "\uffff")
    .limit(PAGE_SIZE);
```

**Expected behavior**: Query "studyMaterials" collection by title field
**Field it searches**: `title`

---

#### **Tutors Search** (SearchFragment.java, line 285-306)
```java
Query query = db.collection("tutors")  // ← Queries HERE
    .whereGreaterThanOrEqualTo("name", searchTerm)
    .whereLessThan("name", searchTerm + "\uffff")
    .limit(PAGE_SIZE);
```

**Expected behavior**: Query "tutors" collection by name field
**Field it searches**: `name`

---

## 💾 WHERE DATA IS ACTUALLY SAVED

### **Materials Are Saved To:**

**File**: `UploadMaterialActivity.java`, line 261
```java
db.collection("materials")  // ← Saves to "materials"
    .add(material)
    .addOnSuccessListener(...)
```

**Saved fields in document**:
- `title` ✅ (matches search field)
- `subject` ✅
- `type` (saved as "type", model expects "materialType")
- `description` ✅
- `fileUrl` ✅
- `thumbnailUrl` ✅
- `uploadedBy` (saved as "uploadedBy", model expects "uploaderUid")
- `uploadedAt` (saved as "uploadedAt", model expects "timestamp")

**PROBLEM**: Saves to `"materials"` but search queries `"studyMaterials"` ❌

---

### **Tutors Are Saved To:**

**File**: `ProfileSetupActivity.java`, line 159-160
```java
db.collection("users").document(currentUser.getUid())
    .set(user, SetOptions.merge())
```

**Saved fields when user registers as tutor**:
- `firstName` (tutor model expects "name")
- `lastName` (not in tutor model)
- `phoneNumber` (not in tutor model)
- `isTutor = true` (boolean flag)
- `email` (not in tutor model)
- `uid` (matches tutor model)
- `profilePicUrl` (matches "profileImageUrl" field?)
- `shareLocation` (not in tutor model)
- `isAdmin` (not in tutor model)

**PROBLEM**: Tutors saved to `"users"` collection but search queries `"tutors"` collection ❌

---

## 📊 COLLECTION LOCATION MISMATCH TABLE

| Data Type | Saved To | Searched In | Search Field | Saved Field | Match? |
|-----------|----------|-------------|--------------|-------------|--------|
| **Materials** | `materials` | `studyMaterials` | `title` | `title` | ❌ WRONG COLLECTION |
| **Tutors** | `users` | `tutors` | `name` | `firstName`+`lastName` (separate) | ❌ WRONG COLLECTION |

---

## 🔧 ADDITIONAL FIELD MAPPING ISSUES

### **Study Materials Field Mismatch:**

**Model expects** (StudyMaterial.java):
```java
private String title;           // Firestore: title ✅
private String description;     // Firestore: description ✅
private String subject;         // Firestore: subject ✅
private String materialType;    // Firestore: type ❌
private String fileUrl;         // Firestore: fileUrl ✅
private String thumbnailUrl;    // Firestore: thumbnailUrl ✅
private String uploaderUid;     // Firestore: uploadedBy ❌
private String uploaderName;    // Firestore: ??? (not saved)
private Date timestamp;         // Firestore: uploadedAt ❌
```

**What's saved** (UploadMaterialActivity.java, line 257-265):
```java
material.put("title", title);                    // ✅ Matches
material.put("subject", subject);                // ✅ Matches
material.put("type", materialType);              // ❌ Should be "materialType"
material.put("description", description);        // ✅ Matches
material.put("fileUrl", fileUrl);                // ✅ Matches
material.put("thumbnailUrl", thumbnailUrl);      // ✅ Matches
material.put("uploadedBy", userId);              // ❌ Should be "uploaderUid"
material.put("uploadedAt", serverTimestamp());   // ❌ Should be "timestamp"
// NOT SAVED: uploaderName
```

---

### **Tutor Field Mismatch:**

**Model expects** (Tutor.java):
```java
private String uid;                 // Firestore: uid ✅
private String name;                // Firestore: firstName + lastName ❌
private String profileImageUrl;     // Firestore: profilePicUrl ❌
private String bio;                 // Firestore: ??? (not saved)
private List<String> subjects;      // Firestore: ??? (not saved)
private double rating;              // Firestore: ??? (not saved)
private int reviewCount;            // Firestore: ??? (not saved)
private boolean isAvailable;        // Firestore: ??? (not saved)
```

**What's saved** (ProfileSetupActivity.java, line 142-156):
```java
user.put("firstName", firstName);               // ❌ Should be "name"
user.put("lastName", lastName);                 // ❌ No "lastName" in model
user.put("phoneNumber", phoneNumber);           // ❌ Not in model
user.put("isTutor", isTutor);                   // ❌ Not in model (flag field)
user.put("email", email);                       // ❌ Not in model
user.put("uid", uid);                           // ✅ Matches
user.put("profilePicUrl", "");                  // ❌ Should be "profileImageUrl"
user.put("shareLocation", false);               // ❌ Not in model
user.put("isAdmin", false);                     // ❌ Not in model
// NOT SAVED: bio, subjects, rating, reviewCount, isAvailable
```

---

## 🚫 WHY SEARCH RETURNS EMPTY

When user types "Math" in SearchFragment:

### **For Materials:**
1. User types "Math"
2. SearchFragment queries: `db.collection("studyMaterials").whereGreaterThanOrEqualTo("title", "Math")`
3. **Result**: 0 materials found because:
   - Materials are saved in `"materials"` collection
   - Search looks in `"studyMaterials"` collection
   - `"studyMaterials"` collection is EMPTY
4. UI shows: "No results found"

### **For Tutors:**
1. User types "John"
2. SearchFragment queries: `db.collection("tutors").whereGreaterThanOrEqualTo("name", "John")`
3. **Result**: 0 tutors found because:
   - Tutors are saved in `"users"` collection with `firstName` and `lastName` as separate fields
   - Search looks in `"tutors"` collection
   - `"tutors"` collection is EMPTY (or minimal)
   - Even if tutors were there, search field doesn't exist
4. UI shows: "No results found"

---

## 🚧 BLOCKING FEATURE: "SEARCH COMING SOON"

**File**: `ChatListFragment.java`, line 161
```java
private void setupSearch() {
    binding.searchBarChat.setOnClickListener(v -> {
        Toast.makeText(requireContext(), "Search coming soon", Toast.LENGTH_SHORT).show();  // ← BLOCKS SEARCH
    });
}
```

**Impact**: Even if SearchFragment search worked, users can't search conversations from chat list.

---

## 📋 COMPLETE ROOT CAUSES

### **Root Cause #1: Materials Collection Name Mismatch**
- **Saved to**: `"materials"`
- **Searched in**: `"studyMaterials"`
- **Solution**: Either:
  - Change UploadMaterialActivity to save to `"studyMaterials"`, OR
  - Change SearchFragment to search in `"materials"`

### **Root Cause #2: Tutors Collection Location Wrong**
- **Saved to**: `"users"` (with `isTutor=true` flag)
- **Searched in**: `"tutors"`
- **Additional issue**: Field names don't match (firstName/lastName vs name)
- **Solution**: Either:
  - Create tutor profiles in separate `"tutors"` collection with proper field mapping, OR
  - Change SearchFragment to query `"users"` collection with `isTutor=true` filter and proper field handling

### **Root Cause #3: Field Name Mismatches**
- Materials: "type" vs "materialType", "uploadedBy" vs "uploaderUid", "uploadedAt" vs "timestamp"
- Tutors: "firstName"+"lastName" vs "name", "profilePicUrl" vs "profileImageUrl"
- **Solution**: Align field names between model definitions and what's saved to Firestore

### **Root Cause #4: ChatListFragment Search Blocked**
- **Location**: `ChatListFragment.java`, line 161
- **Issue**: Shows "Search coming soon" toast, preventing users from accessing search
- **Solution**: Remove the blocking toast or implement conversation search

---

## ✅ WHAT WORKS

✅ SearchFragment is correctly written (proper queries, listeners, UI updates)  
✅ Result fragments (MaterialSearchResultsFragment, TutorSearchResultsFragment) work correctly  
✅ Search UI responds to input and updates in real-time  
✅ Error handling is present  

---

## ❌ WHAT DOESN'T WORK

❌ **Materials search returns 0 results** (wrong collection)  
❌ **Tutors search returns 0 results** (wrong collection + field mismatch)  
❌ **Chat list search blocked** ("coming soon" message)  
❌ **Field mapping mismatches** prevent proper deserialization even if collections were correct  

---

## 🔧 FIX OPTIONS

### **Option A: Minimal Fix (Align collections to existing data)**
1. Change SearchFragment to query `"users"` for tutors (with `isTutor=true` filter)
2. Change SearchFragment to query `"materials"` for study materials
3. Fix field mappings in models OR adapt search queries to handle field name differences
4. Remove "Search coming soon" message in ChatListFragment
5. **Pros**: Uses existing data, minimal schema changes
6. **Cons**: Models don't match Firestore structure

### **Option B: Proper Fix (Create dedicated tutor collection)**
1. Create a Cloud Function to sync tutor data:
   - Trigger on `users` collection updates
   - When `isTutor=true`, copy data to `tutors` collection with proper field mapping
2. Keep materials in `"studyMaterials"` (change UploadMaterialActivity)
3. Fix all field mappings
4. Remove "Search coming soon" message
5. **Pros**: Proper architecture, models match schema, better performance
6. **Cons**: More work, requires Cloud Function, data sync complexity

---

## 📝 IMPLEMENTATION PRIORITY

1. **CRITICAL**: Fix collection mismatches
2. **CRITICAL**: Remove "Search coming soon" blocking message
3. **HIGH**: Fix field name mappings
4. **MEDIUM**: Consider Option B (dedicated tutor collection)

---

## 🎯 NEXT STEPS

1. **Decision**: Which option (A or B)?
2. **If Option A**: Update SearchFragment queries and fix model field handling
3. **If Option B**: Create Cloud Function + update collection names
4. **All**: Remove ChatListFragment search blocker
5. **All**: Test search with real data

