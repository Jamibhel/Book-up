import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useCall } from '../contexts/CallContext';
import { collection, query, where, doc, addDoc, updateDoc, onSnapshot, orderBy, serverTimestamp, getDoc, getDocs, limit, setDoc } from 'firebase/firestore';
import { ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage';
import { db, storage } from '../lib/firebase';
import { Search, MessageSquare, Phone, Video, MoreVertical, Send, Paperclip, Mic, PlusCircle, StopCircle, X, Download, Maximize2, ArrowLeft, User as UserIcon } from 'lucide-react';
import React from 'react';
import { useNavigate } from 'react-router-dom';
import CallLog from '../components/CallLog';

interface ChatChannel {
  id: string;
  isGroup: boolean;
  groupName?: string;
  groupImage?: string;
  lastMessage?: string;
  lastMessageTimestamp?: any;
  participantIds: string[];
  participantNames?: Record<string, string>;
  participantPhotos?: Record<string, string>;
}

interface ChatMessage {
  id: string;
  content?: string;
  text?: string;
  senderId: string;
  senderName: string;
  timestamp: any;
  messageType?: string; // "text", "image", "file", "audio"
  type?: string; // Legacy Android type field
  mediaUrl?: string;
}

export default function Messages() {
  const { currentUser } = useAuth();
  const { initiateCall } = useCall();
  const navigate = useNavigate();
  const [channels, setChannels] = useState<ChatChannel[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeChannel, setActiveChannel] = useState<ChatChannel | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [uploading, setUploading] = useState(false);
  const [recording, setRecording] = useState(false);
  const [mediaRecorder, setMediaRecorder] = useState<MediaRecorder | null>(null);
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const [userPhotos, setUserPhotos] = useState<Record<string, string>>({});
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const [isNewChatModalOpen, setIsNewChatModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [allUsers, setAllUsers] = useState<any[]>([]);
  const [loadingUsers, setLoadingUsers] = useState(false);

  useEffect(() => {
    if (isNewChatModalOpen) {
      fetchUsers();
    }
  }, [isNewChatModalOpen]);

  const fetchUsers = async () => {
    setLoadingUsers(true);
    try {
      const q = query(collection(db, 'users'), limit(50));
      const snap = await getDocs(q);
      const users = snap.docs
        .map(d => ({ id: d.id, ...d.data() }))
        .filter(u => u.id !== currentUser?.uid);
      setAllUsers(users);
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingUsers(false);
    }
  };

  const startNewChat = async (targetUser: any) => {
    if (!currentUser) return;
    try {
      const channelId = [currentUser.uid, targetUser.id].sort().join('_');
      const channelRef = doc(db, 'conversations', channelId);
      const snap = await getDoc(channelRef);
      const channelData = {
        id: channelId,
        participantIds: [currentUser.uid, targetUser.id],
        participantNames: {
          [currentUser.uid]: currentUser.displayName || currentUser.email || 'User',
          [targetUser.id]: targetUser.displayName || targetUser.name || 'User'
        },
        participantPhotos: {
          [currentUser.uid]: currentUser.photoURL || '',
          [targetUser.id]: targetUser.photoUrl || targetUser.photoURL || ''
        },
        isGroup: false,
        lastMessage: '',
        lastMessageTimestamp: serverTimestamp()
      };

      if (!snap.exists()) {
        await setDoc(channelRef, channelData);
      }
      
      setActiveChannel(snap.exists() ? { ...snap.data(), id: snap.id } as ChatChannel : channelData as ChatChannel);
      setIsNewChatModalOpen(false);
    } catch (err) {
      console.error(err);
      alert("Failed to start chat.");
    }
  };

  const filteredUsers = allUsers.filter(u => 
    (u.displayName || u.name || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  useEffect(() => {
    if (!currentUser) return;
    
    setLoading(true);
    const channelMap = new Map<string, ChatChannel>();
    
    const qModern = query(collection(db, 'conversations'), where('participantIds', 'array-contains', currentUser.uid));
    const qLegacy = query(collection(db, 'chatChannels'), where('participantIds', 'array-contains', currentUser.uid));
    
    const updateChannels = (items: ChatChannel[], source: string) => {
      items.forEach(item => {
        if (!channelMap.has(item.id) || source === 'modern') {
          channelMap.set(item.id, item);
        }
      });
      
      const sorted = Array.from(channelMap.values()).sort((a, b) => {
        const t1 = a.lastMessageTimestamp?.seconds || 0;
        const t2 = b.lastMessageTimestamp?.seconds || 0;
        return t2 - t1;
      });
      
      setChannels(sorted);
      setLoading(false);
    };

    const unsubModern = onSnapshot(qModern, (snapshot) => {
      const items = snapshot.docs.map(d => ({ ...d.data(), id: d.id } as ChatChannel));
      updateChannels(items, 'modern');
    });

    const unsubLegacy = onSnapshot(qLegacy, (snapshot) => {
      const items = snapshot.docs.map(d => ({ ...d.data(), id: d.id } as ChatChannel));
      updateChannels(items, 'legacy');
    });

    return () => {
      unsubModern();
      unsubLegacy();
    };
  }, [currentUser]);

  useEffect(() => {
    const fetchMissingPhotos = async () => {
      const missingIds = new Set<string>();
      channels.forEach(channel => {
        if (channel.isGroup) return;
        const otherId = channel.participantIds?.find(id => id !== currentUser?.uid);
        if (otherId && !channel.participantPhotos?.[otherId] && !userPhotos[otherId]) {
          missingIds.add(otherId);
        }
      });

      if (missingIds.size === 0) return;

      const newPhotos = { ...userPhotos };
      for (const uid of Array.from(missingIds)) {
        try {
          const userDoc = await getDoc(doc(db, 'users', uid));
          if (userDoc.exists()) {
            const data = userDoc.data();
            newPhotos[uid] = data.photoURL || data.photoUrl || '';
          }
        } catch (err) {
          console.error(`Failed to fetch photo for ${uid}`, err);
        }
      }
      setUserPhotos(newPhotos);
    };

    if (channels.length > 0) {
      fetchMissingPhotos();
    }
  }, [channels, currentUser]);

  useEffect(() => {
    if (!activeChannel || !currentUser) {
      setMessages([]);
      return;
    }

    const fetchedMessagesMap = new Map<string, ChatMessage>();
    const qModern = query(collection(db, `conversations/${activeChannel.id}/messages`), orderBy('timestamp', 'asc'));
    const qLegacy = query(collection(db, `chatChannels/${activeChannel.id}/messages`), orderBy('timestamp', 'asc'));

    const updateMessages = () => {
      const allMsgs = Array.from(fetchedMessagesMap.values()).sort((a, b) => {
        const getTime = (ts: any) => {
          if (!ts) return Date.now() + 10000;
          if (ts.seconds) return ts.seconds * 1000;
          if (ts.toMillis) return ts.toMillis();
          if (ts.getTime) return ts.getTime();
          return 0;
        };
        return getTime(a.timestamp) - getTime(b.timestamp);
      });
      setMessages(allMsgs);
      setTimeout(scrollToBottom, 100);
    };

    const unsubModern = onSnapshot(qModern, (snapshot) => {
      snapshot.docChanges().forEach(change => {
        if (change.type === 'added' || change.type === 'modified') {
          fetchedMessagesMap.set(change.doc.id, { ...change.doc.data(), id: change.doc.id } as ChatMessage);
        }
      });
      updateMessages();
    }, (error) => {
      console.error("Modern messages listener failed:", error);
    });

    const unsubLegacy = onSnapshot(qLegacy, (snapshot) => {
      snapshot.docChanges().forEach(change => {
        if (change.type === 'added' || change.type === 'modified') {
          fetchedMessagesMap.set(change.doc.id, { ...change.doc.data(), id: change.doc.id } as ChatMessage);
        }
      });
      updateMessages();
    }, (error) => {
      console.error("Legacy messages listener failed:", error);
    });

    return () => {
      unsubModern();
      unsubLegacy();
    };
  }, [activeChannel?.id, currentUser]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const getChannelName = (channel: ChatChannel | null) => {
    if (!channel) return 'Unknown';
    if (channel.isGroup) return channel.groupName || 'Unnamed Group';
    if (!channel.participantIds) return 'Private Chat';
    const otherId = channel.participantIds.find(id => id !== currentUser?.uid);
    return channel.participantNames?.[otherId || ''] || 'Chat Member';
  };

  const getChannelPhoto = (channel: ChatChannel | null) => {
    if (!channel) return '';
    if (channel.isGroup) return channel.groupImage || '';
    if (!channel.participantIds) return '';
    const otherId = channel.participantIds.find(id => id !== currentUser?.uid);
    return channel.participantPhotos?.[otherId || ''] || userPhotos[otherId || ''] || '';
  };

  const openUserProfile = () => {
    if (!activeChannel || activeChannel.isGroup || !activeChannel.participantIds) return;
    const otherId = activeChannel.participantIds.find(id => id !== currentUser?.uid);
    if (otherId) navigate(`/user/${otherId}`);
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMessage.trim() || !activeChannel || !currentUser) return;

    const messageContent = newMessage;
    setNewMessage('');
    
    try {
      const modernRef = doc(db, 'conversations', activeChannel.id);
      const snap = await getDoc(modernRef);
      
      const targetCollection = snap.exists() ? 'conversations' : 'chatChannels';

      await addDoc(collection(db, `${targetCollection}/${activeChannel.id}/messages`), {
        text: messageContent,
        content: messageContent,
        senderId: currentUser.uid,
        senderName: currentUser.displayName || currentUser.email || 'User',
        type: 'TEXT',
        messageType: 'text',
        timestamp: serverTimestamp()
      });

      await updateDoc(doc(db, targetCollection, activeChannel.id), {
        lastMessage: messageContent,
        lastMessageSenderId: currentUser.uid,
        lastMessageTimestamp: serverTimestamp()
      });
      
      scrollToBottom();
    } catch (err) {
      console.error("Failed to send message:", err);
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || !e.target.files[0] || !activeChannel || !currentUser) return;
    const file = e.target.files[0];
    setUploading(true);
    
    try {
      const isImage = file.type.startsWith('image/');
      const folder = isImage ? 'images' : 'documents';
      const storageRef = ref(storage, `chat_media/${folder}/${activeChannel.id}/${currentUser.uid}/${Date.now()}_${file.name}`);
      const uploadTask = await uploadBytesResumable(storageRef, file);
      const downloadUrl = await getDownloadURL(uploadTask.ref);
      
      await addDoc(collection(db, `conversations/${activeChannel.id}/messages`), {
        text: isImage ? 'Sent an image' : `Sent a file: ${file.name}`,
        content: isImage ? 'Sent an image' : `Sent a file: ${file.name}`,
        senderId: currentUser.uid,
        senderName: currentUser.displayName || currentUser.email || 'User',
        type: isImage ? 'IMAGE' : 'FILE',
        messageType: isImage ? 'image' : 'file',
        mediaUrl: downloadUrl,
        timestamp: serverTimestamp()
      });
      
      scrollToBottom();
    } catch (err) {
      console.error(err);
    } finally {
      setUploading(false);
    }
  };

  const startVoiceNote = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const recorder = new MediaRecorder(stream);
      const chunks: BlobPart[] = [];
      
      recorder.ondataavailable = (e) => chunks.push(e.data);
      recorder.onstop = async () => {
        const blob = new Blob(chunks, { type: 'audio/webm' });
        await uploadAudio(blob);
      };
      
      recorder.start();
      setMediaRecorder(recorder);
      setRecording(true);
    } catch (err) {
      console.error(err);
      alert("Microphone access denied. Please enable microphone permissions in your browser settings.");
    }
  };

  const stopVoiceNote = () => {
    if (mediaRecorder && recording) {
      mediaRecorder.stop();
      setRecording(false);
      mediaRecorder.stream.getTracks().forEach(t => t.stop());
    }
  };

  const uploadAudio = async (blob: Blob) => {
    if (!activeChannel || !currentUser) return;
    setUploading(true);
    try {
      const storageRef = ref(storage, `chat_media/audio/${activeChannel.id}/${currentUser.uid}/audio_${Date.now()}.webm`);
      const uploadTask = await uploadBytesResumable(storageRef, blob);
      const downloadUrl = await getDownloadURL(uploadTask.ref);
      
      await addDoc(collection(db, `conversations/${activeChannel.id}/messages`), {
        text: '🎙️ Voice message',
        content: '🎙️ Voice message',
        senderId: currentUser.uid,
        senderName: currentUser.displayName || currentUser.email || 'User',
        type: 'AUDIO',
        messageType: 'audio',
        mediaUrl: downloadUrl,
        timestamp: serverTimestamp()
      });
    } catch (err) {
      console.error(err);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="flex h-[calc(100vh-140px)] gap-6 animate-in slide-in-from-bottom-4 duration-500">
      {/* Sidebar */}
      <div className={`w-full md:w-96 flex flex-col bg-white rounded-[2rem] shadow-sm border border-gray-100 overflow-hidden ${activeChannel ? 'hidden md:flex' : 'flex'}`}>
        <div className="p-6 border-b border-gray-50 flex items-center justify-between shrink-0">
          <h2 className="text-2xl font-black text-gray-900 font-display">Chats</h2>
          <button 
            onClick={() => setIsNewChatModalOpen(true)}
            className="p-2.5 bg-bookup-primary/10 text-bookup-primary rounded-xl hover:bg-bookup-primary hover:text-white transition-all shadow-sm"
          >
            <PlusCircle size={22} />
          </button>
        </div>
        
        <div className="p-4 shrink-0">
          <div className="relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
            <input 
              type="text" 
              placeholder="Search conversations..." 
              className="w-full bg-gray-50 border-none rounded-2xl py-3 pl-12 pr-4 focus:ring-2 focus:ring-bookup-primary/20 transition-all font-medium"
            />
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-2 space-y-1">
          {loading ? (
            <div className="flex flex-col gap-4 p-4">
              {[1,2,3,4].map(i => <div key={i} className="h-20 bg-gray-50 animate-pulse rounded-2xl" />)}
            </div>
          ) : channels.length === 0 ? (
            <div className="text-center py-10 px-4">
              <MessageSquare size={32} className="mx-auto text-gray-300 mb-3" />
              <p className="text-gray-500 font-bold">No messages yet</p>
            </div>
          ) : (
            channels.map((channel) => {
              const photo = getChannelPhoto(channel);
              const name = getChannelName(channel);
              const isActive = activeChannel?.id === channel.id;
              
              return (
                <button 
                  key={channel.id}
                  onClick={() => setActiveChannel(channel)}
                  className={`w-full flex items-center gap-4 p-4 rounded-2xl transition-all text-left group ${
                    isActive ? 'bg-bookup-primary text-white shadow-xl shadow-bookup-primary/20' : 'hover:bg-gray-50 text-gray-900'
                  }`}
                >
                  <div className="relative shrink-0">
                    <img 
                      src={photo || `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=${isActive ? 'fff' : '2E8B57'}\&color=${isActive ? '2E8B57' : 'fff'}\&bold=true&size=128`} 
                      alt={name} 
                      className="w-14 h-14 rounded-2xl object-cover shadow-sm border-2 border-white/20" 
                    />
                    <div className={`absolute -bottom-1 -right-1 w-4 h-4 rounded-full border-2 ${isActive ? 'border-bookup-primary' : 'border-white'} bg-green-500 shadow-sm`}></div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-center mb-1">
                      <h3 className={`font-black truncate text-lg font-display ${isActive ? 'text-white' : 'text-gray-900'}`}>
                        {name}
                      </h3>
                    </div>
                    <p className={`text-sm font-medium truncate ${isActive ? 'text-white/80' : 'text-gray-500'}`}>
                      {channel.lastMessage || 'Start a conversation...'}
                    </p>
                  </div>
                </button>
              );
            })
          )}
        </div>
      </div>

      {/* Chat Area */}
      <div className={`flex-1 flex flex-col bg-white rounded-[2rem] shadow-sm border border-gray-100 overflow-hidden ${!activeChannel ? 'hidden md:flex' : 'flex'}`}>
        {activeChannel ? (
          <>
            <div className="h-20 border-b border-gray-50 flex items-center justify-between px-6 shrink-0 bg-white z-10">
              <div className="flex items-center gap-4">
                <button className="md:hidden p-2 -ml-2 text-gray-500" onClick={() => setActiveChannel(null)}>
                  <ArrowLeft size={24} />
                </button>
                <div className="relative cursor-pointer group" onClick={openUserProfile}>
                  <img 
                    src={getChannelPhoto(activeChannel) || `https://ui-avatars.com/api/?name=${encodeURIComponent(getChannelName(activeChannel))}&background=2E8B57&color=fff&bold=true&size=128`} 
                    alt="Avatar" 
                    className="w-12 h-12 rounded-2xl object-cover shadow-sm group-hover:scale-105 transition-transform" 
                  />
                  <div className="absolute -bottom-1 -right-1 w-4 h-4 bg-green-500 rounded-full border-2 border-white shadow-sm"></div>
                </div>
                <div className="cursor-pointer" onClick={openUserProfile}>
                  <h3 className="font-black text-xl text-gray-900 font-display hover:text-bookup-primary transition-colors">{getChannelName(activeChannel)}</h3>
                  <p className="text-xs font-black text-green-500 uppercase tracking-widest">Active Now</p>
                </div>
              </div>
              <div className="flex gap-2">
                <button 
                  onClick={() => initiateCall(
                    activeChannel.participantIds.find(id => id !== currentUser?.uid) || '',
                    getChannelName(activeChannel),
                    getChannelPhoto(activeChannel) || '',
                    'VOICE',
                    activeChannel.id
                  )}
                  className="p-3 rounded-xl hover:bg-gray-50 text-gray-400 hover:text-bookup-primary transition-all"
                ><Phone size={20} /></button>
                <button 
                  onClick={() => initiateCall(
                    activeChannel.participantIds.find(id => id !== currentUser?.uid) || '',
                    getChannelName(activeChannel),
                    getChannelPhoto(activeChannel) || '',
                    'VIDEO',
                    activeChannel.id
                  )}
                  className="p-3 rounded-xl hover:bg-gray-50 text-gray-400 hover:text-bookup-primary transition-all"
                ><Video size={20} /></button>
                <button className="p-3 rounded-xl hover:bg-gray-50 text-gray-400 hover:text-gray-900 transition-all"><MoreVertical size={20} /></button>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto bg-gray-50/30 p-6 flex flex-col gap-4">
              {messages.length === 0 ? (
                <div className="flex-1 flex flex-col items-center justify-center text-gray-300">
                  <MessageSquare size={48} className="mb-2 opacity-20" />
                  <p className="font-bold">No messages yet</p>
                </div>
              ) : (
                messages.map((msg) => {
                  const isMe = msg.senderId === currentUser?.uid;
                  const type = (msg.messageType || msg.type || 'text').toLowerCase();
                  const text = msg.content || msg.text || '';
                  
                  return (
                    <div key={msg.id} className={`flex ${isMe ? 'justify-end' : 'justify-start'} animate-in fade-in slide-in-from-bottom-2 duration-300`}>
                      <div className={`group relative max-w-[80%] ${isMe ? 'items-end' : 'items-start'}`}>
                        <div className={`
                          ${isMe ? 'bg-bookup-primary text-white rounded-tr-sm shadow-md' : 'bg-white border border-gray-100 text-gray-800 rounded-tl-sm shadow-sm'} 
                          rounded-[1.25rem] px-5 py-3.5 font-medium text-[15px]
                        `}>
                          {type === 'image' && msg.mediaUrl && (
                            <div className="relative rounded-lg overflow-hidden mb-2 cursor-zoom-in group/img" onClick={() => setPreviewImage(msg.mediaUrl!)}>
                              <img src={msg.mediaUrl} alt="Attachment" className="max-w-full object-cover max-h-72 transition-transform duration-500 group-hover/img:scale-105" />
                              <div className="absolute inset-0 bg-black/0 group-hover/img:bg-black/20 transition-all flex items-center justify-center opacity-0 group-hover/img:opacity-100">
                                <Maximize2 size={24} className="text-white" />
                              </div>
                            </div>
                          )}
                          {type === 'file' && msg.mediaUrl && (
                            <a href={msg.mediaUrl} target="_blank" rel="noreferrer" className="flex items-center gap-3 bg-white/10 p-3 rounded-xl mb-2 hover:bg-white/20 transition-all">
                              <div className="p-2 bg-white/20 rounded-lg"><Download size={18} /></div>
                              <span className="font-bold text-sm truncate max-w-[150px]">View Document</span>
                            </a>
                          )}
                          {type === 'audio' && msg.mediaUrl && (
                            <div className="mb-2 py-1">
                              <audio src={msg.mediaUrl} controls className={`w-full max-w-[240px] h-10 ${isMe ? 'filter invert' : ''}`} />
                            </div>
                          )}
                          {type === 'call' && (
                            <CallLog type={type} text={text} />
                          )}
                          <p className="leading-relaxed">{type === 'call' ? '' : text}</p>
                        </div>
                        <span className="text-[10px] font-black text-gray-400 mt-1 uppercase tracking-widest opacity-0 group-hover:opacity-100 transition-opacity">
                          {msg.timestamp?.seconds ? new Date(msg.timestamp.seconds * 1000).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : 'Just now'}
                        </span>
                      </div>
                    </div>
                  );
                })
              )}
              <div ref={messagesEndRef} />
            </div>

            <div className="p-6 bg-white border-t border-gray-50 shrink-0">
              <form onSubmit={handleSendMessage} className="flex gap-3 relative items-center">
                <label className="p-3.5 text-gray-400 hover:text-bookup-primary transition-all cursor-pointer rounded-2xl hover:bg-gray-50">
                  <Paperclip size={24} />
                  <input type="file" className="hidden" onChange={handleFileUpload} disabled={uploading} />
                </label>
                
                <div className="flex-1 relative">
                  <input 
                    type="text" 
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    className="w-full bg-gray-50 border border-transparent rounded-2xl px-6 py-4 focus:outline-none focus:ring-2 focus:ring-bookup-primary/20 focus:bg-white focus:border-bookup-primary transition-all font-medium text-lg"
                    placeholder={uploading ? "Uploading..." : "Type your message..."}
                    disabled={uploading}
                  />
                </div>
                
                {recording ? (
                  <button type="button" onClick={stopVoiceNote} className="p-4 text-white bg-red-500 rounded-2xl shadow-lg shadow-red-200 animate-pulse"><StopCircle size={24} /></button>
                ) : (
                  <button type="button" onClick={startVoiceNote} className="p-4 text-gray-400 hover:text-bookup-primary hover:bg-gray-50 rounded-2xl transition-all"><Mic size={24} /></button>
                )}
                
                <button 
                  type="submit"
                  disabled={!newMessage.trim() || uploading}
                  className="bg-gray-900 hover:bg-bookup-primary disabled:bg-gray-100 disabled:text-gray-400 text-white p-5 rounded-2xl flex items-center justify-center transition-all shadow-xl shadow-gray-200 active:scale-95"
                >
                  <Send size={24} />
                </button>
              </form>
            </div>
          </>
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center text-gray-300 p-12 text-center">
            <div className="w-24 h-24 bg-gray-50 rounded-[2.5rem] flex items-center justify-center mb-6 text-gray-200">
              <MessageSquare size={48} />
            </div>
            <h3 className="text-3xl font-black text-gray-900 mb-3 font-display">Select a conversation</h3>
            <p className="text-gray-500 font-medium max-w-sm">Choose a person or group from the list on the left to start your learning journey.</p>
          </div>
        )}
      </div>

      {/* Image Preview Modal */}
      {previewImage && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/90 backdrop-blur-sm animate-in fade-in duration-300" onClick={() => setPreviewImage(null)}>
          <button className="absolute top-8 right-8 text-white hover:scale-110 transition-transform p-2 bg-white/10 rounded-full"><X size={32} /></button>
          <img src={previewImage} alt="Full preview" className="max-w-full max-h-full rounded-2xl shadow-2xl animate-in zoom-in-95 duration-300" />
        </div>
      )}
      {/* New Chat Modal */}
      {isNewChatModalOpen && (
        <div className="fixed inset-0 z-[60] flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={() => setIsNewChatModalOpen(false)} />
          <div className="bg-white w-full max-w-md rounded-[2.5rem] shadow-2xl overflow-hidden relative z-10 animate-in zoom-in-95 duration-300">
            <div className="p-8 border-b border-gray-50 flex items-center justify-between bg-white">
              <h3 className="text-2xl font-black text-gray-900 font-display">New Conversation</h3>
              <button 
                onClick={() => setIsNewChatModalOpen(false)}
                className="p-2 hover:bg-gray-100 rounded-xl transition-colors"
              >
                <X size={24} className="text-gray-400" />
              </button>
            </div>
            
            <div className="p-6">
              <div className="relative mb-6">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                <input 
                  type="text" 
                  autoFocus
                  placeholder="Search people..." 
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full bg-gray-50 border-none rounded-2xl py-4 pl-12 pr-4 focus:ring-2 focus:ring-bookup-primary/20 transition-all font-medium"
                />
              </div>

              <div className="max-h-[400px] overflow-y-auto space-y-2 pr-2">
                {loadingUsers ? (
                  <div className="flex flex-col items-center py-10 gap-4">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-bookup-primary"></div>
                    <p className="text-gray-400 font-bold text-sm uppercase tracking-widest">Searching users...</p>
                  </div>
                ) : filteredUsers.length > 0 ? (
                  filteredUsers.map(user => (
                    <button
                      key={user.id}
                      onClick={() => startNewChat(user)}
                      className="w-full flex items-center gap-4 p-4 rounded-2xl hover:bg-bookup-primary/5 transition-all text-left group"
                    >
                      <div className="relative">
                        <img 
                          src={user.photoUrl || user.photoURL || `https://ui-avatars.com/api/?name=${user.displayName || user.name || 'U'}&background=2E8B57&color=fff&bold=true`} 
                          alt={user.displayName} 
                          className="w-14 h-14 rounded-xl object-cover shadow-sm group-hover:scale-105 transition-transform"
                        />
                        {user.online && (
                          <div className="absolute -bottom-1 -right-1 w-4 h-4 bg-green-500 rounded-full border-2 border-white" />
                        )}
                      </div>
                      <div>
                        <p className="font-black text-gray-900 font-display group-hover:text-bookup-primary transition-colors">
                          {user.displayName || user.name || 'Unknown User'}
                        </p>
                        <p className="text-xs font-bold text-gray-400 uppercase tracking-widest">
                          {user.role || 'student'}
                        </p>
                      </div>
                    </button>
                  ))
                ) : (
                  <div className="text-center py-10">
                    <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4">
                      <UserIcon size={32} className="text-gray-300" />
                    </div>
                    <p className="text-gray-500 font-bold">No users found</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
