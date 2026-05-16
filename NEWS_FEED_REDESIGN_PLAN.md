# News Feed Redesign - Comprehensive Implementation Plan

## Overview
Comprehensive redesign of the News Feed system with image uploads, rich headlines, comments, and likes.

---

## Phase 1: Core Data Model ✅ DONE

### 1.1 Enhanced NewsItem Model ✅
**File:** `NewsItem.java`
**Changes:**
- Added `headline` field (short title for feed display)
- Added `content` field (full article body text)
- Added `likes` array (list of user IDs who liked)
- Added `likesCount` cache field
- Added `comments` array (list of comment maps)
- Added helper methods: `addLike()`, `removeLike()`, `isLikedByUser()`, `addComment()`, `removeComment()`
- Backward compatible with existing `title` field (aliased to `headline`)

### 1.2 New Comment Model ✅
**File:** `Comment.java` (NEW)
**Fields:**
- `userId`: UID of commenter
- `userName`: Display name
- `userImageUrl`: Profile picture (optional)
- `text`: Comment text
- `timestamp`: When posted (ServerTimestamp)
- `likeCount`: Optional comment likes

---

## Phase 2: Admin News Management (IN PROGRESS)

### 2.1 Enhanced CreateNewsItemActivity
**File:** `CreateNewsItemActivity.java`
**Required Changes:**

#### UI Updates:
```xml
<!-- Replace text input fields with: -->
<ImageView
    android:id="@+id/image_preview"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:scaleType="centerCrop"
    android:src="@drawable/ic_image_placeholder"
/>

<MaterialButton
    android:id="@+id/btn_pick_image"
    android:text="Upload Image from Phone"
/>

<TextInputLayout android:id="@+id/layout_headline">
    <TextInputEditText
        android:id="@+id/edit_headline"
        android:hint="Headline"
        android:inputType="text"
    />
</TextInputLayout>

<TextInputLayout android:id="@+id/layout_content">
    <TextInputEditText
        android:id="@+id/edit_content"
        android:hint="Full news content..."
        android:inputType="textMultiLine"
        android:minLines="6"
    />
</TextInputLayout>

<TextInputLayout android:id="@+id/layout_description">
    <TextInputEditText
        android:id="@+id/edit_description"
        android:hint="Brief summary (optional)"
        android:inputType="text"
    />
</TextInputLayout>

<TextInputLayout android:id="@+id/layout_source">
    <TextInputEditText
        android:id="@+id/edit_source"
        android:hint="News source"
        android:inputType="text"
    />
</TextInputLayout>
```

#### Code Changes:
1. **Add image picker using ActivityResultContracts:**
   ```java
   private final ActivityResultContracts.GetContent imagePickerContract = 
       new ActivityResultContracts.GetContent();
   
   private final ActivityResultLauncher<String> imagePicker = 
       registerForActivityResult(imagePickerContract, uri -> {
           if (uri != null) {
               selectedImageUri = uri;
               Glide.with(this).load(uri).centerCrop().into(imagePreview);
           }
       });
   ```

2. **Add image upload to Firebase Storage:**
   ```java
   private void uploadImageToFirebase(Uri imageUri, OnImageUploadedListener listener) {
       String filename = "news_" + System.currentTimeMillis() + ".jpg";
       StorageReference ref = FirebaseStorage.getInstance()
           .getReference("news_images/" + filename);
       
       ref.putFile(imageUri)
           .addOnSuccessListener(taskSnapshot -> 
               ref.getDownloadUrl().addOnSuccessListener(uri -> 
                   listener.onImageUploaded(uri.toString())))
           .addOnFailureListener(e -> listener.onImageFailed(e));
   }
   ```

3. **Update publishNewsItem() to:**
   - Validate headline + content (not just title)
   - Upload image first (if provided)
   - Create NewsItem with headline, content, imageUrl
   - Save to Firestore

### 2.2 Enhanced EditNewsItemActivity
**File:** `EditNewsItemActivity.java`
**Changes:** Same as CreateNewsItemActivity but pre-populate with existing news data

---

## Phase 3: Dashboard News Feed UI (NEXT PRIORITY)

### 3.1 Update DashboardFragment
**File:** `DashboardFragment.java`
**Changes:**
```java
// In setupNewsFeedRecyclerView():
// Change from HORIZONTAL to VERTICAL layout for better card display
recyclerNewsFeed.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

// Update click listener to open NewsDetailActivity:
newsFeedAdapter.setOnNewsItemClickListener(item -> {
    Intent intent = new Intent(requireActivity(), NewsDetailActivity.class);
    intent.putExtra(NewsDetailActivity.EXTRA_NEWS_ID, item.getId());
    intent.putExtra(NewsDetailActivity.EXTRA_NEWS_ITEM, item);
    startActivity(intent);
});
```

### 3.2 Update NewsFeedAdapter
**File:** `NewsFeedAdapter.java`
**Changes:**
- Display compact headline card format
- Show image at top
- Display headline (not full description)
- Show excerpt of description
- Add like count display
- Updated layout `item_news_feed.xml`:
  ```xml
  <com.google.android.material.card.MaterialCardView>
      <!-- Image -->
      <ImageView
          android:id="@+id/image_news"
          android:layout_height="200dp"
          android:scaleType="centerCrop"
      />
      
      <!-- Content -->
      <LinearLayout android:orientation="vertical">
          <TextView android:id="@+id/headline"
              android:textSize="18sp"
              android:textStyle="bold"
          />
          
          <TextView android:id="@+id/excerpt"
              android:textSize="14sp"
              android:maxLines="2"
              android:ellipsize="end"
          />
          
          <LinearLayout android:orientation="horizontal">
              <ImageView android:id="@+id/icon_like"/>
              <TextView android:id="@+id/like_count"/>
              <TextView android:id="@+id/source"/>
          </LinearLayout>
      </LinearLayout>
  </com.google.android.material.card.MaterialCardView>
  ```

---

## Phase 4: News Detail View (CRITICAL)

### 4.1 Create NewsDetailActivity (NEW)
**File:** `NewsDetailActivity.java`
**Features:**
- Display full headline, image, content
- Like button with count
- Comments section
- User who posted + timestamp

**Layout:** `activity_news_detail.xml`
```xml
<ScrollView>
    <LinearLayout orientation="vertical">
        <!-- Image -->
        <ImageView android:id="@+id/image_news_detail"
            android:layout_height="300dp"
            android:scaleType="centerCrop"
        />
        
        <!-- Headline -->
        <TextView android:id="@+id/headline"
            android:textSize="22sp"
            android:textStyle="bold"
            android:padding="16dp"
        />
        
        <!-- Metadata (source, date) -->
        <LinearLayout android:padding="16dp" android:orientation="horizontal">
            <TextView android:id="@+id/source"/>
            <TextView android:id="@+id/timestamp"/>
        </LinearLayout>
        
        <!-- Full content -->
        <TextView android:id="@+id/content"
            android:padding="16dp"
            android:lineSpacingMultiplier="1.5"
        />
        
        <!-- Like button & count -->
        <LinearLayout android:padding="16dp" android:orientation="horizontal">
            <ImageButton android:id="@+id/btn_like"
                android:src="@drawable/ic_heart_outline"
            />
            <TextView android:id="@+id/like_count"/>
        </LinearLayout>
        
        <!-- Comments section -->
        <TextView android:text="Comments"
            android:textSize="16sp"
            android:textStyle="bold"
            android:padding="16dp"
        />
        
        <RecyclerView android:id="@+id/recycler_comments"
            android:layout_height="wrap_content"
        />
        
        <!-- Add comment input -->
        <LinearLayout android:padding="16dp" android:orientation="horizontal">
            <EditText android:id="@+id/edit_comment"
                android:layout_weight="1"
                android:hint="Add a comment..."
            />
            <Button android:id="@+id/btn_post_comment"
                android:text="Post"
            />
        </LinearLayout>
    </LinearLayout>
</ScrollView>
```

**Code Implementation:**
```java
public class NewsDetailActivity extends AppCompatActivity {
    private NewsItem newsItem;
    private String newsId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get newsItem from intent or fetch from Firestore by ID
        // Load and display all content
        // Setup like button listener
        // Setup comments recycler
    }
    
    private void toggleLike() {
        String userId = mAuth.getCurrentUser().getUid();
        if (newsItem.isLikedByUser(userId)) {
            newsItem.removeLike(userId);
            // Update Firestore: db.collection("newsFeed").document(newsId).update(...)
        } else {
            newsItem.addLike(userId);
            // Update Firestore
        }
        updateLikeButton();
    }
    
    private void postComment(String text) {
        // Create comment map with userId, userName, text, timestamp
        // Add to newsItem.comments
        // Update Firestore
        // Refresh comments adapter
    }
}
```

---

## Phase 5: Likes Feature

### Implementation:
1. **In NewsDetailActivity:**
   - Like button toggles `newsItem.addLike(userId)` or `removeLike(userId)`
   - Update Firestore: `db.collection("newsFeed").document(newsId).update("likes", newsItem.getLikes())`
   - Refresh UI to show updated like count

2. **In NewsFeedAdapter:**
   - Display like count from `newsItem.getLikesCount()`

3. **In DashboardFragment:**
   - Like count visible in headline card

---

## Phase 6: Comments Feature

### Create CommentsAdapter
**File:** `CommentsAdapter.java`
```java
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Map<String, Object>> comments;
    
    // Display: user avatar, name, comment text, timestamp
    // Optional: like button for comments
}
```

### In NewsDetailActivity:
```java
private void loadComments() {
    // Query comments from newsItem.getComments()
    // Display in RecyclerView
}

private void addComment(String text) {
    Map<String, Object> comment = new HashMap<>();
    comment.put("userId", mAuth.getCurrentUser().getUid());
    comment.put("userName", currentUserName);
    comment.put("userImageUrl", currentUserImageUrl);
    comment.put("text", text);
    comment.put("timestamp", System.currentTimeMillis());
    
    newsItem.addComment(comment);
    db.collection("newsFeed").document(newsId)
        .update("comments", newsItem.getComments())
        .addOnSuccessListener(v -> {
            editCommentText.setText("");
            commentsAdapter.notifyDataSetChanged();
        });
}
```

---

## Implementation Priority

1. ✅ **DONE:** Enhanced NewsItem & Comment models
2. **NEXT:** Create NewsDetailActivity (Phase 4)
3. **THEN:** Enhance CreateNewsItemActivity with image upload (Phase 2)
4. **THEN:** Update DashboardFragment & NewsFeedAdapter (Phase 3)
5. **FINALLY:** Add Likes & Comments functionality (Phases 5-6)

---

## Build & Test Checklist

- [ ] Build project with enhanced models
- [ ] Create NewsDetailActivity layout & code
- [ ] Test news detail opens with full content
- [ ] Add image picker to CreateNewsItemActivity
- [ ] Test image upload to Firebase Storage
- [ ] Update DashboardFragment to show headline cards
- [ ] Test like functionality
- [ ] Test comment functionality

---

## Files to Create/Modify

### Create (NEW):
- `NewsDetailActivity.java` + `activity_news_detail.xml`
- `CommentsAdapter.java` + `item_comment.xml`

### Modify:
- `NewsItem.java` ✅
- `Comment.java` ✅
- `CreateNewsItemActivity.java`
- `EditNewsItemActivity.java`
- `DashboardFragment.java`
- `NewsFeedAdapter.java`
- `activity_create_news_item.xml`
- `activity_edit_news_item.xml`
- `item_news_feed.xml`

---

## Technical Notes

### Image Upload to Firebase Storage:
```
news_images/
  ├── news_1234567890.jpg
  ├── news_1234567891.jpg
  └── ...
```

### Firestore Collection Structure:
```
newsFeed/
  ├── docId1/
  │   ├── headline: "..."
  │   ├── content: "..."
  │   ├── description: "..."
  │   ├── imageUrl: "gs://bucket/news_images/..."
  │   ├── likes: [userId1, userId2]
  │   ├── likesCount: 2
  │   ├── comments: [{userId, userName, text, timestamp}, ...]
  │   ├── source: "..."
  │   └── timestamp: ...
  └── ...
```

---

## Next Immediate Steps

Would you like me to proceed with:
1. **Create NewsDetailActivity** (Phase 4) - Core feature for viewing full news
2. **Enhance image upload in CreateNewsItemActivity** (Phase 2) - Admin feature
3. **Both in parallel** - Using new approach

Which should I focus on first?
