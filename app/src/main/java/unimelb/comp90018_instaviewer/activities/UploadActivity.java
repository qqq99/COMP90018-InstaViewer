package unimelb.comp90018_instaviewer.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.models.Callback;
import unimelb.comp90018_instaviewer.utilities.FirebaseUtil;
import unimelb.comp90018_instaviewer.utilities.ProgressLoading;
import unimelb.comp90018_instaviewer.utilities.Redirection;

public class UploadActivity extends AppCompatActivity {
    EditText editTextCaption;
    ConstraintLayout mainLayout;
    ProgressBar progressBar;
    ProgressLoading progressLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        /* Set up toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        /* Initialize preview of image to upload */
        ImageView previewImage = findViewById(R.id.imgUploadPreview);
        Bitmap finalImage = BitmapFactory.decodeFile(PhotoEditActivity.FINAL_IMAGE_PATH);
        Glide.with(UploadActivity.this).load(finalImage)
                .apply(RequestOptions.centerCropTransform())
                .into(previewImage);

        editTextCaption = findViewById(R.id.textViewCaption);
        mainLayout = findViewById(R.id.layoutMain);

        progressLoading = new ProgressLoading(UploadActivity.this, mainLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.navigation_share) {
            uploadPost();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void uploadPost() {
        final String caption = editTextCaption.getText().toString();

        /* Prevent post with empty caption */
        if (caption.equals("")) {
            Toast.makeText(UploadActivity.this, "Please type in a caption!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressLoading.start();

        /* Upload image first to get its url, then upload the post */
        FirebaseUtil.uploadImage(PhotoEditActivity.FINAL_IMAGE_PATH, new Callback() {
            @Override
            public void onSuccess(Object o) {
                String imageUrl = o.toString();
                uploadPostToFirebase(caption, imageUrl);
                Timber.d("Successfully uploaded image, result: " + imageUrl);
            }

            @Override
            public void onFailure(Object o) {
                progressLoading.stop();
                Timber.e("Failed to upload image, result: " + o.toString());
            }
        });
    }

    private void uploadPostToFirebase(String caption, String imageUrl) {
        Timber.d("Uploading post to firebase...");

        Map<String, Object> data = new HashMap<>();
        data.put("userId", FirebaseAuth.getInstance().getUid());
        data.put("caption", caption);
        data.put("mediaLink", imageUrl);
//        data.put("location", new Float[] {});

        FirebaseUtil.uploadPost(caption, imageUrl, null)
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        progressLoading.stop();

                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();
                                String error = "Failed to upload post to Firebase, error: " + e.getMessage() + ", code: " + code.name();

                                Timber.e(error);
                                Toast.makeText(UploadActivity.this, error, Toast.LENGTH_LONG).show();
                            } else {
                                String error = "Failed to upload post to Firebase, error: " + e.getMessage();
                                Timber.e(error);
                                Toast.makeText(UploadActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String message = "Uploaded post successfully";
                            Timber.d(message);
                            Toast.makeText(UploadActivity.this, message, Toast.LENGTH_SHORT).show();
                            Redirection.redirectToHome(UploadActivity.this);
                        }
                    }
                });
    }
}
