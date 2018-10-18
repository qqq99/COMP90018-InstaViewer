package unimelb.comp90018_instaviewer.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.activities.PhotoActivity;
import unimelb.comp90018_instaviewer.activities.PhotoProcessActivity;
import unimelb.comp90018_instaviewer.utilities.PermissionUtil;
import unimelb.comp90018_instaviewer.utilities.PhotoOrCropUtil;

import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.MY_PERMISSIONS_REQUEST_CAMERA;
import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;
import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.checkCamera;
import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.checkWriteStorage;

public class PhotoFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        PhotoOrCropUtil.getInstance().setAlbumAndCameraContext(getActivity());

        Intent fromIntent = getActivity().getIntent();
        if (fromIntent != null) {
            String imageSavedPath = fromIntent.getStringExtra("imageSavedPath");
            if (imageSavedPath != null) {
                Toast.makeText(getActivity(),
                        "Processed image has been saved to path: " + imageSavedPath, Toast.LENGTH_LONG).show();
            }
        }

        PhotoOrCropUtil.getInstance().setAlbumAndCameraCallback(new PhotoOrCropUtil.PhotoOrCropListener() {
            @Override
            public void uploadAvatar(String imageFilePath) {
            if (imageFilePath != null) {
                Intent intent = new Intent(getActivity(), PhotoProcessActivity.class);
                intent.putExtra("imagePath", imageFilePath);
                startActivity(intent);
            }
            }
        });

        setButtonListeners(view);
        return view;
    }

    /**
     * Set listeners for image and camera button
     *
     * @param view layout view of fragment
     */
    private void setButtonListeners(View view) {
        view.findViewById(R.id.imageBtnCamera)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (PermissionUtil.checkWriteStorage(getActivity())
                                && PermissionUtil.checkCamera(getActivity())) {
                            PhotoOrCropUtil.getInstance().camera();
                        }
                    }
                });

        view.findViewById(R.id.imageBtnGallery)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (PermissionUtil.checkReadStorage(getActivity())) {
                            PhotoOrCropUtil.getInstance().album();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PhotoOrCropUtil.getInstance().album();
                } else {
                    Toast.makeText(getActivity(), "Read storage permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    Toast.makeText(getActivity(), "Camera permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    Toast.makeText(getActivity(), "Write permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        PhotoOrCropUtil.getInstance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Launches the camera (while ensuring that permissions required for camera are granted)
     */
    private void launchCamera() {
        if (checkCamera(getActivity()) && checkWriteStorage(getActivity())) {
            PhotoOrCropUtil.getInstance().camera();
        } else {
            Timber.d("Not all permissions are granted for using camera");
        }
    }
}
