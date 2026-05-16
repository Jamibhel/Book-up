package com.example.bookup.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages data migration from old "chatChannels" collection to new "conversations" collection.
 * 
 * The unified Conversation model with @PropertyName annotations provides backward compatibility,
 * so this migration is optional and primarily for data consolidation and cleanup.
 * 
 * Current System:
 * - ChatRepository uses "chatChannels" collection (COLLECTION_CONVERSATIONS)
 * - Conversation model has @PropertyName for both old and new field names
 * - System is fully backward compatible
 * 
 * Migration Strategy:
 * 1. Query all documents from "chatChannels"
 * 2. Transform to new schema (if needed)
 * 3. Write to "conversations" collection (optional)
 * 4. Update ChatRepository to use "conversations" if desired
 */
public class DataMigrationManager {

    private static final String TAG = "DataMigrationManager";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Check if migration from chatChannels to conversations is needed.
     */
    public static void checkMigrationStatus(OnMigrationStatusListener listener) {
        // Check if conversations collection exists and has documents
        db.collection("conversations")
            .limit(1)
            .get()
            .addOnSuccessListener(snapshot -> {
                boolean conversationsExists = !snapshot.isEmpty();
                
                // Check if chatChannels has documents
                db.collection("chatChannels")
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshot2 -> {
                        boolean chatChannelsExists = !snapshot2.isEmpty();
                        
                        MigrationStatus status = new MigrationStatus();
                        status.conversationsCollectionExists = conversationsExists;
                        status.chatChannelsCollectionExists = chatChannelsExists;
                        status.needsMigration = chatChannelsExists && !conversationsExists;
                        
                        listener.onStatusChecked(status);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking chatChannels", e);
                        listener.onError(e);
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking conversations", e);
                listener.onError(e);
            });
    }

    /**
     * Migrate all conversations from chatChannels to conversations collection.
     * Only call if checkMigrationStatus indicates migration is needed.
     */
    public static void migrateData(OnMigrationProgressListener listener) {
        Log.i(TAG, "🔄 Starting data migration from chatChannels to conversations");
        
        db.collection("chatChannels")
            .get()
            .addOnSuccessListener(snapshot -> {
                int totalDocs = snapshot.size();
                
                if (totalDocs == 0) {
                    Log.i(TAG, "✅ No documents to migrate");
                    listener.onComplete(0);
                    return;
                }
                
                listener.onProgress(0, totalDocs);
                
                // Use array to hold mutable count (workaround for lambda final requirement)
                int[] migratedCount = {0};
                
                for (QueryDocumentSnapshot doc : snapshot) {
                    String docId = doc.getId();
                    Map<String, Object> data = new HashMap<>(doc.getData());
                    
                    // Optionally transform data if new schema requires changes
                    // For now, data is already compatible
                    
                    // Write to new collection
                    db.collection("conversations")
                        .document(docId)
                        .set(data)
                        .addOnSuccessListener(aVoid -> {
                            migratedCount[0]++;
                            Log.d(TAG, "✅ Migrated document: " + docId + " (" + migratedCount[0] + "/" + totalDocs + ")");
                            listener.onProgress(migratedCount[0], totalDocs);
                            
                            // If all documents migrated, callback
                            if (migratedCount[0] == totalDocs) {
                                Log.i(TAG, "✅ Migration complete! Migrated " + migratedCount[0] + " documents");
                                listener.onComplete(migratedCount[0]);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "❌ Error migrating document: " + docId, e);
                            listener.onError(e);
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Error querying chatChannels", e);
                listener.onError(e);
            });
    }

    // Callbacks
    public interface OnMigrationStatusListener {
        void onStatusChecked(MigrationStatus status);
        void onError(Exception e);
    }

    public interface OnMigrationProgressListener {
        void onProgress(int current, int total);
        void onComplete(int migratedCount);
        void onError(Exception e);
    }

    public static class MigrationStatus {
        public boolean conversationsCollectionExists;
        public boolean chatChannelsCollectionExists;
        public boolean needsMigration;

        @Override
        public String toString() {
            return "MigrationStatus{" +
                    "conversationsExists=" + conversationsCollectionExists +
                    ", chatChannelsExists=" + chatChannelsCollectionExists +
                    ", needsMigration=" + needsMigration +
                    '}';
        }
    }
}
