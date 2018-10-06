package unimelb.comp90018_instaviewer.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.fragments.PhotoFragment;

public class HomeActivity extends AppCompatActivity {
    private FragmentManager fm = getSupportFragmentManager();
    private PhotoFragment photoFragment = new PhotoFragment();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_user_feed:
                    return true;
                case R.id.navigation_search:
                    return true;
                case R.id.navigation_post:
                    fm.beginTransaction().replace(R.id.layoutSelectPhotoFragment, photoFragment).commit();
                    return true;
                case R.id.navigation_activity_feed:
                    return true;
                case R.id.navigation_profile:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}
