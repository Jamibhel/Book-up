import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { doc, updateDoc } from 'firebase/firestore';
import { ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage';
import { db, storage } from '../lib/firebase';
import { User as UserIcon, Mail, MapPin, Briefcase, Award, Star, Clock, Camera, ShieldCheck } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function Profile() {
  const { currentUser, userProfile, logout } = useAuth();
  const navigate = useNavigate();
  const [saving, setSaving] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({
    displayName: '',
    bio: '',
    locationName: '',
    workPreference: 'both',
    photoURL: '',
    role: 'student'
  });
  
  const [uploadingImage, setUploadingImage] = useState(false);

  useEffect(() => {
    if (userProfile) {
      setFormData({
        displayName: userProfile.displayName || currentUser?.displayName || '',
        bio: (userProfile as any).bio || '',
        locationName: (userProfile as any).locationName || '',
        workPreference: (userProfile as any).workPreference || 'both',
        photoURL: userProfile.photoURL || userProfile.photoUrl || currentUser?.photoURL || '',
        role: userProfile.role || 'student'
      });
    }
  }, [userProfile, currentUser]);

  const handleSave = async () => {
    if (!currentUser) return;
    try {
      setSaving(true);
      const docRef = doc(db, 'users', currentUser.uid);
      await updateDoc(docRef, formData);
      setEditMode(false);
    } catch (err) {
      console.error(err);
      alert('Failed to update profile.');
    } finally {
      setSaving(false);
    }
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || !e.target.files[0] || !currentUser) return;
    const file = e.target.files[0];
    setUploadingImage(true);
    
    try {
      const storageRef = ref(storage, `userProfiles/${currentUser.uid}/profile_${Date.now()}`);
      const uploadTask = await uploadBytesResumable(storageRef, file);
      const downloadUrl = await getDownloadURL(uploadTask.ref);
      
      setFormData(prev => ({ ...prev, photoURL: downloadUrl }));
      
      // Update Firestore immediately to keep it in sync
      const docRef = doc(db, 'users', currentUser.uid);
      await updateDoc(docRef, { photoURL: downloadUrl, photoUrl: downloadUrl });
    } catch (err) {
      console.error(err);
      alert('Failed to upload image. Please check your network and Firebase storage rules.');
    } finally {
      setUploadingImage(false);
    }
  };

  const profilePhoto = formData.photoURL || `https://ui-avatars.com/api/?name=${encodeURIComponent(formData.displayName || currentUser?.email || 'User')}&background=1B9A8B&color=fff&size=200&bold=true`;

  return (
    <div className="max-w-4xl mx-auto space-y-8 animate-in fade-in duration-500 pb-20">
      <div className="bg-white rounded-[2.5rem] shadow-sm border border-gray-100 overflow-hidden relative">
        <div className="h-48 bg-gradient-to-r from-bookup-primary to-bookup-secondary relative">
          <div className="absolute inset-0 bg-black/10"></div>
          {(userProfile?.isAdmin || userProfile?.role === 'admin') && (
            <div className="absolute top-6 left-6 bg-amber-500 text-white px-4 py-2 rounded-xl font-black text-xs uppercase tracking-[0.2em] flex items-center gap-2 shadow-lg animate-in slide-in-from-left-4 duration-500">
              <ShieldCheck size={16} /> Administrator
            </div>
          )}
        </div>
        
        <div className="px-8 pb-8">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-end -mt-16 mb-6 gap-4">
            <div className="relative group">
              <img 
                src={profilePhoto} 
                alt="Profile" 
                className={`w-32 h-32 rounded-[2rem] object-cover border-4 border-white shadow-xl bg-white ${uploadingImage ? 'opacity-50' : ''}`} 
              />
              {editMode && (
                <label className="absolute inset-0 flex items-center justify-center bg-black/40 text-white rounded-[2rem] opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer">
                  <Camera size={32} />
                  <input type="file" className="hidden" accept="image/*" onChange={handleImageUpload} disabled={uploadingImage} />
                </label>
              )}
            </div>
            <div className="flex flex-wrap gap-3">
              {(userProfile?.isAdmin || userProfile?.role === 'admin') && (
                <button 
                  onClick={() => navigate('/admin')} 
                  className="px-8 py-3.5 bg-amber-500 text-white font-black rounded-2xl hover:bg-amber-600 transition-all shadow-xl shadow-amber-500/20 uppercase tracking-widest text-xs flex items-center gap-2"
                >
                  <ShieldCheck size={18} /> Admin Console
                </button>
              )}
              {editMode ? (
                <>
                  <button onClick={() => setEditMode(false)} className="px-8 py-3.5 bg-gray-100 text-gray-700 font-black rounded-2xl hover:bg-gray-200 transition-all uppercase tracking-widest text-xs">
                    Cancel
                  </button>
                  <button onClick={handleSave} disabled={saving} className="px-8 py-3.5 bg-bookup-primary text-white font-black rounded-2xl hover:bg-bookup-primary-dark transition-all shadow-lg shadow-bookup-primary/20 uppercase tracking-widest text-xs">
                    {saving ? 'Saving...' : 'Save Changes'}
                  </button>
                </>
              ) : (
                <>
                  <button onClick={() => setEditMode(true)} className="px-8 py-3.5 bg-gray-900 text-white font-black rounded-2xl hover:bg-gray-800 transition-all shadow-xl uppercase tracking-widest text-xs">
                    Edit Profile
                  </button>
                  <button onClick={logout} className="px-8 py-3.5 bg-red-50 text-red-600 font-black rounded-2xl hover:bg-red-100 transition-all uppercase tracking-widest text-xs">
                    Log out
                  </button>
                </>
              )}
            </div>
          </div>

          <div className="space-y-8 mt-10">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
              <div className="space-y-2">
                <label className="block text-xs font-black text-gray-400 uppercase tracking-[0.2em] ml-1">Full Name</label>
                {editMode ? (
                  <input 
                    type="text" 
                    value={formData.displayName}
                    onChange={e => setFormData({...formData, displayName: e.target.value})}
                    className="w-full px-6 py-4 bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-bookup-primary/20 outline-none font-black text-gray-900 font-display" 
                  />
                ) : (
                  <div className="flex items-center gap-4 text-2xl font-black text-gray-900 font-display">
                    {formData.displayName || 'Anonymous User'}
                  </div>
                )}
              </div>
              
              <div className="space-y-2">
                <label className="block text-xs font-black text-gray-400 uppercase tracking-[0.2em] ml-1">Account Role</label>
                {editMode ? (
                  <select 
                    value={formData.role}
                    onChange={e => setFormData({...formData, role: e.target.value})}
                    className="w-full px-6 py-4 bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-bookup-primary/20 outline-none font-bold text-gray-900"
                  >
                    <option value="student">Student</option>
                    <option value="tutor">Tutor</option>
                  </select>
                ) : (
                  <div className="flex items-center gap-2">
                    <span className="bg-gray-900 text-white px-4 py-1.5 rounded-lg text-xs font-black uppercase tracking-widest">
                      {formData.role}
                    </span>
                  </div>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <label className="block text-xs font-black text-gray-400 uppercase tracking-[0.2em] ml-1">Bio</label>
              {editMode ? (
                <textarea 
                  value={formData.bio}
                  onChange={e => setFormData({...formData, bio: e.target.value})}
                  className="w-full px-6 py-4 bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-bookup-primary/20 outline-none font-medium text-gray-900 resize-none h-32" 
                />
              ) : (
                <p className="text-lg font-medium text-gray-600 leading-relaxed">
                  {formData.bio || 'No bio provided.'}
                </p>
              )}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-10 pt-8 border-t border-gray-50">
              <div className="space-y-2">
                <label className="block text-xs font-black text-gray-400 uppercase tracking-[0.2em] ml-1">Email Address</label>
                <div className="flex items-center gap-3 text-lg font-bold text-gray-900">
                  <Mail size={20} className="text-gray-400" />
                  {currentUser?.email}
                </div>
              </div>

              <div className="space-y-2">
                <label className="block text-xs font-black text-gray-400 uppercase tracking-[0.2em] ml-1">Location</label>
                {editMode ? (
                  <input 
                    type="text" 
                    value={formData.locationName}
                    onChange={e => setFormData({...formData, locationName: e.target.value})}
                    className="w-full px-6 py-4 bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-bookup-primary/20 outline-none font-medium text-gray-900" 
                  />
                ) : (
                  <div className="flex items-center gap-3 text-lg font-bold text-gray-900">
                    <MapPin size={20} className="text-bookup-primary" />
                    {formData.locationName || 'Global'}
                  </div>
                )}
              </div>
            </div>
            
            {userProfile?.role === 'tutor' && (
              <div className="pt-10 border-t border-gray-50">
                <h3 className="text-xs font-black text-gray-400 mb-6 uppercase tracking-[0.2em] ml-1">Tutor Performance</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                   {[
                     { label: 'Rating', value: (userProfile as any).rating || '0.0', icon: Star, color: 'text-amber-500' },
                     { label: 'Rate/Hr', value: `₦${(userProfile as any).hourlyRate || '0'}`, icon: Clock, color: 'text-bookup-primary' },
                     { label: 'Reviews', value: (userProfile as any).reviewCount || '0', icon: UserIcon, color: 'text-bookup-secondary' }
                   ].map((stat, i) => (
                     <div key={i} className="bg-gray-50 p-6 rounded-3xl border border-transparent hover:border-gray-100 transition-all group">
                        <stat.icon size={24} className={`${stat.color} mb-3 group-hover:scale-110 transition-transform`} />
                        <p className="font-black text-3xl text-gray-900 font-display">{stat.value}</p>
                        <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mt-1">{stat.label}</p>
                     </div>
                   ))}
                </div>
              </div>
            )}
            
          </div>
        </div>
      </div>
    </div>
  );
}
