const { onRequest, onCall } = require("firebase-functions/v2/https");
const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");
const { OpenAI } = require("openai");
const { RtcTokenBuilder, RtcRole } = require("agora-token");

admin.initializeApp();

const AGORA_APP_ID = "cae7a5275c7a4283a32df9bdd13f8a47";

setGlobalOptions({
    region: "africa-south1",
    timeoutSeconds: 60,
    memory: "256MiB"
});

// --- AGORA TOKEN GENERATOR ---
exports.generateAgoraToken = onCall({ secrets: ["AGORA_APP_CERTIFICATE"] }, async (request) => {
    const { channelName, uid } = request.data;
    if (!request.auth) throw new Error("Unauthenticated");

    const appCertificate = process.env.AGORA_APP_CERTIFICATE;
    if (!appCertificate) {
        throw new Error("AGORA_APP_CERTIFICATE not found in environment.");
    }

    const role = RtcRole.PUBLISHER;
    const expirationTimeInSeconds = 3600; // 1 hour
    const currentTimestamp = Math.floor(Date.now() / 1000);
    const privilegeExpiredTs = currentTimestamp + expirationTimeInSeconds;

    const finalUid = uid || 0;

    try {
        const token = RtcTokenBuilder.buildTokenWithUid(
            AGORA_APP_ID,
            appCertificate,
            channelName,
            finalUid,
            role,
            privilegeExpiredTs,
            privilegeExpiredTs
        );

        return { token, uid: finalUid };
    } catch (error) {
        console.error("Agora Token Error:", error);
        return { error: error.message };
    }
});

// --- CHAT NOTIFICATIONS ---
exports.onNewMessage = onDocumentCreated("conversations/{channelId}/messages/{messageId}", async (event) => {
    const message = event.data.data();
    const channelId = event.params.channelId;

    const channelDoc = await admin.firestore().collection("conversations").doc(channelId).get();
    if (!channelDoc.exists) return;
    const channel = channelDoc.data();

    const recipientId = channel.participantIds.find(id => id !== message.senderId);
    if (!recipientId) return;

    let body = message.type === "TEXT" ? message.text : `Sent a ${message.type.toLowerCase()}`;

    await sendToUser(recipientId, {
        notification: { title: message.senderName || "New Message", body },
        data: { channelId, type: "CHAT_MESSAGE" }
    });
});

// --- CALL NOTIFICATIONS ---
exports.onNewCall = onDocumentCreated("calls/{callId}", async (event) => {
    const call = event.data.data();
    if (!call || call.status !== "DIALING") return;

    const recipientId = call.receiverId;
    if (!recipientId) return;

    // Send DATA-ONLY high priority message for background ringing reliability
    await sendToUser(recipientId, {
        data: {
            type: "INCOMING_CALL",
            title: `Incoming ${call.type.toLowerCase()} call`,
            body: `${call.callerName} is calling you...`,
            callId: event.params.callId,
            callerId: call.callerId,
            callerName: call.callerName,
            callerPhotoUrl: call.callerPhotoUrl || "",
            channelName: call.channelName,
            callType: call.type,
            chatId: call.chatId || ""
        }
    });
});

// --- BOOKING NOTIFICATIONS ---
exports.onBookingUpdate = onDocumentUpdated("bookings/{bookingId}", async (event) => {
    const before = event.data.before.data();
    const after = event.data.after.data();

    if (before.status === after.status) return;

    const studentId = after.userId;
    const status = after.status;
    const subject = after.subject || "session";

    let title = "Booking Update";
    let body = `Your booking status is now: ${status}`;

    if (status === "confirmed") {
        title = "Booking Accepted! 🎉";
        body = `Your ${subject} session has been confirmed.`;
    } else if (status === "cancelled") {
        title = "Booking Rejected ❌";
        body = `Your ${subject} session was not accepted.`;
    }

    await sendToUser(studentId, {
        notification: { title, body },
        data: { type: "BOOKING_UPDATE", bookingId: event.params.bookingId }
    });

    await admin.firestore()
        .collection("notifications")
        .doc(studentId)
        .collection("messages")
        .add({
            type: "booking_status_changed",
            status: status,
            subject: subject,
            bookingId: event.params.bookingId,
            timestamp: admin.firestore.FieldValue.serverTimestamp()
        });
});

// --- AI TUTOR ---
exports.getAITutorResponse = onCall({ secrets: ["OPENAI_API_KEY"] }, async (request) => {
    const { message, subject, userId, messageHistory } = request.data;
    if (!request.auth) throw new Error("Unauthenticated");

    try {
        const openai = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });
        const response = await openai.chat.completions.create({
            model: "gpt-4",
            messages: [
                { role: "system", content: `You are an expert tutor in ${subject}.` },
                ...messageHistory,
                { role: "user", content: message }
            ],
            temperature: 0.7
        });

        const aiText = response.choices[0].message.content;
        await admin.firestore().collection("ai_chat_messages").add({
            messageText: aiText,
            role: "assistant",
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
            subject: subject,
            userId: userId,
            isMarkdown: true
        });

        return { response: aiText };
    } catch (error) {
        console.error("AI Error:", error);
        return { error: error.message };
    }
});

// --- HELPER: Send Multicast ---
async function sendToUser(userId, payload) {
    const userDoc = await admin.firestore().collection("users").doc(userId).get();
    if (!userDoc.exists) return;

    const data = userDoc.data();
    const tokens = [];
    if (data.fcmToken) tokens.push(data.fcmToken);
    if (data.deviceTokens) tokens.push(...data.deviceTokens);

    const uniqueTokens = [...new Set(tokens)].filter(t => t);
    if (uniqueTokens.length === 0) return;

    await admin.messaging().sendEachForMulticast({
        tokens: uniqueTokens,
        notification: payload.notification,
        data: payload.data,
        android: {
            priority: "high"
        }
    });
}
