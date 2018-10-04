package unimelb.comp90018_instaviewer.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.utilities.PermissionUtil;
import unimelb.comp90018_instaviewer.utilities.PhotoOrCropUtil;

import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.MY_PERMISSIONS_REQUEST_CAMERA;
import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;
import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.checkCamera;
import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.checkWriteStorage;

public class PhotoActivity extends AppCompatActivity {
    private final String TAG = "PhotoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        PhotoOrCropUtil.getInstance().setAlbumAndCameraContext(this);

        Intent fromIntent = getIntent();
        if (fromIntent != null) {
            String imageSavedPath = fromIntent.getStringExtra("imageSavedPath");
            if (imageSavedPath != null) {
                Toast.makeText(PhotoActivity.this,
                        "Processed image has been saved to path: " + imageSavedPath, Toast.LENGTH_LONG).show();
            }
        }

        PhotoOrCropUtil.getInstance().setAlbumAndCameraCallback(new PhotoOrCropUtil.PhotoOrCropListener() {
            @Override
            public void uploadAvatar(String imageFilePath) {
                if (imageFilePath != null) {
                    Intent intent = new Intent(PhotoActivity.this, PhotoProcessActivity.class);
                    intent.putExtra("imagePath", imageFilePath);
                    startActivity(intent);
                    PhotoActivity.this.finish();
                }
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fromCamera:
                if (PermissionUtil.checkWriteStorage(this)
                        && PermissionUtil.checkCamera(this)) {
                    PhotoOrCropUtil.getInstance().camera();
                }
                break;
            case R.id.fromAlbum:
                if (PermissionUtil.checkReadStorage(this)) {
                    PhotoOrCropUtil.getInstance().album();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PhotoOrCropUtil.getInstance().album();
                } else {
                    Toast.makeText(PhotoActivity.this, "Read storage permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    Toast.makeText(PhotoActivity.this, "Camera permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    Toast.makeText(PhotoActivity.this, "Write permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PhotoOrCropUtil.getInstance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Launches the camera (while ensuring that permissions required for camera are granted)
     */
    private void launchCamera() {
        if (checkCamera(this) && checkWriteStorage(this)) {
            PhotoOrCropUtil.getInstance().camera();
        } else {
            Log.i(TAG, "Not all permissions are granted for using camera");
        }
    }
}
