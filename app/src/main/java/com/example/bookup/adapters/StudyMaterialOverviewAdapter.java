package com.example.bookup.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.activities.MaterialDetailsActivity;
import com.example.bookup.models.StudyMaterial;

import java.util.List;
import java.util.Locale;

public class StudyMaterialOverviewAdapter extends RecyclerView.Adapter<StudyMaterialOverviewAdapter.MaterialOverviewViewHolder> {

    private List<StudyMaterial> materialList;
    private OnMaterialClickListener listener;

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

        if (currentMaterial.isPremium()) {
            holder.textPriceBadge.setVisibility(View.VISIBLE);
            holder.textPriceBadge.setText(String.format(Locale.getDefault(), "₦%.0f", currentMaterial.getPrice()));
        } else {
            holder.textPriceBadge.setVisibility(View.VISIBLE);
            holder.textPriceBadge.setText("FREE");
        }

        if (currentMaterial.getThumbnailUrl() != null && !currentMaterial.getThumbnailUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentMaterial.getThumbnailUrl())
                    .placeholder(R.drawable.ic_document_placeholder)
                    .error(R.drawable.ic_document_placeholder)
                    .into(holder.materialThumbnail);
        } else {
            holder.materialThumbnail.setImageResource(R.drawable.ic_document_placeholder);
        }

        holder.btnViewMaterialCard.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), MaterialDetailsActivity.class);
            intent.putExtra(MaterialDetailsActivity.EXTRA_MATERIAL, currentMaterial);
            holder.itemView.getContext().startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMaterialClick(currentMaterial);
            } else {
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
        TextView textPriceBadge;
        Button btnViewMaterialCard;

        public MaterialOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
            materialThumbnail = itemView.findViewById(R.id.material_thumbnail);
            materialTitle = itemView.findViewById(R.id.material_title);
            materialSubjectType = itemView.findViewById(R.id.material_subject_type);
            materialRatingDownloads = itemView.findViewById(R.id.material_rating_downloads);
            materialUploader = itemView.findViewById(R.id.material_uploader);
            textPriceBadge = itemView.findViewById(R.id.text_price_badge);
            btnViewMaterialCard = itemView.findViewById(R.id.btn_view_material_card);
        }
    }
}
