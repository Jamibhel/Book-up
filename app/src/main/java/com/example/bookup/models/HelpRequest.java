package com.example.bookup.models;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.Date;

public class HelpRequest implements Serializable {
    private String id; // Document ID from Firestore
    private String title;
    private String description;
    private String subject;
    private String requestedByUid;
    private String requestedByName;
    private String status; // e.g., "Open", "Assigned", "Resolved"
    @ServerTimestamp
    private Date timestamp;
    
    @Exclude
    private long timestampMillis; // Fallback timestamp in milliseconds

    public HelpRequest() {
        // Required for Firestore deserialization
    }

    public HelpRequest(String id, String title, String description, String subject, String requestedByUid, String requestedByName, String status, Date timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.requestedByUid = requestedByUid;
        this.requestedByName = requestedByName;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRequestedByUid() {
        return requestedByUid;
    }

    public void setRequestedByUid(String requestedByUid) {
        this.requestedByUid = requestedByUid;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimestamp() {
        // If timestamp is null, try to create one from timestampMillis fallback
        if (timestamp == null && timestampMillis > 0) {
            return new Date(timestampMillis);
        }
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        // Also update timestampMillis fallback
        if (timestamp != null) {
            this.timestampMillis = timestamp.getTime();
        }
    }
    
    public long getTimestampMillis() {
        return timestampMillis;
    }
    
    public void setTimestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;
        // Don't override timestamp - let Firestore handle it
    }
}
