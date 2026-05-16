import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

/**
 * Cloud Function triggered on Firestore document creation in /notifications collection.
 * Sends an FCM message to the target user's device token.
 */
export const sendNotificationOnBookingStatusChange = functions.firestore
  .document("notifications/{docId}")
  .onCreate(async (snap, context) => {
    const notificationData = snap.data();

    // Extract fields from notification document
    const toUserId = notificationData.toUserId as string;
    const fromUserId = notificationData.fromUserId as string;
    const type = notificationData.type as string;
    const status = notificationData.status as string;
    const bookingId = notificationData.bookingId as string;

    if (!toUserId || !type) {
      console.log("Missing required fields. Skipping notification.");
      return;
    }

    try {
      // Fetch the target user's device tokens from Firestore
      const userDoc = await admin
        .firestore()
        .collection("users")
        .doc(toUserId)
        .get();

      if (!userDoc.exists) {
        console.log(`User ${toUserId} not found. Skipping.`);
        return;
      }

      const userData = userDoc.data();
      const deviceTokens = userData?.deviceTokens || [];

      if (!Array.isArray(deviceTokens) || deviceTokens.length === 0) {
        console.log(`User ${toUserId} has no device tokens. Skipping.`);
        return;
      }

      // Construct FCM message payload
      let title = "Booking Update";
      let body = "Your booking status has been updated.";

      if (type === "booking_status_changed") {
        if (status === "confirmed") {
          title = "Booking Accepted!";
          body = "Your tutor has accepted your booking request.";
        } else if (status === "cancelled") {
          title = "Booking Rejected";
          body = "Your tutor has rejected your booking request.";
        }
      }

      const message: admin.messaging.MulticastMessage = {
        tokens: deviceTokens,
        notification: {
          title,
          body,
        },
        data: {
          type,
          bookingId,
          fromUserId,
          status,
        },
      };

      // Send multicast (to all device tokens for the user)
      const response = await admin.messaging().sendMulticast(message);
      console.log(`Sent ${response.successCount} notifications.`);

      if (response.failureCount > 0) {
        console.log(`Failed to send ${response.failureCount} notifications.`);
      }
    } catch (error) {
      console.error("Error sending notification:", error);
    }
  });
