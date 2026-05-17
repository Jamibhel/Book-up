import { useState, useEffect } from 'react';
import { collection, query, getDocs, doc, updateDoc, deleteDoc, orderBy, limit, addDoc, serverTimestamp, getDoc } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../contexts/AuthContext';
import { Users, Shield, Newspaper, MessageSquare, AlertCircle, TrendingUp, UserPlus, Settings, ArrowRight, Trash2, ShieldCheck, ShieldAlert, Send } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function AdminPanel() {
  const { userProfile, currentUser } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState({
    users: 0,
    tutors: 0,
    news: 0,
    requests: 0
  });
  const [recentUsers, setRecentUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [downloadLink, setDownloadLink] = useState('');

  const [newsTitle, setNewsTitle] = useState('');
  const [newsContent, setNewsContent] = useState('');
  const [newsImageUrl, setNewsImageUrl] = useState('');
  const [isPosting, setIsPosting] = useState(false);

  useEffect(() => {
    if (!userProfile?.isAdmin) {
      navigate('/dashboard');
      return;
    }

    async function fetchAdminData() {
      try {
        const [usersSnap, newsSnap, requestsSnap, configSnap] = await Promise.all([
          getDocs(collection(db, 'users')),
          getDocs(collection(db, 'newsFeed')),
          getDocs(collection(db, 'helpRequests')),
          getDoc(doc(db, 'appSettings', 'config'))
        ]);

        const usersList = usersSnap.docs.map(d => ({ id: d.id, ...d.data() }));
        setStats({
          users: usersList.length,
          tutors: usersList.filter((u: any) => u.role === 'tutor').length,
          news: newsSnap.size,
          requests: requestsSnap.size
        });

        setRecentUsers(usersList.sort((a: any, b: any) => (b.createdAt?.seconds || 0) - (a.createdAt?.seconds || 0)).slice(0, 5));
        
        if (configSnap.exists()) {
          setDownloadLink(configSnap.data().downloadLink || '');
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }

    fetchAdminData();
  }, [userProfile, navigate]);

  const handlePostNews = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newsTitle || !newsContent) return;
    
    setIsPosting(true);
    try {
      await addDoc(collection(db, 'newsFeed'), {
        title: newsTitle,
        headline: newsTitle,
        content: newsContent,
        imageUrl: newsImageUrl || null,
        authorName: userProfile?.displayName || 'Admin',
        authorId: currentUser?.uid,
        authorRole: 'admin',
        priority: true,
        timestamp: serverTimestamp(),
        likesCount: 0,
        likedBy: [],
        comments: []
      });
      
      setNewsTitle('');
      setNewsContent('');
      setNewsImageUrl('');
      alert('News published successfully!');
      
      // Update local count
      setStats(prev => ({ ...prev, news: prev.news + 1 }));
    } catch (err) {
      console.error("Error posting news:", err);
      alert('Failed to publish news.');
    } finally {
      setIsPosting(false);
    }
  };

  const toggleAdmin = async (userId: string, currentStatus: boolean) => {
    if (window.confirm(`Are you sure you want to ${currentStatus ? 'remove' : 'grant'} admin privileges?`)) {
      await updateDoc(doc(db, 'users', userId), { isAdmin: !currentStatus });
      // Refresh list
      setRecentUsers(prev => prev.map(u => u.id === userId ? { ...u, isAdmin: !currentStatus } : u));
    }
  };

  const deleteUser = async (userId: string) => {
    if (window.confirm("Are you sure you want to delete this user? This cannot be undone.")) {
      await deleteDoc(doc(db, 'users', userId));
      setRecentUsers(prev => prev.filter(u => u.id !== userId));
    }
  };
  if (loading) {
    return (
      <div className="flex justify-center items-center py-20 min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-bookup-primary border-t-transparent shadow-lg"></div>
      </div>
    );
  }

  if (!userProfile?.isAdmin) return null;

  return (
    <div className="space-y-10 animate-in slide-in-from-bottom-8 duration-700 pb-20">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-6">
        <div>
          <div className="flex items-center gap-3 mb-2">
            <span className="p-2 bg-amber-100 text-amber-600 rounded-lg"><Shield size={20} /></span>
            <span className="text-sm font-black text-amber-600 uppercase tracking-[0.2em]">Administrative Control</span>
          </div>
          <h1 className="text-5xl font-black text-gray-900 tracking-tight font-display">Command Center</h1>
        </div>
        <div className="flex gap-4">
          <button className="bg-gray-100 p-4 rounded-2xl text-gray-500 hover:bg-gray-200 transition-all"><Settings size={24} /></button>
          <div className="flex items-center gap-3 bg-white px-6 py-4 rounded-2xl border border-gray-100 shadow-sm">
            <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
            <span className="font-bold text-gray-900">System Online</span>
          </div>
        </div>
      </div>

      {/* Hero Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
        {[
          { label: 'Total Users', value: stats.users, icon: Users, color: 'bg-indigo-500', trend: '+12% this month' },
          { label: 'Active Tutors', value: stats.tutors, icon: UserPlus, color: 'bg-emerald-500', trend: '8 new applications' },
          { label: 'Published News', value: stats.news, icon: Newspaper, color: 'bg-amber-500', trend: '3 drafted' },
          { label: 'Help Requests', value: stats.requests, icon: MessageSquare, color: 'bg-rose-500', trend: '14 resolved today' },
        ].map((stat, i) => (
          <div key={i} className="bg-white p-8 rounded-[2.5rem] shadow-sm border border-gray-100 relative overflow-hidden group hover:shadow-xl transition-all duration-500">
            <div className={`absolute top-0 right-0 w-32 h-32 ${stat.color} opacity-[0.03] rounded-full -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-700`}></div>
            <div className={`w-14 h-14 ${stat.color} text-white rounded-2xl flex items-center justify-center mb-6 shadow-lg shadow-${stat.color.split('-')[1]}-200`}>
              <stat.icon size={28} />
            </div>
            <h3 className="text-4xl font-black text-gray-900 mb-1">{stat.value}</h3>
            <p className="font-bold text-gray-400 uppercase tracking-widest text-xs mb-4">{stat.label}</p>
            <div className="flex items-center gap-2 text-emerald-600 font-black text-xs">
              <TrendingUp size={14} /> {stat.trend}
            </div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">
        {/* News Creation & System Settings */}
        <div className="space-y-10">
           <section className="space-y-6">
              <h2 className="text-3xl font-black text-gray-900 font-display">System Config</h2>
              <div className="bg-white p-8 rounded-[2.5rem] shadow-sm border border-gray-100 space-y-6">
                 <div className="space-y-2">
                    <label className="text-xs font-black uppercase tracking-widest text-gray-400 ml-1">App Download Link</label>
                    <div className="flex gap-3">
                       <input 
                         type="url" 
                         id="downloadUrlInput"
                         value={downloadLink}
                         onChange={(e) => setDownloadLink(e.target.value)}
                         className="flex-1 px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:bg-white focus:ring-2 focus:ring-bookup-primary/20 focus:border-bookup-primary outline-none transition-all font-bold"
                         placeholder="https://drive.google.com/..."
                       />
                       <button 
                         onClick={async () => {
                           if (!downloadLink) return;
                           await updateDoc(doc(db, 'appSettings', 'config'), { downloadLink: downloadLink });
                           alert('Settings updated!');
                         }}
                         className="bg-gray-900 text-white px-6 rounded-2xl font-black hover:bg-bookup-primary transition-all"
                       >
                          Save
                       </button>
                    </div>
                 </div>
              </div>
           </section>

           <section className="space-y-6">
              <h2 className="text-3xl font-black text-gray-900 font-display">Quick Announcement</h2>
              <form onSubmit={handlePostNews} className="bg-white p-8 rounded-[2.5rem] shadow-sm border border-gray-100 space-y-6">
              <div className="space-y-2">
                <label className="text-xs font-black uppercase tracking-widest text-gray-400 ml-1">Headline</label>
                <input 
                  type="text" 
                  value={newsTitle}
                  onChange={(e) => setNewsTitle(e.target.value)}
                  className="w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:bg-white focus:ring-2 focus:ring-bookup-primary/20 focus:border-bookup-primary outline-none transition-all font-bold"
                  placeholder="Enter news headline..."
                  required
                />
              </div>
              <div className="space-y-2">
                <label className="text-xs font-black uppercase tracking-widest text-gray-400 ml-1">Content</label>
                <textarea 
                  value={newsContent}
                  onChange={(e) => setNewsContent(e.target.value)}
                  className="w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:bg-white focus:ring-2 focus:ring-bookup-primary/20 focus:border-bookup-primary outline-none transition-all font-medium min-h-[150px] resize-none"
                  placeholder="What's happening in BookUp?"
                  required
                />
              </div>
              <div className="space-y-2">
                <label className="text-xs font-black uppercase tracking-widest text-gray-400 ml-1">Image URL (Optional)</label>
                <input 
                  type="url" 
                  value={newsImageUrl}
                  onChange={(e) => setNewsImageUrl(e.target.value)}
                  className="w-full px-5 py-4 bg-gray-50 border border-gray-100 rounded-2xl focus:bg-white focus:ring-2 focus:ring-bookup-primary/20 focus:border-bookup-primary outline-none transition-all font-bold"
                  placeholder="https://example.com/image.jpg"
                />
              </div>
              <button 
                type="submit" 
                disabled={isPosting}
                className="w-full bg-gray-900 text-white py-5 rounded-2xl font-black hover:bg-bookup-primary transition-all shadow-lg flex items-center justify-center gap-3 disabled:opacity-50"
              >
                {isPosting ? 'Publishing...' : <><Send size={20} /> Publish to Feed</>}
              </button>
           </form>
          </section>
        </div>

        {/* User Management Section */}
        <div className="lg:col-span-2 space-y-8">
          <div className="flex justify-between items-center">
            <h2 className="text-3xl font-black text-gray-900 font-display">Recent Users</h2>
            <button className="text-bookup-primary font-black flex items-center gap-2 hover:gap-3 transition-all">
              Manage All <ArrowRight size={20} />
            </button>
          </div>

          <div className="bg-white rounded-[2.5rem] border border-gray-100 shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr className="bg-gray-50/50 border-b border-gray-100">
                    <th className="px-8 py-6 font-black text-gray-400 uppercase tracking-widest text-xs">User</th>
                    <th className="px-8 py-6 font-black text-gray-400 uppercase tracking-widest text-xs">Role</th>
                    <th className="px-8 py-6 font-black text-gray-400 uppercase tracking-widest text-xs">Status</th>
                    <th className="px-8 py-6 font-black text-gray-400 uppercase tracking-widest text-xs text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {recentUsers.map((user) => (
                    <tr key={user.id} className="hover:bg-gray-50/50 transition-colors group">
                      <td className="px-8 py-6">
                        <div className="flex items-center gap-4">
                          <img 
                            src={user.photoURL || user.photoUrl || `https://ui-avatars.com/api/?name=${user.displayName}&background=random&bold=true`} 
                            className="w-12 h-12 rounded-xl object-cover shadow-sm" 
                            alt=""
                          />
                          <div>
                            <p className="font-black text-gray-900">{user.displayName}</p>
                            <p className="text-sm font-bold text-gray-400">{user.email}</p>
                          </div>
                        </div>
                      </td>
                      <td className="px-8 py-6">
                        <span className="bg-gray-100 text-gray-600 px-3 py-1 rounded-lg text-xs font-black uppercase">
                          {user.role}
                        </span>
                      </td>
                      <td className="px-8 py-6">
                        {user.isAdmin ? (
                          <div className="flex items-center gap-2 text-amber-600 font-black text-xs uppercase tracking-widest">
                            <ShieldCheck size={14} /> Administrator
                          </div>
                        ) : (
                          <div className="flex items-center gap-2 text-gray-400 font-black text-xs uppercase tracking-widest">
                            Standard User
                          </div>
                        )}
                      </td>
                      <td className="px-8 py-6 text-right">
                        <div className="flex justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                          <button 
                            onClick={() => toggleAdmin(user.id, user.isAdmin)}
                            className={`p-3 rounded-xl transition-all ${user.isAdmin ? 'bg-amber-50 text-amber-600 hover:bg-amber-100' : 'bg-gray-50 text-gray-400 hover:bg-amber-50 hover:text-amber-600'}`}
                            title={user.isAdmin ? "Remove Admin" : "Make Admin"}
                          >
                            <Shield size={20} />
                          </button>
                          <button 
                            onClick={() => deleteUser(user.id)}
                            className="p-3 bg-red-50 text-red-500 hover:bg-red-100 rounded-xl transition-all"
                            title="Delete User"
                          >
                            <Trash2 size={20} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
