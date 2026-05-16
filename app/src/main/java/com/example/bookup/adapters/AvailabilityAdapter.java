package com.example.bookup.adapters;

import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.R;
import com.example.bookup.databinding.ItemAvailabilityDayBinding;
import com.example.bookup.models.Availability;

import java.util.List;
import java.util.Locale;

public class AvailabilityAdapter extends RecyclerView.Adapter<AvailabilityAdapter.ViewHolder> {
    private final List<Availability> items;
    private final boolean isEditable;

    public AvailabilityAdapter(List<Availability> items, boolean isEditable) {
        this.items = items;
        this.isEditable = isEditable;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemAvailabilityDayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAvailabilityDayBinding binding;

        ViewHolder(ItemAvailabilityDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Availability item) {
            binding.textDayName.setText(item.getDay());
            binding.switchAvailable.setChecked(item.isAvailable());
            binding.textStartTime.setText(item.getStartTime());
            binding.textEndTime.setText(item.getEndTime());
            
            binding.layoutTimeSelectors.setVisibility(item.isAvailable() ? View.VISIBLE : View.GONE);
            binding.switchAvailable.setEnabled(isEditable);
            
            if (isEditable) {
                binding.switchAvailable.setOnCheckedChangeListener((v, isChecked) -> {
                    item.setAvailable(isChecked);
                    binding.layoutTimeSelectors.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                });

                binding.textStartTime.setOnClickListener(v -> showTimePicker(item, true, binding.textStartTime));
                binding.textEndTime.setOnClickListener(v -> showTimePicker(item, false, binding.textEndTime));
            } else {
                binding.switchAvailable.setVisibility(View.GONE);
                // If not editable and not available, maybe hide the row or show "Unavailable"
                if (!item.isAvailable()) {
                    binding.layoutTimeSelectors.setVisibility(View.VISIBLE);
                    binding.textStartTime.setText("Unavailable");
                    binding.textEndTime.setVisibility(View.GONE);
                    itemView.findViewById(R.id.text_day_name).setAlpha(0.5f);
                }
            }
        }

        private void showTimePicker(Availability item, boolean isStart, TextView textView) {
            String currentTime = isStart ? item.getStartTime() : item.getEndTime();
            int hour = Integer.parseInt(currentTime.split(":")[0]);
            int minute = Integer.parseInt(currentTime.split(":")[1]);

            TimePickerDialog dialog = new TimePickerDialog(itemView.getContext(), (view, h, m) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                textView.setText(time);
                if (isStart) item.setStartTime(time);
                else item.setEndTime(time);
            }, hour, minute, true);
            dialog.show();
        }
    }
}
