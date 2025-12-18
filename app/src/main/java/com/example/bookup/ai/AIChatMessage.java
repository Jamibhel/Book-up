package com.example.bookup.ai;

import com.google.firebase.Timestamp;

/**
 * Model class representing an AI Chat message
 * Stores both user and AI messages in the conversation
 */
public class AIChatMessage {
    public static final String ROLE_USER = "user";
    public static final String ROLE_AI = "ai";

    private String messageId;
    private String userId;
    private String subject;
    private String messageText;
    private String role; // "user" or "ai"
    private Timestamp timestamp;
    private long messageOrder; // For sorting messages
    private String displayName; // User's display name for reference
    private boolean isMarkdown; // Whether message contains markdown formatting

    // Empty constructor for Firebase
    public AIChatMessage() {}

    /**
     * Constructor for creating new chat messages
     */
    public AIChatMessage(String userId, String subject, String messageText, String role) {
        this.userId = userId;
        this.subject = subject;
        this.messageText = messageText;
        this.role = role;
        this.timestamp = new Timestamp(System.currentTimeMillis() / 1000, 0);
        this.messageOrder = System.currentTimeMillis();
        this.isMarkdown = role.equals(ROLE_AI); // AI messages use markdown
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public long getMessageOrder() {
        return messageOrder;
    }

    public void setMessageOrder(long messageOrder) {
        this.messageOrder = messageOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isMarkdown() {
        return isMarkdown;
    }

    public void setMarkdown(boolean markdown) {
        isMarkdown = markdown;
    }

    // Helper methods
    public boolean isFromUser() {
        return ROLE_USER.equals(role);
    }

    public boolean isFromAI() {
        return ROLE_AI.equals(role);
    }

    @Override
    public String toString() {
        return "AIChatMessage{" +
                "messageId='" + messageId + '\'' +
                ", userId='" + userId + '\'' +
                ", subject='" + subject + '\'' +
                ", role='" + role + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
