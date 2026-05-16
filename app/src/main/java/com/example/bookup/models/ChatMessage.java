package com.example.bookup.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

/**
 * Model class for chat messages, supporting the new chat system.
 */
@IgnoreExtraProperties
public class ChatMessage {
    private String messageId;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String senderProfileImage;
    private String content;
    private String messageType;
    private String mediaUrl;
    private Timestamp timestamp;
    private String status;

    public ChatMessage() {
        // Required for Firestore
    }

    public ChatMessage(String senderId, String senderName, String content, String messageType) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = Timestamp.now();
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderProfileImage() { return senderProfileImage; }
    public void setSenderProfileImage(String senderProfileImage) { this.senderProfileImage = senderProfileImage; }

    @PropertyName("content")
    public String getContent() { return content; }
    @PropertyName("content")
    public void setContent(String content) { this.content = content; }

    // Compatibility with 'text' field in older messages
    @PropertyName("text")
    public String getText() { return content; }
    @PropertyName("text")
    public void setText(String text) { 
        if (this.content == null) this.content = text; 
    }

    // SearchService expects getMessageText()
    public String getMessageText() {
        return content;
    }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
