package unimelb.comp90018_instaviewer.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.activities.SelectPhotoActivity;
import unimelb.comp90018_instaviewer.utilities.PhotoOrCropUtil;

import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.checkCamera;
import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.checkWriteStorage;

public class CameraFragment extends Fragment {
    private boolean isViewInitialized = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        view.findViewById(R.id.btn_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        isViewInitialized = true;

        return view;
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser && isViewInitialized) {
//            launchCamera();
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    /**
     * Launches the camera (while ensuring that permissions required for camera are granted)
     */
    private void launchCamera() {
        if (checkCamera(getActivity()) && checkWriteStorage(getActivity())) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            getActivity().startActivityForResult(intent, SelectPhotoActivity.REQUEST_CODE_GET_CAMERA_PHOTO);
        } else {
            Timber.d("Not all permissions are granted for using camera");
        }
    }
}
