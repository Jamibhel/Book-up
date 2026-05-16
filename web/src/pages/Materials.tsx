import { useState, useEffect } from 'react';
import { FileText, Download, Eye, Folder, PlayCircle, X } from 'lucide-react';
import { collection, query, orderBy, getDocs, addDoc, serverTimestamp } from 'firebase/firestore';
import { db, storage } from '../lib/firebase';
import { useAuth } from '../contexts/AuthContext';
import { ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage';

interface StudyMaterial {
  id: string;
  title: string;
  description: string;
  subject: string;
  materialType: string; // "Notes", "Video Link", "PDF"
  fileUrl: string;
  uploaderName: string;
  averageRating: number;
  downloadCount: number;
  premium: boolean;
  price: number;
}

export default function Materials() {
  const { currentUser } = useAuth();
  const [materials, setMaterials] = useState<StudyMaterial[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploadData, setUploadData] = useState({ title: '', subject: '', type: 'PDF' });
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);

  const fetchMaterials = async () => {
    try {
      setLoading(true);
      const q = query(collection(db, 'studyMaterials'), orderBy('timestamp', 'desc'));
      const querySnapshot = await getDocs(q);
      const fetchedMats: StudyMaterial[] = [];
      querySnapshot.forEach((doc) => {
        fetchedMats.push({ id: doc.id, ...doc.data() } as StudyMaterial);
      });
      setMaterials(fetchedMats);
    } catch (err: any) {
      console.error("Error fetching materials:", err);
      setError('Failed to load study materials. Check Firebase permissions.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMaterials();
  }, []);

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser) return alert('Must be logged in');
    if (!uploadFile) return alert('Please select a file to upload');
    
    try {
      setUploading(true);
      
      // Upload file to Firebase Storage
      const storageRef = ref(storage, `materials/${Date.now()}/${uploadFile.name}`);
      const uploadTask = await uploadBytesResumable(storageRef, uploadFile);
      const downloadUrl = await getDownloadURL(uploadTask.ref);

      await addDoc(collection(db, 'studyMaterials'), {
        title: uploadData.title,
        subject: uploadData.subject,
        materialType: uploadData.type,
        fileUrl: downloadUrl,
        uploaderUid: currentUser.uid,
        uploaderName: currentUser.displayName || currentUser.email,
        downloadCount: 0,
        timestamp: serverTimestamp()
      });
      setShowUploadModal(false);
      setUploadData({ title: '', subject: '', type: 'PDF' });
      setUploadFile(null);
      fetchMaterials(); // refresh list
    } catch (err) {
      console.error(err);
      alert('Upload failed. Please check your network and Firebase storage rules.');
    } finally {
      setUploading(false);
    }
  };

  const getIconForType = (type: string) => {
    if (type?.toLowerCase().includes('video')) return <PlayCircle size={20} />;
    return <FileText size={20} />;
  };

  const getColorForType = (type: string) => {
    if (type?.toLowerCase().includes('video')) return 'bg-red-100 text-red-600';
    if (type?.toLowerCase().includes('pdf')) return 'bg-blue-100 text-blue-600';
    return 'bg-green-100 text-green-600';
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8 border-b border-bookup-border pb-6">
        <div>
          <h1 className="text-4xl font-extrabold text-bookup-text tracking-tight">Study Materials</h1>
          <p className="text-lg text-bookup-text-muted mt-2">Access notes, past questions, and resources uploaded by top tutors.</p>
        </div>
        <button 
          onClick={() => setShowUploadModal(true)}
          className="bg-gray-900 text-white px-6 py-3 rounded-xl font-bold hover:bg-bookup-primary transition-all shadow-md flex items-center gap-2"
        >
          <Folder size={20} />
          Upload Material
        </button>
      </div>

      <div className="flex gap-3 overflow-x-auto pb-4 custom-scrollbar">
        {['All Subjects', 'Mathematics', 'Physics', 'Chemistry', 'English', 'Biology'].map((subject, i) => (
          <button 
            key={subject}
            className={`whitespace-nowrap px-6 py-2.5 rounded-full font-bold transition-all border ${
              i === 0 
                ? 'bg-bookup-primary text-white border-bookup-primary shadow-md' 
                : 'bg-white text-gray-600 border-gray-200 hover:border-bookup-primary hover:text-bookup-primary'
            }`}
          >
            {subject}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex justify-center items-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-bookup-primary"></div>
        </div>
      ) : error ? (
        <div className="bg-red-50 text-red-600 p-6 rounded-2xl font-bold border border-red-100">
          {error}
        </div>
      ) : materials.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-[2rem] border border-gray-100 shadow-sm">
          <Folder size={48} className="mx-auto text-gray-300 mb-4" />
          <h3 className="text-2xl font-bold text-gray-900">No materials yet</h3>
          <p className="text-gray-500 mt-2 font-medium">Tutors haven't uploaded any study materials.</p>
        </div>
      ) : (
        <div className="bg-white rounded-[2rem] shadow-sm border border-gray-100 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100">
                  <th className="p-6 font-bold text-gray-500 uppercase tracking-wider text-sm">Material Name</th>
                  <th className="p-6 font-bold text-gray-500 uppercase tracking-wider text-sm hidden md:table-cell">Subject</th>
                  <th className="p-6 font-bold text-gray-500 uppercase tracking-wider text-sm hidden sm:table-cell">Author</th>
                  <th className="p-6 font-bold text-gray-500 uppercase tracking-wider text-sm text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {materials.map((material) => (
                  <tr key={material.id} className="hover:bg-gray-50/50 transition-colors group">
                    <td className="p-6">
                      <div className="flex items-center gap-4">
                        <div className={`p-3 rounded-2xl ${getColorForType(material.materialType)}`}>
                          {getIconForType(material.materialType)}
                        </div>
                        <div>
                          <p className="font-black text-lg text-gray-900 group-hover:text-bookup-primary transition-colors">{material.title}</p>
                          <div className="flex items-center gap-3 mt-1 text-sm font-bold text-gray-400">
                            <span className="bg-gray-100 px-2 py-0.5 rounded-md">{material.materialType || 'File'}</span>
                            <span>{material.downloadCount || 0} downloads</span>
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="p-6 hidden md:table-cell">
                      <span className="font-bold text-gray-600">{material.subject || 'General'}</span>
                    </td>
                    <td className="p-6 hidden sm:table-cell">
                      <span className="font-bold text-gray-900">{material.uploaderName || 'Anonymous'}</span>
                    </td>
                    <td className="p-6 text-right">
                      <div className="flex items-center justify-end gap-3">
                        <button className="p-2.5 text-gray-400 hover:text-bookup-secondary bg-white border border-gray-200 rounded-xl hover:border-bookup-secondary transition-all shadow-sm">
                          <Eye size={20} />
                        </button>
                        <a 
                          href={material.fileUrl || '#'} 
                          target="_blank"
                          rel="noreferrer"
                          className="p-2.5 text-white bg-gray-900 hover:bg-bookup-primary rounded-xl transition-all shadow-sm"
                        >
                          <Download size={20} />
                        </a>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Upload Modal */}
      {showUploadModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/40 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-white rounded-[2rem] shadow-2xl w-full max-w-md overflow-hidden animate-in zoom-in-95 duration-200">
            <div className="p-6 border-b border-gray-100 flex justify-between items-center">
              <h2 className="text-2xl font-black text-gray-900">Upload Material</h2>
              <button onClick={() => setShowUploadModal(false)} className="text-gray-400 hover:text-gray-900 bg-gray-50 hover:bg-gray-100 p-2 rounded-full transition-colors">
                <X size={20} />
              </button>
            </div>
            
            <form onSubmit={handleUpload} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1.5">Material Title</label>
                <input 
                  type="text" 
                  required
                  value={uploadData.title}
                  onChange={e => setUploadData({...uploadData, title: e.target.value})}
                  className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-bookup-primary outline-none font-medium" 
                  placeholder="e.g. Intro to Calculus Notes" 
                />
              </div>
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-1.5">Subject</label>
                <input 
                  type="text" 
                  required
                  value={uploadData.subject}
                  onChange={e => setUploadData({...uploadData, subject: e.target.value})}
                  className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-bookup-primary outline-none font-medium" 
                  placeholder="e.g. Mathematics" 
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1.5">Type</label>
                  <select 
                    value={uploadData.type}
                    onChange={e => setUploadData({...uploadData, type: e.target.value})}
                    className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-bookup-primary outline-none font-medium"
                  >
                    <option>PDF</option>
                    <option>Notes</option>
                    <option>Video Link</option>
                    <option>Past Paper</option>
                  </select>
                </div>
              </div>
              
              <div>
                  <label className="block text-sm font-bold text-gray-700 mb-1.5">Upload File</label>
                  <input 
                    type="file" 
                    onChange={e => setUploadFile(e.target.files ? e.target.files[0] : null)}
                    className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-bookup-primary outline-none font-medium text-sm" 
                    required
                  />
              </div>

              <div className="pt-4 flex gap-3">
                <button 
                  type="button" 
                  onClick={() => setShowUploadModal(false)}
                  className="flex-1 px-4 py-3 bg-gray-50 text-gray-600 font-bold rounded-xl hover:bg-gray-100 transition-colors"
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  disabled={uploading}
                  className="flex-1 px-4 py-3 bg-bookup-primary text-white font-bold rounded-xl hover:bg-bookup-primary-dark transition-colors disabled:opacity-50"
                >
                  {uploading ? 'Uploading...' : 'Publish'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
