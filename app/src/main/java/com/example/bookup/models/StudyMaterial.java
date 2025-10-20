package com.example.bookup.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class StudyMaterial implements Serializable {
    private String id; // Document ID
    private String title;
    private String description;
    private String subject; // e.g., "Math", "Chemistry"
    private String materialType; // e.g., "Notes", "Past Paper", "Video Link"
    private String fileUrl; // URL to PDF, image, or video
    private String thumbnailUrl; // For preview images
    private String uploaderUid; // UID of the user who uploaded it
    private String uploaderName; // Name of the user who uploaded it
    private double averageRating; // Rating given by users
    private int downloadCount;
    @ServerTimestamp
    private Date timestamp; // When it was uploaded

    public StudyMaterial() {
        // Required empty public constructor for Firebase
    }

    public StudyMaterial(String id, String title, String description, String subject, String materialType, String fileUrl, String thumbnailUrl, String uploaderUid, String uploaderName, double averageRating, int downloadCount, Date timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.materialType = materialType;
        this.fileUrl = fileUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.uploaderUid = uploaderUid;
        this.uploaderName = uploaderName;
        this.averageRating = averageRating;
        this.downloadCount = downloadCount;
        this.timestamp = timestamp;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSubject() { return subject; }
    public String getMaterialType() { return materialType; }
    public String getFileUrl() { return fileUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getUploaderUid() { return uploaderUid; }
    public String getUploaderName() { return uploaderName; }
    public double getAverageRating() { return averageRating; }
    public int getDownloadCount() { return downloadCount; }
    public Date getTimestamp() { return timestamp; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setUploaderUid(String uploaderUid) { this.uploaderUid = uploaderUid; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
