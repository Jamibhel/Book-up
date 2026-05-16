import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';
import { getMessaging } from 'firebase/messaging';

const firebaseConfig = {
  apiKey: "AIzaSyD5DjJD56FPrf81_JePZ4sYwCLrX-qWtuQ",
  authDomain: "book-up-ishola.firebaseapp.com",
  projectId: "book-up-ishola",
  storageBucket: "book-up-ishola.firebasestorage.app",
  messagingSenderId: "857578147513",
  appId: "1:857578147513:web:placeholder"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);

let messagingInstance = null;
if (typeof window !== 'undefined' && 'serviceWorker' in navigator) {
  try {
    messagingInstance = getMessaging(app);
  } catch (e) {
    console.warn("Firebase Messaging not supported:", e);
  }
}
export const messaging = messagingInstance;
