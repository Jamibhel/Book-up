package com.example.bookup.models;

import java.io.Serializable;

public class TutorOverview implements Serializable {
    private String name;
    private String profileImageUrl;
    private float rating;
    private String mainSubject;
    private String uid;

    public TutorOverview(){
    }
    public TutorOverview(String name, String profileImageUrl, float rating, String mainSubject, String uid) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.rating = rating;
        this.mainSubject = mainSubject;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }
    public String getProfileImageUrl(){
        return profileImageUrl;
    }
    public float getRating(){
        return rating;
    }
    public String getMainSubject(){
        return mainSubject;
    }
    public String getUid(){
        return uid;
    }

    //Setters for Firebase Mapping
    public void setName(String name) {
        this.name = name;
    }

    public void setMainSubject(String mainSubject) {
        this.mainSubject = mainSubject;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    public void setRating(float rating) {
        this.rating = rating;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

}
