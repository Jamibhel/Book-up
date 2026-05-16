# 🚀 PHASE 1: BookUp Web App - Project Setup & Initialization

**Approach**: All at Once  
**Starting With**: Phase 1 Project Setup  
**Status**: Ready to Begin

---

## 📋 PHASE 1 COMPLETE SPECIFICATION

### **What You're About to Build**
A complete, production-ready project structure for the BookUp web application with:
- React 18 + TypeScript
- Vite (fast build tool)
- Tailwind CSS + Shadcn/ui
- Firebase integration
- Redux Toolkit for state management
- React Router for navigation
- Complete folder structure
- All configuration files
- Dev environment ready to start coding

---

## 🎯 PHASE 1: STEP-BY-STEP INSTRUCTIONS

### **STEP 1: Create Vite Project**
```bash
npm create vite@latest bookup-web -- --template react-ts
cd bookup-web
npm install
```

### **STEP 2: Install Core Dependencies**
```bash
npm install react-router-dom
npm install @reduxjs/toolkit react-redux
npm install firebase
npm install -D tailwindcss postcss autoprefixer
npm install -D typescript @types/react @types/react-dom
npm install class-variance-authority clsx tailwind-merge lucide-react
npm install @radix-ui/react-slot
npx shadcn-ui@latest init
```

### **STEP 3: Configure Tailwind CSS**
Create `tailwind.config.js`:
```javascript
export default {
  darkMode: ["class"],
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: "#f0f7ff",
          500: "#0066cc",
          600: "#0052a3",
          700: "#003d7a",
        },
        secondary: {
          50: "#f5f3ff",
          500: "#9333ea",
        },
      },
    },
  },
  plugins: [require("tailwindcss-animate")],
}
```

Create `postcss.config.js`:
```javascript
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
```

### **STEP 4: Setup Vite Configuration**
Create/Update `vite.config.ts`:
```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    open: true,
  },
})
```

### **STEP 5: Create Project Folder Structure**
```
src/
├── components/
│   ├── Auth/
│   │   ├── SignUp.tsx
│   │   ├── SignIn.tsx
│   │   └── AuthLayout.tsx
│   ├── Chat/
│   │   ├── ChatList.tsx
│   │   └── ChatWindow.tsx
│   ├── Tutor/
│   │   ├── TutorCard.tsx
│   │   └── TutorFilters.tsx
│   ├── Booking/
│   │   ├── BookingForm.tsx
│   │   └── Calendar.tsx
│   ├── Common/
│   │   ├── Header.tsx
│   │   ├── Navbar.tsx
│   │   ├── Sidebar.tsx
│   │   └── LoadingSpinner.tsx
│   └── Layout/
│       ├── MainLayout.tsx
│       └── ProtectedRoute.tsx
├── pages/
│   ├── Auth/
│   │   ├── SignUpPage.tsx
│   │   ├── SignInPage.tsx
│   │   ├── ForgotPasswordPage.tsx
│   │   └── ResetPasswordPage.tsx
│   ├── Dashboard/
│   │   └── DashboardPage.tsx
│   ├── Chat/
│   │   └── ChatPage.tsx
│   ├── Tutor/
│   │   ├── TutorSearchPage.tsx
│   │   └── TutorProfilePage.tsx
│   ├── Booking/
│   │   └── BookingPage.tsx
│   ├── Profile/
│   │   ├── StudentProfilePage.tsx
│   │   └── TutorProfilePage.tsx
│   └── NotFound.tsx
├── services/
│   ├── firebaseService.ts
│   ├── authService.ts
│   ├── chatService.ts
│   ├── tutorService.ts
│   └── bookingService.ts
├── store/
│   ├── store.ts
│   ├── authSlice.ts
│   ├── chatSlice.ts
│   ├── tutorSlice.ts
│   └── bookingSlice.ts
├── types/
│   ├── user.ts
│   ├── chat.ts
│   ├── tutor.ts
│   └── booking.ts
├── utils/
│   ├── validators.ts
│   ├── formatters.ts
│   └── helpers.ts
├── styles/
│   ├── globals.css
│   └── variables.css
├── App.tsx
├── main.tsx
└── index.css
```

### **STEP 6: Create Firebase Configuration**

Create `src/services/firebaseService.ts`:
```typescript
import { initializeApp } from 'firebase/app'
import { getAuth } from 'firebase/auth'
import { getFirestore } from 'firebase/firestore'
import { getStorage } from 'firebase/storage'

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

const app = initializeApp(firebaseConfig)
export const auth = getAuth(app)
export const db = getFirestore(app)
export const storage = getStorage(app)

export default app
```

Create `.env.example`:
```
VITE_FIREBASE_API_KEY=your_api_key
VITE_FIREBASE_AUTH_DOMAIN=your_auth_domain
VITE_FIREBASE_PROJECT_ID=your_project_id
VITE_FIREBASE_STORAGE_BUCKET=your_storage_bucket
VITE_FIREBASE_MESSAGING_SENDER_ID=your_messaging_id
VITE_FIREBASE_APP_ID=your_app_id
```

### **STEP 7: Setup Redux Store**

Create `src/store/store.ts`:
```typescript
import { configureStore } from '@reduxjs/toolkit'
import authReducer from './authSlice'
import chatReducer from './chatSlice'
import tutorReducer from './tutorSlice'
import bookingReducer from './bookingSlice'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    chat: chatReducer,
    tutor: tutorReducer,
    booking: bookingReducer,
  },
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
```

Create `src/store/authSlice.ts`:
```typescript
import { createSlice, PayloadAction } from '@reduxjs/toolkit'

interface User {
  uid: string
  email: string
  name: string
  role: 'student' | 'tutor'
  profilePicture?: string
}

interface AuthState {
  user: User | null
  isAuthenticated: boolean
  loading: boolean
  error: string | null
}

const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
  loading: false,
  error: null,
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setUser: (state, action: PayloadAction<User>) => {
      state.user = action.payload
      state.isAuthenticated = true
      state.loading = false
    },
    clearUser: (state) => {
      state.user = null
      state.isAuthenticated = false
      state.loading = false
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload
    },
    setError: (state, action: PayloadAction<string | null>) => {
      state.error = action.payload
    },
  },
})

export const { setUser, clearUser, setLoading, setError } = authSlice.actions
export default authSlice.reducer
```

Create stub slices for chat, tutor, and booking (similar structure).

### **STEP 8: Create TypeScript Types**

Create `src/types/user.ts`:
```typescript
export interface User {
  uid: string
  email: string
  name: string
  phone?: string
  profilePicture?: string
  role: 'student' | 'tutor'
  createdAt: Date
  updatedAt: Date
}

export interface StudentProfile extends User {
  role: 'student'
  learningGoals?: string
  subjectsInterested?: string[]
  location?: string
  timezone?: string
}

export interface TutorProfile extends User {
  role: 'tutor'
  bio?: string
  subjectsTaught: string[]
  hourlyRate: number
  yearsOfExperience: number
  qualifications?: string[]
  verificationBadge: boolean
  averageRating: number
  reviewCount: number
}
```

### **STEP 9: Setup Main Application Files**

Create `src/App.tsx`:
```typescript
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { Provider } from 'react-redux'
import { store } from './store/store'
import ProtectedRoute from './components/Layout/ProtectedRoute'
import MainLayout from './components/Layout/MainLayout'

// Pages
import SignUpPage from './pages/Auth/SignUpPage'
import SignInPage from './pages/Auth/SignInPage'
import DashboardPage from './pages/Dashboard/DashboardPage'
import NotFound from './pages/NotFound'

function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <Routes>
          <Route path="/signup" element={<SignUpPage />} />
          <Route path="/signin" element={<SignInPage />} />
          
          <Route element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
            <Route path="/dashboard" element={<DashboardPage />} />
            {/* Add other protected routes here */}
          </Route>
          
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </Provider>
  )
}

export default App
```

Create `src/main.tsx`:
```typescript
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
```

Create `src/index.css`:
```css
@tailwind base;
@tailwind components;
@tailwind utilities;

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
```

### **STEP 10: Create ESLint & Prettier Configuration**

Create `.eslintrc.cjs`:
```javascript
module.exports = {
  root: true,
  env: { browser: true, es2020: true },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:react-hooks/recommended',
  ],
  ignorePatterns: ['dist', '.eslintrc.cjs'],
  parser: '@typescript-eslint/parser',
  plugins: ['react-refresh'],
  rules: {
    'react-refresh/only-export-components': [
      'warn',
      { allowConstantExport: true },
    ],
  },
}
```

Create `.prettierrc`:
```json
{
  "semi": true,
  "trailingComma": "all",
  "singleQuote": true,
  "printWidth": 100,
  "tabWidth": 2,
  "useTabs": false
}
```

### **STEP 11: Update package.json Scripts**

Update `package.json`:
```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "lint:fix": "eslint . --ext ts,tsx --fix"
  }
}
```

### **STEP 12: Create README.md**

Create `README.md`:
```markdown
# BookUp Web Application

A modern, responsive web application for connecting students with tutors.

## Features
- User authentication (Student & Tutor roles)
- User profiles (Student & Tutor specific)
- Tutor discovery & search
- Real-time chat messaging
- Booking system with calendar
- Reviews & ratings
- PWA support (offline, installable)

## Tech Stack
- React 18 + TypeScript
- Vite
- Tailwind CSS + Shadcn/ui
- Firebase (Authentication, Firestore, Storage)
- Redux Toolkit
- React Router v6

## Getting Started

### Prerequisites
- Node.js 16+
- npm or yarn
- Firebase account

### Installation
1. Clone the repository
2. Copy `.env.example` to `.env.local`
3. Add your Firebase credentials
4. Install dependencies: `npm install`
5. Start dev server: `npm run dev`

### Build
```bash
npm run build
```

### Lint
```bash
npm run lint
npm run lint:fix
```

## Project Structure
See folder structure above.

## Contributing
Follow ESLint and Prettier rules.

## License
MIT
```

---

## ✅ DELIVERABLES CHECKLIST FOR PHASE 1

- [ ] Vite project created with React + TypeScript
- [ ] Tailwind CSS configured
- [ ] Shadcn/ui installed and configured
- [ ] Firebase SDK integrated
- [ ] Redux Toolkit store setup
- [ ] React Router configured
- [ ] Complete folder structure created
- [ ] TypeScript interfaces defined
- [ ] Redux slices created (auth, chat, tutor, booking)
- [ ] Firebase service configured
- [ ] Environment variables setup
- [ ] ESLint configured
- [ ] Prettier configured
- [ ] README.md created
- [ ] Dev server runs without errors
- [ ] All dependencies installed
- [ ] .gitignore updated
- [ ] Initial commit ready

---

## 🚀 HOW TO PROCEED

Now that you have the complete Phase 1 specification:

### **Option 1: I Set Up Everything (Recommended)**
Say: **"Build Phase 1 now. Set up the complete project structure with all configurations, Redux store, Firebase integration, folder structure, and all configuration files."**

Then I'll:
1. Create the complete project structure
2. Generate all necessary files
3. Configure everything
4. Create a working dev environment
5. You can start Phase 2 immediately

### **Option 2: Build It Yourself**
Follow the steps above manually, then we proceed to Phase 2 once you're ready.

### **Option 3: Modify Requirements First**
Tell me what changes you want, and I'll update the specifications before building.

---

## 📊 WHAT'S NEXT AFTER PHASE 1

Once Phase 1 is complete, you'll move to:

**Phase 2**: Authentication System (Sign Up, Sign In, Password Reset, Email Verification)  
**Phase 3**: User Profiles (Student & Tutor profiles, editing, picture uploads)  
**Phase 4**: Tutor Discovery & Search (Browse, search, filters, detail pages)  
**Phase 5**: Real-time Chat System (Conversations, messages, typing indicators)  
**Phase 6**: Booking System (Calendar, booking forms, confirmations)  
**Phase 7**: Reviews & Ratings (Submit reviews, display ratings, filtering)  
**Phase 8**: PWA Features (Service workers, offline support, push notifications)  

Each phase builds on the previous one, with a working dev environment by the end of Phase 1.

---

## 💡 IMPORTANT NOTES

- Make sure Node.js 16+ is installed
- Have a Firebase account ready with credentials
- .env.local will be in .gitignore (don't commit it)
- Dev server will hot-reload as you make changes
- All components use TypeScript for type safety
- Firebase credentials stay in environment variables

---

**Status**: ✅ Phase 1 Specification Complete  
**Ready to Build**: YES  
**Next Step**: Tell me to build Phase 1!

---

**Ready to begin? Say:**
> "Build Phase 1 now. Set up the complete project structure with all configurations."

