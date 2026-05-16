# 🎯 BookUp Web App - Specific Implementation Prompts

Use these detailed prompts to guide development of specific features. Simply copy and paste into our conversation.

---

## 📌 PHASE 1: PROJECT SETUP

### Prompt: Initial Project Setup & Configuration

```
I need you to create a new BookUp web application with the following specifications:

TECHNOLOGY STACK:
- Framework: React 18 with TypeScript
- Build Tool: Vite
- Styling: Tailwind CSS with Shadcn/ui components
- State Management: Redux Toolkit
- Routing: React Router v6
- Real-time Database: Firebase (Firestore + Realtime DB)
- Authentication: Firebase Authentication
- Package Manager: npm

PROJECT SETUP TASKS:
1. Create new Vite project with React + TypeScript template
2. Install and configure Tailwind CSS
3. Install and setup Shadcn/ui component library
4. Configure Firebase SDK and initialize connection
5. Setup Redux Toolkit with store and slices
6. Configure React Router with basic route structure
7. Create folder structure as outlined:
   - components/ (with Auth, Chat, Tutor, Booking, Common, Layout subdirs)
   - pages/ (with Auth, Dashboard, Chat, Tutor, Booking, Settings subdirs)
   - services/ (firebaseService, authService, chatService, tutorService, bookingService)
   - store/ (authSlice, chatSlice, tutorSlice, bookingSlice)
   - types/ (user, chat, tutor, booking TypeScript interfaces)
   - utils/ (validators, formatters, helpers)
   - styles/ (global styles)
8. Setup ESLint configuration for TypeScript
9. Configure Prettier for code formatting
10. Create .env.example file with required environment variables
11. Add README.md with project overview
12. Create package.json scripts for dev, build, preview, lint

DELIVERABLES:
- Fully functional development environment
- All configuration files (vite.config.ts, tsconfig.json, tailwind.config.js, .eslintrc)
- Folder structure ready for component development
- README with setup instructions

Make sure everything is properly configured and the dev server starts without errors.
```

---

## 📌 PHASE 2: AUTHENTICATION SYSTEM

### Prompt: Build Complete Authentication System

```
I need you to build the complete authentication system for BookUp web app.

REQUIREMENTS:
1. Create AUTH PAGES:
   - Sign Up page with:
     * Email input with validation
     * Password input with strength indicator
     * Confirm password field
     * Role selector (Student/Tutor)
     * Phone number input
     * Terms & conditions checkbox
     * Sign up button
     * Link to sign in page
   
   - Sign In page with:
     * Email input
     * Password input
     * Remember me checkbox
     * Sign in button
     * Forgot password link
     * Link to sign up page
   
   - Forgot Password page with:
     * Email input
     * Send reset email button
     * Confirmation message
   
   - Reset Password page with:
     * New password input with strength indicator
     * Confirm password field
     * Reset password button
   
   - Email Verification page with:
     * Verification code input
     * Auto-send verification code option
     * Resend code button

2. Create AUTH SERVICES:
   - authService.ts with functions:
     * signUp(email, password, role, phone)
     * signIn(email, password)
     * signOut()
     * sendPasswordReset(email)
     * verifyEmail(code)
     * resetPassword(newPassword)
     * getCurrentUser()
     * isAuthenticated()

3. Setup REDUX AUTH SLICE:
   - User state (email, uid, role, phone, name, profilePicture)
   - Auth status (loading, error, isAuthenticated)
   - Actions: setUser, clearUser, setLoading, setError, updateUserProfile
   - Selectors for getting auth state

4. Implement PROTECTED ROUTES:
   - Create ProtectedRoute wrapper component
   - Redirect unauthenticated users to login
   - Preserve intended destination after login

5. Add FIREBASE INTEGRATION:
   - Configure Firebase Authentication
   - Setup Google OAuth (optional)
   - Add email verification workflow
   - Implement password reset email

6. Create AUTH FORMS VALIDATION:
   - Email format validation
   - Password strength validation (min 8 chars, special chars, numbers)
   - Phone number format validation
   - Real-time form validation feedback

STYLING:
- Use Shadcn/ui Button, Input, Card components
- Responsive design (mobile-first)
- Error messages in red, success in green
- Loading states with spinners
- Smooth transitions and animations

DELIVERABLES:
- 5 complete authentication pages
- authService.ts with all required functions
- Redux auth slice with actions and selectors
- ProtectedRoute component
- Form validation utilities
- All integrated with Firebase
- Fully responsive and styled
```

---

## 📌 PHASE 3: USER PROFILES

### Prompt: Build Complete User Profile System

```
I need you to build the user profile system for both students and tutors.

STUDENT PROFILE REQUIREMENTS:
1. Student Profile View Page showing:
   - Profile header with: picture, name, rating, verification badge
   - About section with learning goals
   - Subjects interested in
   - Location & timezone
   - Email & phone (on own profile)
   - Booking history (list of past/upcoming sessions)
   - Reviews given to tutors
   - Average rating and review count
   - Edit profile button (only visible on own profile)

2. Student Profile Edit Page with:
   - Upload/change profile picture
   - Edit name
   - Edit email (with verification)
   - Edit phone number
   - Edit location
   - Edit timezone
   - Edit learning goals
   - Select subjects interested in
   - Save button with validation
   - Confirmation message on success

TUTOR PROFILE REQUIREMENTS:
1. Tutor Profile View Page showing:
   - Profile header with: picture, name, rating, verification badge
   - Bio/description
   - Subjects taught
   - Qualifications & certifications
   - Years of experience
   - Hourly rate / pricing tiers
   - Availability calendar (next 30 days)
   - Average rating and review count
   - Recent reviews from students (show 3-5)
   - Response time
   - Cancellation policy
   - Contact buttons: "Book Session", "Send Message"
   - Edit button (only on own profile)

2. Tutor Profile Edit Page with:
   - Upload/change profile picture
   - Edit name
   - Edit bio (textarea with character count)
   - Edit hourly rate
   - Select subjects (multiple)
   - Add/edit qualifications
   - Add certifications
   - Add years of experience
   - Set availability (weekly calendar)
   - Edit cancellation policy
   - Preview public profile button
   - Save button with validation

SERVICES:
- profileService.ts with functions:
  * fetchStudentProfile(userId)
  * fetchTutorProfile(userId)
  * updateStudentProfile(userId, data)
  * updateTutorProfile(userId, data)
  * uploadProfilePicture(userId, file)
  * getAvailability(tutorId)
  * setAvailability(tutorId, schedule)

COMPONENTS:
- ProfileHeader component (shows basic info & badge)
- ProfilePicture component (with upload capability)
- QualificationsDisplay component (for tutors)
- AvailabilityCalendar component (for tutors)
- ReviewsList component (shows recent reviews)
- ProfileEditForm component
- UploadPictureModal component

REDUX:
- User profile slice with:
  * User profile data state
  * Loading and error states
  * Actions: setProfile, updateProfile, setLoading, setError

STYLING:
- Responsive design (mobile-first)
- Use Shadcn/ui components (Card, Button, Input, Textarea, Badge, Avatar)
- Star rating display
- Verification badge with icon
- Smooth image upload with preview
- Calendar view for availability

DELIVERABLES:
- Student profile view and edit pages
- Tutor profile view and edit pages
- All components needed
- profileService.ts with all functions
- Redux slice for profile management
- Image upload functionality
- Fully responsive and styled
- Form validation on edit pages
```

---

## 📌 PHASE 4: TUTOR DISCOVERY & SEARCH

### Prompt: Build Tutor Discovery & Search System

```
I need you to build the complete tutor discovery and search system.

SEARCH & BROWSE PAGE REQUIREMENTS:
1. Search Bar Component:
   - Search by tutor name or subject
   - Real-time search (debounced)
   - Clear search button
   - Search history (optional)

2. Filter Sidebar (left side on desktop, collapsible on mobile):
   - Subject filter (multiple select)
   - Price range slider ($10-$200/hour)
   - Rating filter (1-5 stars)
   - Availability filter (today, this week, flexible)
   - Location filter (or radius)
   - Verification status (verified only toggle)
   - Experience level filter (beginner, intermediate, expert)

3. Sort Options:
   - Sort by rating (highest first)
   - Sort by price (lowest first)
   - Sort by newest profiles
   - Sort by availability
   - Sort by experience

4. Tutor List Display:
   - Grid layout on desktop (2-3 columns), 1 column on mobile
   - Each tutor card shows:
     * Profile picture (circular)
     * Name & verification badge
     * Subject specialties (tags)
     * Hourly rate
     * Star rating & review count
     * "View Profile" button
     * "Book Now" button
     * "Message" button
   - Pagination or infinite scroll
   - "No results" state with suggestions

TUTOR DETAIL PAGE:
1. Header Section:
   - Large profile picture
   - Name, title, verification badge
   - Star rating with review count
   - Hourly rate prominently displayed
   - "Book Now" and "Message" buttons

2. About Section:
   - Full bio/description
   - Years of experience
   - Qualifications & certifications
   - Languages spoken (optional)

3. Subjects Section:
   - List of all subjects taught
   - Experience level in each (if available)

4. Availability Section:
   - Calendar showing available slots
   - Next 30 days display
   - Color coding for available/booked
   - Click to select slot for booking

5. Reviews Section:
   - Display 5-10 recent reviews
   - Show reviewer name, rating, date, text
   - Overall rating statistics
   - Review count breakdown by stars

6. Cancellation Policy Section:
   - Display tutor's cancellation terms
   - Refund policy

SERVICES:
- tutorService.ts with functions:
  * fetchAllTutors(filters, sort, page)
  * fetchTutorById(tutorId)
  * searchTutors(query, filters)
  * filterTutors(filters)
  * sortTutors(sortOption)
  * getTutorReviews(tutorId)
  * getTutorAvailability(tutorId)

COMPONENTS:
- SearchBar component
- FilterSidebar component
- TutorCard component (for list view)
- TutorList component
- TutorDetailPage component
- AvailabilityCalendar component
- ReviewsList component
- RatingStars component

REDUX:
- Tutor slice with:
  * Tutors list state
  * Selected filters state
  * Sort option state
  * Current tutor detail state
  * Loading and error states
  * Actions for filtering, sorting, fetching

STYLING:
- Mobile-first responsive design
- Use Shadcn/ui (Card, Button, Badge, Slider, Select)
- Star ratings with icons
- Tag/badge styling for subjects
- Smooth filter transitions
- Loading states
- Error states

PERFORMANCE:
- Implement pagination or infinite scroll
- Lazy load images
- Debounce search input
- Cache tutor data when possible

DELIVERABLES:
- Complete search & browse page
- Tutor detail page
- All search/filter/sort functionality
- tutorService.ts with all functions
- Redux tutor slice
- Tutor card component
- Filter sidebar component
- Calendar component for availability
- Fully responsive design
- Pagination working correctly
```

---

## 📌 PHASE 5: CHAT SYSTEM

### Prompt: Build Real-time Chat System

```
I need you to build the complete real-time chat system for BookUp.

CHAT LIST PAGE:
1. Header:
   - Title "Messages"
   - "New Message" button

2. Search:
   - Search conversations by name
   - Real-time search

3. Conversation List:
   - Display all conversations
   - Each item shows:
     * User profile picture (circular)
     * User name
     * Last message preview
     * Timestamp of last message
     * Unread message count (badge)
     * Online/offline indicator
   - Sort by most recent first
   - Delete conversation option (with confirmation)
   - Responsive: full width on mobile, sidebar on desktop

4. Empty State:
   - "No messages yet" message
   - "Start a conversation" button link

CHAT DETAIL PAGE:
1. Header:
   - Back button (mobile)
   - User profile picture & name
   - Online/offline indicator
   - Info/details button (show user profile)
   - Options menu (block, report, etc.)

2. Message Display Area:
   - Messages sorted oldest to newest
   - Message bubbles with:
     * Sender name or avatar (on first message or if from different sender)
     * Message text or content
     * Timestamp (on hover)
     * Delivery status (sending, sent, delivered, read)
     * Read receipt if message is read
   - Typing indicator: "John is typing..."
   - Load more messages button (when scrolling up)
   - Loading state while fetching old messages

3. Message Input Area:
   - Text input field with placeholder "Type a message..."
   - Send button (icon button)
   - Attachment button (image, file)
   - Emoji picker (optional)
   - Typing indicator sent to other user

4. Image Messages:
   - Display images in message bubbles
   - Lightbox on click
   - Caption support

SERVICES:
- chatService.ts with functions:
  * fetchConversations(userId)
  * fetchMessages(conversationId, limit, startAfter)
  * sendMessage(conversationId, message)
  * markAsRead(conversationId, messageId)
  * deleteConversation(conversationId)
  * startNewConversation(userId1, userId2)
  * uploadMessageAttachment(file)
  * subscribeToConversations(userId, callback)
  * subscribeToMessages(conversationId, callback)
  * unsubscribeFromConversations()
  * unsubscribeFromMessages()

COMPONENTS:
- ChatList component
- ChatListItem component
- ChatDetail component
- MessageBubble component
- MessageInput component
- TypingIndicator component
- AttachmentPreview component
- NewConversationModal component

REDUX:
- Chat slice with:
  * Conversations list state
  * Current conversation state
  * Messages state
  * Selected conversation id state
  * New message input state
  * Typing indicator state
  * Loading and error states
  * Actions: setConversations, setCurrentConversation, addMessage, markAsRead, etc.

FIREBASE SETUP:
- Realtime listeners for instant message updates
- Message structure in Firestore:
  {
    conversationId,
    senderId,
    senderName,
    senderAvatar,
    text,
    timestamp,
    read: false,
    readAt: null,
    attachments: []
  }
- Conversation structure:
  {
    participants: [userId1, userId2],
    lastMessage,
    lastMessageTime,
    lastMessageSenderId,
    unreadCount: {userId1: 0, userId2: 3},
    createdAt
  }

STYLING:
- Mobile-first responsive design
- Different bubble colors for sent vs received
- Avatars for received messages
- Timestamp tooltips
- Loading skeletons
- Smooth animations
- Online status indicator (green dot)

FEATURES:
- Real-time message updates
- Unread message count
- Read receipts
- Typing indicators
- Message delivery status
- Auto-scroll to bottom on new messages
- Pagination for old messages
- Search conversations
- Delete conversations

DELIVERABLES:
- Chat list page with all conversations
- Chat detail page with real-time messaging
- All components (ChatList, ChatDetail, MessageBubble, MessageInput, etc.)
- chatService.ts with all Firestore functions
- Redux chat slice with proper state management
- Real-time listeners setup
- Message persistence in Firestore
- Fully responsive on all screen sizes
- Typing indicators working
- Read receipts working
- Message history loading correctly
```

---

## 📌 PHASE 6: BOOKING SYSTEM

### Prompt: Build Complete Booking System

```
I need you to build the complete booking system for BookUp.

BOOKING FLOW:
1. User clicks "Book Now" on tutor profile
2. Booking modal/page opens with:
   - Tutor name & picture
   - Select date from calendar
   - Select time from available slots
   - Select subject/topic
   - Select duration (30 min, 60 min, 90 min, etc.)
   - Notes/special requests (optional textarea)
   - Display total price
   - Confirm button

3. Payment page (if not already paid):
   - Show booking details
   - Show total price
   - Payment method selection (Stripe/PayPal)
   - Payment form
   - Process payment button
   - Confirmation page with booking details

4. Confirmation page:
   - Booking confirmed message
   - Display all booking details
   - Display tutor contact info
   - Display video call link (if applicable)
   - Add to calendar button
   - Send reminder button

BOOKING LIST PAGE:
1. Upcoming Sessions Tab:
   - List of upcoming bookings
   - Each item shows: tutor picture, date, time, subject, duration, status
   - "Start Session" button (5 min before)
   - "Reschedule" button
   - "Cancel" button
   - "Message Tutor" button

2. Past Sessions Tab:
   - List of completed bookings
   - Each item shows: tutor picture, date, time, subject, duration
   - "Leave Review" button (if not reviewed)
   - "View Review" button (if already reviewed)
   - "Rebook" button

3. Cancelled Sessions Tab:
   - List of cancelled bookings
   - Reason for cancellation
   - Refund status

SESSION DETAIL PAGE:
- Displays complete booking information
- Tutor details
- Date, time, duration
- Subject
- Video call link (if available)
- Notes
- Cancellation policy
- Cancel/reschedule options

RESCHEDULING FLOW:
1. User clicks "Reschedule"
2. New availability calendar appears
3. Select new date and time
4. Confirm reschedule
5. Update Firestore and notify tutor

CANCELLATION FLOW:
1. User clicks "Cancel"
2. Cancellation reason modal
3. Show refund policy
4. Confirm cancellation
5. Process refund
6. Send notification to tutor

SERVICES:
- bookingService.ts with functions:
  * createBooking(studentId, tutorId, date, time, duration, subject, notes)
  * fetchStudentBookings(studentId)
  * fetchTutorBookings(tutorId)
  * fetchBookingDetails(bookingId)
  * updateBooking(bookingId, updates)
  * cancelBooking(bookingId, reason)
  * rescheduleBooking(bookingId, newDate, newTime)
  * processRefund(bookingId)
  * getTutorAvailability(tutorId, date)
  * generateVideoCallLink(bookingId)
  * getBookingHistory(userId, limit)

COMPONENTS:
- BookingModal component
- BookingForm component
- DatePicker component
- TimePicker component
- BookingList component
- BookingListItem component
- SessionDetail component
- RescheduleModal component
- CancellationModal component
- ConfirmationPage component

REDUX:
- Booking slice with:
  * Bookings list state
  * Current booking state
  * Booking form data state
  * Loading and error states
  * Actions: createBooking, cancelBooking, rescheduleBooking, etc.

FIREBASE:
- Bookings collection structure:
  {
    bookingId,
    studentId,
    studentName,
    tutorId,
    tutorName,
    date,
    time,
    duration,
    subject,
    notes,
    price,
    status: 'confirmed', 'cancelled', 'completed', 'pending',
    videoCallLink,
    cancelledReason,
    refundStatus,
    createdAt,
    updatedAt
  }

STYLING:
- Mobile-first responsive design
- Calendar date picker
- Time slot selector
- Progress indicator for booking flow
- Loading states
- Error states
- Confirmation animations

FEATURES:
- Calendar view of availability
- Time slot selection
- Automated reminders (via Firestore Cloud Functions)
- Video call link generation
- Refund processing
- Booking history
- Cancel with reason tracking
- Reschedule functionality
- Add to calendar option

DELIVERABLES:
- Complete booking modal/page
- Booking list with upcoming/past/cancelled tabs
- Session detail page
- Reschedule functionality
- Cancellation functionality
- bookingService.ts with all functions
- Redux booking slice
- Calendar component
- All styling responsive
- Payment integration ready (Stripe/PayPal placeholder)
```

---

## 📌 PHASE 7: REVIEWS & RATINGS

### Prompt: Build Reviews & Ratings System

```
I need you to build the reviews and ratings system.

LEAVE REVIEW FLOW:
1. After session is completed, show "Leave Review" button
2. Review modal/page with:
   - Tutor picture & name
   - 5-star rating selector
   - Text area for review (min 10 chars, max 500)
   - Character count display
   - Submit button
   - Cancel button

3. Confirmation message

REVIEWS DISPLAY:
1. On Tutor Profile:
   - Overall rating: "4.8 / 5.0"
   - Total reviews count: "347 reviews"
   - Rating breakdown (e.g., 5-star: 300, 4-star: 40, etc.)
   - Recent reviews list showing:
     * Reviewer name & avatar
     * Rating (stars)
     * Review text
     * Date posted
     * Helpful count (thumbs up)

2. Reviews Page (separate page showing all reviews):
   - Same layout but shows all reviews
   - Filter by rating
   - Sort by newest, oldest, most helpful
   - Pagination

REVIEW MANAGEMENT:
- User can edit own review (within 30 days)
- User can delete own review (with confirmation)
- Admin can delete inappropriate reviews

SERVICES:
- reviewService.ts with functions:
  * submitReview(bookingId, tutorId, rating, text)
  * fetchTutorReviews(tutorId, limit, sortBy)
  * updateReview(reviewId, rating, text)
  * deleteReview(reviewId)
  * markReviewAsHelpful(reviewId, userId)
  * getTutorRating(tutorId)
  * getReviewCount(tutorId)

COMPONENTS:
- ReviewModal/Page component
- ReviewForm component
- ReviewList component
- ReviewItem component
- RatingStars component
- RatingBreakdown component
- AverageRating component

REDUX:
- Review slice with:
  * Reviews list state
  * Tutor rating state
  * New review form state
  * Loading and error states

FIREBASE:
- Reviews collection:
  {
    reviewId,
    tutorId,
    studentId,
    studentName,
    studentAvatar,
    bookingId,
    rating: 1-5,
    text,
    helpfulCount,
    createdAt,
    updatedAt,
    isEdited: false
  }

STYLING:
- Star rating selector with hover effect
- Star display with decimal precision
- Review card styling
- Character counter
- Helpful/not helpful buttons
- Modal/form styling

DELIVERABLES:
- Review submission modal/page
- Reviews display on tutor profile
- Separate reviews page with all reviews
- Review editing/deletion functionality
- reviewService.ts with all functions
- Redux review slice
- Star rating component
- Review list component
- Fully responsive design
```

---

## 📌 PHASE 8: PWA SETUP

### Prompt: Convert App to Progressive Web App

```
I need you to convert the BookUp web app into a full Progressive Web App (PWA).

PWA FEATURES TO IMPLEMENT:
1. Service Worker:
   - Create service-worker.ts
   - Implement Workbox for caching strategies
   - Cache API responses
   - Implement offline mode
   - Background sync (optional)

2. Web App Manifest:
   - Create public/manifest.json
   - Define app name (BookUp)
   - Short name (4-12 chars)
   - Icons (192x192, 512x512)
   - Theme color
   - Background color
   - Display mode (standalone, fullscreen, minimal-ui)
   - Start URL
   - Screenshots for app store

3. PWA Icons:
   - Create 192x192 icon (PNG)
   - Create 512x512 icon (PNG)
   - Create splash screens (optional)

4. HTTPS:
   - Ensure all endpoints are HTTPS
   - Update Firebase rules for production

5. App Install:
   - Add "Install App" button to navigation
   - Detect if app is installed
   - Offer install prompt to users
   - Handle before install prompt
   - Track installation

6. Offline Support:
   - Cache critical assets
   - Offline fallback page
   - Show offline indicator
   - Queue messages/actions for when online
   - Sync data when connection restored

7. Push Notifications:
   - Request notification permission
   - Setup Firebase Cloud Messaging
   - Send notifications to users
   - Handle notification clicks
   - Notification settings page

TASKS:
1. Setup Workbox configuration
2. Create service worker with:
   - Asset caching strategy
   - API response caching
   - Offline fallback
   - Cache versioning
3. Create manifest.json with all required fields
4. Generate app icons (192x192, 512x512)
5. Create offline.html fallback page
6. Implement install button in header
7. Setup Firebase Cloud Messaging
8. Create push notification handler
9. Add notification permission request
10. Create notification settings page
11. Implement offline indicator
12. Test PWA on mobile (Add to home screen)

COMPONENTS:
- InstallPrompt component (header button)
- OfflineIndicator component
- NotificationSettings page
- PushNotificationHandler (logic)

DELIVERABLES:
- Complete service worker
- manifest.json configured
- PWA icons (192x192, 512x512)
- Offline support working
- Install to home screen working
- Push notifications working
- Offline indicator showing
- Notification settings page
- All tested on mobile devices
```

---

## 💡 HOW TO USE THESE PROMPTS

1. **Copy** the entire prompt you want to use
2. **Paste** it into our conversation
3. **I will** implement the complete feature as specified
4. **Review** the implementation
5. **Request** any modifications or improvements
6. **Move to** the next feature

---

## 🎯 SUGGESTED IMPLEMENTATION ORDER

1. **Phase 1**: Project Setup (1-2 hours)
2. **Phase 2**: Authentication (3-4 hours)
3. **Phase 3**: User Profiles (3-4 hours)
4. **Phase 4**: Tutor Discovery (4-5 hours)
5. **Phase 5**: Chat System (4-5 hours)
6. **Phase 6**: Booking System (5-6 hours)
7. **Phase 7**: Reviews & Ratings (2-3 hours)
8. **Phase 8**: PWA Setup (2-3 hours)
9. **Testing & Deployment** (3-4 hours)

**Total Estimated Time**: 25-35 hours of development

---

## 📝 NOTES

- Each prompt is self-contained and detailed
- You can modify any prompt to fit your specific needs
- Some features can be built in parallel (e.g., auth while designing UI)
- Always review code before moving to next phase
- Ask for clarifications if anything is unclear

**Ready to build the BookUp web app? Start with Phase 1!** 🚀
