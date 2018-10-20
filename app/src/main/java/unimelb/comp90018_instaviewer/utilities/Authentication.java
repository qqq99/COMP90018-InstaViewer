package unimelb.comp90018_instaviewer.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.constants.FirebaseApi;

public class Authentication implements Runnable {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String TAG = "AUTHENTICATION";

    private Context context;
    private FirebaseUser user;
    private FirebaseFunctions mFunctions;

    public Authentication(Context myContext, FirebaseUser user) {
        this.context = myContext;
        this.user = user;
        mFunctions = FirebaseFunctions.getInstance();
    }

    private Task<String> addUser(FirebaseUser user) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUid());
        data.put("name", user.getDisplayName());

        return mFunctions
                .getHttpsCallable(FirebaseApi.createUser)
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }

    private void updateLoginStatus(boolean status) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.login_status), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (status) {
            editor.putBoolean(context.getString(R.string.login_status), true);
            editor.commit();
        } else {
            editor.putBoolean(context.getString(R.string.login_status), false);
            editor.commit();
        }
    }


    @Override
    public void run() {
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        updateLoginStatus(true);
                        Log.i(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.i(TAG, "No such document");
                        addUser(user).addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Exception e = task.getException();
                                    if (e instanceof FirebaseFunctionsException) {
                                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                        FirebaseFunctionsException.Code code = ffe.getCode();
                                        Object details = ffe.getDetails();
                                        Log.e(TAG, "Failed with Firebase Functions Exception");
                                    }
                                    updateLoginStatus(false);
                                    Log.e(TAG, "User failed to be created");
                                } else {
                                    updateLoginStatus(true);
                                    Log.i(TAG, "User created successfully");
                                }
                            }
                        });
                    }
                } else {
                    updateLoginStatus(false);
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }
}
