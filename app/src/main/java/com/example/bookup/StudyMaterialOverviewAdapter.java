package com.example.bookup;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // NEW IMPORT
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.models.StudyMaterial;

import java.util.List;
import java.util.Locale;

public class StudyMaterialOverviewAdapter extends RecyclerView.Adapter<StudyMaterialOverviewAdapter.MaterialOverviewViewHolder> {

    private List<StudyMaterial> materialList;
    private OnMaterialClickListener listener; // Retaining for broader click handling if needed

    public interface OnMaterialClickListener {
        void onMaterialClick(StudyMaterial material);
    }

    public void setOnMaterialClickListener(OnMaterialClickListener listener) {
        this.listener = listener;
    }

    public StudyMaterialOverviewAdapter(List<StudyMaterial> materialList) {
        this.materialList = materialList;
    }

    @NonNull
    @Override
    public MaterialOverviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_study_material_card, parent, false);
        return new MaterialOverviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialOverviewViewHolder holder, int position) {
        StudyMaterial currentMaterial = materialList.get(position);

        holder.materialTitle.setText(currentMaterial.getTitle());
        holder.materialSubjectType.setText(String.format(Locale.getDefault(), "%s • %s", currentMaterial.getSubject(), currentMaterial.getMaterialType()));
        holder.materialRatingDownloads.setText(String.format(Locale.getDefault(), "%.1f • %d downloads", currentMaterial.getAverageRating(), currentMaterial.getDownloadCount()));
        holder.materialUploader.setText(String.format(Locale.getDefault(), "by %s", currentMaterial.getUploaderName()));

        // Load thumbnail image using Glide
        if (currentMaterial.getThumbnailUrl() != null && !currentMaterial.getThumbnailUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentMaterial.getThumbnailUrl())
                    .placeholder(R.drawable.ic_document_placeholder) // Placeholder image
                    .error(R.drawable.ic_document_placeholder) // Error image
                    .into(holder.materialThumbnail);
        } else {
            holder.materialThumbnail.setImageResource(R.drawable.ic_document_placeholder); // Default if no URL
        }

        // Set click listener for the "View Material" button
        holder.btnViewMaterialCard.setOnClickListener(v -> {
            // Launch MaterialDetailsActivity when the button is clicked
            Intent intent = new Intent(holder.itemView.getContext(), MaterialDetailsActivity.class);
            intent.putExtra(MaterialDetailsActivity.EXTRA_MATERIAL, currentMaterial);
            holder.itemView.getContext().startActivity(intent);
        });

        // Optional: Make the entire card clickable as well
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMaterialClick(currentMaterial); // Use the existing listener
            } else {
                // Fallback if no listener is set explicitly, or if you want both to go to details
                Intent intent = new Intent(holder.itemView.getContext(), MaterialDetailsActivity.class);
                intent.putExtra(MaterialDetailsActivity.EXTRA_MATERIAL, currentMaterial);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return materialList.size();
    }

    public static class MaterialOverviewViewHolder extends RecyclerView.ViewHolder {
        ImageView materialThumbnail;
        TextView materialTitle;
        TextView materialSubjectType;
        TextView materialRatingDownloads;
        TextView materialUploader;
        Button btnViewMaterialCard; // NEW UI Element

        public MaterialOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
            materialThumbnail = itemView.findViewById(R.id.material_thumbnail);
            materialTitle = itemView.findViewById(R.id.material_title);
            materialSubjectType = itemView.findViewById(R.id.material_subject_type);
            materialRatingDownloads = itemView.findViewById(R.id.material_rating_downloads);
            materialUploader = itemView.findViewById(R.id.material_uploader);
            btnViewMaterialCard = itemView.findViewById(R.id.btn_view_material_card); // Initialize new button
        }
    }
}
