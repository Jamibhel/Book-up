package com.example.bookup.models;

import java.io.Serializable;
import java.util.Date;

public class Booking implements Serializable {
    private String id;
    private String tutorId;
    private String studentId;
    private String tutorName;
    private String studentName;
    private Date sessionDate;
    private String subject;
    private String description;
    private String status; // "pending", "confirmed", "completed", "cancelled"
    private Date createdAt;
    private Date updatedAt;

    public Booking() {}

    public Booking(String tutorId, String studentId, String tutorName, String studentName) {
        this.tutorId = tutorId;
        this.studentId = studentId;
        this.tutorName = tutorName;
        this.studentName = studentName;
        this.status = "pending";
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
