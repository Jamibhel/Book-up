import { useState, useEffect } from 'react';
import { Search, Star, MapPin, Clock, Globe, UserX } from 'lucide-react';
import { useLocalizedPricing } from '../hooks/useLocalizedPricing';
import { collection, query, getDocs, doc, getDoc, setDoc, serverTimestamp } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { CATEGORIZED_SKILLS } from '../data/skills';


interface Tutor {
  id: string;
  displayName: string;
  firstName?: string;
  lastName?: string;
  role: string;
  tutoringSubjects: string[];
  hourlyRate: number;
  locationName?: string;
  rating?: number;
  reviewCount?: number;
  isAvailable?: boolean;
  bio?: string;
  photoUrl?: string;
  photoURL?: string;
  workPreference?: string;
}

export default function TutorDirectory() {
  const { currentUser } = useAuth();
  const navigate = useNavigate();
  const { formatPrice, currency, loading: pricingLoading } = useLocalizedPricing();
  const [tutors, setTutors] = useState<Tutor[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSubject, setSelectedSubject] = useState('All Subjects');

  const categorizedSkills = CATEGORIZED_SKILLS;

  useEffect(() => {
    async function fetchTutors() {
      try {
        const q = query(collection(db, 'users'));
        const querySnapshot = await getDocs(q);
        const fetchedTutors: Tutor[] = [];
        querySnapshot.forEach((docSnap) => {
          const data = docSnap.data();
          fetchedTutors.push({ id: docSnap.id, ...data } as Tutor);
        });
        setTutors(fetchedTutors);
      } catch (err: any) {
        console.error("Error fetching tutors:", err);
        setError('Failed to load tutors.');
      } finally {
        setLoading(false);
      }
    }

    fetchTutors();
  }, []);

  const filteredTutors = tutors.filter(tutor => {
    const name = getTutorName(tutor).toLowerCase();
    const matchesSearch = name.includes(searchQuery.toLowerCase()) || 
                          (Array.isArray(tutor.tutoringSubjects) && tutor.tutoringSubjects.some(s => s.toLowerCase().includes(searchQuery.toLowerCase())));
    const matchesSubject = selectedSubject === 'All Subjects' || 
                           (Array.isArray(tutor.tutoringSubjects) && tutor.tutoringSubjects.includes(selectedSubject));
    return matchesSearch && matchesSubject;
  });

  const getTutorName = (tutor: Tutor) => {
    if (tutor.displayName) return tutor.displayName;
    if (tutor.firstName && tutor.lastName) return `${tutor.firstName} ${tutor.lastName}`;
    return 'Unknown User';
  };

  const handleMessage = async (e: React.MouseEvent, targetUser: Tutor) => {
    e.stopPropagation();
    if (!currentUser) return alert("Please log in to send a message.");
    
    if (currentUser.uid === targetUser.id) {
      return alert("You can't message yourself.");
    }
    
    try {
      const channelId = [currentUser.uid, targetUser.id].sort().join('_');
      // The Android app uses 'conversations' as the MODERN_CHANNELS collection
      const channelRef = doc(db, 'conversations', channelId);
      const snap = await getDoc(channelRef);
      
      if (!snap.exists()) {
        await setDoc(channelRef, {
          id: channelId,
          participantIds: [currentUser.uid, targetUser.id],
          participantNames: {
            [currentUser.uid]: currentUser.displayName || currentUser.email || 'Me',
            [targetUser.id]: getTutorName(targetUser)
          },
          participantPhotos: {
            [currentUser.uid]: currentUser.photoURL || '',
            [targetUser.id]: targetUser.photoUrl || ''
          },
          isGroup: false,
          lastMessage: '',
          lastMessageTimestamp: serverTimestamp()
        });
      }
      
      navigate('/messages');
    } catch (err) {
      console.error(err);
      alert("Failed to start chat.");
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-6 mb-8 border-b border-gray-100 pb-8">
        <div className="flex-1">
          <h1 className="text-4xl font-extrabold text-bookup-text tracking-tight">Users Directory</h1>
          <div className="flex flex-wrap items-center gap-2 mt-2">
            <p className="text-lg text-bookup-text-muted">Connect with tutors and peers in the BookUp community.</p>
            {!pricingLoading && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-bookup-primary/10 text-bookup-primary rounded-full text-sm font-bold">
                <Globe size={16} /> Showing prices in {currency}
              </span>
            )}
          </div>
        </div>
        
        <div className="flex flex-col sm:flex-row gap-4 w-full md:w-auto">
          <select 
            value={selectedSubject}
            onChange={(e) => setSelectedSubject(e.target.value)}
            className="px-6 py-3.5 bg-white border border-gray-100 rounded-2xl shadow-sm focus:ring-2 focus:ring-bookup-primary focus:border-bookup-primary outline-none font-bold text-gray-700 transition-all cursor-pointer"
          >
            <option value="All Subjects">All Subjects</option>
            {Object.entries(categorizedSkills).map(([category, skills]) => (
              <optgroup key={category} label={category} className="font-black text-xs text-bookup-primary uppercase tracking-widest bg-gray-50 py-2">
                {skills.map(skill => (
                  <option key={skill} value={skill} className="font-bold text-sm text-gray-700 bg-white capitalize py-1">
                    {skill}
                  </option>
                ))}
              </optgroup>
            ))}
          </select>

          <div className="relative flex-1 sm:w-64 md:w-80">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <Search className="h-5 w-5 text-bookup-text-muted" />
            </div>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="block w-full pl-12 pr-4 py-3.5 bg-white border border-gray-100 rounded-2xl focus:ring-2 focus:ring-bookup-primary focus:border-bookup-primary outline-none transition-all shadow-sm font-bold text-gray-700"
              placeholder="Search users..."
            />
          </div>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center items-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-bookup-primary"></div>
        </div>
      ) : error ? (
        <div className="bg-red-50 text-red-600 p-6 rounded-2xl font-bold border border-red-100">
          {error}
        </div>
      ) : filteredTutors.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-center">
          <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center text-gray-400 mb-4">
            <UserX size={40} />
          </div>
          <h3 className="text-2xl font-bold text-gray-900">No users found</h3>
          <p className="text-gray-500 mt-2 font-medium">Try adjusting your search or filters.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 pb-10">
          {filteredTutors.map((tutor) => (
            <div 
              key={tutor.id} 
              onClick={() => navigate(`/user/${tutor.id}`)}
              className="bg-white rounded-[2rem] shadow-sm border border-gray-100 overflow-hidden hover:shadow-xl transition-all duration-300 group hover:-translate-y-1 cursor-pointer"
            >
              <div className="p-8 flex flex-col sm:flex-row gap-8">
                <div className="shrink-0 relative">
                  <img 
                    src={tutor.photoUrl || tutor.photoURL || `https://ui-avatars.com/api/?name=${getTutorName(tutor)}&background=2E8B57&color=fff&bold=true`} 
                    alt={getTutorName(tutor)} 
                    className="w-28 h-28 rounded-2xl object-cover shadow-md group-hover:scale-105 transition-transform duration-300" 
                  />
                  <div className="absolute -bottom-3 -right-3 bg-white rounded-xl p-1 shadow-sm border border-gray-100">
                    <div className="bg-green-100 text-green-600 px-2 py-1 rounded-lg text-xs font-black flex items-center gap-1 uppercase">
                      {tutor.role || 'student'}
                    </div>
                  </div>
                </div>
                
                <div className="flex-1 min-w-0 flex flex-col justify-between">
                  <div>
                    <div className="flex justify-between items-start gap-4">
                      <div className="min-w-0">
                        <h3 className="text-2xl font-black text-gray-900 group-hover:text-bookup-primary transition-colors truncate">{getTutorName(tutor)}</h3>
                        <div className="flex flex-wrap gap-1.5 mt-2.5">
                          {Array.isArray(tutor.tutoringSubjects) && tutor.tutoringSubjects.length > 0 ? (
                            <>
                              {tutor.tutoringSubjects.slice(0, 3).map((sub, index) => (
                                <span 
                                  key={index}
                                  className="inline-flex items-center px-3 py-1 bg-bookup-primary/5 text-bookup-primary border border-bookup-primary/10 rounded-xl text-[10px] font-black uppercase tracking-wider"
                                >
                                  {sub}
                                </span>
                              ))}
                              {tutor.tutoringSubjects.length > 3 && (
                                <span className="inline-flex items-center px-2 py-1 bg-gray-100 text-gray-600 rounded-xl text-[10px] font-black">
                                  +{tutor.tutoringSubjects.length - 3}
                                </span>
                              )}
                            </>
                          ) : (
                            <span className="inline-flex items-center px-3 py-1 bg-gray-100 text-gray-600 rounded-xl text-[10px] font-black uppercase tracking-wider">
                              {tutor.role === 'tutor' ? 'General Tutor' : 'Student'}
                            </span>
                          )}
                        </div>
                      </div>
                      {tutor.role === 'tutor' && tutor.hourlyRate && (
                        <div className="shrink-0 text-right">
                          <span className="font-black text-2xl text-gray-900 block leading-none">
                            {formatPrice(Number(tutor.hourlyRate) || 0)}
                          </span>
                          <span className="text-sm font-bold text-gray-400">/hour</span>
                        </div>
                      )}
                    </div>
                    
                    <div className="mt-6 flex flex-wrap items-center gap-4 text-sm font-bold text-gray-500">
                      <div className="flex items-center gap-1.5 bg-gray-50 px-3 py-1.5 rounded-lg border border-gray-100">
                        <Star className="w-4 h-4 text-bookup-accent fill-bookup-accent" />
                        <span className="text-gray-900">{tutor.rating || 0}</span>
                        <span className="text-gray-400">({tutor.reviewCount || 0})</span>
                      </div>
                      <div className="flex items-center gap-1.5 bg-gray-50 px-3 py-1.5 rounded-lg border border-gray-100">
                        <MapPin className="w-4 h-4 text-bookup-secondary" />
                        <span>{tutor.locationName || 'Online'}</span>
                      </div>
                      <div className="flex items-center gap-1.5 bg-gray-50 px-3 py-1.5 rounded-lg border border-gray-100">
                        <Clock className="w-4 h-4 text-bookup-secondary" />
                        <span>{tutor.workPreference === 'both' ? 'Online & In-person' : tutor.workPreference || 'Online'}</span>
                      </div>
                    </div>
                  </div>

                  <div className="mt-8 flex gap-4">
                    <button onClick={(e) => { e.stopPropagation(); navigate(`/user/${tutor.id}`); }} className="flex-1 bg-gray-900 text-white px-6 py-3.5 rounded-xl font-bold hover:bg-bookup-primary transition-colors shadow-sm text-center">
                      View Profile
                    </button>
                    <button 
                      onClick={(e) => handleMessage(e, tutor)}
                      className="px-6 py-3.5 bg-white border-2 border-gray-200 text-gray-700 rounded-xl font-bold hover:border-bookup-primary hover:text-bookup-primary transition-colors text-center"
                    >
                      Message
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
