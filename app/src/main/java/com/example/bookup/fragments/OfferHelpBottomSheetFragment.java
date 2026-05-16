package com.example.bookup.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bookup.R;
import com.example.bookup.models.TutorOffer;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * BottomSheetFragment for tutors to submit offers for help requests
 */
public class OfferHelpBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_REQUEST_ID = "requestId";
    private static final String ARG_STUDENT_UID = "studentUid";
    private static final String TAG = "OfferHelpFragment";

    private TextInputEditText editTextMessage;
    private TextInputEditText editTextBid;
    private MaterialButton btnSubmitOffer;
    private MaterialButton btnCancel;

    private String requestId;
    private String studentUid;
    private boolean isSubmitting = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Callback interface for when offer is submitted
    public interface OfferSubmittedCallback {
        void onOfferSubmitted(TutorOffer offer);
    }

    private OfferSubmittedCallback callback;

    public static OfferHelpBottomSheetFragment newInstance(String requestId, String studentUid, OfferSubmittedCallback callback) {
        OfferHelpBottomSheetFragment fragment = new OfferHelpBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_ID, requestId);
        args.putString(ARG_STUDENT_UID, studentUid);
        fragment.setArguments(args);
        fragment.callback = callback;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (getArguments() != null) {
            requestId = getArguments().getString(ARG_REQUEST_ID);
            studentUid = getArguments().getString(ARG_STUDENT_UID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_offer_help, container, false);

        // Initialize views
        editTextMessage = view.findViewById(R.id.edit_text_message);
        editTextBid = view.findViewById(R.id.edit_text_bid);
        btnSubmitOffer = view.findViewById(R.id.btn_submit_offer);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Set up button listeners
        btnCancel.setOnClickListener(v -> dismiss());

        btnSubmitOffer.setOnClickListener(v -> submitOffer());

        return view;
    }

    /**
     * Submit the tutor's offer to Firestore
     */
    private void submitOffer() {
        if (isSubmitting) return;

        // Validate inputs
        String message = editTextMessage.getText().toString().trim();
        String bidText = editTextBid.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            editTextMessage.setError("Please provide a message about your experience");
            return;
        }

        if (message.length() < 20) {
            editTextMessage.setError("Message must be at least 20 characters");
            return;
        }

        isSubmitting = true;
        btnSubmitOffer.setEnabled(false);

        // Parse bid amount if provided
        final Double bidAmount;
        if (!TextUtils.isEmpty(bidText)) {
            try {
                bidAmount = Double.parseDouble(bidText);
                if (bidAmount < 0) {
                    editTextBid.setError("Bid amount must be positive");
                    isSubmitting = false;
                    btnSubmitOffer.setEnabled(true);
                    return;
                }
            } catch (NumberFormatException e) {
                editTextBid.setError("Invalid bid amount");
                isSubmitting = false;
                btnSubmitOffer.setEnabled(true);
                return;
            }
        } else {
            bidAmount = null;
        }

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            isSubmitting = false;
            btnSubmitOffer.setEnabled(true);
            return;
        }

        // Fetch tutor details and create offer
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;

                    if (documentSnapshot.exists()) {
                        String tutorName = documentSnapshot.getString("displayName");
                        String tutorPhotoUrl = documentSnapshot.getString("photoUrl");

                        // Create offer object
                        TutorOffer offer = new TutorOffer(
                                requestId,
                                currentUser.getUid(),
                                tutorName != null ? tutorName : "Unknown Tutor",
                                tutorPhotoUrl,
                                message,
                                bidAmount
                        );

                        // Save to Firestore
                        saveOfferToFirestore(offer);
                    } else {
                        Toast.makeText(getContext(), "Could not fetch tutor information", Toast.LENGTH_SHORT).show();
                        isSubmitting = false;
                        btnSubmitOffer.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    isSubmitting = false;
                    btnSubmitOffer.setEnabled(true);
                });
    }

    /**
     * Save the offer to Firestore
     */
    private void saveOfferToFirestore(TutorOffer offer) {
        // Store in helpRequests/{requestId}/offers subcollection
        db.collection("helpRequests")
                .document(requestId)
                .collection("offers")
                .add(offer)
                .addOnSuccessListener(documentReference -> {
                    if (!isAdded()) return;

                    // Set the offer ID
                    offer.setId(documentReference.getId());

                    // Notify callback
                    if (callback != null) {
                        callback.onOfferSubmitted(offer);
                    }

                    Toast.makeText(getContext(), "Offer submitted successfully!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to submit offer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    isSubmitting = false;
                    btnSubmitOffer.setEnabled(true);
                });
    }
}
