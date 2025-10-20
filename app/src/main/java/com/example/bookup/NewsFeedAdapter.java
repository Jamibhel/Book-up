package com.example.bookup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Will need Glide for image loading
import com.example.bookup.models.NewsItem;

import java.util.List;

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.NewsViewHolder> {

    private List<NewsItem> newsList;
    private OnNewsItemClickListener listener;

    public interface OnNewsItemClickListener {
        void onNewsItemClick(NewsItem item);
    }

    public void setOnNewsItemClickListener(OnNewsItemClickListener listener) {
        this.listener = listener;
    }

    public NewsFeedAdapter(List<NewsItem> newsList) {
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_feed, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem currentItem = newsList.get(position);

        holder.title.setText(currentItem.getTitle());
        holder.description.setText(currentItem.getDescription());
        holder.source.setText("Source: " + currentItem.getSource());

        if (currentItem.getImageUrl() != null && !currentItem.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentItem.getImageUrl())
                    .placeholder(R.drawable.ic_dashboard_banner_placeholder) // Use your placeholder image
                    .error(R.drawable.ic_dashboard_banner_placeholder) // Fallback on error
                    .centerCrop()
                    .into(holder.imageView);
            holder.imageView.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNewsItemClick(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title;
        TextView description;
        TextView source;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.news_item_image);
            title = itemView.findViewById(R.id.news_item_title);
            description = itemView.findViewById(R.id.news_item_description);
            source = itemView.findViewById(R.id.news_item_source);
        }
    }
}
