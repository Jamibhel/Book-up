package com.example.bookup.adapters;

import android.graphics.Color;
import android.text.format.DateUtils;
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
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NewsFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ADMIN = 0;
    private static final int VIEW_TYPE_SOCIAL = 1;

    private List<NewsItem> newsList = new ArrayList<>();
    private final OnNewsItemClickListener listener;
    private final String currentUserId;

    public interface OnNewsItemClickListener {
        void onNewsItemClick(NewsItem item);
        void onLikeClick(NewsItem item);
        void onCommentClick(NewsItem item);
        void onShareClick(NewsItem item);
    }

    public NewsFeedAdapter(String currentUserId, OnNewsItemClickListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setItems(List<NewsItem> items) {
        this.newsList = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return newsList.get(position).isPriority() ? VIEW_TYPE_ADMIN : VIEW_TYPE_SOCIAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADMIN) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
            return new AdminViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_social, parent, false);
            return new SocialViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NewsItem item = newsList.get(position);
        
        if (holder instanceof AdminViewHolder) {
            ((AdminViewHolder) holder).bind(item, currentUserId, listener);
        } else if (holder instanceof SocialViewHolder) {
            ((SocialViewHolder) holder).bind(item, currentUserId, listener);
        }
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    // --- VIEW HOLDERS ---

    public static class AdminViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView imageView;
        TextView authorName, time, content;
        MaterialButton btnLike, btnComment;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            imageView = itemView.findViewById(R.id.post_image);
            authorName = itemView.findViewById(R.id.post_author_name);
            time = itemView.findViewById(R.id.post_time);
            content = itemView.findViewById(R.id.post_content);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnComment = itemView.findViewById(R.id.btn_comment);
        }

        public void bind(NewsItem item, String userId, OnNewsItemClickListener listener) {
            authorName.setText(item.getAuthorName() + " (Official)");
            content.setText(item.getContent());
            btnLike.setText(String.valueOf(item.getLikesCount()));
            btnComment.setText(String.valueOf(item.getComments() != null ? item.getComments().size() : 0));

            boolean liked = item.isLikedByUser(userId);
            btnLike.setIconResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            btnLike.setIconTintResource(liked ? R.color.error : R.color.colorOnSurfaceVariant);

            if (item.getTimestamp() != null) {
                time.setText(DateUtils.getRelativeTimeSpanString(item.getTimestamp().getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
            }

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView).load(item.getImageUrl()).centerCrop().into(imageView);
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onNewsItemClick(item));
            btnLike.setOnClickListener(v -> listener.onLikeClick(item));
            btnComment.setOnClickListener(v -> listener.onCommentClick(item));
        }
    }

    public static class SocialViewHolder extends RecyclerView.ViewHolder {
        TextView authorName, time, content;
        MaterialButton btnLike, btnComment;
        ImageView avatar;

        public SocialViewHolder(@NonNull View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.post_author_name);
            time = itemView.findViewById(R.id.post_time);
            content = itemView.findViewById(R.id.post_content);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnComment = itemView.findViewById(R.id.btn_comment);
            avatar = itemView.findViewById(R.id.post_author_avatar);
        }

        public void bind(NewsItem item, String userId, OnNewsItemClickListener listener) {
            authorName.setText(item.getAuthorName());
            content.setText(item.getContent());
            btnLike.setText(String.valueOf(item.getLikesCount()));
            btnComment.setText(String.valueOf(item.getComments() != null ? item.getComments().size() : 0));

            boolean liked = item.isLikedByUser(userId);
            btnLike.setIconResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            btnLike.setIconTintResource(liked ? R.color.error : R.color.colorOnSurfaceVariant);

            if (item.getTimestamp() != null) {
                time.setText(DateUtils.getRelativeTimeSpanString(item.getTimestamp().getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
            }

            itemView.setOnClickListener(v -> listener.onNewsItemClick(item));
            btnLike.setOnClickListener(v -> listener.onLikeClick(item));
            btnComment.setOnClickListener(v -> listener.onCommentClick(item));
        }
    }
}
