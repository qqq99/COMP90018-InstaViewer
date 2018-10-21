package unimelb.comp90018_instaviewer.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import unimelb.comp90018_instaviewer.adapters.CommentsAdapter;
import unimelb.comp90018_instaviewer.models.Comment;
import unimelb.comp90018_instaviewer.models.FeedPost;
import unimelb.comp90018_instaviewer.utilities.ProgressLoading;

public class CommentsActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button mSubmitBtn;
    private EditText mCommentText;
    private FirebaseFunctions mFunctions;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ArrayList<Comment> comments = new ArrayList<Comment>();
    private FeedPost post;
    private String postId;
    private static final String TAG = "CommentsActivity";

    private ProgressLoading progressLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        mSubmitBtn = findViewById(R.id.post_comment_btn);
        mCommentText = findViewById(R.id.comment_text);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mCommentText.getText().toString();
                submitComment(message).addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();
                                Object details = ffe.getDetails();
                            }
                            Toast.makeText(CommentsActivity.this, "Comment submission failed due to server error", Toast.LENGTH_LONG);
                            Log.w(TAG, "getUserFeed:onFailure", e);
                            return;
                        }
                        Toast.makeText(CommentsActivity.this, "Comment submitted!", Toast.LENGTH_LONG);
                        String result = task.getResult();
                        Log.d(TAG, result);
                    }
                });
            }
        });

        progressLoading = new ProgressLoading(CommentsActivity.this, (ConstraintLayout) findViewById(R.id.layoutComments));

        getIncomingIntent();
        if (postId == null) {
            finish();
            return;
        }

        mFunctions = FirebaseFunctions.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        mRecyclerView = findViewById(R.id.comments_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        getComments(postId).addOnCompleteListener(new OnCompleteListener<HashMap>() {
            @Override
            public void onComplete(@NonNull Task<HashMap> task) {
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
                writePost(task.getResult());
                writeComments(task.getResult());
                mAdapter = new CommentsAdapter(comments, getApplicationContext(), post);
                mRecyclerView.setAdapter(mAdapter);
            }
        });


    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra("postId")) {
            this.postId = getIntent().getStringExtra("postId");
        }
    }

    private void writePost(HashMap data) {
        HashMap postMap = (HashMap) data.get("post");
        HashMap user = (HashMap) data.get("user");
        this.post = new FeedPost((String) postMap.get("userId"),
                postId,
                (String) postMap.get("username"),
                (String) postMap.get("mediaLink"),
                (String) postMap.get("caption"),
                (int) data.get("likesCount"),
                comments.size(),
                (String) user.get("avatar"));
    }

    private void writeComments(HashMap data) {
        ArrayList<HashMap> commentList = (ArrayList<HashMap>) data.get("comments");
        if (commentList.isEmpty()) {
            return;
        }
        for (HashMap commentMap : commentList){
            HashMap comment = (HashMap) commentMap.get("comment");
            HashMap user = (HashMap) commentMap.get("user");

            comments.add(new Comment((String) comment.get("message"),
                    (String) comment.get("userId"),
                    ((Long)comment.get("timestamp")).toString(),
                    (String) user.get("name"),
                    (String) user.get("avatar")));
        }

    }

    private Task<HashMap> getComments(String postId) {
        Map<String, Object> data = new HashMap<>();
        data.put("postId", postId);

        return mFunctions
                .getHttpsCallable("getComments")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, HashMap>() {
                    @Override
                    public HashMap then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        HashMap result = (HashMap) task.getResult().getData();

                        return (HashMap) result.get("data");
                    }
                });
    }

    private Task<String> submitComment(String message) {
        progressLoading.start();
        mCommentText.clearComposingText();
        mCommentText.clearFocus();

        Map<String, Object> data = new HashMap<>();
        data.put("postId", postId);
        data.put("userId", currentUser.getUid());
        data.put("targetUserId", post.getUserId());
        data.put("message", message);

        return mFunctions
                .getHttpsCallable("createComment")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        progressLoading.stop();
                        Toast.makeText(CommentsActivity.this, "Your comment has been posted", Toast.LENGTH_SHORT).show();
                        recreate();

                        String result = (String) task.getResult().getData();

                        return result;
                    }
                });
    }
}
