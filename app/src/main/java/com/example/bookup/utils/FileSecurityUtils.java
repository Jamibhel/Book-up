package com.example.bookup.utils;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Locale;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for secure file handling
 */
public class FileSecurityUtils {
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>();
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>();
    
    static {
        // Document formats
        ALLOWED_EXTENSIONS.add("pdf");
        ALLOWED_EXTENSIONS.add("doc");
        ALLOWED_EXTENSIONS.add("docx");
        
        // Image formats
        ALLOWED_EXTENSIONS.add("jpg");
        ALLOWED_EXTENSIONS.add("jpeg");
        ALLOWED_EXTENSIONS.add("png");
        
        // MIME types
        ALLOWED_MIME_TYPES.add("application/pdf");
        ALLOWED_MIME_TYPES.add("application/msword");
        ALLOWED_MIME_TYPES.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        ALLOWED_MIME_TYPES.add("image/jpeg");
        ALLOWED_MIME_TYPES.add("image/png");
    }

    /**
     * Validates a file based on size, type and extension
     */
    public static boolean isFileValid(@NonNull Context context, @NonNull Uri fileUri) throws IOException {
        String mimeType = context.getContentResolver().getType(fileUri);
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
        
        if (mimeType == null || extension == null) {
            return false;
        }
        
        // Check file size
        File file = new File(fileUri.getPath());
        if (file.length() > MAX_FILE_SIZE) {
            return false;
        }
        
        // Check extension and MIME type
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT)) &&
               ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase(Locale.ROOT));
    }

    /**
     * Calculates MD5 hash of file for integrity checking
     */
    @Nullable
    public static String calculateFileHash(@NonNull File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            
            while ((read = fis.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            
            byte[] md5sum = digest.digest();
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : md5sum) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Sanitizes a filename to prevent path traversal attacks
     */
    @NonNull
    public static String sanitizeFilename(@Nullable String filename) {
        if (filename == null) return "";
        
        // Remove any path components
        filename = new File(filename).getName();
        
        // Remove any potentially dangerous characters
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    /**
     * Validates a file's magic numbers to ensure it matches its extension
     */
    public static boolean validateFileType(@NonNull File file, @NonNull String expectedMimeType) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] magic = new byte[4];
            if (fis.read(magic) != 4) {
                return false;
            }
            
            // Check magic numbers for common file types
            switch (expectedMimeType) {
                case "application/pdf":
                    return magic[0] == 0x25 && magic[1] == 0x50 && 
                           magic[2] == 0x44 && magic[3] == 0x46;
                case "image/jpeg":
                    return magic[0] == (byte)0xFF && magic[1] == (byte)0xD8 && 
                           magic[2] == (byte)0xFF;
                case "image/png":
                    return magic[0] == (byte)0x89 && magic[1] == 0x50 && 
                           magic[2] == 0x4E && magic[3] == 0x47;
                default:
                    return false;
            }
        } catch (IOException e) {
            return false;
        }
    }
}