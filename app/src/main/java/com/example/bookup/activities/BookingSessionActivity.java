package com.example.bookup.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.R;
import com.example.bookup.adapters.TimeSlotAdapter;
import com.example.bookup.models.Availability;
import com.example.bookup.models.Booking;
import com.example.bookup.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingSessionActivity extends AppCompatActivity {
    private static final String TAG = "BookingSession";

    private String tutorId, tutorName;
    private User tutorUser;
    private final Calendar selectedCalendar = Calendar.getInstance();
    private String selectedTimeSlot = null;

    private CalendarView calendarView;
    private RecyclerView recyclerSlots;
    private TimeSlotAdapter slotAdapter;
    private TextInputEditText editSubject, editDescription;
    private MaterialButton btnBook;
    private TextView textNoSlots;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_session);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tutorId = getIntent().getStringExtra("tutorId");
        tutorName = getIntent().getStringExtra("tutorName");

        initViews();
        loadTutorData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_booking);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Book with " + (tutorName != null ? tutorName : "Tutor"));
        }

        calendarView = findViewById(R.id.calendar_view);
        recyclerSlots = findViewById(R.id.recycler_time_slots);
        editSubject = findViewById(R.id.edit_subject);
        editDescription = findViewById(R.id.edit_description);
        btnBook = findViewById(R.id.btn_book);
        textNoSlots = findViewById(R.id.text_no_slots);

        calendarView.setMinDate(System.currentTimeMillis() - 1000);
        calendarView.setOnDateChangeListener((view, year, month, day) -> {
            selectedCalendar.set(year, month, day);
            updateAvailableSlots();
        });

        slotAdapter = new TimeSlotAdapter(slot -> {
            selectedTimeSlot = slot;
            Log.d(TAG, "Selected slot: " + slot);
        });
        recyclerSlots.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerSlots.setAdapter(slotAdapter);

        btnBook.setOnClickListener(v -> bookSession());
    }

    private void loadTutorData() {
        if (tutorId == null) return;
        db.collection("users").document(tutorId).get().addOnSuccessListener(doc -> {
            tutorUser = doc.toObject(User.class);
            if (tutorUser != null) {
                updateAvailableSlots();
            }
        });
    }

    private void updateAvailableSlots() {
        if (tutorUser == null || tutorUser.getAvailability() == null) {
            showNoSlots();
            return;
        }

        String dayName = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(selectedCalendar.getTime());
        Availability dayAvail = null;
        for (Availability a : tutorUser.getAvailability()) {
            if (a.getDay().equalsIgnoreCase(dayName) && a.isAvailable()) {
                dayAvail = a;
                break;
            }
        }

        if (dayAvail == null) {
            showNoSlots();
            return;
        }

        List<String> slots = generateSlots(dayAvail.getStartTime(), dayAvail.getEndTime());
        if (slots.isEmpty()) {
            showNoSlots();
        } else {
            textNoSlots.setVisibility(View.GONE);
            recyclerSlots.setVisibility(View.VISIBLE);
            slotAdapter.setSlots(slots);
        }
    }

    private List<String> generateSlots(String start, String end) {
        List<String> list = new ArrayList<>();
        try {
            int startHour = Integer.parseInt(start.split(":")[0]);
            int endHour = Integer.parseInt(end.split(":")[0]);
            
            for (int h = startHour; h < endHour; h++) {
                String time = String.format(Locale.getDefault(), "%02d:00", h);
                list.add(time);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating slots", e);
        }
        return list;
    }

    private void showNoSlots() {
        textNoSlots.setVisibility(View.VISIBLE);
        recyclerSlots.setVisibility(View.GONE);
        slotAdapter.setSlots(new ArrayList<>());
    }

    private void bookSession() {
        if (editSubject.getText() == null) return;
        String subject = editSubject.getText().toString().trim();
        
        if (subject.isEmpty()) {
            Toast.makeText(this, "Please enter a subject", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTimeSlot == null) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser cu = auth.getCurrentUser();
        if (cu == null) return;

        btnBook.setEnabled(false);
        
        int hour = Integer.parseInt(selectedTimeSlot.split(":")[0]);
        selectedCalendar.set(Calendar.HOUR_OF_DAY, hour);
        selectedCalendar.set(Calendar.MINUTE, 0);
        selectedCalendar.set(Calendar.SECOND, 0);

        Booking b = new Booking(tutorId, cu.getUid(), tutorName, cu.getDisplayName());
        b.setSubject(subject);
        b.setDescription(editDescription.getText() != null ? editDescription.getText().toString() : "");
        b.setSessionDate(selectedCalendar.getTime());
        b.setStatus("pending");
        b.setParticipantIds(Arrays.asList(tutorId, cu.getUid()));

        db.collection("bookings").add(b).addOnSuccessListener(ref -> {
            b.setId(ref.getId());
            ref.set(b).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Booking request sent!", Toast.LENGTH_LONG).show();
                finish();
            });
        }).addOnFailureListener(e -> {
            btnBook.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
