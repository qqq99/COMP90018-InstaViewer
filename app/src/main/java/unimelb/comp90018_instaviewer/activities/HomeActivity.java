package unimelb.comp90018_instaviewer.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.fragments.HomeFragment;
import unimelb.comp90018_instaviewer.fragments.PhotoFragment;

public class HomeActivity extends AppCompatActivity {
    private FragmentManager fm = getSupportFragmentManager();
    private PhotoFragment photoFragment = new PhotoFragment();
    private static final String TAG = "HOME";


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_user_feed:
                    fm.beginTransaction()
                            .replace(R.id.homeLayoutFragment, new HomeFragment())
                            .commit();
                    return true;
                case R.id.navigation_search:
                    startActivity(new Intent(HomeActivity.this, WifiDirectActivity.class));
                    return true;
                case R.id.navigation_post:
                    startActivity(new Intent(HomeActivity.this, SelectPhotoActivity.class));
                    return true;
                case R.id.navigation_activity_feed:
                    return true;
                case R.id.navigation_profile:
                    return true;
            }
            return false;
        }
    };

//    @Override
//    public void onStart(){
//        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        Log.i(TAG, "Logged in as: " + currentUser.getDisplayName());
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /* Set up toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        fm.beginTransaction()
                .add(R.id.homeLayoutFragment, new HomeFragment())
                .commit();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}
