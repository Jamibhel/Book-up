package com.example.bookup.notifications;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Manages Firestore listeners for real-time notifications.
 */
public class NotificationListener {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private ListenerRegistration notificationListener;
    private Context context;

    public NotificationListener(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void startListening() {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();

        notificationListener = firestore.collection("notifications")
                .document(currentUserId)
                .collection("messages")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (querySnapshot != null) {
                        querySnapshot.getDocumentChanges().forEach(docChange -> {
                            if (docChange.getType().toString().equals("ADDED")) {
                                String type = docChange.getDocument().getString("type");
                                String status = docChange.getDocument().getString("status");
                                String bookingId = docChange.getDocument().getString("bookingId");
                                String subject = docChange.getDocument().getString("subject");

                                if ("booking_status_changed".equals(type)) {
                                    NotificationService.showBookingNotification(context, status, 
                                            subject != null ? subject : "session", bookingId);
                                }
                            }
                        });
                    }
                });
    }

    public void stopListening() {
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }
}
