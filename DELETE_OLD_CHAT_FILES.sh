#!/bin/bash
# ============================================
# DELETE OLD CHAT SYSTEM FILES
# Run this to clean up duplicate Activity-based chat system
# ============================================

echo "🗑️  Deleting old Activity-based chat system files..."

# Delete old Java files
rm -f /Users/user/AndroidStudioProjects/BookUp/app/src/main/java/com/example/bookup/activities/ChatListActivity.java
echo "✅ Deleted ChatListActivity.java"

rm -f /Users/user/AndroidStudioProjects/BookUp/app/src/main/java/com/example/bookup/activities/ChatActivity.java
echo "✅ Deleted ChatActivity.java"

rm -f /Users/user/AndroidStudioProjects/BookUp/app/src/main/java/com/example/bookup/adapters/ChatChannelAdapter.java
echo "✅ Deleted ChatChannelAdapter.java"

rm -f /Users/user/AndroidStudioProjects/BookUp/app/src/main/java/com/example/bookup/models/ChatChannel.java
echo "✅ Deleted ChatChannel.java"

# Delete old layout files
rm -f /Users/user/AndroidStudioProjects/BookUp/app/src/main/res/layout/activity_chat_list.xml
echo "✅ Deleted activity_chat_list.xml"

rm -f /Users/user/AndroidStudioProjects/BookUp/app/src/main/res/layout/activity_chat.xml
echo "✅ Deleted activity_chat.xml"

echo ""
echo "✅ OLD SYSTEM CLEANUP COMPLETE"
echo ""
echo "✅ Remaining unified chat system:"
echo "   - ChatListFragment (new, correct)"
echo "   - ChatFragment (new, correct)"
echo "   - ConversationAdapter (new, correct)"
echo "   - MessageAdapter (new, correct)"
echo "   - Conversation model (unified)"
echo "   - ChatRepository (points to chatChannels)"
echo ""
echo "🔄 Next: Run './gradlew clean build' to verify no errors"
