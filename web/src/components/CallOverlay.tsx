import React, { useEffect, useRef, useState } from 'react';
import { PhoneOff, Mic, MicOff, Video, VideoOff, RefreshCw, Play } from 'lucide-react';
import type { ICameraVideoTrack, IRemoteVideoTrack, IRemoteAudioTrack } from 'agora-rtc-sdk-ng';

interface CallOverlayProps {
  type: 'VOICE' | 'VIDEO';
  peerName: string;
  peerPhoto?: string;
  localVideoTrack: ICameraVideoTrack | null;
  remoteVideoTrack: IRemoteVideoTrack | null;
  remoteAudioTrack: IRemoteAudioTrack | null;
  isMuted: boolean;
  isVideoDisabled: boolean;
  onEndCall: () => void;
  onToggleMute: () => void;
  onToggleVideo: () => void;
  onFlipCamera: () => void;
}

export default function CallOverlay({
  type,
  peerName,
  peerPhoto,
  localVideoTrack,
  remoteVideoTrack,
  remoteAudioTrack,
  isMuted,
  isVideoDisabled,
  onEndCall,
  onToggleMute,
  onToggleVideo,
  onFlipCamera
}: CallOverlayProps) {
  const localRef = useRef<HTMLDivElement>(null);
  const remoteRef = useRef<HTMLDivElement>(null);
  const [timer, setTimer] = useState(0);
  const [showAutoplayHint, setShowAutoplayHint] = useState(false);

  const isConnected = !!remoteVideoTrack || !!remoteAudioTrack;

  useEffect(() => {
    let interval: any;
    if (isConnected) {
        console.log("[CallStep 8] Peer connection confirmed in UI. Starting timer.");
        interval = setInterval(() => {
          setTimer(prev => prev + 1);
        }, 1000);
    }
    return () => clearInterval(interval);
  }, [isConnected]);

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  useEffect(() => {
    if (localVideoTrack && localRef.current) {
      localVideoTrack.play(localRef.current);
    }
  }, [localVideoTrack, isVideoDisabled]);

  useEffect(() => {
    if (remoteVideoTrack && remoteRef.current) {
      remoteVideoTrack.play(remoteRef.current);
    }
  }, [remoteVideoTrack]);

  const handleManualPlay = () => {
    remoteAudioTrack?.play();
    setShowAutoplayHint(false);
  };

  useEffect(() => {
    if (remoteAudioTrack) {
        try {
            const res = remoteAudioTrack.play() as any;
            if (res && res.catch) res.catch(() => setShowAutoplayHint(true));
        } catch(e) {
            setShowAutoplayHint(true);
        }
    }
  }, [remoteAudioTrack]);

  return (
    <div className="fixed inset-0 bg-[#0A0F0D] z-[10000] flex flex-col items-center justify-center overflow-hidden animate-in fade-in duration-500 font-sans">
      {/* Remote Video (Full Screen) */}
      <div className="absolute inset-0 z-0 bg-black">
        {type === 'VIDEO' && remoteVideoTrack ? (
          <div ref={remoteRef} className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full flex flex-col items-center justify-center bg-[#0d1511]">
            <div className="relative mb-8">
               {isConnected && (
                 <div className="absolute inset-0 bg-bookup-primary/20 rounded-[4.5rem] animate-ping scale-110"></div>
               )}
               <img 
                 src={peerPhoto || `https://ui-avatars.com/api/?name=${encodeURIComponent(peerName)}&background=1B9A8B&color=fff&size=256`} 
                 alt={peerName} 
                 className="w-48 h-48 rounded-[4rem] object-cover border-8 border-white/5 shadow-2xl relative z-10"
               />
            </div>
            <h2 className="text-4xl font-black text-white font-display mb-2">{peerName}</h2>
            <p className="text-bookup-primary font-black uppercase tracking-widest text-xs animate-pulse">
              {isConnected ? 'Active Session' : 'Waiting for connection...'}
            </p>
          </div>
        )}
      </div>

      {/* Autoplay Hint */}
      {showAutoplayHint && (
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-[100] flex flex-col items-center gap-4 bg-black/80 backdrop-blur-xl p-8 rounded-3xl border border-white/10 shadow-2xl text-center">
           <Play className="text-bookup-primary animate-bounce" size={48} />
           <div>
             <p className="text-white font-bold text-lg">Sound is blocked</p>
             <p className="text-white/60 text-sm">Click the button below to hear the other person</p>
           </div>
           <button 
             onClick={handleManualPlay}
             className="bg-bookup-primary text-white px-8 py-3 rounded-xl font-black uppercase tracking-widest hover:scale-105 active:scale-95 transition-all"
           >
             Enable Audio
           </button>
        </div>
      )}

      {/* Peer Info (Top Left) */}
      <div className="absolute top-12 left-12 z-30 flex flex-col gap-1">
        <div className="bg-black/40 backdrop-blur-xl px-6 py-3 rounded-2xl border border-white/10 shadow-2xl">
          <p className="text-white font-black font-display tracking-tight text-xl">{peerName}</p>
          <div className="flex items-center gap-2 mt-1">
             <div className={`w-2 h-2 ${isConnected ? 'bg-green-500 animate-pulse shadow-[0_0_8px_rgba(34,197,94,0.6)]' : 'bg-yellow-500'} rounded-full`}></div>
             <p className="text-[10px] text-white/70 font-black uppercase tracking-widest">
               {isConnected ? formatTime(timer) : 'Connecting...'}
             </p>
          </div>
        </div>
      </div>

      {/* Local Video (Floating) */}
      <div 
        id="local-player-wrapper"
        className={`absolute top-12 right-12 w-40 h-56 bg-gray-900 rounded-[2.5rem] shadow-2xl border-2 border-white/10 overflow-hidden z-20 group ring-4 ring-black/20 ${type === 'VIDEO' && !isVideoDisabled ? 'opacity-100' : 'opacity-0 pointer-events-none'}`}
      >
        <div ref={localRef} className="w-full h-full object-cover" />
      </div>

      {/* Controls (Pill Design) */}
      <div className="absolute bottom-12 left-1/2 -translate-x-1/2 z-30">
        <div className="bg-black/60 backdrop-blur-2xl p-3 rounded-[3rem] border border-white/10 shadow-2xl flex items-center gap-4">
          <button 
            onClick={onToggleMute}
            className={`w-14 h-14 rounded-full flex items-center justify-center transition-all ${
              isMuted ? 'bg-red-500 text-white shadow-lg shadow-red-500/20' : 'bg-white/10 text-white hover:bg-white/20'
            }`}
          >
            {isMuted ? <MicOff size={24} /> : <Mic size={24} />}
          </button>

          {type === 'VIDEO' && (
            <button 
              onClick={onToggleVideo}
              className={`w-14 h-14 rounded-full flex items-center justify-center transition-all ${
                isVideoDisabled ? 'bg-red-500 text-white shadow-lg shadow-red-500/20' : 'bg-white/10 text-white hover:bg-white/20'
              }`}
            >
              {isVideoDisabled ? <VideoOff size={24} /> : <Video size={24} />}
            </button>
          )}

          <button 
            onClick={onEndCall}
            className="w-16 h-16 bg-red-600 text-white rounded-full flex items-center justify-center shadow-2xl shadow-red-900/40 hover:bg-red-700 hover:-scale-105 active:scale-95 transition-all"
          >
            <PhoneOff size={28} />
          </button>

          {type === 'VIDEO' && (
            <button 
              onClick={onFlipCamera}
              className="w-14 h-14 bg-white/10 text-white rounded-full flex items-center justify-center hover:bg-white/20 transition-all shadow-xl"
            >
              <RefreshCw size={24} />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
