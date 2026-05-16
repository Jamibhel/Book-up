# 🎯 Visual Architecture: User Search in NewChatFragment

## Complete User Journey When Starting a New Chat

```
┌─────────────────────────────────────────────────────────────────┐
│ USER CLICKS "START NEW CHAT" FAB IN ChatListFragment            │
└─────────────────────────────────────────┬───────────────────────┘
                                          │
                                          ▼
┌─────────────────────────────────────────────────────────────────┐
│ NewChatFragment opens as BottomSheetDialog                      │
│                                                                  │
│ ┌──────────────────────────────────────────────────────────────┐│
│ │ Dialog Title: "Select User to Chat"                          ││
│ │                                                               ││
│ │ [Search Field: Type name or email...]                        ││
│ │                                                               ││
│ │ [All] [Students] [Tutors]  ← Chip filters                   ││
│ │                                                               ││
│ │ ┌─────────────────────────────────────────────────────────┐ ││
│ │ │ RecyclerView (UserSelectionAdapter)                    │ ││
│ │ │                                                         │ ││
│ │ │ ┌─────────────────────────────────────────────────────┐│ ││
│ │ │ │ [👤] John Doe              john@example.com        ││ ││
│ │ │ │ ┌──────────────────────────────────────────────────┐││ ││
│ │ │ │ │ RIPPLE EFFECT ON CLICK                           │││ ││
│ │ │ │ │ User selected listener fires                     │││ ││
│ │ │ │ └──────────────────────────────────────────────────┘││ ││
│ │ │ └─────────────────────────────────────────────────────┘│ ││
│ │ │                                                         │ ││
│ │ │ ┌─────────────────────────────────────────────────────┐│ ││
│ │ │ │ [👤] Jane Smith            jane@example.com        ││ ││
│ │ │ │ (Tap to select)                                    ││ ││
│ │ │ └─────────────────────────────────────────────────────┘│ ││
│ │ │                                                         │ ││
│ │ └─────────────────────────────────────────────────────────┘ ││
│ └──────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────┬───────────────────────┘
                                          │
                ┌─────────────────────────┴────────────────────────┐
                │                                                   │
                ▼                                                   ▼
    [User types in search]          [User taps a user]
    │                                       │
    │ onTextChanged() fires                 │
    │ (NO debounce - immediate)             │
    │                                       │
    ▼                                       │
    chatRepository.searchUsers(query)       │
    │                                       │
    ├─→ db.collection("users").get()        │
    │   │                                   │
    │   └─→ Fetches ALL users               │
    │       │                               │
    │       └─→ Client-side filter:         │
    │           if (displayName.contains    │
    │               or email.contains)      │
    │           then add to results         │
    │                                       │
    └─→ Returns list of matching users      │
        │                                   │
        ▼                                   │
    adapter.submitList(users)               │
    │                                       │
    └─→ DiffUtil compares:                  │
        - Old list vs New list              │
        - Animates insertions/removals      │
        - Updates only changed items        │
                                            │
                                            ▼
                                    onUserSelectedForNewChat(User)
                                            │
                                            ├─→ Check existing conversation
                                            │   │
                                            │   ├─→ If exists: Open it
                                            │   │
                                            │   └─→ If new: Create it
                                            │
                                            └─→ ChatActivity.startChat()
                                                │
                                                ▼
                                         ChatActivity opens
                                         Chat view shows selected user
```

---

## 🔄 Data Flow Diagram: Search to Display

```
┌─────────────────────────────────────────────────────────────────┐
│                    FIRESTORE USERS COLLECTION                    │
│                                                                  │
│  Document: "uid1"              Document: "uid2"                 │
│  ├─ firstName: "John"          ├─ firstName: "Jane"             │
│  ├─ lastName: "Doe"            ├─ lastName: "Smith"             │
│  ├─ email: "john@ex.com"       ├─ email: "jane@ex.com"          │
│  ├─ photoUrl: "..."            ├─ photoUrl: "..."               │
│  └─ ...                        └─ ...                           │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
        ┌────────────────────────────────────┐
        │ chatRepository.searchUsers(query) │
        │                                    │
        │ 1. db.collection("users").get()   │
        │    └─ Fetches ALL documents       │
        │                                    │
        │ 2. For each user:                 │
        │    - Deserialize to User model    │
        │    - Set User.id from doc ID      │
        │    - Get displayName via getter   │
        │      (combines firstName+lastName)│
        │    - Get email                    │
        │                                    │
        │ 3. Client-side filter:            │
        │    if (displayName.contains(query)│
        │        || email.contains(query))  │
        │    then add to results list       │
        │                                    │
        │ 4. Return filtered list           │
        └────────────────────────────────────┘
                             │
                             ▼
        ┌────────────────────────────────────┐
        │   NewChatFragment receives list   │
        │                                    │
        │   adapter.submitList(users)       │
        └────────────────────────────────────┘
                             │
                             ▼
        ┌────────────────────────────────────┐
        │   UserSelectionAdapter             │
        │                                    │
        │   1. DiffUtil.ItemCallback:        │
        │      areItemsTheSame()  ← Uses ID │
        │      areContentsTheSame() ← name  │
        │                                    │
        │   2. onBindViewHolder():           │
        │      - Set textUserName            │
        │      - Set textUserEmail           │
        │      - Load profile picture        │
        │      - Set click listener          │
        │                                    │
        │   3. Updates RecyclerView          │
        └────────────────────────────────────┘
                             │
                             ▼
        ┌────────────────────────────────────┐
        │         USER INTERFACE              │
        │                                    │
        │  ┌──────────────────────────────┐ │
        │  │ [👤] John Doe                │ │
        │  │      john@ex.com             │ │
        │  │      ↓ CLICK                 │ │
        │  └──────────────────────────────┘ │
        │                                    │
        │  ┌──────────────────────────────┐ │
        │  │ [👤] Jane Smith              │ │
        │  │      jane@ex.com             │ │
        │  │      ↓ CLICK                 │ │
        │  └──────────────────────────────┘ │
        └────────────────────────────────────┘
                             │
                             ▼ User clicks a user
        ┌────────────────────────────────────┐
        │  UserSelectionAdapter.ViewHolder   │
        │                                    │
        │  binding.getRoot()                 │
        │    .setOnClickListener(v -> {      │
        │      int pos = getBindingAdapter   │
        │                    Position()      │
        │      User user = getItem(pos)      │
        │                                    │
        │      userClickListener             │
        │        .onUserClick(user)          │
        │    })                              │
        └────────────────────────────────────┘
                             │
                             ▼
        ┌────────────────────────────────────┐
        │    NewChatFragment.onViewCreated   │
        │                                    │
        │    adapter.setOnUserClickListener( │
        │      (user) -> {                   │
        │        userSelectedListener        │
        │          .onUserSelected(user)     │
        │        dismiss()                   │
        │      }                             │
        │    )                               │
        └────────────────────────────────────┘
                             │
                             ▼
        ┌────────────────────────────────────┐
        │   ChatListFragment                 │
        │                                    │
        │   onUserSelectedForNewChat(user) { │
        │     // Check if conversation       │
        │     // already exists              │
        │                                    │
        │     chatRepository                 │
        │       .checkExistingConversation() │
        │       .then({                      │
        │         if (exists) {              │
        │           open existing chat       │
        │         } else {                   │
        │           createNewConversation()  │
        │           launch ChatActivity      │
        │         }                          │
        │       })                           │
        │   }                                │
        └────────────────────────────────────┘
                             │
                             ▼
        ┌────────────────────────────────────┐
        │        ChatActivity                │
        │                                    │
        │   Conversation with John Doe      │
        │                                    │
        │  ┌──────────────────────────────┐ │
        │  │ [Send message...]            │ │
        │  └──────────────────────────────┘ │
        └────────────────────────────────────┘
```

---

## 📱 Class Relationships

```
ChatListFragment
  │
  ├─→ clicks FAB Button
  │   │
  │   ▼
  ├─→ shows NewChatFragment (BottomSheet)
  │   │
  │   ▼
  │   NewChatFragment
  │   ├─ chatRepository: ChatRepository
  │   ├─ adapter: UserSelectionAdapter
  │   ├─ userSelectedListener: OnUserSelectedListener
  │   │
  │   ├─ setupSearch()
  │   │   └─ TextWatcher on editSearchUsers
  │   │
  │   ├─ searchUsers(query)
  │   │   └─ chatRepository.searchUsers(query, callback)
  │   │
  │   ├─ loadAllUsers()
  │   │   └─ chatRepository.getAllUsers(callback)
  │   │
  │   └─ setupChipFilters()
  │       ├─ chipAll
  │       ├─ chipStudents (TODO)
  │       └─ chipTutors (TODO)
  │
  │   UserSelectionAdapter
  │   ├─ setOnUserClickListener(listener)
  │   │
  │   └─ UserViewHolder
  │       ├─ bind(user)
  │       │   ├─ Set display name
  │       │   ├─ Set email
  │       │   ├─ Load profile picture
  │       │   └─ Log binding info
  │       │
  │       └─ Constructor
  │           └─ Set click listener on card
  │
  │   User (model)
  │   ├─ id, firstName, lastName
  │   ├─ email, photoUrl, bio
  │   │
  │   └─ getDisplayName()
  │       └─ Returns firstName + lastName
  │           (or displayName if set)
  │
  ▼ User selects a user
  
  onUserSelectedForNewChat(user)
  ├─ Check existing conversation
  │   └─ chatRepository.checkExistingConversation()
  │
  └─ If new: createNewConversation(user)
      └─ ChatActivity.startChat()
```

---

## 🔍 Search Method Comparison

### NewChatFragment.searchUsers():
```
┌─────────────────────────────────────┐
│ setupSearch()                       │
│ └─ TextWatcher                      │
│    └─ onTextChanged(CharSequence)   │
│       └─ searchUsers(query)         │
│          │                          │
│          ├─ If query.isEmpty():     │
│          │  └─ loadAllUsers()       │
│          │                          │
│          └─ Else:                   │
│             └─ chatRepository       │
│                .searchUsers(query)  │
│                └─ db.collection     │
│                   ("users")         │
│                   .get()            │
│                   └─ Client-side    │
│                      filter         │
│                      (contains)     │
│                   └─ Return list    │
│                      │              │
│                      └─→ adapter    │
│                         .submitList │
│                         (users)     │
│                         └─ UI       │
│                            updates  │
└─────────────────────────────────────┘
```

### SearchFragment.performSearch():
```
┌─────────────────────────────────────┐
│ setupSearchView()                   │
│ └─ TextWatcher                      │
│    └─ onTextChanged(CharSequence)   │
│       └─ searchHandler              │
│          .postDelayed()             │
│          .removeCallbacks()  ← Can  │
│                               cancel│
│          .performSearch()           │
│          (500ms delay)              │
│          │                          │
│          ├─ searchMaterials()       │
│          │  └─ db.collection       │
│          │     ("materials")        │
│          │     .whereGreater...     │
│          │     .limit(PAGE_SIZE)    │
│          │     .get()               │
│          │     └─ Client-side       │
│          │        filtering         │
│          │                          │
│          ├─ searchTutors()          │
│          │  └─ db.collection       │
│          │     ("users")            │
│          │     .where(               │
│          │      "isTutor", ==, true)│
│          │     .limit()             │
│          │     .get()               │
│          │     └─ Client-side       │
│          │        filter by         │
│          │        firstName         │
│          │        .startsWith()     │
│          │                          │
│          └─ searchStudents()        │
│             └─ db.collection       │
│                ("users")            │
│                .where(              │
│                 "isTutor", !=, true)│
│                .limit()             │
│                .get()               │
│                └─ Client-side       │
│                   filter by         │
│                   firstName         │
│                   .startsWith()     │
└─────────────────────────────────────┘
```

---

## 💾 Storage & Retrieval

### User Data in Firestore:
```
Collection: "users"
├─ Document ID: "uid1" (Firebase Auth UID)
│  ├─ firstName: "John"
│  ├─ lastName: "Doe"
│  ├─ email: "john@example.com"
│  ├─ photoUrl: "https://..."
│  ├─ bio: "Software Engineer"
│  ├─ isTutor: true/false
│  ├─ blocked: false
│  └─ fcmToken: "..."
│
└─ Document ID: "uid2"
   ├─ firstName: "Jane"
   ├─ lastName: "Smith"
   ├─ email: "jane@example.com"
   └─ ...
```

### User Model Deserialization:
```
Firestore Document          User Java Object
│                           │
├─ firstName: "John"    →   ├─ firstName: "John"
├─ lastName: "Doe"      →   ├─ lastName: "Doe"
├─ email: "..."         →   ├─ email: "..."
├─ photoUrl: "..."      →   ├─ photoUrl: "..."
└─ ... (other fields)       └─ ... (other fields)

When getDisplayName() is called:
└─ Combines firstName + lastName
   └─ Returns: "John Doe"
```

---

## 🎯 Summary

**NewChatFragment Search** is a **simple, direct, real-time** search:
- User types → Immediate search
- No debounce → Instant feedback
- Client-side filtering → Easy to understand
- Best for: Finding people to chat with

**SearchFragment** is a **sophisticated, debounced, multi-collection** search:
- User types → 500ms delay before search
- Server-side + Client-side filtering → Efficient
- Multiple collections → Search materials, tutors, students
- Best for: General discovery of content and people

