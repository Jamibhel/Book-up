package com.example.bookup;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // NEW IMPORT
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Added for testing purposes

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.models.Tutor;
import com.google.android.material.imageview.ShapeableImageView; // NEW IMPORT

import java.util.List;
import java.util.Locale;

public class TutorOverviewAdapter extends RecyclerView.Adapter<TutorOverviewAdapter.TutorViewHolder> {

    private List<Tutor> tutorList;
    private OnTutorClickListener listener; // Keep listener for card background click if needed, or remove if button handles all interaction

    // Interface for click events on tutors
    public interface OnTutorClickListener {
        void onTutorClick(Tutor tutor);
    }

    public void setOnTutorClickListener(OnTutorClickListener listener) {
        this.listener = listener;
    }

    public TutorOverviewAdapter(List<Tutor> tutorList) {
        this.tutorList = tutorList;
    }

    @NonNull
    @Override
    public TutorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tutor_card, parent, false);
        return new TutorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorViewHolder holder, int position) {
        Tutor currentTutor = tutorList.get(position);

        holder.tutorName.setText(currentTutor.getName());
        holder.tutorRating.setText(String.format(Locale.getDefault(), "%.1f (%d)", currentTutor.getRating(), currentTutor.getReviewCount()));

        // Display subjects, handling null or empty list
        if (currentTutor.getSubjects() != null && !currentTutor.getSubjects().isEmpty()) {
            // Join first 2 subjects for display in card, if more exist
            String subjectsText = TextUtils.join(", ", currentTutor.getSubjects().subList(0, Math.min(currentTutor.getSubjects().size(), 2)));
            if (currentTutor.getSubjects().size() > 2) {
                subjectsText += "...";
            }
            holder.tutorSubjects.setText(subjectsText);
            holder.tutorSubjects.setVisibility(View.VISIBLE);
        } else {
            holder.tutorSubjects.setVisibility(View.GONE); // Hide if no subjects
        }

        // Load tutor image using Glide
        if (currentTutor.getProfileImageUrl() != null && !currentTutor.getProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentTutor.getProfileImageUrl())
                    .placeholder(R.drawable.ic_profile_black_24dp) // Placeholder image
                    .error(R.drawable.ic_profile_black_24dp) // Error image
                    .into(holder.tutorImage);
        } else {
            holder.tutorImage.setImageResource(R.drawable.ic_profile_black_24dp); // Default if no URL
        }

        // Set click listener for the "View Profile" button
        holder.btnViewTutorProfile.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), TutorDetailsActivity.class);
            intent.putExtra(TutorDetailsActivity.EXTRA_TUTOR, currentTutor);
            holder.itemView.getContext().startActivity(intent);
        });

        // Optional: Keep the whole card clickable for consistency, if desired
        holder.itemView.setOnClickListener(v -> {
            // This could launch the same activity, or perform a different action
            // For now, it will launch the details activity
            Intent intent = new Intent(holder.itemView.getContext(), TutorDetailsActivity.class);
            intent.putExtra(TutorDetailsActivity.EXTRA_TUTOR, currentTutor);
            holder.itemView.getContext().startActivity(intent);
        });

        // If you only want the button to be clickable, remove the holder.itemView.setOnClickListener above.
        // If you want both clickable, ensure the button click doesn't bubble up to the item click
        // by returning true from the button's onClick listener (though typically not an issue here).
    }

    @Override
    public int getItemCount() {
        return tutorList.size();
    }

    public static class TutorViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView tutorImage; // Changed to ShapeableImageView
        TextView tutorName;
        TextView tutorRating;
        TextView tutorSubjects;
        Button btnViewTutorProfile; // NEW UI Element

        public TutorViewHolder(@NonNull View itemView) {
            super(itemView);
            tutorImage = itemView.findViewById(R.id.tutor_image);
            tutorName = itemView.findViewById(R.id.tutor_name);
            tutorRating = itemView.findViewById(R.id.tutor_rating);
            tutorSubjects = itemView.findViewById(R.id.tutor_subjects);
            btnViewTutorProfile = itemView.findViewById(R.id.btn_view_tutor_profile); // Initialize new button
        }
    }
}
