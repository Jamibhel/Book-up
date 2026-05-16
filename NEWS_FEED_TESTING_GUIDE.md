# News Feed Feature - Quick Testing Guide

## Prerequisites
- App installed on device/emulator
- User account with admin role (`isAdmin: true`)
- Internet connection for Firebase
- Camera/photos available for image testing

---

## Test Scenario 1: Create News with Image

### Steps:
1. Launch the app and login as admin
2. Navigate to **Admin Panel** → **Manage News**
3. Tap **"Create News"** button
4. Screen shows: Image preview placeholder, headline field, content field, description, source

### Test Image Upload:
1. Tap **"Upload Image from Phone"** button
2. Select image from device gallery
3. Verify: Image preview updates with Glide
4. Button text changes to **"Change Image"**

### Test Form:
1. Fill **Headline:** "Major Tech Breakthrough"
2. Fill **Content:** (Long-form article text, 6+ lines)
3. Fill **Description:** "Short summary"
4. Fill **Source:** "Tech News Daily"
5. Tap **"Publish News"**
6. Verify: 
   - Progress bar shows during upload
   - Image uploads to Firebase Storage
   - Toast: "News item published successfully!"
   - Screen closes, returns to ManageNewsActivity

---

## Test Scenario 2: View News in Dashboard

### Steps:
1. Open app as regular user
2. Navigate to **Dashboard** → **News Feed** section
3. Verify: News card displays with:
   - ✓ Image (uploaded from previous test)
   - ✓ Headline text
   - ✓ Excerpt/description
   - ✓ Like count (should be 0)

### Test Navigation:
1. Tap on the news headline card
2. Verify: **NewsDetailActivity** opens with:
   - ✓ Full-size news image at top
   - ✓ Headline text
   - ✓ Source name
   - ✓ Publication date/time
   - ✓ Full content text
   - ✓ Like button (heart outline)
   - ✓ Like count (0 likes)
   - ✓ "Comments" section header
   - ✓ "No comments yet" empty state message
   - ✓ Comment input field at bottom

---

## Test Scenario 3: Like Functionality

### Steps:
1. In NewsDetailActivity, verify heart icon is **outline** (not filled)
2. Tap heart icon (like button)
3. Verify:
   - ✓ Heart icon changes to **filled** (red/primary color)
   - ✓ Like count updates from "0 likes" to "1 likes"
   - ✓ No errors in logcat

### Test Unlike:
1. Tap filled heart icon again
2. Verify:
   - ✓ Heart returns to **outline**
   - ✓ Like count back to "0 likes"
   - ✓ Firestore document updated correctly

### Check Firestore:
1. Open Firebase Console → Firestore
2. Navigate to `newsFeed/{newsId}`
3. Verify:
   - ✓ `likes` array contains user ID (after like)
   - ✓ `likes` array is empty (after unlike)
   - ✓ `likesCount` reflects current count

---

## Test Scenario 4: Comments Feature

### Steps:
1. In NewsDetailActivity, scroll down to comment section
2. Tap comment input field
3. Type: "Great article! Very informative."
4. Tap **"Post"** button
5. Verify:
   - ✓ Comment appears in list immediately
   - ✓ Shows: Your name, avatar, comment text, timestamp
   - ✓ Empty state disappears
   - ✓ Toast: "Comment posted"
   - ✓ Input field clears

### Test Multiple Comments:
1. Add 2-3 more comments with different text
2. Verify: Comments appear in list below first comment
3. Verify: Each shows correct user info and timestamp

### Check Firestore:
1. In Firebase Console, check `newsFeed/{newsId}/comments`
2. Verify: Array contains all posted comments with:
   - ✓ userId
   - ✓ userName
   - ✓ text
   - ✓ timestamp
   - ✓ userImageUrl

---

## Test Scenario 5: Comment Persistence

### Steps:
1. Close NewsDetailActivity
2. Go back to Dashboard
3. Click the same news article again
4. Scroll to comments section
5. Verify: All previously posted comments appear
6. Verify: Comment count hasn't reset

---

## Test Scenario 6: Image Handling

### Negative Test - No Image:
1. Create news without uploading image
2. Verify: News publishes successfully
3. In detail view, verify: Placeholder image shows (not broken/blank)

### Large Image Test:
1. Select very large image file (3+ MB)
2. Tap publish
3. Verify:
   - ✓ Upload takes reasonable time
   - ✓ No app crash
   - ✓ Success toast appears
   - ✓ Image displays in detail view

### Invalid File Test:
1. Attempt to select non-image file (if possible)
2. Verify: Image picker filters to images only

---

## Test Scenario 7: Error Scenarios

### Network Error:
1. Turn off WiFi/mobile data
2. Try to like a news item
3. Verify: Error toast appears
4. Reconnect, try again
5. Verify: Like successful after reconnect

### Firebase Error:
1. Check logcat for any Firebase exceptions
2. Verify: All error messages user-friendly (no stack traces shown)
3. Verify: App doesn't crash on error

### Empty Fields:
1. Try to publish without filling required fields
2. Verify:
   - ✓ Error message on field: "is required"
   - ✓ Focus moves to first empty field
   - ✓ Publish button disabled if validation fails

---

## Test Scenario 8: UI/UX Checks

### Layout Verification:
- [ ] All text is readable (font size, contrast)
- [ ] Images scale properly (not stretched/squashed)
- [ ] Buttons are easily tappable (48dp minimum)
- [ ] No overlapping text or elements
- [ ] Proper spacing between sections
- [ ] ScrollView works smoothly

### Material Design:
- [ ] Colors consistent with app theme
- [ ] Icons render correctly
- [ ] Toolbar back button works
- [ ] Progress bar displays centered
- [ ] Empty states are visible and clear

### Responsive Design:
- [ ] Test on different screen sizes
- [ ] Landscape orientation doesn't break layout
- [ ] Keyboard doesn't hide important buttons

---

## Test Scenario 9: Performance Checks

### Image Loading:
1. Open news with large image
2. Verify: Image loads smoothly with Glide
3. Verify: No placeholder flashing (uses caching)

### Comments Loading:
1. Open news with 10+ comments
2. Verify: RecyclerView scrolls smoothly
3. Verify: No jank or stuttering

### Database Operations:
1. Check logcat for Firestore operations
2. Verify: No excessive read/write operations
3. Verify: Queries are efficient

---

## Test Scenario 10: Multi-User Testing

### Steps:
1. Open news on Device A (logged in as User1)
2. Open same news on Device B (logged in as User2)
3. Like news on Device A
4. Verify: Like count updates immediately on Device A
5. Refresh/reopen news on Device B
6. Verify: Like count shows 1 like

### Comments Multi-User:
1. User1 posts comment on Device A
2. User2 opens news on Device B
3. Verify: User1's comment appears on Device B
4. User2 posts reply on Device B
5. Verify: User2's comment appears on Device A (after refresh)

---

## Logcat Monitoring

### What to Look For:
```
// Expected logs:
D/NewsDetailActivity: News item loaded successfully
D/NewsDetailActivity: Comment posted successfully
D/NewsDetailActivity: Likes updated successfully

// Errors to investigate:
E/NewsDetailActivity: Error loading news
E/NewsDetailActivity: Error posting comment
E/FirebaseFirestore: FAILED: permission denied
```

---

## Success Criteria

✅ **All features working:**
- Create news with image upload
- View news in dashboard and detail view
- Like/unlike functionality with count
- Post and view comments
- All data persists across sessions

✅ **No crashes** on any test scenario

✅ **Good UX:**
- Clear feedback on all actions
- Reasonable load times
- Smooth animations
- Proper error handling

---

## Known Limitations

- Comments cannot be edited or deleted (future feature)
- Comment likes not implemented (future feature)
- News can't be edited (EditNewsItemActivity needs updating)
- News can't be deleted (future feature)

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Image won't upload | Check Firebase Storage permissions, internet connection |
| Likes not saving | Check Firestore permissions, user authentication |
| Comments disappear | Check Firestore database, verify array structure |
| Detail view crashes | Check logcat for NPE on NewsItem, verify ID passed correctly |
| Image placeholder shows | Check imageUrl is valid Firebase Storage URL |

---

**Test Environment:** Android 10+, Firebase Emulator (optional), Real Device or Emulator
**Last Updated:** December 26, 2025
