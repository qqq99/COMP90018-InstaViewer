package unimelb.comp90018_instaviewer.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.adapters.CommentsAdapter;
import unimelb.comp90018_instaviewer.models.Comment;

public class CommentsActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseFunctions mFunctions;
    private ArrayList<Comment> comments = new ArrayList<Comment>();
    private String postId;
    private static final String TAG = "CommentsActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getIncomingIntent();
        if (postId == null){
            finish();
            return;
        }
        
        mFunctions = FirebaseFunctions.getInstance();

        mRecyclerView = findViewById(R.id.comments_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        getComments(postId).addOnCompleteListener(new OnCompleteListener<ArrayList<HashMap>>() {
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
            }
        });
        mAdapter = new CommentsAdapter(comments, this);
        mRecyclerView.setAdapter(mAdapter);

    }

    private void getIncomingIntent(){
        if(getIntent().hasExtra("postId")) {
            this.postId = getIntent().getStringExtra("postId");
        }
    }

    private Task<ArrayList<HashMap>> getComments(String postId) {
        Map<String, Object> data = new HashMap<>();
        data.put("postId", postId);

        return mFunctions
                .getHttpsCallable("getComments")
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
