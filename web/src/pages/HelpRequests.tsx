import { useState, useEffect } from 'react';
import { collection, query, orderBy, onSnapshot, addDoc, serverTimestamp, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../contexts/AuthContext';
import { MessageSquare, Plus, Clock, Tag, User, Trash2, Send, X } from 'lucide-react';

interface HelpRequest {
  id: string;
  title: string;
  description: string;
  subject: string;
  requestedByUid: string;
  requestedByName: string;
  status: string;
  timestamp: any;
}

export default function HelpRequests() {
  const { userProfile } = useAuth();
  const [requests, setRequests] = useState<HelpRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [newRequest, setNewRequest] = useState({ title: '', description: '', subject: '' });

  useEffect(() => {
    const q = query(collection(db, 'helpRequests'), orderBy('timestamp', 'desc'));
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const items = snapshot.docs.map(d => ({ id: d.id, ...d.data() } as HelpRequest));
      setRequests(items);
      setLoading(false);
    });
    return unsubscribe;
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!userProfile) return;
    
    try {
      await addDoc(collection(db, 'helpRequests'), {
        ...newRequest,
        requestedByUid: userProfile.id,
        requestedByName: userProfile.displayName,
        status: 'Open',
        timestamp: serverTimestamp()
      });
      setShowModal(false);
      setNewRequest({ title: '', description: '', subject: '' });
    } catch (err) {
      console.error(err);
    }
  };

  const deleteRequest = async (id: string) => {
    if (window.confirm("Are you sure you want to delete this request?")) {
      await deleteDoc(doc(db, 'helpRequests', id));
    }
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-4xl font-black text-gray-900 tracking-tight font-display">Help Requests</h1>
          <p className="text-gray-500 font-medium mt-2">Find or post requests for academic assistance.</p>
        </div>
        <button 
          onClick={() => setShowModal(true)}
          className="bg-bookup-primary text-white px-8 py-4 rounded-2xl font-black shadow-xl shadow-bookup-primary/20 hover:-translate-y-1 transition-all flex items-center gap-3"
        >
          <Plus size={24} /> New Request
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-bookup-primary border-t-transparent"></div>
        </div>
      ) : requests.length === 0 ? (
        <div className="bg-white rounded-[2.5rem] p-20 text-center border border-gray-100 shadow-sm">
          <MessageSquare size={64} className="mx-auto text-gray-200 mb-6" />
          <h2 className="text-2xl font-black text-gray-900 mb-2">No Requests Yet</h2>
          <p className="text-gray-500 font-medium max-w-sm mx-auto">Be the first to post a help request and get matched with an expert tutor.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {requests.map((request) => (
            <div key={request.id} className="bg-white p-8 rounded-[2.5rem] border border-gray-100 shadow-sm hover:shadow-xl transition-all group relative overflow-hidden">
              <div className="absolute top-0 right-0 p-6 opacity-0 group-hover:opacity-100 transition-opacity">
                {userProfile?.id === request.requestedByUid && (
                  <button onClick={() => deleteRequest(request.id)} className="p-3 text-red-500 hover:bg-red-50 rounded-xl transition-colors">
                    <Trash2 size={20} />
                  </button>
                )}
              </div>
              
              <div className="flex items-center gap-3 mb-6">
                <span className="bg-bookup-primary/10 text-bookup-primary px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-widest">
                  {request.subject}
                </span>
                <span className={`px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-widest ${
                  request.status === 'Open' ? 'bg-green-100 text-green-600' : 'bg-amber-100 text-amber-600'
                }`}>
                  {request.status}
                </span>
              </div>

              <h3 className="text-2xl font-black text-gray-900 mb-4 group-hover:text-bookup-primary transition-colors">{request.title}</h3>
              <p className="text-gray-500 font-medium line-clamp-3 mb-8 leading-relaxed">
                {request.description}
              </p>

              <div className="flex items-center justify-between pt-6 border-t border-gray-50">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-gray-100 rounded-xl flex items-center justify-center text-gray-400">
                    <User size={20} />
                  </div>
                  <div>
                    <p className="text-sm font-black text-gray-900">{request.requestedByName}</p>
                    <p className="text-[10px] font-bold text-gray-400 uppercase flex items-center gap-1">
                      <Clock size={10} /> {request.timestamp?.toDate ? request.timestamp.toDate().toLocaleDateString() : 'Just now'}
                    </p>
                  </div>
                </div>
                <button className="bg-gray-900 text-white p-4 rounded-xl hover:bg-bookup-primary transition-all shadow-lg hover:-translate-y-1">
                  <Send size={18} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* New Request Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-300">
          <div className="bg-white w-full max-w-xl rounded-[2.5rem] shadow-2xl overflow-hidden animate-in zoom-in-95 duration-300">
            <div className="p-8 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
              <h2 className="text-2xl font-black text-gray-900">Post New Help Request</h2>
              <button onClick={() => setShowModal(false)} className="p-2 hover:bg-gray-100 rounded-full transition-colors"><X size={24} /></button>
            </div>
            <form onSubmit={handleSubmit} className="p-8 space-y-6">
              <div className="space-y-2">
                <label className="text-sm font-black text-gray-400 uppercase tracking-widest ml-1">Title</label>
                <input 
                  required
                  type="text" 
                  value={newRequest.title}
                  onChange={e => setNewRequest({...newRequest, title: e.target.value})}
                  className="w-full bg-gray-50 border-none rounded-2xl p-4 focus:ring-2 focus:ring-bookup-primary/20 font-bold"
                  placeholder="e.g., Need help with Calculus Integration"
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-black text-gray-400 uppercase tracking-widest ml-1">Subject</label>
                <select 
                  required
                  value={newRequest.subject}
                  onChange={e => setNewRequest({...newRequest, subject: e.target.value})}
                  className="w-full bg-gray-50 border-none rounded-2xl p-4 focus:ring-2 focus:ring-bookup-primary/20 font-bold outline-none cursor-pointer"
                >
                  <option value="" disabled>Select a subject</option>
                  <option value="Mathematics">Mathematics</option>
                  <option value="Science">Science</option>
                  <option value="English">English</option>
                  <option value="Physics">Physics</option>
                  <option value="Chemistry">Chemistry</option>
                  <option value="Biology">Biology</option>
                  <option value="History">History</option>
                  <option value="Geography">Geography</option>
                  <option value="Computer Science">Computer Science</option>
                  <option value="Economics">Economics</option>
                  <option value="Other">Other</option>
                </select>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-black text-gray-400 uppercase tracking-widest ml-1">Description</label>
                <textarea 
                  required
                  rows={4}
                  value={newRequest.description}
                  onChange={e => setNewRequest({...newRequest, description: e.target.value})}
                  className="w-full bg-gray-50 border-none rounded-2xl p-4 focus:ring-2 focus:ring-bookup-primary/20 font-bold resize-none"
                  placeholder="Tell us more about what you need help with..."
                />
              </div>
              <button type="submit" className="w-full bg-bookup-primary text-white py-5 rounded-2xl font-black shadow-xl shadow-bookup-primary/20 hover:-translate-y-1 transition-all mt-4">
                Post Request
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
