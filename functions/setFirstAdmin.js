const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

// Initialize Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Function to set a user as admin
async function setFirstAdmin(email) {
  try {
    // Get user by email
    const userRecord = await admin.auth().getUserByEmail(email);
    
    // Update Firestore user document
    await admin.firestore().collection('users').doc(userRecord.uid).update({
      isAdmin: true,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    console.log(`Successfully set user ${email} as admin`);
  } catch (error) {
    console.error('Error setting admin:', error);
  }
}

// Replace with the email of the user you want to make admin
const adminEmail = 'admin@example.com';  // REPLACE THIS WITH YOUR ADMIN'S EMAIL
setFirstAdmin(adminEmail);