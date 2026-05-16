import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import Home from './pages/Home';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import Layout from './components/Layout';
import TutorDirectory from './pages/TutorDirectory';
import Materials from './pages/Materials';
import NewsFeed from './pages/NewsFeed';
import Profile from './pages/Profile';
import Messages from './pages/Messages';
import PublicProfile from './pages/PublicProfile';
import HelpRequests from './pages/HelpRequests';
import AdminPanel from './pages/AdminPanel';
import Download from './pages/Download';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { CallProvider } from './contexts/CallContext';
import ProtectedRoute from './components/ProtectedRoute';

// Helper component to redirect authenticated users away from Login/Home
function PublicRoute({ children }: { children: ReactNode }) {
  const { currentUser } = useAuth();
  if (currentUser) {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
}

function App() {
  return (
    <AuthProvider>
      <CallProvider>
        <Router>
          <Routes>
            {/* Public Routes (Only accessible if NOT logged in) */}
            <Route path="/" element={<PublicRoute><Home /></PublicRoute>} />
            <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
            <Route path="/signup" element={<PublicRoute><Signup /></PublicRoute>} />
            
            {/* Protected App Routes */}
            <Route element={<ProtectedRoute />}>
              <Route element={<Layout />}>
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/tutors" element={<TutorDirectory />} />
                <Route path="/materials" element={<Materials />} />
                <Route path="/feed" element={<NewsFeed />} />
                <Route path="/messages" element={<Messages />} />
                <Route path="/requests" element={<HelpRequests />} />
                <Route path="/profile" element={<Profile />} />
                <Route path="/admin" element={<AdminPanel />} />
                <Route path="/download" element={<Download />} />
                <Route path="/user/:id" element={<PublicProfile />} />
              </Route>
            </Route>
          </Routes>
        </Router>
      </CallProvider>
    </AuthProvider>
  );
}

export default App;
