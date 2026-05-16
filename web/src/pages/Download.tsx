import { Smartphone, Download as DownloadIcon, ShieldCheck, Zap, Globe, MessageSquare, Info, AlertCircle, ArrowRight } from 'lucide-react';
import { useState, useEffect } from 'react';
import { doc, getDoc } from 'firebase/firestore';
import { db } from '../lib/firebase';

export default function Download() {
  const [downloadLink, setDownloadLink] = useState<string>('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchConfig() {
      try {
        const snap = await getDoc(doc(db, 'appSettings', 'config'));
        if (snap.exists()) {
          setDownloadLink(snap.data().downloadLink || '');
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    fetchConfig();
  }, []);

  return (
    <div className="min-h-screen bg-gray-50/50 pb-20 animate-in fade-in duration-700">
      
      {/* Hero Header */}
      <section className="bg-white border-b border-gray-100 py-20 px-4">
         <div className="max-w-4xl mx-auto text-center space-y-8">
            <div className="inline-flex items-center gap-2 px-6 py-2.5 rounded-full bg-bookup-primary/10 text-bookup-primary font-black text-xs uppercase tracking-widest border border-bookup-primary/20">
               <Smartphone size={16} /> Mobile Experience
            </div>
            <h1 className="text-5xl md:text-7xl font-black text-gray-900 tracking-tight font-display leading-none">
              Take BookUp <br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-bookup-primary to-bookup-secondary">Everywhere.</span>
            </h1>
            <p className="text-xl text-gray-500 font-medium max-w-2xl mx-auto leading-relaxed">
              Stay connected with your tutors and learning materials on the go. Get real-time notifications for every update.
            </p>
         </div>
      </section>

      {/* Main Download Focus */}
      <section className="max-w-5xl mx-auto px-4 -mt-10">
         <div className="bg-gray-900 rounded-[4rem] p-12 md:p-20 shadow-2xl relative overflow-hidden text-center text-white">
            <div className="absolute top-0 right-0 w-96 h-96 bg-bookup-primary/20 rounded-full blur-[100px] -mr-48 -mt-48"></div>
            
            <div className="relative z-10 space-y-10">
               <div className="w-24 h-24 bg-white/10 rounded-[2rem] flex items-center justify-center mx-auto shadow-xl backdrop-blur-md">
                  <Smartphone size={48} className="text-bookup-primary-light" />
               </div>
               
               <div className="space-y-4">
                  <h2 className="text-3xl md:text-5xl font-black">Official Android Application</h2>
                  <p className="text-white/60 font-bold text-lg max-w-xl mx-auto">
                     Direct download of our secure APK. Optimized for all modern Android devices.
                  </p>
               </div>

               {loading ? (
                 <div className="animate-pulse flex flex-col items-center gap-4">
                    <div className="h-16 w-64 bg-white/10 rounded-2xl"></div>
                 </div>
               ) : downloadLink ? (
                 <div className="flex flex-col items-center gap-6">
                    <a 
                      href={downloadLink} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="bg-bookup-primary text-white px-12 py-6 rounded-[2rem] font-black text-2xl hover:bg-bookup-primary-dark transition-all hover:scale-105 shadow-2xl shadow-bookup-primary/30 flex items-center gap-4 group"
                    >
                       Download App <DownloadIcon size={28} className="group-hover:translate-y-1 transition-transform" />
                    </a>
                    <p className="text-white/40 font-black text-xs uppercase tracking-widest flex items-center gap-2">
                       <ShieldCheck size={16} /> Verified & Secure Download
                    </p>
                 </div>
               ) : (
                 <div className="bg-white/10 p-8 rounded-3xl border border-white/10">
                    <AlertCircle size={32} className="text-amber-400 mx-auto mb-4" />
                    <p className="font-black text-xl">Download Link Pending</p>
                    <p className="text-white/50 font-medium">The administrator hasn't updated the latest download link yet.</p>
                 </div>
               )}
            </div>
         </div>
      </section>

      {/* Bulky Content Section: Instructions */}
      <section className="max-w-4xl mx-auto px-4 py-24 space-y-16">
         <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
            <div className="space-y-6">
               <h3 className="text-3xl font-black text-gray-900">How to Install</h3>
               <div className="space-y-4">
                  {[
                    'Download the official BookUp APK file.',
                    'Open the file from your notifications bar.',
                    'Allow "Unknown Sources" if prompted by your device.',
                    'Click "Install" and wait for the magic to happen!'
                  ].map((step, i) => (
                    <div key={i} className="flex gap-4 items-start">
                       <div className="w-8 h-8 bg-white border border-gray-100 rounded-full flex items-center justify-center shrink-0 font-black text-bookup-primary shadow-sm">{i+1}</div>
                       <p className="font-bold text-gray-600 pt-1">{step}</p>
                    </div>
                  ))}
               </div>
            </div>
            <div className="bg-white p-8 rounded-[3rem] border border-gray-100 shadow-sm relative">
               <div className="absolute top-4 right-4 text-bookup-primary/20"><Info size={80} /></div>
               <h4 className="text-xl font-black text-gray-900 mb-4">Pro Tip</h4>
               <p className="text-gray-500 font-bold leading-relaxed relative z-10">
                  Enable "Auto-Update" in the app settings to always have the latest features and security patches from BookUp.
               </p>
            </div>
         </div>

         {/* Features Grid */}
         <div className="space-y-10">
            <div className="text-center">
               <h3 className="text-3xl font-black text-gray-900">Mobile Features</h3>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
               {[
                 { title: 'Instant Alerts', desc: 'Never miss a call or message from your tutor again.', icon: Zap },
                 { title: 'Offline Mode', desc: 'Study even without an active internet connection.', icon: Globe },
                 { title: 'Video Classes', desc: 'Seamless video calling experience on mobile data.', icon: Smartphone },
               ].map((feature, i) => (
                 <div key={i} className="bg-white p-8 rounded-[2.5rem] border border-gray-100 shadow-sm hover:shadow-md transition-all text-center">
                    <div className="w-14 h-14 bg-gray-50 rounded-2xl flex items-center justify-center text-bookup-primary mb-6 mx-auto">
                       <feature.icon size={28} />
                    </div>
                    <h4 className="text-xl font-black text-gray-900 mb-2">{feature.title}</h4>
                    <p className="text-gray-500 font-bold text-sm leading-relaxed">{feature.desc}</p>
                 </div>
               ))}
            </div>
         </div>
      </section>

      {/* Support Section */}
      <section className="max-w-4xl mx-auto px-4">
         <div className="bg-white rounded-[3rem] p-12 text-center border border-gray-100 shadow-sm flex flex-col items-center">
            <MessageSquare size={40} className="text-bookup-primary mb-6" />
            <h3 className="text-2xl font-black text-gray-900 mb-4">Need help with installation?</h3>
            <p className="text-gray-500 font-bold mb-8 max-w-md">Our technical team is available 24/7 to guide you through the process.</p>
            <button className="bg-gray-50 text-gray-900 px-10 py-4 rounded-2xl font-black hover:bg-gray-100 transition-all flex items-center gap-2 group">
               Contact Support <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />
            </button>
         </div>
      </section>

    </div>
  );
}
