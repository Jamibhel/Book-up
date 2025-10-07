package com.example.bookup; // Consider moving to com.example.bookup.adapters

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.models.TutorOverview;
// import com.bumptech.glide.Glide; // Uncomment when you integrate Glide for image loading

import java.util.List;

public class TutorOverviewAdapter extends RecyclerView.Adapter<TutorOverviewAdapter.TutorOverviewViewHolder> {

    private List<TutorOverview> tutors;
    private OnItemClickListener listener;

    // Interface to handle clicks on individual tutor cards
    public interface OnItemClickListener {
        void onItemClick(TutorOverview tutor);
    }

    // Setter for the click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Constructor to provide the data list
    public TutorOverviewAdapter(List<TutorOverview> tutors) {
        this.tutors = tutors;
    }

    @NonNull
    @Override
    public TutorOverviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single tutor item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_overview, parent, false);
        return new TutorOverviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorOverviewViewHolder holder, int position) {
        TutorOverview tutor = tutors.get(position);

        // Set data to the views
        holder.txtTutorName.setText(tutor.getName());
        holder.txtTutorRating.setText(String.valueOf(tutor.getRating()));
        holder.txtTutorSubject.setText(tutor.getMainSubject());

        // TODO: Future: Load profile image with Glide/Picasso here
        // if (tutor.getProfileImageUrl() != null && !tutor.getProfileImageUrl().isEmpty()) {
        //     Glide.with(holder.imgTutorProfile.getContext())
        //          .load(tutor.getProfileImageUrl())
        //          .placeholder(R.drawable.ic_default_profile_picture)
        //          .error(R.drawable.ic_default_profile_picture)
        //          .into(holder.imgTutorProfile);
        // } else {
        holder.imgTutorProfile.setImageResource(R.drawable.ic_default_profile_picture); // Default placeholder
        // }

        // Attach click listener to the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(tutor);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tutors.size();
    }

    // ViewHolder class to hold references to the views in each item layout
    public static class TutorOverviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTutorProfile;
        TextView txtTutorName;
        TextView txtTutorRating;
        TextView txtTutorSubject;

        public TutorOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTutorProfile = itemView.findViewById(R.id.img_tutor_profile);
            txtTutorName = itemView.findViewById(R.id.txt_tutor_name);
            txtTutorRating = itemView.findViewById(R.id.txt_tutor_rating);
            txtTutorSubject = itemView.findViewById(R.id.txt_tutor_subject);
        }
    }
}
