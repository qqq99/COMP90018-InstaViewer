package unimelb.comp90018_instaviewer.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.awen.camera.model.PermissionsModel;
import com.awen.camera.view.TakePhotoActivity;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.utilities.PhotoOrCropUtil;

public class PhotoActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 124;
    public static final int MAX_WIDTH = 100;
    public static final int MAX_HEIGHT = 100;

    private Button fromCameraBtn;
    private Button fromAlbumBtn;
    private Bitmap bitmap;
    private int imageHeight;
    private int imageWidth;
    private ImageView mImage;
    private SeekBar saturationSeekBar, brightnessSeekBar, contrastSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        PermissionsModel permissionsModel = new PermissionsModel(this);
        permissionsModel.checkCameraPermission(new PermissionsModel.PermissionListener() {
            @Override
            public void onPermission(boolean isPermission) {
                if (isPermission) {
                    Intent intent = new Intent(PhotoActivity.this, TakePhotoActivity.class);
                    startActivityForResult(intent, TakePhotoActivity.REQUEST_CAPTRUE_CODE);
                }
            }
        });


        fromCameraBtn = findViewById(R.id.fromCamera);
        fromAlbumBtn = findViewById(R.id.fromAlbum);
        mImage = findViewById(R.id.img);
        saturationSeekBar = findViewById(R.id.saturationSeekbar);
        brightnessSeekBar = findViewById(R.id.brightnessSeekbar);
        contrastSeekBar = findViewById(R.id.contrastSeekbar);

        PhotoOrCropUtil.getInstance().setContext(this);
        PhotoOrCropUtil.getInstance().setPhotoOrCropListener(new PhotoOrCropUtil.PhotoOrCropListener() {
            @Override
            public void uploadAvatar(String imageFilePath) {
                mImage.setImageBitmap(resizeBitmap(imageFilePath));
                findViewById(R.id.operation).setVisibility(View.VISIBLE);
            }
        });

        addEventListener();
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions(
                            (Activity) context,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public boolean checkPermissionWRITE_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat.requestPermissions(
                            (Activity) context,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{permission},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fromCamera:
                PhotoOrCropUtil.getInstance().camera();
                break;
            case R.id.fromAlbum:
                if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
                    PhotoOrCropUtil.getInstance().album();
                }
                break;
        }
    }

    public void cropImage(View view) {
        if (checkPermissionWRITE_EXTERNAL_STORAGE(this)) {
            PhotoOrCropUtil.getInstance().cropImage(mImage);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PhotoOrCropUtil.getInstance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PhotoOrCropUtil.getInstance().album();
                } else {
                    Toast.makeText(PhotoActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PhotoOrCropUtil.getInstance().cropImage(mImage);
                } else {
                    Toast.makeText(PhotoActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    private Bitmap resizeBitmap(String imageFilePath) {
        try {
            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = true;

            int wRatio = (int) Math.ceil(ops.outWidth / (float) MAX_WIDTH);
            int hRatio = (int) Math.ceil(ops.outHeight / (float) MAX_HEIGHT);

            if (wRatio > 1 && hRatio > 1) {
                if (wRatio > hRatio) {
                    ops.inSampleSize = wRatio;
                } else {
                    ops.inSampleSize = hRatio;
                }
            }
            ops.inJustDecodeBounds = false;

            this.bitmap = BitmapFactory.decodeFile(imageFilePath, ops);
            this.imageHeight = this.bitmap.getHeight();
            this.imageWidth = this.bitmap.getWidth();
            return this.bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void addEventListener() {
        saturationSeekBar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar arg0, int progress,
                                                  boolean fromUser) {
                        Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight,
                                Bitmap.Config.ARGB_8888);
                        ColorMatrix cMatrix = new ColorMatrix();
                        cMatrix.setSaturation((float) (progress / 100.0));

                        Paint paint = new Paint();
                        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

                        Canvas canvas = new Canvas(bmp);
                        canvas.drawBitmap(bitmap, 0, 0, paint);
                        mImage.setImageBitmap(bmp);
                    }

                    public void onStartTrackingTouch(SeekBar bar) {
                    }

                    public void onStopTrackingTouch(SeekBar bar) {
                    }
                });

        brightnessSeekBar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar arg0, int progress,
                                                  boolean fromUser) {
                        Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight,
                                Bitmap.Config.ARGB_8888);
                        int brightness = progress - 127;
                        ColorMatrix cMatrix = new ColorMatrix();
                        cMatrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1,
                                0, 0, brightness,
                                0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});

                        Paint paint = new Paint();
                        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

                        Canvas canvas = new Canvas(bmp);
                        canvas.drawBitmap(bitmap, 0, 0, paint);
                        mImage.setImageBitmap(bmp);
                    }

                    public void onStartTrackingTouch(SeekBar bar) {
                    }

                    public void onStopTrackingTouch(SeekBar bar) {
                    }
                });

        contrastSeekBar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar arg0, int progress,
                                                  boolean fromUser) {
                        Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight,
                                Bitmap.Config.ARGB_8888);
                        float contrast = (float) ((progress + 64) / 128.0);
                        ColorMatrix cMatrix = new ColorMatrix();
                        cMatrix.set(new float[] { contrast, 0, 0, 0, 0, 0,
                                contrast, 0, 0, 0,
                                0, 0, contrast, 0, 0, 0, 0, 0, 1, 0 });

                        Paint paint = new Paint();
                        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

                        Canvas canvas = new Canvas(bmp);
                        canvas.drawBitmap(bitmap, 0, 0, paint);
                        mImage.setImageBitmap(bmp);
                    }

                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
    }

}
