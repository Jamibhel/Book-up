import { useState, useEffect } from 'react';
import { BookOpen, Calendar, Clock, Award, ArrowRight, Video, Users, HelpCircle, Rss, Shield, FileText } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { collection, query, where, onSnapshot, orderBy, limit } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { Link } from 'react-router-dom';

interface Booking {
  id: string;
  tutorName: string;
  studentName: string;
  sessionDate: any;
  subject: string;
  status: string;
  tutorId?: string;
}

export default function Dashboard() {
  const { currentUser } = useAuth();
  const [upcomingSessions, setUpcomingSessions] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [posts, setPosts] = useState<any[]>([]);
  const [loadingPosts, setLoadingPosts] = useState(true);
  const [helpRequests, setHelpRequests] = useState<any[]>([]);
  const [loadingRequests, setLoadingRequests] = useState(true);

  useEffect(() => {
    if (!currentUser) return;
    
    // Real-time bookings
    const qBookings = query(
      collection(db, 'bookings'),
      where('participantIds', 'array-contains', currentUser.uid)
    );
    
    const unsubscribeBookings = onSnapshot(qBookings, 
      (snapshot) => {
        const bookings: Booking[] = [];
        snapshot.forEach((doc) => {
          bookings.push({ id: doc.id, ...doc.data() } as Booking);
        });
        
        bookings.sort((a, b) => {
          const dateA = a.sessionDate?.toDate ? a.sessionDate.toDate() : new Date(0);
          const dateB = b.sessionDate?.toDate ? b.sessionDate.toDate() : new Date(0);
          return dateA.getTime() - dateB.getTime();
        });
        
        setUpcomingSessions(bookings.filter(b => b.status !== 'cancelled'));
        setLoading(false);
      },
      (error) => {
        console.error("Dashboard bookings load error:", error);
        setUpcomingSessions([]);
        setLoading(false);
      }
    );

    // Real-time news feed (top 3)
    const qNews = query(collection(db, 'newsFeed'), orderBy('timestamp', 'desc'), limit(3));
    const unsubscribeNews = onSnapshot(qNews, 
      (snapshot) => {
        const fetchedPosts: any[] = [];
        snapshot.forEach((doc) => {
          fetchedPosts.push({ id: doc.id, ...doc.data() });
        });
        setPosts(fetchedPosts);
        setLoadingPosts(false);
      },
      (error) => {
        console.error("Dashboard news load error:", error);
        setPosts([]);
        setLoadingPosts(false);
      }
    );

    // Real-time help requests (top 4)
    const qRequests = query(collection(db, 'helpRequests'), orderBy('timestamp', 'desc'), limit(4));
    const unsubscribeRequests = onSnapshot(qRequests, 
      (snapshot) => {
        const fetchedRequests: any[] = [];
        snapshot.forEach((doc) => {
          fetchedRequests.push({ id: doc.id, ...doc.data() });
        });
        setHelpRequests(fetchedRequests);
        setLoadingRequests(false);
      },
      (error) => {
        console.error("Dashboard help requests load error:", error);
        setHelpRequests([]);
        setLoadingRequests(false);
      }
    );

    return () => {
      unsubscribeBookings();
      unsubscribeNews();
      unsubscribeRequests();
    };
  }, [currentUser]);

  const formatSessionTime = (timestamp: any) => {
    if (!timestamp) return 'TBD';
    const date = timestamp.toDate ? timestamp.toDate() : new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ' - ' + date.toLocaleDateString();
  };

  const formatTime = (timestamp: any) => {
    if (!timestamp) return 'Recently';
    const date = timestamp.toDate ? timestamp.toDate() : new Date(timestamp);
    const now = new Date();
    const diffInHours = Math.abs(now.getTime() - date.getTime()) / 3600000;
    if (diffInHours < 1) return 'Just now';
    if (diffInHours < 24) return `${Math.floor(diffInHours)}h ago`;
    return date.toLocaleDateString();
  };

  return (
    <div className="space-y-12 animate-in fade-in slide-in-from-bottom-4 duration-700 pb-20">
      
      {/* Welcome Header with Mesh Gradient */}
      <div className="relative p-10 md:p-16 rounded-[3.5rem] overflow-hidden bg-gray-900 text-white shadow-2xl">
        <div className="absolute top-0 right-0 w-96 h-96 bg-bookup-primary/20 rounded-full blur-[120px] -mr-48 -mt-48"></div>
        <div className="absolute bottom-0 left-0 w-64 h-64 bg-bookup-secondary/10 rounded-full blur-[80px] -ml-32 -mb-32"></div>
        
        <div className="relative z-10">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-12 h-12 rounded-2xl bg-white/10 flex items-center justify-center backdrop-blur-md border border-white/10">
               <Award size={24} className="text-bookup-primary-light" />
            </div>
            <span className="text-sm font-black uppercase tracking-[0.3em] text-white/50">Your Student Journey</span>
          </div>
          <h1 className="text-5xl md:text-7xl font-black tracking-tight mb-4 font-display leading-tight">
            Welcome back, <br />
            <span className="text-bookup-primary-light">{currentUser?.displayName?.split(' ')[0] || 'Scholar'}</span>! 👋
          </h1>
          <p className="text-xl text-white/60 font-medium max-w-2xl">
            You have {upcomingSessions.length} sessions scheduled this week. Let's make progress today.
          </p>
        </div>
      </div>

      {/* Modern Quick Actions Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
         {[
           { title: 'Marketplace', desc: 'Post a help request', color: 'bg-emerald-600', icon: HelpCircle, path: '/requests' },
           { title: 'Tutor Directory', desc: 'Find expert guidance', color: 'bg-indigo-600', icon: Users, path: '/tutors' },
           { title: 'Study Hub', desc: 'Access all materials', color: 'bg-amber-500', icon: BookOpen, path: '/materials' },
         ].map((action, i) => (
           <Link key={i} to={action.path} className="group relative bg-white p-10 rounded-[3rem] border border-gray-100 shadow-sm hover:shadow-2xl transition-all duration-500 hover:-translate-y-2 overflow-hidden">
              <div className={`absolute top-0 right-0 w-32 h-32 ${action.color} opacity-[0.03] rounded-full -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-700`}></div>
              <div className={`w-16 h-16 ${action.color} text-white rounded-3xl flex items-center justify-center mb-8 shadow-xl shadow-gray-200 group-hover:rotate-6 transition-transform`}>
                 <action.icon size={32} />
              </div>
              <h3 className="text-2xl font-black text-gray-900 mb-2">{action.title}</h3>
              <p className="text-gray-500 font-bold text-sm mb-8">{action.desc}</p>
              <div className="flex items-center gap-2 text-gray-900 font-black text-xs uppercase tracking-widest">
                 Explore Now <ArrowRight size={16} className="group-hover:translate-x-2 transition-transform" />
              </div>
           </Link>
         ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-12">
        <div className="lg:col-span-2 space-y-12">
          
          {/* Upcoming Sessions - Glassmorphic List */}
          <section className="space-y-8">
            <div className="flex justify-between items-end px-4">
              <h2 className="text-4xl font-black text-gray-900 tracking-tight font-display">Schedule</h2>
              <Link to="/tutors" className="text-bookup-primary font-black hover:underline flex items-center gap-2 text-xs uppercase tracking-widest">
                View Full Calendar <ArrowRight size={16} />
              </Link>
            </div>

            {loading ? (
              <div className="bg-white rounded-[3rem] p-20 border border-gray-100 flex justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-bookup-primary"></div>
              </div>
            ) : upcomingSessions.length === 0 ? (
              <div className="bg-gray-50 rounded-[3rem] p-16 border-2 border-dashed border-gray-200 text-center">
                <div className="w-24 h-24 bg-white rounded-full flex items-center justify-center mx-auto mb-8 shadow-sm">
                  <Calendar size={48} className="text-gray-300" />
                </div>
                <h3 className="text-2xl font-black text-gray-900">Your schedule is open</h3>
                <p className="text-gray-500 mt-4 font-medium text-lg max-w-sm mx-auto">Book your next session with a top-rated tutor to stay on track.</p>
              </div>
            ) : (
              <div className="space-y-6">
                {upcomingSessions.map((session) => (
                  <div key={session.id} className="bg-white p-8 rounded-[2.5rem] border border-gray-100 shadow-sm hover:shadow-xl transition-all flex flex-col md:flex-row items-center justify-between gap-6 group">
                    <div className="flex items-center gap-8 w-full md:w-auto">
                      <div className="w-20 h-20 bg-emerald-50 rounded-3xl flex flex-col items-center justify-center text-emerald-600 group-hover:bg-bookup-primary group-hover:text-white transition-all duration-500">
                        <Video size={32} />
                        <span className="text-[10px] font-black mt-1 uppercase tracking-tighter">HD Live</span>
                      </div>
                      <div>
                        <h4 className="text-2xl font-black text-gray-900 group-hover:text-bookup-primary transition-colors">{session.subject}</h4>
                        <div className="flex flex-wrap items-center gap-4 mt-2">
                           <p className="text-gray-500 font-bold flex items-center gap-2 text-sm">
                             <Users size={16} className="text-bookup-primary" />
                             {currentUser?.uid === session.tutorId ? session.studentName : session.tutorName}
                           </p>
                           <span className="w-1.5 h-1.5 bg-gray-200 rounded-full"></span>
                           <p className="text-gray-400 font-bold text-sm flex items-center gap-2">
                             <Clock size={16} />
                             {formatSessionTime(session.sessionDate)}
                           </p>
                        </div>
                      </div>
                    </div>
                    <button className="w-full md:w-auto bg-gray-900 text-white px-10 py-5 rounded-2xl font-black hover:bg-bookup-primary transition-all shadow-xl active:scale-95 text-lg">
                      Enter Classroom
                    </button>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* Active Help Requests */}
          <section className="space-y-8">
            <div className="flex justify-between items-end px-4">
              <h2 className="text-4xl font-black text-gray-900 tracking-tight font-display">Active Requests</h2>
            </div>

            {loadingRequests ? (
               <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  {[1,2].map(i => <div key={i} className="h-56 bg-gray-100 rounded-[3rem] animate-pulse"></div>)}
               </div>
            ) : (
               <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  {helpRequests.map((request) => (
                    <div key={request.id} className="bg-white p-10 rounded-[3rem] border border-gray-100 shadow-sm hover:shadow-xl transition-all group relative overflow-hidden">
                       <div className="flex justify-between items-start mb-6">
                          <span className="bg-indigo-50 text-indigo-600 px-5 py-2 rounded-xl text-[10px] font-black uppercase tracking-[0.2em] border border-indigo-100/50">
                             {request.subject}
                          </span>
                          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">{formatTime(request.timestamp)}</span>
                       </div>
                       <h4 className="text-2xl font-black text-gray-900 mb-4 group-hover:text-indigo-600 transition-colors line-clamp-1 leading-tight">{request.title}</h4>
                       <p className="text-gray-500 text-base font-medium line-clamp-2 mb-8">{request.description}</p>
                       <div className="flex items-center gap-3">
                          <div className="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 text-[10px] font-black uppercase">
                             {request.requestedByName?.[0] || 'U'}
                          </div>
                          <span className="text-xs font-black text-gray-900 uppercase tracking-widest">{request.requestedByName}</span>
                       </div>
                    </div>
                  ))}
               </div>
            )}
          </section>

        </div>

        {/* Sidebar News Feed - Premium Glassmorphic */}
        <aside className="space-y-10">
          <div className="bg-gradient-to-br from-gray-900 via-gray-900 to-[#0f3421] rounded-[3.5rem] p-10 text-white shadow-2xl relative overflow-hidden flex flex-col min-h-[600px]">
            <div className="absolute top-0 right-0 w-64 h-64 bg-bookup-primary/10 rounded-full blur-[100px] -mr-32 -mt-32"></div>
            
            <div className="flex items-center justify-between mb-10 relative z-10">
               <h2 className="text-3xl font-black font-display">Pulse</h2>
               <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center">
                  <Rss size={20} className="text-bookup-primary-light" />
               </div>
            </div>
            
            <div className="space-y-10 relative z-10 flex-1">
              {loadingPosts ? (
                [1,2,3].map(i => (
                  <div key={i} className="space-y-4 animate-pulse opacity-50">
                    <div className="h-3 w-20 bg-white/20 rounded"></div>
                    <div className="h-6 w-full bg-white/20 rounded-lg"></div>
                    <div className="h-20 w-full bg-white/20 rounded-2xl"></div>
                  </div>
                ))
              ) : posts.map((post) => (
                  <div key={post.id} className="group cursor-pointer">
                    <div className="flex items-center justify-between mb-3">
                      <span className="text-[10px] font-black uppercase tracking-[0.2em] text-bookup-primary-light">{post.authorRole || 'Update'}</span>
                      <span className="text-[10px] font-bold text-white/30">{formatTime(post.timestamp)}</span>
                    </div>
                    <h4 className="font-black text-xl group-hover:text-bookup-primary-light transition-colors line-clamp-2 leading-snug">{post.title || post.headline}</h4>
                    <p className="text-sm text-white/50 line-clamp-2 mt-3 leading-relaxed font-medium">{post.content}</p>
                    {post.imageUrl && (
                       <img src={post.imageUrl} className="w-full h-32 object-cover rounded-2xl mt-5 opacity-70 group-hover:opacity-100 transition-all border border-white/5 shadow-lg" alt="" />
                    )}
                    <div className="mt-6 h-px bg-white/5 w-full"></div>
                  </div>
                ))}
            </div>
            
            <Link to="/feed" className="mt-12 group flex items-center justify-between w-full p-6 bg-white/5 hover:bg-white/10 rounded-3xl font-black transition-all border border-white/10 relative z-10 overflow-hidden">
               <div className="absolute inset-0 bg-gradient-to-r from-bookup-primary/0 via-bookup-primary/5 to-bookup-primary/0 translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-1000"></div>
               <span className="text-sm uppercase tracking-widest">Full Feed</span>
               <ArrowRight size={20} className="group-hover:translate-x-2 transition-transform" />
            </Link>
          </div>

          {/* Quick Stats Card */}
          <div className="bg-white rounded-[3rem] p-10 border border-gray-100 shadow-sm relative overflow-hidden group">
             <div className="absolute -bottom-10 -right-10 opacity-[0.03] group-hover:scale-110 transition-transform duration-700">
                <Shield size={200} />
             </div>
             <h3 className="text-xs font-black text-gray-400 uppercase tracking-[0.3em] mb-8">System Status</h3>
             <div className="space-y-6">
                {[
                  { label: 'Security', val: 'Protected', color: 'text-emerald-500' },
                  { label: 'Cloud Sync', val: 'Active', color: 'text-bookup-primary' },
                  { label: 'Network', val: 'Optimal', color: 'text-bookup-secondary' },
                ].map((s, i) => (
                  <div key={i} className="flex justify-between items-center">
                     <span className="text-sm font-bold text-gray-500">{s.label}</span>
                     <span className={`text-xs font-black uppercase tracking-widest ${s.color}`}>{s.val}</span>
                  </div>
                ))}
             </div>
          </div>
        </aside>
      </div>
    </div>
  );
}
