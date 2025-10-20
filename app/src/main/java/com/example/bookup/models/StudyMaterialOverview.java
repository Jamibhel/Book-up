package com.example.bookup.models;

import java.io.Serializable;

public class StudyMaterialOverview implements Serializable {
    private String title;
    private String courseCode;
    private String thumbnailUrl;
    private String materialId; // Firestore document ID for the material

    // No-argument constructor required for Firestore
    public StudyMaterialOverview() {
    }

    public StudyMaterialOverview(String title, String courseCode, String thumbnailUrl, String materialId) {
        this.title = title;
        this.courseCode = courseCode;
        this.thumbnailUrl = thumbnailUrl;
        this.materialId = materialId;
    }

    public interface OnMaterialClickListener {
        void onMaterialClick(StudyMaterial material); // Takes a StudyMaterial object
    }


    // Getters
    public String getTitle() { return title; }
    public String getCourseCode() { return courseCode; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getMaterialId() { return materialId; }

    // Setters (if needed)
    public void setTitle(String title) { this.title = title; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }
}
