package com.example.bookup.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.databinding.ItemStudyMaterialBinding;
import com.example.bookup.models.StudyMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudyMaterialAdapter extends RecyclerView.Adapter<StudyMaterialAdapter.ViewHolder> {
    private List<StudyMaterial> materials = new ArrayList<>();
    private final OnMaterialClickListener listener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnMaterialClickListener {
        void onMaterialClick(StudyMaterial material);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(StudyMaterial material);
    }

    public StudyMaterialAdapter(List<StudyMaterial> materials) {
        this.materials = materials != null ? materials : new ArrayList<>();
        this.listener = null;
    }

    public StudyMaterialAdapter(OnMaterialClickListener listener) {
        this.listener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setMaterials(List<StudyMaterial> materials) {
        this.materials = materials != null ? materials : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemStudyMaterialBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(materials.get(position));
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudyMaterialBinding binding;

        ViewHolder(ItemStudyMaterialBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(StudyMaterial material) {
            binding.textTitle.setText(material.getTitle());
            binding.textSubject.setText(material.getSubject());
            binding.textMaterialType.setText(material.getMaterialType());

            if (material.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                binding.textTimestamp.setText(sdf.format(material.getTimestamp()));
            }

            // Display Uploader Name
            if (material.getUploaderName() != null) {
                binding.textUploader.setText("By " + material.getUploaderName());
            }

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onMaterialClick(material));
            }
            
            if (deleteClickListener != null) {
                binding.btnDelete.setVisibility(android.view.View.VISIBLE);
                binding.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(material));
            } else {
                binding.btnDelete.setVisibility(android.view.View.GONE);
            }
        }
    }
}
