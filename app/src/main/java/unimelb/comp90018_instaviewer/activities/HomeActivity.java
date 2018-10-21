package unimelb.comp90018_instaviewer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.fragments.HomeFragment;
import unimelb.comp90018_instaviewer.fragments.PhotoFragment;
import unimelb.comp90018_instaviewer.fragments.ProfileFragment;
import unimelb.comp90018_instaviewer.utilities.LocationFinder;
import unimelb.comp90018_instaviewer.fragments.SearchFragment;

public class HomeActivity extends AppCompatActivity {
    private FragmentManager fm = getSupportFragmentManager();
    private SearchFragment searchFragment = new SearchFragment();
    private static final String TAG = "HOME";

    private MenuItem menuSearch;
    private SearchView searchView;

    private ProfileFragment profileFragment = new ProfileFragment();


    Toolbar toolbar;
    BottomNavigationView navigation;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            invalidateOptionsMenu();
            switch (item.getItemId()) {
                case R.id.navigation_user_feed:
                    getSupportActionBar().setTitle("Instaviewer");
                    fm.beginTransaction()
                            .replace(R.id.homeLayoutFragment, new HomeFragment()) // TODO: Do not instantiate new one every time (after refresh is added)
                            .commit();
                    return true;
                case R.id.navigation_search:
                    getSupportActionBar().setTitle("Search");
                    fm.beginTransaction()
                            .replace(R.id.homeLayoutFragment, searchFragment)
                            .commit();
                    return true;
                case R.id.navigation_post:
                    startActivity(new Intent(HomeActivity.this, SelectPhotoActivity.class));
                    return true;
                case R.id.navigation_activity_feed:
                    getSupportActionBar().setTitle("Activities");
                    return true;
                case R.id.navigation_profile:
                    getSupportActionBar().setTitle("Profile");
                    fm.beginTransaction().replace(R.id.homeLayoutFragment, profileFragment).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /* Set up toolbar */
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Instaviewer");

        fm.beginTransaction()
                .add(R.id.homeLayoutFragment, new HomeFragment())
                .commit();
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Timber.d("Current selected: " + navigation.getMenu().findItem(navigation.getSelectedItemId()).getItemId());
        Timber.d("Navigation search: " + R.id.navigation_search);

        if (navigation.getMenu().findItem(navigation.getSelectedItemId()).getItemId() == R.id.navigation_search) {
            getMenuInflater().inflate(R.menu.search, menu);
            menuSearch = menu.findItem(R.id.action_search);
            searchView = (SearchView) menuSearch.getActionView();
            searchView.setQueryHint("Search users");

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchView.clearFocus();
//                    searchView.onActionViewCollapsed();

                    searchFragment.updateSearchText(query);

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        } else {
            toolbar.getMenu().clear();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
