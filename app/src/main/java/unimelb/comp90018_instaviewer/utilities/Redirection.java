package unimelb.comp90018_instaviewer.utilities;

import android.app.Activity;
import android.content.Intent;

import unimelb.comp90018_instaviewer.activities.HomeActivity;

public class Redirection {
    public static void redirectToHome(Activity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }
}
