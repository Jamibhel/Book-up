package com.example.bookup.models;

import java.io.Serializable;
import java.util.Date;

public class Call implements Serializable {
    public enum Type { VOICE, VIDEO }
    public enum Status { DIALING, CONNECTED, REJECTED, MISSED, ENDED }

    private String id;
    private String callerId;
    private String callerName;
    private String callerPhotoUrl;
    private String receiverId;
    private String receiverName;
    private String receiverPhotoUrl;
    private String channelName; // For Agora
    private String chatId; // Link to chat
    private Type type;
    private Status status;
    private Date timestamp;

    public Call() {
        this.timestamp = new Date();
    }

    public Call(String callerId, String callerName, String receiverId, String receiverName, String channelName, Type type) {
        this();
        this.callerId = callerId;
        this.callerName = callerName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.channelName = channelName;
        this.type = type;
        this.status = Status.DIALING;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCallerId() { return callerId; }
    public void setCallerId(String callerId) { this.callerId = callerId; }

    public String getCallerName() { return callerName; }
    public void setCallerName(String callerName) { this.callerName = callerName; }

    public String getCallerPhotoUrl() { return callerPhotoUrl; }
    public void setCallerPhotoUrl(String callerPhotoUrl) { this.callerPhotoUrl = callerPhotoUrl; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhotoUrl() { return receiverPhotoUrl; }
    public void setReceiverPhotoUrl(String receiverPhotoUrl) { this.receiverPhotoUrl = receiverPhotoUrl; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
