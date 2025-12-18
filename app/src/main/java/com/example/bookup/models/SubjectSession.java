package com.example.bookup.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

public class SubjectSession implements Serializable {
    private String id;
    private String subjectName;
    private int messageCount;
    private String lastMessage;
    @ServerTimestamp
    private Date lastAccessTime;

    public SubjectSession() {
        // Required empty constructor for Firestore
    }

    public SubjectSession(String subjectName) {
        this.subjectName = subjectName;
        this.messageCount = 0;
        this.lastMessage = "";
    }

    // Getters
    public String getId() { return id; }
    public String getSubjectName() { return subjectName; }
    public int getMessageCount() { return messageCount; }
    public String getLastMessage() { return lastMessage; }
    public Date getLastAccessTime() { return lastAccessTime; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public void setMessageCount(int messageCount) { this.messageCount = messageCount; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastAccessTime(Date lastAccessTime) { this.lastAccessTime = lastAccessTime; }
}