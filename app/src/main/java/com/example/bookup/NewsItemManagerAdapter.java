package com.example.bookup;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.example.bookup.models.NewsItem; // Import your NewsItem model

import java.util.List;

public class NewsItemManagerAdapter extends RecyclerView.Adapter<NewsItemManagerAdapter.NewsItemViewHolder> {

    private List<NewsItem> newsList;
    private OnNewsItemActionListener listener;

    public interface OnNewsItemActionListener {
        void onEditClick(NewsItem newsItem);
        void onDeleteClick(NewsItem newsItem);
    }

    public void setOnNewsItemActionListener(OnNewsItemActionListener listener) {
        this.listener = listener;
    }

    public NewsItemManagerAdapter(List<NewsItem> newsList) {
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_news, parent, false);
        return new NewsItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsItemViewHolder holder, int position) {
        NewsItem currentNewsItem = newsList.get(position);

        holder.textNewsTitle.setText(currentNewsItem.getTitle());
        holder.textNewsDescriptionSummary.setText(currentNewsItem.getDescription()); // Display full description, or truncate as needed

        String dateString = "";
        if (currentNewsItem.getTimestamp() != null) { // Assuming NewsItem model has a timestamp field
            dateString = DateFormat.format("MMM dd, yyyy", currentNewsItem.getTimestamp()).toString();
        }
        holder.textNewsSourceDate.setText(String.format("%s | %s", currentNewsItem.getSource(), dateString));

        holder.btnEditNewsItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(currentNewsItem);
            }
        });

        holder.btnDeleteNewsItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(currentNewsItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsItemViewHolder extends RecyclerView.ViewHolder {
        TextView textNewsTitle;
        TextView textNewsDescriptionSummary;
        TextView textNewsSourceDate;
        MaterialButton btnEditNewsItem;
        MaterialButton btnDeleteNewsItem;

        public NewsItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textNewsTitle = itemView.findViewById(R.id.text_news_manager_title);
            textNewsDescriptionSummary = itemView.findViewById(R.id.text_news_manager_description_summary);
            textNewsSourceDate = itemView.findViewById(R.id.text_news_manager_source_date);
            btnEditNewsItem = itemView.findViewById(R.id.btn_edit_news_item);
            btnDeleteNewsItem = itemView.findViewById(R.id.btn_delete_news_item);
        }
    }
}
