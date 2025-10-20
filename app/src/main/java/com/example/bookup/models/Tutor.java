package com.example.bookup.models;

import java.io.Serializable;
import java.util.List;

public class Tutor implements Serializable {
    private String uid;
    private String name;
    private String profileImageUrl;
    private String bio;
    private List<String> subjects; // e.g., ["Math", "Physics"]
    private double rating; // Average rating
    private int reviewCount;
    private boolean isAvailable; // Whether they are currently accepting new students

    public Tutor() {
        // Required empty public constructor for Firebase
    }

    public Tutor(String uid, String name, String profileImageUrl, String bio, List<String> subjects, double rating, int reviewCount, boolean isAvailable) {
        this.uid = uid;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
        this.subjects = subjects;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.isAvailable = isAvailable;
    }

    // Getters
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getBio() { return bio; }
    public List<String> getSubjects() { return subjects; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public boolean isAvailable() { return isAvailable; }

    // Setters (if needed, useful for Firebase data updates)
    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setBio(String bio) { this.bio = bio; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }
    public void setRating(double rating) { this.rating = rating; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
