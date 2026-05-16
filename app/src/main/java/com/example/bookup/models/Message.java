package com.example.bookup.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.ArrayList;
import java.util.List;

public class Message {
    public enum Type {
        TEXT, IMAGE, AUDIO, VIDEO, FILE, CALL
    }

    private String id;
    private String senderId;
    private String senderName;
    private String text;
    private String mediaUrl;
    private String mediaFileName;
    private Type type;
    private String messageType; // Web compatibility fallback
    private String content; // Web compatibility fallback
    private Timestamp timestamp;
    
    private String replyToId;
    private String replyToText;
    private String replyToName;
    private boolean isDeletedForEveryone;
    private List<String> deletedForUsers;

    public Message() {
        this.deletedForUsers = new ArrayList<>();
    }

    public Message(String senderId, String senderName, String text, Type type) {
        this();
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.type = type;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getText() { 
        return (text != null && !text.isEmpty()) ? text : content; 
    }
    public void setText(String text) { this.text = text; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getMediaFileName() { return mediaFileName; }
    public void setMediaFileName(String mediaFileName) { this.mediaFileName = mediaFileName; }

    public Type getType() { 
        if (type != null) return type;
        if (messageType != null) {
            try {
                return Type.valueOf(messageType.toUpperCase());
            } catch (Exception e) {
                return Type.TEXT;
            }
        }
        return Type.TEXT;
    }
    public void setType(Type type) { this.type = type; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    @ServerTimestamp
    @PropertyName("timestamp")
    public Timestamp getTimestamp() { return timestamp; }
    
    @PropertyName("timestamp")
    public void setTimestamp(Object timestamp) {
        if (timestamp instanceof Timestamp) {
            this.timestamp = (Timestamp) timestamp;
        } else if (timestamp instanceof Long) {
            this.timestamp = new Timestamp(new java.util.Date((Long) timestamp));
        }
    }

    public String getReplyToId() { return replyToId; }
    public void setReplyToId(String replyToId) { this.replyToId = replyToId; }

    public String getReplyToText() { return replyToText; }
    public void setReplyToText(String replyToText) { this.replyToText = replyToText; }

    public String getReplyToName() { return replyToName; }
    public void setReplyToName(String replyToName) { this.replyToName = replyToName; }

    public boolean isDeletedForEveryone() { return isDeletedForEveryone; }
    public void setDeletedForEveryone(boolean deletedForEveryone) { isDeletedForEveryone = deletedForEveryone; }

    public List<String> getDeletedForUsers() { return deletedForUsers; }
    public void setDeletedForUsers(List<String> deletedForUsers) { this.deletedForUsers = deletedForUsers; }
}
