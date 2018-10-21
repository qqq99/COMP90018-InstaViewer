package unimelb.comp90018_instaviewer.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.models.ProfilePost;
import unimelb.comp90018_instaviewer.utilities.FirebaseUtil;

public class ProfileFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private ImageView avatarImage;
    private TextView userProfileName;
    private TextView postsCountText;
    private TextView followersCountText;
    private TextView followingCountText;

    private GridView postsGridList;
    private PostsAdapter postsAdapter;

    private ArrayList<ProfilePost> profilePosts = new ArrayList<>();

    public ProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        avatarImage = view.findViewById(R.id.imgAvatar);
        userProfileName = view.findViewById(R.id.textUsername);
        postsCountText = view.findViewById(R.id.textPostStats);
        followersCountText = view.findViewById(R.id.textFollowerStats);
        followingCountText = view.findViewById(R.id.textFollowingStats);

        postsAdapter = new PostsAdapter(getActivity());
        postsGridList = view.findViewById(R.id.gridProfilePosts);
        postsGridList.setAdapter(postsAdapter);
        postsGridList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (null != profilePosts && !profilePosts.isEmpty()) {
                    ProfilePost post = profilePosts.get(position);

                    Toast.makeText(getActivity(),"Post with id: " + post.getPostId(), Toast.LENGTH_SHORT).show();

//                    fragmentListener.onGalleryImageSelected(imagePath);
//                    Glide.with(getActivity()).load(imagePath)
//                            .into(galleryImagePreview
//                            );
                }
            }
        });

        loadProfile();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
    }

    private void loadProfile() {
        FirebaseUtil.getProfile()
                .addOnCompleteListener(new OnCompleteListener<HashMap>() {
                    @Override
                    public void onComplete(@NonNull Task<HashMap> task) {
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();
                                String error = "Failed to get profile from Firebase, error: " + e.getMessage() + ", code: " + code.name();

                                Timber.e(error);
                                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            } else {
                                String error = "Failed to get profile from Firebase, error: " + e.getMessage();
                                Timber.e(error);
                                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String message = "Successfully retrieved profile from Firebase";
                            Timber.d(message);

                            HashMap result = task.getResult();

                            HashMap data = (HashMap) result.get("data");
                            HashMap user = (HashMap) data.get("user");
                            ArrayList posts = (ArrayList) data.get("posts");

                            /* Get all of this user's posts */
                            profilePosts = new ArrayList<>();
                            for (Object post: posts) {
                                profilePosts.add(new ProfilePost((HashMap) post));
                            }

                            /* Initialize profile stats */
                            initProfileStats(user, posts);
                        }
                    }
                });
    }

    private void initProfileStats(HashMap user, ArrayList posts) {
        String mediaLink = (String) (user.get("mediaLink") != null ? user.get("mediaLink") : "");
        String name = (String) (user.get("name") != null ? user.get("name") : "");
        int postsCount = posts.size();
        int followersCount = user.get("followers") != null ?
                ((ArrayList)user.get("followers")).size() : 0;
        int followingCount = user.get("following") != null ?
                ((ArrayList)user.get("following")).size() : 0;

        Glide.with(getContext())
                .applyDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.drawable.ic_person_black_24dp)
                )
//                .load("https://firebasestorage.googleapis.com/v0/b/comp90018-instaviewer.appspot.com/o/SavrPq25IsTd5KKVg9pAFjKk6pg2-1540056292537.jpg?alt=media&token=0edc41cd-b57e-41d4-9335-d611afe99bf6")
                .load(mediaLink)
                .apply(RequestOptions.circleCropTransform())
                .into(avatarImage);
        userProfileName.setText(name);

        postsCountText.setText(String.valueOf(postsCount));
        followersCountText.setText(String.valueOf(followersCount));
        followingCountText.setText(String.valueOf(followingCount));
    }

    /**
     * Posts adapter for the grid view
     */
    private class PostsAdapter extends BaseAdapter {
        private Activity context;

        PostsAdapter(Context c) {
            context = (Activity) c;
        }

        public int getCount() {
            return profilePosts.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
            } else {
                imageView = (ImageView) convertView;
            }

            ProfilePost profilePost = profilePosts.get(position);
            String mediaLink = profilePost.getMedialink();

            Glide.with(context)
//                    .applyDefaultRequestOptions(new RequestOptions()
//                            .placeholder(new ColorDrawable(context.getResources().getColor(R.color.colorAccent)))
//                    )
                    .load(mediaLink)
                    .apply(new RequestOptions().fitCenter())
//                    .apply(RequestOptions.centerCropTransform())
                    .into(imageView);

            return imageView;
        }
    }
}
