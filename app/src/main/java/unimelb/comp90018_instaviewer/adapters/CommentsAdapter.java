package unimelb.comp90018_instaviewer.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.models.Comment;
import unimelb.comp90018_instaviewer.models.FeedPost;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Comment> comments = new ArrayList<Comment>();
    private FeedPost post;
    private Context mContext;
    private static final int TYPE_POST = 0;
    private static final int TYPE_COMMENT = 1;

    public CommentsAdapter(ArrayList<Comment> comments, Context mContext, FeedPost feedPost) {
        this.comments = comments;
        this.mContext = mContext;
        this.post = feedPost;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        TextView cUserName;
        ImageView cPicture;
        TextView cPostMessage;

        public CommentViewHolder(@NonNull View v) {
            super(v);
            cUserName = v.findViewById(R.id.commentName);
            cPicture = v.findViewById(R.id.commentPicture);
            cPostMessage = v.findViewById(R.id.commentMessage);
        }
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView postMessage;
        TextView likes;
        ImageView picture;
        ImageView likeIcon;
        ImageView userIcon;
        public PostViewHolder(@NonNull View v) {
            super(v);
            userName = v.findViewById(R.id.comment_post_user);
            picture = v.findViewById(R.id.comment_post_image);
            postMessage = v.findViewById(R.id.comment_post_message);
            likeIcon = v.findViewById(R.id.comment_post_like_btn);
            userIcon = v.findViewById(R.id.comment_post_owner_icon);
            likes = v.findViewById(R.id.comment_post_likes);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_POST) {
            View v = LayoutInflater.from(mContext)
                    .inflate(R.layout.image_post, parent, false);
            return new PostViewHolder(v);
        } else {
            View v = LayoutInflater.from(mContext)
                    .inflate(R.layout.comment_post, parent, false);
            return new CommentViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof PostViewHolder) {
            PostViewHolder postView = (PostViewHolder) viewHolder;
            postView.userName.setText(post.getOwnerName());
            postView.postMessage.setText(post.getMessage());

            postView.likes.setText(String.format("%d likes", post.getnLikes()));
            Glide.with(mContext)
                    .load(post.getImageUrl())
                    .apply(new RequestOptions()
                            .fitCenter())
                    .into(postView.picture);

            if (post.getAvatar() != null) {
                Glide.with(mContext)
                        .load(post.getAvatar())
                        .into(postView.userIcon);
            }

        } else if (viewHolder instanceof CommentViewHolder) {
            CommentViewHolder commentView = (CommentViewHolder) viewHolder;
            Comment currentComment = getComment(i);
            commentView.cUserName.setText(currentComment.getUsername());
            commentView.cPostMessage.setText(currentComment.getMessage());
            Glide.with(mContext)
                    .load(currentComment.getAvatar())
                    .into(commentView.cPicture);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isFirstPosition(position)) {
            return TYPE_POST;
        }
        return TYPE_COMMENT;
    }

    @Override
    public int getItemCount() {
        return comments.size() + 1;
    }

    private Comment getComment(int position){
        return comments.get(position-1);
    }

    private boolean isFirstPosition(int position){
        return position == 0;
    }

}
