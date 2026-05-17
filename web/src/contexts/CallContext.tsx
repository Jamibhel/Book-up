import { createContext, useContext, useState, useEffect, useRef } from 'react';
import { useAuth } from './AuthContext';
import { 
  collection, 
  query, 
  where, 
  onSnapshot, 
  addDoc, 
  doc, 
  updateDoc, 
  serverTimestamp,
  getDoc,
  deleteDoc
} from 'firebase/firestore';
import { getFunctions, httpsCallable } from 'firebase/functions';
import { db } from '../lib/firebase';
import AgoraRTC from 'agora-rtc-sdk-ng';
import type { IAgoraRTCClient, ICameraVideoTrack, IMicrophoneAudioTrack, IRemoteVideoTrack, IRemoteAudioTrack } from 'agora-rtc-sdk-ng';
import CallOverlay from '../components/CallOverlay';
import IncomingCallUI from '../components/IncomingCallUI';

const AGORA_APP_ID = "cae7a5275c7a4283a32df9bdd13f8a47";
const WEB_UID = 2000;

AgoraRTC.setLogLevel(1);

interface CallSession {
  id: string;
  callerId: string;
  callerName: string;
  callerPhotoUrl?: string;
  receiverId: string;
  receiverName: string;
  receiverPhotoUrl?: string;
  status: 'DIALING' | 'CONNECTED' | 'REJECTED' | 'ENDED' | 'MISSED';
  type: 'VOICE' | 'VIDEO';
  channelName: string;
  chatId?: string;
  timestamp: any;
}

interface CallContextType {
  activeCall: CallSession | null;
  incomingCall: CallSession | null;
  localVideoTrack: ICameraVideoTrack | null;
  remoteVideoTrack: IRemoteVideoTrack | null;
  remoteAudioTrack: IRemoteAudioTrack | null;
  isMuted: boolean;
  isVideoDisabled: boolean;
  initiateCall: (receiverId: string, receiverName: string, receiverPhoto: string, type: 'VOICE' | 'VIDEO', chatId?: string) => Promise<void>;
  acceptCall: () => Promise<void>;
  rejectCall: () => Promise<void>;
  endCall: () => Promise<void>;
  toggleMute: () => void;
  toggleVideo: () => void;
}

const CallContext = createContext<CallContextType | null>(null);

export function useCall() {
  const context = useContext(CallContext);
  if (!context) throw new Error('useCall must be used within a CallProvider');
  return context;
}

export function CallProvider({ children }: { children: React.ReactNode }) {
  const { currentUser, userProfile } = useAuth();
  const [activeCall, setActiveCall] = useState<CallSession | null>(null);
  const [incomingCall, setIncomingCall] = useState<CallSession | null>(null);
  const [peerData, setPeerData] = useState<{name: string, photo?: string} | null>(null);

  const [localAudioTrack, setLocalAudioTrack] = useState<IMicrophoneAudioTrack | null>(null);
  const [localVideoTrack, setLocalVideoTrack] = useState<ICameraVideoTrack | null>(null);
  const [remoteVideoTrack, setRemoteVideoTrack] = useState<IRemoteVideoTrack | null>(null);
  const [remoteAudioTrack, setRemoteAudioTrack] = useState<IRemoteAudioTrack | null>(null);
  
  const [isMuted, setIsMuted] = useState(false);
  const [isVideoDisabled, setIsVideoDisabled] = useState(false);
  const callStartTimeRef = useRef<number>(0);
  const isJoiningRef = useRef<boolean>(false);
  const clientRef = useRef<IAgoraRTCClient | null>(null);

  useEffect(() => {
    if (!clientRef.current) {
        console.log("[CallStep 1] Initializing Agora client...");
        const agoraClient = AgoraRTC.createClient({ mode: "rtc", codec: "vp8" });
        clientRef.current = agoraClient;

        agoraClient.on("user-published", async (user, mediaType) => {
          console.log("[CallStep 7] Remote media published:", user.uid, mediaType);
          await agoraClient.subscribe(user, mediaType);
          if (mediaType === "video") setRemoteVideoTrack(user.videoTrack || null);
          if (mediaType === "audio") {
            setRemoteAudioTrack(user.audioTrack || null);
            try {
              user.audioTrack?.play();
            } catch (err) {
              console.error("Failed to play audio track:", err);
            }
          }
        });

        agoraClient.on("user-unpublished", (user, mediaType) => {
          if (mediaType === "video") setRemoteVideoTrack(null);
          if (mediaType === "audio") setRemoteAudioTrack(null);
        });
    }

    return () => {
      if (clientRef.current) {
          clientRef.current.removeAllListeners();
          clientRef.current.leave().catch(() => {});
          clientRef.current = null;
      }
    };
  }, []);

  useEffect(() => {
    if (!currentUser) return;
    const q = query(
      collection(db, 'calls'),
      where('receiverId', '==', currentUser.uid),
      where('status', '==', 'DIALING')
    );
    const unsubscribe = onSnapshot(q, 
      (snapshot) => {
        if (!snapshot.empty) {
          const docSnap = snapshot.docs[0];
          const data = docSnap.data();
          if (data.callerId !== currentUser.uid && !activeCall) {
              console.log("[CallStep 2b] Real incoming call detected.");
              setIncomingCall({ ...data, id: docSnap.id } as CallSession);
          }
        } else {
          setIncomingCall(null);
        }
      },
      (error) => {
        console.error("CallContext incoming calls snapshot error:", error);
      }
    );
    return () => unsubscribe();
  }, [currentUser, activeCall]);

  useEffect(() => {
    if (!activeCall?.id) return;
    const unsubscribe = onSnapshot(doc(db, 'calls', activeCall.id), 
      async (docSnap) => {
        if (docSnap.exists()) {
          const data = docSnap.data();
          const status = (data.status || '').toUpperCase();
          
          if (status === 'REJECTED' || status === 'ENDED' || status === 'MISSED') {
            cleanupAgora();
            setActiveCall(null);
            setTimeout(async () => {
               try { await deleteDoc(doc(db, 'calls', activeCall.id)); } catch(e) {}
            }, 5000);
          } else if (status === 'CONNECTED' && activeCall.status === 'DIALING') {
            setActiveCall(prev => prev ? { ...prev, status: 'CONNECTED' } : null);
            if (callStartTimeRef.current === 0) callStartTimeRef.current = Date.now();
            console.log("[CallStep 6] Remote device connected. Audio/Video should flow.");
          }
        } else {
          cleanupAgora();
          setActiveCall(null);
        }
      },
      (error) => {
        console.error("CallContext active call snapshot error:", error);
      }
    );
    return () => unsubscribe();
  }, [activeCall?.id, activeCall?.status]);

  useEffect(() => {
    if (!activeCall) {
      setPeerData(null);
      return;
    }
    const fetchPeerData = async () => {
      const peerId = activeCall.callerId === currentUser?.uid ? activeCall.receiverId : activeCall.callerId;
      try {
        const userDoc = await getDoc(doc(db, 'users', peerId));
        if (userDoc.exists()) {
          const data = userDoc.data();
          setPeerData({
            name: data.displayName || data.name || 'User',
            photo: data.photoURL || data.photoUrl || ''
          });
        }
      } catch (err) {
        console.error("Failed to fetch peer data:", err);
      }
    };
    fetchPeerData();
  }, [activeCall?.id, currentUser?.uid]);

  const joinAgoraChannel = async (channelName: string, type: 'VOICE' | 'VIDEO') => {
    const activeClient = clientRef.current;
    if (!activeClient || isJoiningRef.current) return;
    isJoiningRef.current = true;

    try {
      cleanupAgora();

      const functions = getFunctions(undefined, 'africa-south1');
      const generateToken = httpsCallable(functions, 'generateAgoraToken');
      const result = await generateToken({ channelName, uid: WEB_UID });
      const { token }: any = result.data;

      if (!token) throw new Error("Security token failed");

      const audioTrack = await AgoraRTC.createMicrophoneAudioTrack();
      setLocalAudioTrack(audioTrack);

      let videoTrack = null;
      if (type === 'VIDEO') {
        try {
          videoTrack = await AgoraRTC.createCameraVideoTrack();
          setLocalVideoTrack(videoTrack);
        } catch (vErr) { console.error("Camera fail:", vErr); }
      }

      await activeClient.join(AGORA_APP_ID, channelName, token, WEB_UID);
      if (videoTrack) await activeClient.publish([audioTrack, videoTrack]);
      else await activeClient.publish([audioTrack]);
      
      console.log("[CallStep 5b] Published media tracks.");
    } catch (err) {
      console.error("Agora join failed:", err);
      alert("Media permission denied or connection lost.");
      endCall();
    } finally {
      isJoiningRef.current = false;
    }
  };

  const cleanupAgora = () => {
    localAudioTrack?.stop();
    localAudioTrack?.close();
    localVideoTrack?.stop();
    localVideoTrack?.close();
    setLocalAudioTrack(null);
    setLocalVideoTrack(null);
    setRemoteVideoTrack(null);
    setRemoteAudioTrack(null);
    clientRef.current?.leave().catch(() => {});
    callStartTimeRef.current = 0;
  };

  const initiateCall = async (receiverId: string, receiverName: string, receiverPhoto: string, type: 'VOICE' | 'VIDEO', chatId?: string) => {
    if (!currentUser) return;

    try {
        console.log("[CallStep 2] Requesting permissions...");
        const aTrack = await AgoraRTC.createMicrophoneAudioTrack();
        setLocalAudioTrack(aTrack);
        
        let vTrack = null;
        if (type === 'VIDEO') {
            try {
                vTrack = await AgoraRTC.createCameraVideoTrack();
                setLocalVideoTrack(vTrack);
            } catch(e) { console.error("Camera fail:", e); }
        }

        const channelName = `call_${currentUser.uid}_${Date.now()}`;
        
        const functions = getFunctions(undefined, 'africa-south1');
        const generateToken = httpsCallable(functions, 'generateAgoraToken');
        const tokenResult = await generateToken({ channelName, uid: WEB_UID });
        const { token }: any = tokenResult.data;

        const callData: any = {
          callerId: currentUser.uid,
          callerName: userProfile?.displayName || currentUser.displayName || 'User',
          callerPhotoUrl: userProfile?.photoURL || currentUser.photoURL || '',
          receiverId, receiverName, receiverPhotoUrl: receiverPhoto,
          status: 'DIALING', type, channelName, chatId: chatId || "", timestamp: serverTimestamp()
        };

        const docRef = await addDoc(collection(db, 'calls'), callData);
        setActiveCall({ ...callData, id: docRef.id } as CallSession);

        await clientRef.current?.join(AGORA_APP_ID, channelName, token, WEB_UID);
        if (vTrack) await clientRef.current?.publish([aTrack, vTrack]);
        else await clientRef.current?.publish([aTrack]);
        console.log("[CallStep 5] Caller ready.");

    } catch (err) {
        console.error("Initiate fail:", err);
        alert("Please Allow Camera and Microphone permissions in your browser to start a call.");
        cleanupAgora();
    }
  };

  const acceptCall = async () => {
    if (!incomingCall) return;
    try {
        await joinAgoraChannel(incomingCall.channelName, incomingCall.type);
        await updateDoc(doc(db, 'calls', incomingCall.id), { status: 'CONNECTED' });
        setActiveCall({ ...incomingCall, status: 'CONNECTED' });
        setIncomingCall(null);
        if (callStartTimeRef.current === 0) callStartTimeRef.current = Date.now();
    } catch (err) {
        console.error("Accept fail:", err);
    }
  };

  const rejectCall = async () => {
    if (!incomingCall) return;
    try { await updateDoc(doc(db, 'calls', incomingCall.id), { status: 'REJECTED' }); } catch (e) {}
    setIncomingCall(null);
  };

  const logCallToChat = async (session: CallSession, status: string) => {
    if (!session.chatId || !currentUser) return;
    const duration = (callStartTimeRef.current > 0) ? Math.floor((Date.now() - callStartTimeRef.current) / 1000) : 0;
    const typeName = session.type === 'VIDEO' ? 'Video call' : 'Voice call';
    let messageText = '';
    const isIncoming = session.receiverId === currentUser.uid;
    switch (status) {
      case 'REJECTED': messageText = isIncoming ? `Declined ${typeName}` : `${typeName} rejected`; break;
      case 'MISSED': messageText = `Missed ${typeName}`; break;
      case 'ENDED':
        if (duration > 0) {
          const mins = Math.floor(duration / 60);
          const secs = duration % 60;
          messageText = `${typeName} ended - ${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
        } else messageText = isIncoming ? `Missed ${typeName}` : 'No answer';
        break;
    }
    if (messageText) {
      try {
        await addDoc(collection(db, `conversations/${session.chatId}/messages`), {
          text: messageText, content: messageText, senderId: currentUser.uid, senderName: userProfile?.displayName || currentUser.displayName || 'User',
          type: 'CALL', messageType: 'call', timestamp: serverTimestamp()
        });
        await updateDoc(doc(db, 'conversations', session.chatId), {
          lastMessage: messageText, lastMessageSenderId: currentUser.uid, lastMessageTimestamp: serverTimestamp()
        });
      } catch (err) {}
    }
  };

  const endCall = async () => {
    const call = activeCall || incomingCall;
    if (call?.id) {
        const status = activeCall?.status === 'CONNECTED' ? 'ENDED' : 'REJECTED';
        await updateDoc(doc(db, 'calls', call.id), { status }).catch(() => {});
        logCallToChat(call, status);
    }
    cleanupAgora();
    setActiveCall(null);
    setIncomingCall(null);
  };

  const toggleMute = async () => {
    if (localAudioTrack) {
      const target = !isMuted;
      await localAudioTrack.setEnabled(!target);
      setIsMuted(target);
    }
  };

  const toggleVideo = async () => {
    if (localVideoTrack) {
        const target = !isVideoDisabled;
        await localVideoTrack.setEnabled(!target);
        setIsVideoDisabled(target);
    }
  };

  return (
    <CallContext.Provider value={{ activeCall, incomingCall, localVideoTrack, remoteVideoTrack, remoteAudioTrack, isMuted, isVideoDisabled, initiateCall, acceptCall, rejectCall, endCall, toggleMute, toggleVideo }}>
      {children}
      {incomingCall && (
        <IncomingCallUI 
          callerName={incomingCall.callerName}
          callerPhoto={incomingCall.callerPhotoUrl}
          type={incomingCall.type}
          onAccept={acceptCall}
          onReject={rejectCall}
        />
      )}
      {activeCall && (
        <CallOverlay
          type={activeCall.type}
          peerName={peerData?.name || (activeCall.callerId === currentUser?.uid ? activeCall.receiverName : activeCall.callerName)}
          peerPhoto={peerData?.photo || (activeCall.callerId === currentUser?.uid ? activeCall.receiverPhotoUrl : activeCall.callerPhotoUrl)}
          localVideoTrack={localVideoTrack}
          remoteVideoTrack={remoteVideoTrack}
          remoteAudioTrack={remoteAudioTrack}
          isMuted={isMuted}
          isVideoDisabled={isVideoDisabled}
          onEndCall={endCall}
          onToggleMute={toggleMute}
          onToggleVideo={toggleVideo}
          onFlipCamera={() => {}}
        />
      )}
    </CallContext.Provider>
  );
}
