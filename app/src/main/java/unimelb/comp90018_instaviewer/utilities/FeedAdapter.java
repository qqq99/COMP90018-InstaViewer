package unimelb.comp90018_instaviewer.utilities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.models.FeedPost;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private ArrayList<FeedPost> mDataset;

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView userName;
        public ImageView picture;
        public TextView postMessage;

        public FeedViewHolder(View v) {
            super(v);

        }
    }

    public FeedAdapter(ArrayList<FeedPost> myDataset) {
        this.mDataset = myDataset;

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FeedViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.mTextView.setText(mDataset[position]);

    }

    // Create new views (invoked by the layout manager)
    @Override
    public FeedAdapter.FeedViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
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
