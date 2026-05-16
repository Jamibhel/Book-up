# Data Migration Guide - ChatChannels to Conversations

## Current Status

The Book Up chat system uses a unified architecture that is **fully backward compatible** with the old "chatChannels" collection while supporting the new "conversations" collection structure.

## Architecture

### Unified Conversation Model
- **File**: `Conversation.java`
- **Features**: Uses `@PropertyName` annotations to map both old and new field names
- **Status**: Automatically handles both schemas without code changes

### ChatRepository Configuration
- **File**: `ChatRepository.java`
- **Current Collection**: `"chatChannels"` (COLLECTION_CONVERSATIONS variable)
- **Status**: Fully functional and backward compatible

## Migration Overview

### Why Migrate?
1. **Data Consolidation**: Move from old "chatChannels" to new "conversations" collection
2. **Schema Consistency**: Ensure all data uses the new conversation structure
3. **Future Scalability**: Prepare for additional features requiring the new schema

### Do You Need to Migrate?

**Not Required If:**
- Your app is already using "chatChannels" collection
- Users can seamlessly chat using existing data
- You want to maintain the current system

**Recommended If:**
- You're starting fresh and want clean data structure
- You plan extensive feature development
- You want to follow the new unified schema

## Migration Process

### Step 1: Check Migration Status

```java
DataMigrationManager.checkMigrationStatus(new DataMigrationManager.OnMigrationStatusListener() {
    @Override
    public void onStatusChecked(DataMigrationManager.MigrationStatus status) {
        Log.i("Migration", "Status: " + status);
        // status.needsMigration indicates if migration is required
        // status.conversationsCollectionExists - true if "conversations" collection exists
        // status.chatChannelsCollectionExists - true if "chatChannels" collection exists
    }

    @Override
    public void onError(Exception e) {
        Log.e("Migration", "Error checking status", e);
    }
});
```

### Step 2: Execute Migration (if needed)

```java
DataMigrationManager.migrateData(new DataMigrationManager.OnMigrationProgressListener() {
    @Override
    public void onProgress(int current, int total) {
        Log.d("Migration", "Progress: " + current + "/" + total);
        // Update UI with progress
    }

    @Override
    public void onComplete(int migratedCount) {
        Log.i("Migration", "✅ Migration complete! Migrated " + migratedCount + " conversations");
        // Now update ChatRepository.COLLECTION_CONVERSATIONS to "conversations"
    }

    @Override
    public void onError(Exception e) {
        Log.e("Migration", "❌ Migration failed", e);
    }
});
```

### Step 3: Switch to New Collection (Optional)

After successful migration, update `ChatRepository.java`:

```java
// BEFORE
private static final String COLLECTION_CONVERSATIONS = "chatChannels";

// AFTER
private static final String COLLECTION_CONVERSATIONS = "conversations";
```

## Implementation Details

### DataMigrationManager
- **Location**: `com.example.bookup.utils.DataMigrationManager`
- **Features**:
  - Check migration status
  - Migrate all conversations with progress tracking
  - Handle errors gracefully
  - Non-blocking async operations

### What Gets Migrated
- **From**: `db.collection("chatChannels").documents`
- **To**: `db.collection("conversations").documents`
- **Data**: Copied exactly as-is (no transformation needed due to unified schema)
- **Messages**: Message subcollections are NOT migrated (they stay under original conversation IDs)

## Firestore Collections After Migration

### Before Migration
```
Firestore
├── chatChannels/
│   ├── conversation-1/ (Conversation document)
│   │   └── messages/ (Subcollection)
│   └── conversation-2/
│       └── messages/
└── (other collections)
```

### After Migration
```
Firestore
├── chatChannels/ (Original, can be deleted after verification)
│   ├── conversation-1/
│   │   └── messages/
│   └── conversation-2/
│       └── messages/
├── conversations/ (New, active)
│   ├── conversation-1/ (Same data as chatChannels)
│   ├── conversation-2/
│   └── (no messages subcollections - they stay under chatChannels)
└── (other collections)
```

## Important Notes

### Message Subcollections
- Messages in subcollections (e.g., `chatChannels/{id}/messages`) are NOT automatically migrated
- This is intentional - keeping historical messages in original location reduces cost
- Messages are queried by conversation ID, so references still work after migration
- If you want to move messages too, create a separate migration script

### Backward Compatibility
- Old code will continue to work even after migration
- The unified Conversation model handles both old and new schemas
- No frontend code changes required

### Data Verification
After migration, verify:
1. All conversations appear in the new "conversations" collection
2. Conversation IDs match between old and new collections
3. Messages are still queryable (subcollections not moved is OK)
4. No data loss occurred

## Rollback Plan

If migration fails:
1. Verify original "chatChannels" collection is intact
2. Delete problematic documents from "conversations" collection
3. Check error logs for specific issues
4. Retry migration after fixing issues

## FAQ

**Q: Will this affect active users?**
A: No. The chat system is fully functional before, during, and after migration.

**Q: Do I have to migrate?**
A: No. The system is backward compatible. However, migration is recommended for long-term data management.

**Q: What about message attachments and media?**
A: Messages and media are stored in Firebase Storage independently. The migration only updates Firestore document references.

**Q: Can I migrate in the background?**
A: Yes. The migration process is fully asynchronous and won't block the UI.

**Q: What if some conversations are not migrated?**
A: The progress callback shows which documents succeeded. You can retry failed conversations individually.

## Support

For migration issues:
1. Check the Logcat output for detailed error messages
2. Verify Firestore rules allow writes to "conversations" collection
3. Ensure sufficient Firestore quota
4. Contact development team with error logs

---

**Last Updated**: 2025-12-23
**Status**: Ready for migration when needed
**Effort**: Low (automated process)
**Risk**: Very Low (backward compatible, non-destructive)
