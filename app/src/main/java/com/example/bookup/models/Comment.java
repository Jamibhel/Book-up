package com.example.bookup.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Comment model for news articles and community posts.
 */
public class Comment implements Serializable {
    private String id;
    private String userId;
    private String userName;
    private String userImageUrl;
    private String text;
    @ServerTimestamp
    private Date timestamp; // Changed from Timestamp to Date for Serializable
    
    private int likeCount;
    private List<String> likedBy = new ArrayList<>();
    
    // Reply functionality (Tag-like format)
    private String replyToId;
    private String replyToText;
    private String replyToName;

    public Comment() {
        this.id = UUID.randomUUID().toString();
        this.likedBy = new ArrayList<>();
    }

    public Comment(String userId, String userName, String text) {
        this();
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.likeCount = 0;
    }

    // ===== GETTERS & SETTERS =====
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserImageUrl() { return userImageUrl; }
    public void setUserImageUrl(String userImageUrl) { this.userImageUrl = userImageUrl; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }

    public String getReplyToId() { return replyToId; }
    public void setReplyToId(String replyToId) { this.replyToId = replyToId; }

    public String getReplyToText() { return replyToText; }
    public void setReplyToText(String replyToText) { this.replyToText = replyToText; }

    public String getReplyToName() { return replyToName; }
    public void setReplyToName(String replyToName) { this.replyToName = replyToName; }
}
