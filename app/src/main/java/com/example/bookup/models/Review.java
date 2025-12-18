package com.example.bookup.models;

import java.io.Serializable;
import java.util.Date;

public class Review implements Serializable {
    private String id;
    private String tutorId;
    private String studentId;
    private String studentName;
    private String studentPhotoUrl;
    private float rating;
    private String comment;
    private String bookingId;
    private Date createdAt;

    public Review() {}

    public Review(String tutorId, String studentId, String studentName, String studentPhotoUrl) {
        this.tutorId = tutorId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentPhotoUrl = studentPhotoUrl;
        this.createdAt = new Date();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentPhotoUrl() { return studentPhotoUrl; }
    public void setStudentPhotoUrl(String studentPhotoUrl) { this.studentPhotoUrl = studentPhotoUrl; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
