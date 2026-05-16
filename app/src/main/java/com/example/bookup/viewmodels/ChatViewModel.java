package com.example.bookup.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookup.models.ChatChannel;
import com.example.bookup.models.Message;
import com.example.bookup.repositories.ChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.Map;

public class ChatViewModel extends ViewModel {
    private final ChatRepository repository = new ChatRepository();
    private final MutableLiveData<List<ChatChannel>> channels = new MutableLiveData<>();
    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    private final MutableLiveData<ChatChannel> currentChannel = new MutableLiveData<>();
    private final MutableLiveData<Map<String, com.google.firebase.Timestamp>> lastReadMap = new MutableLiveData<>();
    private final MutableLiveData<String> typingUser = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    private ListenerRegistration channelsListener;
    private ListenerRegistration messagesListener;
    private ListenerRegistration typingListener;
    private ListenerRegistration channelListener;

    public LiveData<List<ChatChannel>> getChannels() { return channels; }
    public LiveData<ChatChannel> getCurrentChannel() { return currentChannel; }
    public LiveData<List<Message>> getMessages() { return messages; }
    public LiveData<Map<String, com.google.firebase.Timestamp>> getLastReadMap() { return lastReadMap; }
    public LiveData<String> getTypingUser() { return typingUser; }
    public LiveData<String> getError() { return error; }

    public void loadChannels() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;
        
        if (channelsListener != null) channelsListener.remove();
        
        channelsListener = repository.getChannels(userId, new ChatRepository.OnChannelsLoadedListener() {
            @Override
            public void onChannelsLoaded(List<ChatChannel> loadedChannels) {
                channels.setValue(loadedChannels);
            }

            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });
    }

    public void loadMessages(String channelId) {
        if (messagesListener != null) messagesListener.remove();
        if (typingListener != null) typingListener.remove();
        if (channelListener != null) channelListener.remove();

        // Check modern collection for metadata
        channelListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("conversations").document(channelId)
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        ChatChannel channel = value.toObject(ChatChannel.class);
                        if (channel != null) {
                            channel.setId(value.getId());
                            currentChannel.setValue(channel);
                            lastReadMap.setValue(channel.getLastRead());
                        }
                    } else {
                        // Fallback check legacy collection for metadata
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("chatChannels").document(channelId)
                                .get().addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        ChatChannel channel = doc.toObject(ChatChannel.class);
                                        if (channel != null) {
                                            channel.setId(doc.getId());
                                            currentChannel.setValue(channel);
                                        }
                                    }
                                });
                    }
                });

        messagesListener = repository.getMessages(channelId, new ChatRepository.OnMessagesLoadedListener() {
            @Override
            public void onMessagesLoaded(List<Message> loadedMessages) {
                messages.setValue(loadedMessages);
            }

            @Override
            public void onError(Exception e) {
                error.setValue(e.getMessage());
            }
        });

        typingListener = repository.listenForTypingStatus(channelId, typingStatus -> {
            if (typingStatus == null) return;
            String currentUserId = FirebaseAuth.getInstance().getUid();
            for (Map.Entry<String, Boolean> entry : typingStatus.entrySet()) {
                if (!entry.getKey().equals(currentUserId) && Boolean.TRUE.equals(entry.getValue())) {
                    typingUser.setValue("Someone is typing...");
                    return;
                }
            }
            typingUser.setValue(null);
        });
    }

    public void setTypingStatus(String channelId, boolean isTyping) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            repository.setTypingStatus(channelId, userId, isTyping);
        }
    }

    public void markAsRead(String channelId) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            repository.markAsRead(channelId, userId);
        }
    }

    public void sendMessage(String channelId, String text, Message.Type type) {
        String userId = FirebaseAuth.getInstance().getUid();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        
        Message message = new Message(userId, userName, text, type);
        repository.sendMessage(channelId, message).addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void sendMediaMessage(String channelId, String mediaUrl, Message.Type type) {
        String userId = FirebaseAuth.getInstance().getUid();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        
        Message message = new Message(userId, userName, "", type);
        message.setMediaUrl(mediaUrl);
        repository.sendMessage(channelId, message).addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void sendReplyMessage(String channelId, String text, Message replyTo) {
        String userId = FirebaseAuth.getInstance().getUid();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        
        Message message = new Message(userId, userName, text, Message.Type.TEXT);
        message.setReplyToId(replyTo.getId());
        message.setReplyToName(replyTo.getSenderName());
        message.setReplyToText(replyTo.getType() == Message.Type.TEXT ? replyTo.getText() : "[" + replyTo.getType().name() + "]");
        
        repository.sendMessage(channelId, message).addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void deleteForEveryone(String channelId, String messageId) {
        repository.deleteMessageForEveryone(channelId, messageId).addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void deleteForMe(String channelId, String messageId) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            repository.deleteMessageForMe(channelId, messageId, userId).addOnFailureListener(e -> error.setValue(e.getMessage()));
        }
    }

    public void pinChannel(String channelId, boolean pin) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            repository.pinChannel(channelId, userId, pin).addOnFailureListener(e -> error.setValue(e.getMessage()));
        }
    }

    public void deleteChannelForMe(String channelId) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            repository.deleteChannelForMe(channelId, userId).addOnFailureListener(e -> error.setValue(e.getMessage()));
        }
    }

    public void addMember(String channelId, com.example.bookup.models.User user) {
        repository.addParticipant(channelId, user).addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void removeMember(String channelId, String userId) {
        repository.removeParticipant(channelId, userId).addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (channelsListener != null) channelsListener.remove();
        if (messagesListener != null) messagesListener.remove();
        if (typingListener != null) typingListener.remove();
        if (channelListener != null) channelListener.remove();
    }
}
