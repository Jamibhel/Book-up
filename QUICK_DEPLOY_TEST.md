# ⚡ QUICK DEPLOY & TEST COMMANDS

## One-Command Deploy

```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

After this runs successfully, you'll see: ✅ **Install successful**

---

## View Logs While Testing

```bash
adb logcat | grep -E "NewChatFragment|UserSelectionAdapter|ChatRepository"
```

Open this in a separate terminal **BEFORE** you test.

---

## Quick Test Flow

1. Open the app
2. Go to **Chat tab** (bottom navigation)
3. Click **blue FAB button** (+ icon at top)
4. Type in search: `ahmad`
5. Watch what happens
6. **Save the terminal logs**

---

## Critical Things to Check in Logs

Look for these exact log lines:

### ✅ Should Appear:
```
NewChatFragment: ✅ User authenticated
ChatRepository: ✅ Found [N] matching users
NewChatFragment: 🔄 Clearing old adapter data before submitting
UserSelectionAdapter: 🏗️ Creating ViewHolder
UserSelectionAdapter: 📍 onBindViewHolder called at position: 0
```

### ❌ If These Appear, We Have a Problem:
```
NewChatFragment: 🔴 CRITICAL: User is NOT authenticated!
ChatRepository: ⚠️ NO USERS FOUND
UserSelectionAdapter: (no ViewHolder logs)
NewChatFragment: 📏 RecyclerView dimensions - Width: 0, Height: 0
```

---

## What You'll See on Screen

### ✅ If It Works:
- Dialog opens
- Search box visible
- Filter chips visible (All, Students, Tutors)
- **User cards appear below chips** ← This is what we want!
  - Name: Ahmad Opeyemi
  - Email: ahmad@example.com
  - Role badge: Student/Tutor

### ❌ If It Doesn't Work:
- Dialog opens
- Search box visible
- Filter chips visible
- **Nothing below chips** ← This is the problem
- OR user cards have 0 height (invisible)

---

## Then Report Back With:

1. **Screenshot of dialog** (what it looks like)
2. **Logcat output** (terminal logs)
3. **Specific answers:**
   - Dialog opens? YES/NO
   - Search box visible? YES/NO
   - Chips visible? YES/NO
   - **User cards visible? YES/NO** ← Most important
   - Any error messages? YES/NO

---

## If Users Still Don't Show

We'll know from the logs which fix to apply:

| Log Pattern | Fix Needed |
|-----------|-----------|
| Height: 0 | Increase BottomSheet height |
| No ViewHolder logs | Check adapter setup |
| No bind logs | Check DiffUtil logic |
| Item count: 6 (not 2) | Verify submitList(null) works |

---

## Status: READY TO DEPLOY ✅

- Build: ✅ SUCCESSFUL
- Logging: ✅ COMPLETE
- Code: ✅ MODIFIED (submitList + logs)
- Docs: ✅ CREATED

**Next: RUN THE TEST!**
