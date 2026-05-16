package com.example.bookup.models;

import java.io.Serializable;
import java.util.Date;

public class Purchase implements Serializable {
    private String id;
    private String userId;
    private String materialId;
    private String materialTitle;
    private double amount;
    private Date timestamp;

    public Purchase() {
        this.timestamp = new Date();
    }

    public Purchase(String userId, String materialId, String materialTitle, double amount) {
        this();
        this.userId = userId;
        this.materialId = materialId;
        this.materialTitle = materialTitle;
        this.amount = amount;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }

    public String getMaterialTitle() { return materialTitle; }
    public void setMaterialTitle(String materialTitle) { this.materialTitle = materialTitle; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
