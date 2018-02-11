/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cn.tcl.meetingassistant.log.MeetingLog;
import mst.widget.SliderView;

/**
 * Created on 16-11-3.
 */
public class SilderView extends SliderView{

    private final String TAG = SilderView.class.getSimpleName();
    private boolean mNeedToInterceptMove;

    public SilderView(Context context) {
        super(context);
    }

    public SilderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SilderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        MeetingLog.i("my SilderView measure width  ", "" + getMeasuredWidth());
        MeetingLog.i("my SilderView measure height ", "" + getMeasuredHeight());
    }

    public void setNeedToInterceptMove(boolean needToInterceptMove){
        mNeedToInterceptMove = needToInterceptMove;
    }
}
