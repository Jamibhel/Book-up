package com.example.bookup.models;

import java.util.Date;

public class Request {
    private String id;
    private String userId;
    private String userDisplayName;
    private String subject;
    private String description;
    private String status; // "pending", "accepted", "rejected", "completed"
    private Date timestamp;
    private String tutorId; // ID of assigned tutor if any
    private String tutorDisplayName; // Name of assigned tutor if any
    private String rejectionReason; // If request is rejected
    private String contactEmail;
    private String title;

    // Required empty constructor for Firestore
    public Request() {}

    public Request(String userId, String userDisplayName, String subject, String description, String contactEmail) {
        this.userId = userId;
        this.userDisplayName = userDisplayName;
        this.subject = subject;
        this.description = description;
        this.contactEmail = contactEmail;
        this.status = "pending";
        this.timestamp = new Date();
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserDisplayName() { return userDisplayName; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public Date getTimestamp() { return timestamp; }
    public String getTutorId() { return tutorId; }
    public String getTutorDisplayName() { return tutorDisplayName; }
    public String getRejectionReason() { return rejectionReason; }
    public String getContactEmail() { return contactEmail; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }
    public void setTutorDisplayName(String tutorDisplayName) { this.tutorDisplayName = tutorDisplayName; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public void setTitle(String title) {
        this.title = title;
    }
}