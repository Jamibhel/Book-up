const functions = require('firebase-functions');
const admin = require('firebase-admin');
const { OpenAI } = require('openai');
const { VertexAI } = require('@google-cloud/vertexai');

admin.initializeApp();

const openai = new OpenAI({
    apiKey: process.env.OPENAI_API_KEY
});

const vertexAi = new VertexAI({
    project: process.env.GOOGLE_CLOUD_PROJECT,
    location: 'us-central1',
});

exports.getAITutorResponse = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be logged in');
    }

    const { message, subject, userId, messageHistory } = data;

    try {
        // Use OpenAI for AI responses
        const response = await openai.chat.completions.create({
            model: "gpt-4",
            messages: [
                {
                    role: "system",
                    content: `You are an expert tutor in ${subject}. Provide clear, educational responses 
                             that help students understand concepts deeply. Include examples and explanations 
                             that are appropriate for their level.`
                },
                ...messageHistory,
                {
                    role: "user",
                    content: message
                }
            ],
            temperature: 0.7,
            max_tokens: 1000
        });

        // Store the interaction in Firestore
        await admin.firestore()
            .collection('aiChats')
            .doc(userId)
            .collection(subject)
            .add({
                messageText: response.choices[0].message.content,
                isAi: true,
                timestamp: admin.firestore.FieldValue.serverTimestamp(),
                subject: subject,
                userId: userId
            });

        return {
            response: response.choices[0].message.content
        };
    } catch (error) {
        console.error('AI response error:', error);
        throw new functions.https.HttpsError('internal', 'Error getting AI response');
    }
});

// Function to handle image processing in chats
exports.processImageMessage = functions.storage.object().onFinalize(async (object) => {
    if (!object.contentType.startsWith('image/')) return;

    const filePath = object.name;
    if (!filePath.startsWith('chat_images/')) return;

    try {
        // Generate thumbnail
        const thumbnail = await admin.storage()
            .bucket(object.bucket)
            .file(filePath)
            .download();

        // Upload thumbnail
        const thumbnailPath = filePath.replace('chat_images/', 'chat_thumbnails/');
        await admin.storage()
            .bucket(object.bucket)
            .file(thumbnailPath)
            .save(thumbnail);

        // Update message in Firestore with thumbnail URL
        const messageId = filePath.split('/').pop().split('.')[0];
        await admin.firestore()
            .collectionGroup('messages')
            .where('imageId', '==', messageId)
            .get()
            .then(snapshot => {
                snapshot.forEach(doc => {
                    doc.ref.update({
                        thumbnailUrl: `https://storage.googleapis.com/${object.bucket}/${thumbnailPath}`
                    });
                });
            });
    } catch (error) {
        console.error('Error processing image:', error);
    }
});

// Function to handle push notifications for new messages
exports.sendMessageNotification = functions.firestore
    .document('chatChannels/{channelId}/messages/{messageId}')
    .onCreate(async (snap, context) => {
        const message = snap.data();
        const channelId = context.params.channelId;

        try {
            // Get channel details
            const channelDoc = await admin.firestore()
                .collection('chatChannels')
                .doc(channelId)
                .get();

            const channel = channelDoc.data();
            const recipients = channel.participantIds.filter(id => id !== message.senderId);

            // Get recipient tokens
            const tokens = await admin.firestore()
                .collection('users')
                .where('uid', 'in', recipients)
                .get()
                .then(snapshot => {
                    return snapshot.docs
                        .map(doc => doc.data().fcmToken)
                        .filter(token => token);
                });

            if (tokens.length === 0) return;

            // Send notification
            const notification = {
                title: message.senderName,
                body: message.messageType === 'text' ? message.messageText : 'Sent an image',
                clickAction: 'OPEN_CHAT_ACTIVITY'
            };

            const payload = {
                notification,
                data: {
                    channelId: channelId,
                    messageId: snap.id,
                    type: 'new_message'
                }
            };

            await admin.messaging().sendToDevice(tokens, payload);
        } catch (error) {
            console.error('Error sending notification:', error);
        }
    });

// Function to update chat channels when a new message is added
exports.updateChatChannel = functions.firestore
    .document('chatChannels/{channelId}/messages/{messageId}')
    .onCreate(async (snap, context) => {
        const message = snap.data();
        const channelId = context.params.channelId;

        try {
            await admin.firestore()
                .collection('chatChannels')
                .doc(channelId)
                .update({
                    lastMessage: message.messageType === 'text' ? message.messageText : '[Image]',
                    lastMessageTimestamp: message.timestamp,
                    [`unreadCount.${message.senderId}`]: admin.firestore.FieldValue.increment(1)
                });
        } catch (error) {
            console.error('Error updating chat channel:', error);
        }
    });