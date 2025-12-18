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
        booking.setCreatedAt(new Date());
        booking.setUpdatedAt(new Date());

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
