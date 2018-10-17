//package unimelb.comp90018_instaviewer.utilities;
//
//import android.support.annotation.NonNull;
//import android.util.Log;
//
//import com.google.android.gms.tasks.Continuation;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.*;
//import com.google.firebase.functions.FirebaseFunctions;
//import com.google.firebase.functions.FirebaseFunctionsException;
//import com.google.firebase.functions.HttpsCallableResult;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class Authentication implements Runnable {
//
//    private FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private CollectionReference users = db.collection("users");
//
//    private static final String TAG = "AUTHENTICATION";
//
//    private FirebaseUser user;
//    private FirebaseFunctions mFunctions;
//
//
//    private void authenticate(){
//        db.collection("user").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if(task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                    } else {
//                        Log.d(TAG, "No such document");
//                        user.getDisplayName();
//                        //TODO Create user in database
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
//    }
//
//    private Task<String> addUser(FirebaseUser user){
//        Map<String, Object> data = new HashMap<>();
//        data.put("userId", user.getUid());
//        data.put("name", user.getDisplayName());
//
//        return mFunctions
//                .getHttpsCallable("createUser")
//                .call(data)
//                .continueWith(new Continuation<HttpsCallableResult, String>() {
//                    @Override
//                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
//                        // This continuation runs on either success or failure, but if the task
//                        // has failed then getResult() will throw an Exception which will be
//                        // propagated down.
//                        String result = (String) task.getResult().getData();
//                        return result;
//                    }
//                });
//    }
//
//    public void setAuthDetails(FirebaseUser user){
//        this.user = user;
//    }
//
//    @Override
//    public void run() {
//        mFunctions = FirebaseFunctions.getInstance();
//        db.collection("user").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if(task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                    } else {
//                        Log.d(TAG, "No such document");
//
//                        addUser(user).addOnCompleteListener(new OnCompleteListener<String>() {
//                            @Override
//                            public void onComplete(@NonNull Task<String> task) {
//                                if (!task.isSuccessful()) {
//                                    Exception e = task.getException();
//                                    if (e instanceof FirebaseFunctionsException) {
//                                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
//                                        FirebaseFunctionsException.Code code = ffe.getCode();
//                                        Object details = ffe.getDetails();
//                                    }
//                                    Log.d(TAG, "User failed to be created");
//                                } else {
//                                    Log.d(TAG, "User created successfully");
//                                }
//                            }
//                        });
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
//    }
//}
