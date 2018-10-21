package unimelb.comp90018_instaviewer.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.models.FeedPost;
import unimelb.comp90018_instaviewer.adapters.FeedAdapter;

public class HomeFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<FeedPost> mDataset = new ArrayList<FeedPost>();
    private FirebaseFunctions mFunctions;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private static final String TAG = "USER_FEED";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        mRecyclerView = view.findViewById(R.id.comments_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mFunctions = FirebaseFunctions.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        getFeed(currentUser).addOnCompleteListener(new OnCompleteListener<ArrayList<HashMap>>() {

            @Override
            public void onComplete(@NonNull Task<ArrayList<HashMap>> task) {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        FirebaseFunctionsException.Code code = ffe.getCode();
                        Object details = ffe.getDetails();
                    }

                    Log.w(TAG, "getUserFeed:onFailure", e);
                    return;
                }
                writeDataSet(task.getResult());
                mAdapter = new FeedAdapter(mDataset, getContext());
                mRecyclerView.setAdapter(mAdapter);
            }
        });

//        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                getFeed(mAuth.getCurrentUser()).addOnCompleteListener(new OnCompleteListener<ArrayList<HashMap>>() {
//
//                    @Override
//                    public void onComplete(@NonNull Task<ArrayList<HashMap>> task) {
//                        if (!task.isSuccessful()) {
//                            Exception e = task.getException();
//                            if (e instanceof FirebaseFunctionsException) {
//                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
//                                FirebaseFunctionsException.Code code = ffe.getCode();
//                                Object details = ffe.getDetails();
//                            }
//
//                            Log.w(TAG, "getUserFeed:onFailure", e);
//                            return;
//                        }
//                        writeDataSet(task.getResult());
//                        ((FeedAdapter)mAdapter).clear();
//                        ((FeedAdapter)mAdapter).addAll(mDataset);
//                        swipeContainer.setRefreshing(false);
//                    }
//                });;
//            }
//        });

        // Inflate the layout for this fragment
        return view;
    }

    private void writeDataSet(ArrayList<HashMap> results) {
        mDataset.clear();
        for (HashMap entry : results) {
            HashMap post = (HashMap) entry.get("post");
            HashMap user = (HashMap) entry.get("user");
            mDataset.add(new FeedPost((String) post.get("userId"),
                    (String) entry.get("id"),
                    (String) user.get("name"),
                    (String) post.get("mediaLink"),
                    (String) post.get("caption"),
                    (int) entry.get("likesCount"),
                    (int) entry.get("commentsCount"),
                    (String) user.get("avatar")));
        }
    }

    private Task<ArrayList<HashMap>> getFeed(FirebaseUser user) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUid());

        return mFunctions
                .getHttpsCallable("getUserFeed")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, ArrayList<HashMap>>() {
                    @Override
                    public ArrayList<HashMap> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap result = (HashMap) task.getResult().getData();
                        ArrayList<HashMap> resultData = (ArrayList<HashMap>) result.get("data");

                        return resultData;
                    }
                });
    }
}
