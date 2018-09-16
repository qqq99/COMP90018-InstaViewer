package unimelb.comp90018_instaviewer.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
 
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
 
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
 
/**
 * Created by qxx1 on 2018/9/16.
 */
public class PhotoOrCropUtil {
 
    private static final String TAG = "PhotoOrCropUtil";
 
    private static final int PHOTO_REQUEST_GALLERY = 1;
    private static final int PHOTO_REQUEST_CAREMA = 2;
    private static final int PHOTO_REQUEST_CUT = 3;
    private static final String PHOTO_FILE_NAME = "image";
 
    private File tempFile = new File(Environment.getExternalStorageDirectory(), PHOTO_FILE_NAME);
    private Uri imageUri = null;
    private ArrayList<String> mSelectPath;
    private static PhotoOrCropUtil mInstance;
    private Context mContext;
    private PhotoOrCropListener mListener;
 
    public static synchronized PhotoOrCropUtil getInstance() {
        if (mInstance == null) {
            mInstance = new PhotoOrCropUtil();
        }
        return mInstance;
    }
 
    public void setContext(Context context) {
        mContext = context;
    }

    public void gallery() {
        Intent intent = new Intent(mContext, MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        if (mSelectPath != null && mSelectPath.size() > 0) {
            intent.putExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
        }
        ((Activity) mContext).startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    public void camera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (hasSdcard()) {
            Uri uri = Uri.fromFile(tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        ((Activity) mContext).startActivityForResult(intent, PHOTO_REQUEST_CAREMA);
    }

    private void crop(Uri uri) {
        Log.e(TAG, "tempFile:" + tempFile.toString());
        Log.e(TAG, "uri:" + uri.toString());
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        imageUri = Uri.fromFile(tempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        ((Activity) mContext).startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }
 
    public static boolean hasSdcard() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }
 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            if (data != null) {
                mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                StringBuilder sb = new StringBuilder();
                for (String p : mSelectPath) {
                    sb.append(p);
                }
                crop(Uri.parse("file://" + sb.toString()));
            }
        } else if (requestCode == PHOTO_REQUEST_CAREMA) {
            if (hasSdcard()) {
                crop(Uri.fromFile(tempFile));
            } else {
                showToast("未找到存储卡，无法存储照片！");
            }
        } else if (requestCode == PHOTO_REQUEST_CUT) {
            if (resultCode == ((Activity) mContext).RESULT_OK) {
                if (imageUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageUri);
                        mListener.uploadAvatar(bitmap);
                        imageUri = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                imageUri = null;
            }
            try {
                tempFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
 
    public void setPhotoOrCropListener(PhotoOrCropListener listener) {
        mListener = listener;
    }
 
    public interface PhotoOrCropListener {
        void uploadAvatar(Bitmap bitmap);
    }
 
    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}