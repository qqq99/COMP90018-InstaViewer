package unimelb.comp90018_instaviewer.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.utilities.PhotoOrCropUtil;

public class PhotoActivity extends AppCompatActivity {
    private Button mBtn1;
    private Button mBtn2;
    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);


        mBtn1 = (Button) findViewById(R.id.button1);
        mBtn2 = (Button) findViewById(R.id.button2);
        mImage = (ImageView) findViewById(R.id.img);

        PhotoOrCropUtil.getInstance().setContext(this);
        PhotoOrCropUtil.getInstance().setPhotoOrCropListener(new PhotoOrCropUtil.PhotoOrCropListener() {
            @Override
            public void uploadAvatar(Bitmap bitmap) {
                mImage.setImageBitmap(bitmap);
            }
        });

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
                PhotoOrCropUtil.getInstance().camera();
                break;
            case R.id.button2:
                PhotoOrCropUtil.getInstance().gallery();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PhotoOrCropUtil.getInstance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

}
