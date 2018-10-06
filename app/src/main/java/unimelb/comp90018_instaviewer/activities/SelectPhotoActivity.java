package unimelb.comp90018_instaviewer.activities;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.fragments.GalleryFragment;
import unimelb.comp90018_instaviewer.utilities.PhotoOrCropUtil;

import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.checkCamera;
import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.checkWriteStorage;

public class SelectPhotoActivity extends AppCompatActivity implements GalleryFragment.OnGalleryImageSelectedListener {
    private FragmentManager fm = getSupportFragmentManager();
    private GalleryFragment galleryFragment = new GalleryFragment();
    private ImageView galleryImagePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo);

        galleryImagePreview = findViewById(R.id.imageGalleryPreview);

        fm.beginTransaction().replace(R.id.layoutSelectPhotoFragment, galleryFragment).commit();
    }


    @Override
    public void onGalleryImageSelected(String imagePath) {
        Timber.i("Loading preview image of path: " + imagePath);
        Glide.with(SelectPhotoActivity.this).load(imagePath)
                .apply(RequestOptions.centerCropTransform())
                .into(galleryImagePreview);
    }

    /**
     * Launches the camera (while ensuring that permissions required for camera are granted)
     */
    private void launchCamera() {
        if (checkCamera(this) && checkWriteStorage(this)) {
            PhotoOrCropUtil.getInstance().camera();
        } else {
            Timber.d("Not all permissions are granted for using camera");
        }
    }
}
