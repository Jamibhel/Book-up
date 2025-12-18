package com.example.bookup.models;

public class User {
    private String id;
    private String displayName;
    private String email;
    private String photoUrl;
    private String bio;
    private boolean isAdmin;
    private boolean blocked;
    private String fcmToken;

    // Required empty constructor for Firestore
    public User() {}

    public User(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
        this.bio = "";
        this.isAdmin = false;
        this.blocked = false;
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
    public String getBio() { return bio; }
    public boolean isAdmin() { return isAdmin; }
    public boolean isBlocked() { return blocked; }
    public String getFcmToken() { return fcmToken; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setBio(String bio) { this.bio = bio; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
}