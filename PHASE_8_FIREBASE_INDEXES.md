# Required Firestore Indexes for BookUp

To resolve the "Error loading feed" and other query issues, you must create the following composite indexes in your Firebase Console.

## 1. News Feed Index
Used for the Campus Community and Dashboard.
- **Collection**: `newsFeed`
- **Fields**:
  - `priority` (Descending)
  - `timestamp` (Descending)

## 2. Help Requests Index
Used for the Requests screen.
- **Collection**: `helpRequests`
- **Fields**:
  - `status` (Ascending)
  - `timestamp` (Descending)

## 3. Conversations Index (Chat)
Used for sorting the chat list by the latest message.
- **Collection**: `conversations`
- **Fields**:
  - `participantIds` (Arrays)
  - `lastMessageTimestamp` (Descending)

## How to create these:
1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Go to **Firestore Database** -> **Indexes** tab.
3. Click **Add Index**.
4. Enter the collection and fields as specified above.
5. Alternatively, check your **Logcat** in Android Studio while running the app. If a query fails due to a missing index, Firestore will provide a direct link in the logs that you can click to create the index automatically.
