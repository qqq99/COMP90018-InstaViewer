package unimelb.comp90018_instaviewer.utilities;

import android.widget.ListView;

/**
 * Created by xxq1 on 2018/10/20.
 */
public class DeviceListView extends ListView {
    public DeviceListView(android.content.Context context, android.util.AttributeSet attrs){
        super(context, attrs);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
