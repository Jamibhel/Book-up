# New Chat Feature - Code Snippets Reference

## How to Use the New Chat Feature

### 1. Launch New Chat Dialog

```java
// In any Activity or Fragment
NewChatFragment dialog = new NewChatFragment();
dialog.setOnUserSelectedListener(user -> {
    // Handle user selection
    Log.d("TAG", "Selected: " + user.getDisplayName());
});
dialog.show(getSupportFragmentManager(), "new_chat");
```

### 2. Search Users in Repository

```java
ChatRepository repo = new ChatRepository();
repo.searchUsers("john", (users, error) -> {
    if (error != null) {
        Log.e("TAG", "Search error: " + error);
        return;
    }
    if (users != null) {
        for (User user : users) {
            Log.d("TAG", "Found: " + user.getDisplayName());
        }
    }
});
```

### 3. Get All Users

```java
ChatRepository repo = new ChatRepository();
repo.getAllUsers((users, error) -> {
    if (users != null) {
        adapter.submitList(users);
    }
});
```

### 4. Check Existing Conversation

```java
ChatRepository repo = new ChatRepository();
String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
String otherUserId = selectedUser.getId();

repo.checkExistingConversation(currentUserId, otherUserId, conversationId -> {
    if (conversationId != null) {
        // Open existing conversation
        ChatActivity.startChat(context, conversationId, 
                              selectedUser.getDisplayName(), 
                              selectedUser.getId());
    } else {
        // Create new conversation
        createNewConversation(selectedUser);
    }
});
```

---

## Key Classes Overview

### NewChatFragment.java

**Import Statements:**
```java
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.bookup.adapters.UserSelectionAdapter;
import com.example.bookup.databinding.FragmentNewChatBinding;
import com.example.bookup.models.User;
import com.example.bookup.repositories.ChatRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
```

**Key Methods:**

```java
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    // Initialize
    chatRepository = new ChatRepository();
    adapter = new UserSelectionAdapter(requireContext());
    
    // Setup RecyclerView
    binding.recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.recyclerUsers.setAdapter(adapter);
    
    // Setup search
    setupSearch();
    
    // Load users
    loadAllUsers();
}

private void setupSearch() {
    binding.editSearchUsers.addTextChangedListener(new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String query = s.toString().trim();
            if (query.isEmpty()) {
                loadAllUsers();
            } else {
                searchUsers(query);
            }
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void afterTextChanged(Editable s) {}
    });
}

private void loadAllUsers() {
    chatRepository.getAllUsers((users, error) -> {
        if (error != null) {
            showEmptyState(true);
            return;
        }
        if (users != null && !users.isEmpty()) {
            adapter.submitList(users);
            binding.layoutEmptyUsers.setVisibility(View.GONE);
        } else {
            showEmptyState(true);
        }
    });
}

private void searchUsers(String query) {
    chatRepository.searchUsers(query, (users, error) -> {
        if (users != null && !users.isEmpty()) {
            adapter.submitList(users);
            binding.layoutEmptyUsers.setVisibility(View.GONE);
        } else {
            showEmptyState(true);
        }
    });
}
```

---

### UserSelectionAdapter.java

**Binding in ViewHolder:**

```java
public class UserViewHolder extends RecyclerView.ViewHolder {
    private final ItemUserSelectionBinding binding;
    
    public UserViewHolder(ItemUserSelectionBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
    
    public void bind(User user) {
        // Set name
        binding.textUserName.setText(
            user.getDisplayName() != null ? 
            user.getDisplayName() : 
            "Unknown User"
        );
        
        // Set email
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            binding.textUserEmail.setText(user.getEmail());
            binding.textUserEmail.setVisibility(View.VISIBLE);
        } else {
            binding.textUserEmail.setVisibility(View.GONE);
        }
        
        // Load profile picture
        String photoUrl = user.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(context)
                .load(photoUrl)
                .centerCrop()
                .into(binding.imageUserProfile);
        } else {
            Glide.with(context)
                .load(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(binding.imageUserProfile);
        }
        
        // Click listener
        binding.getRoot().setOnClickListener(v -> {
            if (userClickListener != null) {
                userClickListener.onUserClick(user);
            }
        });
    }
}
```

---

### ChatRepository.java

**New Methods:**

```java
public interface OnUsersFoundListener {
    void onUsersFound(List<User> users, String error);
}

public interface OnConversationCheckListener {
    void onResult(String conversationId);
}

public void searchUsers(String query, OnUsersFoundListener listener) {
    Log.d(TAG, "🔍 Searching users for: " + query);
    
    query = query.toLowerCase().trim();
    final String finalQuery = query;
    
    db.collection("users")
        .get()
        .addOnSuccessListener(querySnapshot -> {
            List<User> users = new ArrayList<>();
            
            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                for (int i = 0; i < querySnapshot.getDocuments().size(); i++) {
                    try {
                        User user = querySnapshot.getDocuments()
                            .get(i).toObject(User.class);
                        
                        if (user != null) {
                            String displayName = user.getDisplayName() != null ? 
                                user.getDisplayName().toLowerCase() : "";
                            String email = user.getEmail() != null ? 
                                user.getEmail().toLowerCase() : "";
                            
                            if (displayName.contains(finalQuery) || 
                                email.contains(finalQuery)) {
                                users.add(user);
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "⚠️ Error parsing user: " + e.getMessage());
                    }
                }
            }
            
            listener.onUsersFound(users, null);
        })
        .addOnFailureListener(e -> {
            Log.e(TAG, "❌ Error searching users: " + e.getMessage());
            listener.onUsersFound(null, e.getMessage());
        });
}

public void getAllUsers(OnUsersFoundListener listener) {
    Log.d(TAG, "📋 Getting all users");
    
    db.collection("users")
        .get()
        .addOnSuccessListener(querySnapshot -> {
            List<User> users = new ArrayList<>();
            
            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                for (int i = 0; i < querySnapshot.getDocuments().size(); i++) {
                    try {
                        User user = querySnapshot.getDocuments()
                            .get(i).toObject(User.class);
                        
                        if (user != null) {
                            users.add(user);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "⚠️ Error parsing user: " + e.getMessage());
                    }
                }
            }
            
            listener.onUsersFound(users, null);
        })
        .addOnFailureListener(e -> {
            Log.e(TAG, "❌ Error loading users: " + e.getMessage());
            listener.onUsersFound(null, e.getMessage());
        });
}

public void checkExistingConversation(String userId1, String userId2, 
                                      OnConversationCheckListener listener) {
    Log.d(TAG, "🔍 Checking existing conversation");
    
    db.collection("chatChannels")
        .whereArrayContains("participantIds", userId1)
        .get()
        .addOnSuccessListener(querySnapshot -> {
            String foundConversationId = null;
            
            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                for (int i = 0; i < querySnapshot.getDocuments().size(); i++) {
                    Conversation conv = querySnapshot.getDocuments()
                        .get(i).toObject(Conversation.class);
                    if (conv != null && conv.getParticipantIds() != null &&
                        conv.getParticipantIds().contains(userId2)) {
                        foundConversationId = conv.getConversationId();
                        break;
                    }
                }
            }
            
            listener.onResult(foundConversationId);
        })
        .addOnFailureListener(e -> {
            Log.e(TAG, "❌ Error checking conversation: " + e.getMessage());
            listener.onResult(null);
        });
}
```

---

### ChatListFragment.java

**FAB Integration:**

```java
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    // ... existing code ...
    
    // Setup FAB for new chat
    binding.fabNewChat.setOnClickListener(v -> showNewChatDialog());
}

private void showNewChatDialog() {
    Log.d("ChatListFragment", "💬 Showing new chat dialog");
    NewChatFragment dialog = new NewChatFragment();
    dialog.setOnUserSelectedListener(user -> onUserSelectedForNewChat(user));
    dialog.show(getChildFragmentManager(), "new_chat");
}

private void onUserSelectedForNewChat(User user) {
    Log.d("ChatListFragment", "👤 User selected: " + user.getDisplayName());
    
    // Check if conversation exists
    chatRepository.checkExistingConversation(currentUserId, user.getId(), 
        existingConversationId -> {
        if (existingConversationId != null) {
            // Open existing
            ChatActivity.startChat(
                requireContext(),
                existingConversationId,
                user.getDisplayName(),
                user.getId()
            );
        } else {
            // Create new
            createNewConversation(user);
        }
    });
}

private void createNewConversation(User otherUser) {
    Log.d("ChatListFragment", "🆕 Creating new conversation");
    
    String conversationId = currentUserId.compareTo(otherUser.getId()) < 0 
        ? currentUserId + "_" + otherUser.getId()
        : otherUser.getId() + "_" + currentUserId;
    
    Map<String, Object> conversation = new HashMap<>();
    conversation.put("conversationId", conversationId);
    conversation.put("participantIds", Arrays.asList(currentUserId, otherUser.getId()));
    conversation.put("conversationName", otherUser.getDisplayName());
    conversation.put("conversationImage", otherUser.getPhotoUrl());
    conversation.put("lastMessage", "");
    conversation.put("lastMessageTimestamp", new Date());
    conversation.put("unreadCount", 0);
    conversation.put("createdAt", new Date());
    
    FirebaseFirestore.getInstance()
        .collection("chatChannels")
        .document(conversationId)
        .set(conversation)
        .addOnSuccessListener(aVoid -> {
            Log.d("ChatListFragment", "✅ Conversation created: " + conversationId);
            ChatActivity.startChat(
                requireContext(),
                conversationId,
                otherUser.getDisplayName(),
                otherUser.getId()
            );
        })
        .addOnFailureListener(e -> {
            Log.e("ChatListFragment", "❌ Error creating conversation: " + e.getMessage());
            Toast.makeText(requireContext(), "Error creating conversation", 
                Toast.LENGTH_SHORT).show();
        });
}
```

---

## XML Layout References

### fragment_new_chat.xml - Key Views

```xml
<!-- Search Input -->
<EditText
    android:id="@+id/edit_search_users"
    android:layout_width="0dp"
    android:layout_height="44dp"
    android:hint="@string/hint_search_user"
    android:inputType="text" />

<!-- User List -->
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recycler_users"
    android:layout_width="0dp"
    android:layout_height="0dp"
    tools:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />

<!-- Empty State -->
<LinearLayout
    android:id="@+id/layout_empty_users"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:visibility="gone">
    <ImageView android:id="@+id/image_empty" />
    <TextView android:id="@+id/text_empty" />
</LinearLayout>
```

### item_user_selection.xml - Key Views

```xml
<!-- Profile Picture -->
<com.google.android.material.shape.ShapeableImageView
    android:id="@+id/image_user_profile"
    android:layout_width="48dp"
    android:layout_height="48dp"
    app:shapeAppearanceOverlay="@style/ShapeAppearance.Material3.Corner.Full" />

<!-- User Name -->
<TextView
    android:id="@+id/text_user_name"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:textAppearance="@style/TextAppearance.BookUp.TitleMedium"
    android:textStyle="bold" />

<!-- User Email -->
<TextView
    android:id="@+id/text_user_email"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:textAppearance="@style/TextAppearance.BookUp.BodySmall" />

<!-- Role Chip -->
<com.google.android.material.chip.Chip
    android:id="@+id/chip_user_role"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

---

## Firestore Schema

### users collection

```json
{
    "userId": {
        "displayName": "John Doe",
        "email": "john@example.com",
        "photoUrl": "https://storage.googleapis.com/.../photo.jpg",
        "bio": "Math Tutor",
        "isAdmin": false,
        "blocked": false,
        "fcmToken": "device_token_here"
    }
}
```

### chatChannels collection

```json
{
    "userId1_userId2": {
        "conversationId": "userId1_userId2",
        "participantIds": ["userId1", "userId2"],
        "conversationName": "John Doe",
        "conversationImage": "https://...",
        "lastMessage": "Last message text",
        "lastMessageTimestamp": "2025-12-25T10:30:00Z",
        "unreadCount": 0,
        "createdAt": "2025-12-25T10:00:00Z"
    }
}
```

---

## String Resources

```xml
<string name="hint_search_user">Search by name or email</string>
<string name="no_users">No users found</string>
<string name="new_chat_dialog_title">Start New Chat</string>
<string name="search_messages">Search Messages</string>
<string name="online_status">Online</string>
<string name="chat_list_title">Chats</string>
```

---

**Last Updated:** December 25, 2025
**Version:** 1.0
