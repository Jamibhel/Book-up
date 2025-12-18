package com.example.bookup.models;

public final class MessageStatus {
    public static final int STATUS_SENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_DELIVERED = 2;
    public static final int STATUS_ERROR = 3;

    private MessageStatus() {
        // Prevent instantiation
    }
}