# Quick Debugging Guide - BookUp Chat Runtime Issues

## 🚀 Quick Start: Testing the Fixes

### Step 1: Build and Install
```bash
cd /Users/user/AndroidStudioProjects/BookUp
./gradlew build
# Install on device/emulator
```

### Step 2: Start Logcat Monitoring
```bash
adb logcat | grep -E "AudioRecordingService|StorageRepository|ChatFragment"
```

### Step 3: Test Each Feature

---

## 📱 Testing Audio Recording

**How to Test:**
1. Open chat conversation
2. Press and hold the microphone button (bottom right)
3. Speak for at least 1 second
4. Release the button

**Success Indicators in Logcat:**
```
✅ MediaRecorder created
✅ Audio source set to MIC
✅ Output format set to MPEG-4
✅ Audio encoder set to AAC
✅ Sample rate set to 44100 Hz
✅ Bit rate set to 128000 bps
✅ Channels set to 1 (mono)
✅ MediaRecorder prepared
✅ Recording started
... (after release)
Recording stopped. Duration: XXXX ms
```

**Failure Scenarios:**

| Issue | Log Pattern | Solution |
|-------|------------|----------|
| Recording won't start | ❌ IOException when starting recording | Check mic permission |
| Recording won't stop | ❌ MediaRecorder in invalid state when stopping | Try again, check logcat for state info |
| Recording too short | Recording too short: XXXms (minimum 1 second) | Record longer, minimum 1 second |

---

## 📷 Testing Camera Capture

**How to Test:**
1. Open chat conversation
2. Tap the attachment button (📎)
3. Tap "Camera"
4. Take a photo
5. Confirm/send

**Success Indicators in Logcat:**
```
Launching camera with intent: android.media.action.IMAGE_CAPTURE
✅ Camera intent launched successfully
... (after taking photo)
Starting image upload: IMG_XXXXXXXX.jpg (Size: XXXX bytes)
Upload destination: chat_media/images/conversationId/messageId.jpg
MIME type: image/jpeg
File URI: file:///...
Upload progress: 0% (0 B / XXX KB)
... (progress updates)
Upload progress: 100% (XXX KB / XXX KB)
✅ Download URL obtained: https://firebasestorage...
```

**Failure Scenarios:**

| Issue | Log Pattern | Solution |
|-------|------------|----------|
| Camera won't open | ❌ No camera app available on device | Device may not have camera app |
| Camera won't open | ❌ Failed to launch camera: [Exception] | Check exception details in logs |
| Permission denied | ❌ Camera permission not granted | Grant camera permission in settings |
| Image upload fails | ❌ File does not exist: /path/to/image | Camera app may not save image properly |

---

## 🎥 Testing Video Capture

**How to Test:**
1. Open chat conversation
2. Tap the attachment button (📎)
3. Tap "Video"
4. Record a video (max 5 minutes)
5. Confirm/send

**Success Indicators in Logcat:**
```
Launching video recorder with intent: android.media.action.VIDEO_CAPTURE
✅ Video intent launched successfully
... (after recording)
Starting video upload: XXX.mp4 (Size: XXXX KB)
Upload destination: chat_media/videos/conversationId/messageId.mp4
MIME type: video/mp4
File URI: file:///...
Upload progress: X% (XXX MB / XXX MB)
... (progress updates)
✅ Download URL obtained: https://firebasestorage...
```

**Failure Scenarios:**

| Issue | Log Pattern | Solution |
|-------|------------|----------|
| Video won't record | ❌ No video recorder app available | Device may not support video recording |
| Video upload slow | Upload progress: XX% | Network issue, wait for completion |
| Video upload fails | ❌ Upload failed: chat_media/videos/... | Check Firebase Storage rules, quota |

---

## 🖼️ Testing Image Upload (Gallery)

**How to Test:**
1. Open chat conversation
2. Tap the attachment button (📎)
3. Tap "Gallery"
4. Select an image
5. Send

**Success Indicators in Logcat:**
```
uploadImageUri called with URI: content://media/external/...
✅ Image file created from URI: /data/user/.../IMG_XXXXXXXX.jpg
Starting image upload: IMG_XXXXXXXX.jpg (Size: XXXX KB)
Upload destination: chat_media/images/conversationId/messageId.jpg
MIME type: image/jpeg
File URI: file:///...
Upload progress: 0% ... 100%
✅ Download URL obtained: https://firebasestorage...
```

**Failure Scenarios:**

| Issue | Log Pattern | Solution |
|-------|------------|----------|
| No images shown | No logs appear | Storage permission may not be granted |
| Image selected but won't upload | ❌ Failed to create image file from URI | Try selecting image again |
| Upload fails | ❌ File exceeds max size | Image too large (>100MB) |
| Upload fails | ❌ File is empty | Try another image, file may be corrupted |

---

## 📄 Testing Document Upload

**How to Test:**
1. Open chat conversation
2. Tap the attachment button (📎)
3. Tap "Document"
4. Select a PDF or document
5. Send

**Success Indicators in Logcat:**
```
uploadDocumentUri called with URI: content://com.android.providers.downloads...
✅ Document file created from URI: /data/user/.../document.pdf
Starting document upload: document.pdf (Size: XXXX KB)
Upload destination: chat_media/documents/conversationId/messageId.pdf
MIME type: application/pdf
Upload progress: 0% ... 100%
✅ Download URL obtained: https://firebasestorage...
```

**Failure Scenarios:**

| Issue | Log Pattern | Solution |
|-------|------------|----------|
| No documents shown | No logs appear | Storage permission may not be granted |
| Document selected but won't upload | ❌ Failed to create document file from URI | Try selecting document again |
| Upload fails | ❌ File exceeds max size | Document too large (>100MB) |

---

## 🔍 Common Issues & Solutions

### Issue 1: "No internet connection"
**Symptoms**: Upload starts but hangs
**Check in logcat**: `Upload progress: 0%` then nothing
**Solution**: 
- Check device internet connection
- Try on WiFi instead of cellular
- Check if Firebase Storage bucket is accessible

### Issue 2: "Permission denied" 
**Symptoms**: Camera/photo/video/document features say "Permission not granted"
**Check in logcat**: `Camera permission not granted, requesting...`
**Solution**:
- Open device Settings
- Find BookUp app
- Grant Camera, Microphone, Storage permissions
- Restart app

### Issue 3: "Firebase Storage disabled"
**Symptoms**: Uploads fail immediately
**Check in logcat**: `❌ Upload failed` with exception about Firebase
**Solution**:
- Check Firebase Console -> Storage -> Rules
- Ensure rules allow authenticated user uploads
- Verify storage bucket exists in correct region

### Issue 4: "File not found"
**Symptoms**: Upload fails with "File does not exist"
**Check in logcat**: `⚠️ File validation failed: File does not exist: /path/...`
**Solution**:
- Camera/gallery app may not be saving properly
- Try taking photo/video directly in chat app
- Try selecting different image/document

---

## 📊 Upload Failure Troubleshooting Tree

```
Upload fails?
├─ Check logcat for exception
│  ├─ "File does not exist"
│  │  └─ Try selecting file again, restart app
│  ├─ "File exceeds max size"
│  │  └─ Choose smaller file (<100MB)
│  ├─ "File is empty"
│  │  └─ File may be corrupted, try different file
│  ├─ "Permission denied"
│  │  └─ Grant permissions in Settings
│  └─ Other Firebase Storage exception
│     └─ Check Firebase Console -> Storage
└─ No exception, just hangs
   ├─ Check internet connection
   ├─ Check Firebase Storage quotas
   └─ Try again after network recovers
```

---

## 🎯 Logcat Filtering Cheat Sheet

**View all chat/upload logs:**
```bash
adb logcat | grep -E "ChatFragment|StorageRepository|AudioRecordingService"
```

**View only errors:**
```bash
adb logcat | grep "❌"
```

**View only successes:**
```bash
adb logcat | grep "✅"
```

**View specific feature:**
```bash
# Audio only
adb logcat | grep "AudioRecordingService"

# Uploads only
adb logcat | grep "StorageRepository"

# Camera/video intents
adb logcat | grep "launch"
```

**Clear logcat before testing:**
```bash
adb logcat -c
```

---

## 📋 Pre-Testing Checklist

- [ ] Device/emulator connected via adb
- [ ] App built and installed: `./gradlew build`
- [ ] Logcat window open and filtering
- [ ] Device has internet connection
- [ ] Device has camera app installed
- [ ] Storage/camera/microphone permissions granted
- [ ] Firebase Storage rules configured correctly
- [ ] Conversation is properly loaded

---

## 🐛 Providing Logs for Debugging

If something still doesn't work:

1. **Clear logcat**:
   ```bash
   adb logcat -c
   ```

2. **Reproduce the issue**:
   - Follow exact steps above

3. **Capture logs**:
   ```bash
   adb logcat | grep -E "ChatFragment|StorageRepository|AudioRecordingService" > debug_logs.txt
   ```

4. **Share output** including:
   - What feature was being tested
   - Exact steps taken
   - Screenshot of error message (if any)
   - Contents of debug_logs.txt with the ❌ error messages

---

## ✅ Expected Behavior After Fixes

| Feature | Before | After |
|---------|--------|-------|
| Audio Recording | May fail to stop | Clear error messages in logcat, can retry |
| Camera Capture | "Camera not opening" | Gets intent error if no camera app, logs clarify issue |
| Video Capture | "Video not recording" | Clear indication if device doesn't support it |
| Image Upload | "Generic upload error" | Detailed file size, destination, MIME type logged |
| Video Upload | "Upload fails silently" | Progress tracking with percentage shown |
| Document Upload | "No feedback" | Detailed upload progress and clear errors |

---

**Status**: ✅ All runtime error handling implemented  
**Build**: ✅ Successful (0 errors)  
**Ready for**: Testing on device/emulator with logcat monitoring

