package cn.tcl.music.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class PlaylistView extends ListView {

    public PlaylistView(Context context) {
        super(context);
    }

    public PlaylistView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlaylistView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
