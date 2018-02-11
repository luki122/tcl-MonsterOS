package cn.tcl.music.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class ActionModeLayout extends RelativeLayout {

    private Context mContext;

    public ActionModeLayout(Context context) {
        super(context);
    }

    public ActionModeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
//        init();
    }

    public ActionModeLayout(Context context, AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getScreenWidth(mContext);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, mode);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int height = View.MeasureSpec.getSize(heightMeasureSpec);
//        setMeasuredDimension(width, height);
    }

    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
}
