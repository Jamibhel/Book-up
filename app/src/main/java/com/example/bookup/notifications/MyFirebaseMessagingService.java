package com.example.bookup.notifications;

import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.bookup.activities.CallActivity;
import com.example.bookup.models.Call;
import com.example.bookup.activities.HomePageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessaging";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String title = "New Notification";
        String body = "You have a new update";
        
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        Map<String, String> data = remoteMessage.getData();
        
        if (!data.isEmpty()) {
            String type = data.get("type");
            if ("INCOMING_CALL".equals(type)) {
                handleIncomingCall(data);
                return; // Stop generic notification for calls
            }

            Intent intent = new Intent(this, HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            String channelId = data.get("channelId");
            if ("CHAT_MESSAGE".equals(type) && channelId != null) {
                intent.putExtra("channelId", channelId);
                intent.putExtra("tabIndex", 4); 
            }
            NotificationService.showNotification(this, title, body, (int) System.currentTimeMillis(), intent);
        } else {
            Intent intent = new Intent(this, HomePageActivity.class);
            NotificationService.showNotification(this, title, body, (int) System.currentTimeMillis(), intent);
        }
    }

    private void handleIncomingCall(Map<String, String> data) {
        String callId = data.get("callId");
        String callerId = data.get("callerId");
        String callerName = data.get("callerName");
        String channelName = data.get("channelName");
        String typeStr = data.get("callType");
        String chatId = data.get("chatId");

        Call.Type type = Call.Type.VIDEO;
        try {
            if (typeStr != null) type = Call.Type.valueOf(typeStr);
        } catch (Exception ignored) {}

        Call call = new Call(callerId, callerName, FirebaseAuth.getInstance().getUid(), "", channelName, type);
        call.setId(callId);
        call.setChatId(chatId);

        Intent intent = new Intent(this, CallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(CallActivity.EXTRA_CALL, call);
        intent.putExtra(CallActivity.EXTRA_IS_INCOMING, true);
        
        // Ensure screen wakes up for the call
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        
        NotificationService.showCallNotification(this, 
            "Incoming " + type.name().toLowerCase() + " call", 
            callerName + " is calling...", 
            intent);
            
        // Fallback: If full-screen intent is restricted, start activity directly
        // but only if app is NOT in foreground (listener would handle it otherwise)
        startActivity(intent);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("fcmToken", token);
            data.put("lastTokenUpdate", com.google.firebase.Timestamp.now());
            FirebaseFirestore.getInstance().collection("users").document(uid).update(data);
        }
    }
}
