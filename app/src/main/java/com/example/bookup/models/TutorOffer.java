package com.example.bookup.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

/**
 * Model class representing a tutor's offer to help with a specific help request
 */
public class TutorOffer implements Serializable {
    private String id; // Document ID from Firestore
    private String requestId; // ID of the help request this offer is for
    private String tutorUid; // UID of the tutor offering help
    private String tutorName; // Name of the tutor
    private String tutorPhotoUrl; // Profile photo URL of the tutor
    private Double bidAmount; // Optional bid amount (null if not specified)
    private String message; // Tutor's offer message
    private String status; // "pending", "accepted", "rejected"
    @ServerTimestamp
    private Date timestamp; // When the offer was created
    private String acceptedByStudent; // UID of student if they accepted (null otherwise)
    private Date acceptedAt; // Timestamp when student accepted the offer

    public TutorOffer() {
        // Required for Firestore deserialization
    }

    public TutorOffer(String requestId, String tutorUid, String tutorName, String tutorPhotoUrl,
                      String message, Double bidAmount) {
        this.requestId = requestId;
        this.tutorUid = tutorUid;
        this.tutorName = tutorName;
        this.tutorPhotoUrl = tutorPhotoUrl;
        this.message = message;
        this.bidAmount = bidAmount;
        this.status = "pending";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTutorUid() {
        return tutorUid;
    }

    public void setTutorUid(String tutorUid) {
        this.tutorUid = tutorUid;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }

    public String getTutorPhotoUrl() {
        return tutorPhotoUrl;
    }

    public void setTutorPhotoUrl(String tutorPhotoUrl) {
        this.tutorPhotoUrl = tutorPhotoUrl;
    }

    public Double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(Double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAcceptedByStudent() {
        return acceptedByStudent;
    }

    public void setAcceptedByStudent(String acceptedByStudent) {
        this.acceptedByStudent = acceptedByStudent;
    }

    public Date getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Date acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
}
