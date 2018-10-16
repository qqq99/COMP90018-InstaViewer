package unimelb.comp90018_instaviewer.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.fragments.CameraFragment;
import unimelb.comp90018_instaviewer.fragments.GalleryFragment;
import unimelb.comp90018_instaviewer.utilities.PhotoOrCropUtil;

import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.checkCamera;
import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.checkWriteStorage;

public class SelectPhotoActivity extends AppCompatActivity implements GalleryFragment.OnGalleryImageSelectedListener {
//    public static final int REQUEST_CODE_CAMERA = 111;
//    public static final int REQUEST_CODE_READ_STORAGE = 222;
//    public static final int REQUEST_CODE_WRITE_STORAGE = 333;

    public static final int REQUEST_CODE_GET_CAMERA_PHOTO = 1;

    private FragmentManager fm = getSupportFragmentManager();
    private GalleryFragment galleryFragment = new GalleryFragment();
    private CameraFragment cameraFragment = new CameraFragment();
    private ImageView galleryImagePreview;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private String gallerySelectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo);

        /* Set up tabs and view pager for fragments */
        tabLayout = findViewById(R.id.tabLayoutSelectPhoto);
        viewPager = findViewById(R.id.viewPagerSelectPhoto);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        /* Set up toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.navigation_next) {
            Timber.d("Going to edit image with path: " + gallerySelectedImage);
            Intent intent = new Intent(SelectPhotoActivity.this, UploadActivity.class);
            intent.putExtra(UploadActivity.UPLOAD_IMAGE_EXTRA, gallerySelectedImage);
            startActivity(intent);
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGalleryImageSelected(String imagePath) {
        gallerySelectedImage = imagePath;
        Timber.i("Loading preview image of path: " + imagePath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Timber.d("Select photo activity code: " + requestCode);

        switch (requestCode) {
            case REQUEST_CODE_GET_CAMERA_PHOTO:
                Timber.d("Camera result code: " + resultCode);

                Bitmap photo1 = (Bitmap) data.getExtras().get("data");

                Uri tempUri1 = getImageUri(getApplicationContext(), photo1);
                Timber.d("Camera photo1: " + getRealPathFromURI(tempUri1));

                if (resultCode == Activity.RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");

                    Uri tempUri = getImageUri(getApplicationContext(), photo);
                    Timber.d("Camera photo: " + getRealPathFromURI(tempUri));
                }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        /* TODO: Check for permission when accessing photo library */
        adapter.addFragment(galleryFragment, "Library");
        adapter.addFragment(cameraFragment, "Photo");

        viewPager.setAdapter(adapter);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPathFromURI(Uri uri) {
        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
