package com.example.bookup;

import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // For context-aware color fetching
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookup.models.HelpRequest;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Locale;

public class HelpRequestAdapter extends RecyclerView.Adapter<HelpRequestAdapter.RequestViewHolder> {

    private List<HelpRequest> requestList;
    // Removed direct listener interface as we'll handle navigation within the adapter
    // private OnRequestClickListener listener;

    // You can keep this interface if you want more specific callback logic
    // or remove it if direct navigation is sufficient
    /*
    public interface OnRequestClickListener {
        void onRequestClick(HelpRequest request);
        void onViewDetailsClick(HelpRequest request);
    }

    public void setOnRequestClickListener(OnRequestClickListener listener) {
        this.listener = listener;
    }
    */

    public HelpRequestAdapter(List<HelpRequest> requestList) {
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_card, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        HelpRequest currentRequest = requestList.get(position);

        holder.textRequestTitle.setText(currentRequest.getTitle());
        holder.textRequestSubject.setText(String.format("Subject: %s", currentRequest.getSubject()));
        holder.textRequestDescriptionSummary.setText(currentRequest.getDescription());
        holder.chipRequestStatus.setText(currentRequest.getStatus());

        // Set status chip color dynamically using ContextCompat
        if ("Open".equalsIgnoreCase(currentRequest.getStatus())) {
            holder.chipRequestStatus.setChipBackgroundColorResource(R.color.colorPrimaryContainer);
            holder.chipRequestStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorOnPrimaryContainer));
        } else if ("Resolved".equalsIgnoreCase(currentRequest.getStatus())) {
            // Using placeholder colors based on Material Design 3 for success state
            holder.chipRequestStatus.setChipBackgroundColorResource(R.color.md_theme_light_tertiaryContainer);
            holder.chipRequestStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_light_onTertiaryContainer));
        } else { // Default or other statuses
            holder.chipRequestStatus.setChipBackgroundColorResource(R.color.colorSurfaceVariant);
            holder.chipRequestStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorOnSurfaceVariant));
        }

        // Format timestamp
        String dateString = "";
        if (currentRequest.getTimestamp() != null) {
            dateString = DateFormat.format("MMM dd, yyyy", currentRequest.getTimestamp()).toString();
        }
        holder.textRequestByDate.setText(String.format("Posted by %s on %s", currentRequest.getRequestedByName(), dateString));

        // --- NEW: Direct navigation to RequestDetailsActivity ---
        View.OnClickListener navigateToDetails = v -> {
            Intent intent = new Intent(holder.itemView.getContext(), RequestDetailsActivity.class);
            intent.putExtra(RequestDetailsActivity.EXTRA_REQUEST, currentRequest);
            holder.itemView.getContext().startActivity(intent);
        };

        holder.itemView.setOnClickListener(navigateToDetails);
        holder.btnViewRequestDetails.setOnClickListener(navigateToDetails);
        // --- END NEW ---
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView textRequestTitle;
        Chip chipRequestStatus;
        TextView textRequestSubject;
        TextView textRequestDescriptionSummary;
        TextView textRequestByDate;
        Button btnViewRequestDetails; // Changed to MaterialButton in XML, but can be referenced as Button in Java if IDs match

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            textRequestTitle = itemView.findViewById(R.id.text_request_title);
            chipRequestStatus = itemView.findViewById(R.id.chip_request_status);
            textRequestSubject = itemView.findViewById(R.id.text_request_subject);
            textRequestDescriptionSummary = itemView.findViewById(R.id.text_request_description_summary);
            textRequestByDate = itemView.findViewById(R.id.text_request_by_date);
            btnViewRequestDetails = itemView.findViewById(R.id.btn_view_request_details);
        }
    }
}
