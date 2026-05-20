import { useState, useEffect } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Home, Users, BookOpen, MessageSquare, Bell, Search, Rss, Shield, HelpCircle, Menu, X, Smartphone, Sun, Moon } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { collection, query, onSnapshot, where, orderBy, limit, updateDoc, doc } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useTheme } from '../hooks/useTheme';

export default function Layout() {
  const { theme, toggleTheme } = useTheme();
  const location = useLocation();
  const navigate = useNavigate();
  const { currentUser, userProfile } = useAuth();
  const [notificationCount, setNotificationCount] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [showDownloadPrompt, setShowDownloadPrompt] = useState(false);
  const [deviceType, setDeviceType] = useState<'ios' | 'android' | 'desktop'>('desktop');
  const [isMobileDrawerOpen, setIsMobileDrawerOpen] = useState(false);

  useEffect(() => {
    setIsMobileDrawerOpen(false);
  }, [location.pathname]);

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
      collection(db, 'notifications', currentUser.uid, 'messages')
    );

    const unsubscribe = onSnapshot(q, 
      (snapshot) => {
        let fetched: any[] = [];
        let unread = 0;
        snapshot.forEach((doc) => {
          const data = doc.data();
          fetched.push({ id: doc.id, ...data });
          if (!data.read) unread++;
        });

        // Sort by timestamp descending
        fetched.sort((a, b) => {
          const timeA = a.timestamp?.toMillis ? a.timestamp.toMillis() : (a.timestamp || 0);
          const timeB = b.timestamp?.toMillis ? b.timestamp.toMillis() : (b.timestamp || 0);
          return timeB - timeA;
        });

        // Take top 5 for the dropdown
        fetched = fetched.slice(0, 5);

        setNotifications(fetched);
        setNotificationCount(unread);
      },
      (error) => {
        console.error("Layout notifications load error:", error);
        setNotifications([]);
        setNotificationCount(0);
      }
    );

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
    <div className="min-h-screen flex p-0 md:p-6 md:gap-6 overflow-hidden max-h-screen bg-gray-50 dark:bg-transparent">
      
      {/* Desktop Sidebar */}
      <aside className="hidden md:flex flex-col w-72 glass-card rounded-[2.5rem] h-[calc(100vh-3rem)] overflow-hidden shrink-0 relative">
        <div className="absolute top-0 right-0 w-32 h-32 bg-bookup-primary/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
        
        <div className="p-8 relative z-10 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-bookup-primary to-bookup-accent flex items-center justify-center text-white font-bold text-xl shadow-lg shadow-bookup-primary/30">
              B
            </div>
            <span className="text-2xl font-black tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-bookup-primary-dark to-bookup-accent font-display">
              BookUp
            </span>
          </Link>
        </div>

        <nav className="flex-1 px-4 space-y-2.5 relative z-10 overflow-y-auto no-scrollbar">
          {navItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            const Icon = item.icon;
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex items-center space-x-4 px-5 py-4 rounded-2xl transition-all duration-300 group ${
                  isActive 
                    ? 'bg-gradient-to-r from-bookup-primary to-bookup-primary-dark text-white shadow-lg shadow-bookup-primary/20 scale-[1.02] border-l-4 border-bookup-accent' 
                    : 'text-gray-500 hover:bg-white/50 dark:hover:bg-white/5 hover:text-bookup-primary hover:shadow-sm'
                }`}
              >
                <Icon size={20} className={isActive ? 'text-bookup-accent' : 'text-gray-400 group-hover:text-bookup-primary transition-colors'} strokeWidth={isActive ? 2.5 : 2} />
                <span className={`font-black uppercase tracking-widest text-[10px] ${isActive ? 'text-white' : 'text-gray-500 dark:text-gray-400 group-hover:text-bookup-primary'}`}>{item.name}</span>
              </Link>
            );
          })}
        </nav>

        <div className="p-4 mt-auto relative z-10 border-t border-gray-200/30">
          <Link to="/profile" className="flex items-center gap-4 p-4 rounded-2xl hover:bg-white/60 dark:hover:bg-white/5 transition-all cursor-pointer border border-transparent hover:border-gray-200/50 hover:shadow-sm">
            <img 
              src={profilePhoto} 
              alt="Profile" 
              className="w-12 h-12 rounded-[1rem] ring-2 ring-bookup-accent shadow-sm object-cover" 
            />
            <div className="flex-1 min-w-0">
              <p className="font-black text-gray-900 truncate font-display">{userProfile?.displayName || currentUser?.displayName || 'User'}</p>
              <p className="text-[10px] font-black text-bookup-accent truncate uppercase tracking-tighter">{currentUser?.email}</p>
            </div>
          </Link>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 h-[100vh] md:h-[calc(100vh-3rem)] relative">
        
        {/* Download App Prompt (Mobile only) */}
        {showDownloadPrompt && (
          <div className="md:hidden bg-gradient-to-r from-gray-950 via-bookup-primary-dark to-bookup-primary text-white px-6 py-4 flex items-center justify-between gap-4 animate-in slide-in-from-top duration-500 z-50">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-white/10 rounded-xl flex items-center justify-center border border-white/10">
                <Smartphone size={20} className="text-bookup-accent" />
              </div>
              <div>
                <p className="font-black text-sm leading-tight">Get the BookUp App</p>
                <p className="text-[10px] font-bold text-bookup-accent uppercase tracking-widest">Better on Mobile</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <button 
                onClick={() => navigate('/download')}
                className="bg-bookup-accent text-gray-950 px-4 py-2 rounded-lg font-black text-[10px] uppercase tracking-widest shadow-lg hover:scale-105 active:scale-95"
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
        <header className="h-16 md:h-20 mb-0 md:mb-6 glass md:rounded-3xl flex items-center justify-between px-4 md:px-6 shrink-0 z-40 border-b md:border-b-0 border-gray-200/50">
          
          <div className="md:hidden flex items-center gap-2">
            <button 
              onClick={() => setIsMobileDrawerOpen(true)}
              className="p-2 -ml-2 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-white/5 rounded-xl transition-colors"
              aria-label="Open menu"
            >
              <Menu size={22} />
            </button>
            <Link to="/dashboard" className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-bookup-primary to-bookup-accent flex items-center justify-center text-white font-bold text-base shadow-sm">
                B
              </div>
              <span className="text-xl font-black tracking-tight text-gray-900 dark:text-white font-display">
                BookUp
              </span>
            </Link>
          </div>

          <form onSubmit={handleSearch} className="relative flex-1 max-w-md hidden sm:block group ml-4 md:ml-0">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400 group-focus-within:text-bookup-accent transition-colors" />
            <input 
              type="text" 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search tutors, materials..." 
              className="w-full pl-12 pr-4 py-2.5 bg-white/40 dark:bg-black/20 border border-gray-200/50 dark:border-white/5 rounded-2xl focus:bg-white dark:focus:bg-black focus:ring-2 focus:ring-bookup-accent/20 focus:border-bookup-accent outline-none transition-all placeholder-gray-400 font-bold text-sm"
            />
          </form>
          
          <div className="flex items-center gap-3 md:gap-4 ml-auto relative">
            <button onClick={() => navigate('/tutors')} className="sm:hidden p-2.5 rounded-full bg-white dark:bg-gray-950/40 text-gray-600 dark:text-gray-300 shadow-sm border border-gray-100 dark:border-white/5 active:scale-95 transition-transform">
              <Search size={20} />
            </button>

            {/* Theme Toggle Button */}
            <button 
              onClick={toggleTheme}
              className="p-2.5 md:p-3 rounded-full bg-white dark:bg-gray-950/40 text-gray-600 dark:text-gray-300 border border-gray-100 dark:border-white/5 hover:text-bookup-primary dark:hover:text-bookup-accent hover:shadow-md dark:hover:shadow-bookup-accent/5 active:scale-95 transition-all"
              aria-label="Toggle theme"
            >
              {theme === 'dark' ? <Sun size={20} className="text-bookup-accent" /> : <Moon size={20} className="text-bookup-primary" />}
            </button>
            
            <button 
              onClick={() => setShowNotifications(!showNotifications)}
              className={`relative p-2.5 md:p-3 rounded-full transition-all border active:scale-95 ${showNotifications ? 'bg-bookup-primary text-white border-bookup-primary shadow-lg shadow-bookup-primary/20' : 'bg-white dark:bg-gray-950/40 text-gray-600 dark:text-gray-300 border border-gray-100 dark:border-white/5 hover:text-bookup-primary dark:hover:text-bookup-primary-light hover:shadow-md'}`}
            >
              <Bell size={20} className={showNotifications ? 'text-bookup-accent' : 'text-gray-500'} />
              {notificationCount > 0 && (
                <span className="absolute top-2 right-2 min-w-[18px] h-[18px] px-1 bg-bookup-accent rounded-full border-2 border-white dark:border-gray-950 text-[10px] font-black text-gray-950 dark:text-white flex items-center justify-center animate-bounce shadow-md">
                  {notificationCount > 9 ? '9+' : notificationCount}
                </span>
              )}
            </button>

            {/* Notifications Dropdown */}
            {showNotifications && (
              <div className="absolute top-full right-0 mt-4 w-80 glass-card rounded-3xl shadow-2xl p-6 animate-in slide-in-from-top-4 duration-300 z-50">
                <div className="flex justify-between items-center mb-6">
                  <h3 className="font-black text-gray-900 tracking-tight font-display text-lg">Notifications</h3>
                  <button onClick={markAllRead} className="text-[10px] font-black text-bookup-accent uppercase tracking-widest hover:underline">Mark all read</button>
                </div>
                <div className="space-y-4 max-h-96 overflow-y-auto no-scrollbar">
                  {notifications.length > 0 ? (
                    notifications.map((n) => (
                      <div key={n.id} className={`p-4 rounded-2xl transition-all border ${n.read ? 'bg-gray-50/20 border-transparent' : 'bg-white border-bookup-accent/20 shadow-sm'}`}>
                        <p className={`text-sm ${n.read ? 'text-gray-500' : 'text-gray-900 font-bold'}`}>{n.message || n.title}</p>
                        <p className="text-[10px] font-black text-bookup-accent mt-2 uppercase tracking-tighter">Recently</p>
                      </div>
                    ))
                  ) : (
                    <div className="text-center py-10">
                      <p className="text-gray-400 font-bold text-sm">All caught up! ✌️</p>
                    </div>
                  )}
                </div>
              </div>
            )}

            <Link to="/profile" className="md:hidden ml-1">
               <img 
                 src={profilePhoto} 
                 alt="Profile" 
                 className="w-9 h-9 rounded-xl ring-2 ring-bookup-accent shadow-sm object-cover" 
               />
            </Link>
          </div>
        </header>

        {/* Scrollable Page Content */}
        <main className={`flex-1 md:rounded-3xl bg-transparent ${
          location.pathname.startsWith('/messages') 
            ? 'overflow-hidden h-full flex flex-col p-0' 
            : 'overflow-y-auto pb-24 md:pb-10 px-4 md:px-0 pt-4 md:pt-0'
        }`}>
          <Outlet />
        </main>
      </div>

      {/* Refined Mobile Bottom Navigation Bar (Core 4 tabs + Profile) */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-white/80 dark:bg-gray-950/80 backdrop-blur-lg border-t border-gray-200/50 dark:border-white/5 z-50 flex items-center py-2.5 px-4 shadow-[0_-8px_30px_rgb(0,0,0,0.06)]">
        <div className="flex items-center justify-around w-full pb-[env(safe-area-inset-bottom)]">
          {navItems.slice(0, 4).map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            const Icon = item.icon;
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex flex-col items-center gap-1 px-3 py-1 rounded-2xl transition-all ${
                  isActive 
                    ? 'text-bookup-primary dark:text-bookup-accent' 
                    : 'text-gray-400'
                }`}
              >
                <Icon size={20} className={isActive ? 'text-bookup-primary dark:text-bookup-accent scale-110' : 'text-gray-400'} strokeWidth={isActive ? 2.5 : 2} />
                <span className={`text-[9px] font-black uppercase tracking-tighter ${isActive ? 'text-bookup-primary dark:text-bookup-accent' : 'text-gray-500'}`}>
                  {item.name.replace('Messages', 'Chat').replace('Dashboard', 'Home').replace('Users', 'Tutors')}
                </span>
              </Link>
            );
          })}
          <Link
            to="/profile"
            className={`flex flex-col items-center gap-1 px-3 py-1 rounded-2xl transition-all ${
              location.pathname === '/profile' 
                ? 'text-bookup-primary dark:text-bookup-accent' 
                : 'text-gray-400'
            }`}
          >
            <img 
              src={profilePhoto} 
              alt="Profile" 
              className={`w-5 h-5 rounded-lg object-cover ${location.pathname === '/profile' ? 'ring-2 ring-bookup-accent dark:ring-bookup-accent' : ''}`}
            />
            <span className={`text-[9px] font-black uppercase tracking-tighter ${location.pathname === '/profile' ? 'text-bookup-primary dark:text-bookup-accent' : 'text-gray-500'}`}>
              Profile
            </span>
          </Link>
        </div>
      </nav>

      {/* Mobile Slide-Over Navigation Drawer */}
      {isMobileDrawerOpen && (
        <div className="md:hidden fixed inset-0 z-50 flex animate-in fade-in duration-300">
          {/* Backdrop */}
          <div 
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => setIsMobileDrawerOpen(false)}
          />

          {/* Drawer Panel */}
          <div className="relative w-80 max-w-sm bg-white dark:bg-gray-950 h-full shadow-2xl flex flex-col p-6 overflow-y-auto animate-in slide-in-from-left duration-300">
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-bookup-primary to-bookup-accent flex items-center justify-center text-white font-bold text-xl shadow-lg shadow-bookup-primary/30">
                  B
                </div>
                <span className="text-2xl font-black tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-bookup-primary-dark to-bookup-accent font-display">
                  BookUp
                </span>
              </div>
              <button 
                onClick={() => setIsMobileDrawerOpen(false)}
                className="p-2 hover:bg-gray-100 dark:hover:bg-white/5 rounded-xl transition-colors text-gray-500"
              >
                <X size={20} />
              </button>
            </div>

            {/* Nav Menu */}
            <nav className="flex-1 space-y-2">
              <p className="text-[10px] font-black uppercase tracking-widest text-gray-400 mb-4 px-3">Main Menu</p>
              {navItems.map((item) => {
                const isActive = location.pathname.startsWith(item.path);
                const Icon = item.icon;
                return (
                  <Link
                    key={item.name}
                    to={item.path}
                    className={`flex items-center space-x-4 px-4 py-3.5 rounded-xl transition-all ${
                      isActive 
                        ? 'bg-bookup-primary/10 dark:bg-bookup-accent/10 text-bookup-primary dark:text-bookup-accent font-black' 
                        : 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-white/5'
                    }`}
                  >
                    <Icon size={18} className={isActive ? 'text-bookup-primary dark:text-bookup-accent' : 'text-gray-400'} />
                    <span className="text-sm font-bold">{item.name}</span>
                  </Link>
                );
              })}
            </nav>

            {/* Profile Footer inside Drawer */}
            <div className="pt-6 mt-auto border-t border-gray-200/50 dark:border-white/5">
              <Link to="/profile" className="flex items-center gap-4 p-3 rounded-2xl hover:bg-gray-50 dark:hover:bg-white/5 transition-all">
                <img 
                  src={profilePhoto} 
                  alt="Profile" 
                  className="w-10 h-10 rounded-xl object-cover ring-2 ring-bookup-accent" 
                />
                <div className="flex-1 min-w-0">
                  <p className="font-bold text-gray-900 dark:text-white text-sm truncate">{userProfile?.displayName || currentUser?.displayName || 'User'}</p>
                  <p className="text-[10px] text-gray-400 truncate">{currentUser?.email}</p>
                </div>
              </Link>
            </div>
          </div>
        </div>
      )}
      
    </div>
  );
}

