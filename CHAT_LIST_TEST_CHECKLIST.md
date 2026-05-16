# Chat List Testing Checklist

## ✅ Icon Changes
- [ ] Pin button shows **📌 pushpin icon** (not close icon)
- [ ] Delete button shows **🗑️ delete icon** (not close icon)
- [ ] Both icons are 24dp and centered properly
- [ ] Icons have correct touch feedback (ripple effect)

## ✅ Name Display
- [ ] Single tutor conversations show tutor name
- [ ] Group conversations show **all tutor names** separated by commas
- [ ] Names are bold, 15sp font size
- [ ] No "null" values shown
- [ ] No duplicate names in list
- [ ] Names truncate with ellipsis if too long

## ✅ Pin Functionality
- [ ] Tap pin icon → conversation moves to top
- [ ] Pinned conversations stay at top even after sorting
- [ ] Tap pin again → conversation unpins and moves back to normal position
- [ ] Pin status persists after closing and reopening app
- [ ] Pin icon appears next to conversation name (📌 emoji)
- [ ] Logs show "📌 Pin toggled: true/false"

## ✅ Delete Functionality  
- [ ] Tap delete icon → conversation disappears immediately
- [ ] Deleted conversation is removed from Firestore
- [ ] Cannot undo deletion
- [ ] Logs show "🗑️ Conversation deleted"
- [ ] Delete button not accessible if user not authenticated

## ✅ Unread Badge
- [ ] Badge appears as red circle with white text
- [ ] Shows unread count (e.g., "3", "9+")
- [ ] Badge only appears when `unreadCount > 0`
- [ ] Badge disappears when conversation is read
- [ ] Positioned in top-right of conversation item
- [ ] Logs show "🔴 Unread count: X"

## ✅ Search & Filtering
- [ ] Type tutor name → shows matching conversations
- [ ] Type part of name → finds matches (case-insensitive)
- [ ] Type message content → shows conversations with that message
- [ ] Search finds **all tutors** in group conversations (not just first one)
- [ ] Clear button (X) appears when typing
- [ ] Tapping clear button → shows all conversations again
- [ ] Empty state shown when no results match
- [ ] Search results update in real-time as you type
- [ ] Logs show "🔍 Filtered X / Y conversations"

## ✅ Real-Time Updates
- [ ] New conversations appear immediately
- [ ] Deleted conversations disappear immediately
- [ ] Message updates show in real-time
- [ ] Timestamp updates in real-time
- [ ] Pin/unpin updates in real-time
- [ ] No need to refresh or restart app
- [ ] Listener properly registered and not leaking memory

## ✅ UI Layout
- [ ] Main row: 72dp height with avatar, name, message, timestamp
- [ ] Action row: 44dp height with pin and delete buttons
- [ ] Avatar: 56dp, rounded with proper scaling
- [ ] Status icons (📌🔇) visible next to name when active
- [ ] Background uses theme color surface
- [ ] Proper spacing and padding throughout

## ✅ Error Handling
- [ ] Network error → toast message shown
- [ ] Permission denied → error logged
- [ ] Null data → fallback names used ("Conversation")
- [ ] Missing image → placeholder shown
- [ ] No crashes on edge cases

## ✅ Performance
- [ ] List scrolls smoothly with 100+ conversations
- [ ] Search responds instantly for 50+ characters typed
- [ ] No lag when tapping pin/delete buttons
- [ ] Memory usage stable over time
- [ ] No excessive database queries

## ✅ Logs
- [ ] Search "ConversationAdapter" in logcat → shows debug info
- [ ] Search "ChatListFragment" in logcat → shows fragment actions
- [ ] Search "ChatRepository" in logcat → shows database operations
- [ ] No error logs (❌ symbol) appearing
- [ ] Success messages (✅ or 📌 or 🗑️) appearing as expected

---

## Device Testing
- [ ] Tested on phone (device name: _______________)
- [ ] Tested on tablet (device name: _______________)
- [ ] Tested with varying network speeds (WiFi, 4G, 3G)
- [ ] Tested with low storage space
- [ ] Tested with dark mode enabled
- [ ] Tested with light mode enabled

## User Scenarios
- [ ] New user with no conversations
- [ ] User with 1 conversation
- [ ] User with 10+ conversations
- [ ] User with mixed group and one-on-one conversations
- [ ] User with many unread messages
- [ ] User receiving messages while viewing list
- [ ] User pinning/unpinning multiple conversations

---

**Test Date**: ___________  
**Tester Name**: ___________  
**Device**: ___________  
**Android Version**: ___________  

**Overall Status**: 
- [ ] ✅ All tests passed
- [ ] ⚠️ Some issues found (list below)
- [ ] ❌ Critical issues found (do not release)

**Issues Found** (if any):
```
1. 
2. 
3. 
```

**Notes**:
```


```
