import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword,
  updateProfile,
  signOut, 
  onAuthStateChanged,
  GoogleAuthProvider,
  signInWithPopup,
  signInWithRedirect,
  getRedirectResult
} from 'firebase/auth';
import type { User } from 'firebase/auth';
import { doc, setDoc, getDoc, serverTimestamp, onSnapshot, updateDoc } from 'firebase/firestore';
import { getToken } from 'firebase/messaging';
import { auth, db, messaging } from '../lib/firebase';

interface UserProfile {
  id: string;
  email: string;
  displayName: string;
  role: string;
  isAdmin?: boolean;
  photoURL?: string;
  photoUrl?: string;
}

interface AuthContextType {
  currentUser: User | null;
  userProfile: UserProfile | null;
  loading: boolean;
  login: (email: string, pass: string) => Promise<void>;
  signup: (email: string, pass: string, name: string, role: string) => Promise<void>;
  loginWithGoogle: () => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  const handleUserSignInResult = async (user: any) => {
    const userRef = doc(db, 'users', user.uid);
    const docSnap = await getDoc(userRef);
    if (!docSnap.exists()) {
      await setDoc(userRef, {
        id: user.uid,
        email: user.email,
        displayName: user.displayName || 'Google User',
        role: 'student', // default
        isAdmin: false,
        createdAt: serverTimestamp(),
        photoURL: user.photoURL || ''
      });
    }
  };

  useEffect(() => {
    // Process Google redirect result if any
    getRedirectResult(auth)
      .then(async (result) => {
        if (result && result.user) {
          await handleUserSignInResult(result.user);
        }
      })
      .catch((err) => {
        console.error("Redirect sign-in failed:", err);
      });

    const unsubscribe = onAuthStateChanged(auth, (user) => {
      setCurrentUser(user);
      if (user) {
        // Handle FCM token for web notifications
        const setupFCM = async () => {
          if (!messaging) return;
          try {
            const permission = await Notification.requestPermission();
            if (permission === 'granted') {
              const token = await getToken(messaging, {
                vapidKey: 'BCm569tX1yL3973_HjZ_y91-Z0vWqYv6-z6Vz-Y_X_Z_Z_Y_X_Z_Z_Y'
              });
              if (token) {
                await updateDoc(doc(db, 'users', user.uid), {
                  fcmToken: token,
                  lastTokenUpdate: serverTimestamp()
                });
              }
            }
          } catch (err) {
            console.error('Failed to setup FCM token:', err);
          }
        };
        setupFCM();

        // Listen to user profile changes in real-time
        const unsubProfile = onSnapshot(doc(db, 'users', user.uid), 
          (doc) => {
            if (doc.exists()) {
              setUserProfile(doc.data() as UserProfile);
            } else {
              setUserProfile(null);
            }
            setLoading(false);
          },
          (error) => {
            console.error("AuthContext profile load error:", error);
            setUserProfile(null);
            setLoading(false);
          }
        );
        return () => unsubProfile();
      } else {
        setUserProfile(null);
        setLoading(false);
      }
    });

    return unsubscribe;
  }, []);

  const login = async (email: string, pass: string) => {
    await signInWithEmailAndPassword(auth, email, pass);
  };

  const signup = async (email: string, pass: string, name: string, role: string) => {
    const userCredential = await createUserWithEmailAndPassword(auth, email, pass);
    await updateProfile(userCredential.user, { displayName: name });
    
    // Save user to Firestore
    await setDoc(doc(db, 'users', userCredential.user.uid), {
      id: userCredential.user.uid,
      email: email,
      displayName: name,
      role: role.toLowerCase(),
      isAdmin: false, // Default
      createdAt: serverTimestamp(),
      photoURL: ''
    });
  };

  const loginWithGoogle = async () => {
    const provider = new GoogleAuthProvider();
    provider.setCustomParameters({
      prompt: 'select_account'
    });
    
    try {
      const result = await signInWithPopup(auth, provider);
      if (result && result.user) {
        await handleUserSignInResult(result.user);
      }
    } catch (err: any) {
      console.warn("signInWithPopup failed, trying redirect fallback:", err);
      // Fallback for popups blocked, iframe restriction, or mobile Safari
      if (
        err.code === 'auth/popup-blocked' || 
        err.code === 'auth/popup-closed-by-user' ||
        err.code === 'auth/cancelled-popup-request' ||
        /iPad|iPhone|iPod|Android/.test(navigator.userAgent)
      ) {
        await signInWithRedirect(auth, provider);
      } else {
        throw err;
      }
    }
  };

  const logout = async () => {
    await signOut(auth);
  };

  const value = {
    currentUser,
    userProfile,
    loading,
    login,
    signup,
    loginWithGoogle,
    logout
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}
