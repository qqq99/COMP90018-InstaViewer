package unimelb.comp90018_instaviewer.utilities;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import unimelb.comp90018_instaviewer.R;

/**
 * Created by xxq1 on 2018/10/20.
 */
public class MyAdapter extends BaseAdapter {
    private List<DeviceInformation> data = new LinkedList<>();
    private Context context;

    public void setData(List data) {
        this.data = data;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        View view = View.inflate(context, R.layout.activity_device_list, null);
        DeviceInformation device = data.get(position);
        ImageView photo = view.findViewById(R.id.user_photo);
        TextView deviceInfo = view.findViewById(R.id.device_info);
        // photo.setImageResource();
        deviceInfo.setText(device.getDeviceName() + ": " + device.getAddress());
        return view;
    }
}

