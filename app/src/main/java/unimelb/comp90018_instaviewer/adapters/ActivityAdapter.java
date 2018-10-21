package unimelb.comp90018_instaviewer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import unimelb.comp90018_instaviewer.R;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>{
    private ArrayList<String> mDataset;
    private Context mContext;
    private static final String TAG = "ActivityAdapter";


    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView message;

        public ActivityViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.activity_message);
        }
    }

    public ActivityAdapter(ArrayList<String> myDataset, Context context) {
        this.mDataset = myDataset;
        this.mContext = context;

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ActivityAdapter.ActivityViewHolder holder, int position) {
        String currentMessage = mDataset.get(position);
        holder.message.setText(currentMessage);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ActivityAdapter.ActivityViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.feed_activity, parent, false);
        ActivityAdapter.ActivityViewHolder vh = new ActivityAdapter.ActivityViewHolder(v);
        return vh;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
