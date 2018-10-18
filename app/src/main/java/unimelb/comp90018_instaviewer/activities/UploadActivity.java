package unimelb.comp90018_instaviewer.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.models.Callback;
import unimelb.comp90018_instaviewer.utilities.FirebaseUtil;
import unimelb.comp90018_instaviewer.utilities.Redirection;

public class UploadActivity extends AppCompatActivity {

    public static final String UPLOAD_IMAGE_EXTRA = "Upload image path";

    String imagePathToUpload;
    Bitmap imageToUpload;

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
//        imagePathToUpload = getIntent().getStringExtra(UPLOAD_IMAGE_EXTRA);
        imageToUpload = getIntent().getParcelableExtra(UPLOAD_IMAGE_EXTRA);
//        Glide.with(UploadActivity.this).load(imagePathToUpload)
        Glide.with(UploadActivity.this).load(imageToUpload)
                .apply(RequestOptions.centerCropTransform())
                .into(previewImage);
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
        FirebaseUtil.uploadImage(imagePathToUpload, new Callback() {
            @Override
            public void onSuccess(Object o) {
                Redirection.redirectToHome(UploadActivity.this);
                Timber.d("Successfully uploaded image, result: " + o.toString());
            }

            @Override
            public void onFailure(Object o) {
                Timber.d("Failed to upload image, result: " + o.toString());
            }
        });
    }

}
