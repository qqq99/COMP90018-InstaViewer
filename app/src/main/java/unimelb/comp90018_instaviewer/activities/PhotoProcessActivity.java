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
import android.graphics.drawable.BitmapDrawable;
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
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.utilities.ImageFilters;
import unimelb.comp90018_instaviewer.utilities.PhotoOrCropUtil;

public class PhotoProcessActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 124;
    private Button btn1, btn2, btn3;
    private Button cropImage, resetImage, doneWithImage;

    private ImageView imageView;
    private Bitmap oriBitmap;
    private ImageFilters imgFilter;

    private Bitmap intermediateBitmap;
    private String oriImagePath;
    private SeekBar saturationSeekBar, brightnessSeekBar, contrastSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_process);

        Intent fromIntent = getIntent();
        this.oriImagePath = fromIntent.getStringExtra("imagePath");

        this.initialize();
        this.addEventListener();

        PhotoOrCropUtil.getInstance().setCropContext(this);
        PhotoOrCropUtil.getInstance().setCropCallback(new PhotoOrCropUtil.PhotoOrCropListener() {
            @Override
            public void uploadAvatar(String imageFilePath) {
                Toast.makeText(PhotoProcessActivity.this, "from crop", Toast.LENGTH_SHORT).show();
                Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
                intermediateBitmap = bitmap;
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    private void initialize() {
        imgFilter = new ImageFilters();
        btn1 = findViewById(R.id.filter1);
        btn2 = findViewById(R.id.filter2);
        btn3 = findViewById(R.id.filter3);

        this.cropImage = findViewById(R.id.crop_image);
        this.resetImage = findViewById(R.id.reset_image);
        this.doneWithImage = findViewById(R.id.done);

        this.saturationSeekBar = findViewById(R.id.saturationSeekbar);
        this.brightnessSeekBar = findViewById(R.id.brightnessSeekbar);
        this.contrastSeekBar = findViewById(R.id.contrastSeekbar);

        this.oriBitmap = BitmapFactory.decodeFile(this.oriImagePath);
        this.intermediateBitmap = oriBitmap;
        this.imageView = findViewById(R.id.IvImage);
        this.imageView.setImageBitmap(this.oriBitmap);
    }

    private void filter(String type) {
        if (!type.equals("")) {
            Toast.makeText(PhotoProcessActivity.this, "converting to " + type, Toast.LENGTH_SHORT).show();
        }

        Bitmap bitmap;
        Bitmap old = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        switch (type) {
            case "snow":
                bitmap = imgFilter.applySnowEffect(old);
                break;
            case "black":
                bitmap = imgFilter.applyBlackFilter(old);
                break;
            case "flea":
                bitmap = imgFilter.applyFleaEffect(old);
                break;
            default:
                bitmap = oriBitmap;
                break;
        }
        intermediateBitmap = bitmap;
        imageView.setImageBitmap(bitmap);
    }

    private void addEventListener() {
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter("snow");
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter("black");
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter("flea");
            }
        });

        resetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter("");
                brightnessSeekBar.setProgress(127);
                contrastSeekBar.setProgress(63);
                saturationSeekBar.setProgress(100);
            }
        });

        cropImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage(imageView);
            }
        });

        doneWithImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = PhotoOrCropUtil.getInstance().saveImageToAlbum(imageView);
                Intent intent = new Intent(PhotoProcessActivity.this, PhotoActivity.class);
                intent.putExtra("imageSavedPath", path);
                startActivity(intent);
                PhotoProcessActivity.this.finish();
            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage(imageView);
            }
        });

        saturationSeekBar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar arg0, int progress,
                                                  boolean fromUser) {
                        Bitmap bmp = Bitmap.createBitmap(intermediateBitmap.getWidth(), intermediateBitmap.getHeight(),
                                Bitmap.Config.ARGB_8888);
                        ColorMatrix cMatrix = new ColorMatrix();
                        cMatrix.setSaturation((float) (progress / 100.0));

                        Paint paint = new Paint();
                        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

                        Canvas canvas = new Canvas(bmp);
                        canvas.drawBitmap(intermediateBitmap, 0, 0, paint);
                        imageView.setImageBitmap(bmp);
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
                        Bitmap bmp = Bitmap.createBitmap(intermediateBitmap.getWidth(), intermediateBitmap.getHeight(),
                                Bitmap.Config.ARGB_8888);
                        int brightness = progress - 127;
                        ColorMatrix cMatrix = new ColorMatrix();
                        cMatrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1,
                                0, 0, brightness,
                                0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});

                        Paint paint = new Paint();
                        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

                        Canvas canvas = new Canvas(bmp);
                        canvas.drawBitmap(intermediateBitmap, 0, 0, paint);
                        imageView.setImageBitmap(bmp);
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
                        Bitmap bmp = Bitmap.createBitmap(intermediateBitmap.getWidth(), intermediateBitmap.getHeight(),
                                Bitmap.Config.ARGB_8888);
                        float contrast = (float) ((progress + 64.0) / 128.0);
                        ColorMatrix cMatrix = new ColorMatrix();
                        cMatrix.set(new float[] { contrast, 0, 0, 0, 0, 0,
                                contrast, 0, 0, 0,
                                0, 0, contrast, 0, 0, 0, 0, 0, 1, 0 });

                        Paint paint = new Paint();
                        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

                        Canvas canvas = new Canvas(bmp);
                        canvas.drawBitmap(intermediateBitmap, 0, 0, paint);
                        imageView.setImageBitmap(bmp);
                    }

                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

    }

    public void cropImage(View view) {
        if (checkPermissionWRITE_EXTERNAL_STORAGE(this)) {
            PhotoOrCropUtil.getInstance().cropImage(imageView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PhotoOrCropUtil.getInstance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PhotoOrCropUtil.getInstance().cropImage(imageView);
                } else {
                    Toast.makeText(PhotoProcessActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

}
