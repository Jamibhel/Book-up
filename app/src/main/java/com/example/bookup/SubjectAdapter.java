package com.example.bookup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Using ImageButton for the remove button
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList; // Added for defensive initialization
import java.util.List;
import java.util.Map;
import java.util.Objects; // Keep if you use it elsewhere, otherwise it's not strictly needed here

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    // The list that holds your subject data. It MUST NOT be null.
    private List<Map<String, Object>> subjects;
    // The listener for remove button clicks.
    private OnItemRemoveListener listener;

    // Interface for item removal callbacks (defined here for modularity)
    public interface OnItemRemoveListener {
        void onItemRemove(int position);
    }

    /**
     * Sets the listener for item removal events.
     * @param onItemRemoveListener The listener to be set.
     */
    public void setOnItemRemoveListener(OnItemRemoveListener onItemRemoveListener) {
        // Corrected: Assign the passed parameter to the class field.
        this.listener = onItemRemoveListener;
    }

    /**
     * Constructor for the SubjectAdapter.
     * Initializes the subjects list, ensuring it's never null.
     * @param subjectsList The list of subjects to display. Can be null or empty.
     */
    public SubjectAdapter(List<Map<String, Object>> subjectsList) {
        // CRITICAL FIX: Properly assign the passed list to the class member.
        // Also, defensively initialize to an empty ArrayList if null is passed,
        // preventing NullPointerExceptions in methods like getItemCount().
        if (subjectsList == null) {
            this.subjects = new ArrayList<>();
        } else {
            this.subjects = subjectsList;
        }
    }

    /**
     * ViewHolder class for subject items in the RecyclerView.
     * Holds references to the views within each item layout.
     */
    public static class SubjectViewHolder extends RecyclerView.ViewHolder { // Removed redundant 'implements'
        public TextView textViewRole;
        public TextView textViewDepartmentCode;
        public TextView textViewTopics;
        public TextView textViewCourseName;
        public ImageButton buttonRemove; // Using ImageButton

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRole = itemView.findViewById(R.id.text_view_subject_role);
            textViewDepartmentCode = itemView.findViewById(R.id.text_view_department_code);
            textViewCourseName = itemView.findViewById(R.id.text_view_course_name);
            textViewTopics = itemView.findViewById(R.id.text_view_topics);
            buttonRemove = itemView.findViewById(R.id.button_remove_subject);
        }
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the item_subject_entry.xml layout to create a new view holder
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject_entry, parent, false);
        return new SubjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        // Retrieve the current subject data map from the list
        Map<String, Object> currentSubject = subjects.get(position);

        // Populate the views in the ViewHolder with data from the current subject
        holder.textViewRole.setText((String) currentSubject.get("role"));
        holder.textViewDepartmentCode.setText(
                String.format("%s %s",
                        (String) currentSubject.get("department"),
                        (String) currentSubject.get("courseCode")
                )
        );
        holder.textViewCourseName.setText((String) currentSubject.get("courseName"));
        // Safely retrieve topics, handling potential null (though TextUtils.isEmpty in validation should prevent)
        String topics = (String) currentSubject.get("topics");
        holder.textViewTopics.setText("Topics: " + (topics != null ? topics : "N/A"));

        // Set the OnClickListener for the remove button
        // Only set the listener and make the button visible if a listener has been provided.
        if (listener != null) {
            holder.buttonRemove.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) { // Check for valid position
                    listener.onItemRemove(adapterPosition); // Notify the activity/fragment
                }
            });
            holder.buttonRemove.setVisibility(View.VISIBLE); // Make remove button visible
        } else {
            holder.buttonRemove.setOnClickListener(null); // Clear listener if none provided
            holder.buttonRemove.setVisibility(View.GONE); // Hide remove button (e.g., in read-only mode)
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * This method is crucial and will not throw NPE after the constructor fix.
     * @return The total number of items.
     */
    @Override
    public int getItemCount() {
        return subjects.size();
    }
}
