package unimelb.comp90018_instaviewer.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;

public class UploadActivity extends AppCompatActivity {

    public static final String UPLOAD_IMAGE_EXTRA = "Upload image path";

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
        String imagePathToUpload = getIntent().getStringExtra(UPLOAD_IMAGE_EXTRA);
        Glide.with(UploadActivity.this).load(imagePathToUpload)
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
