package unimelb.comp90018_instaviewer.adapters;

import android.app.Activity;
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
import unimelb.comp90018_instaviewer.models.User;

public class SearchedUsersAdapter extends RecyclerView.Adapter<SearchedUsersAdapter.MyViewHolder> {
    private ArrayList<User> mDataset;
    private Activity mActivity;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ImageView mImageView;

        public MyViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.textSearchedUser);
            mImageView = v.findViewById(R.id.imgSearchUser);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SearchedUsersAdapter(ArrayList<User> myDataset, Activity activity) {
        mDataset = myDataset;
        mActivity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SearchedUsersAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.searched_user_list, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = mDataset.get(position);
        holder.mTextView.setText(user.getName());
        Glide.with(mActivity)
                .applyDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.drawable.ic_person_black_24dp)
                )
                .load(user.getAvatar())
                .apply(RequestOptions.centerCropTransform())
                .into(holder.mImageView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void swap(ArrayList<User> mDataset) {
        this.mDataset = mDataset;
        notifyDataSetChanged();
    }
}
