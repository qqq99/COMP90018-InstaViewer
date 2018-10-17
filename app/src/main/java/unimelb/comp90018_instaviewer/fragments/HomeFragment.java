package unimelb.comp90018_instaviewer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.models.FeedPost;
import unimelb.comp90018_instaviewer.utilities.FeedAdapter;

public class HomeFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<FeedPost> mDataset;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_feed, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new FeedAdapter(mDataset, getContext());
        mRecyclerView.setAdapter(mAdapter);

        // Inflate the layout for this fragment
        return mRecyclerView;
    }
}
