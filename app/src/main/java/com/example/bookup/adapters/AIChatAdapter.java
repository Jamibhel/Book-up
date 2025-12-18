package com.example.bookup.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.R;
import com.example.bookup.ai.AIChatMessage;
import com.example.bookup.utils.MarkdownHelper;

import java.util.List;

/**
 * Adapter for displaying AI Chat messages
 * Handles both user and AI messages with different styling
 * AI messages support markdown formatting
 */
public class AIChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER_MESSAGE = 1;
    private static final int VIEW_TYPE_AI_MESSAGE = 2;

    private List<AIChatMessage> messages;
    private Context context;

    public AIChatAdapter(List<AIChatMessage> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        AIChatMessage message = messages.get(position);
        if (message.isFromAI()) {
            return VIEW_TYPE_AI_MESSAGE;
        } else {
            return VIEW_TYPE_USER_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_ai_message_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_ai_message_ai, parent, false);
            return new AIMessageViewHolder(view, context);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AIChatMessage message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else {
            ((AIMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * ViewHolder for user messages (green bubble, right-aligned)
     */
    private static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView timeText;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.userMessageText);
            timeText = itemView.findViewById(R.id.userMessageTime);
        }

        void bind(AIChatMessage message) {
            messageText.setText(message.getMessageText());
            if (message.getTimestamp() != null) {
                String timeFormatted = DateFormat.format("hh:mm a", 
                        message.getTimestamp().toDate()).toString();
                timeText.setText(timeFormatted);
            } else {
                timeText.setText("");
            }
        }
    }

    /**
     * ViewHolder for AI messages (gray bubble, left-aligned)
     * Supports markdown rendering
     */
    private static class AIMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView timeText;
        private final Context context;

        AIMessageViewHolder(View itemView, Context context) {
            super(itemView);
            messageText = itemView.findViewById(R.id.aiMessageText);
            timeText = itemView.findViewById(R.id.aiMessageTime);
            this.context = context;
        }

        void bind(AIChatMessage message) {
            // Render message with markdown support
            if (message.isMarkdown()) {
                MarkdownHelper.setMarkdown(context, messageText, message.getMessageText());
            } else {
                messageText.setText(message.getMessageText());
            }

            if (message.getTimestamp() != null) {
                String timeFormatted = DateFormat.format("hh:mm a", 
                        message.getTimestamp().toDate()).toString();
                timeText.setText(timeFormatted);
            } else {
                timeText.setText("");
            }
        }
    }

    public void addMessage(AIChatMessage message) {
        messages.add(0, message);
        notifyItemInserted(0);
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }
}
