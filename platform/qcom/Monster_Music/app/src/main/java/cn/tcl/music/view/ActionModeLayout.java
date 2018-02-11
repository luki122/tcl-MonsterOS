package cn.tcl.music.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.tcl.music.R;

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
