# Quick Start Guide - Chat List Features

## Pin Conversation
1. Tap the **📌 pin icon** under any conversation
2. Conversation moves to the top of the list
3. Tap again to unpin
4. Pinned status persists in Firestore

## Delete Conversation
1. Tap the **🗑️ delete icon** under any conversation
2. Conversation is permanently deleted
3. Removed from list immediately
4. Deleted from Firestore

## Search Conversations
1. Type in the **search box** at the top
2. Searches across:
   - Tutor names (all tutors in group conversations)
   - Conversation names
   - Last message content
3. Results update in real-time as you type
4. Tap the **X** button to clear search

## View Unread Messages
1. Conversations with unread messages show a **red badge** 🔴
2. Badge displays the count (max 9+)
3. Badge disappears when you read messages

## Navigation
- **Single Tap**: Open conversation to view/send messages
- **Pin Icon**: Toggle pin status
- **Delete Icon**: Delete conversation permanently

---

## Common Issues & Solutions

### Names showing as "Conversation"
- Make sure tutor profiles are filled in Firestore
- `participantNames` map must have user ID → display name entries
- Conversation should have `conversationName` field set

### Search not finding conversations
- Type the tutor's first or last name
- Search is case-insensitive
- Checks participant names, conversation names, and message content
- Make sure tutor names are populated in `participantNames` map

### Unread badge not showing
- Badge only shows when `unreadCount > 0`
- Unread count increments when other users send messages
- Badge disappears when you open the conversation

### Pin/Delete buttons not working
- Check internet connection - requires Firestore
- Check user authentication - must be logged in
- Monitor app logs for error messages (search "ConversationAdapter" in logcat)
- Check Firestore permissions - user must have write access
