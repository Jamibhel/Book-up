# Search Feature - Complete Debugging Guide

## Issue Summary
**Toast appears ✅ | Results don't show ❌**

## What Was Fixed

### The Real Problem
Firestore callbacks execute on a **background thread**, but the code wasn't catching exceptions that occurred when trying to update the UI on the main thread. This means:
- Exceptions were silently swallowed
- UI updates failed silently
- No error messages to debug

### The Solution
Added comprehensive exception handling:

#### searchUsers() Method
- **Line 339**: Outer try-catch for `requireActivity().runOnUiThread()` call
- **Line 343**: Inner try-catch for lambda body (actual UI updates)
- **Line 419-420**: Error logging with stack trace

#### loadAllUsers() Method  
- **Line 241**: Outer try-catch for `requireActivity().runOnUiThread()` call
- **Line 245**: Inner try-catch for lambda body
- **Line 283-286**: Error logging with stack trace

## Debug Workflow

### Step 1: Rebuild
```bash
# Clean and build
./gradlew clean build
```

### Step 2: Run on Device/Emulator
- Launch the app
- Navigate to Chat list
- Click "New Chat" button

### Step 3: Open Logcat
```bash
# Filter for NewChatFragment logs
adb logcat -s NewChatFragment
```

### Step 4: Perform Search
1. Type a user name
2. Press "Search" on keyboard

### Step 5: Check Logs
Expected log sequence:
```
🔍 Setting up search listener
✅ Search listener setup complete
🔍 Searching users for: 'john'
🧵 Search called on thread: main
🔄 Search callback received on thread: ...
📊 Search callback: users=3, error=null
✅ Found 3 matching users
  [0] John Doe (john@example.com)
  [1] John Smith (john.smith@example.com)
  [2] Jonah (jonah@example.com)
📝 Submitting search results to adapter...
✅ Adapter search results updated with 3 items
```

## Troubleshooting

### Scenario 1: Toast Shows, No Results, NO ERROR LOGS
**Problem**: Exception caught but not the cause shown

**Debug**: Check if:
1. Is `adapter` null?
   - Look for logs showing adapter state before submitList
2. Is `binding` null after UI thread switch?
   - Check: "⚠️ Binding is null after switching to main thread"
3. Are search results actually coming from Firestore?
   - Look for: "📊 Search callback: users=X"

### Scenario 2: Toast Shows, No Results, ERROR LOG SHOWS
**Problem**: Exception caught! You'll see:
```
💥 Error in search callback UI update: [exception message]
```

**Debug**: Look at the stack trace to identify:
- NullPointerException - likely binding or adapter issue
- IllegalStateException - fragment state problem
- Other - application specific error

### Scenario 3: Toast Doesn't Show
**Problem**: Search not being triggered

**Debug**: Check logs for:
- `🔍 Setting up search listener` - If missing, setupSearch() not called
- `🎯 EditorAction triggered` - If missing, EditorActionListener not firing
- `🔍 SEARCH SUBMITTED` - If missing, EditorInfo check failing

### Scenario 4: Search Works, But Partial Data
**Problem**: Results showing but incomplete

**Debug**: Check:
- RecyclerView dimensions post-submit
- Adapter itemcount after submitList
- Look for: "🔍 POST-SUBMIT RecyclerView state:"

## Common Issues & Fixes

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| Toast shows, blank results | `adapter.submitList()` exception | Check adapter is initialized |
| No toast | `setOnEditorActionListener` failed | Check EditText binding |
| Partial results | RecyclerView height 0 | Check layout XML heights |
| Crash after search | NullPointerException in callback | Fragment destroyed during search |
| Search results flicker | Multiple calls to `submitList` | Remove `setLifecycleOwner` if present |

## Code Locations

### File
`/app/src/main/java/com/example/bookup/fragments/NewChatFragment.java`

### Key Methods
- `onViewCreated()` - Line 69: Initial setup
- `setupSearch()` - Line 163: Search listener setup
- `searchUsers()` - Line 332: Search execution
- `loadAllUsers()` - Line 230: Initial load

### Key Lines with Try-Catch
- **Line 339**: Search outer try
- **Line 343**: Search inner try
- **Line 241**: Load outer try
- **Line 245**: Load inner try

## Expected Behavior After Fix

### Before Fix
```
User types "john" and presses Search
↓
Toast shows "Searching for: john" ✅
↓
[Silent exception in callback]
↓
No results shown ❌
No error message ❌
```

### After Fix
```
User types "john" and presses Search
↓
Toast shows "Searching for: john" ✅
↓
Results queried from Firestore
↓
Callback received on background thread
↓
runOnUiThread() called
↓
Results submitted to adapter ✅
↓
RecyclerView displays users ✅
OR
💥 Error logged if exception occurs
```

## Next Debugging Steps If Still Not Working

If results STILL don't show after rebuild:

1. **Verify ChatRepository.searchUsers()**
   - Check if it's querying Firestore at all
   - Add logs in ChatRepository.searchUsers() method
   - Verify callback is being invoked

2. **Verify Firestore Data**
   - Go to Firebase Console
   - Check if "users" collection exists
   - Verify users have displayName and email fields

3. **Verify Adapter**
   - Check if `UserSelectionAdapter.submitList()` calls `notifyDataSetChanged()`
   - Verify adapter's `onBindViewHolder()` is being called

4. **Verify Layout**
   - Check `fragment_new_chat.xml` for `recycler_users`
   - Verify RecyclerView has proper height (not 0dp)
   - Check `layout_empty_users` doesn't cover recycler

5. **Add More Logging**
   - In ChatRepository.searchUsers(): Log query execution
   - In UserSelectionAdapter: Log submitList calls
   - In onBindViewHolder(): Log item binding

## Testing Commands

### Run with filtered logs
```bash
adb logcat -s NewChatFragment | grep -E "🔍|✅|❌|💥"
```

### Check for exceptions
```bash
adb logcat -s NewChatFragment:E  # Errors only
```

### See thread names
```bash
adb logcat -s NewChatFragment | grep "thread:"
```

## Summary
The fix adds proper exception handling so any errors in the search callback will be caught and logged instead of silently failing. This makes debugging much easier going forward.

**Next step**: Rebuild and test, then check logcat output to see if search is working or if there are now visible errors to fix.
