package com.example.bookup.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.bookup.R;

public class NotificationService {
    public static final String CALL_CHANNEL_ID = "call_channel";
    public static final String MESSAGE_CHANNEL_ID = "message_channel";
    public static final int CALL_NOTIFICATION_ID = 1001;

    public static void initializeNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // Call Channel: High priority, sound enabled
            NotificationChannel callChannel = new NotificationChannel(
                    CALL_CHANNEL_ID,
                    "Incoming Calls",
                    NotificationManager.IMPORTANCE_HIGH
            );
            callChannel.setDescription("Notifications for incoming video and voice calls");
            callChannel.enableLights(true);
            callChannel.enableVibration(true);
            callChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(callChannel);

            // Message Channel: Default priority
            NotificationChannel messageChannel = new NotificationChannel(
                    MESSAGE_CHANNEL_ID,
                    "Messages",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            messageChannel.setDescription("Notifications for new chat messages");
            manager.createNotificationChannel(messageChannel);
        }
    }

    public static void showCallNotification(Context context, String title, String message, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        com.example.bookup.models.Call call = (com.example.bookup.models.Call) intent.getSerializableExtra(com.example.bookup.activities.CallActivity.EXTRA_CALL);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, CALL_NOTIFICATION_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Accept Action
        Intent acceptIntent = new Intent(context, CallActionReceiver.class);
        acceptIntent.setAction("ACTION_ACCEPT");
        acceptIntent.putExtra(com.example.bookup.activities.CallActivity.EXTRA_CALL, call);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(context, 0, acceptIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Reject Action
        Intent rejectIntent = new Intent(context, CallActionReceiver.class);
        rejectIntent.setAction("ACTION_REJECT");
        rejectIntent.putExtra(com.example.bookup.activities.CallActivity.EXTRA_CALL, call);
        PendingIntent rejectPendingIntent = PendingIntent.getBroadcast(context, 1, rejectIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CALL_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_phone_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .setOngoing(true)
                .addAction(R.drawable.ic_phone_black_24dp, "Accept", acceptPendingIntent)
                .addAction(R.drawable.ic_logout_black_24dp, "Reject", rejectPendingIntent);

        notificationManager.notify(CALL_NOTIFICATION_ID, builder.build());
    }

    public static void showBookingNotification(Context context, String status, String subject, String bookingId) {
        String title = "Booking Update";
        String body = "Your booking for " + subject + " is now " + status;
        
        if ("confirmed".equalsIgnoreCase(status)) {
            title = "Booking Accepted! 🎉";
            body = "Great news! Your " + subject + " session was accepted.";
        } else if ("cancelled".equalsIgnoreCase(status)) {
            title = "Booking Rejected ❌";
            body = "Sorry, your " + subject + " session was not accepted.";
        }

        showNotification(context, title, body, bookingId.hashCode(), null);
    }

    public static void showNotification(Context context, String title, String message, int notificationId, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context, notificationId, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MESSAGE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message_black_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        notificationManager.notify(notificationId, builder.build());
    }
}
