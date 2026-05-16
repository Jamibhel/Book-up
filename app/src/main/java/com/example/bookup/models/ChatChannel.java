package com.example.bookup.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatChannel {
    private String id;
    private List<String> participantIds;
    private Map<String, String> participantNames = new HashMap<>();
    private Map<String, String> participantPhotos = new HashMap<>();
    private String lastMessage;
    private String lastMessageSenderId;
    private Timestamp lastMessageTimestamp;
    private boolean isGroup;
    private String groupName;
    private String groupDescription;
    private String groupImage;
    private Map<String, Timestamp> lastRead = new HashMap<>();
    private Map<String, Boolean> pinnedBy = new HashMap<>();
    private List<String> deletedBy = new java.util.ArrayList<>();
    
    // Extra fields to suppress warnings and support legacy data
    private Map<String, Boolean> typingStatus = new HashMap<>();
    private Object createdAt;
    private String adminId;

    public ChatChannel() {}

    public ChatChannel(List<String> participantIds, boolean isGroup) {
        this.participantIds = participantIds;
        this.isGroup = isGroup;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    @PropertyName("participantIds")
    public List<String> getParticipantIds() { return participantIds; }
    @PropertyName("participantIds")
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }

    @PropertyName("participantNames")
    public Map<String, String> getParticipantNames() { return participantNames; }
    @PropertyName("participantNames")
    public void setParticipantNames(Map<String, String> participantNames) { 
        if (participantNames != null) this.participantNames = participantNames; 
    }

    @PropertyName("participantPhotos")
    public Map<String, String> getParticipantPhotos() { return participantPhotos; }
    @PropertyName("participantPhotos")
    public void setParticipantPhotos(Map<String, String> participantPhotos) { 
        if (participantPhotos != null) this.participantPhotos = participantPhotos; 
    }

    @PropertyName("lastMessage")
    public String getLastMessage() { return lastMessage; }
    @PropertyName("lastMessage")
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastMessageSenderId() { return lastMessageSenderId; }
    public void setLastMessageSenderId(String lastMessageSenderId) { this.lastMessageSenderId = lastMessageSenderId; }

    @PropertyName("lastMessageTimestamp")
    public Timestamp getLastMessageTimestamp() { return lastMessageTimestamp; }
    
    @PropertyName("lastMessageTimestamp")
    public void setLastMessageTimestamp(Object timestamp) {
        if (timestamp instanceof Timestamp) {
            this.lastMessageTimestamp = (Timestamp) timestamp;
        } else if (timestamp instanceof Long) {
            this.lastMessageTimestamp = new Timestamp(new java.util.Date((Long) timestamp));
        }
    }

    @PropertyName("isGroup")
    public boolean isGroup() { return isGroup; }
    @PropertyName("isGroup")
    public void setGroup(boolean group) { isGroup = group; }
    
    @PropertyName("group") // Legacy field name support
    public void setGroupLegacy(boolean group) { this.isGroup = group; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String name) { this.groupName = name; }
    
    @PropertyName("conversationName") // Legacy field name support
    public void setConversationName(String name) { this.groupName = name; }

    public String getGroupDescription() { return groupDescription; }
    public void setGroupDescription(String groupDescription) { this.groupDescription = groupDescription; }

    public String getGroupImage() { return groupImage; }
    public void setGroupImage(String image) { this.groupImage = image; }
    
    @PropertyName("conversationImage") // Legacy field name support
    public void setConversationImage(String image) { this.groupImage = image; }

    @PropertyName("lastRead")
    public Map<String, Timestamp> getLastRead() { return lastRead; }
    @PropertyName("lastRead")
    public void setLastRead(Map<String, Timestamp> lastRead) { 
        if (lastRead != null) this.lastRead = lastRead; 
    }

    public Map<String, Boolean> getPinnedBy() { return pinnedBy; }
    public void setPinnedBy(Map<String, Boolean> pinnedBy) { 
        if (pinnedBy != null) this.pinnedBy = pinnedBy; 
    }

    public List<String> getDeletedBy() { return deletedBy; }
    public void setDeletedBy(List<String> deletedBy) { 
        if (deletedBy != null) this.deletedBy = deletedBy; 
    }

    // Suppress dynamic field warnings
    @PropertyName("typingStatus")
    public Map<String, Boolean> getTypingStatus() { return typingStatus; }
    @PropertyName("typingStatus")
    public void setTypingStatus(Map<String, Boolean> typingStatus) { if (typingStatus != null) this.typingStatus = typingStatus; }

    @PropertyName("createdAt")
    public Object getCreatedAt() { return createdAt; }
    @PropertyName("createdAt")
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
}
