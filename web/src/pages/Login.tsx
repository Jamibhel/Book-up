import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login, loginWithGoogle } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setError('');
      setLoading(true);
      await login(email, password);
      navigate('/dashboard');
    } catch (err: any) {
      setError('Failed to sign in. Please check your credentials.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSignIn = async () => {
    try {
      setError('');
      setLoading(true);
      await loginWithGoogle();
      navigate('/dashboard');
    } catch (err: any) {
      console.error(err);
      let errMsg = 'Google sign-in failed or was cancelled.';
      if (err.code === 'auth/operation-not-allowed') {
        errMsg = 'Google auth provider is disabled. Please enable Google Sign-In in your Firebase Console.';
      } else if (err.code === 'auth/unauthorized-domain') {
        errMsg = `This domain is not authorized for Google Sign-In. Add ${window.location.hostname} to your Authorized Domains in the Firebase Console.`;
      } else if (err.message) {
        errMsg = `Google sign-in failed: ${err.message}`;
      }
      setError(errMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-[80vh]">
      <div className="bg-bookup-surface p-10 rounded-[2rem] shadow-2xl shadow-bookup-primary/10 border border-bookup-border w-full max-w-md relative overflow-hidden">
        
        {/* Decorative corner accent */}
        <div className="absolute -top-12 -right-12 w-32 h-32 bg-bookup-primary/10 rounded-full blur-2xl"></div>
        <div className="absolute -bottom-12 -left-12 w-32 h-32 bg-bookup-secondary/10 rounded-full blur-2xl"></div>

        <div className="relative z-10">
          <div className="flex justify-center mb-6">
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-bookup-primary to-bookup-secondary flex items-center justify-center text-white font-black text-3xl shadow-lg shadow-bookup-primary/30">
              B
            </div>
          </div>
          
          <h2 className="text-3xl font-extrabold text-center text-bookup-text mb-2 tracking-tight">Welcome Back</h2>
          <p className="text-center text-bookup-text-muted mb-8 font-medium">Sign in to continue your academic journey</p>
          
          {error && (
            <div className="bg-red-50 text-red-600 p-4 rounded-xl font-bold text-sm mb-6 border border-red-100 flex items-center gap-2">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
              {error}
            </div>
          )}

          <button 
            type="button"
            onClick={handleGoogleSignIn}
            disabled={loading}
            className="w-full bg-white text-gray-700 border-2 border-gray-200 py-3.5 rounded-xl font-bold hover:bg-gray-50 hover:border-gray-300 transition-all flex items-center justify-center gap-3 mb-6 disabled:opacity-70 disabled:cursor-not-allowed"
          >
            <svg viewBox="0 0 24 24" className="w-6 h-6" xmlns="http://www.w3.org/2000/svg">
              <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
              <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
              <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
              <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
            </svg>
            Continue with Google
          </button>

          <div className="relative flex items-center justify-center mb-6">
            <div className="border-t border-gray-200 w-full"></div>
            <div className="bg-white px-4 text-sm text-gray-400 font-bold uppercase tracking-wider absolute">Or sign in with email</div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-bold text-bookup-text mb-1.5">Email Address</label>
              <input 
                type="email" 
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-5 py-3.5 bg-bookup-bg border border-bookup-border rounded-xl focus:ring-2 focus:ring-bookup-primary focus:border-bookup-primary transition-all outline-none font-medium text-gray-900" 
                placeholder="you@example.com" 
              />
            </div>
            <div>
              <div className="flex justify-between mb-1.5">
                <label className="block text-sm font-bold text-bookup-text">Password</label>
                <a href="#" className="text-sm text-bookup-secondary hover:text-bookup-secondary-dark font-bold transition-colors">Forgot password?</a>
              </div>
              <input 
                type="password" 
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-5 py-3.5 bg-bookup-bg border border-bookup-border rounded-xl focus:ring-2 focus:ring-bookup-primary focus:border-bookup-primary transition-all outline-none font-medium text-gray-900" 
                placeholder="••••••••" 
              />
            </div>
            
            <button 
              type="submit" 
              disabled={loading}
              className="w-full bg-bookup-primary text-white py-4 rounded-xl font-bold hover:bg-bookup-primary-dark transition-all shadow-md shadow-bookup-primary/20 mt-4 disabled:opacity-70 disabled:cursor-not-allowed"
            >
              {loading ? 'Signing In...' : 'Sign In'}
            </button>
            
            <div className="mt-6 text-center text-sm font-medium text-bookup-text-muted">
              Don't have an account? <Link to="/signup" className="text-bookup-primary font-bold hover:underline">Sign up</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
