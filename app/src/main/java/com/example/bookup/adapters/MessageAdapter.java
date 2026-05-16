package com.example.bookup.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.databinding.ItemMessageReceivedBinding;
import com.example.bookup.databinding.ItemMessageSentBinding;
import com.example.bookup.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    
    private List<Message> messages = new ArrayList<>();
    private Map<String, com.google.firebase.Timestamp> lastReadMap = new java.util.HashMap<>();
    private Map<String, String> participantPhotos = new java.util.HashMap<>();
    private boolean isGroup = false;
    private final String currentUserId = FirebaseAuth.getInstance().getUid();
    private final OnMessageClickListener listener;
    
    private String currentlyPlayingUrl = null;
    private final Handler seekHandler = new Handler();

    public interface OnMessageClickListener {
        void onAudioClick(Message message, SeekBar seekBar, ImageButton playBtn, TextView durationText);
        void onImageClick(Message message);
        void onMessageLongClick(Message message, View view);
        void onReplyClick(Message message);
    }

    public MessageAdapter(OnMessageClickListener listener) {
        this.listener = listener;
    }

    public void setMessages(List<Message> messages) {
        List<Message> visibleMessages = new ArrayList<>();
        for (Message m : messages) {
            if (m.getDeletedForUsers() == null || !m.getDeletedForUsers().contains(currentUserId)) {
                visibleMessages.add(m);
            }
        }
        this.messages = visibleMessages;
        notifyDataSetChanged();
    }

    public void setLastReadMap(Map<String, com.google.firebase.Timestamp> lastReadMap) {
        this.lastReadMap = lastReadMap;
        notifyDataSetChanged();
    }

    public void setParticipantPhotos(Map<String, String> participantPhotos) {
        this.participantPhotos = participantPhotos;
        notifyDataSetChanged();
    }

    public void setGroup(boolean group) {
        isGroup = group;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderId().equals(currentUserId)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            ItemMessageSentBinding binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new SentViewHolder(binding, listener);
        } else {
            ItemMessageReceivedBinding binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceivedViewHolder(binding, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(message, lastReadMap, currentUserId);
        } else {
            String photoUrl = participantPhotos != null ? participantPhotos.get(message.getSenderId()) : null;
            ((ReceivedViewHolder) holder).bind(message, photoUrl, isGroup);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;
        private final OnMessageClickListener listener;

        SentViewHolder(ItemMessageSentBinding binding, OnMessageClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Message message, Map<String, com.google.firebase.Timestamp> lastReadMap, String currentUserId) {
            if (message.isDeletedForEveryone()) {
                binding.messageText.setText("You deleted this message");
                binding.messageText.setVisibility(View.VISIBLE);
                binding.messageText.setTypeface(null, Typeface.ITALIC);
                binding.messageText.setTextColor(Color.GRAY);
                binding.messageImage.setVisibility(View.GONE);
                binding.replyTagLayout.setVisibility(View.GONE);
                binding.statusIcon.setVisibility(View.GONE);
                binding.voiceMessageLayout.getRoot().setVisibility(View.GONE);
            } else {
                binding.messageText.setTypeface(null, Typeface.NORMAL);
                binding.messageText.setTextColor(Color.BLACK);
                binding.statusIcon.setVisibility(View.VISIBLE);
                
                if (message.getType() == Message.Type.TEXT) {
                    binding.messageText.setVisibility(View.VISIBLE);
                    binding.messageImage.setVisibility(View.GONE);
                    binding.voiceMessageLayout.getRoot().setVisibility(View.GONE);
                    binding.callLogLayout.getRoot().setVisibility(View.GONE);
                    binding.messageText.setText(message.getText());
                } else if (message.getType() == Message.Type.IMAGE) {
                    binding.messageText.setVisibility(View.GONE);
                    binding.messageImage.setVisibility(View.VISIBLE);
                    binding.voiceMessageLayout.getRoot().setVisibility(View.GONE);
                    binding.callLogLayout.getRoot().setVisibility(View.GONE);
                    Glide.with(binding.messageImage).load(message.getMediaUrl()).placeholder(R.drawable.ic_user_placeholder).into(binding.messageImage);
                    binding.messageImage.setOnClickListener(v -> listener.onImageClick(message));
                } else if (message.getType() == Message.Type.AUDIO) {
                    binding.messageText.setVisibility(View.GONE);
                    binding.messageImage.setVisibility(View.GONE);
                    binding.voiceMessageLayout.getRoot().setVisibility(View.VISIBLE);
                    binding.callLogLayout.getRoot().setVisibility(View.GONE);
                    String duration = message.getText();
                    binding.voiceMessageLayout.audioDuration.setText(duration != null && !duration.isEmpty() ? duration : "Voice Note");
                    binding.voiceMessageLayout.playPauseButton.setOnClickListener(v -> 
                        listener.onAudioClick(message, binding.voiceMessageLayout.audioSeekBar, 
                            binding.voiceMessageLayout.playPauseButton, binding.voiceMessageLayout.audioDuration));
                } else if (message.getType() == Message.Type.CALL) {
                    binding.messageText.setVisibility(View.GONE);
                    binding.messageImage.setVisibility(View.GONE);
                    binding.voiceMessageLayout.getRoot().setVisibility(View.GONE);
                    binding.callLogLayout.getRoot().setVisibility(View.VISIBLE);
                    
                    TextView callTitle = binding.callLogLayout.callTitle;
                    TextView callDetails = binding.callLogLayout.callDetails;
                    ImageView callIcon = binding.callLogLayout.callTypeIcon;
                    View statusIndicator = binding.callLogLayout.callStatusIndicator;

                    String text = message.getText() != null ? message.getText() : "";
                    callDetails.setText(text);
                    
                    if (text.toLowerCase().contains("video")) {
                        callTitle.setText("Video Call");
                        callIcon.setImageResource(R.drawable.ic_videocam_black_24dp);
                    } else {
                        callTitle.setText("Voice Call");
                        callIcon.setImageResource(R.drawable.ic_phone_black_24dp);
                    }

                    if (text.toLowerCase().contains("missed") || text.toLowerCase().contains("declined")) {
                        statusIndicator.setBackgroundColor(Color.RED);
                    } else {
                        statusIndicator.setBackgroundColor(Color.GREEN);
                    }
                }

                if (message.getReplyToId() != null) {
                    binding.replyTagLayout.setVisibility(View.VISIBLE);
                    binding.replyNameText.setText(message.getReplyToName());
                    binding.replyMessageText.setText(message.getReplyToText());
                    binding.replyTagLayout.setOnClickListener(v -> listener.onReplyClick(message));
                } else {
                    binding.replyTagLayout.setVisibility(View.GONE);
                }

                if (message.getTimestamp() != null) {
                    boolean isRead = false;
                    for (Map.Entry<String, com.google.firebase.Timestamp> entry : lastReadMap.entrySet()) {
                        if (!entry.getKey().equals(currentUserId)) {
                            if (entry.getValue() != null && entry.getValue().compareTo(message.getTimestamp()) >= 0) {
                                isRead = true;
                                break;
                            }
                        }
                    }
                    
                    if (isRead) {
                        binding.statusIcon.setImageResource(R.drawable.ic_check_double);
                        binding.statusIcon.setColorFilter(Color.parseColor("#34B7F1")); // Blue
                    } else {
                        binding.statusIcon.setImageResource(R.drawable.ic_check);
                        binding.statusIcon.setColorFilter(Color.parseColor("#666666")); // Gray
                    }
                }
            }

            if (message.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                binding.messageTime.setText(sdf.format(message.getTimestamp().toDate()));
            }

            itemView.setOnLongClickListener(v -> {
                listener.onMessageLongClick(message, v);
                return true;
            });
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;
        private final OnMessageClickListener listener;

        ReceivedViewHolder(ItemMessageReceivedBinding binding, OnMessageClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Message message, String senderPhotoUrl, boolean isGroupChat) {
            if (message.isDeletedForEveryone()) {
                binding.messageText.setText("This message was deleted");
                binding.messageText.setVisibility(View.VISIBLE);
                binding.messageText.setTypeface(null, Typeface.ITALIC);
                binding.messageText.setTextColor(Color.GRAY);
                binding.messageImage.setVisibility(View.GONE);
                binding.replyTagLayout.setVisibility(View.GONE);
                binding.voiceMessageLayout.getRoot().setVisibility(View.GONE);
                binding.senderName.setVisibility(isGroupChat ? View.VISIBLE : View.GONE);
            } else {
                binding.messageText.setTypeface(null, Typeface.NORMAL);
                binding.messageText.setTextColor(Color.BLACK);
                binding.senderName.setText(message.getSenderName());
                binding.senderName.setVisibility(isGroupChat ? View.VISIBLE : View.GONE);

                if (message.getType() == Message.Type.TEXT) {
                    binding.messageText.setVisibility(View.VISIBLE);
                    binding.messageImage.setVisibility(View.GONE);
                    binding.voiceMessageLayout.getRoot().setVisibility(View.GONE);
                    binding.callLogLayout.getRoot().setVisibility(View.GONE);
                    binding.messageText.setText(message.getText());
                } else if (message.getType() == Message.Type.IMAGE) {
                    binding.messageText.setVisibility(View.GONE);
                    binding.messageImage.setVisibility(View.VISIBLE);
                    binding.voiceMessageLayout.getRoot().setVisibility(View.GONE);
                    binding.callLogLayout.getRoot().setVisibility(View.GONE);
                    Glide.with(binding.messageImage).load(message.getMediaUrl()).placeholder(R.drawable.ic_user_placeholder).into(binding.messageImage);
                    binding.messageImage.setOnClickListener(v -> listener.onImageClick(message));
                } else if (message.getType() == Message.Type.AUDIO) {
                    binding.messageText.setVisibility(View.GONE);
                    binding.messageImage.setVisibility(View.GONE);
                    binding.voiceMessageLayout.getRoot().setVisibility(View.VISIBLE);
                    binding.callLogLayout.getRoot().setVisibility(View.GONE);
                    binding.voiceMessageLayout.audioDuration.setText("Voice Note");
                    binding.voiceMessageLayout.playPauseButton.setOnClickListener(v -> 
                        listener.onAudioClick(message, binding.voiceMessageLayout.audioSeekBar, 
                            binding.voiceMessageLayout.playPauseButton, binding.voiceMessageLayout.audioDuration));
                } else if (message.getType() == Message.Type.CALL) {
                    binding.messageText.setVisibility(View.GONE);
                    binding.messageImage.setVisibility(View.GONE);
                    binding.voiceMessageLayout.getRoot().setVisibility(View.GONE);
                    binding.callLogLayout.getRoot().setVisibility(View.VISIBLE);

                    TextView callTitle = binding.callLogLayout.callTitle;
                    TextView callDetails = binding.callLogLayout.callDetails;
                    ImageView callIcon = binding.callLogLayout.callTypeIcon;
                    View statusIndicator = binding.callLogLayout.callStatusIndicator;

                    String text = message.getText() != null ? message.getText() : "";
                    callDetails.setText(text);

                    if (text.toLowerCase().contains("video")) {
                        callTitle.setText("Video Call");
                        callIcon.setImageResource(R.drawable.ic_videocam_black_24dp);
                    } else {
                        callTitle.setText("Voice Call");
                        callIcon.setImageResource(R.drawable.ic_phone_black_24dp);
                    }

                    if (text.toLowerCase().contains("missed") || text.toLowerCase().contains("declined")) {
                        statusIndicator.setBackgroundColor(Color.RED);
                    } else {
                        statusIndicator.setBackgroundColor(Color.GREEN);
                    }
                }

                if (message.getReplyToId() != null) {
                    binding.replyTagLayout.setVisibility(View.VISIBLE);
                    binding.replyNameText.setText(message.getReplyToName());
                    binding.replyMessageText.setText(message.getReplyToText());
                    binding.replyTagLayout.setOnClickListener(v -> listener.onReplyClick(message));
                } else {
                    binding.replyTagLayout.setVisibility(View.GONE);
                }
            }

            Glide.with(binding.senderProfileImage).load(senderPhotoUrl).placeholder(R.drawable.ic_user_placeholder).circleCrop().into(binding.senderProfileImage);

            if (message.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                binding.messageTime.setText(sdf.format(message.getTimestamp().toDate()));
            }

            itemView.setOnLongClickListener(v -> {
                listener.onMessageLongClick(message, v);
                return true;
            });
        }
    }
}
