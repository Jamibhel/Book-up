package com.example.bookup;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.models.ChatChannel;
import com.google.android.material.card.MaterialCardView; // NEW
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ChatChannelAdapter extends RecyclerView.Adapter<ChatChannelAdapter.ChatChannelViewHolder> {

    private List<ChatChannel> chatChannels;
    private OnChatChannelClickListener listener;
    private String currentUserId;

    public interface OnChatChannelClickListener {
        void onChatChannelClick(ChatChannel channel);
    }

    public void setOnChatChannelClickListener(OnChatChannelClickListener listener) {
        this.listener = listener;
    }

    public ChatChannelAdapter(List<ChatChannel> chatChannels) {
        this.chatChannels = chatChannels;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            this.currentUserId = user.getUid();
        } else {
            this.currentUserId = ""; // Should not happen if user is authenticated to view chats
        }
    }

    @NonNull
    @Override
    public ChatChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_channel, parent, false);
        return new ChatChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatChannelViewHolder holder, int position) {
        ChatChannel currentChannel = chatChannels.get(position);

        // Determine the other participant's name
        String otherParticipantName = currentChannel.getOtherParticipantName(currentUserId);
        holder.textChatParticipantName.setText(otherParticipantName != null ? otherParticipantName : "Group Chat");

        holder.textChatLastMessage.setText(currentChannel.getLastMessage() != null ? currentChannel.getLastMessage() : "");

        if (currentChannel.getLastMessageTimestamp() != null) {
            holder.textChatTimestamp.setText(DateUtils.getRelativeTimeSpanString(
                    currentChannel.getLastMessageTimestamp().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE)); // More concise formatting
        } else {
            holder.textChatTimestamp.setText("");
        }

        // Load profile image of the other participant
        // TODO: This part requires fetching the actual profile image URL from the 'users' collection
        // For now, it uses a placeholder.
        Glide.with(holder.itemView.getContext())
                .load(R.drawable.ic_profile_placeholder) // Placeholder
                .circleCrop()
                .into(holder.imgChatParticipantProfile);

        // --- NEW: Unread Indicator Handling ---
        // For now, this is static as ChatChannel model doesn't have unreadCount.
        // In a real scenario, you'd calculate this per user.
        int unreadCount = 0; // Replace with actual unread count from ChatChannel model
        if (unreadCount > 0) {
            holder.cardUnreadIndicator.setVisibility(View.VISIBLE);
            holder.textUnreadCount.setText(String.valueOf(unreadCount));
        } else {
            holder.cardUnreadIndicator.setVisibility(View.GONE);
        }
        // --- END NEW ---


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatChannelClick(currentChannel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatChannels.size();
    }

    public static class ChatChannelViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgChatParticipantProfile;
        TextView textChatParticipantName;
        TextView textChatLastMessage;
        TextView textChatTimestamp;
        MaterialCardView cardUnreadIndicator; // NEW
        TextView textUnreadCount; // NEW

        public ChatChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            imgChatParticipantProfile = itemView.findViewById(R.id.img_chat_participant_profile);
            textChatParticipantName = itemView.findViewById(R.id.text_chat_participant_name);
            textChatLastMessage = itemView.findViewById(R.id.text_chat_last_message);
            textChatTimestamp = itemView.findViewById(R.id.text_chat_timestamp);
            cardUnreadIndicator = itemView.findViewById(R.id.card_unread_indicator); // NEW
            textUnreadCount = itemView.findViewById(R.id.text_unread_count); // NEW
        }
    }
}
