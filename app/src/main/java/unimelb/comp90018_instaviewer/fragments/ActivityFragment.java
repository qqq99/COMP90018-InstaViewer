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
import unimelb.comp90018_instaviewer.adapters.ActivityAdapter;

public class ActivityFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<String> mDataset = new ArrayList<String>();
    private FirebaseFunctions mFunctions;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private static final String TAG = "ACTIVITY_FEED";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        mRecyclerView = view.findViewById(R.id.activity_feed);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mFunctions = FirebaseFunctions.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        getFeed(currentUser).addOnCompleteListener(new OnCompleteListener<ArrayList<String>>() {

            @Override
            public void onComplete(@NonNull Task<ArrayList<String>> task) {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        FirebaseFunctionsException.Code code = ffe.getCode();
                        Object details = ffe.getDetails();
                    }

                    Log.w(TAG, "getActivityFeed:onFailure", e);
                    return;
                }
                writeDataSet(task.getResult());
                mAdapter = new ActivityAdapter(mDataset, getContext());
                mRecyclerView.setAdapter(mAdapter);


            }
        });

        return view;
    }

    private void writeDataSet(ArrayList<String> results) {
        mDataset.clear();
        mDataset = results;
    }

    private Task<ArrayList<String>> getFeed(FirebaseUser user) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUid());

        return mFunctions
                .getHttpsCallable("getActivityFeed")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, ArrayList<String>>() {
                    @Override
                    public ArrayList<String> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap result = (HashMap) task.getResult().getData();
                        ArrayList<String> resultData = (ArrayList<String>) result.get("data");
                        return resultData;
                    }
                });
    }
}