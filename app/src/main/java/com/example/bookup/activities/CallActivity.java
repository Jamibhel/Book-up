package com.example.bookup.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.models.Call;
import com.example.bookup.models.User;
import com.example.bookup.utils.RingtonePlayer;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

import java.util.Locale;

public class CallActivity extends AppCompatActivity {
    private static final String TAG = "CallActivity";
    public static final String EXTRA_CALL = "extra_call";
    public static final String EXTRA_IS_INCOMING = "extra_is_incoming";
    
    private static final int MOBILE_UID = 0; // 0 forces Agora to assign a dynamic, unique UID
    // Updated to match the Web and Firebase Backend App ID
    private static final String AGORA_APP_ID = "cae7a5275c7a4283a32df9bdd13f8a47";

    private FrameLayout localVideoContainer, remoteVideoContainer;
    private View callerInfoLayout;
    private TextView textName, textStatus;
    private ShapeableImageView imgAvatar;
    private MaterialCardView localVideoCard;
    private FloatingActionButton btnAccept, btnEnd, btnMute, btnSwitchCamera, btnVideoToggle, btnSpeaker;

    private Call currentCall;
    private boolean isIncoming;
    private boolean isMuted = false;
    private boolean isVideoDisabled = false;
    private boolean isSpeakerOn = true;
    private long callStartTime = 0;
    private final Handler timerHandler = new Handler();
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (callStartTime > 0) {
                long duration = (System.currentTimeMillis() - callStartTime) / 1000;
                long mins = duration / 60;
                long secs = duration % 60;
                textStatus.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
            }
            timerHandler.postDelayed(this, 1000);
        }
    };
    private FirebaseFirestore db;
    private RingtonePlayer ringtonePlayer;
    private ListenerRegistration callListener;
    private RtcEngine mRtcEngine;
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        db = FirebaseFirestore.getInstance();
        currentCall = (Call) getIntent().getSerializableExtra(EXTRA_CALL);
        isIncoming = getIntent().getBooleanExtra(EXTRA_IS_INCOMING, false);

        if (currentCall == null) { finish(); return; }

        // Use Singleton RingtonePlayer
        ringtonePlayer = RingtonePlayer.getInstance(this);
        if (isIncoming) ringtonePlayer.startRinging();

        initViews();
        fetchFreshProfileData();
        setupCallUI();
        listenForCallUpdates();

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!isIncoming && mRtcEngine == null) initializeAndJoinChannel();
            } else {
                finish();
            }
        }
    }

    private void initViews() {
        localVideoContainer = findViewById(R.id.local_video_view_container);
        remoteVideoContainer = findViewById(R.id.remote_video_view_container);
        callerInfoLayout = findViewById(R.id.caller_info_layout);
        textName = findViewById(R.id.text_caller_name);
        textStatus = findViewById(R.id.text_call_status);
        imgAvatar = findViewById(R.id.img_caller_avatar);
        localVideoCard = findViewById(R.id.local_video_card);
        
        btnAccept = findViewById(R.id.btn_accept_call);
        btnEnd = findViewById(R.id.btn_end_call);
        btnMute = findViewById(R.id.btn_mute);
        btnSwitchCamera = findViewById(R.id.btn_switch_camera);
        btnVideoToggle = findViewById(R.id.btn_video_toggle);
        btnSpeaker = findViewById(R.id.btn_speaker);

        btnEnd.setOnClickListener(v -> endCall(currentCall.getStatus() == Call.Status.DIALING ? Call.Status.REJECTED : Call.Status.ENDED));
        btnAccept.setOnClickListener(v -> acceptCall());
        btnMute.setOnClickListener(v -> toggleMute());
        btnSpeaker.setOnClickListener(v -> toggleSpeaker());
        btnSwitchCamera.setOnClickListener(v -> { if (mRtcEngine != null) mRtcEngine.switchCamera(); });
        btnVideoToggle.setOnClickListener(v -> toggleVideo());

        if (currentCall.getType() == Call.Type.VOICE) {
            btnSwitchCamera.setVisibility(View.GONE);
            btnVideoToggle.setVisibility(View.GONE);
            localVideoCard.setVisibility(View.GONE);
        }
    }

    private void fetchFreshProfileData() {
        String peerId = isIncoming ? currentCall.getCallerId() : currentCall.getReceiverId();
        db.collection("users").document(peerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    textName.setText(user.getDisplayName());
                    Glide.with(this).load(user.getPhotoUrl()).placeholder(R.drawable.ic_profile_placeholder).into(imgAvatar);
                }
            }
        });
    }

    private void setupCallUI() {
        textStatus.setText(isIncoming ? "Incoming Call..." : "Dialing...");
        btnAccept.setVisibility(isIncoming ? View.VISIBLE : View.GONE);
        btnMute.setVisibility(View.GONE);
        btnVideoToggle.setVisibility(View.GONE);
        btnSwitchCamera.setVisibility(View.GONE);
        btnSpeaker.setVisibility(View.GONE);
        
        if (!isIncoming && checkSelfPermission()) {
            initializeAndJoinChannel();
        }
    }

    private void toggleMute() {
        isMuted = !isMuted;
        if (mRtcEngine != null) mRtcEngine.muteLocalAudioStream(isMuted);
        btnMute.setAlpha(isMuted ? 0.5f : 1.0f);
    }

    private void toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn;
        if (mRtcEngine != null) {
            mRtcEngine.setEnableSpeakerphone(isSpeakerOn);
            btnSpeaker.setAlpha(isSpeakerOn ? 1.0f : 0.5f);
        }
    }

    private void toggleVideo() {
        isVideoDisabled = !isVideoDisabled;
        if (mRtcEngine != null) {
            mRtcEngine.muteLocalVideoStream(isVideoDisabled);
            localVideoContainer.setVisibility(isVideoDisabled ? View.GONE : View.VISIBLE);
        }
        btnVideoToggle.setAlpha(isVideoDisabled ? 0.5f : 1.0f);
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            runOnUiThread(() -> {
                textStatus.setText("Connected");
                btnMute.setVisibility(View.VISIBLE);
                btnSpeaker.setVisibility(View.VISIBLE);
                if (currentCall.getType() == Call.Type.VIDEO) {
                    btnVideoToggle.setVisibility(View.VISIBLE);
                    btnSwitchCamera.setVisibility(View.VISIBLE);
                    localVideoCard.setVisibility(View.VISIBLE);
                    localVideoCard.bringToFront();
                }
                
                if (mRtcEngine != null) {
                    mRtcEngine.muteLocalAudioStream(false);
                    if (currentCall.getType() == Call.Type.VIDEO) {
                        mRtcEngine.muteLocalVideoStream(false);
                    }
                }
                if (callStartTime == 0) {
                    callStartTime = System.currentTimeMillis();
                    timerHandler.post(timerRunnable);
                }
                if (ringtonePlayer != null) ringtonePlayer.stopRinging();
                Log.d(TAG, "[CallStep 7] Joined Agora channel successfully. Media publishing...");
                Toast.makeText(CallActivity.this, "Call connected", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> {
                remoteVideoContainer.removeAllViews();
                callerInfoLayout.setVisibility(View.VISIBLE);
                textStatus.setText("User offline");
            });
        }

        @Override
        public void onError(int err) {
            Log.e(TAG, "Agora error: " + err);
            runOnUiThread(() -> Toast.makeText(CallActivity.this, "Agora Error: " + err, Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onWarning(int warn) {
            Log.w(TAG, "Agora warning: " + warn);
        }

        @Override
        public void onConnectionStateChanged(int state, int reason) {
            Log.d(TAG, "Agora connection state: " + state + ", reason: " + reason);
        }
    };

    private boolean isJoiningChannel = false;

    private void initializeAndJoinChannel() {
        if (isJoiningChannel || mRtcEngine != null) return;
        isJoiningChannel = true;
        
        Log.d(TAG, "[CallStep 4] Joining channel directly without token...");
        joinWithToken(null);
    }

    private void joinWithToken(String token) {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getApplicationContext();
            config.mAppId = AGORA_APP_ID;
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);
            mRtcEngine.setChannelProfile(io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION);
            mRtcEngine.enableAudio();
            
            if (currentCall.getType() == Call.Type.VIDEO) {
                mRtcEngine.enableVideo();
                mRtcEngine.startPreview();
                SurfaceView surfaceView = new SurfaceView(getBaseContext());
                surfaceView.setZOrderMediaOverlay(true);
                localVideoContainer.addView(surfaceView);
                mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, MOBILE_UID));
                localVideoCard.setVisibility(View.VISIBLE);
            } else {
                mRtcEngine.disableVideo();
            }
            
            // Set parameters for better quality/connectivity if needed
            mRtcEngine.setParameters("{\"che.audio.opensl\":true}"); 
            
            Log.d(TAG, "[CallStep 6] Joining Agora channel: " + currentCall.getChannelName() + " with UID: " + MOBILE_UID);
            mRtcEngine.joinChannel(null, currentCall.getChannelName(), "", MOBILE_UID);
        } catch (Exception e) {
            Log.e(TAG, "Agora init error", e);
        }
    }

    private void setupRemoteVideo(int uid) {
        if (currentCall.getType() == Call.Type.VIDEO) {
            SurfaceView surfaceView = new SurfaceView(getBaseContext());
            remoteVideoContainer.addView(surfaceView);
            mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
            remoteVideoContainer.setVisibility(View.VISIBLE);
            callerInfoLayout.setVisibility(View.GONE);
        }
    }

    private void listenForCallUpdates() {
        callListener = db.collection("calls").document(currentCall.getId())
                .addSnapshotListener((doc, e) -> {
                    if (doc != null && doc.exists()) {
                        String status = doc.getString("status");
                        if ("CONNECTED".equals(status)) {
                            if (isIncoming && mRtcEngine == null) initializeAndJoinChannel();
                        } else if ("REJECTED".equals(status) || "ENDED".equals(status) || "MISSED".equals(status)) {
                            finish();
                        }
                    } else {
                        finish();
                    }
                });
    }

    private void acceptCall() {
        if (ringtonePlayer != null) ringtonePlayer.stopRinging();
        db.collection("calls").document(currentCall.getId()).update("status", "CONNECTED")
                .addOnSuccessListener(v -> {
                    btnAccept.setVisibility(View.GONE);
                    if (mRtcEngine == null) initializeAndJoinChannel();
                });
    }

    private void endCall(Call.Status status) {
        if (ringtonePlayer != null) ringtonePlayer.stopRinging();
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
        logCallToChat(status);
        db.collection("calls").document(currentCall.getId()).update("status", status.name()).addOnSuccessListener(v -> finish());
    }

    private void logCallToChat(Call.Status status) {
        if (currentCall.getChatId() == null) return;
        String typeName = currentCall.getType() == Call.Type.VIDEO ? "Video call" : "Voice call";
        String messageText = "";
        long duration = (callStartTime > 0) ? (System.currentTimeMillis() - callStartTime) / 1000 : 0;
        switch (status) {
            case REJECTED: messageText = isIncoming ? "Declined " + typeName : typeName + " rejected"; break;
            case MISSED: messageText = "Missed " + typeName; break;
            case ENDED:
                if (duration > 0) messageText = String.format(Locale.getDefault(), "%s ended - %02d:%02d", typeName, duration / 60, duration % 60);
                else messageText = isIncoming ? "Missed " + typeName : "No answer";
                break;
        }
        if (!messageText.isEmpty()) {
            com.example.bookup.repositories.ChatRepository repo = new com.example.bookup.repositories.ChatRepository();
            repo.sendMessage(currentCall.getChatId(), new com.example.bookup.models.Message(com.google.firebase.auth.FirebaseAuth.getInstance().getUid(), com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), messageText, com.example.bookup.models.Message.Type.CALL));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        if (ringtonePlayer != null) ringtonePlayer.stopRinging();
        if (callListener != null) callListener.remove();
        if (mRtcEngine != null) { mRtcEngine.leaveChannel(); RtcEngine.destroy(); }
    }
}
