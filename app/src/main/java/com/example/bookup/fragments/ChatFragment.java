package com.example.bookup.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.activities.ImagePreviewActivity;
import com.example.bookup.activities.TutorDetailsActivity;
import com.example.bookup.adapters.MessageAdapter;
import com.example.bookup.databinding.FragmentChatBinding;
import com.example.bookup.helpers.AudioPlayer;
import com.example.bookup.helpers.VoiceRecorder;
import com.example.bookup.models.ChatChannel;
import com.example.bookup.models.Message;
import com.example.bookup.models.User;
import com.example.bookup.repositories.StorageRepository;
import com.example.bookup.viewmodels.ChatViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    private FragmentChatBinding binding;
    private ChatViewModel viewModel;
    private MessageAdapter adapter;
    private String channelId;
    private VoiceRecorder voiceRecorder;
    private AudioPlayer audioPlayer;
    private final String currentUserId = FirebaseAuth.getInstance().getUid();
    
    // Playback Coordinator State
    private String currentlyPlayingUrl = null;
    private SeekBar activeSeekBar = null;
    private ImageButton activePlayBtn = null;
    private TextView activeDurationText = null;

    private Message replyToMessage = null;
    private File lastRecordedFile = null;
    private long recordingStartTime = 0;
    private boolean isRecording = false;
    private long totalRecordedTime = 0;

    private final Handler timerHandler = new Handler();
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null || !isRecording || voiceRecorder.isPaused()) return;
            long millis = (System.currentTimeMillis() - recordingStartTime) + totalRecordedTime;
            int seconds = (int) (millis / 1000);
            binding.recordingTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));
            timerHandler.postDelayed(this, 500);
        }
    };

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> { if (uri != null) uploadImage(uri); }
    );

    private com.example.bookup.models.Call.Type pendingCallType = null;
    private final ActivityResultLauncher<String[]> permissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (boolean granted : result.values()) if (!granted) allGranted = false;
                
                if (allGranted) {
                    if (pendingCallType != null) {
                        startCall(pendingCallType);
                        pendingCallType = null;
                    } else if (isRecording) {
                        startRecording();
                    }
                } else {
                    Toast.makeText(getContext(), "Permissions required", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ChatFragment starting");
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        voiceRecorder = new VoiceRecorder(requireContext());
        audioPlayer = new AudioPlayer();
        if (getArguments() != null) {
            channelId = getArguments().getString("channelId");
            binding.toolbarTitle.setText(getArguments().getString("channelName"));
        }
        setupRecyclerView();
        observeViewModel();
        setupListeners();
        setupInputLogic();
        if (channelId != null) viewModel.loadMessages(channelId);
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(new MessageAdapter.OnMessageClickListener() {
            @Override
            public void onAudioClick(Message message, SeekBar seekBar, ImageButton playBtn, TextView durationText) {
                handleAudioPlayback(message, seekBar, playBtn, durationText);
            }
            @Override
            public void onImageClick(Message message) {
                if (message.getMediaUrl() != null) {
                    Intent intent = new Intent(getContext(), ImagePreviewActivity.class);
                    intent.putExtra("imageUrl", message.getMediaUrl());
                    startActivity(intent);
                }
            }
            @Override
            public void onMessageLongClick(Message message, View view) { showOptionsPopup(message, view); }
            @Override
            public void onReplyClick(Message message) {
                if (message.getReplyToId() != null) {
                    List<Message> msgs = viewModel.getMessages().getValue();
                    if (msgs != null) {
                        for (int i = 0; i < msgs.size(); i++) {
                            if (msgs.get(i).getId().equals(message.getReplyToId())) {
                                binding.messagesRecyclerView.smoothScrollToPosition(i);
                                break;
                            }
                        }
                    }
                }
            }
        });
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setStackFromEnd(true);
        binding.messagesRecyclerView.setLayoutManager(lm);
        binding.messagesRecyclerView.setAdapter(adapter);
    }

    private void handleAudioPlayback(Message message, SeekBar seekBar, ImageButton playBtn, TextView durationText) {
        String url = message.getMediaUrl();
        if (url == null || url.isEmpty()) return;

        if (url.equals(currentlyPlayingUrl)) {
            audioPlayer.togglePausePlay();
            return;
        }

        stopActiveAudio();
        currentlyPlayingUrl = url;
        activeSeekBar = seekBar;
        activePlayBtn = playBtn;
        activeDurationText = durationText;

        audioPlayer.playAudio(url, new AudioPlayer.OnProgressListener() {
            @Override
            public void onProgress(int current, int total) {
                if (activeSeekBar != null) { activeSeekBar.setMax(total); activeSeekBar.setProgress(current); }
                if (activeDurationText != null) {
                    int sec = current / 1000;
                    activeDurationText.setText(String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60));
                }
            }

            @Override
            public void onStateChanged(boolean isPlaying) {
                if (activePlayBtn != null) {
                    activePlayBtn.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                }
            }

            @Override
            public void onFinished() { resetAudioUI(); }
            @Override
            public void onError(String error) { resetAudioUI(); }
        });
    }

    private void stopActiveAudio() {
        if (activePlayBtn != null) activePlayBtn.setImageResource(android.R.drawable.ic_media_play);
        if (activeSeekBar != null) activeSeekBar.setProgress(0);
        audioPlayer.stopAudio();
        currentlyPlayingUrl = null;
    }

    private void resetAudioUI() {
        if (activePlayBtn != null) activePlayBtn.setImageResource(android.R.drawable.ic_media_play);
        if (activeSeekBar != null) activeSeekBar.setProgress(0);
        currentlyPlayingUrl = null;
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (binding != null) {
                adapter.setMessages(messages);
                if (adapter.getItemCount() > 0) {
                    binding.messagesRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    if (channelId != null) viewModel.markAsRead(channelId);
                }
            }
        });
        viewModel.getCurrentChannel().observe(getViewLifecycleOwner(), channel -> {
            if (binding != null && channel != null && isAdded()) {
                if (!channel.isGroup()) {
                    String otherId = "";
                    for (String id : channel.getParticipantIds()) if (!id.equals(currentUserId)) { otherId = id; break; }
                    fetchOtherUserInfo(otherId);
                } else {
                    binding.toolbarTitle.setText(channel.getGroupName());
                    Glide.with(this).load(channel.getGroupImage()).placeholder(R.drawable.ic_user_placeholder).circleCrop().into(binding.toolbarProfileImage);
                    binding.toolbarSubtitle.setText(channel.getParticipantIds().size() + " participants");
                }
                adapter.setGroup(channel.isGroup());
                adapter.setParticipantPhotos(channel.getParticipantPhotos());
            }
        });
        viewModel.getLastReadMap().observe(getViewLifecycleOwner(), map -> adapter.setLastReadMap(map));
        viewModel.getTypingUser().observe(getViewLifecycleOwner(), text -> { 
            if (binding != null && text != null) binding.toolbarSubtitle.setText(text); 
        });
    }

    private void fetchOtherUserInfo(String userId) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .addSnapshotListener((doc, e) -> {
                    if (binding != null && doc != null && doc.exists() && isAdded()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            binding.toolbarTitle.setText(user.getDisplayName());
                            Glide.with(this).load(user.getPhotoUrl()).placeholder(R.drawable.ic_user_placeholder).circleCrop().into(binding.toolbarProfileImage);
                            if (user.isOnline()) binding.toolbarSubtitle.setText("online");
                            else if (user.getLastSeen() != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                binding.toolbarSubtitle.setText("last seen " + sdf.format(user.getLastSeen().toDate()));
                            }
                            binding.toolbarContent.setOnClickListener(v -> {
                                if (getContext() != null) {
                                    Intent intent = new Intent(getContext(), TutorDetailsActivity.class);
                                    intent.putExtra("tutorId", userId);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                });
    }

    private void showOptionsPopup(Message message, View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenu().add(0, 1, 0, "Reply");
        if (message.getSenderId().equals(currentUserId) && !message.isDeletedForEveryone()) {
            if (message.getTimestamp() != null && (new Date().getTime() - message.getTimestamp().toDate().getTime() < 600000))
                popup.getMenu().add(0, 2, 0, "Delete for Everyone");
        }
        popup.getMenu().add(0, 3, 0, "Delete for Me");
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: showReplyLayout(message); break;
                case 2: viewModel.deleteForEveryone(channelId, message.getId()); break;
                case 3: viewModel.deleteForMe(channelId, message.getId()); break;
            }
            return true;
        });
        popup.show();
    }

    private void showReplyLayout(Message message) {
        replyToMessage = message;
        binding.replyPreview.getRoot().setVisibility(View.VISIBLE);
        binding.replyPreview.replyNameText.setText(message.getSenderName());
        binding.replyPreview.replyMessageText.setText(message.getType() == Message.Type.TEXT ? message.getText() : "[" + message.getType().name() + "]");
        binding.replyPreview.cancelReplyButton.setOnClickListener(v -> hideReplyLayout());
    }

    private void hideReplyLayout() {
        replyToMessage = null;
        binding.replyPreview.getRoot().setVisibility(View.GONE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupListeners() {
        binding.sendButton.setOnClickListener(v -> {
            if (lastRecordedFile != null) {
                uploadAudio(lastRecordedFile);
                lastRecordedFile = null;
                binding.recordingPreviewLayout.setVisibility(View.GONE);
                binding.messageInputLayout.setVisibility(View.VISIBLE);
            } else sendMessage();
            updateSendButtonVisibility();
        });
        binding.attachButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        binding.recordButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) checkMicPermission();
            else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) stopRecording();
            return true;
        });
        binding.deleteRecordingButton.setOnClickListener(v -> {
            lastRecordedFile = null;
            stopActiveAudio();
            binding.recordingPreviewLayout.setVisibility(View.GONE);
            binding.messageInputLayout.setVisibility(View.VISIBLE);
            updateSendButtonVisibility();
        });
        binding.pauseResumeRecordingButton.setOnClickListener(v -> {
            if (voiceRecorder.isPaused()) {
                voiceRecorder.resumeRecording();
                recordingStartTime = System.currentTimeMillis();
                binding.pauseResumeRecordingButton.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                voiceRecorder.pauseRecording();
                totalRecordedTime += (System.currentTimeMillis() - recordingStartTime);
                binding.pauseResumeRecordingButton.setImageResource(android.R.drawable.ic_media_play);
            }
        });
        binding.playRecordingPreviewButton.setOnClickListener(v -> {
            if (lastRecordedFile == null) return;
            String path = lastRecordedFile.getAbsolutePath();
            if (path.equals(currentlyPlayingUrl)) {
                audioPlayer.togglePausePlay();
            } else {
                stopActiveAudio();
                currentlyPlayingUrl = path;
                activePlayBtn = binding.playRecordingPreviewButton;
                activeSeekBar = binding.previewSeekBar;
                audioPlayer.playAudio(path, new AudioPlayer.OnProgressListener() {
                    @Override public void onProgress(int c, int t) {
                        if (binding == null) return;
                        binding.previewSeekBar.setMax(t); 
                        binding.previewSeekBar.setProgress(c);
                    }
                    @Override public void onStateChanged(boolean isPlaying) {
                        if (binding != null) binding.playRecordingPreviewButton.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                    }
                    @Override public void onFinished() { resetAudioUI(); }
                    @Override public void onError(String e) { resetAudioUI(); }
                });
            }
        });
        binding.chatToolbar.setNavigationOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        
        binding.voiceCallButton.setOnClickListener(v -> checkCallPermissions(com.example.bookup.models.Call.Type.VOICE));
        binding.videoCallButton.setOnClickListener(v -> checkCallPermissions(com.example.bookup.models.Call.Type.VIDEO));
    }

    private void checkCallPermissions(com.example.bookup.models.Call.Type type) {
        Log.d(TAG, "checkCallPermissions: type=" + type);
        pendingCallType = type;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkCallPermissions: Permissions granted, starting call");
            startCall(type);
        } else {
            Log.d(TAG, "checkCallPermissions: Requesting permissions");
            permissionsLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA});
        }
    }

    private void startCall(com.example.bookup.models.Call.Type type) {
        Log.d(TAG, "startCall: Attempting to start " + type + " call");
        if (channelId == null || currentUserId == null) {
            Log.e(TAG, "startCall: channelId or currentUserId is null");
            Toast.makeText(getContext(), "Error: User or Channel not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ChatChannel channel = viewModel.getCurrentChannel().getValue();
        if (channel == null) {
            Log.e(TAG, "startCall: current channel is null");
            Toast.makeText(getContext(), "Chat loading, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        String receiverId = "";
        for (String id : channel.getParticipantIds()) if (!id.equals(currentUserId)) { receiverId = id; break; }
        
        if (receiverId.isEmpty()) {
            Log.e(TAG, "startCall: No receiver found");
            Toast.makeText(getContext(), "Error: No participant found to call", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "startCall: receiverId=" + receiverId);
        Toast.makeText(getContext(), "Starting " + type.name().toLowerCase() + " call...", Toast.LENGTH_SHORT).show();

        com.example.bookup.models.Call call = new com.example.bookup.models.Call(
            currentUserId, 
            FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
            receiverId,
            channel.isGroup() ? channel.getGroupName() : channel.getParticipantNames().get(receiverId),
            UUID.randomUUID().toString(),
            type
        );
        call.setChatId(channelId);
        call.setStatus(com.example.bookup.models.Call.Status.DIALING);
        call.setCallerPhotoUrl(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null ? FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString() : "");
        call.setReceiverPhotoUrl(channel.getParticipantPhotos().get(receiverId));

        Log.d(TAG, "startCall: Adding call document to Firestore");
        // Convert to Map for 100% control over types (especially Enums)
        java.util.Map<String, Object> callMap = new java.util.HashMap<>();
        callMap.put("callerId", call.getCallerId());
        callMap.put("callerName", call.getCallerName());
        callMap.put("callerPhotoUrl", call.getCallerPhotoUrl());
        callMap.put("receiverId", call.getReceiverId());
        callMap.put("receiverName", call.getReceiverName());
        callMap.put("receiverPhotoUrl", call.getReceiverPhotoUrl());
        callMap.put("channelName", call.getChannelName());
        callMap.put("chatId", call.getChatId());
        callMap.put("status", "DIALING");
        callMap.put("type", type.name());
        callMap.put("timestamp", com.google.firebase.Timestamp.now());

        FirebaseFirestore.getInstance().collection("calls").add(callMap)
            .addOnSuccessListener(ref -> {
                Log.d(TAG, "startCall: Call document added with ID: " + ref.getId());
                call.setId(ref.getId());
                ref.update("id", ref.getId());
                
                Intent intent = new Intent(getContext(), com.example.bookup.activities.CallActivity.class);
                intent.putExtra(com.example.bookup.activities.CallActivity.EXTRA_CALL, call);
                intent.putExtra(com.example.bookup.activities.CallActivity.EXTRA_IS_INCOMING, false);
                startActivity(intent);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "startCall: Failed to add call document", e);
                Toast.makeText(getContext(), "Failed to start call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void setupInputLogic() {
        binding.messageInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { if (channelId != null) viewModel.setTypingStatus(channelId, s.length() > 0); updateSendButtonVisibility(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        updateSendButtonVisibility();
    }

    private void updateSendButtonVisibility() {
        if (binding == null) return;
        boolean hasText = !binding.messageInput.getText().toString().trim().isEmpty();
        boolean hasVoice = lastRecordedFile != null;
        if (hasVoice || hasText) {
            binding.sendButton.setVisibility(View.VISIBLE);
            binding.recordButton.setVisibility(View.GONE);
        } else {
            binding.sendButton.setVisibility(View.GONE);
            binding.recordButton.setVisibility(View.VISIBLE);
        }
    }

    private void checkMicPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecording();
        else permissionsLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO});
    }

    private void startRecording() {
        isRecording = true;
        totalRecordedTime = 0;
        recordingStartTime = System.currentTimeMillis();
        binding.messageInputLayout.setVisibility(View.GONE);
        binding.recordingLayout.setVisibility(View.VISIBLE);
        binding.pauseResumeRecordingButton.setImageResource(android.R.drawable.ic_media_pause);
        timerHandler.post(timerRunnable);
        voiceRecorder.startRecording();
    }

    private void stopRecording() {
        if (!isRecording) return;
        isRecording = false;
        binding.recordingLayout.setVisibility(View.GONE);
        timerHandler.removeCallbacks(timerRunnable);
        File audioFile = voiceRecorder.stopRecording();
        if (audioFile != null && audioFile.exists()) {
            lastRecordedFile = audioFile;
            binding.recordingPreviewLayout.setVisibility(View.VISIBLE);
        } else {
            binding.messageInputLayout.setVisibility(View.VISIBLE);
        }
        updateSendButtonVisibility();
    }

    private void sendMessage() {
        if (binding == null) return;
        String text = binding.messageInput.getText().toString().trim();
        if (!text.isEmpty() && channelId != null) {
            if (replyToMessage != null) { viewModel.sendReplyMessage(channelId, text, replyToMessage); hideReplyLayout(); }
            else viewModel.sendMessage(channelId, text, Message.Type.TEXT);
            binding.messageInput.setText("");
        }
    }

    private void uploadImage(Uri uri) {
        try {
            File tempFile = File.createTempFile("upload", ".jpg", requireContext().getCacheDir());
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            if (is == null) return;
            FileOutputStream os = new FileOutputStream(tempFile);
            byte[] buf = new byte[1024]; int r;
            while ((r = is.read(buf)) != -1) os.write(buf, 0, r);
            os.close(); is.close();
            StorageRepository.uploadImage(tempFile, channelId, UUID.randomUUID().toString(), (u, t) -> {}, new StorageRepository.OnUploadCompleteListener() {
                @Override public void onSuccess(String url) { viewModel.sendMediaMessage(channelId, url, Message.Type.IMAGE); }
                @Override public void onError(Exception e) { Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show(); }
            });
        } catch (Exception e) { Log.e(TAG, "Upload error", e); }
    }

    private void uploadAudio(File file) {
        StorageRepository.uploadAudio(file, channelId, UUID.randomUUID().toString(), (u, t) -> {}, new StorageRepository.OnUploadCompleteListener() {
            @Override public void onSuccess(String url) { viewModel.sendMediaMessage(channelId, url, Message.Type.AUDIO); }
            @Override public void onError(Exception e) { Toast.makeText(getContext(), "Voice note failed", Toast.LENGTH_SHORT).show(); }
        });
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        isRecording = false;
        timerHandler.removeCallbacks(timerRunnable);
        stopActiveAudio();
        binding = null;
    }
}
