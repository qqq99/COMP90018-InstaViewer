package unimelb.comp90018_instaviewer.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import timber.log.Timber;
import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.activities.SelectPhotoActivity;
import unimelb.comp90018_instaviewer.utilities.PermissionUtil;

public class GalleryFragment extends Fragment {

    /* List of the gallery images */
    private ArrayList<String> images;
    private OnGalleryImageSelectedListener fragmentListener;

    private GridView gallery;
    private ImageView galleryImagePreview;
    private Button showGalleryButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        gallery = view.findViewById(R.id.gridGalleryPicker);
        galleryImagePreview = view.findViewById(R.id.imageGalleryPreview);
        showGalleryButton = view.findViewById(R.id.btnShowGallery);

        showGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGalleryImages();
            }
        });

        showGalleryImages();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentListener = (OnGalleryImageSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnGalleryImageSelectedListener");
        }
    }

    private void showGalleryImages() {
        if (!PermissionUtil.checkReadStorage(getActivity()) || !PermissionUtil.checkWriteStorage(getActivity())) {
            showGalleryButton.setVisibility(View.VISIBLE);
            return;
        }

        /* Hide button to show gallery (and prompt permission) and show gallery in grid */
        showGalleryButton.setVisibility(View.GONE);
        gallery.setAdapter(new ImageAdapter(getActivity()));
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (null != images && !images.isEmpty()) {
                    String imagePath = images.get(position);

                    fragmentListener.onGalleryImageSelected(imagePath);
                    Glide.with(getActivity()).load(imagePath)
                            .into(galleryImagePreview
                            );
                }
            }
        });
    }

    /**
     * Image adapter for the grid view
     */
    private class ImageAdapter extends BaseAdapter {
        private Activity context;

        ImageAdapter(Context c) {
            context = (Activity) c;
            images = getImages(context);
            Timber.i("Finished getting images");
        }

        public int getCount() {
            return images.size();
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

            Glide.with(context).load(images.get(position))
                    .apply(RequestOptions.centerCropTransform())
                    .into(imageView);

            return imageView;
        }

        /**
         * Gets all image paths from the gallery
         *
         * @param activity activity context
         * @return ArrayList of image paths from gallery
         */
        public ArrayList<String> getImages(Activity activity) {
            Timber.i("Getting images");

            ArrayList<String> paths = new ArrayList<>();
            final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
            String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
            String[] selectionArgs = new String[] {
                    "Camera"
            };
            final String orderBy = MediaStore.Images.Media.DATE_ADDED;

            //Stores all the images from the gallery in Cursor
            Cursor cursor = activity.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null , orderBy);
//            Cursor cursor = activity.getContentResolver().query(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, selection, selectionArgs, orderBy);


            //Total number of images
            int count = cursor.getCount();

            //Create an array to store path to all the images
            String[] arrPath = new String[count];

            Timber.i("Image count: " + count);

            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                //Store the path of the image
                String imagePath = cursor.getString(dataColumnIndex);
                arrPath[i]= imagePath;
                paths.add(arrPath[i]);

                /* Set first image to be the preview */
                if (i == 0) {
                    fragmentListener.onGalleryImageSelected(imagePath);
                    Glide.with(activity).load(imagePath)
//                            .apply(RequestOptions.centerCropTransform())
                            .into(galleryImagePreview);
                }
            }

            cursor.close();
            return paths;
        }
    }

    public interface OnGalleryImageSelectedListener {
        public void onGalleryImageSelected(String imagePath);
    }
}
