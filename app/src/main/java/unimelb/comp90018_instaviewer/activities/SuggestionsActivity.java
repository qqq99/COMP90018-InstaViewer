package unimelb.comp90018_instaviewer.activities;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.adapters.SearchedUsersAdapter;
import unimelb.comp90018_instaviewer.models.User;
import unimelb.comp90018_instaviewer.utilities.FirebaseUtil;
import unimelb.comp90018_instaviewer.utilities.ProgressLoading;
import unimelb.comp90018_instaviewer.utilities.Redirection;

public class SuggestionsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    SearchedUsersAdapter adapter;

    ProgressLoading progressLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        /* Set up toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerViewSuggestedUsers);
        progressLoading = new ProgressLoading(SuggestionsActivity.this, (ConstraintLayout) findViewById(R.id.layoutSuggestions));
        initRecyclerView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(SuggestionsActivity.this));

        progressLoading.start();

        FirebaseUtil.getUserSuggestions()
                .addOnCompleteListener(new OnCompleteListener<HashMap>() {
                    @Override
                    public void onComplete(@NonNull Task<HashMap> task) {
                        progressLoading.stop();

                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();
                                String error = "Failed to get user suggestions, error: " + e.getMessage() + ", code: " + code.name();

                                Timber.e(error);
                                Toast.makeText(SuggestionsActivity.this, error, Toast.LENGTH_LONG).show();
                            } else {
                                String error = "Failed to get user suggestions, error: " + e.getMessage();
                                Timber.e(error);
                                Toast.makeText(SuggestionsActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String message = "Retrieved user suggestions";
                            Timber.d(message);
                            Toast.makeText(SuggestionsActivity.this, message, Toast.LENGTH_SHORT).show();

                            HashMap result = task.getResult();
                            ArrayList data = (ArrayList) result.get("data");
                            ArrayList<User> myDataset = new ArrayList<>();

                            /* Update found users */
                            for (Object o: data) {
                                myDataset.add(new User((HashMap) o));
                            }

                            adapter = new SearchedUsersAdapter(myDataset, SuggestionsActivity.this);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });
    }
}
