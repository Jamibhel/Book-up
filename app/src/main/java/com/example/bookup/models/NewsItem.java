package com.example.bookup.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewsItem implements Serializable {
    private String id;
    private String title;
    private String headline; 
    private String content;
    private String description; 
    private String source; 
    private String imageUrl;
    private String authorId;
    private String authorName;
    private String authorRole;
    private boolean priority;
    private long likesCount;
    private List<String> likedBy = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();
    
    @ServerTimestamp
    private Date timestamp;

    public NewsItem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }

    public boolean isPriority() { return priority; }
    public void setPriority(boolean priority) { this.priority = priority; }

    public long getLikesCount() { return likesCount; }
    public void setLikesCount(long likesCount) { this.likesCount = likesCount; }

    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isLikedByUser(String userId) {
        return likedBy != null && likedBy.contains(userId);
    }
}
