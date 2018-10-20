package unimelb.comp90018_instaviewer.utilities;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

public class ProgressLoading {
    Activity activity;
    ProgressBar progressBar;

    public ProgressLoading(Activity activity, ConstraintLayout layout) {
        this.activity = activity;
        this.progressBar = new ProgressBar(activity,null,android.R.attr.progressBarStyleLarge);
        this.progressBar.setId(View.generateViewId());

        progressBar.setVisibility(View.INVISIBLE);
        layout.addView(progressBar, 0);

        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.connect(progressBar.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP,0);
        set.connect(progressBar.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT,0);
        set.connect(progressBar.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT,0);
        set.connect(progressBar.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM,0);
        set.applyTo(layout);
    }

    public void start() {
        progressBar.setVisibility(View.VISIBLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void stop() {
        progressBar.setVisibility(View.INVISIBLE);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
