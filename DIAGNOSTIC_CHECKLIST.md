# Chat/Upload Diagnostic Checklist (Dec 24, 2025)

## What to Test & What to Look For

### 1. Text Message Send/Receive
**Test:**
- Open app → go to a conversation
- Type "Test message" and tap Send
- Check if message appears immediately in the chat

**If FAILS, check logcat for:**
- `ChatRepository: ❌ Failed to send message` — indicates Firestore write error
- `Permission denied` — indicates Firestore rules issue or auth failure
- `conversationId is null` — indicates the conversation wasn't initialized properly

### 2. Image Upload
**Test:**
- In conversation, tap image icon
- Select a photo from gallery
- Should see "Uploading..." and then message with image

**If FAILS, check logcat for:**
- `StorageRepository: ❌ Failed to upload file` — Firebase Storage write error
- `Permission denied` — Storage rules blocking upload
- `File not found` — URI conversion issue (createFileFromUri failed)
- `NullPointerException` in uploadImageUri

### 3. Audio Recording
**Test:**
- Tap microphone icon
- Speak for 3 seconds
- Tap "Send" button

**If FAILS, check logcat for:**
- `AudioRecordingService: ❌ stopRecording failed` — MediaRecorder issue
- `IllegalStateException` — recorder not in correct state
- `Permission denied: RECORD_AUDIO` — runtime permission not granted

### 4. Video Recording/Upload
**Test:**
- Tap video icon → camera
- Record 3 seconds of video
- Tap send

**If FAILS, check logcat for:**
- Same as image upload + video-specific errors
- Camera permission errors

### 5. Document Upload
**Test:**
- Tap attachment/document icon
- Pick a PDF or text file
- Should upload and appear as message

**If FAILS, check logcat for:**
- Storage rules errors
- File MIME type issues

### 6. Conversation Load
**Test:**
- Open ChatListFragment
- Does conversation list appear with last message previews?

**If FAILS, check logcat for:**
- `ChatRepository: Error loading conversations` — Firestore read error
- `Empty participantIds` — conversation structure issue

---

## How to Capture Logcat

### On Android Studio:
1. Run the app on emulator/device
2. Bottom menu → **Logcat** tab
3. Search/filter for: `ChatRepository`, `StorageRepository`, `AudioRecordingService`, `ChatFragment`
4. Reproduce the issue
5. Copy any error logs and paste here

### On Terminal (adb):
```bash
adb logcat | grep -E "ChatRepository|StorageRepository|AudioRecordingService|ChatFragment" > logcat.txt
# Then reproduce the issue, wait 30 seconds, Ctrl+C
cat logcat.txt
```

---

## What to Report Back

For EACH failing feature, provide:
1. **Feature**: (Text message / Image / Video / Audio / Document / Conversation load)
2. **Expected**: (What should happen)
3. **Actual**: (What actually happens)
4. **Logcat error**: (Copy relevant error line from logcat)

---

## Example Report
```
Feature: Image Upload
Expected: Image selected → "Uploading..." → message appears with image
Actual: Dialog opens, image selected, then nothing happens (no error toast)
Logcat: 
  E/StorageRepository: ❌ Failed to upload file: java.lang.NullPointerException: Attempt to invoke virtual method 'boolean java.io.File.exists()' on a null object reference
```

---

Once you provide this info, I can pinpoint and fix the exact issues.
