package unimelb.comp90018_instaviewer.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.activities.UploadActivity;
import unimelb.comp90018_instaviewer.constants.FirebaseApi;
import unimelb.comp90018_instaviewer.models.Callback;

public class FirebaseUtil {
    public static Task<String> uploadPost(String caption, String imageUrl, float[] location) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", FirebaseAuth.getInstance().getUid());
        data.put("caption", caption);
        data.put("mediaLink", imageUrl);
        //        data.put("location", new Float[] {});

        return FirebaseFunctions.getInstance()
                .getHttpsCallable(FirebaseApi.createPost)
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }

    public static void uploadImage(String path, final Callback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        /* Make filename */
        String userId = auth.getCurrentUser().getUid();
        Long currentTime = System.currentTimeMillis();
        String uploadFilePath = userId + "-" + currentTime + ".jpg";
        final StorageReference storageRef = storage.getReference().child(uploadFilePath);

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                getDownloadUrl(storageRef, callback);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Timber.e("Failed to upload image: " + e.getMessage());
                callback.onFailure(e);
            }
        });
    }

    private static void getDownloadUrl(StorageReference storageRef, final Callback callback) {
        storageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloadUrl = task.getResult();
                Timber.d("Upload url: " + downloadUrl);
                callback.onSuccess(downloadUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Timber.e("Failed to get url: " + e.getMessage());
                callback.onFailure(e);
            }
        });
    }
}
