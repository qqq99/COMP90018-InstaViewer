package unimelb.comp90018_instaviewer.adapters;

import android.content.Context;
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
import unimelb.comp90018_instaviewer.models.FeedPost;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private ArrayList<FeedPost> mDataset;
    private Context mContext;


    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView userName;
        ImageView picture;
        TextView postMessage;
        ImageView likeIcon;
        ImageView userIcon;
        TextView comments;

        public FeedViewHolder(View v) {
            super(v);
            userName = v.findViewById(R.id.feed_post_user);
            picture = v.findViewById(R.id.feed_post_image);
            postMessage = v.findViewById(R.id.feed_post_message);
            likeIcon = v.findViewById(R.id.feed_post_like);
            userIcon = v.findViewById(R.id.feed_post_owner_icon);
            comments = v.findViewById(R.id.feed_post_comments);
        }
    }

    public FeedAdapter(ArrayList<FeedPost> myDataset, Context context) {
        this.mDataset = myDataset;
        this.mContext = context;

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FeedViewHolder holder, int position) {
        FeedPost currentPost = mDataset.get(position);
        holder.userName.setText(currentPost.getmOwnerName());
        holder.postMessage.setText(currentPost.getmMessage());
        int numberOfComments = currentPost.getmComments();
        if (numberOfComments > 0) {
            holder.comments.setText(String.format("View all %d comments", numberOfComments));
        } else {
            holder.comments.setText("No comments");
        }
        Glide.with(mContext)
                .load(currentPost.getmImageUrl())
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
}
