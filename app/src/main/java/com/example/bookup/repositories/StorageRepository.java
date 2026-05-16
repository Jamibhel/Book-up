package com.example.bookup.repositories;

import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Locale;

/**
 * Repository for managing media uploads and downloads in Firebase Storage.
 * Handles images, videos, audio files, and documents for chat messages.
 * Provides progress tracking, error handling, and retry logic.
 */
public class StorageRepository {

    private static final String TAG = "StorageRepository";
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB max

    // Storage bucket paths
    private static final String PATH_CHAT_IMAGES = "chat_media/images/";
    private static final String PATH_CHAT_VIDEOS = "chat_media/videos/";
    private static final String PATH_CHAT_AUDIO = "chat_media/audio/";
    private static final String PATH_CHAT_DOCUMENTS = "chat_media/documents/";
    private static final String PATH_TEMP_AUDIO = "temp/audio_recordings/";
    private static final String PATH_USER_MATERIALS = "user_uploads/materials/";

    // Callbacks
    public interface OnUploadProgressListener {
        void onProgress(long uploadedBytes, long totalBytes);
    }

    public interface OnUploadCompleteListener {
        void onSuccess(String downloadUrl);
        void onError(Exception exception);
    }

    public interface OnDownloadProgressListener {
        void onProgress(long downloadedBytes, long totalBytes);
    }

    public interface OnDownloadCompleteListener {
        void onSuccess(byte[] data);
        void onError(Exception exception);
    }

    public interface OnDeleteCompleteListener {
        void onSuccess();
        void onError(Exception exception);
    }

    // ==================== IMAGE UPLOAD ====================

    /**
     * Upload image file to Firebase Storage.
     * Stores at: chat_media/images/{conversationId}/{messageId}.jpg
     */
    public static void uploadImage(File imageFile, String conversationId, String messageId,
                                   OnUploadProgressListener onProgress, OnUploadCompleteListener onComplete) {
        if (!validateFile(imageFile)) {
            if (onComplete != null) {
                onComplete.onError(new Exception("Invalid image file"));
            }
            return;
        }

        String fileName = messageId + ".jpg";
        String path = PATH_CHAT_IMAGES + conversationId + "/" + fileName;
        StorageReference ref = storage.getReference(path);

        uploadFile(ref, imageFile, "image/jpeg", onProgress, onComplete);
    }

    // ==================== VIDEO UPLOAD ====================

    /**
     * Upload video file to Firebase Storage.
     * Stores at: chat_media/videos/{conversationId}/{messageId}.mp4
     */
    public static void uploadVideo(File videoFile, String conversationId, String messageId,
                                   OnUploadProgressListener onProgress, OnUploadCompleteListener onComplete) {
        if (!validateFile(videoFile)) {
            if (onComplete != null) {
                onComplete.onError(new Exception("Invalid video file"));
            }
            return;
        }

        String fileName = messageId + ".mp4";
        String path = PATH_CHAT_VIDEOS + conversationId + "/" + fileName;
        StorageReference ref = storage.getReference(path);

        uploadFile(ref, videoFile, "video/mp4", onProgress, onComplete);
    }

    // ==================== AUDIO UPLOAD ====================

    /**
     * Upload audio file to Firebase Storage.
     * Stores at: chat_media/audio/{conversationId}/{messageId}.m4a
     */
    public static void uploadAudio(File audioFile, String conversationId, String messageId,
                                   OnUploadProgressListener onProgress, OnUploadCompleteListener onComplete) {
        if (!validateFile(audioFile)) {
            if (onComplete != null) {
                onComplete.onError(new Exception("Invalid audio file"));
            }
            return;
        }

        String fileName = messageId + ".m4a";
        String path = PATH_CHAT_AUDIO + conversationId + "/" + fileName;
        StorageReference ref = storage.getReference(path);

        uploadFile(ref, audioFile, "audio/mp4", onProgress, onComplete);
    }

    // ==================== DOCUMENT UPLOAD ====================

    /**
     * Upload document file to Firebase Storage.
     * Supports PDF, DOC, DOCX, XLS, XLSX, etc.
     * Stores at: chat_media/documents/{conversationId}/{messageId}.{extension}
     */
    public static void uploadDocument(File documentFile, String conversationId, String messageId,
                                      OnUploadProgressListener onProgress, OnUploadCompleteListener onComplete) {
        if (!validateFile(documentFile)) {
            if (onComplete != null) {
                onComplete.onError(new Exception("Invalid document file"));
            }
            return;
        }

        String extension = getFileExtension(documentFile);
        String fileName = messageId + "." + extension;
        String path = PATH_CHAT_DOCUMENTS + conversationId + "/" + fileName;
        StorageReference ref = storage.getReference(path);

        String mimeType = getMimeType(extension);
        uploadFile(ref, documentFile, mimeType, onProgress, onComplete);
    }

    // ==================== MATERIALS UPLOAD ====================

    /**
     * Upload material file (PDF, image, video) to user's materials folder.
     * Stores at: user_uploads/materials/{userId}/{timestamp}.{extension}
     * Used by the "Upload Materials" feature.
     */
    public static void uploadMaterial(File materialFile, String userId,
                                      OnUploadProgressListener onProgress, OnUploadCompleteListener onComplete) {
        if (!validateFile(materialFile)) {
            if (onComplete != null) {
                onComplete.onError(new Exception("Invalid material file"));
            }
            return;
        }

        long timestamp = System.currentTimeMillis();
        String extension = getFileExtension(materialFile);
        String fileName = timestamp + "." + extension;
        String path = PATH_USER_MATERIALS + userId + "/" + fileName;
        StorageReference ref = storage.getReference(path);

        String mimeType = getMimeType(extension);
        uploadFile(ref, materialFile, mimeType, onProgress, onComplete);
    }

    // ==================== GENERIC UPLOAD ====================

    /**
     * Generic file upload with progress tracking and error handling.
     */
    private static void uploadFile(StorageReference ref, File file, String mimeType,
                                   OnUploadProgressListener onProgress, OnUploadCompleteListener onComplete) {
        Log.d(TAG, "Starting upload for: " + file.getName() + " (" + formatFileSize(file.length()) + ")");
        Log.d(TAG, "Upload destination: " + ref.getPath());
        Log.d(TAG, "MIME type: " + mimeType);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(mimeType)
                .setCustomMetadata("uploadTime", String.valueOf(System.currentTimeMillis()))
                .build();

        android.net.Uri fileUri = android.net.Uri.fromFile(file);
        Log.d(TAG, "File URI: " + fileUri);

        UploadTask uploadTask = ref.putFile(fileUri, metadata);

        // Track progress
        uploadTask.addOnProgressListener(taskSnapshot -> {
            long uploadedBytes = taskSnapshot.getBytesTransferred();
            long totalBytes = taskSnapshot.getTotalByteCount();

            if (totalBytes > 0) {
                int percentage = (int) ((uploadedBytes * 100) / totalBytes);
                Log.d(TAG, String.format("Upload progress: %d%% (%s / %s)", 
                    percentage, formatFileSize(uploadedBytes), formatFileSize(totalBytes)));
            }

            if (onProgress != null) {
                onProgress.onProgress(uploadedBytes, totalBytes);
            }
        });

        // Handle success
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "✅ Upload successful: " + ref.getPath());

            // Get download URL
            ref.getDownloadUrl()
                    .addOnSuccessListener(downloadUri -> {
                        Log.d(TAG, "✅ Download URL obtained: " + downloadUri.toString());
                        if (onComplete != null) {
                            onComplete.onSuccess(downloadUri.toString());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to get download URL after successful upload", e);
                        if (onComplete != null) {
                            onComplete.onError(e);
                        }
                    });
        });

        // Handle failure
        uploadTask.addOnFailureListener(e -> {
            Log.e(TAG, "❌ Upload failed: " + ref.getPath(), e);
            Log.e(TAG, "Exception type: " + e.getClass().getSimpleName());
            Log.e(TAG, "Exception message: " + e.getMessage());
            
            if (onComplete != null) {
                onComplete.onError(e);
            }
        });
    }

    // ==================== DOWNLOAD ====================

    /**
     * Download file from Firebase Storage.
     * Useful for caching media or bulk downloads.
     */
    public static void downloadFile(String downloadUrl, OnDownloadProgressListener onProgress,
                                    OnDownloadCompleteListener onComplete) {
        StorageReference ref = storage.getReferenceFromUrl(downloadUrl);

        ref.getBytes(MAX_FILE_SIZE)
                .addOnSuccessListener(bytes -> {
                    Log.d(TAG, "✅ Download successful: " + bytes.length + " bytes");
                    if (onComplete != null) {
                        onComplete.onSuccess(bytes);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Download failed", e);
                    if (onComplete != null) {
                        onComplete.onError(e);
                    }
                });
    }

    // ==================== DELETE ====================

    /**
     * Delete file from Firebase Storage by path.
     */
    public static void deleteFile(String path, OnDeleteCompleteListener onComplete) {
        StorageReference ref = storage.getReference(path);
        ref.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ File deleted: " + path);
                    if (onComplete != null) {
                        onComplete.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to delete file: " + path, e);
                    if (onComplete != null) {
                        onComplete.onError(e);
                    }
                });
    }

    /**
     * Delete file from Firebase Storage by download URL.
     */
    public static void deleteFileByUrl(String downloadUrl, OnDeleteCompleteListener onComplete) {
        try {
            StorageReference ref = storage.getReferenceFromUrl(downloadUrl);
            ref.delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ File deleted from URL");
                        if (onComplete != null) {
                            onComplete.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to delete file from URL", e);
                        if (onComplete != null) {
                            onComplete.onError(e);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Invalid download URL", e);
            if (onComplete != null) {
                onComplete.onError(e);
            }
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Validate file before upload.
     */
    private static boolean validateFile(File file) {
        if (file == null) {
            Log.w(TAG, "⚠️ File validation failed: File is null");
            return false;
        }

        if (!file.exists()) {
            Log.w(TAG, "⚠️ File validation failed: File does not exist at " + file.getAbsolutePath());
            return false;
        }

        if (file.length() > MAX_FILE_SIZE) {
            Log.w(TAG, "⚠️ File validation failed: File exceeds max size. Size: " + formatFileSize(file.length()) + " (max: " + formatFileSize(MAX_FILE_SIZE) + ")");
            return false;
        }

        if (file.length() == 0) {
            Log.w(TAG, "⚠️ File validation failed: File is empty (0 bytes)");
            return false;
        }

        Log.d(TAG, "✅ File validation passed: " + file.getName() + " (" + formatFileSize(file.length()) + ")");
        return true;
    }

    /**
     * Get file extension from file.
     */
    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0 && lastDot < name.length() - 1) {
            return name.substring(lastDot + 1).toLowerCase();
        }
        return "unknown";
    }

    /**
     * Get MIME type based on file extension.
     */
    private static String getMimeType(String extension) {
        switch (extension.toLowerCase()) {
            // Images
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";

            // Videos
            case "mp4":
                return "video/mp4";
            case "mkv":
                return "video/x-matroska";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";

            // Audio
            case "m4a":
                return "audio/mp4";
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "flac":
                return "audio/flac";
            case "aac":
                return "audio/aac";

            // Documents
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            case "csv":
                return "text/csv";
            case "zip":
                return "application/zip";

            default:
                return "application/octet-stream";
        }
    }

    /**
     * Format file size for display (bytes to KB, MB, GB).
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Get file size from download URL metadata.
     * (Requires HEAD request - not directly available from URL)
     */
    public static void getFileSizeFromUrl(String downloadUrl, com.google.android.gms.tasks.OnSuccessListener<Long> onSuccess,
                                         com.google.android.gms.tasks.OnFailureListener onFailure) {
        try {
            StorageReference ref = storage.getReferenceFromUrl(downloadUrl);
            ref.getMetadata()
                    .addOnSuccessListener(metadata -> {
                        long size = metadata.getSizeBytes();
                        Log.d(TAG, "File size: " + formatFileSize(size));
                        onSuccess.onSuccess(size);
                    })
                    .addOnFailureListener(onFailure);
        } catch (Exception e) {
            Log.e(TAG, "Invalid download URL", e);
            onFailure.onFailure(e);
        }
    }
}
