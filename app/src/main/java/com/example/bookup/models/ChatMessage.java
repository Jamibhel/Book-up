package com.example.bookup.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
    private String id; // Document ID from Firestore (optional, messages can be subcollection documents)
    private String senderId; // UID of the user who sent the message
    private String senderName; // Display name of the sender
    private String messageText; // The actual content of the message
    @ServerTimestamp
    private Date timestamp; // When the message was sent
    private boolean read; // True if the message has been read by recipients (optional)
    // Add other fields as needed, e.g., imageUrl for image messages, messageType etc.

    public ChatMessage() {
        // Required for Firestore deserialization
    }

    public ChatMessage(String senderId, String senderName, String messageText, Date timestamp, boolean read) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.read = read;
    }

    // Getters
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getMessageText() { return messageText; }
    public Date getTimestamp() { return timestamp; }
    public boolean isRead() { return read; } // Note: isRead for boolean getters


    // Setters
    public void setId(String id) { this.id = id; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setRead(boolean read) { this.read = read; }
}
