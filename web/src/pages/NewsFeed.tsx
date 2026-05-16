import { useState, useEffect } from 'react';
import { Heart, MessageCircle, Share2, MoreHorizontal, Send, X } from 'lucide-react';
import { collection, query, orderBy, onSnapshot, doc, updateDoc, arrayUnion, arrayRemove, increment } from 'firebase/firestore';
import { db } from '../lib/firebase';
import { useAuth } from '../contexts/AuthContext';

interface NewsItem {
  id: string;
  title: string;
  headline?: string;
  content: string;
  imageUrl?: string;
  authorName: string;
  authorId?: string;
  likesCount: number;
  likedBy?: string[];
  comments?: any[];
  timestamp?: any;
}

export default function NewsFeed() {
  const { currentUser } = useAuth();
  const [posts, setPosts] = useState<NewsItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [newPostContent, setNewPostContent] = useState('');
  const [activeComments, setActiveComments] = useState<string | null>(null);
  const [commentText, setCommentText] = useState('');

  useEffect(() => {
    const q = query(collection(db, 'newsFeed'), orderBy('timestamp', 'desc'));
    
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const fetchedPosts: NewsItem[] = [];
      snapshot.forEach((doc) => {
        fetchedPosts.push({ id: doc.id, ...doc.data() } as NewsItem);
      });
      setPosts(fetchedPosts);
      setLoading(false);
    }, (err) => {
      console.error("Error fetching news:", err);
      setError('Failed to load news feed. Check Firebase permissions.');
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const handleLike = async (post: NewsItem) => {
    if (!currentUser) return alert("Log in to like posts.");
    const isLiked = post.likedBy?.includes(currentUser.uid);
    const postRef = doc(db, 'newsFeed', post.id);

    try {
      if (isLiked) {
        await updateDoc(postRef, {
          likedBy: arrayRemove(currentUser.uid),
          likesCount: increment(-1)
        });
      } else {
        await updateDoc(postRef, {
          likedBy: arrayUnion(currentUser.uid),
          likesCount: increment(1)
        });
      }
    } catch (err) {
      console.error("Error liking post:", err);
    }
  };

  const handlePostComment = async (postId: string) => {
    if (!currentUser || !commentText.trim()) return;
    const postRef = doc(db, 'newsFeed', postId);

    try {
      await updateDoc(postRef, {
        comments: arrayUnion({
          userId: currentUser.uid,
          userName: currentUser.displayName || 'User',
          text: commentText,
          timestamp: new Date().toISOString()
        })
      });
      setCommentText('');
    } catch (err) {
      console.error("Error posting comment:", err);
    }
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
    <div className="max-w-2xl mx-auto space-y-8 animate-in fade-in duration-500 pb-20">
      <div className="mb-2 border-b border-gray-100 pb-8">
        <h1 className="text-4xl font-black text-gray-900 tracking-tight font-display">Community Feed</h1>
        <p className="text-lg text-gray-500 mt-2 font-medium">Stay updated with your tutors and peers.</p>
      </div>

      {/* Create Post Input (Admin only or all?) - Keeping original logic */}
      <div className="bg-white rounded-[2.5rem] shadow-sm border border-gray-100 p-8">
        <div className="flex gap-6">
          <img 
            src={`https://ui-avatars.com/api/?name=${currentUser?.displayName || currentUser?.email}&background=2E8B57&color=fff&bold=true`} 
            alt="You" 
            className="w-14 h-14 rounded-2xl object-cover border-2 border-white shadow-md" 
          />
          <div className="flex-1">
            <textarea 
              value={newPostContent}
              onChange={(e) => setNewPostContent(e.target.value)}
              className="w-full border-none focus:ring-0 resize-none bg-gray-50 rounded-[1.5rem] p-5 text-gray-900 placeholder-gray-400 transition-all outline-none font-medium text-lg"
              placeholder="What's on your mind?"
              rows={2}
            />
            <div className="mt-4 flex justify-end">
              <button 
                className="bg-gray-900 text-white px-8 py-3 rounded-2xl font-black hover:bg-bookup-primary transition-all shadow-lg shadow-gray-200 disabled:opacity-50 uppercase tracking-widest text-xs"
                disabled={!newPostContent.trim()}
              >
                Post Update
              </button>
            </div>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center items-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-bookup-primary"></div>
        </div>
      ) : error ? (
        <div className="bg-red-50 text-red-600 p-8 rounded-[2rem] font-bold border border-red-100">
          {error}
        </div>
      ) : posts.length === 0 ? (
        <div className="text-center py-20 text-gray-400 font-bold bg-white rounded-[2.5rem] border border-gray-100 shadow-sm">
          No posts yet. Be the first to share!
        </div>
      ) : (
        <div className="space-y-8">
          {posts.map((post) => {
            const isLiked = post.likedBy?.includes(currentUser?.uid || '');
            return (
              <article key={post.id} className="bg-white rounded-[3rem] shadow-sm border border-gray-100 overflow-hidden hover:shadow-xl transition-all duration-300">
                <div className="p-8">
                  <div className="flex justify-between items-start mb-6">
                    <div className="flex items-center gap-4">
                      <img 
                        src={`https://ui-avatars.com/api/?name=${post.authorName || 'User'}&background=1B9A8B&color=fff&bold=true`} 
                        alt={post.authorName} 
                        className="w-14 h-14 rounded-2xl object-cover shadow-sm" 
                      />
                      <div>
                        <h3 className="font-black text-xl text-gray-900">{post.authorName || 'Anonymous User'}</h3>
                        <p className="text-xs font-bold text-gray-400 uppercase tracking-widest">{formatTime(post.timestamp)}</p>
                      </div>
                    </div>
                    <button className="text-gray-400 hover:text-gray-900 transition-colors p-3 rounded-xl hover:bg-gray-50">
                      <MoreHorizontal size={24} />
                    </button>
                  </div>
                  
                  {post.headline && <h4 className="font-black text-2xl mb-3 text-gray-900 tracking-tight">{post.headline}</h4>}
                  {post.title && !post.headline && <h4 className="font-black text-2xl mb-3 text-gray-900 tracking-tight">{post.title}</h4>}
                  
                  <p className="text-gray-600 text-lg leading-relaxed whitespace-pre-wrap font-medium">{post.content}</p>
                </div>

                {post.imageUrl && (
                  <div className="px-8 pb-8">
                    <img src={post.imageUrl} alt="Post attachment" className="w-full max-h-[500px] object-cover rounded-[2rem] shadow-sm border border-gray-50" />
                  </div>
                )}

                <div className="px-8 py-6 bg-gray-50/50 border-t border-gray-50 flex flex-wrap gap-8">
                  <button 
                    onClick={() => handleLike(post)}
                    className={`flex items-center gap-3 transition-all group ${isLiked ? 'text-red-500' : 'text-gray-500 hover:text-red-500'}`}
                  >
                    <div className={`p-3 rounded-2xl transition-all border ${isLiked ? 'bg-red-50 border-red-100 shadow-sm' : 'bg-white border-transparent group-hover:border-red-100 group-hover:bg-red-50'}`}>
                      <Heart size={22} className={isLiked ? 'fill-red-500' : 'group-hover:scale-110 transition-transform'} />
                    </div>
                    <span className="font-black text-lg">{post.likesCount || 0}</span>
                  </button>

                  <button 
                    onClick={() => setActiveComments(activeComments === post.id ? null : post.id)}
                    className={`flex items-center gap-3 transition-all group ${activeComments === post.id ? 'text-bookup-primary' : 'text-gray-500 hover:text-bookup-primary'}`}
                  >
                    <div className={`p-3 rounded-2xl transition-all border ${activeComments === post.id ? 'bg-bookup-primary/10 border-bookup-primary/20 shadow-sm' : 'bg-white border-transparent group-hover:border-bookup-primary/20 group-hover:bg-bookup-primary/10'}`}>
                      <MessageCircle size={22} className={activeComments === post.id ? 'fill-bookup-primary' : 'group-hover:scale-110 transition-transform'} />
                    </div>
                    <span className="font-black text-lg">{post.comments?.length || 0}</span>
                  </button>

                  <button className="flex items-center gap-3 text-gray-500 hover:text-gray-900 transition-colors group ml-auto">
                    <div className="p-3 rounded-2xl bg-white border border-transparent group-hover:border-gray-200 group-hover:shadow-sm transition-all">
                      <Share2 size={22} />
                    </div>
                  </button>
                </div>

                {/* Comments Section */}
                {activeComments === post.id && (
                  <div className="px-8 py-8 border-t border-gray-100 bg-white animate-in slide-in-from-top-4 duration-300">
                    <div className="space-y-6 mb-8">
                      {post.comments && post.comments.length > 0 ? (
                        post.comments.map((comment, idx) => (
                          <div key={idx} className="flex gap-4">
                            <img src={`https://ui-avatars.com/api/?name=${comment.userName}&background=random&bold=true`} className="w-10 h-10 rounded-xl" alt="" />
                            <div className="flex-1 bg-gray-50 p-4 rounded-2xl">
                              <div className="flex justify-between items-center mb-1">
                                <span className="font-black text-sm text-gray-900">{comment.userName}</span>
                                <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">
                                  {new Date(comment.timestamp).toLocaleDateString()}
                                </span>
                              </div>
                              <p className="text-gray-600 text-sm font-medium">{comment.text}</p>
                            </div>
                          </div>
                        ))
                      ) : (
                        <p className="text-center text-gray-400 font-bold py-4">No comments yet. Start the conversation!</p>
                      )}
                    </div>

                    <div className="flex gap-4">
                      <input 
                        type="text" 
                        value={commentText}
                        onChange={(e) => setCommentText(e.target.value)}
                        placeholder="Write a comment..."
                        className="flex-1 bg-gray-50 border-none rounded-2xl px-6 py-4 focus:ring-2 focus:ring-bookup-primary/20 font-medium"
                        onKeyDown={(e) => e.key === 'Enter' && handlePostComment(post.id)}
                      />
                      <button 
                        onClick={() => handlePostComment(post.id)}
                        className="p-4 bg-gray-900 text-white rounded-2xl hover:bg-bookup-primary transition-all shadow-lg active:scale-95"
                      >
                        <Send size={20} />
                      </button>
                    </div>
                  </div>
                )}
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}
