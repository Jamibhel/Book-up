package com.example.bookup.base;

import androidx.annotation.NonNull;

/**
 * Configuration class for security settings across the application
 */
public class SecurityConfig {
    // Token refresh settings
    public static final long TOKEN_REFRESH_INTERVAL = 45 * 60 * 1000; // 45 minutes
    public static final long TOKEN_EXPIRY_THRESHOLD = 50 * 60 * 1000; // 50 minutes

    // Rate limiting settings
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOGIN_COOLDOWN_PERIOD = 15 * 60 * 1000; // 15 minutes
    public static final int MAX_FILE_UPLOAD_SIZE = 50 * 1024 * 1024; // 50MB

    // Session settings
    public static final long SESSION_TIMEOUT = 2 * 60 * 60 * 1000; // 2 hours
    public static final boolean REQUIRE_STRONG_AUTH = true;

    // Security headers
    @NonNull
    public static String[] getSecurityHeaders() {
        return new String[] {
            "X-Content-Type-Options: nosniff",
            "X-Frame-Options: DENY",
            "X-XSS-Protection: 1; mode=block",
            "Strict-Transport-Security: max-age=31536000; includeSubDomains"
        };
    }
}