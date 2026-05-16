package com.example.bookup.models;

import java.io.Serializable;

public class Availability implements Serializable {
    private String day; // "Monday", "Tuesday", etc.
    private boolean available;
    private String startTime; // "09:00"
    private String endTime;   // "17:00"

    public Availability() {}

    public Availability(String day, boolean available, String startTime, String endTime) {
        this.day = day;
        this.available = available;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
