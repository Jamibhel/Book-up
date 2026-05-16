package com.example.bookup.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Model class for chat conversations, supporting the new chat system.
 */
@IgnoreExtraProperties
public class Conversation implements Serializable {
    public String conversationId;
    public String conversationName;
    public String conversationImage;
    public List<String> participantIds;
    public Map<String, String> participantNames;
    public String lastMessageId;
    public String lastMessageContent;
    public String lastMessageSenderId;
    public String lastMessageSenderName;
    public Timestamp lastMessageTimestamp;
    public long unreadCount;
    public boolean isMuted;
    public boolean isPinned;
    public boolean isGroupChat;
    public Timestamp createdAt;
    public Timestamp updatedAt;

    public Conversation() {
        // Required for Firestore
    }

    @PropertyName("conversationId")
    public String getConversationId() { return conversationId; }
    @PropertyName("conversationId")
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    // Compatibility with 'id' field in older documents
    @PropertyName("id")
    public String getId() { return conversationId; }
    @PropertyName("id")
    public void setId(String id) { 
        if (this.conversationId == null) this.conversationId = id; 
    }

    @PropertyName("conversationName")
    public String getConversationName() { return conversationName; }
    @PropertyName("conversationName")
    public void setConversationName(String conversationName) { this.conversationName = conversationName; }

    // Compatibility with 'groupName' field
    @PropertyName("groupName")
    public String getGroupName() { return conversationName; }
    @PropertyName("groupName")
    public void setGroupName(String groupName) {
        if (this.conversationName == null) this.conversationName = groupName;
    }

    @PropertyName("lastMessageContent")
    public String getLastMessageContent() { return lastMessageContent; }
    @PropertyName("lastMessageContent")
    public void setLastMessageContent(String lastMessageContent) { this.lastMessageContent = lastMessageContent; }

    // Compatibility with 'lastMessage' field
    @PropertyName("lastMessage")
    public String getLastMessage() { return lastMessageContent; }
    @PropertyName("lastMessage")
    public void setLastMessage(String lastMessage) {
        if (this.lastMessageContent == null) this.lastMessageContent = lastMessage;
    }
}
