package unimelb.comp90018_instaviewer.utilities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
    private static PhotoOrCropUtil UTIL_INSTANCE = new PhotoOrCropUtil();

    private Context mContext;
    private File tempFile = new File(Environment.getExternalStorageDirectory(), PHOTO_FILE_NAME);
    private Uri imageUri = null;
    private ArrayList<String> mSelectPath;
    private PhotoOrCropListener mListener;

    private PhotoOrCropUtil() {}

    public static PhotoOrCropUtil getInstance() {
        return UTIL_INSTANCE;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void album() {
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
        /**
         * Check whether flash is enabled
         */
        if (!this.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(this.mContext, "Flash is not support in this device.", Toast.LENGTH_SHORT).show();
        }

        /**
         * The Intent for camera doesn't support for flash control, please refer to:
         * https://stackoverflow.com/questions/19667094/intent-does-not-set-the-camera-parameters
         * After testing on my own cellphones, it's true that flash mode can be changed on devices
         * that support flash functionality
         */
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (hasSdcard()) {
            Uri uri = Uri.fromFile(tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        ((Activity) mContext).startActivityForResult(intent, PHOTO_REQUEST_CAREMA);
    }

    private void crop(Uri uri, int w, int h) {
        int windowSize = w < h ? w : h;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", windowSize);
        intent.putExtra("outputY", windowSize);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        imageUri = Uri.fromFile(tempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        ((Activity) mContext).startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    public void cropImage(ImageView imageView) {
        Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        imageView.draw(canvas);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(this.tempFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            this.crop(Uri.fromFile(this.tempFile), imageView.getWidth(), imageView.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                this.mListener.uploadAvatar(this.getRealFilePath(this.mContext, Uri.parse("file://" + sb.toString())));
            }
        } else if (requestCode == PHOTO_REQUEST_CAREMA) {
            if (hasSdcard()) {
                Uri uri = Uri.fromFile(tempFile);
                this.mListener.uploadAvatar(this.getRealFilePath(this.mContext, uri));
            } else {
                showToast("No storage card found, image cannot be saved.");
            }
        } else if (requestCode == PHOTO_REQUEST_CUT) {
            if (resultCode == ((Activity) mContext).RESULT_OK) {
                if (imageUri != null) {
                    try {
                        mListener.uploadAvatar(this.getRealFilePath(mContext, imageUri));
                        imageUri = null;
                    } catch (Exception e) {
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

    public String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String pathStr = null;
        if (scheme == null)
            pathStr = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            pathStr = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver()
                    .query(uri, new String[]{MediaStore.Images.ImageColumns.DATA},
                            null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        pathStr = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return pathStr;
    }

    public interface PhotoOrCropListener {
        void uploadAvatar(String imageFilePath);
    }

    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}