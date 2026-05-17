package com.example.bookup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.R;
import com.example.bookup.models.Booking;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookings;
    private FirebaseFirestore db;
    private OnBookingStatusChangeListener statusChangeListener;

    public BookingAdapter(Context context, List<Booking> bookings) {
        this.context = context;
        this.bookings = bookings;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setOnStatusChangeListener(OnBookingStatusChangeListener listener) {
        this.statusChangeListener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        
        // Set student name
        holder.studentNameText.setText(booking.getStudentName() != null ? booking.getStudentName() : "Unknown Student");
        
        // Set subject
        holder.subjectText.setText(booking.getSubject() != null ? booking.getSubject() : "No subject specified");
        
        // Set description
        holder.descriptionText.setText(booking.getDescription() != null ? booking.getDescription() : "No description");
        
        // Set session date
        if (booking.getSessionDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
            String dateString = dateFormat.format(booking.getSessionDate());
            holder.dateText.setText("Session: " + dateString);
        } else {
            holder.dateText.setText("Session: Date not specified");
        }
        
        // Set status
        String status = booking.getStatus() != null ? booking.getStatus() : "pending";
        holder.statusText.setText("Status: " + status.substring(0, 1).toUpperCase() + status.substring(1));
        
        // Style based on status
        int statusColor;
        if ("confirmed".equals(status)) {
            statusColor = context.getResources().getColor(R.color.colorPrimary, null);
        } else if ("cancelled".equals(status)) {
            statusColor = context.getResources().getColor(R.color.colorSecondary, null);
        } else {
            statusColor = context.getResources().getColor(R.color.colorOnSurfaceVariant, null);
        }
        holder.statusText.setTextColor(statusColor);
        
        // Show action buttons only for pending bookings
        if ("pending".equals(status)) {
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.VISIBLE);
        } else {
            holder.acceptButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
        }
        
        // Accept button click
        holder.acceptButton.setOnClickListener(v -> updateBookingStatus(booking, "confirmed", position));
        
        // Reject button click
        holder.rejectButton.setOnClickListener(v -> updateBookingStatus(booking, "cancelled", position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    private void updateBookingStatus(Booking booking, String newStatus, int position) {
        if (booking.getId() == null) {
            Toast.makeText(context, "Error: Booking ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        db.collection("bookings").document(booking.getId())
                .update("status", newStatus, "updatedAt", new java.util.Date())
                .addOnSuccessListener(aVoid -> {
                    booking.setStatus(newStatus);
                    notifyItemChanged(position);
                    
                    String message = "confirmed".equals(newStatus) ? "Booking accepted!" : "Booking rejected!";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    
                    // Notify listener if set
                    if (statusChangeListener != null) {
                        statusChangeListener.onStatusChanged(booking);
                    }
                        // Create a notification document for the student so they can be alerted
                        try {
                            java.util.Map<String, Object> notif = new java.util.HashMap<>();
                            notif.put("toUserId", booking.getStudentId());
                            notif.put("fromUserId", booking.getTutorId());
                            notif.put("type", "booking_status_changed");
                            notif.put("bookingId", booking.getId());
                            notif.put("status", newStatus);
                            notif.put("subject", booking.getSubject() != null ? booking.getSubject() : "session");
                            notif.put("title", "Booking Update");
                            notif.put("message", "Your booking request for " + (booking.getSubject() != null ? booking.getSubject() : "session") + " has been " + newStatus + ".");
                            notif.put("read", false);
                            notif.put("createdAt", new java.util.Date());
                            notif.put("timestamp", new java.util.Date());

                            db.collection("notifications")
                                    .document(booking.getStudentId())
                                    .collection("messages")
                                    .add(notif);
                        } catch (Exception ignored) {
                            // Non-fatal: if notifications can't be written we still updated booking
                        }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView studentNameText;
        TextView subjectText;
        TextView descriptionText;
        TextView dateText;
        TextView statusText;
        Button acceptButton;
        Button rejectButton;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            studentNameText = itemView.findViewById(R.id.text_student_name);
            subjectText = itemView.findViewById(R.id.text_subject);
            descriptionText = itemView.findViewById(R.id.text_description);
            dateText = itemView.findViewById(R.id.text_session_date);
            statusText = itemView.findViewById(R.id.text_status);
            acceptButton = itemView.findViewById(R.id.btn_accept_booking);
            rejectButton = itemView.findViewById(R.id.btn_reject_booking);
        }
    }

    public interface OnBookingStatusChangeListener {
        void onStatusChanged(Booking booking);
    }
}
