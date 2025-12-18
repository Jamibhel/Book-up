# Firebase Storage Setup & Debugging Guide

## Current Configuration

### Storage Paths
- Media uploads stored in: `chats/{chatChannelId}/{filename}`
- Naming pattern: `{timestamp}_{mediaType}`

### Current Implementation Status
✅ Image picker implemented
✅ Video picker implemented  
✅ Upload to Firebase Storage implemented
✅ Firestore message creation implemented
✅ Comprehensive logging added for debugging

## Security Rules - Cloud Storage

Firebase Storage security rules should be configured as follows:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Allow authenticated users to read/write in their chat directories
    match /chats/{chatChannelId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.resource.size < 50 * 1024 * 1024; // 50MB max
    }
  }
}
```

## Debugging Media Upload Issues

### 1. Check Logs
When uploading media, check Android Studio Logcat for:
- ✅ `Upload path: chats/{chatChannelId}/...` - Upload started
- ✅ `File uploaded successfully. Getting download URL...` - Upload complete  
- ✅ `Download URL obtained: https://...` - Ready to send
- ✅ `{mediaType} message sent successfully` - Complete

### 2. Common Issues & Fixes

| Issue | Cause | Fix |
|-------|-------|-----|
| "Failed to upload media" | Firebase Storage permission denied | Check Security Rules above |
| "Failed to get download URL" | File uploaded but can't access | Verify bucket name in rules |
| No error but upload hangs | Network issue or large file | Check file size (<50MB) and connectivity |
| Messages not appearing in chat | Firestore write failed | Check chatChannelId exists |

### 3. Verify Connectivity
- Check Firebase Console > Storage to see if files are being stored
- Verify chatChannelId is being passed correctly
- Ensure user is authenticated (check Firebase Auth)

### 4. Testing Steps
1. **Test Image Upload:**
   - Tap image button
   - Select image from gallery
   - Watch Logcat for upload progress
   - Verify image appears in message

2. **Test Video Upload:**
   - Tap video button
   - Select video (keep under 50MB for testing)
   - Watch Logcat for upload progress
   - Verify video appears in message

3. **Manual Firebase Verification:**
   - Go to Firebase Console
   - Check Storage tab > chats folder
   - Should see dated files with content

## Next Steps

If uploads still fail after verifying logs:
1. Check Firebase Console for any error messages
2. Verify project ID in google-services.json
3. Check network connectivity on device
4. Test with smaller files first
5. Check device storage space (uploads to memory before sending)

