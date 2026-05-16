package com.example.bookup.services;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Service for uploading and downloading files to/from Firebase Cloud Storage.
 * Handles images, audio, video, and documents with progress tracking.
 */
public class FirebaseStorageService {

    private static final String TAG = "FirebaseStorageService";
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    // Storage paths
    private static final String PATH_CHAT_MEDIA = "chat_media/";
    private static final String PATH_CHAT_AUDIO = PATH_CHAT_MEDIA + "audio/";
    private static final String PATH_CHAT_IMAGES = PATH_CHAT_MEDIA + "images/";
    private static final String PATH_CHAT_VIDEOS = PATH_CHAT_MEDIA + "videos/";
    private static final String PATH_CHAT_DOCUMENTS = PATH_CHAT_MEDIA + "documents/";

    // Callbacks
    public interface OnUploadProgressListener {
        void onProgress(long bytesTransferred, long totalBytes);
        void onSuccess(String downloadUrl);
        void onFailure(Exception exception);
    }

    public interface OnDownloadProgressListener {
        void onProgress(long bytesTransferred, long totalBytes);
        void onSuccess(File downloadedFile);
        void onFailure(Exception exception);
    }

    /**
     * Upload image to Firebase Storage.
     *
     * @param imageUri URI of the image file
     * @param conversationId ID of the conversation
     * @param userId ID of the user uploading
     * @param listener Progress listener
     */
    public static void uploadImage(Uri imageUri, String conversationId, String userId,
                                   OnUploadProgressListener listener) {
        String fileName = "img_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storage.getReference()
                .child(PATH_CHAT_IMAGES)
                .child(conversationId)
                .child(userId)
                .child(fileName);

        uploadFile(imageRef, imageUri, listener);
    }

    /**
     * Upload audio recording to Firebase Storage.
     *
     * @param audioUri URI of the audio file
     * @param conversationId ID of the conversation
     * @param userId ID of the user uploading
     * @param listener Progress listener
     */
    public static void uploadAudio(Uri audioUri, String conversationId, String userId,
                                   OnUploadProgressListener listener) {
        String fileName = "audio_" + System.currentTimeMillis() + ".m4a";
        StorageReference audioRef = storage.getReference()
                .child(PATH_CHAT_AUDIO)
                .child(conversationId)
                .child(userId)
                .child(fileName);

        uploadFile(audioRef, audioUri, listener);
    }

    /**
     * Upload video to Firebase Storage.
     *
     * @param videoUri URI of the video file
     * @param conversationId ID of the conversation
     * @param userId ID of the user uploading
     * @param listener Progress listener
     */
    public static void uploadVideo(Uri videoUri, String conversationId, String userId,
                                   OnUploadProgressListener listener) {
        String fileName = "video_" + System.currentTimeMillis() + ".mp4";
        StorageReference videoRef = storage.getReference()
                .child(PATH_CHAT_VIDEOS)
                .child(conversationId)
                .child(userId)
                .child(fileName);

        uploadFile(videoRef, videoUri, listener);
    }

    /**
     * Upload document to Firebase Storage.
     *
     * @param documentUri URI of the document file
     * @param conversationId ID of the conversation
     * @param userId ID of the user uploading
     * @param listener Progress listener
     */
    public static void uploadDocument(Uri documentUri, String conversationId, String userId,
                                      OnUploadProgressListener listener) {
        String fileName = "doc_" + System.currentTimeMillis() + ".pdf";
        StorageReference docRef = storage.getReference()
                .child(PATH_CHAT_DOCUMENTS)
                .child(conversationId)
                .child(userId)
                .child(fileName);

        uploadFile(docRef, documentUri, listener);
    }

    /**
     * Generic file upload method.
     */
    private static void uploadFile(StorageReference fileRef, Uri fileUri,
                                   OnUploadProgressListener listener) {
        fileRef.putFile(fileUri)
                .addOnProgressListener(task -> {
                    if (listener != null) {
                        long bytesTransferred = task.getBytesTransferred();
                        long totalBytes = task.getTotalByteCount();
                        listener.onProgress(bytesTransferred, totalBytes);
                    }
                })
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        if (listener != null) {
                            listener.onSuccess(uri.toString());
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get download URL", e);
                        if (listener != null) {
                            listener.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Upload failed", e);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Download file from Firebase Storage.
     */
    public static void downloadFile(String fileUrl, String fileName, Context context,
                                    OnDownloadProgressListener listener) {
        StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
        File downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        if (downloadsDir == null) {
            if (listener != null) {
                listener.onFailure(new Exception("Downloads directory not available"));
            }
            return;
        }

        File localFile = new File(downloadsDir, fileName);

        fileRef.getFile(localFile)
                .addOnProgressListener(task -> {
                    if (listener != null) {
                        long bytesTransferred = task.getBytesTransferred();
                        long totalBytes = task.getTotalByteCount();
                        listener.onProgress(bytesTransferred, totalBytes);
                    }
                })
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Download successful: " + localFile.getPath());
                    if (listener != null) {
                        listener.onSuccess(localFile);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Download failed", e);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Delete file from Firebase Storage.
     */
    public static void deleteFile(String downloadUrl) {
        try {
            StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl);
            fileRef.delete().addOnFailureListener(e ->
                    Log.e(TAG, "Failed to delete file", e));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid download URL", e);
        }
    }

    /**
     * Get metadata about a file in Storage.
     */
    public interface MetadataListener {
        void onMetadata(StorageMetadata metadata);
    }

    public static void getFileMetadata(String downloadUrl, MetadataListener listener) {
        try {
            StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl);
            fileRef.getMetadata().addOnSuccessListener(listener::onMetadata)
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to get metadata", e));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid download URL", e);
        }
    }
}
