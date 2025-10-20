package com.example.bookup.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class NewsItem implements Serializable {
    private String id; // NEW: Document ID for editing/deleting
    private String title;
    private String description;
    private String imageUrl; // Optional, for visual appeal
    private String source; // e.g., "University Admin", "Student Union"
    @ServerTimestamp
    private Date timestamp; // When it was uploaded

    public NewsItem() {
        // Default constructor required for Firebase deserialization
    }

    // Updated constructor to include ID and timestamp for a complete model
    public NewsItem(String id, String title, String description, String imageUrl, String source, Date timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.source = source;
        this.timestamp = timestamp;
    }

    // Previous constructor (without ID/timestamp) can be kept or removed if not needed
    public NewsItem(String title, String description, String imageUrl, String source) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.source = source;
        // Timestamp will be set by Firestore @ServerTimestamp on creation
    }


    // Getters
    public String getId() { return id; } // NEW Getter
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getSource() { return source; }
    public Date getTimestamp() { return timestamp; } // Getter for timestamp

    // Setters
    public void setId(String id) { this.id = id; } // NEW Setter
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSource(String source) { this.source = source; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; } // Setter for timestamp
}
