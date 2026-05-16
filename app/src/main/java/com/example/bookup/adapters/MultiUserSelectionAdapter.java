package com.example.bookup.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.databinding.ItemUserSelectionBinding;
import com.example.bookup.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiUserSelectionAdapter extends RecyclerView.Adapter<MultiUserSelectionAdapter.ViewHolder> {
    private List<User> users = new ArrayList<>();
    private final Set<User> selectedUsers = new HashSet<>();

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public List<User> getSelectedUsers() {
        return new ArrayList<>(selectedUsers);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserSelectionBinding binding = ItemUserSelectionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserSelectionBinding binding;

        ViewHolder(ItemUserSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.textUserName.setText(user.getDisplayName());
            binding.textUserEmail.setText(user.getEmail());
            
            Glide.with(binding.imageUserProfile)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(binding.imageUserProfile);

            updateSelectionUI(user);

            itemView.setOnClickListener(v -> {
                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user);
                } else {
                    selectedUsers.add(user);
                }
                updateSelectionUI(user);
            });
        }

        private void updateSelectionUI(User user) {
            if (selectedUsers.contains(user)) {
                binding.getRoot().setAlpha(0.5f);
                binding.chipUserRole.setVisibility(android.view.View.VISIBLE);
                binding.chipUserRole.setText("Selected");
            } else {
                binding.getRoot().setAlpha(1.0f);
                binding.chipUserRole.setVisibility(android.view.View.GONE);
            }
        }
    }
}
