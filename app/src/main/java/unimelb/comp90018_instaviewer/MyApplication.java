package unimelb.comp90018_instaviewer;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import com.awen.camera.CameraApplication;

/**
 * Created by qxx1 on 2018/9/16.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }

        CameraApplication.init(this,true);
    }
}
