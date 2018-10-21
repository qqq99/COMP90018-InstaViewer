package unimelb.comp90018_instaviewer.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.activities.WifiDirectActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {


    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        Button wifiFeatureButton = view.findViewById(R.id.btnShowWifiFeature);

        wifiFeatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchWifiActivity();
            }
        });

        return view;
    }

    private void launchWifiActivity() {
        getActivity().startActivity(new Intent(getActivity(), WifiDirectActivity.class));
    }
}
