package com.example.bookup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.models.UserSearchItem;

public class UserSearchAdapter extends ListAdapter<UserSearchItem, UserSearchAdapter.UserViewHolder> {
    
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(UserSearchItem user);
    }

    public UserSearchAdapter() {
        super(new DiffUtil.ItemCallback<UserSearchItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull UserSearchItem oldItem, @NonNull UserSearchItem newItem) {
                return oldItem.getUserId().equals(newItem.getUserId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull UserSearchItem oldItem, @NonNull UserSearchItem newItem) {
                return oldItem.getDisplayName().equals(newItem.getDisplayName()) &&
                       oldItem.getPhotoUrl().equals(newItem.getPhotoUrl());
            }
        });
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserSearchItem user = getItem(position);
        holder.bind(user, onUserClickListener);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewUserPhoto;
        private final TextView textViewUserName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewUserPhoto = itemView.findViewById(R.id.image_user_photo);
            textViewUserName = itemView.findViewById(R.id.text_user_name);
        }

        public void bind(UserSearchItem user, OnUserClickListener listener) {
            textViewUserName.setText(user.getDisplayName());
            
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_black_24dp)
                        .error(R.drawable.ic_profile_black_24dp)
                        .into(imageViewUserPhoto);
            } else {
                imageViewUserPhoto.setImageResource(R.drawable.ic_profile_black_24dp);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
        }
    }
}