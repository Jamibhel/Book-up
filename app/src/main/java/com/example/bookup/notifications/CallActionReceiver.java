package com.example.bookup.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.example.bookup.activities.CallActivity;
import com.example.bookup.models.Call;
import com.example.bookup.utils.RingtonePlayer;
import com.google.firebase.firestore.FirebaseFirestore;

public class CallActionReceiver extends BroadcastReceiver {
    private static final String TAG = "CallActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Call call = (Call) intent.getSerializableExtra(CallActivity.EXTRA_CALL);
        
        if (call == null) return;

        // 1. Stop the ringtone immediately using the Singleton
        RingtonePlayer.getInstance(context).stopRinging();

        // 2. Dismiss the notification
        NotificationManagerCompat.from(context).cancel(NotificationService.CALL_NOTIFICATION_ID);

        if ("ACTION_ACCEPT".equals(action)) {
            Log.d(TAG, "Call Accepted via notification");
            
            // 3. Update Firestore FIRST
            FirebaseFirestore.getInstance().collection("calls").document(call.getId())
                    .update("status", "CONNECTED")
                    .addOnSuccessListener(v -> {
                        Log.d(TAG, "Firestore status updated to CONNECTED from notification");
                        
                        // 4. Launch CallActivity
                        Intent callIntent = new Intent(context, CallActivity.class);
                        callIntent.putExtra(CallActivity.EXTRA_CALL, call);
                        callIntent.putExtra(CallActivity.EXTRA_IS_INCOMING, true);
                        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(callIntent);
                    });

        } else if ("ACTION_REJECT".equals(action)) {
            Log.d(TAG, "Call Rejected via notification");
            FirebaseFirestore.getInstance().collection("calls").document(call.getId())
                    .update("status", Call.Status.REJECTED.name());
        }
    }
}
