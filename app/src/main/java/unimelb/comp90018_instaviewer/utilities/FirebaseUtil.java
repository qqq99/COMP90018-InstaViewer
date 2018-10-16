package unimelb.comp90018_instaviewer.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.models.Callback;

public class FirebaseUtil {
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
                Timber.d("Failed to upload image: " + e.getMessage());
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
                Timber.d("Failed to get url: " + e.getMessage());
                callback.onFailure(e);
            }
        });
    }
}
