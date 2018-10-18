package unimelb.comp90018_instaviewer.utilities;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView userName;
        public ImageView picture;
        public TextView postMessage;

        public CommentViewHolder(View v) {
            super(v);

        }
    }

    @NonNull
    @Override
    public CommentsAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.CommentViewHolder commentViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


}
