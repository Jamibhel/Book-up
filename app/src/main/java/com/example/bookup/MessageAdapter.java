package com.example.bookup;

import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.models.ChatMessage;

import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<ChatMessage> messageList;
    private String currentUserId; // The UID of the currently logged-in user

    // Flag to indicate if it's a group chat (for showing sender names)
    private boolean isGroupChat = false;

    public MessageAdapter(List<ChatMessage> messageList, String currentUserId, boolean isGroupChat) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.isGroupChat = isGroupChat;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message, isGroupChat);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder for sent messages
    private static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_sent);
            timeText = itemView.findViewById(R.id.text_timestamp_sent);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getMessageText());
            if (message.getTimestamp() != null) {
                timeText.setText(DateFormat.format("hh:mm a", message.getTimestamp()).toString());
            } else {
                timeText.setText("");
            }
        }
    }

    // ViewHolder for received messages
    private static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, senderNameText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_received);
            timeText = itemView.findViewById(R.id.text_timestamp_received);
            senderNameText = itemView.findViewById(R.id.text_sender_name); // For group chats
        }

        void bind(ChatMessage message, boolean isGroupChat) {
            messageText.setText(message.getMessageText());
            if (message.getTimestamp() != null) {
                timeText.setText(DateFormat.format("hh:mm a", message.getTimestamp()).toString());
            } else {
                timeText.setText("");
            }

            // Only show sender name if it's a group chat
            if (isGroupChat) {
                senderNameText.setText(message.getSenderName());
                senderNameText.setVisibility(View.VISIBLE);
            } else {
                senderNameText.setVisibility(View.GONE);
            }
        }
    }
}
