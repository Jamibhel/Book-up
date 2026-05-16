import React, { useEffect, useRef, useState } from 'react';
import { Phone, PhoneOff } from 'lucide-react';

interface IncomingCallUIProps {
  callerName: string;
  callerPhoto?: string;
  type: 'VOICE' | 'VIDEO';
  onAccept: () => void;
  onReject: () => void;
}

export default function IncomingCallUI({ callerName, callerPhoto, type, onAccept, onReject }: IncomingCallUIProps) {
  const [audioError, setAudioError] = useState(false);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    const ringtoneUrl = "https://assets.mixkit.co/active_storage/sfx/1359/1359-preview.mp3";
    audioRef.current = new Audio(ringtoneUrl);
    audioRef.current.loop = true;
    
    const startAudio = async () => {
      try {
        await audioRef.current?.play();
      } catch (err) {
        console.warn("Ringtone blocked by browser autoplay policy.");
        setAudioError(true);
      }
    };

    startAudio();

    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
    };
  }, []);

  return (
    <div className="fixed inset-0 bg-[#0A0F0D]/90 backdrop-blur-2xl z-[9999] flex items-center justify-center p-6 animate-in fade-in duration-500">
      <div className="bg-white/5 rounded-[4rem] p-12 w-full max-w-md text-center shadow-2xl border border-white/10 relative overflow-hidden ring-1 ring-white/20">
        <div className="absolute inset-0 z-0 opacity-20">
           <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-bookup-primary rounded-full blur-[100px] animate-pulse"></div>
        </div>

        <div className="relative z-10">
          <div className="relative inline-block mb-10">
            <div className="absolute inset-0 bg-bookup-primary/40 rounded-[3.5rem] animate-ping scale-110 opacity-30"></div>
            <img 
              src={callerPhoto || `https://ui-avatars.com/api/?name=${encodeURIComponent(callerName)}&background=1B9A8B&color=fff&size=256`} 
              alt="Caller" 
              className="w-44 h-44 rounded-[3.5rem] object-cover border-4 border-white/20 shadow-2xl relative z-10"
            />
          </div>
          
          <h2 className="text-4xl font-black text-white mb-3 font-display tracking-tight">{callerName}</h2>
          <div className="flex items-center justify-center gap-3 mb-12">
             <div className="w-2 h-2 bg-bookup-primary rounded-full animate-bounce"></div>
             <p className="text-bookup-primary font-black uppercase tracking-[0.3em] text-[10px]">
               Incoming {type} Call
             </p>
          </div>
          
          {audioError && (
             <p className="text-white/40 text-[10px] mb-6 uppercase tracking-wider font-bold">Sound disabled by browser (Click to interact)</p>
          )}

          <div className="flex gap-8 justify-center">
            <button 
              onClick={(e) => { e.stopPropagation(); onReject(); }}
              className="w-24 h-24 bg-red-500 text-white rounded-full flex items-center justify-center shadow-2xl hover:bg-red-600 hover:-translate-y-2 transition-all active:scale-90 group"
            >
              <PhoneOff size={36} className="group-hover:rotate-12 transition-transform" />
            </button>
            <button 
              onClick={(e) => { e.stopPropagation(); onAccept(); }}
              className="w-24 h-24 bg-bookup-primary text-white rounded-full flex items-center justify-center shadow-2xl hover:bg-bookup-primary-dark hover:-translate-y-2 transition-all active:scale-90 group"
            >
              <Phone size={36} className="group-hover:-rotate-12 transition-transform" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
