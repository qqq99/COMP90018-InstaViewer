package unimelb.comp90018_instaviewer.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.activities.UploadActivity;
import unimelb.comp90018_instaviewer.activities.WifiDirectActivity;
import unimelb.comp90018_instaviewer.adapters.SearchedUsersAdapter;
import unimelb.comp90018_instaviewer.models.User;
import unimelb.comp90018_instaviewer.utilities.FirebaseUtil;
import unimelb.comp90018_instaviewer.utilities.ProgressLoading;
import unimelb.comp90018_instaviewer.utilities.Redirection;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {
    RecyclerView recyclerView;
    SearchedUsersAdapter adapter;

    ProgressLoading progressLoading;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        Button wifiFeatureButton = view.findViewById(R.id.btnShowWifiFeature);
        recyclerView = view.findViewById(R.id.recyclerViewSearchedsUsers);

        wifiFeatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchWifiActivity();
            }
        });

        progressLoading = new ProgressLoading(getActivity(), (ConstraintLayout) view.findViewById(R.id.layoutSearch));
        initRecyclerView();

        return view;
    }

    public void updateSearchText(String text) {
        Timber.d("Updated search query: " + text);

        progressLoading.start();
        updateRecyclerView(text);
    }

    private void initRecyclerView() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new SearchedUsersAdapter(new ArrayList<User>(), getActivity());
        recyclerView.setAdapter(adapter);
    }

    private void updateRecyclerView(String text) {
        FirebaseUtil.searchUsers(text)
                .addOnCompleteListener(new OnCompleteListener<HashMap>() {
                    @Override
                    public void onComplete(@NonNull Task<HashMap> task) {
                        progressLoading.stop();

                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();
                                String error = "Failed to search user from Firebase, error: " + e.getMessage() + ", code: " + code.name();

                                Timber.e(error);
                                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            } else {
                                String error = "Failed to search user from Firebase, error: " + e.getMessage();
                                Timber.e(error);
                                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            HashMap result = task.getResult();
                            ArrayList data = (ArrayList) result.get("data");
                            ArrayList<User> myDataset = new ArrayList<>();

                            /* Update found users */
                            for (Object o: data) {
                                myDataset.add(new User((HashMap) o));
                            }
                            adapter.swap(myDataset);

                            String message = "Found users: " + data.size();
                            Timber.d(message);
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void launchWifiActivity() {
        getActivity().startActivity(new Intent(getActivity(), WifiDirectActivity.class));
    }
}
