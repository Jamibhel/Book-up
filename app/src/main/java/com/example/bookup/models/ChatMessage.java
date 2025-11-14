package com.example.bookup.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
    private String id;
    private String senderId;
    private String senderName;
    private String messageText;
    private String messageType; // "text", "image", "audio"
    private String mediaUrl;    // URL for image or audio file
    private String thumbnailUrl; // Thumbnail URL for images
    private long audioDuration; // Duration in milliseconds for audio messages
    @ServerTimestamp
    private Date timestamp;
    private boolean read;

    // No-argument constructor required for Firestore deserialization
    public ChatMessage() {
        // Required empty constructor for Firestore
    }

    public ChatMessage(String uid, String currentUserName, String messageText, Date date, boolean b) {
        this.senderId = uid;
        this.senderName = currentUserName;
        this.messageText = messageText;
        this.timestamp = date;
        this.read = b;
        this.messageType = "text";
    }

    // Constructor for text messages
    public ChatMessage(String senderId, String senderName, String messageText) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.messageType = "text";
        this.read = false;
    }

    // Constructor for media messages
    public ChatMessage(String senderId, String senderName, String mediaUrl, String messageType) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.mediaUrl = mediaUrl;
        this.messageType = messageType;
        this.read = false;
    }

    // Getters
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getMessageText() { return messageText; }
    public String getMessageType() { return messageType; }
    public String getMediaUrl() { return mediaUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public long getAudioDuration() { return audioDuration; }
    public Date getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setAudioDuration(long audioDuration) { this.audioDuration = audioDuration; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setRead(boolean read) { this.read = read; }
}
