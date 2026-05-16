package com.example.bookup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.databinding.ItemGroupParticipantBinding;
import com.example.bookup.models.UserSearchItem;

import java.util.ArrayList;
import java.util.List;

public class GroupParticipantAdapter extends RecyclerView.Adapter<GroupParticipantAdapter.ViewHolder> {

    private List<UserSearchItem> participants = new ArrayList<>();
    private String adminId;
    private final String currentUserId;
    private final OnParticipantActionListener listener;

    public interface OnParticipantActionListener {
        void onRemoveClick(String userId);
    }

    public GroupParticipantAdapter(String currentUserId, OnParticipantActionListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setParticipants(List<UserSearchItem> participants, String adminId) {
        this.participants = participants;
        this.adminId = adminId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemGroupParticipantBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserSearchItem user = participants.get(position);
        boolean isAdmin = user.getUserId().equals(adminId);
        boolean isMe = user.getUserId().equals(currentUserId);
        boolean iAmAdmin = currentUserId != null && currentUserId.equals(adminId);

        holder.binding.textUserName.setText(user.getDisplayName() + (isMe ? " (You)" : ""));
        
        if (isAdmin) {
            holder.binding.textUserRole.setVisibility(View.VISIBLE);
            holder.binding.textUserRole.setText("Group Admin");
        } else {
            holder.binding.textUserRole.setVisibility(View.GONE);
        }

        Glide.with(holder.itemView)
                .load(user.getPhotoUrl())
                .placeholder(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(holder.binding.imageUserPhoto);

        // Show remove button only if I am admin and this user is not me and not the admin
        if (iAmAdmin && !isMe) {
            holder.binding.btnRemove.setVisibility(View.VISIBLE);
            holder.binding.btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onRemoveClick(user.getUserId());
            });
        } else {
            holder.binding.btnRemove.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemGroupParticipantBinding binding;
        ViewHolder(ItemGroupParticipantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
