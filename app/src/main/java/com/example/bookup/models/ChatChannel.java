package com.example.bookup.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatChannel implements Serializable {
    private String id; // Document ID from Firestore
    private List<String> participantIds; // UIDs of all participants in the channel
    private Map<String, String> participantNames; // Map of participant UIDs to their display names
    private String lastMessage; // Short text of the last message in the channel
    @ServerTimestamp
    private Date lastMessageTimestamp; // Timestamp of the last message
    private boolean isGroupChat; // False for 1-to-1, true for group chats (future expansion)
    // You might add an unreadCount for the current user, but often managed client-side or with cloud functions

    public ChatChannel() {
        // Required for Firestore deserialization
    }

    public ChatChannel(List<String> participantIds, Map<String, String> participantNames, String lastMessage, Date lastMessageTimestamp, boolean isGroupChat) {
        this.participantIds = participantIds;
        this.participantNames = participantNames;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.isGroupChat = isGroupChat;
    }

    // Getters
    public String getId() { return id; }
    public List<String> getParticipantIds() { return participantIds; }
    public Map<String, String> getParticipantNames() { return participantNames; }
    public String getLastMessage() { return lastMessage; }
    public Date getLastMessageTimestamp() { return lastMessageTimestamp; }
    public boolean getIsGroupChat() { return isGroupChat; }


    // Setters
    public void setId(String id) { this.id = id; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }
    public void setParticipantNames(Map<String, String> participantNames) { this.participantNames = participantNames; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageTimestamp(Date lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }
    public void setIsGroupChat(boolean isGroupChat) { this.isGroupChat = isGroupChat; }

    /**
     * Helper method to get the display name of the other participant in a 1-to-1 chat.
     * Assumes it's a 1-to-1 chat (not a group chat).
     * @param currentUserId The UID of the current user viewing the chat list.
     * @return The display name of the other participant, or null if not found or group chat.
     */
    @Exclude // Exclude from Firestore serialization
    public String getOtherParticipantName(String currentUserId) {
        if (participantIds == null || participantIds.size() != 2 || participantNames == null) {
            return null; // Not a 1-to-1 chat or data is incomplete
        }
        for (String uid : participantIds) {
            if (!uid.equals(currentUserId)) {
                return participantNames.get(uid);
            }
        }
        return null; // Should not happen in a valid 1-to-1 chat
    }

    /**
     * Helper method to get the UID of the other participant in a 1-to-1 chat.
     * Assumes it's a 1-to-1 chat (not a group chat).
     * @param currentUserId The UID of the current user.
     * @return The UID of the other participant, or null if not found or group chat.
     */
    @Exclude
    public String getOtherParticipantId(String currentUserId) {
        if (participantIds == null || participantIds.size() != 2) {
            return null;
        }
        for (String uid : participantIds) {
            if (!uid.equals(currentUserId)) {
                return uid;
            }
        }
        return null;
    }
}
