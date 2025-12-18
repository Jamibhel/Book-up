package com.example.bookup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.models.StudyMaterial;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying and managing study materials in admin panel.
 * Displays material info with delete action.
 */
public class StudyMaterialAdapter extends RecyclerView.Adapter<StudyMaterialAdapter.MaterialViewHolder> {

    private final List<StudyMaterial> materials;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(StudyMaterial material);
    }

    public StudyMaterialAdapter(List<StudyMaterial> materials) {
        this.materials = materials;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_material, parent, false);
        return new MaterialViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        StudyMaterial material = materials.get(position);
        holder.bind(material);
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    /**
     * ViewHolder for study material items
     */
    public class MaterialViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailImage;
        private final TextView titleText;
        private final TextView uploaderText;
        private final TextView materialTypeText;
        private final TextView subjectText;
        private final TextView timestampText;
        private final RatingBar ratingBar;
        private final TextView downloadsText;
        private final ImageButton deleteButton;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImage = itemView.findViewById(R.id.image_thumbnail);
            titleText = itemView.findViewById(R.id.text_title);
            uploaderText = itemView.findViewById(R.id.text_uploader);
            materialTypeText = itemView.findViewById(R.id.text_material_type);
            subjectText = itemView.findViewById(R.id.text_subject);
            timestampText = itemView.findViewById(R.id.text_timestamp);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            downloadsText = itemView.findViewById(R.id.text_downloads);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(StudyMaterial material) {
            // Load thumbnail with Glide
            if (material.getThumbnailUrl() != null && !material.getThumbnailUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(material.getThumbnailUrl())
                        .placeholder(R.drawable.ic_dashboard_black_24dp)
                        .error(R.drawable.ic_dashboard_black_24dp)
                        .into(thumbnailImage);
            } else {
                thumbnailImage.setImageResource(R.drawable.ic_dashboard_black_24dp);
            }

            // Set text fields
            titleText.setText(material.getTitle());
            uploaderText.setText("By: " + (material.getUploaderName() != null ? material.getUploaderName() : "Unknown"));
            materialTypeText.setText(material.getMaterialType());
            subjectText.setText(material.getSubject());

            // Format timestamp
            if (material.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                timestampText.setText(sdf.format(material.getTimestamp()));
            } else {
                timestampText.setText("Date unknown");
            }

            // Set rating and downloads
            ratingBar.setRating((float) material.getAverageRating());
            downloadsText.setText("Downloads: " + material.getDownloadCount());

            // Delete button listener
            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(material);
                }
            });
        }
    }
}
