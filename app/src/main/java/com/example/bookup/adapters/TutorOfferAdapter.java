package com.example.bookup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.models.TutorOffer;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying tutor offers in a RecyclerView
 */
public class TutorOfferAdapter extends RecyclerView.Adapter<TutorOfferAdapter.OfferViewHolder> {

    private List<TutorOffer> offers;
    private Context context;
    private OnOfferClickListener listener;

    public interface OnOfferClickListener {
        void onOfferClick(TutorOffer offer, int position);
        void onAcceptClick(TutorOffer offer, int position);
        void onRejectClick(TutorOffer offer, int position);
    }

    public TutorOfferAdapter(List<TutorOffer> offers, OnOfferClickListener listener) {
        this.offers = offers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_tutor_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        TutorOffer offer = offers.get(position);
        holder.bind(offer, position);
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    class OfferViewHolder extends RecyclerView.ViewHolder {
        private ImageView tutorPhoto;
        private TextView tutorName;
        private TextView offerMessage;
        private TextView bidAmount;
        private TextView offerDate;
        private TextView offerStatus;
        private View acceptButton;
        private View rejectButton;

        OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            tutorPhoto = itemView.findViewById(R.id.img_tutor_photo);
            tutorName = itemView.findViewById(R.id.text_tutor_name);
            offerMessage = itemView.findViewById(R.id.text_offer_message);
            bidAmount = itemView.findViewById(R.id.text_bid_amount);
            offerDate = itemView.findViewById(R.id.text_offer_date);
            offerStatus = itemView.findViewById(R.id.text_offer_status);
            acceptButton = itemView.findViewById(R.id.btn_accept_offer);
            rejectButton = itemView.findViewById(R.id.btn_reject_offer);
        }

        void bind(TutorOffer offer, int position) {
            // Load tutor photo
            if (offer.getTutorPhotoUrl() != null && !offer.getTutorPhotoUrl().isEmpty()) {
                Glide.with(context)
                        .load(offer.getTutorPhotoUrl())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .circleCrop()
                        .into(tutorPhoto);
            }

            // Set tutor name
            tutorName.setText(offer.getTutorName() != null ? offer.getTutorName() : "Unknown Tutor");

            // Set offer message
            offerMessage.setText(offer.getMessage());

            // Set bid amount
            if (offer.getBidAmount() != null && offer.getBidAmount() > 0) {
                bidAmount.setText("Bid: $" + String.format(Locale.US, "%.2f", offer.getBidAmount()));
                bidAmount.setVisibility(View.VISIBLE);
            } else {
                bidAmount.setVisibility(View.GONE);
            }

            // Set offer date
            if (offer.getTimestamp() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                String dateText = dateFormat.format(offer.getTimestamp());
                offerDate.setText(dateText);
            }

            // Set offer status
            String status = offer.getStatus() != null ? offer.getStatus() : "pending";
            offerStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            
            // Update status colors based on offer status
            switch (status.toLowerCase()) {
                case "accepted":
                    offerStatus.setTextColor(context.getResources().getColor(R.color.primary));
                    break;
                case "rejected":
                    offerStatus.setTextColor(context.getResources().getColor(R.color.primary_dark));
                    break;
                default:
                    offerStatus.setTextColor(context.getResources().getColor(R.color.secondary));
                    break;
            }

            // Show/hide buttons based on offer status
            if ("pending".equalsIgnoreCase(status)) {
                acceptButton.setVisibility(View.VISIBLE);
                rejectButton.setVisibility(View.VISIBLE);
                acceptButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAcceptClick(offer, getAdapterPosition());
                    }
                });
                rejectButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRejectClick(offer, getAdapterPosition());
                    }
                });
            } else {
                acceptButton.setVisibility(View.GONE);
                rejectButton.setVisibility(View.GONE);
            }

            // Item click to view details
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOfferClick(offer, getAdapterPosition());
                }
            });
        }
    }
}
