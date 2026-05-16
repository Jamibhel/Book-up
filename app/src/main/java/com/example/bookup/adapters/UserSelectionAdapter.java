package com.example.bookup.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.databinding.ItemUserSelectionBinding;
import com.example.bookup.models.User;

/**
 * Adapter for displaying users in the new chat selection dialog.
 * Shows user profile picture, name, and email.
 */
public class UserSelectionAdapter extends ListAdapter<User, UserSelectionAdapter.UserViewHolder> {

    private final Context context;
    private OnUserClickListener userClickListener;

    public UserSelectionAdapter(Context context) {
        super(new UserDiffCallback());
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("UserSelectionAdapter", "🏗️ Creating ViewHolder");
        ItemUserSelectionBinding binding = ItemUserSelectionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        Log.d("UserSelectionAdapter", "✅ ViewHolder created and binding inflated");
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Log.d("UserSelectionAdapter", "📍 onBindViewHolder called at position: " + position);
        User user = getItem(position);
        if (user != null) {
            Log.d("UserSelectionAdapter", "✅ User found at position " + position + ": " + user.getDisplayName());
            holder.bind(user);
        } else {
            Log.w("UserSelectionAdapter", "⚠️ User is NULL at position " + position);
        }
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.userClickListener = listener;
        Log.d("UserSelectionAdapter", "🔌 OnUserClickListener set: " + (listener != null ? "✅ Active" : "❌ Null"));
    }

    /**
     * Clear the listener (for cleanup)
     */
    public void clearOnUserClickListener() {
        this.userClickListener = null;
        Log.d("UserSelectionAdapter", "🔌 OnUserClickListener cleared");
    }

    /**
     * ViewHolder for user selection items
     */
    public class UserViewHolder extends RecyclerView.ViewHolder {

        private final ItemUserSelectionBinding binding;

        public UserViewHolder(ItemUserSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            // Set click listener on the entire card
            binding.getRoot().setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    User user = getItem(position);
                    if (user != null) {
                        Log.d("UserSelectionAdapter", "👤 User card clicked: " + user.getDisplayName() + " (ID: " + user.getId() + ")");
                        if (userClickListener != null) {
                            userClickListener.onUserClick(user);
                        } else {
                            Log.w("UserSelectionAdapter", "⚠️ userClickListener is null!");
                        }
                    } else {
                        Log.w("UserSelectionAdapter", "⚠️ User object is null at position " + position);
                    }
                } else {
                    Log.w("UserSelectionAdapter", "⚠️ Invalid position detected");
                }
            });
        }

        public void bind(User user) {
            // Set user name
            binding.textUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Unknown User");

            // Set user email
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                binding.textUserEmail.setText(user.getEmail());
                binding.textUserEmail.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.textUserEmail.setVisibility(android.view.View.GONE);
            }

            // Set user role chip (optional - only if bio contains role info)
            String bio = user.getBio();
            if (bio != null && !bio.isEmpty()) {
                binding.chipUserRole.setText(bio);
                binding.chipUserRole.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.chipUserRole.setVisibility(android.view.View.GONE);
            }

            // Load profile picture
            String profileUrl = user.getPhotoUrl();
            if (profileUrl != null && !profileUrl.isEmpty()) {
                Glide.with(context)
                        .load(profileUrl)
                        .centerCrop()
                        .into(binding.imageUserProfile);
            } else {
                // Use placeholder if no profile picture
                Glide.with(context)
                        .load(android.R.drawable.ic_menu_gallery)
                        .centerCrop()
                        .into(binding.imageUserProfile);
            }
            
            Log.d("UserSelectionAdapter", "� Bound user: " + user.getDisplayName() + " (ID: " + user.getId() + ")");
        }
    }

    /**
     * DiffCallback for user list updates
     */
    private static class UserDiffCallback extends DiffUtil.ItemCallback<User> {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            // Use null-safe comparison
            String oldId = oldItem.getId();
            String newId = newItem.getId();
            
            boolean same = false;
            if (oldId != null && newId != null) {
                same = oldId.equals(newId);
            } else if (oldId == null && newId == null) {
                // Both null - compare by name as fallback
                String oldName = oldItem.getDisplayName();
                String newName = newItem.getDisplayName();
                same = (oldName != null && oldName.equals(newName));
            }
            
            Log.d("UserSelectionAdapter", "areItemsTheSame: " + (oldItem.getDisplayName() != null ? oldItem.getDisplayName() : "null") + 
                  " vs " + (newItem.getDisplayName() != null ? newItem.getDisplayName() : "null") + 
                  " = " + same);
            return same;
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            // Compare display names with null checks
            String oldName = oldItem.getDisplayName();
            String newName = newItem.getDisplayName();
            boolean namesEqual = (oldName == null && newName == null) || 
                                 (oldName != null && oldName.equals(newName));
            
            // Compare emails with null checks
            String oldEmail = oldItem.getEmail();
            String newEmail = newItem.getEmail();
            boolean emailsEqual = (oldEmail == null && newEmail == null) || 
                                  (oldEmail != null && oldEmail.equals(newEmail));
            
            // Compare photo URLs with null checks
            String oldPhoto = oldItem.getPhotoUrl();
            String newPhoto = newItem.getPhotoUrl();
            boolean photosEqual = (oldPhoto == null && newPhoto == null) || 
                                  (oldPhoto != null && oldPhoto.equals(newPhoto));
            
            boolean same = namesEqual && emailsEqual && photosEqual;
            Log.d("UserSelectionAdapter", "areContentsTheSame: " + same);
            return same;
        }
    }

    /**
     * Callback interface for user selection
     */
    public interface OnUserClickListener {
        void onUserClick(User user);
    }
}
