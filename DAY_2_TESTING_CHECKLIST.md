# Day 2: Voice Recording & Media Testing Checklist

**Date**: December 18, 2025  
**Duration**: 6 hours estimated  
**Status**: IN PROGRESS

---

## Overview

Day 2 focuses on testing the real-time audio & media features across multiple Android versions and permission scenarios. This is critical for MVP as voice recording is a core feature (users send voice notes in chat).

---

## Section 1: Voice Recording Testing (3 hours)

### Implementation Summary
**File**: `ChatActivity.java` (lines 895-970)

**Key Components**:
- ✅ MediaRecorder with AAC codec at 44.1kHz
- ✅ Output format: MPEG-4 (.m4a files)
- ✅ Bitrate: 128kbps
- ✅ Permission handling for Android 6.0+
- ✅ Minimum 1-second recording requirement
- ✅ Direct upload to Firebase Storage after recording

**Code Flow**:
1. User taps audio button
2. Permission check (Android 6+)
3. MediaRecorder initializes & starts
4. User speaks (records audio)
5. User taps again to stop
6. File validated (min 1 sec)
7. Upload to Firebase Storage

---

### Test Plan A: Basic Recording Functionality

#### A1: Short Recording (<1 second) - SHOULD FAIL
- [ ] **Device**: Android 12+ (test device name: ________)
- [ ] **Action**: 
  - Open chat
  - Tap audio button
  - Hold for 0.5 seconds
  - Release
- [ ] **Expected**: Toast: "Recording too short (minimum 1 second)"
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

#### A2: Standard Recording (3-5 seconds) - SHOULD SUCCEED
- [ ] **Device**: Android 12+
- [ ] **Action**:
  - Open chat
  - Tap audio button
  - Hold for 4 seconds (speak clearly: "Hello, this is a test")
  - Release
- [ ] **Expected**: 
  - Toast: "Recording... (tap to stop)"
  - Upload progress shown
  - Message appears in chat with audio attachment
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

#### A3: Long Recording (15+ seconds) - SHOULD SUCCEED
- [ ] **Device**: Android 12+
- [ ] **Action**:
  - Open chat
  - Tap audio button
  - Hold for 20 seconds (speak: "This is a longer test message to verify that the system can handle extended recordings without issues")
  - Release
- [ ] **Expected**: 
  - Upload succeeds
  - Audio metadata shows ~20 seconds
  - No crashes or freezes
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

---

### Test Plan B: Permission Handling

#### B1: First-Time Permission Request (Android 6+)
- [ ] **Device**: Android 10 (need to clear app cache first)
- [ ] **Prerequisites**: App not given microphone permission yet
- [ ] **Action**:
  - Open ChatActivity
  - Tap audio button
  - See permission dialog
  - Tap "Allow"
- [ ] **Expected**: 
  - Permission dialog appears
  - Recording starts after granting permission
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

#### B2: Deny Permission (Android 6+)
- [ ] **Device**: Android 10
- [ ] **Prerequisites**: Clear app permissions
- [ ] **Action**:
  - Open ChatActivity
  - Tap audio button
  - See permission dialog
  - Tap "Deny"
- [ ] **Expected**: 
  - Toast: "Microphone permission is required to record voice notes"
  - No recording starts
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

#### B3: Permission Already Granted (Quick Flow)
- [ ] **Device**: Android 12+
- [ ] **Prerequisites**: Permission already granted
- [ ] **Action**:
  - Open ChatActivity
  - Tap audio button
  - Should start immediately without dialog
- [ ] **Expected**: 
  - No permission dialog
  - Recording starts immediately
  - Toast: "Recording... (tap to stop)"
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

---

### Test Plan C: Cross-Android Version Testing

#### C1: Android 6 (API 23)
- [ ] **Device**: Android 6 emulator
- [ ] **Tests**: A2 (standard recording), B1 (permission), permission caching
- [ ] **Expected**: Full functionality
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP (device unavailable)
- [ ] **Notes**: ________________

#### C2: Android 8 (API 26)
- [ ] **Device**: Android 8 emulator
- [ ] **Tests**: A2, B1, codec compatibility
- [ ] **Expected**: Full functionality, verify AAC codec support
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP
- [ ] **Notes**: ________________

#### C3: Android 10 (API 29)
- [ ] **Device**: Android 10 emulator/device
- [ ] **Tests**: A2, B1, scoped storage handling
- [ ] **Expected**: Full functionality, verify file caching
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP
- [ ] **Notes**: ________________

#### C4: Android 12+ (API 31+)
- [ ] **Device**: Android 12+ device (real device preferred)
- [ ] **Tests**: A1, A2, A3, B1-B3
- [ ] **Expected**: All tests pass
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP
- [ ] **Notes**: ________________

---

### Test Plan D: Error Handling & Edge Cases

#### D1: Microphone Unavailable (Low-end Device)
- [ ] **Device**: Low-end Android device (if available)
- [ ] **Action**: 
  - Tap audio button
  - System doesn't have microphone support
- [ ] **Expected**: 
  - Graceful error message (not crash)
  - Toast or error shown
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP
- [ ] **Notes**: ________________

#### D2: Storage Full Scenario
- [ ] **Device**: Android 12+
- [ ] **Prerequisite**: Simulate low storage (fill device to 95%+ or use developer options)
- [ ] **Action**: 
  - Tap audio button
  - Try to record
- [ ] **Expected**: 
  - Graceful failure (not crash)
  - Toast: "Failed to save recording: ..." or similar
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP
- [ ] **Notes**: ________________

#### D3: Recording While Network Offline
- [ ] **Device**: Android 12+
- [ ] **Action**:
  - Enable airplane mode
  - Tap audio button, record 3 seconds
  - Turn airplane mode off
- [ ] **Expected**: 
  - Recording succeeds (stored locally)
  - Upload queued or retried after network returns
  - Eventually uploaded successfully
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP
- [ ] **Notes**: ________________

#### D4: Rapid Tap (Click button multiple times)
- [ ] **Device**: Android 12+
- [ ] **Action**:
  - Tap audio button rapidly 5+ times
  - Don't hold
- [ ] **Expected**: 
  - Only one recording starts (not multiple simultaneous)
  - No crash or corruption
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP
- [ ] **Notes**: ________________

---

### Audio File Validation

After each successful recording, verify:

- [ ] **File Created**: Audio file exists in cache directory
- [ ] **File Size**: Not suspiciously large (should be ~50-200KB for 3-5 sec recording)
- [ ] **Codec**: AAC format (.m4a file extension)
- [ ] **Duration**: Matches expected (3 sec recording = ~3 seconds duration)
- [ ] **Playback**: Can be played back in chat message

**File Path Pattern**: `{externalCacheDir}/voice_{timestamp}.m4a`

**Example**: `/storage/emulated/0/Android/data/com.example.bookup/cache/voice_1702876543210.m4a`

---

## Section 2: Image & Video Upload Testing (2 hours)

### Implementation Summary
**File**: `ChatActivity.java` - `uploadMediaFile()` method

**Supported Formats**:
- Images: JPG, PNG, GIF
- Videos: MP4, MOV
- Upload destination: Firebase Cloud Storage (`/chatMedia/{chatChannelId}/`)

---

### Test Plan E: Image Upload

#### E1: Single Image Upload (JPG)
- [ ] **Device**: Android 12+
- [ ] **Action**:
  - Open chat
  - Tap image/attachment button
  - Select JPG image from gallery
  - Confirm upload
- [ ] **Expected**: 
  - Upload progress shown (if >5MB)
  - Image appears in chat as thumbnail
  - Can tap to open full size
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

#### E2: PNG Image Upload
- [ ] **Device**: Android 12+
- [ ] **Action**: Same as E1, select PNG image
- [ ] **Expected**: Same as E1
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

#### E3: Large Image (>5MB)
- [ ] **Device**: Android 12+
- [ ] **Action**:
  - Select high-resolution image (>5MB)
  - Confirm upload
- [ ] **Expected**: 
  - Progress indicator visible
  - Upload completes (may take 10-30 seconds depending on network)
  - No crashes during upload
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

---

### Test Plan F: Video Upload

#### F1: Short Video Upload (MP4, <30MB)
- [ ] **Device**: Android 12+
- [ ] **Action**:
  - Open chat
  - Tap video button or attachment
  - Select MP4 video (15 seconds, ~5-10MB)
  - Confirm upload
- [ ] **Expected**: 
  - Upload progress shown
  - Video thumbnail appears
  - Can tap to watch preview
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

#### F2: Larger Video (>30MB)
- [ ] **Device**: Android 12+ (Wi-Fi recommended)
- [ ] **Action**: Select larger video file (>30MB)
- [ ] **Expected**: 
  - Upload may take longer
  - Progress shown
  - No timeout or crash
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

#### F3: MOV Video Format
- [ ] **Device**: Android 12+
- [ ] **Action**: Select MOV video from gallery (if available)
- [ ] **Expected**: Uploads successfully
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP (MOV not in gallery)
- [ ] **Notes**: ________________

---

### Test Plan G: Media Upload Edge Cases

#### G1: Unsupported Format (PDF)
- [ ] **Device**: Android 12+
- [ ] **Action**:
  - Try to send PDF file
  - (Depending on implementation, may be filtered)
- [ ] **Expected**: 
  - Either: Rejected with message "Unsupported file type"
  - Or: Filtered out of file picker
- [ ] **Result**: ✅ PASS / ❌ FAIL / N/A
- [ ] **Notes**: ________________

#### G2: Upload While Offline
- [ ] **Device**: Android 12+
- [ ] **Action**:
  - Enable airplane mode
  - Try to upload image
  - Turn airplane mode off
- [ ] **Expected**: 
  - Either: Queued for retry
  - Or: Error message with retry option
  - Eventually uploads successfully
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP
- [ ] **Notes**: ________________

#### G3: Cancel Mid-Upload
- [ ] **Device**: Android 12+
- [ ] **Action**:
  - Start uploading large file
  - Before completion, tap back or cancel
- [ ] **Expected**: 
  - Upload stops gracefully
  - No orphaned files in Storage
  - No crash
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

---

## Section 3: Permission Testing Across Versions (1 hour)

### Permissions Required
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

---

### Test Plan H: Runtime Permission Flow

#### H1: Android 6-7 (API 23-24) - First Permissions
- [ ] **Device**: Android 6 or 7 emulator
- [ ] **Action**:
  - Fresh install (app data cleared)
  - Tap audio button
  - See permission request
  - Grant permission
- [ ] **Expected**: 
  - Permission dialog shown
  - Dialog is system standard (not custom)
  - Permission granted after tapping "Allow"
  - Recording starts
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP
- [ ] **Notes**: ________________

#### H2: Android 10+ (API 29+) - Scoped Storage
- [ ] **Device**: Android 10+ emulator/device
- [ ] **Action**: Same as H1
- [ ] **Expected**: 
  - Permissions dialog uses modern UI
  - All permissions can be granted
  - Recording and upload work normally
- [ ] **Result**: ✅ PASS / ❌ FAIL / 🚫 SKIP
- [ ] **Notes**: ________________

#### H3: Verify Permission State Persists
- [ ] **Device**: Android 12+
- [ ] **Action**:
  - Tap audio (grant permission)
  - Close app
  - Reopen app
  - Tap audio again
- [ ] **Expected**: 
  - No permission dialog second time
  - Recording starts immediately
- [ ] **Result**: ✅ PASS / ❌ FAIL
- [ ] **Notes**: ________________

---

## Test Summary

### Overall Results

**Voice Recording**: _____ / _____ tests passed

**Image Upload**: _____ / _____ tests passed

**Video Upload**: _____ / _____ tests passed

**Permission Handling**: _____ / _____ tests passed

**Cross-Android**: _____ / _____ versions tested

---

### Critical Issues Found
(List any crashes, data corruption, or security issues)

1. ________________
2. ________________
3. ________________

---

### Minor Issues Found
(Non-blocking issues for MVP)

1. ________________
2. ________________
3. ________________

---

### Sign-Off

- [ ] **All Critical Tests Passed**: ✅ YES / ❌ NO
- [ ] **MVP Audio/Media Features Verified**: ✅ YES / ❌ NO
- [ ] **Ready for Day 3 (UI Polish)**: ✅ YES / ❌ NO

**Tested By**: ___________________  
**Date**: ___________________  
**Duration**: ___________________

---

## Notes for Future Versions

- [ ] Consider adding audio compression options
- [ ] Add audio visualization during recording (waveform)
- [ ] Implement batch media upload
- [ ] Add media preview before sending
- [ ] Consider transcription API integration (Phase 4)

