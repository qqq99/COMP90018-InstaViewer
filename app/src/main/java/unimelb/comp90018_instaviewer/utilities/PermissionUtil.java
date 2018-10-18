package unimelb.comp90018_instaviewer.utilities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * This class helps check and request for device permissions
 */
public class PermissionUtil {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 124;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 125;

    /**
     * General permission checking function
     *
     * @param context activity context
     * @param permission permission type
     * @param requestMessage message to display when requesting for permission
     * @param permissionResultCode code number for identifying result of permission request
     * @return true if permission is granted, false otherwise
     */
    public static boolean checkPermission(Context context, String permission, String requestMessage,
                                          int permissionResultCode) {
        try {
            int currentAPIVersion = Build.VERSION.SDK_INT;

            /* Only ask for permission for Android OS Marshmallow and above (APK 23) */
            if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {

                /* Check if permission is granted and request if not */
                if (ContextCompat.checkSelfPermission(context,
                        permission) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            (Activity) context, permission)) {
                        showDialog(requestMessage, context, permission, permissionResultCode);
                    } else {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{permission}, permissionResultCode);
                    }
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks for write external storage permission
     *
     * @param context activity context
     * @return true if permission granted, false otherwise
     */
    public static boolean checkCamera(Context context) {
        String message = "We need read permission to access your camera.";
        return checkPermission(context, Manifest.permission.CAMERA, message,
                MY_PERMISSIONS_REQUEST_CAMERA);
    }

    /**
     * Checks for read external storage permission
     *
     * @param context activity context
     * @return true if permission granted, false otherwise
     */
    public static boolean checkReadStorage(Context context) {
        String message = "We need read permission to access your photos.";
        return checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE, message,
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    /**
     * Checks for write external storage permission
     *
     * @param context activity context
     * @return true if permission granted, false otherwise
     */
    public static boolean checkWriteStorage(Context context) {
        String message = "We need read permission to access your photos.";
        return checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, message,
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Dialog for requesting permission
     *
     * @param msg message for requesting permission
     * @param context activity context
     * @param permission name of permission requested
     * @param permissionResultCode code number for identifying result of permission request
     */
    private static void showDialog(final String msg, final Context context, final String permission,
                           final int permissionResultCode) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg);
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{permission},
                                permissionResultCode);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}
