package com.example.bookup; // Consider moving to com.example.bookup.adapters

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.models.StudyMaterialOverview;

import java.util.List;

public class StudyMaterialOverviewAdapter extends RecyclerView.Adapter<StudyMaterialOverviewAdapter.StudyMaterialOverviewViewHolder> {

    private List<StudyMaterialOverview> materials;
    private OnItemClickListener listener;

    // Interface to handle clicks on individual material cards
    public interface OnItemClickListener {
        void onItemClick(StudyMaterialOverview material);
    }

    // Setter for the click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Constructor to provide the data list
    public StudyMaterialOverviewAdapter(List<StudyMaterialOverview> materials) {
        this.materials = materials;
    }

    @NonNull
    @Override
    public StudyMaterialOverviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single material item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_study_material_overview, parent, false);
        return new StudyMaterialOverviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudyMaterialOverviewViewHolder holder, int position) {
        StudyMaterialOverview material = materials.get(position);

        // Set data to the views
        holder.txtMaterialTitle.setText(material.getTitle());
        holder.txtMaterialCourseCode.setText(material.getCourseCode());

        // TODO: Future: Load thumbnail image if available
        // For now, default icon based on type (if type was in model) or generic PDF icon
        holder.imgMaterialIcon.setImageResource(R.drawable.ic_pdf_black_24dp);

        // Attach click listener to the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(material);
            }
        });
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    // ViewHolder class to hold references to the views in each item layout
    public static class StudyMaterialOverviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMaterialIcon;
        TextView txtMaterialTitle;
        TextView txtMaterialCourseCode;

        public StudyMaterialOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMaterialIcon = itemView.findViewById(R.id.img_material_icon);
            txtMaterialTitle = itemView.findViewById(R.id.txt_material_title);
            txtMaterialCourseCode = itemView.findViewById(R.id.txt_material_course_code);
        }
    }
}
