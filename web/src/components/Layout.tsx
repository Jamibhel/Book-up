import { useState, useEffect } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Home, Users, BookOpen, MessageSquare, Bell, Search, Rss, Shield, HelpCircle, Menu, X, Smartphone } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { collection, query, onSnapshot, where, orderBy, limit, updateDoc, doc } from 'firebase/firestore';
import { db } from '../lib/firebase';

export default function Layout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { currentUser, userProfile } = useAuth();
  const [notificationCount, setNotificationCount] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [showDownloadPrompt, setShowDownloadPrompt] = useState(false);
  const [deviceType, setDeviceType] = useState<'ios' | 'android' | 'desktop'>('desktop');

  useEffect(() => {
    const ua = navigator.userAgent.toLowerCase();
    const isMobile = /iphone|ipad|ipod|android/i.test(ua);
    if (isMobile) {
      setDeviceType(/iphone|ipad|ipod/i.test(ua) ? 'ios' : 'android');
      const dismissed = sessionStorage.getItem('dismissedDownloadPrompt');
      if (!dismissed) {
        setShowDownloadPrompt(true);
      }
    }
  }, []);

  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: Home },
    { name: 'Users', path: '/tutors', icon: Users },
    { name: 'Messages', path: '/messages', icon: MessageSquare },
    { name: 'Materials', path: '/materials', icon: BookOpen },
    { name: 'Requests', path: '/requests', icon: HelpCircle },
    { name: 'Feed', path: '/feed', icon: Rss },
    { name: 'Get the App', path: '/download', icon: Smartphone },
  ];

  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState<any[]>([]);

  useEffect(() => {
    if (!currentUser) return;

    // Real-time notifications
    const q = query(
      collection(db, 'notifications', currentUser.uid, 'messages'),
      orderBy('timestamp', 'desc'),
      limit(5)
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const fetched: any[] = [];
      let unread = 0;
      snapshot.forEach((doc) => {
        const data = doc.data();
        fetched.push({ id: doc.id, ...data });
        if (!data.read) unread++;
      });
      setNotifications(fetched);
      setNotificationCount(unread);
    });

    return () => unsubscribe();
  }, [currentUser]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/tutors?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const markAllRead = async () => {
    if (!currentUser) return;
    try {
      notifications.forEach(async (notif) => {
        if (!notif.read) {
          await updateDoc(doc(db, 'notifications', currentUser.uid, 'messages', notif.id), { read: true });
        }
      });
      setShowNotifications(false);
    } catch (err) {
      console.error(err);
    }
  };

  const profilePhoto = userProfile?.photoURL || userProfile?.photoUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(userProfile?.displayName || currentUser?.displayName || 'User')}&background=2E8B57&color=fff&bold=true`;

  return (
    <div className="min-h-screen flex p-0 md:p-6 md:gap-6 overflow-hidden max-h-screen bg-white md:bg-transparent">
      
      {/* Desktop Sidebar */}
      <aside className="hidden md:flex flex-col w-72 glass rounded-3xl h-[calc(100vh-3rem)] overflow-hidden shrink-0 relative">
        <div className="absolute top-0 right-0 w-32 h-32 bg-bookup-primary/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
        
        <div className="p-8 relative z-10 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-bookup-primary to-bookup-secondary flex items-center justify-center text-white font-bold text-xl shadow-lg shadow-bookup-primary/30">
              B
            </div>
            <span className="text-2xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-bookup-primary-dark to-bookup-secondary font-display">
              BookUp
            </span>
          </Link>
        </div>

        <nav className="flex-1 px-4 space-y-2 relative z-10 overflow-y-auto custom-scrollbar">
          {navItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            const Icon = item.icon;
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex items-center space-x-4 px-5 py-4 rounded-2xl transition-all duration-300 group ${
                  isActive 
                    ? 'bg-bookup-primary text-white shadow-md shadow-bookup-primary/20 scale-[1.02]' 
                    : 'text-gray-500 hover:bg-white hover:text-bookup-primary hover:shadow-sm'
                }`}
              >
                <Icon size={22} className={isActive ? 'text-white' : 'text-gray-400 group-hover:text-bookup-primary transition-colors'} strokeWidth={isActive ? 2.5 : 2} />
                <span className={`font-black uppercase tracking-widest text-[10px] ${isActive ? 'text-white' : 'text-gray-500'}`}>{item.name}</span>
              </Link>
            );
          })}
        </nav>

        <div className="p-4 mt-auto relative z-10">
          <Link to="/profile" className="flex items-center gap-4 p-4 rounded-2xl hover:bg-white transition-all cursor-pointer border border-transparent hover:border-gray-100 hover:shadow-sm">
            <img 
              src={profilePhoto} 
              alt="Profile" 
              className="w-12 h-12 rounded-[1rem] ring-2 ring-white shadow-sm object-cover" 
            />
            <div className="flex-1 min-w-0">
              <p className="font-black text-gray-900 truncate font-display">{userProfile?.displayName || currentUser?.displayName || 'User'}</p>
              <p className="text-[10px] font-black text-bookup-primary truncate uppercase tracking-tighter">{currentUser?.email}</p>
            </div>
          </Link>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 h-[100vh] md:h-[calc(100vh-3rem)] relative">
        
        {/* Download App Prompt (Mobile only) */}
        {showDownloadPrompt && (
          <div className="md:hidden bg-gradient-to-r from-gray-900 to-bookup-primary text-white px-6 py-4 flex items-center justify-between gap-4 animate-in slide-in-from-top duration-500 z-50">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center">
                <Smartphone size={20} />
              </div>
              <div>
                <p className="font-black text-sm leading-tight">Get the BookUp App</p>
                <p className="text-[10px] font-bold text-white/70 uppercase tracking-widest">Better on Mobile</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <button 
                onClick={() => navigate('/download')}
                className="bg-white text-gray-900 px-4 py-2 rounded-lg font-black text-[10px] uppercase tracking-widest shadow-lg"
              >
                Download
              </button>
              <button 
                onClick={() => {
                  setShowDownloadPrompt(false);
                  sessionStorage.setItem('dismissedDownloadPrompt', 'true');
                }}
                className="p-1 hover:bg-white/10 rounded-full transition-colors"
              >
                <X size={18} />
              </button>
            </div>
          </div>
        )}

        {/* Top Header */}
        <header className="h-16 md:h-20 mb-0 md:mb-6 glass md:rounded-2xl flex items-center justify-between px-4 md:px-6 shrink-0 z-40 border-b md:border-b-0 border-gray-100">
          
          <Link to="/dashboard" className="md:hidden flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-bookup-primary to-bookup-secondary flex items-center justify-center text-white font-bold text-base shadow-sm">
              B
            </div>
            <span className="text-xl font-extrabold tracking-tight text-gray-900 font-display">
              BookUp
            </span>
          </Link>

          <form onSubmit={handleSearch} className="relative flex-1 max-w-md hidden sm:block group ml-4 md:ml-0">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400 group-focus-within:text-bookup-primary transition-colors" />
            <input 
              type="text" 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search tutors, materials..." 
              className="w-full pl-12 pr-4 py-2.5 bg-white/50 border border-gray-200 rounded-full focus:bg-white focus:ring-2 focus:ring-bookup-primary/20 focus:border-bookup-primary outline-none transition-all placeholder-gray-400 font-bold text-sm md:text-base"
            />
          </form>
          
          <div className="flex items-center gap-3 md:gap-4 ml-auto relative">
            <button onClick={() => navigate('/tutors')} className="sm:hidden p-2.5 rounded-full bg-white text-gray-600 shadow-sm border border-gray-100 active:scale-95 transition-transform">
              <Search size={20} />
            </button>
            
            <button 
              onClick={() => setShowNotifications(!showNotifications)}
              className={`relative p-2.5 md:p-3 rounded-full transition-all border active:scale-95 ${showNotifications ? 'bg-bookup-primary text-white border-bookup-primary shadow-lg shadow-bookup-primary/20' : 'bg-white text-gray-600 border-gray-100 hover:text-bookup-primary hover:shadow-md'}`}
            >
              <Bell size={20} />
              {notificationCount > 0 && (
                <span className="absolute top-2 right-2 min-w-[18px] h-[18px] px-1 bg-bookup-accent rounded-full border-2 border-white text-[10px] font-black text-white flex items-center justify-center animate-bounce">
                  {notificationCount > 9 ? '9+' : notificationCount}
                </span>
              )}
            </button>

            {/* Notifications Dropdown */}
            {showNotifications && (
              <div className="absolute top-full right-0 mt-4 w-80 glass rounded-[2rem] shadow-2xl border border-white/50 p-6 animate-in slide-in-from-top-4 duration-300 z-50">
                <div className="flex justify-between items-center mb-6">
                  <h3 className="font-black text-gray-900 tracking-tight">Notifications</h3>
                  <button onClick={markAllRead} className="text-[10px] font-black text-bookup-primary uppercase tracking-widest hover:underline">Mark all as read</button>
                </div>
                <div className="space-y-4 max-h-96 overflow-y-auto no-scrollbar">
                  {notifications.length > 0 ? (
                    notifications.map((n) => (
                      <div key={n.id} className={`p-4 rounded-2xl transition-all border ${n.read ? 'bg-gray-50/50 border-transparent' : 'bg-white border-bookup-primary/10 shadow-sm'}`}>
                        <p className={`text-sm ${n.read ? 'text-gray-500' : 'text-gray-900 font-bold'}`}>{n.message || n.title}</p>
                        <p className="text-[10px] font-black text-gray-400 mt-2 uppercase tracking-tighter">Recently</p>
                      </div>
                    ))
                  ) : (
                    <div className="text-center py-10">
                      <p className="text-gray-400 font-bold">All caught up! ✌️</p>
                    </div>
                  )}
                </div>
              </div>
            )}

            <Link to="/profile" className="md:hidden ml-1">
               <img 
                 src={profilePhoto} 
                 alt="Profile" 
                 className="w-9 h-9 rounded-full ring-2 ring-white shadow-sm object-cover" 
               />
            </Link>
          </div>
        </header>

        {/* Scrollable Page Content */}
        <main className="flex-1 overflow-y-auto md:rounded-3xl pb-24 md:pb-10 custom-scrollbar px-4 md:px-0 bg-gray-50 md:bg-transparent pt-4 md:pt-0">
          <Outlet />
        </main>
      </div>

      {/* Improved Mobile Bottom Navigation Bar (Horizontally Scrollable) */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-white/80 backdrop-blur-lg border-t border-gray-100 z-50 flex items-center overflow-x-auto no-scrollbar py-2 px-4 shadow-[0_-10px_20px_-5px_rgba(0,0,0,0.05)]">
        <div className="flex items-center gap-2 min-w-max mx-auto pb-[env(safe-area-inset-bottom)]">
          {navItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            const Icon = item.icon;
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex flex-col items-center gap-1 px-4 py-1.5 rounded-2xl transition-all ${isActive ? 'bg-bookup-primary text-white shadow-lg shadow-bookup-primary/20' : 'text-gray-400'}`}
              >
                <Icon size={20} strokeWidth={isActive ? 2.5 : 2} />
                <span className={`text-[9px] font-black uppercase tracking-tighter ${isActive ? 'text-white' : 'text-gray-400'}`}>
                  {item.name}
                </span>
              </Link>
            );
          })}
          <Link
            to="/profile"
            className={`flex flex-col items-center gap-1 px-4 py-1.5 rounded-2xl transition-all ${location.pathname === '/profile' ? 'bg-bookup-primary text-white shadow-lg shadow-bookup-primary/20' : 'text-gray-400'}`}
          >
            <img 
              src={profilePhoto} 
              alt="Profile" 
              className={`w-5 h-5 rounded-full object-cover ${location.pathname === '/profile' ? 'ring-2 ring-white' : ''}`}
            />
            <span className={`text-[9px] font-black uppercase tracking-tighter ${location.pathname === '/profile' ? 'text-white' : 'text-gray-400'}`}>
              Profile
            </span>
          </Link>
        </div>
      </nav>
      
    </div>
  );
}

