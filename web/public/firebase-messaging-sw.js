importScripts('https://www.gstatic.com/firebasejs/10.13.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.13.0/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: "AIzaSyD5DjJD56FPrf81_JePZ4sYwCLrX-qWtuQ",
  authDomain: "book-up-ishola.firebaseapp.com",
  projectId: "book-up-ishola",
  storageBucket: "book-up-ishola.firebasestorage.app",
  messagingSenderId: "857578147513",
  appId: "1:857578147513:web:placeholder"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
  console.log('[firebase-messaging-sw.js] Received background message ', payload);
  const notificationTitle = payload.notification.title;
  const notificationOptions = {
    body: payload.notification.body,
    icon: '/favicon.svg',
    data: payload.data
  };

  self.registration.showNotification(notificationTitle, notificationOptions);
});
