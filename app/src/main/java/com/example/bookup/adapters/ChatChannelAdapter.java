package com.example.bookup.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.databinding.ItemChatChannelBinding;
import com.example.bookup.models.ChatChannel;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatChannelAdapter extends ListAdapter<ChatChannel, ChatChannelAdapter.ViewHolder> {
    private static final String TAG = "ChatChannelAdapter";
    private final OnChannelClickListener listener;
    private final String currentUserId = FirebaseAuth.getInstance().getUid();

    public interface OnChannelClickListener {
        void onChannelClick(ChatChannel channel);
        void onChannelLongClick(ChatChannel channel, View view);
    }

    public ChatChannelAdapter(OnChannelClickListener listener) {
        super(new DiffUtil.ItemCallback<ChatChannel>() {
            @Override
            public boolean areItemsTheSame(@NonNull ChatChannel oldItem, @NonNull ChatChannel newItem) {
                return oldItem.getId().equals(newItem.getId());
            }
            @Override
            public boolean areContentsTheSame(@NonNull ChatChannel oldItem, @NonNull ChatChannel newItem) {
                return String.valueOf(oldItem.getLastMessage()).equals(String.valueOf(newItem.getLastMessage())) &&
                       String.valueOf(oldItem.getGroupName()).equals(String.valueOf(newItem.getGroupName())) &&
                       String.valueOf(oldItem.getLastMessageTimestamp()).equals(String.valueOf(newItem.getLastMessageTimestamp()));
            }
        });
        this.listener = listener;
    }

    public void setChannels(List<ChatChannel> channels) {
        if (channels == null) return;
        
        List<ChatChannel> sorted = new ArrayList<>(channels);
        sorted.sort((c1, c2) -> {
            boolean p1 = c1.getPinnedBy() != null && Boolean.TRUE.equals(c1.getPinnedBy().get(currentUserId));
            boolean p2 = c2.getPinnedBy() != null && Boolean.TRUE.equals(c2.getPinnedBy().get(currentUserId));
            if (p1 && !p2) return -1;
            if (!p1 && p2) return 1;
            long t1 = c1.getLastMessageTimestamp() != null ? c1.getLastMessageTimestamp().getSeconds() : 0;
            long t2 = c2.getLastMessageTimestamp() != null ? c2.getLastMessageTimestamp().getSeconds() : 0;
            return Long.compare(t2, t1);
        });

        List<ChatChannel> visible = new ArrayList<>();
        for (ChatChannel c : sorted) {
            if (c.getDeletedBy() == null || !c.getDeletedBy().contains(currentUserId)) {
                visible.add(c);
            }
        }
        submitList(visible);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemChatChannelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatChannelBinding binding;
        ViewHolder(ItemChatChannelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatChannel channel) {
            String displayName = "Chat";
            String photoUrl = "";

            if (channel.isGroup()) {
                displayName = channel.getGroupName() != null ? channel.getGroupName() : "Group Chat";
                photoUrl = channel.getGroupImage();
            } else {
                Map<String, String> names = channel.getParticipantNames();
                if (names != null) {
                    for (Map.Entry<String, String> entry : names.entrySet()) {
                        if (!entry.getKey().equals(currentUserId)) {
                            displayName = entry.getValue();
                            break;
                        }
                    }
                }
                Map<String, String> photos = channel.getParticipantPhotos();
                if (photos != null) {
                    for (Map.Entry<String, String> entry : photos.entrySet()) {
                        if (!entry.getKey().equals(currentUserId)) {
                            photoUrl = entry.getValue();
                            break;
                        }
                    }
                }
            }

            binding.channelName.setText(displayName);
            String lastMsg = channel.getLastMessage();
            binding.lastMessage.setText((lastMsg == null || lastMsg.isEmpty()) ? "No messages yet" : lastMsg);
            
            if (channel.getLastMessageTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                binding.lastMessageTime.setText(sdf.format(channel.getLastMessageTimestamp().toDate()));
                binding.lastMessageTime.setVisibility(View.VISIBLE);
            } else {
                binding.lastMessageTime.setVisibility(View.GONE);
            }

            boolean isPinned = channel.getPinnedBy() != null && Boolean.TRUE.equals(channel.getPinnedBy().get(currentUserId));
            binding.pinIcon.setVisibility(isPinned ? View.VISIBLE : View.GONE);

            // Unread Indicator logic
            com.google.firebase.Timestamp lastRead = channel.getLastRead() != null ? channel.getLastRead().get(currentUserId) : null;
            com.google.firebase.Timestamp lastMsgTimestamp = channel.getLastMessageTimestamp();
            boolean isUnread = false;
            if (lastMsgTimestamp != null) {
                if (lastRead == null) isUnread = true;
                else if (lastMsgTimestamp.compareTo(lastRead) > 0) isUnread = true;
            }
            binding.unreadIndicator.setVisibility(isUnread ? View.VISIBLE : View.GONE);
            
            if (isUnread) {
                binding.lastMessageTime.setTextColor(itemView.getContext().getColor(R.color.primary));
                binding.lastMessage.setTypeface(null, Typeface.BOLD);
                binding.lastMessage.setTextColor(Color.BLACK);
            } else {
                binding.lastMessageTime.setTextColor(Color.GRAY);
                binding.lastMessage.setTypeface(null, Typeface.NORMAL);
                binding.lastMessage.setTextColor(Color.GRAY);
            }

            Glide.with(binding.channelImage).load(photoUrl).placeholder(R.drawable.ic_user_placeholder).circleCrop().into(binding.channelImage);
            itemView.setOnClickListener(v -> listener.onChannelClick(channel));
            itemView.setOnLongClickListener(v -> { listener.onChannelLongClick(channel, v); return true; });
        }
    }
}
