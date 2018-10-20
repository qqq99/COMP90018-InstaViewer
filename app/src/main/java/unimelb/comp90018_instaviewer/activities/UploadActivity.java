package unimelb.comp90018_instaviewer.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.models.Callback;
import unimelb.comp90018_instaviewer.utilities.FirebaseUtil;
import unimelb.comp90018_instaviewer.utilities.LocationFinder;
import unimelb.comp90018_instaviewer.utilities.PermissionUtil;
import unimelb.comp90018_instaviewer.utilities.ProgressLoading;
import unimelb.comp90018_instaviewer.utilities.Redirection;

import static unimelb.comp90018_instaviewer.utilities.PermissionUtil.MY_PERMISSIONS_REQUEST_FINE_LOCATION;

public class UploadActivity extends AppCompatActivity {
    EditText editTextCaption;
    ConstraintLayout mainLayout;
    ProgressLoading progressLoading;
    Switch locationSwitch;

    LocationFinder locationFinder;

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
        Bitmap finalImage = BitmapFactory.decodeFile(PhotoEditActivity.FINAL_IMAGE_PATH);
        Glide.with(UploadActivity.this).load(finalImage)
                .apply(RequestOptions.centerCropTransform())
                .into(previewImage);

        editTextCaption = findViewById(R.id.textViewCaption);
        mainLayout = findViewById(R.id.layoutMain);
        locationSwitch = findViewById(R.id.switchLocation);

        progressLoading = new ProgressLoading(UploadActivity.this, mainLayout);

        initLocationSwitch();
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
            uploadPost();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                if (resultCode == Activity.RESULT_OK) {
                    locationSwitch.setChecked(true);
                    initLocation();
                }
        }
    }

    private void uploadPost() {
        final String caption = editTextCaption.getText().toString();

        /* Prevent post with empty caption */
        if (caption.equals("")) {
            Toast.makeText(UploadActivity.this, "Please type in a caption!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressLoading.start();

        /* Upload image first to get its url, then upload the post */
        FirebaseUtil.uploadImage(PhotoEditActivity.FINAL_IMAGE_PATH, new Callback() {
            @Override
            public void onSuccess(Object o) {
                String imageUrl = o.toString();
                uploadPostToFirebase(caption, imageUrl);
                Timber.d("Successfully uploaded image, result: " + imageUrl);
            }

            @Override
            public void onFailure(Object o) {
                progressLoading.stop();
                Timber.e("Failed to upload image, result: " + o.toString());
            }
        });
    }

    private void uploadPostToFirebase(String caption, String imageUrl) {
        Timber.d("Uploading post to firebase...");
        Timber.d("Location == null: " + (locationFinder.getLocation() == null ));
        Timber.d("Location switch not checked: " + (!locationSwitch.isChecked()));

        Map<String, Object> data = new HashMap<>();
        data.put("userId", FirebaseAuth.getInstance().getUid());
        data.put("caption", caption);
        data.put("mediaLink", imageUrl);
        data.put("location", (locationFinder == null || !locationSwitch.isChecked()) ?
                new double[] {} :
                new double[] {locationFinder.getLat(), locationFinder.getLon()}
        );

        FirebaseUtil.uploadPost(caption, imageUrl, null)
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        progressLoading.stop();

                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();
                                String error = "Failed to upload post to Firebase, error: " + e.getMessage() + ", code: " + code.name();

                                Timber.e(error);
                                Toast.makeText(UploadActivity.this, error, Toast.LENGTH_LONG).show();
                            } else {
                                String error = "Failed to upload post to Firebase, error: " + e.getMessage();
                                Timber.e(error);
                                Toast.makeText(UploadActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String message = "Uploaded post successfully";
                            Timber.d(message);
                            Toast.makeText(UploadActivity.this, message, Toast.LENGTH_SHORT).show();
                            Redirection.redirectToHome(UploadActivity.this);
                        }
                    }
                });
    }

    private void initLocation() {
        locationFinder = new LocationFinder(UploadActivity.this);
//        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private void initLocationSwitch() {
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && PermissionUtil.checkFineLocation(UploadActivity.this)) {
                    initLocation();
                } else {
                    locationSwitch.setChecked(false);
                }
            }
        });

        /* Turn location switch on if location permission is allowed */
        if (ContextCompat.checkSelfPermission(UploadActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationSwitch.setChecked(true);
        } else {
            locationSwitch.setChecked(false);
        }
    }
}
