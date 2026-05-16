package com.example.bookup.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.models.Review;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private List<Review> reviews = new ArrayList<>();

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView studentImage;
        TextView studentName, reviewDate, reviewComment;
        RatingBar ratingBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            studentImage = itemView.findViewById(R.id.student_image);
            studentName = itemView.findViewById(R.id.student_name);
            reviewDate = itemView.findViewById(R.id.review_date);
            reviewComment = itemView.findViewById(R.id.review_comment);
            ratingBar = itemView.findViewById(R.id.review_rating_bar);
        }

        void bind(Review review) {
            studentName.setText(review.getStudentName());
            reviewComment.setText(review.getComment());
            ratingBar.setRating(review.getRating());

            if (review.getCreatedAt() != null) {
                reviewDate.setText(DateUtils.getRelativeTimeSpanString(
                        review.getCreatedAt().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.DAY_IN_MILLIS));
            }

            Glide.with(itemView.getContext())
                    .load(review.getStudentPhotoUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(studentImage);
        }
    }
}
