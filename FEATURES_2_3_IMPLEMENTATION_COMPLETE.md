# Complete Implementation: Features 2, 3 + Bio + Profile Viewing

**Date**: December 18, 2025  
**Scope**: 
- Feature 2: Book Tutoring Session
- Feature 3: Leave Reviews & Ratings
- New: User Bio (edit + display)
- New: Click tutor name in chat to view profile

**Estimated Time**: 4-5 hours total  
**Build Status**: Will verify after each component

---

## Summary of Tasks

| Task | File(s) | Est. Time | Priority |
|------|---------|-----------|----------|
| Update User model (add bio) | User.java | 15 min | HIGH |
| Update ProfileEditActivity | ProfileEditActivity.java | 45 min | HIGH |
| Create Booking model | Booking.java | 20 min | HIGH |
| Create BookingSessionActivity | BookingSessionActivity.java | 60 min | HIGH |
| Create ReviewsBottomSheetFragment | ReviewsBottomSheetFragment.java | 60 min | HIGH |
| Integrate booking with TutorDetailsActivity | TutorDetailsActivity.java | 30 min | MEDIUM |
| Integrate reviews with ChatActivity | ChatActivity.java | 30 min | MEDIUM |
| Make tutor name clickable in chat | ChatActivity.java | 45 min | MEDIUM |
| Deploy & Test | All | 60 min | HIGH |

**Total**: 4.5-5 hours

---

## Part 1: Add User Bio Field (45 minutes)

### Step 1.1: Update User Model

**File**: `app/src/main/java/com/example/bookup/models/User.java`

Replace entire file with:

```java
package com.example.bookup.models;

public class User {
    private String id;
    private String displayName;
    private String email;
    private String photoUrl;
    private String bio;
    private boolean isAdmin;
    private boolean blocked;
    private String fcmToken;

    // Required empty constructor for Firestore
    public User() {}

    public User(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
        this.bio = "";
        this.isAdmin = false;
        this.blocked = false;
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
    public String getBio() { return bio; }
    public boolean isAdmin() { return isAdmin; }
    public boolean isBlocked() { return blocked; }
    public String getFcmToken() { return fcmToken; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setBio(String bio) { this.bio = bio; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
}
```

### Step 1.2: Update ProfileEditActivity

**File**: `app/src/main/java/com/example/bookup/activities/ProfileEditActivity.java`

Find the section where you have `displayName` EditText and add bio field after it:

**BEFORE** (existing code - line ~50-80):
```java
private EditText displayNameEditText;
private EditText emailEditText;
private Button saveButton;
```

**ADD** after `displayNameEditText`:
```java
private EditText displayNameEditText;
private EditText bioEditText;  // ← NEW
private EditText emailEditText;
private Button saveButton;
```

**Find onCreate()** (around line 120-150) and add:
```java
displayNameEditText = findViewById(R.id.edit_display_name);
bioEditText = findViewById(R.id.edit_bio);  // ← NEW
emailEditText = findViewById(R.id.edit_email);
```

**Find the method that loads user data** (around line 200-250) and add:
```java
displayNameEditText.setText(user.getDisplayName());
bioEditText.setText(user.getBio() != null ? user.getBio() : "");  // ← NEW
emailEditText.setText(user.getEmail());
```

**Find the save button click handler** (around line 300-350) and update:
```java
saveButton.setOnClickListener(v -> {
    String displayName = displayNameEditText.getText().toString().trim();
    String bio = bioEditText.getText().toString().trim();  // ← NEW
    String email = emailEditText.getText().toString().trim();
    
    if (displayName.isEmpty()) {
        Toast.makeText(this, "Display name cannot be empty", Toast.LENGTH_SHORT).show();
        return;
    }
    
    if (bio.length() > 500) {  // ← NEW
        Toast.makeText(this, "Bio must be less than 500 characters", Toast.LENGTH_SHORT).show();
        return;
    }
    
    user.setDisplayName(displayName);
    user.setBio(bio);  // ← NEW
    user.setEmail(email);
    
    // Save to Firestore
    db.collection("users").document(user.getId()).set(user)
        .addOnSuccessListener(aVoid -> {
            Toast.makeText(ProfileEditActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        })
        .addOnFailureListener(e -> {
            Toast.makeText(ProfileEditActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
});
```

### Step 1.3: Update Layout File

**File**: `app/src/main/res/layout/activity_profile_edit.xml`

Find the `displayName` EditText and add a bio field after it. Add this XML:

```xml
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/bio_input_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    android:hint="Bio">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/edit_bio"
        android:layout_width="match_parent"
        android:layout_height="120dp"
        android:gravity="top"
        android:inputType="textMultiLine"
        android:lines="4" />

</com.google.android.material.textfield.TextInputLayout>
```

### Step 1.4: Display Bio in ProfileFragment

**File**: `app/src/main/java/com/example/bookup/fragments/ProfileFragment.java`

Find where user data is displayed and add:

```java
TextView bioTextView = view.findViewById(R.id.profile_bio);  // ← ADD
if (user.getBio() != null && !user.getBio().isEmpty()) {
    bioTextView.setText(user.getBio());
    bioTextView.setVisibility(View.VISIBLE);
} else {
    bioTextView.setText("No bio added yet");
    bioTextView.setVisibility(View.VISIBLE);
}
```

Add to `activity_profile.xml` layout:

```xml
<TextView
    android:id="@+id/profile_bio"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    android:textSize="14sp"
    android:textColor="@android:color/darker_gray" />
```

---

## Part 2: Feature 2 - Book Tutoring Session (2 hours)

### Step 2.1: Create Booking Model

**File**: `app/src/main/java/com/example/bookup/models/Booking.java`

```java
package com.example.bookup.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Date;

public class Booking implements Serializable {
    private String id;
    private String tutorId;
    private String studentId;
    private String tutorName;
    private String studentName;
    private Date sessionDate;
    private String subject;
    private String description;
    private String status; // "pending", "confirmed", "completed", "cancelled"
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Booking() {}

    public Booking(String tutorId, String studentId, String tutorName, String studentName) {
        this.tutorId = tutorId;
        this.studentId = studentId;
        this.tutorName = tutorName;
        this.studentName = studentName;
        this.status = "pending";
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
```

### Step 2.2: Create BookingSessionActivity

**File**: `app/src/main/java/com/example/bookup/activities/BookingSessionActivity.java`

```java
package com.example.bookup.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookup.R;
import com.example.bookup.models.Booking;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BookingSessionActivity extends AppCompatActivity {

    private String tutorId;
    private String tutorName;
    private Date selectedDate;
    private EditText subjectEditText;
    private EditText descriptionEditText;
    private TextView dateTimeTextView;
    private Button bookButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_session);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get tutor details from intent
        tutorId = getIntent().getStringExtra("tutorId");
        tutorName = getIntent().getStringExtra("tutorName");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_booking);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Book Session with " + tutorName);
        }

        // Initialize views
        subjectEditText = findViewById(R.id.edit_subject);
        descriptionEditText = findViewById(R.id.edit_description);
        dateTimeTextView = findViewById(R.id.text_date_time);
        bookButton = findViewById(R.id.btn_book);

        // Setup date/time picker
        dateTimeTextView.setOnClickListener(v -> showDateTimePicker());

        // Setup book button
        bookButton.setOnClickListener(v -> bookSession());
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    showTimePicker(calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker(Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    selectedDate = calendar.getTime();
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        if (selectedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            dateTimeTextView.setText(sdf.format(selectedDate));
        }
    }

    private void bookSession() {
        String subject = subjectEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        // Validation
        if (subject.isEmpty()) {
            Toast.makeText(this, "Please enter subject", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == null) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create booking
        Booking booking = new Booking(
                tutorId,
                auth.getCurrentUser().getUid(),
                tutorName,
                auth.getCurrentUser().getDisplayName() != null ? 
                    auth.getCurrentUser().getDisplayName() : "Student"
        );
        booking.setSubject(subject);
        booking.setDescription(description);
        booking.setSessionDate(selectedDate);
        booking.setStatus("pending");
        booking.setCreatedAt(Timestamp.now());
        booking.setUpdatedAt(Timestamp.now());

        // Save to Firestore
        db.collection("bookings").add(booking)
                .addOnSuccessListener(documentReference -> {
                    booking.setId(documentReference.getId());
                    db.collection("bookings").document(documentReference.getId()).set(booking)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(BookingSessionActivity.this, 
                                    "Session booked successfully! Awaiting tutor confirmation.", 
                                    Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(BookingSessionActivity.this, 
                                    "Failed to save booking: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingSessionActivity.this, 
                        "Failed to create booking: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
```

### Step 2.3: Create Booking Layout

**File**: `app/src/main/res/layout/activity_booking_session.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@android:color/white">

    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar_booking"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="@color/primary"
        app:titleTextColor="@android:color/white" />

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                android:hint="Subject">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/edit_subject"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:inputType="text" />

            </com.google.android.material.textfield.TextInputLayout>

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Date & Time"
                android:textStyle="bold"
                android:textSize="14sp"
                android:layout_marginBottom="8dp" />

            <TextView
                android:id="@+id/text_date_time"
                android:layout_width="match_parent"
                android:layout_height="48dp"
                android:gravity="center_vertical"
                android:paddingStart="12dp"
                android:paddingEnd="12dp"
                android:text="Tap to select date & time"
                android:background="@drawable/border_box"
                android:layout_marginBottom="16dp"
                android:textColor="@android:color/darker_gray" />

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                android:hint="Description (Optional)">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/edit_description"
                    android:layout_width="match_parent"
                    android:layout_height="120dp"
                    android:gravity="top"
                    android:inputType="textMultiLine"
                    android:lines="4" />

            </com.google.android.material.textfield.TextInputLayout>

        </LinearLayout>

    </ScrollView>

    <Button
        android:id="@+id/btn_book"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:layout_margin="16dp"
        android:text="Book Session"
        android:background="@color/primary"
        android:textColor="@android:color/white"
        android:textStyle="bold" />

</LinearLayout>
```

### Step 2.4: Add Book Button to TutorDetailsActivity

**File**: `app/src/main/java/com/example/bookup/activities/TutorDetailsActivity.java`

Find the section with message button (around line 150-180) and add book button:

```java
Button messageButton = findViewById(R.id.btn_message);
Button bookButton = findViewById(R.id.btn_book_session);  // ← ADD THIS

messageButton.setOnClickListener(v -> startChat());

bookButton.setOnClickListener(v -> {  // ← ADD THIS
    Intent intent = new Intent(TutorDetailsActivity.this, BookingSessionActivity.class);
    intent.putExtra("tutorId", currentTutor.getUid());
    intent.putExtra("tutorName", currentTutor.getDisplayName());
    startActivity(intent);
});
```

### Step 2.5: Add Book Button to TutorDetailsActivity Layout

**File**: `app/src/main/res/layout/activity_tutor_details.xml`

Find the message button and add book session button next to it:

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="16dp"
    android:gravity="center">

    <Button
        android:id="@+id/btn_message"
        android:layout_width="0dp"
        android:layout_height="48dp"
        android:layout_weight="1"
        android:layout_marginEnd="8dp"
        android:text="Message"
        android:background="@color/primary"
        android:textColor="@android:color/white" />

    <Button
        android:id="@+id/btn_book_session"
        android:layout_width="0dp"
        android:layout_height="48dp"
        android:layout_weight="1"
        android:layout_marginStart="8dp"
        android:text="Book Session"
        android:background="@color/primary"
        android:textColor="@android:color/white" />

</LinearLayout>
```

### Step 2.6: Add Booking to Firestore Rules

**File**: `firebase.rules` or `firestore.rules`

Add these rules in the service firebase.firestore section:

```
match /bookings/{document=**} {
  allow read: if request.auth.uid in get(/databases/$(database)/documents/bookings/$(document)).data.participantIds;
  allow create: if request.auth != null && request.resource.data.studentId == request.auth.uid;
  allow update: if request.auth.uid == resource.data.tutorId || request.auth.uid == resource.data.studentId;
}
```

---

## Part 3: Feature 3 - Reviews & Ratings (2 hours)

### Step 3.1: Create Review Model

**File**: `app/src/main/java/com/example/bookup/models/Review.java`

```java
package com.example.bookup.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Review implements Serializable {
    private String id;
    private String tutorId;
    private String studentId;
    private String studentName;
    private String studentPhotoUrl;
    private float rating;
    private String comment;
    private String bookingId;
    private Timestamp createdAt;

    public Review() {}

    public Review(String tutorId, String studentId, String studentName, String studentPhotoUrl) {
        this.tutorId = tutorId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentPhotoUrl = studentPhotoUrl;
        this.createdAt = Timestamp.now();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentPhotoUrl() { return studentPhotoUrl; }
    public void setStudentPhotoUrl(String studentPhotoUrl) { this.studentPhotoUrl = studentPhotoUrl; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
```

### Step 3.2: Create ReviewsBottomSheetFragment

**File**: `app/src/main/java/com/example/bookup/fragments/ReviewsBottomSheetFragment.java`

```java
package com.example.bookup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bookup.R;
import com.example.bookup.models.Review;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReviewsBottomSheetFragment extends BottomSheetDialogFragment {

    private String tutorId;
    private String tutorName;
    private RatingBar ratingBar;
    private EditText commentEditText;
    private Button submitButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public static ReviewsBottomSheetFragment newInstance(String tutorId, String tutorName) {
        ReviewsBottomSheetFragment fragment = new ReviewsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("tutorId", tutorId);
        args.putString("tutorName", tutorName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tutorId = getArguments().getString("tutorId");
            tutorName = getArguments().getString("tutorName");
        }
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ratingBar = view.findViewById(R.id.rating_bar);
        commentEditText = view.findViewById(R.id.edit_comment);
        submitButton = view.findViewById(R.id.btn_submit_review);

        submitButton.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String comment = commentEditText.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(getContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a review comment", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        Review review = new Review(
                tutorId,
                currentUser.getUid(),
                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous",
                currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : ""
        );
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(Timestamp.now());

        // Save review
        db.collection("tutorReviews").add(review)
                .addOnSuccessListener(documentReference -> {
                    review.setId(documentReference.getId());
                    updateTutorRating();
                    Toast.makeText(getContext(), "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to submit review: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    private void updateTutorRating() {
        // Recalculate average rating and update tutor document
        db.collection("tutorReviews")
                .whereEqualTo("tutorId", tutorId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        float totalRating = 0;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            Review review = doc.toObject(Review.class);
                            if (review != null) {
                                totalRating += review.getRating();
                            }
                        }
                        float averageRating = totalRating / queryDocumentSnapshots.size();

                        db.collection("users").document(tutorId)
                                .update("rating", averageRating, "reviewCount", queryDocumentSnapshots.size())
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to update rating", 
                                        Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }
}
```

### Step 3.3: Create Reviews Bottom Sheet Layout

**File**: `app/src/main/res/layout/fragment_reviews_bottom_sheet.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@android:color/white">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Rate Your Experience"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp" />

    <RatingBar
        android:id="@+id/rating_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:numStars="5"
        android:rating="0"
        android:stepSize="1"
        android:layout_gravity="center_horizontal"
        android:layout_marginBottom="16dp"
        android:scaleX="1.5"
        android:scaleY="1.5" />

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp"
        android:hint="Write your review (optional)">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/edit_comment"
            android:layout_width="match_parent"
            android:layout_height="100dp"
            android:gravity="top"
            android:inputType="textMultiLine"
            android:lines="4" />

    </com.google.android.material.textfield.TextInputLayout>

    <Button
        android:id="@+id/btn_submit_review"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:text="Submit Review"
        android:background="@color/primary"
        android:textColor="@android:color/white"
        android:textStyle="bold" />

</LinearLayout>
```

### Step 3.4: Add Review Button to ChatActivity

**File**: `app/src/main/java/com/example/bookup/activities/ChatActivity.java`

Find the toolbar setup section (around line 385) and add a review option:

**Add to menu_chat.xml or update onCreateOptionsMenu():**

```java
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_chat, menu);
    MenuItem reviewItem = menu.add("Leave Review");  // ← ADD
    reviewItem.setOnMenuItemClickListener(item -> {
        showReviewDialog();
        return true;
    });
    return true;
}

private void showReviewDialog() {
    if (otherUserId != null && otherUserName != null) {
        ReviewsBottomSheetFragment reviewFragment = ReviewsBottomSheetFragment.newInstance(otherUserId, otherUserName);
        reviewFragment.show(getSupportFragmentManager(), "reviews");
    } else {
        Toast.makeText(this, "Cannot submit review for group chats", Toast.LENGTH_SHORT).show();
    }
}
```

### Step 3.5: Add Reviews to Firestore Rules

Add to `firebase.rules` or `firestore.rules`:

```
match /tutorReviews/{document=**} {
  allow read: if request.auth != null;
  allow create: if request.auth != null && request.resource.data.studentId == request.auth.uid;
  allow delete: if request.auth.uid == resource.data.studentId;
}
```

---

## Part 4: View Tutor Profile from Chat (1 hour)

### Step 4.1: Make Tutor Name Clickable in ChatActivity

**File**: `app/src/main/java/com/example/bookup/activities/ChatActivity.java`

Find `setupToolbar()` method (around line 384) and update it:

```java
private void setupToolbar() {
    Toolbar toolbar = findViewById(R.id.toolbar_chat);
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(otherUserName != null ? otherUserName : getString(R.string.app_name));
    }

    // Make title clickable to view profile  ← ADD BELOW
    toolbar.setOnClickListener(v -> {
        if (otherUserId != null && !isGroupChat) {
            openTutorProfile();
        }
    });
}

private void openTutorProfile() {
    Intent intent = new Intent(ChatActivity.this, TutorDetailsActivity.class);
    intent.putExtra("tutorId", otherUserId);
    startActivity(intent);
}
```

### Step 4.2: Update TutorDetailsActivity to Handle Direct Open

**File**: `app/src/main/java/com/example/bookup/activities/TutorDetailsActivity.java`

Find onCreate() method and update to handle tutorId from intent:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tutor_details);

    auth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    currentUser = auth.getCurrentUser();

    // Get tutor ID from intent or use default
    String tutorIdFromIntent = getIntent().getStringExtra("tutorId");  // ← ADD THIS
    
    if (tutorIdFromIntent != null) {
        // Loading tutor by ID from chat profile click
        loadTutorById(tutorIdFromIntent);
    } else {
        // Original logic - load from arguments or current user
        loadUserTutorInfo();
    }
}

private void loadTutorById(String tutorId) {  // ← ADD THIS METHOD
    db.collection("users").document(tutorId)
        .get()
        .addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentTutor = documentSnapshot.toObject(Tutor.class);
                if (currentTutor == null) {
                    currentTutor = new Tutor();
                }
                currentTutor.setUid(documentSnapshot.getId());
                displayTutorInfo();
            }
        })
        .addOnFailureListener(e -> {
            Toast.makeText(TutorDetailsActivity.this, "Failed to load tutor: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        });
}
```

---

## Build & Test Checklist

**Before Building:**
- [ ] All Java files created/updated
- [ ] All XML layout files created
- [ ] All imports added correctly
- [ ] No syntax errors

**Build Commands:**
```bash
cd /Users/user/AndroidStudioProjects/BookUp
./gradlew clean build 2>&1 | tail -50
```

**Expected Output:**
```
BUILD SUCCESSFUL in 4-5m
92 tasks executed
0 failures
```

**Test Cases:**

**Feature 2 (Booking)**:
1. Open TutorDetailsActivity
2. Click "Book Session" button
3. Select subject
4. Tap "Date & Time" → select date + time
5. Add optional description
6. Click "Book" → should show success toast
7. Verify booking saved in Firestore console

**Feature 3 (Reviews)**:
1. Open chat with tutor
2. Click menu → "Leave Review"
3. Select rating (1-5 stars)
4. Enter review comment
5. Click "Submit Review" → success toast
6. Verify review in Firestore console
7. Verify tutor rating updated

**User Bio**:
1. Open ProfileEditActivity
2. Update bio field (≤500 chars)
3. Click save
4. Verify bio displays in ProfileFragment

**View Tutor Profile**:
1. Open ChatActivity
2. Click tutor name in toolbar
3. Should navigate to TutorDetailsActivity
4. Verify tutor bio displays
5. Can book session or leave review from here

---

## Firestore Collections Schema

### /bookings/{bookingId}
```
{
  "id": "string",
  "tutorId": "string",
  "studentId": "string",
  "tutorName": "string",
  "studentName": "string",
  "sessionDate": "Timestamp",
  "subject": "string",
  "description": "string",
  "status": "pending|confirmed|completed|cancelled",
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

### /tutorReviews/{reviewId}
```
{
  "id": "string",
  "tutorId": "string",
  "studentId": "string",
  "studentName": "string",
  "studentPhotoUrl": "string",
  "rating": "float",
  "comment": "string",
  "bookingId": "string (optional)",
  "createdAt": "Timestamp"
}
```

### /users/{userId} (Updated)
```
{
  ...existing fields...,
  "bio": "string",
  "rating": "float (auto-calculated)",
  "reviewCount": "int (auto-calculated)"
}
```

---

## Next Steps

1. **Implement** all steps above (~4-5 hours)
2. **Build** and verify no errors
3. **Test** all features locally
4. **Deploy Storage Rules** (if not done): `firebase deploy --project book-up-ishola`
5. **Test on device** (audio + features)
6. **Continue Day 2-5 testing** per schedule

---

**Status**: 🟢 READY FOR IMPLEMENTATION  
**Estimated Completion**: 4-5 hours  
**Target**: All features + bio working by end of session  
