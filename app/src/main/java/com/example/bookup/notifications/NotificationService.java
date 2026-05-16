package com.example.bookup.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.bookup.R;
import com.example.bookup.activities.HomePageActivity;

/**
 * Service to display local notifications.
 * Handles both FCM push notifications and Firestore-triggered notifications.
 */
public class NotificationService {

    private static final String NOTIFICATION_CHANNEL_ID = "bookup_notifications";
    private static final String NOTIFICATION_CHANNEL_NAME = "BookUp Notifications";
    private static final String CALL_CHANNEL_ID = "bookup_calls";
    public static final int CHAT_NOTIFICATION_ID = 1001;
    public static final int BOOKING_NOTIFICATION_ID = 1002;
    public static final int CALL_NOTIFICATION_ID = 1003;

    /**
     * Initialize notification channels (required for Android 8.0+).
     */
    public static void initializeNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                // Main Channel
                NotificationChannel channel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notifications for booking status updates and messages");
                notificationManager.createNotificationChannel(channel);

                // Call Channel (with Ringtone)
                NotificationChannel callChannel = new NotificationChannel(
                        CALL_CHANNEL_ID,
                        context.getString(R.string.channel_call_name),
                        NotificationManager.IMPORTANCE_HIGH
                );
                callChannel.setDescription(context.getString(R.string.channel_call_desc));
                callChannel.setSound(android.provider.Settings.System.DEFAULT_RINGTONE_URI, null);
                callChannel.enableVibration(true);
                callChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(callChannel);
            }
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
                .addAction(R.drawable.ic_videocam_black_24dp, "Accept", acceptPendingIntent)
                .addAction(R.drawable.ic_logout_black_24dp, "Reject", rejectPendingIntent);

        notificationManager.notify(CALL_NOTIFICATION_ID, builder.build());
    }

    /**
     * Display a local notification with navigation intent.
     */
    public static void showNotification(Context context, String title, String message, int notificationId, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;

        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context, notificationId, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_chat_black_24dp)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        notificationManager.notify(notificationId, builder.build());
    }

    public static void showBookingNotification(Context context, String status, String subject, String bookingId) {
        String title;
        String message;

        if ("confirmed".equalsIgnoreCase(status)) {
            title = "Booking Accepted! 🎉";
            message = "Your " + subject + " session has been confirmed.";
        } else if ("cancelled".equalsIgnoreCase(status)) {
            title = "Booking Rejected ❌";
            message = "Your " + subject + " session was rejected.";
        } else {
            title = "Booking Update";
            message = "Status updated to: " + status;
        }

        Intent intent = new Intent(context, HomePageActivity.class);
        intent.putExtra("tabIndex", 2); // Navigate to Requests/Bookings tab
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showNotification(context, title, message, BOOKING_NOTIFICATION_ID, intent);
    }
}
