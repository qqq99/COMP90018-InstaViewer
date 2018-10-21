package unimelb.comp90018_instaviewer.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.models.Comment;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private ArrayList<Comment> comments = new ArrayList<Comment>();
    private Context mContext;

    public CommentsAdapter(ArrayList<Comment> comments, Context mContext) {
        this.comments = comments;
        this.mContext = mContext;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView userName;
        public ImageView picture;
        public TextView postMessage;

        public CommentViewHolder(View v) {
            super(v);
            userName = v.findViewById(R.id.commentName);
            this.picture = v.findViewById(R.id.commentPicture);
            this.postMessage = v.findViewById(R.id.commentMessage);
        }
    }

    @NonNull
    @Override
    public CommentsAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.feed_post, parent, false);
        CommentsAdapter.CommentViewHolder vh = new CommentsAdapter.CommentViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.CommentViewHolder commentViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }


}
