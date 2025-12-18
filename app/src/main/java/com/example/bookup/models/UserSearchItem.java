package com.example.bookup.models;

public class UserSearchItem {
    private String userId;
    private String displayName;
    private String photoUrl;

    public UserSearchItem(String userId, String displayName, String photoUrl) {
        this.userId = userId;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}