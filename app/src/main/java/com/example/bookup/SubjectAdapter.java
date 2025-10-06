package com.example.bookup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
    private List<Map<String, Object>> subjects;
    private OnItemRemoveListener listener;


    public interface OnItemRemoveListener {
        void onItemRemove(int position);
    }

    public void setOnItemRemoveListener(SubjectAdapter.OnItemRemoveListener onItemRemoveListener) {
        this.listener = listener;
    }

    public SubjectAdapter(List<Map<String, Object>> subjectsLists) {
        this.subjects = subjects;
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder implements com.example.bookup.SubjectViewHolder {
        public TextView textViewRole;
        public TextView textViewDepartmentCode;
        public TextView textViewTopics;
        public TextView textViewCourseName;
        public ImageButton buttonRemove;

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
    //Adapter Methods
    //This is called when the recyclerView needs a new ViewHolder
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            //inflating the item_subject_entry.xml layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject_entry, parent, false);
        return new SubjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
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
        holder.textViewTopics.setText("Topics: " + (String) currentSubject.get("topics"));

        // Set the OnClickListener for the remove button
        holder.buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current position of the item in the adapter
                int adapterPosition = holder.getAdapterPosition();
                if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onItemRemove(adapterPosition); // Notify the activity
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return subjects.size();
    }




}
