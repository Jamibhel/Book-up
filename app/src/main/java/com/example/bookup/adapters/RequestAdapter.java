package com.example.bookup.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Locale;

import com.example.bookup.R;
import com.example.bookup.models.Request;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private List<Request> requests;
    private final OnRequestClickListener listener;

    public interface OnRequestClickListener {
        void onRequestClick(Request request);
    }

    public RequestAdapter(List<Request> requests, OnRequestClickListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requests.get(position);
        holder.textRequestSubject.setText(request.getSubject());
        holder.textRequestDescription.setText(request.getDescription());
        holder.textRequestUser.setText(request.getUserDisplayName());

        // Format timestamp as relative time
        CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
            request.getTimestamp().getTime(),
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        );
        holder.textRequestTime.setText(relativeTime);

        // Set status chip style and text
        String statusText = request.getStatus();
        int colorResId;
        int textColorResId = android.R.color.white;

        switch (request.getStatus().toLowerCase(Locale.ROOT)) {
            case "pending":
                colorResId = R.color.warning;
                break;
            case "accepted":
                colorResId = R.color.success;
                break;
            case "rejected":
                colorResId = R.color.error;
                break;
            case "completed":
                colorResId = R.color.secondary;
                break;
            default:
                colorResId = R.color.secondary;
                statusText = "Unknown";
                break;
        }

        Context context = holder.itemView.getContext();
        holder.chipRequestStatus.setText(statusText);
        holder.chipRequestStatus.setChipBackgroundColor(ColorStateList.valueOf(context.getColor(colorResId)));
        holder.chipRequestStatus.setTextColor(context.getColor(textColorResId));

        holder.cardRequest.setOnClickListener(v -> listener.onRequestClick(request));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateRequests(List<Request> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardRequest;
        TextView textRequestSubject;
        TextView textRequestDescription;
        TextView textRequestUser;
        TextView textRequestTime;
        Chip chipRequestStatus;

        RequestViewHolder(View itemView) {
            super(itemView);
            cardRequest = itemView.findViewById(R.id.card_request);
            textRequestSubject = itemView.findViewById(R.id.text_request_subject);
            textRequestDescription = itemView.findViewById(R.id.text_request_description);
            textRequestUser = itemView.findViewById(R.id.text_request_user);
            textRequestTime = itemView.findViewById(R.id.text_request_time);
            chipRequestStatus = itemView.findViewById(R.id.chip_request_status);
        }
    }
}