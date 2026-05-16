package com.example.bookup.models;

import com.google.firebase.firestore.PropertyName;
import java.util.List;

public class User {
    private String id;
    private String displayName;
    private String firstName;
    private String lastName;
    private String email;
    private String photoUrl;
    private String bio;
    private boolean isAdmin;
    private boolean blocked;
    private String fcmToken;
    private String role; // "student", "tutor", or "admin"
    private List<String> tutoringSubjects;
    private double rating;
    private int reviewCount;
    private double hourlyRate; // Rate in Naira
    private boolean isAvailable;
    private List<Availability> availability;
    private boolean isOnline;
    private com.google.firebase.Timestamp lastSeen;

    // Location Fields
    private String locationName; // e.g. "Main Campus", "Lagos"
    private String workPreference; // "online", "in_person", "both"
    private double latitude;
    private double longitude;

    public User() {}

    public User(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
        this.bio = "";
        this.isAdmin = false;
        this.blocked = false;
        this.role = "student";
        this.workPreference = "both";
        this.hourlyRate = 0.0;
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { 
        if (displayName != null && !displayName.isEmpty()) return displayName;
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? "Unknown User" : combined;
    }
    public String getEmail() { return email; }
    
    @PropertyName("photoUrl")
    public String getPhotoUrl() { return photoUrl; }
    
    @PropertyName("profileImageUrl") // Legacy fallback
    public String getProfileImageUrl() { return photoUrl; }

    public String getBio() { return bio; }
    
    @PropertyName("isAdmin")
    public boolean isAdmin() { 
        return isAdmin || "admin".equalsIgnoreCase(role); 
    }

    public boolean isBlocked() { return blocked; }
    public String getFcmToken() { return fcmToken; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role != null ? role : "student"; }
    public List<String> getTutoringSubjects() { return tutoringSubjects; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public double getHourlyRate() { return hourlyRate; }
    
    @PropertyName("available")
    public boolean isAvailable() { return isAvailable; }

    public List<Availability> getAvailability() { return availability; }
    public boolean isOnline() { return isOnline; }
    public com.google.firebase.Timestamp getLastSeen() { return lastSeen; }

    public String getLocationName() { return locationName; }
    public String getWorkPreference() { return workPreference != null ? workPreference : "both"; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setEmail(String email) { this.email = email; }
    
    @PropertyName("photoUrl")
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    
    @PropertyName("profileImageUrl")
    public void setProfileImageUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public void setBio(String bio) { this.bio = bio; }
    
    @PropertyName("isAdmin")
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setRole(String role) { this.role = role; }
    public void setTutoringSubjects(List<String> tutoringSubjects) { this.tutoringSubjects = tutoringSubjects; }
    public void setRating(double rating) { this.rating = rating; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
    
    @PropertyName("available")
    public void setAvailable(boolean available) { isAvailable = available; }

    public void setAvailability(List<Availability> availability) { this.availability = availability; }
    public void setOnline(boolean online) { isOnline = online; }
    public void setLastSeen(com.google.firebase.Timestamp lastSeen) { this.lastSeen = lastSeen; }

    public void setLocationName(String locationName) { this.locationName = locationName; }
    public void setWorkPreference(String workPreference) { this.workPreference = workPreference; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
