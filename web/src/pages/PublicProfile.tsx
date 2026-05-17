import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { doc, getDoc, setDoc, serverTimestamp, collection, query, where, getDocs, limit } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../contexts/AuthContext';
import { useCall } from '../contexts/CallContext';
import { MapPin, Award, Star, User as UserIcon, MessageSquare, ArrowLeft, BookOpen, Zap, ShieldCheck, GraduationCap, Calendar, ChevronRight, Briefcase, Phone, Video } from 'lucide-react';

export default function PublicProfile() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const { initiateCall } = useCall();
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchUserAndData() {
      if (!id) return;
      try {
        const docSnap = await getDoc(doc(db, 'users', id));
        if (docSnap.exists()) {
          const data = docSnap.data();
          setProfile({ id: docSnap.id, ...data });
          
          // Fetch some mock/real reviews if they exist
          const reviewsQuery = query(collection(db, 'reviews'), where('tutorId', '==', id), limit(3));
          await getDocs(reviewsQuery);
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    fetchUserAndData();
  }, [id]);

  const handleMessage = async () => {
    if (!currentUser || !profile) return alert("Please log in");
    if (currentUser.uid === profile.id) return alert("You can't message yourself.");
    
    try {
      const channelId = [currentUser.uid, profile.id].sort().join('_');
      const channelRef = doc(db, 'conversations', channelId);
      const snap = await getDoc(channelRef);
      
      if (!snap.exists()) {
        await setDoc(channelRef, {
          id: channelId,
          participantIds: [currentUser.uid, profile.id],
          participantNames: {
            [currentUser.uid]: currentUser.displayName || currentUser.email || 'User',
            [profile.id]: profile.displayName || 'User'
          },
          participantPhotos: {
            [currentUser.uid]: currentUser.photoURL || '',
            [profile.id]: profile.photoURL || profile.photoUrl || ''
          },
          isGroup: false,
          lastMessage: '',
          lastMessageTimestamp: serverTimestamp()
        });
      }
      navigate('/messages');
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20 min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-bookup-primary border-t-transparent shadow-lg"></div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="flex flex-col items-center justify-center py-20 min-h-[60vh] animate-in zoom-in-95 duration-500">
        <div className="bg-gray-100 p-8 rounded-full mb-6">
          <UserIcon size={64} className="text-gray-400" />
        </div>
        <h2 className="text-3xl font-black text-gray-900 tracking-tight">User Not Found</h2>
        <p className="text-gray-500 mt-2 text-lg">This profile may have been removed or doesn't exist.</p>
        <button onClick={() => navigate(-1)} className="mt-8 px-8 py-4 bg-bookup-primary text-white rounded-2xl font-black shadow-xl shadow-bookup-primary/20 hover:-translate-y-1 transition-all duration-300">
          Return to Directory
        </button>
      </div>
    );
  }

  const role = profile.role || 'student';
  const name = profile.displayName || (profile.firstName ? `${profile.firstName} ${profile.lastName}` : 'User');
  const isTutor = role === 'tutor';
  const photoUrl = profile.photoURL || profile.photoUrl;

  return (
    <div className="max-w-6xl mx-auto space-y-8 animate-in slide-in-from-bottom-8 duration-700 pb-20">
      <button 
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-gray-500 hover:text-bookup-primary font-bold transition-colors group mb-4"
      >
        <span className="p-2 bg-white rounded-full shadow-sm group-hover:shadow-md transition-all">
          <ArrowLeft size={20} className="group-hover:-translate-x-1 transition-transform" />
        </span>
        <span className="font-display text-lg">Back to Directory</span>
      </button>

      {/* Main Profile Card */}
      <div className="bg-white rounded-[2.5rem] shadow-xl border border-gray-100 overflow-hidden relative group">
        {/* Cover Graphic - Modern Mesh Gradient */}
        <div className="h-64 md:h-96 w-full relative overflow-hidden bg-gradient-to-br from-[#1a5f3a] via-bookup-primary to-bookup-secondary">
          {/* Decorative elements */}
          <div className="absolute -top-24 -right-24 w-96 h-96 bg-white/10 rounded-full blur-3xl mix-blend-overlay"></div>
          <div className="absolute top-10 left-10 w-64 h-64 bg-bookup-secondary/20 rounded-full blur-2xl"></div>
          <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-10 mix-blend-overlay"></div>
          
          {/* Floating badge for tutors */}
          {isTutor && (
            <div className="absolute top-8 right-8 bg-white/20 backdrop-blur-md border border-white/30 px-6 py-2 rounded-full text-white font-black text-sm uppercase tracking-widest hidden md:flex items-center gap-2">
              <Zap size={16} className="text-amber-400 fill-amber-400" />
              Top Rated Tutor
            </div>
          )}
        </div>
        
        <div className="px-8 pb-12 relative">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-end -mt-32 mb-8 gap-8">
            <div className="relative group/avatar">
              <div className="w-56 h-56 rounded-[2.5rem] p-2 bg-white shadow-2xl relative overflow-hidden">
                <img 
                  src={photoUrl || `https://ui-avatars.com/api/?name=${name}&background=1B9A8B&color=fff&size=400&bold=true`} 
                  alt={name} 
                  className="w-full h-full rounded-[2rem] object-cover transition-transform duration-700 group-hover/avatar:scale-110" 
                />
              </div>
              {isTutor && (
                <div className="absolute -bottom-2 -right-2 bg-amber-500 text-white p-4 rounded-[1.5rem] shadow-2xl border-4 border-white flex items-center justify-center" title="Verified Expert">
                  <ShieldCheck size={28} />
                </div>
              )}
            </div>
            
            <div className="flex flex-wrap gap-4 w-full md:w-auto">
              <button 
                onClick={handleMessage}
                className="flex-1 md:flex-none flex items-center justify-center gap-3 bg-white border-2 border-gray-100 hover:border-bookup-primary text-gray-900 hover:text-bookup-primary px-8 py-5 rounded-2xl font-black transition-all shadow-sm hover:shadow-xl hover:-translate-y-1 font-display"
              >
                <MessageSquare size={24} />
                Message
              </button>
              
              {isTutor && (
                <>
                  <button 
                    onClick={() => {
                      if (currentUser) {
                        const channelId = [currentUser.uid, profile.id].sort().join('_');
                        initiateCall(profile.id, name, photoUrl || '', 'VOICE', channelId);
                      }
                    }}
                    className="flex-1 md:flex-none flex items-center justify-center gap-3 bg-white border-2 border-gray-100 hover:border-bookup-secondary text-gray-900 hover:text-bookup-secondary px-8 py-5 rounded-2xl font-black transition-all shadow-sm hover:shadow-xl hover:-translate-y-1 font-display"
                  >
                    <Phone size={24} />
                    Call
                  </button>
                  <button 
                    onClick={() => {
                      if (currentUser) {
                        const channelId = [currentUser.uid, profile.id].sort().join('_');
                        initiateCall(profile.id, name, photoUrl || '', 'VIDEO', channelId);
                      }
                    }}
                    className="flex-1 md:flex-none flex items-center justify-center gap-3 bg-white border-2 border-gray-100 hover:border-bookup-primary text-gray-900 hover:text-bookup-primary px-8 py-5 rounded-2xl font-black transition-all shadow-sm hover:shadow-xl hover:-translate-y-1 font-display"
                  >
                    <Video size={24} />
                    Video
                  </button>
                  <button className="flex-1 md:flex-none flex items-center justify-center gap-3 bg-bookup-primary hover:bg-bookup-primary-dark text-white px-10 py-5 rounded-2xl font-black transition-all shadow-xl shadow-bookup-primary/30 hover:shadow-bookup-primary/50 hover:-translate-y-1 font-display">
                    <Calendar size={24} />
                    Book Now
                  </button>
                </>
              )}
            </div>
          </div>

          <div className="mb-12 max-w-3xl">
            <h1 className="text-5xl md:text-6xl font-black text-gray-900 tracking-tight mb-6 font-display leading-tight">
              {name}
            </h1>
            
            <div className="flex flex-wrap items-center gap-3">
              <div className="bg-bookup-primary/10 text-bookup-primary px-5 py-2.5 rounded-2xl text-sm font-black uppercase tracking-widest flex items-center gap-2">
                <Award size={18} /> {role}
              </div>
              
              {profile.locationName && (
                <div className="bg-gray-50 text-gray-600 px-5 py-2.5 rounded-2xl text-sm font-bold flex items-center gap-2 border border-gray-100">
                  <MapPin size={18} className="text-gray-400" /> {profile.locationName}
                </div>
              )}

              {profile.workPreference && (
                <div className="bg-amber-50 text-amber-700 px-5 py-2.5 rounded-2xl text-sm font-bold flex items-center gap-2 capitalize border border-amber-100">
                  <Zap size={18} className="text-amber-500 fill-amber-500" /> {profile.workPreference === 'both' ? 'Hybrid' : profile.workPreference}
                </div>
              )}
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-16">
            {/* Left Column: Deep Info */}
            <div className="lg:col-span-2 space-y-16">
              <section>
                <h3 className="text-3xl font-black text-gray-900 mb-8 flex items-center gap-4 font-display">
                  <span className="w-12 h-12 rounded-2xl bg-bookup-secondary/10 text-bookup-secondary flex items-center justify-center">
                    <UserIcon size={26} />
                  </span>
                  Biography
                </h3>
                <p className="text-gray-600 font-medium leading-relaxed text-xl whitespace-pre-wrap leading-relaxed">
                  {profile.bio || "Experience top-tier learning with a dedicated professional. I focus on practical results and deep conceptual understanding to help you achieve your goals."}
                </p>
              </section>

              {isTutor && (
                <>
                  <section>
                    <h3 className="text-3xl font-black text-gray-900 mb-8 flex items-center gap-4 font-display">
                      <span className="w-12 h-12 rounded-2xl bg-bookup-primary/10 text-bookup-primary flex items-center justify-center">
                        <BookOpen size={26} />
                      </span>
                      Areas of Expertise
                    </h3>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      {(Array.isArray(profile.tutoringSubjects) ? profile.tutoringSubjects : ['Mathematics', 'Science', 'English', 'Coding']).map((subject: string) => (
                        <div key={subject} className="bg-white border border-gray-100 p-6 rounded-3xl shadow-sm hover:shadow-md transition-all flex items-center justify-between group">
                          <span className="font-black text-lg text-gray-800">{subject}</span>
                          <ChevronRight size={20} className="text-gray-300 group-hover:text-bookup-primary transition-colors" />
                        </div>
                      ))}
                    </div>
                  </section>

                  <section>
                    <h3 className="text-3xl font-black text-gray-900 mb-8 flex items-center gap-4 font-display">
                      <span className="w-12 h-12 rounded-2xl bg-amber-100 text-amber-600 flex items-center justify-center">
                        <GraduationCap size={26} />
                      </span>
                      Education & Experience
                    </h3>
                    <div className="space-y-6 relative before:absolute before:left-6 before:top-2 before:bottom-2 before:w-0.5 before:bg-gray-100">
                      <div className="relative pl-16">
                        <div className="absolute left-4 top-1 w-4 h-4 rounded-full bg-bookup-primary border-4 border-white shadow-sm z-10"></div>
                        <p className="text-sm font-black text-bookup-primary uppercase tracking-widest mb-1">Current</p>
                        <h4 className="text-xl font-black text-gray-900">Professional Educator</h4>
                        <p className="text-gray-500 font-bold">University of Lagos • 5+ Years Experience</p>
                      </div>
                      <div className="relative pl-16">
                        <div className="absolute left-4 top-1 w-4 h-4 rounded-full bg-gray-300 border-4 border-white shadow-sm z-10"></div>
                        <p className="text-sm font-black text-gray-400 uppercase tracking-widest mb-1">2018 - 2021</p>
                        <h4 className="text-xl font-black text-gray-900">Lead Curriculum Developer</h4>
                        <p className="text-gray-500 font-bold">EduTech Solutions</p>
                      </div>
                    </div>
                  </section>
                </>
              )}
            </div>
            
            {/* Right Column: Premium Stats Widget */}
            <div className="space-y-8">
              {isTutor && (
                <div className="bg-gray-900 rounded-[2.5rem] p-10 text-white shadow-2xl relative overflow-hidden">
                  <div className="absolute -top-12 -right-12 w-64 h-64 bg-bookup-primary/20 rounded-full blur-3xl"></div>
                  
                  <h3 className="text-sm font-black text-gray-400 mb-8 uppercase tracking-[0.2em] font-display">Financial Profile</h3>
                  
                  <div className="space-y-10 relative z-10">
                    <div>
                      <p className="text-gray-400 font-bold mb-2">Hourly Investment</p>
                      <div className="flex items-baseline gap-2">
                        <span className="text-3xl font-black text-bookup-primary">$</span>
                        <span className="text-7xl font-black text-white tracking-tighter font-display">{profile.hourlyRate || '45'}</span>
                        <span className="text-xl font-black text-gray-500 uppercase">/ hr</span>
                      </div>
                    </div>
                    
                    <div className="grid grid-cols-2 gap-8 border-t border-white/10 pt-10">
                      <div>
                        <p className="text-gray-400 font-bold text-sm mb-2 uppercase tracking-wider">Avg. Rating</p>
                        <div className="flex items-center gap-3">
                          <Star size={28} className="text-amber-400 fill-amber-400" />
                          <span className="text-3xl font-black font-display">{profile.rating || '4.9'}</span>
                        </div>
                      </div>
                      <div>
                        <p className="text-gray-400 font-bold text-sm mb-2 uppercase tracking-wider">Total Sessions</p>
                        <div className="flex items-center gap-3">
                          <Briefcase size={28} className="text-bookup-secondary" />
                          <span className="text-3xl font-black font-display">{profile.reviewCount || '128'}+</span>
                        </div>
                      </div>
                    </div>

                    <button className="w-full bg-white text-gray-900 py-5 rounded-[1.5rem] font-black text-lg shadow-xl hover:bg-bookup-primary-light transition-all active:scale-95 font-display">
                      Book Now
                    </button>
                  </div>
                </div>
              )}

              <div className="bg-white border-2 border-gray-50 rounded-[2.5rem] p-10 shadow-sm relative group overflow-hidden">
                <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                  <ShieldCheck size={100} className="text-bookup-primary" />
                </div>
                <h4 className="text-2xl font-black text-gray-900 mb-4 font-display">BookUp Verified</h4>
                <p className="text-gray-500 font-medium leading-relaxed">
                  Every interaction is protected by our BookUp Guarantee. Payments are held in escrow until your session is successfully completed.
                </p>
                <div className="mt-8 flex items-center gap-2 text-bookup-primary font-black text-sm uppercase tracking-widest">
                  Read Protection Policy <ChevronRight size={18} />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
