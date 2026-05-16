package com.example.bookup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookup.R;
import com.example.bookup.models.NewsItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DashboardNewsAdapter extends RecyclerView.Adapter<DashboardNewsAdapter.ViewHolder> {
    private List<NewsItem> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private String userId;

    public interface OnItemClickListener {
        void onItemClick(NewsItem item);
        void onLikeClick(NewsItem item);
    }

    public DashboardNewsAdapter(String userId, OnItemClickListener listener) {
        this.userId = userId;
        this.listener = listener;
    }

    public void setItems(List<NewsItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_feed, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsItem item = items.get(position);
        holder.title.setText(item.getAuthorName());
        holder.description.setText(item.getContent());
        
        holder.btnLike.setText(String.valueOf(item.getLikesCount()));
        boolean liked = item.isLikedByUser(userId);
        holder.btnLike.setIconResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        holder.btnLike.setIconTintResource(liked ? R.color.error : R.color.colorOnSurfaceVariant);

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView).load(item.getImageUrl()).centerCrop().into(holder.image);
            holder.image.setVisibility(View.VISIBLE);
        } else {
            holder.image.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.btnLike.setOnClickListener(v -> listener.onLikeClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, description;
        MaterialButton btnLike;
        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.news_item_image);
            title = itemView.findViewById(R.id.news_item_title);
            description = itemView.findViewById(R.id.news_item_description);
            btnLike = itemView.findViewById(R.id.btn_like_dashboard);
        }
    }
}
