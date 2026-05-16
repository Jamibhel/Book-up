package com.example.bookup.services;

import android.util.Log;
import com.example.bookup.models.ChatMessage;
import com.example.bookup.models.Conversation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Search service for BookUp chat system.
 * Supports searching conversations by name and messages by content.
 *
 * Features:
 * - Case-insensitive conversation search by name
 * - Full-text message search within a conversation
 * - User search (find users by name)
 * - Search history tracking (optional)
 */
public class SearchService {

    private static final String TAG = "SearchService";
    private static final String CONVERSATIONS_COLLECTION = "conversations";
    private static final String MESSAGES_COLLECTION = "messages";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface SearchResultListener<T> {
        void onSearchResults(List<T> results);
        void onSearchError(Exception error);
    }

    /**
     * Search conversations by name (case-insensitive).
     * This searches for partial matches in conversation names.
     */
    public void searchConversations(String query, SearchResultListener<Conversation> listener) {
        if (query == null || query.isEmpty()) {
            Log.w(TAG, "Empty search query");
            if (listener != null) listener.onSearchResults(new ArrayList<>());
            return;
        }

        String lowerQuery = query.toLowerCase();

        db.collection(CONVERSATIONS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Conversation> results = new ArrayList<>();
                    querySnapshot.getDocuments().forEach(doc -> {
                        Conversation conv = doc.toObject(Conversation.class);
                        if (conv != null && conv.participantNames != null) {
                            // Search in participant names (Map<String, String>)
                            for (String name : conv.participantNames.values()) {
                                if (name != null && name.toLowerCase().contains(lowerQuery)) {
                                    results.add(conv);
                                    break;
                                }
                            }
                        } else if (conv != null && conv.getConversationName() != null) {
                            // Fallback: search in conversationName
                            if (conv.getConversationName().toLowerCase().contains(lowerQuery)) {
                                results.add(conv);
                            }
                        }
                    });
                    Log.d(TAG, "Found " + results.size() + " conversations matching: " + query);
                    if (listener != null) listener.onSearchResults(results);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Conversation search failed", e);
                    if (listener != null) listener.onSearchError(e);
                });
    }

    /**
     * Search messages within a specific conversation.
     * Searches message content for the given query string.
     */
    public void searchMessages(String conversationId, String query, SearchResultListener<ChatMessage> listener) {
        if (query == null || query.isEmpty()) {
            Log.w(TAG, "Empty search query");
            if (listener != null) listener.onSearchResults(new ArrayList<>());
            return;
        }

        if (conversationId == null || conversationId.isEmpty()) {
            Log.e(TAG, "Invalid conversation ID for message search");
            if (listener != null) {
                listener.onSearchError(new Exception("Invalid conversation ID"));
            }
            return;
        }

        String lowerQuery = query.toLowerCase();

        db.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(MESSAGES_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<ChatMessage> results = new ArrayList<>();
                    querySnapshot.getDocuments().forEach(doc -> {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null && msg.getMessageText() != null) {
                            if (msg.getMessageText().toLowerCase().contains(lowerQuery)) {
                                results.add(msg);
                            }
                        }
                    });
                    Log.d(TAG, "Found " + results.size() + " messages matching: " + query);
                    if (listener != null) listener.onSearchResults(results);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Message search failed", e);
                    if (listener != null) listener.onSearchError(e);
                });
    }

    /**
     * Search users by name or username.
     * Useful for finding people to start new conversations with.
     */
    public void searchUsers(String query, SearchResultListener<String> listener) {
        if (query == null || query.isEmpty()) {
            Log.w(TAG, "Empty search query");
            if (listener != null) listener.onSearchResults(new ArrayList<>());
            return;
        }

        String lowerQuery = query.toLowerCase();

        db.collection(USERS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> results = new ArrayList<>();
                    querySnapshot.getDocuments().forEach(doc -> {
                        String userId = doc.getId();
                        String displayName = doc.getString("displayName");
                        String email = doc.getString("email");

                        if (displayName != null && displayName.toLowerCase().contains(lowerQuery)) {
                            results.add(userId);
                        } else if (email != null && email.toLowerCase().contains(lowerQuery)) {
                            results.add(userId);
                        }
                    });
                    Log.d(TAG, "Found " + results.size() + " users matching: " + query);
                    if (listener != null) listener.onSearchResults(results);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "User search failed", e);
                    if (listener != null) listener.onSearchError(e);
                });
    }

    /**
     * Search across all messages and conversations.
     * Returns both matching conversations and messages.
     */
    public void globalSearch(String query, OnGlobalSearchListener listener) {
        if (query == null || query.isEmpty()) {
            Log.w(TAG, "Empty search query");
            if (listener != null) listener.onResults(new ArrayList<>(), new ArrayList<>());
            return;
        }

        // Search conversations
        searchConversations(query, new SearchResultListener<Conversation>() {
            @Override
            public void onSearchResults(List<Conversation> results) {
                // Now search messages
                searchAllMessages(query, new SearchResultListener<ChatMessage>() {
                    @Override
                    public void onSearchResults(List<ChatMessage> messageResults) {
                        if (listener != null) listener.onResults(results, messageResults);
                    }

                    @Override
                    public void onSearchError(Exception error) {
                        if (listener != null) listener.onError(error);
                    }
                });
            }

            @Override
            public void onSearchError(Exception error) {
                if (listener != null) listener.onError(error);
            }
        });
    }

    /**
     * Search messages across all conversations.
     */
    private void searchAllMessages(String query, SearchResultListener<ChatMessage> listener) {
        String lowerQuery = query.toLowerCase();

        db.collection(CONVERSATIONS_COLLECTION)
                .get()
                .addOnSuccessListener(conversationSnapshots -> {
                    List<ChatMessage> allResults = new ArrayList<>();
                    int[] completedSearches = {0};
                    int totalConversations = conversationSnapshots.size();

                    if (totalConversations == 0) {
                        if (listener != null) listener.onSearchResults(allResults);
                        return;
                    }

                    conversationSnapshots.getDocuments().forEach(convDoc -> {
                        convDoc.getReference().collection(MESSAGES_COLLECTION)
                                .get()
                                .addOnSuccessListener(messageSnapshots -> {
                                    messageSnapshots.getDocuments().forEach(msgDoc -> {
                                        ChatMessage msg = msgDoc.toObject(ChatMessage.class);
                                        if (msg != null && msg.getMessageText() != null) {
                                            if (msg.getMessageText().toLowerCase().contains(lowerQuery)) {
                                                allResults.add(msg);
                                            }
                                        }
                                    });
                                    completedSearches[0]++;
                                    if (completedSearches[0] == totalConversations) {
                                        Log.d(TAG, "Global search complete. Found " + allResults.size() + " messages");
                                        if (listener != null) listener.onSearchResults(allResults);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    completedSearches[0]++;
                                    if (completedSearches[0] == totalConversations) {
                                        if (listener != null) listener.onSearchError(e);
                                    }
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Global message search failed", e);
                    if (listener != null) listener.onSearchError(e);
                });
    }

    public interface OnGlobalSearchListener {
        void onResults(List<Conversation> conversationResults, List<ChatMessage> messageResults);
        void onError(Exception error);
    }
}
