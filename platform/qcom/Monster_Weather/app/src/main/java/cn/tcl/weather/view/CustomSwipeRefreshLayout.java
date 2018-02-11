/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by user on 16-4-9.
 */
public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {

    private IInterceptyTouchEvent.VerticalInterceptTouchEvent mVerticalInterceptTouchEvent;

    public CustomSwipeRefreshLayout(Context context) {
        super(context);
        mVerticalInterceptTouchEvent = new IInterceptyTouchEvent.VerticalInterceptTouchEvent(this);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mVerticalInterceptTouchEvent = new IInterceptyTouchEvent.VerticalInterceptTouchEvent(this);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isIntercept = mVerticalInterceptTouchEvent.onInterceptTouchEvent(ev);
        if (super.onInterceptTouchEvent(ev) && isIntercept) {
            mVerticalInterceptTouchEvent.resetMotionEvent();
            return true;
        }
        return false;
    }


    public void setIgnoreView(View view) {
        mVerticalInterceptTouchEvent.setIgnoreView(view);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            Log.e("CSR", "IllegalArgumentException in dispatchTouchEvent");
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            Log.e("CSR", "IllegalArgumentException in onTouchEvent");
        }
        return false;
    }
}
