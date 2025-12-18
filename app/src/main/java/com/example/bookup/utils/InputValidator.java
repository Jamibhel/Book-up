package com.example.bookup.utils;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Patterns;

import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for input validation and sanitization
 */
public class InputValidator {
    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{3,30}$");
    
    private static Map<String, Pattern> SUBJECT_PATTERNS = new HashMap<>();
    static {
        // Add patterns for known academic subjects
        SUBJECT_PATTERNS.put("Mathematics", Pattern.compile("^[A-Za-z0-9\\s\\-()]+$"));
        SUBJECT_PATTERNS.put("Science", Pattern.compile("^[A-Za-z0-9\\s\\-()]+$"));
        SUBJECT_PATTERNS.put("Literature", Pattern.compile("^[A-Za-z0-9\\s\\-()]+$"));
        // Add more subjects as needed
    }

    public static boolean isValidEmail(@Nullable String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(@Nullable String password) {
        if (TextUtils.isEmpty(password) || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasNumber = true;
            else hasSpecial = true;
        }
        
        return hasUpper && hasLower && hasNumber && hasSpecial;
    }

    public static boolean isValidUsername(@Nullable String username) {
        return !TextUtils.isEmpty(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    @NonNull
    public static String sanitizeInput(@Nullable String input) {
        if (input == null) return "";
        
        // Remove any potentially harmful characters
        return input.replaceAll("[<>\"'&]", "")
                   .replaceAll("(?i)javascript:", "")
                   .replaceAll("\\\\", "")
                   .trim();
    }

    @NonNull
    public static String sanitizeSubjectInput(@Nullable String subject, @NonNull String subjectType) {
        String sanitized = sanitizeInput(subject);
        Pattern pattern = SUBJECT_PATTERNS.get(subjectType);
        
        if (pattern != null && !pattern.matcher(sanitized).matches()) {
            throw new IllegalArgumentException("Invalid input for subject type: " + subjectType);
        }
        
        return sanitized;
    }

    @NonNull
    public static String hashSensitiveData(@NonNull String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash sensitive data", e);
        }
    }
}