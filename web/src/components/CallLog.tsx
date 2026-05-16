import React from 'react';
import { Phone, Video, PhoneOff, VideoOff, Clock } from 'lucide-react';

interface CallLogProps {
  type: string;
  text: string;
}

export default function CallLog({ type, text }: CallLogProps) {
  const isVideo = text.toLowerCase().includes('video') || text.toLowerCase().includes('ended'); // Approximate check
  const isMissed = text.toLowerCase().includes('missed') || text.toLowerCase().includes('declined') || text.toLowerCase().includes('no answer');
  
  return (
    <div className="flex items-center gap-4 bg-gray-50 p-4 rounded-2xl border border-gray-100 my-2 min-w-[240px] shadow-sm">
      <div className={`w-1 h-12 rounded-full ${isMissed ? 'bg-red-500' : 'bg-green-500'}`}></div>
      <div className="w-12 h-12 bg-white rounded-xl flex items-center justify-center shadow-sm text-gray-400">
        {isVideo ? (isMissed ? <VideoOff size={24} className="text-red-400" /> : <Video size={24} className="text-green-500" />) 
                 : (isMissed ? <PhoneOff size={24} className="text-red-400" /> : <Phone size={24} className="text-green-500" />)}
      </div>
      <div className="flex-1">
        <h4 className="font-black text-gray-900 font-display flex items-center gap-2">
          {isVideo ? 'Video Call' : 'Voice Call'}
          {text.includes(':') && <span className="text-[10px] bg-gray-200 px-2 py-0.5 rounded-full text-gray-600 font-bold uppercase tracking-tighter">Connected</span>}
        </h4>
        <div className="flex items-center gap-1.5 mt-0.5">
           {text.includes(':') && <Clock size={12} className="text-gray-400" />}
           <p className={`text-sm font-bold ${isMissed ? 'text-red-500' : 'text-gray-500'}`}>
             {text}
           </p>
        </div>
      </div>
    </div>
  );
}
