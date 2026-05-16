package com.example.bookup.models;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.List;

public class Tutor implements Serializable {
    private String uid;
    private String name;
    private String profileImageUrl;
    private String bio;
    private List<String> subjects;
    private double rating;
    private int reviewCount;
    private boolean isAvailable; 
    private List<Availability> availability;

    public Tutor() {}

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    @PropertyName("profileImageUrl")
    public String getProfileImageUrl() { return profileImageUrl; }
    @PropertyName("profileImageUrl")
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    @PropertyName("photoUrl")
    public void setPhotoUrl(String photoUrl) { this.profileImageUrl = photoUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    
    @PropertyName("available")
    public boolean isAvailable() { return isAvailable; }
    @PropertyName("available")
    public void setAvailable(boolean available) { isAvailable = available; }

    public List<Availability> getAvailability() { return availability; }
    public void setAvailability(List<Availability> availability) { this.availability = availability; }
}
