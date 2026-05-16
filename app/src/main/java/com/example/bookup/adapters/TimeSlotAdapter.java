package com.example.bookup.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookup.R;
import com.example.bookup.databinding.ItemTimeSlotBinding;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
    private List<String> slots = new ArrayList<>();
    private String selectedSlot = null;
    private final OnSlotClickListener listener;

    public interface OnSlotClickListener {
        void onSlotClick(String slot);
    }

    public TimeSlotAdapter(OnSlotClickListener listener) {
        this.listener = listener;
    }

    public void setSlots(List<String> slots) {
        this.slots = slots != null ? slots : new ArrayList<>();
        this.selectedSlot = null;
        notifyDataSetChanged();
    }

    public String getSelectedSlot() {
        return selectedSlot;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTimeSlotBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(slots.get(position));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTimeSlotBinding binding;

        ViewHolder(ItemTimeSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String slot) {
            binding.textTime.setText(slot);
            boolean isSelected = slot.equals(selectedSlot);
            
            binding.cardTimeSlot.setStrokeColor(isSelected ? 
                ContextCompat.getColor(itemView.getContext(), R.color.primary) : 
                ContextCompat.getColor(itemView.getContext(), R.color.border_light));
            
            binding.cardTimeSlot.setCardBackgroundColor(isSelected ? 
                ContextCompat.getColor(itemView.getContext(), R.color.primary_faded) : 
                ContextCompat.getColor(itemView.getContext(), R.color.white));

            itemView.setOnClickListener(v -> {
                selectedSlot = slot;
                notifyDataSetChanged();
                listener.onSlotClick(slot);
            });
        }
    }
}
