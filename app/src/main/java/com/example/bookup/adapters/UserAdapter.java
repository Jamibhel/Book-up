package com.example.bookup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.models.User;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.textUserName.setText(user.getDisplayName());
        holder.textUserEmail.setText(user.getEmail());
        
        // Set status indicators
        holder.textUserStatus.setText(user.isBlocked() ? "Blocked" : (user.isAdmin() ? "Admin" : "Active"));
        holder.textUserStatus.setTextColor(holder.itemView.getContext().getColor(
            user.isBlocked() ? R.color.error : (user.isAdmin() ? R.color.primary : R.color.success)
        ));

        // Load user photo if available
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(holder.imageUserPhoto.getContext())
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .into(holder.imageUserPhoto);
        } else {
            holder.imageUserPhoto.setImageResource(R.drawable.ic_person_24dp);
        }

        holder.cardUser.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardUser;
        ImageView imageUserPhoto;
        TextView textUserName;
        TextView textUserEmail;
        TextView textUserStatus;

        UserViewHolder(View itemView) {
            super(itemView);
            cardUser = itemView.findViewById(R.id.card_user);
            imageUserPhoto = itemView.findViewById(R.id.image_user_photo);
            textUserName = itemView.findViewById(R.id.text_user_name);
            textUserEmail = itemView.findViewById(R.id.text_user_email);
            textUserStatus = itemView.findViewById(R.id.text_user_status);
        }
    }
}