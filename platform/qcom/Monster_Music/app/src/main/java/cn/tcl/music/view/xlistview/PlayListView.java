package cn.tcl.music.view.xlistview;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by han.lou1 on 16-9-30.
 */

public class PlayListView extends ListView {

    public PlayListView(Context context) {
        super(context);
    }

    public PlayListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
