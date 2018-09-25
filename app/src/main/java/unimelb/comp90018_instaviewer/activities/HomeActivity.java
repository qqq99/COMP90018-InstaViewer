package unimelb.comp90018_instaviewer.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import unimelb.comp90018_instaviewer.R;

public class HomeActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_user_feed:
                    mTextMessage.setText(R.string.nav_user_feed);
                    return true;
                case R.id.navigation_search:
                    mTextMessage.setText(R.string.nav_search);
                    return true;
                case R.id.navigation_post:
                    mTextMessage.setText(R.string.nav_post);
                    return true;
                case R.id.navigation_activity_feed:
                    mTextMessage.setText(R.string.nav_activity_feed);
                    return true;
                case R.id.navigation_profile:
                    mTextMessage.setText(R.string.nav_profile);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}
