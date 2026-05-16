package com.example.bookup.repositories;

import android.util.Log;

import com.example.bookup.models.ChatChannel;
import com.example.bookup.models.Message;
import com.example.bookup.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {
    private static final String TAG = "ChatRepository";
    private static final String MODERN_CHANNELS = "conversations";
    private static final String LEGACY_CHANNELS = "chatChannels";
    private static final String MESSAGES = "messages";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnChannelsLoadedListener {
        void onChannelsLoaded(List<ChatChannel> channels);
        void onError(Exception e);
    }

    public interface OnMessagesLoadedListener {
        void onMessagesLoaded(List<Message> messages);
        void onError(Exception e);
    }

    public ListenerRegistration getChannels(String userId, OnChannelsLoadedListener listener) {
        final Map<String, ChatChannel> mergedMap = new HashMap<>();
        
        ListenerRegistration reg1 = db.collection(MODERN_CHANNELS)
                .whereArrayContains("participantIds", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "getChannels: MODERN_CHANNELS error", error);
                        listener.onError(error);
                        return;
                    }
                    if (value != null) {
                        for (var doc : value.getDocuments()) {
                            ChatChannel channel = doc.toObject(ChatChannel.class);
                            if (channel != null) { channel.setId(doc.getId()); mergedMap.put(doc.getId(), channel); }
                        }
                        notifyMerged(mergedMap, listener);
                    }
                });

        ListenerRegistration reg2 = db.collection(LEGACY_CHANNELS)
                .whereArrayContains("participantIds", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "getChannels: LEGACY_CHANNELS error", error);
                        listener.onError(error);
                        return;
                    }
                    if (value != null) {
                        for (var doc : value.getDocuments()) {
                            ChatChannel channel = doc.toObject(ChatChannel.class);
                            if (channel != null && !mergedMap.containsKey(doc.getId())) {
                                channel.setId(doc.getId());
                                mergedMap.put(doc.getId(), channel);
                            }
                        }
                        notifyMerged(mergedMap, listener);
                    }
                });

        return () -> { reg1.remove(); reg2.remove(); };
    }

    private void notifyMerged(Map<String, ChatChannel> map, OnChannelsLoadedListener listener) {
        List<ChatChannel> merged = new ArrayList<>(map.values());
        merged.sort((c1, c2) -> {
            Date d1 = c1.getLastMessageTimestamp() != null ? c1.getLastMessageTimestamp().toDate() : new Date(0);
            Date d2 = c2.getLastMessageTimestamp() != null ? c2.getLastMessageTimestamp().toDate() : new Date(0);
            return d2.compareTo(d1);
        });
        listener.onChannelsLoaded(merged);
    }

    public ListenerRegistration getMessages(String channelId, OnMessagesLoadedListener listener) {
        final Map<String, Message> messageMap = new HashMap<>();

        ListenerRegistration reg1 = db.collection(MODERN_CHANNELS).document(channelId).collection(MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        for (var doc : value.getDocuments()) {
                            Message m = doc.toObject(Message.class);
                            if (m != null) { m.setId(doc.getId()); messageMap.put(m.getId(), m); }
                        }
                        notifyMessages(messageMap, listener);
                    }
                });

        ListenerRegistration reg2 = db.collection(LEGACY_CHANNELS).document(channelId).collection(MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        for (var doc : value.getDocuments()) {
                            Message m = doc.toObject(Message.class);
                            if (m != null) { m.setId(doc.getId()); messageMap.put(m.getId(), m); }
                        }
                        notifyMessages(messageMap, listener);
                    }
                });

        return () -> { reg1.remove(); reg2.remove(); };
    }

    private void notifyMessages(Map<String, Message> map, OnMessagesLoadedListener listener) {
        List<Message> merged = new ArrayList<>(map.values());
        merged.sort((m1, m2) -> {
            Date d1 = m1.getTimestamp() != null ? m1.getTimestamp().toDate() : new Date(0);
            Date d2 = m2.getTimestamp() != null ? m2.getTimestamp().toDate() : new Date(0);
            return d1.compareTo(d2);
        });
        listener.onMessagesLoaded(merged);
    }

    public Task<Void> sendMessage(String channelId, Message message) {
        DocumentReference modernRef = db.collection(MODERN_CHANNELS).document(channelId);
        return modernRef.get().continueWithTask(task -> {
            // Force create document if missing to ensure sub-collection write succeeds
            DocumentReference target = (task.isSuccessful() && task.getResult().exists()) ? modernRef : db.collection(LEGACY_CHANNELS).document(channelId);
            
            DocumentReference msgRef = target.collection(MESSAGES).document();
            message.setId(msgRef.getId());
            
            WriteBatch batch = db.batch();
            batch.set(msgRef, message);
            
            Map<String, Object> up = new HashMap<>();
            up.put("lastMessage", message.getType() == Message.Type.TEXT ? message.getText() : "[" + message.getType().name() + "]");
            up.put("lastMessageSenderId", message.getSenderId());
            up.put("lastMessageTimestamp", com.google.firebase.Timestamp.now());
            
            batch.update(target, up);
            
            return batch.commit().continueWithTask(t2 -> {
                if (!t2.isSuccessful()) {
                    // Critical fallback: just write the message directly
                    return target.collection(MESSAGES).document(message.getId()).set(message);
                }
                return Tasks.forResult(null);
            });
        });
    }

    public Task<String> getOrCreateChatChannel(User me, User other) {
        List<String> ids = Arrays.asList(me.getId(), other.getId());
        java.util.Collections.sort(ids);
        String customId = ids.get(0) + "_" + ids.get(1);

        return db.collection(MODERN_CHANNELS).document(customId).get().continueWithTask(t -> {
            if (t.isSuccessful() && t.getResult().exists()) return Tasks.forResult(customId);
            return db.collection(LEGACY_CHANNELS).document(customId).get().continueWithTask(t2 -> {
                if (t2.isSuccessful() && t2.getResult().exists()) return Tasks.forResult(customId);
                
                ChatChannel c = new ChatChannel(ids, false);
                c.setId(customId);
                c.getParticipantNames().put(me.getId(), me.getDisplayName());
                c.getParticipantNames().put(other.getId(), other.getDisplayName());
                c.getParticipantPhotos().put(me.getId(), me.getPhotoUrl());
                c.getParticipantPhotos().put(other.getId(), other.getPhotoUrl());
                c.setLastMessageTimestamp(com.google.firebase.Timestamp.now());
                
                return db.collection(MODERN_CHANNELS).document(customId).set(c).continueWith(t3 -> customId);
            });
        });
    }

    public Task<String> createGroupChannel(String name, List<User> members, User creator) {
        List<String> ids = new ArrayList<>();
        Map<String, String> names = new HashMap<>();
        Map<String, String> photos = new HashMap<>();
        ids.add(creator.getId());
        names.put(creator.getId(), creator.getDisplayName());
        photos.put(creator.getId(), creator.getPhotoUrl());
        for (User u : members) {
            ids.add(u.getId());
            names.put(u.getId(), u.getDisplayName());
            photos.put(u.getId(), u.getPhotoUrl());
        }
        ChatChannel c = new ChatChannel(ids, true);
        c.setAdminId(creator.getId());
        c.setGroupName(name);
        c.setParticipantNames(names);
        c.setParticipantPhotos(photos);
        c.setLastMessage("Group created");
        c.setLastMessageTimestamp(com.google.firebase.Timestamp.now());
        return db.collection(MODERN_CHANNELS).add(c).continueWith(task -> task.getResult().getId());
    }

    public void setTypingStatus(String cid, String uid, boolean typing) {
        db.collection(MODERN_CHANNELS).document(cid).update("typingStatus." + uid, typing)
                .addOnFailureListener(e -> db.collection(LEGACY_CHANNELS).document(cid).update("typingStatus." + uid, typing));
    }

    public ListenerRegistration listenForTypingStatus(String cid, OnTypingListener l) {
        return db.collection(MODERN_CHANNELS).document(cid).addSnapshotListener((v, e) -> {
            if (v != null && v.exists()) {
                Map<String, Boolean> map = (Map<String, Boolean>) v.get("typingStatus");
                if (map != null) l.onTypingUpdate(map);
            }
        });
    }

    public interface OnTypingListener { void onTypingUpdate(Map<String, Boolean> status); }

    public void markAsRead(String cid, String uid) {
        db.collection(MODERN_CHANNELS).document(cid).update("lastRead." + uid, com.google.firebase.Timestamp.now())
                .addOnFailureListener(e -> db.collection(LEGACY_CHANNELS).document(cid).update("lastRead." + uid, com.google.firebase.Timestamp.now()));
    }

    public Task<Void> deleteMessageForEveryone(String cid, String mid) {
        return db.collection(MODERN_CHANNELS).document(cid).collection(MESSAGES).document(mid).update("isDeletedForEveryone", true)
                .continueWithTask(t -> t.isSuccessful() ? Tasks.forResult(null) : db.collection(LEGACY_CHANNELS).document(cid).collection(MESSAGES).document(mid).update("isDeletedForEveryone", true));
    }

    public Task<Void> deleteMessageForMe(String cid, String mid, String uid) {
        return db.collection(MODERN_CHANNELS).document(cid).collection(MESSAGES).document(mid).update("deletedForUsers", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
                .continueWithTask(t -> t.isSuccessful() ? Tasks.forResult(null) : db.collection(LEGACY_CHANNELS).document(cid).collection(MESSAGES).document(mid).update("deletedForUsers", com.google.firebase.firestore.FieldValue.arrayUnion(uid)));
    }

    public void updatePresence(String uid, boolean online) {
        Map<String, Object> up = new HashMap<>();
        up.put("isOnline", online);
        up.put("lastSeen", com.google.firebase.Timestamp.now());
        db.collection("users").document(uid).update(up);
    }

    public Task<Void> pinChannel(String cid, String uid, boolean pin) {
        return db.collection(MODERN_CHANNELS).document(cid).update("pinnedBy." + uid, pin)
                .continueWithTask(t -> t.isSuccessful() ? Tasks.forResult(null) : db.collection(LEGACY_CHANNELS).document(cid).update("pinnedBy." + uid, pin));
    }

    public Task<Void> deleteChannelForMe(String cid, String uid) {
        return db.collection(MODERN_CHANNELS).document(cid).update("deletedBy", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
                .continueWithTask(t -> t.isSuccessful() ? Tasks.forResult(null) : db.collection(LEGACY_CHANNELS).document(cid).update("deletedBy", com.google.firebase.firestore.FieldValue.arrayUnion(uid)));
    }

    public Task<Void> addParticipant(String channelId, User user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("participantIds", com.google.firebase.firestore.FieldValue.arrayUnion(user.getId()));
        updates.put("participantNames." + user.getId(), user.getDisplayName());
        updates.put("participantPhotos." + user.getId(), user.getPhotoUrl());
        
        return db.collection(MODERN_CHANNELS).document(channelId).update(updates);
    }

    public Task<Void> removeParticipant(String channelId, String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("participantIds", com.google.firebase.firestore.FieldValue.arrayRemove(userId));
        // We keep the name/photo for message history context usually, or remove them
        return db.collection(MODERN_CHANNELS).document(channelId).update(updates);
    }
}
