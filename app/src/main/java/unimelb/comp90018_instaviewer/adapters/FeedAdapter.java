package unimelb.comp90018_instaviewer.adapters;

import android.content.Context;
import android.content.Intent;
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
import unimelb.comp90018_instaviewer.activities.CommentsActivity;
import unimelb.comp90018_instaviewer.models.FeedPost;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private ArrayList<FeedPost> mDataset;
    private Context mContext;
    private static final String TAG = "FeedAdapter";


    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView userName;
        TextView postMessage;
        TextView comments;
        TextView likes;
        ImageView picture;
        ImageView likeIcon;
        ImageView userIcon;
        ImageView commentIcon;


        public FeedViewHolder(View v) {
            super(v);
            userName = v.findViewById(R.id.feed_post_user);
            picture = v.findViewById(R.id.feed_post_image);
            postMessage = v.findViewById(R.id.feed_post_message);
            likeIcon = v.findViewById(R.id.feed_post_like_btn);
            userIcon = v.findViewById(R.id.feed_post_owner_icon);
            comments = v.findViewById(R.id.feed_post_comments);
            likes = v.findViewById(R.id.feed_post_likes);
            commentIcon = v.findViewById(R.id.feed_post_comment_btn);
        }
    }

    public FeedAdapter(ArrayList<FeedPost> myDataset, Context context) {
        this.mDataset = myDataset;
        this.mContext = context;

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FeedViewHolder holder, final int position) {

        FeedPost currentPost = mDataset.get(position);
        holder.userName.setText(currentPost.getOwnerName());
        holder.postMessage.setText(currentPost.getMessage());
        int numberOfComments = currentPost.getnComments();
        if (numberOfComments > 0) {
            holder.comments.setText(String.format("View all %d comments", numberOfComments));
        } else {
            holder.comments.setText("No comments");
        }
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FeedPost currentPost = mDataset.get(position);
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postId", currentPost.getPostId());
                mContext.startActivity(intent);
            }
        });

        holder.likes.setText(String.format("%d likes", currentPost.getnLikes()));
        Glide.with(mContext)
                .load(currentPost.getImageUrl())
                .apply(new RequestOptions()
                .fitCenter())
                .into(holder.picture);

    }

    // Create new views (invoked by the layout manager)
    @Override
    public FeedAdapter.FeedViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.feed_post, parent, false);
        FeedViewHolder vh = new FeedViewHolder(v);
        return vh;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void clear(){
        mDataset.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<FeedPost> data) {
        mDataset.addAll(data);
        notifyDataSetChanged();
    }
}
