# Chat System - End-to-End Testing Checklist

## Overview
Complete testing guide for the BookUp chat system after all Phase 1-7 fixes.

**Current Build Status**: ✅ **SUCCESS** (Build completed 0 errors)

---

## Pre-Testing Setup

### Requirements
- [ ] Android device or emulator (API 24+)
- [ ] Two user accounts for testing (or two devices)
- [ ] Firebase project configured correctly
- [ ] Firebase Firestore with "chatChannels" or "conversations" collection
- [ ] Firebase Storage bucket configured
- [ ] Internet connectivity

### Build & Install
```bash
# Clean build
./gradlew clean build

# Generate APK
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Phase 1: Basic Navigation & UI

### Test 1.1: App Launch & Authentication
- [ ] App launches without crashes
- [ ] If not logged in, redirects to SignInActivity
- [ ] If logged in, navigates to HomePageActivity
- [ ] Bottom navigation visible with all tabs

### Test 1.2: Bottom Navigation
- [ ] Click Dashboard tab - loads DashboardFragment
- [ ] Click Chat tab - loads ChatListFragment with conversations
- [ ] Click AI Tutor tab - loads AIChatFragment
- [ ] Click Requests tab - loads RequestsFragment
- [ ] Click Profile tab - loads ProfileFragment
- [ ] All tabs load without crashes

### Test 1.3: Chat List Display
- [ ] ChatListFragment shows list of conversations
- [ ] Each conversation shows:
  - [ ] Conversation name/other user's name
  - [ ] Last message preview
  - [ ] Timestamp of last message
  - [ ] Unread badge (if unread > 0)
  - [ ] Profile picture (placeholder if not loaded)
- [ ] Search bar visible and clickable
- [ ] Toolbar shows "Chats" title

### Test 1.4: Navigation to Chat
- [ ] Click on a conversation
- [ ] ChatFragment opens with correct conversation ID
- [ ] ChatFragment displays:
  - [ ] Other user's name in toolbar
  - [ ] Profile picture in toolbar
  - [ ] Messages RecyclerView (may be empty initially)
  - [ ] Input bar with Send button
  - [ ] Attachment button visible
  - [ ] Emoji button visible
  - [ ] Microphone button visible
- [ ] No crashes or ANR (Application Not Responding)

---

## Phase 2: Text Messaging (P1 Fix Verification)

### Test 2.1: Send Text Message
**Setup**: Open ChatFragment with a conversation

- [ ] Type a message in input field
- [ ] Click Send button
- [ ] Message appears in RecyclerView immediately
- [ ] Message contains:
  - [ ] Your message text
  - [ ] Timestamp
  - [ ] Aligned to right (sent message)
  - [ ] Correct formatting
- [ ] Input field clears after send
- [ ] No duplicate messages

### Test 2.2: Receive Text Message
**Setup**: Send message from another device/user to current user

- [ ] Message appears in real-time (within 2-3 seconds)
- [ ] Message shows:
  - [ ] Sender's name (if group chat)
  - [ ] Message content
  - [ ] Timestamp
  - [ ] Aligned to left (received message)
- [ ] Correct formatting
- [ ] RecyclerView scrolls to show new message

### Test 2.3: Multiple Message Flow
- [ ] Send 5-10 messages rapidly
- [ ] All messages appear in correct order
- [ ] No messages are lost
- [ ] Smooth animations (if any)
- [ ] No lag or stuttering

### Test 2.4: Long Messages
- [ ] Type a very long message (100+ characters)
- [ ] Message wraps correctly
- [ ] Displays properly without truncation
- [ ] Timestamp visible

### Test 2.5: Special Characters
- [ ] Type emojis 😀🎉❤️
- [ ] Type special characters @#$%^&*
- [ ] Type different languages (if keyboard available)
- [ ] All display correctly without corruption

---

## Phase 3: UI Buttons & Profile Data (P2-P3 Fixes)

### Test 3.1: Profile Picture Loading
- [ ] Profile picture visible in ChatFragment toolbar
- [ ] If user has profile picture in Firestore, it loads with Glide
- [ ] If no profile picture, placeholder shows (ic_profile_black_24dp)
- [ ] No image loading crashes
- [ ] Images are circular/shaped correctly

### Test 3.2: Profile Picture Click
- [ ] Click on profile picture
- [ ] Shows toast "User profile coming soon" (or navigates to profile view)
- [ ] No crashes

### Test 3.3: Toolbar Menu
- [ ] Options menu visible (3-dot menu or top toolbar buttons)
- [ ] "New Chat" option appears
- [ ] Click "New Chat" shows toast "New Chat feature coming soon"
- [ ] Menu items don't overlap with title/profile

### Test 3.4: Input Bar Buttons

#### Attachment Button
- [ ] Click "Attachment" button (📎 icon)
- [ ] Bottom sheet appears with 4 options:
  - [ ] Camera
  - [ ] Gallery
  - [ ] Video
  - [ ] Document
- [ ] Click Camera → proceeds to camera (see P5 tests)
- [ ] Click Gallery → proceeds to gallery (see P5 tests)
- [ ] Click Video → proceeds to video (see P6 tests)
- [ ] Click Document → proceeds to document picker (see P6 tests)

#### Emoji Button
- [ ] Click "Emoji" button (😊 icon)
- [ ] Shows toast "Emoji picker coming soon"
- [ ] No crashes

#### Mic Button
- [ ] Button visible and clickable
- [ ] Pressing starts audio recording (see P4 tests)

#### Send Button
- [ ] Button visible and highlighted (filled color)
- [ ] Clicking sends messages (already tested in Phase 2)

---

## Phase 4: Audio Recording & Playback (P4 Fix)

### Test 4.1: Audio Recording Setup
- [ ] Permissions granted when first launching recording
- [ ] RECORD_AUDIO permission shown in system dialog (if first time)
- [ ] Permission dialog has "Allow" button
- [ ] Accepting permission enables audio recording

### Test 4.2: Record Audio Message
- [ ] Press and hold microphone button
- [ ] Recording starts (visual feedback would be nice, currently shows typing indicator)
- [ ] Release button to stop recording
- [ ] Audio file created
- [ ] Audio is uploaded to Firebase Storage
- [ ] Progress indicator shows upload progress
- [ ] Audio message appears in chat with:
  - [ ] Audio icon/indicator
  - [ ] Play button
  - [ ] Duration display
  - [ ] Timestamp
  - [ ] Correct alignment (right for sent)

### Test 4.3: Audio Playback
- [ ] Click play button on audio message
- [ ] Audio plays through device speakers
- [ ] Play button changes to show "Playing..." state
- [ ] Audio continues playing for full duration
- [ ] Play button returns to "▶ Play" after completion
- [ ] No crashes during playback

### Test 4.4: Multiple Audio Messages
- [ ] Record and send 2-3 audio messages
- [ ] All show in chat
- [ ] Can play each one independently
- [ ] Playing one stops the other (ideally)

### Test 4.5: Audio Error Handling
- [ ] If recording fails, shows error toast
- [ ] If upload fails, shows error toast
- [ ] App remains stable after errors

---

## Phase 5: Photo Capture & Upload (P5 Fix)

### Test 5.1: Camera Photo Capture
- [ ] Click Attachment → Camera
- [ ] Camera app launches
- [ ] Take a photo
- [ ] Accept photo (click checkmark)
- [ ] Photo is compressed and uploaded
- [ ] Progress indicator shows upload progress
- [ ] Photo message appears in chat with:
  - [ ] Photo thumbnail visible
  - [ ] Correct size and aspect ratio
  - [ ] Timestamp
  - [ ] Correct alignment (right for sent)

### Test 5.2: Gallery Photo Pick
- [ ] Click Attachment → Gallery
- [ ] Gallery app launches
- [ ] Select an image
- [ ] Image is uploaded to Firebase Storage
- [ ] Progress indicator shows upload progress (percentage)
- [ ] Image message appears in chat
- [ ] Image displays correctly

### Test 5.3: Photo Display
- [ ] Click on photo in chat
- [ ] Photo preview shows (can be expanded if implemented)
- [ ] Quality is acceptable
- [ ] No artifacts or compression issues
- [ ] Fast loading (cached or optimized)

### Test 5.4: Multiple Photos
- [ ] Send 3-4 photos in sequence
- [ ] All appear in chat in correct order
- [ ] No upload failures
- [ ] All are visible and clickable

### Test 5.5: Camera Permission Handling
- [ ] First camera usage shows permission dialog
- [ ] Denying permission shows toast and returns to chat
- [ ] Allowing permission enables camera capture

---

## Phase 6: Video Recording & Upload (P6 Fix)

### Test 6.1: Video Recording
- [ ] Click Attachment → Video
- [ ] Camera app launches in video mode
- [ ] Record a short video (5-10 seconds)
- [ ] Stop recording (click stop button)
- [ ] Confirm video
- [ ] Video is uploaded to Firebase Storage
- [ ] Progress indicator shows upload progress
- [ ] Video message appears in chat with:
  - [ ] Video preview/thumbnail
  - [ ] Duration indicator (if supported)
  - [ ] Play button overlay
  - [ ] Timestamp
  - [ ] Correct alignment

### Test 6.2: Video Playback
- [ ] Click play button on video message
- [ ] Video player launches (VideoView or MediaPlayer)
- [ ] Video plays in full quality
- [ ] Controls visible (play/pause, seek bar)
- [ ] Audio from video plays
- [ ] Can seek through video
- [ ] Video stops when reaching end

### Test 6.3: Video Gallery Pick
- [ ] Click Attachment → Video → Choose from gallery option (if implemented)
- [ ] Gallery app shows only videos
- [ ] Select a video
- [ ] Video is uploaded
- [ ] Appears in chat correctly

### Test 6.4: Long Video Handling
- [ ] Try to send a longer video (1-2 minutes)
- [ ] Upload may take longer, progress should show
- [ ] No timeout or disconnection issues
- [ ] Video message completes successfully

### Test 6.5: Video Error Handling
- [ ] If video upload fails, shows error message
- [ ] User can retry upload
- [ ] App remains stable

---

## Phase 7: File/Document Upload (P6 + Extended)

### Test 7.1: Document Picker
- [ ] Click Attachment → Document
- [ ] Document picker launches
- [ ] Can select PDF, DOCX, TXT, etc.
- [ ] Document is uploaded to Firebase Storage
- [ ] Document message appears in chat with:
  - [ ] Document icon
  - [ ] File name
  - [ ] File size
  - [ ] Timestamp
  - [ ] Download/open capability (if implemented)

### Test 7.2: Multiple File Types
- [ ] Upload PDF file
- [ ] Upload Word document
- [ ] Upload text file
- [ ] All appear in chat correctly

---

## Phase 8: Real-Time Updates

### Test 8.1: Receive New Messages
**Setup**: Two devices/users

- [ ] User A sends message
- [ ] User B receives message in real-time (< 2 seconds)
- [ ] Message appears without refresh
- [ ] RecyclerView scrolls to show new message

### Test 8.2: Conversation List Updates
- [ ] User A sends message in conversation 1
- [ ] User B's ChatListFragment updates immediately
- [ ] Conversation 1 moves to top
- [ ] Last message preview updates
- [ ] Timestamp updates

### Test 8.3: Multiple Conversations
- [ ] Have conversations with multiple users
- [ ] Receive messages in different conversations
- [ ] ChatListFragment updates correctly
- [ ] ChatFragment shows correct messages for selected conversation

### Test 8.4: Network Interruption
- [ ] Turn off WiFi/mobile data
- [ ] Try to send message → shows error
- [ ] Turn data back on
- [ ] Message sends successfully (if pending)
- [ ] Or shows clear error state

---

## Phase 9: Data Migration (P7 - If Applicable)

### Test 9.1: Check Migration Status
- [ ] Run DataMigrationManager.checkMigrationStatus()
- [ ] Displays current status
- [ ] Shows if migration is needed

### Test 9.2: Execute Migration (If Needed)
- [ ] Run DataMigrationManager.migrateData()
- [ ] Progress shows: "0/N" to "N/N"
- [ ] All conversations migrate successfully
- [ ] No data loss

### Test 9.3: Post-Migration Verification
- [ ] Chat functions still work
- [ ] All messages appear
- [ ] Can send/receive messages
- [ ] Navigation still works

---

## Phase 10: Stress Testing

### Test 10.1: Rapid Message Sending
- [ ] Send 10-20 messages in quick succession
- [ ] All appear in correct order
- [ ] No duplicate messages
- [ ] No crashes
- [ ] Performance remains smooth

### Test 10.2: Large Messages
- [ ] Type very long text message (1000+ characters)
- [ ] Message sends and displays correctly
- [ ] No truncation
- [ ] Text wrapping works

### Test 10.3: Memory Usage
- [ ] Open app
- [ ] Check memory usage in Settings > Apps
- [ ] Scroll through many messages (100+)
- [ ] Memory should not grow unbounded
- [ ] No memory leak warnings in logcat

### Test 10.4: Battery Usage
- [ ] Keep app open for 10+ minutes
- [ ] Receive multiple messages
- [ ] Battery drain should be acceptable
- [ ] Background listeners should not drain battery excessively

---

## Phase 11: Error Handling & Edge Cases

### Test 11.1: No Internet
- [ ] Turn off all connectivity
- [ ] Try to send message → shows error
- [ ] Error message is clear
- [ ] Turn connectivity back on
- [ ] App recovers gracefully

### Test 11.2: No Conversations
- [ ] Fresh user with no conversations
- [ ] ChatListFragment shows empty state
- [ ] Empty state message is visible
- [ ] No crashes

### Test 11.3: Deleted Conversation
- [ ] Delete a conversation from Firestore (or implement delete)
- [ ] ChatListFragment updates
- [ ] Conversation disappears from list

### Test 11.4: Invalid Conversation ID
- [ ] Somehow navigate with invalid conversation ID
- [ ] App shows error gracefully
- [ ] Can navigate back

---

## Phase 12: UI/UX Polish

### Test 12.1: Landscape Orientation
- [ ] Open chat in portrait
- [ ] Rotate to landscape
- [ ] Chat displays correctly
- [ ] All buttons visible
- [ ] Input bar works correctly
- [ ] No layout issues

### Test 12.2: Dark Mode (If Supported)
- [ ] Enable dark mode in system settings
- [ ] App uses dark theme
- [ ] Text is readable
- [ ] Images display correctly
- [ ] No contrast issues

### Test 12.3: Font Sizes
- [ ] Test with system font size settings
- [ ] Small font - still readable
- [ ] Large font - no layout breaks
- [ ] Text not cut off

### Test 12.4: Scrolling Performance
- [ ] Scroll through 100+ messages
- [ ] Scrolling is smooth
- [ ] No jank or frame drops
- [ ] Memory doesn't spike

---

## Regression Testing Checklist

After all phases, verify:

- [ ] No new crashes introduced
- [ ] All previous fixes still work (from earlier sessions)
- [ ] Message display works (P1 fix)
- [ ] Navigation works (P1 fix)
- [ ] UI buttons wired (P2 fix)
- [ ] Profile data loads (P3 fix)
- [ ] Audio records/plays (P4 fix)
- [ ] Photos upload (P5 fix)
- [ ] Videos upload (P6 fix)
- [ ] Documents upload (P6+ fix)
- [ ] Real-time updates work
- [ ] Migration available if needed (P7)

---

## Known Limitations & TODOs

### Not Yet Implemented
- [ ] User search / new conversation creation
- [ ] Emoji picker (shows placeholder toast)
- [ ] User profile view (shows placeholder toast)
- [ ] Message edit/delete
- [ ] Message reactions
- [ ] Read receipts
- [ ] Typing indicators (implemented but not fully wired)
- [ ] Call functionality
- [ ] Group chat management
- [ ] Message search

### In Progress
- [ ] Profile picture loading from Firestore (P3 - basic done, enhanced fetching)
- [ ] Online status indicator (UI ready, logic pending)

---

## Test Report Template

```
Date: ___________
Tester: ___________
Device: _________ (Model, OS Version)
Build: _________ (Version, Build Number)

Tests Passed: _____ / _____
Tests Failed: _____ 
Tests Skipped: _____

Critical Issues Found:
- 

Minor Issues Found:
- 

Recommendations:
- 

Overall Status: [ ] PASS [ ] PASS WITH NOTES [ ] FAIL
```

---

## Quick Test (5-10 minutes)

If you only have limited time, run this quick test:

1. [ ] Launch app
2. [ ] Navigate to Chat tab
3. [ ] Click on a conversation
4. [ ] Send a text message
5. [ ] Receive a message from another user
6. [ ] Take a photo and send it
7. [ ] Record audio and send it
8. [ ] Verify message appears in other device
9. [ ] No crashes during any operation

**If all above pass, basic functionality is working!**

---

**Last Updated**: 2025-12-23
**Test Status**: Ready for execution
**Estimated Time**: 2-3 hours for full test suite
**Estimated Time (Quick)**: 5-10 minutes
