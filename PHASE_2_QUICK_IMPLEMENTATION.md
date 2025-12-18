# Quick Implementation Guide: Features 2 & 3

This guide provides step-by-step instructions for implementing:
1. **Booking Sessions** (Feature 2)
2. **Reviews & Ratings** (Feature 3)

**Est. Time**: 3-4 hours total  
**Difficulty**: Medium  
**Prerequisites**: Completed Feature 1 ✅

---

## Feature 2: Booking Sessions (1.5-2 hours)

### Step 1: Create Booking Model

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
    private Date sessionDate;
    private String subject;
    private String description;
    private String status; // "pending", "confirmed", "completed", "cancelled"
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Booking() {}

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

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

### Step 2: Create BookingSessionActivity

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

import java.util.Calendar;
import java.util.Date;

public class BookingSessionActivity extends AppCompatActivity {

    private String tutorId;
    private String tutorName;
    private Date selectedDate;
    private EditText editSubject, editDescription;
    private TextView textDate;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_session);

        // Get intent extras
        tutorId = getIntent().getStringExtra("tutorId");
        tutorName = getIntent().getStringExtra("tutorName");

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Book Session with " + tutorName);
        }

        // Initialize views
        editSubject = findViewById(R.id.edit_subject);
        editDescription = findViewById(R.id.edit_description);
        textDate = findViewById(R.id.text_date);
        Button btnPickDate = findViewById(R.id.btn_pick_date);
        Button btnBookSession = findViewById(R.id.btn_book_session);

        // Date picker
        btnPickDate.setOnClickListener(v -> showDateTimePicker());

        // Book button
        btnBookSession.setOnClickListener(v -> bookSession());
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    showTimePicker(calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void showTimePicker(Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    selectedDate = calendar.getTime();
                    textDate.setText(formatDate(selectedDate));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);

        timePickerDialog.show();
    }

    private String formatDate(Date date) {
        return new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", 
            java.util.Locale.getDefault()).format(date);
    }

    private void bookSession() {
        String subject = editSubject.getText().toString().trim();
        String description = editDescription.getText().toString().trim();

        // Validate
        if (subject.isEmpty()) {
            Toast.makeText(this, "Please enter subject", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDate == null) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create booking
        Booking booking = new Booking();
        booking.setTutorId(tutorId);
        booking.setStudentId(mAuth.getCurrentUser().getUid());
        booking.setSessionDate(selectedDate);
        booking.setSubject(subject);
        booking.setDescription(description);
        booking.setStatus("pending");
        booking.setCreatedAt(Timestamp.now());
        booking.setUpdatedAt(Timestamp.now());

        // Save to Firestore
        db.collection("bookings").add(booking)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(BookingSessionActivity.this, 
                        "Booking request sent!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookingSessionActivity.this, 
                        "Failed to book session: " + e.getMessage(), 
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

### Step 3: Create Booking Layout

**File**: `app/src/main/res/layout/activity_booking_session.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/white">

    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        android:elevation="4dp" />

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Subject"
                android:textStyle="bold"
                android:textSize="14sp"
                android:layout_marginBottom="8dp" />

            <EditText
                android:id="@+id/edit_subject"
                android:layout_width="match_parent"
                android:layout_height="48dp"
                android:hint="e.g., Mathematics"
                android:paddingHorizontal="12dp"
                android:background="@drawable/bg_edit_text"
                android:layout_marginBottom="16dp" />

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Session Date & Time"
                android:textStyle="bold"
                android:textSize="14sp"
                android:layout_marginBottom="8dp" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="48dp"
                android:orientation="horizontal"
                android:layout_marginBottom="16dp">

                <TextView
                    android:id="@+id/text_date"
                    android:layout_width="0dp"
                    android:layout_height="match_parent"
                    android:layout_weight="1"
                    android:text="Tap to select date"
                    android:gravity="center_vertical"
                    android:paddingHorizontal="12dp"
                    android:background="@drawable/bg_edit_text" />

                <Button
                    android:id="@+id/btn_pick_date"
                    android:layout_width="48dp"
                    android:layout_height="48dp"
                    android:text="📅"
                    android:layout_marginStart="8dp" />

            </LinearLayout>

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Description (Optional)"
                android:textStyle="bold"
                android:textSize="14sp"
                android:layout_marginBottom="8dp" />

            <EditText
                android:id="@+id/edit_description"
                android:layout_width="match_parent"
                android:layout_height="100dp"
                android:hint="Add notes or topics to cover..."
                android:padding="12dp"
                android:background="@drawable/bg_edit_text"
                android:gravity="top"
                android:inputType="textMultiLine"
                android:layout_marginBottom="24dp" />

            <Button
                android:id="@+id/btn_book_session"
                android:layout_width="match_parent"
                android:layout_height="48dp"
                android:text="Book Session"
                android:textSize="16sp"
                android:textStyle="bold" />

        </LinearLayout>

    </ScrollView>

</LinearLayout>
```

### Step 4: Update TutorDetailsActivity

Add this to the TutorDetailsActivity where you have the message button:

```java
Button btnBookSession = findViewById(R.id.btn_book_session); // Add to layout
btnBookSession.setOnClickListener(v -> {
    Intent intent = new Intent(TutorDetailsActivity.this, BookingSessionActivity.class);
    intent.putExtra("tutorId", tutor.getUid());
    intent.putExtra("tutorName", tutor.getDisplayName());
    startActivity(intent);
});
```

### Step 5: Add to AndroidManifest.xml

```xml
<activity
    android:name=".activities.BookingSessionActivity"
    android:exported="true"
    android:parentActivityName=".activities.TutorDetailsActivity" />
```

---

## Feature 3: Reviews & Ratings (1.5-2 hours)

### Step 1: Create Review Model

**File**: `app/src/main/java/com/example/bookup/models/Review.java`

```java
package com.example.bookup.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Review implements Serializable {
    private String id;
    private String tutorId;
    private String studentId;
    private int rating;
    private String reviewText;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Review() {}

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
```

### Step 2: Create ReviewsBottomSheetFragment

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
import com.google.firebase.firestore.FirebaseFirestore;

public class ReviewsBottomSheetFragment extends BottomSheetDialogFragment {

    private String tutorId;
    private RatingBar ratingBar;
    private EditText editReview;
    private Button btnSubmit;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public static ReviewsBottomSheetFragment newInstance(String tutorId) {
        ReviewsBottomSheetFragment fragment = new ReviewsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("tutorId", tutorId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tutorId = getArguments().getString("tutorId");
        }
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ratingBar = view.findViewById(R.id.rating_bar);
        editReview = view.findViewById(R.id.edit_review);
        btnSubmit = view.findViewById(R.id.btn_submit_review);

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        int rating = (int) ratingBar.getRating();
        String reviewText = editReview.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(getContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reviewText.isEmpty()) {
            Toast.makeText(getContext(), "Please write a review", Toast.LENGTH_SHORT).show();
            return;
        }

        Review review = new Review();
        review.setTutorId(tutorId);
        review.setStudentId(mAuth.getCurrentUser().getUid());
        review.setRating(rating);
        review.setReviewText(reviewText);
        review.setCreatedAt(Timestamp.now());
        review.setUpdatedAt(Timestamp.now());

        db.collection("tutorReviews").add(review)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(getContext(), "Review submitted!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
```

### Step 3: Create Review Layout

**File**: `app/src/main/res/layout/fragment_review_bottom_sheet.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Leave a Review"
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
        android:layout_marginBottom="16dp" />

    <EditText
        android:id="@+id/edit_review"
        android:layout_width="match_parent"
        android:layout_height="120dp"
        android:hint="Share your experience..."
        android:padding="12dp"
        android:background="@drawable/bg_edit_text"
        android:gravity="top"
        android:inputType="textMultiLine"
        android:layout_marginBottom="16dp" />

    <Button
        android:id="@+id/btn_submit_review"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:text="Submit Review"
        android:textStyle="bold" />

</LinearLayout>
```

### Step 4: Update ChatActivity to Show Review Button

Add this after a chat ends or in your ChatActivity:

```java
// After sending last message or in menu
Button btnLeaveReview = findViewById(R.id.btn_leave_review);
btnLeaveReview.setOnClickListener(v -> {
    ReviewsBottomSheetFragment reviewFragment = 
        ReviewsBottomSheetFragment.newInstance(otherUserId);
    reviewFragment.show(getSupportFragmentManager(), "review_sheet");
});
```

### Step 5: Add to AndroidManifest.xml

```xml
<!-- No activity needed for fragment, just the bottom sheet -->
```

---

## Testing Checklist

### Feature 2: Booking Sessions
- [ ] Date/time picker opens
- [ ] Can select future date
- [ ] Subject field required
- [ ] Booking saves to Firestore
- [ ] Confirmation toast appears
- [ ] Activity closes on success
- [ ] Booking appears in user's bookings list

### Feature 3: Reviews & Ratings
- [ ] Bottom sheet opens from chat
- [ ] Can select 1-5 stars
- [ ] Can type review
- [ ] Review field required
- [ ] Rating required
- [ ] Review saves to Firestore
- [ ] Tutor average rating updates
- [ ] Multiple reviews don't duplicate

---

## Firestore Rules to Add

```javascript
// Add to your firebase.rules file

// Bookings collection
match /bookings/{bookingId} {
  allow create: if request.auth != null;
  allow read: if request.auth != null && 
    (request.auth.uid == resource.data.studentId || 
     request.auth.uid == resource.data.tutorId ||
     get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true);
  allow update, delete: if request.auth.uid == resource.data.studentId;
}

// Reviews collection
match /tutorReviews/{reviewId} {
  allow create: if request.auth != null;
  allow read: if request.auth != null;
  allow delete: if request.auth.uid == resource.data.studentId;
}
```

---

## Build & Deploy

```bash
# 1. Build
./gradlew clean build

# 2. If successful, install
./gradlew installDebug

# 3. Deploy Firestore & Storage rules
firebase deploy --project book-up-ishola
```

---

## Support

If you encounter any issues:

1. Check Firestore console for write errors
2. Verify user IDs match
3. Check LogCat for exception traces
4. Verify FirebaseAuth is initialized

---

**Estimated Total Time**: 3-4 hours  
**Difficulty**: Medium-Hard  
**Knowledge Required**: Firebase, Android Layouts, Fragment management
