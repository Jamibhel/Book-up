package com.example.bookup.adapters;

import android.content.Context;
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
import com.example.bookup.models.Comment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private final Context context;
    private List<Comment> commentsList = new ArrayList<>();
    private OnCommentActionListener listener;
    private final String currentUserId = FirebaseAuth.getInstance().getUid();

    public interface OnCommentActionListener {
        void onUserProfileClick(String userId);
        void onLikeCommentClick(Comment comment);
        void onReplyCommentClick(Comment comment);
        void onDeleteCommentClick(Comment comment);
    }

    public void setOnCommentActionListener(OnCommentActionListener listener) {
        this.listener = listener;
    }

    public CommentsAdapter(Context context) {
        this.context = context;
    }

    public void setComments(List<Comment> comments) {
        this.commentsList = comments != null ? comments : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        holder.bind(comment, currentUserId, context, listener);
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage, likeIndicator;
        TextView userName, commentText, time, likeBtn, replyBtn, likeCount, replyReference;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.comment_user_image);
            userName = itemView.findViewById(R.id.comment_user_name);
            commentText = itemView.findViewById(R.id.comment_text);
            time = itemView.findViewById(R.id.text_comment_time);
            likeBtn = itemView.findViewById(R.id.btn_like_comment);
            replyBtn = itemView.findViewById(R.id.btn_reply_comment);
            likeCount = itemView.findViewById(R.id.text_comment_like_count);
            likeIndicator = itemView.findViewById(R.id.img_comment_like_indicator);
            replyReference = itemView.findViewById(R.id.text_reply_reference);
        }

        public void bind(Comment comment, String currentUserId, Context context, OnCommentActionListener listener) {
            userName.setText(comment.getUserName());
            commentText.setText(comment.getText());
            
            Glide.with(context)
                    .load(comment.getUserImageUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(userImage);

            if (comment.getTimestamp() != null) {
                time.setText(DateUtils.getRelativeTimeSpanString(comment.getTimestamp().getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
            }

            // Liked state
            boolean isLiked = comment.getLikedBy() != null && comment.getLikedBy().contains(currentUserId);
            likeBtn.setTextColor(context.getColor(isLiked ? R.color.error : R.color.colorOnSurfaceVariant));
            likeIndicator.setVisibility(comment.getLikeCount() > 0 ? View.VISIBLE : View.GONE);
            likeCount.setText(comment.getLikeCount() > 0 ? String.valueOf(comment.getLikeCount()) : "");

            // Reply Reference
            if (comment.getReplyToName() != null && !comment.getReplyToName().isEmpty()) {
                replyReference.setVisibility(View.VISIBLE);
                replyReference.setText("Replying to " + comment.getReplyToName());
            } else {
                replyReference.setVisibility(View.GONE);
            }

            if (listener != null) {
                userImage.setOnClickListener(v -> listener.onUserProfileClick(comment.getUserId()));
                likeBtn.setOnClickListener(v -> listener.onLikeCommentClick(comment));
                replyBtn.setOnClickListener(v -> listener.onReplyCommentClick(comment));
                itemView.setOnLongClickListener(v -> {
                    if (comment.getUserId().equals(currentUserId)) listener.onDeleteCommentClick(comment);
                    return true;
                });
            }
        }
    }
}
