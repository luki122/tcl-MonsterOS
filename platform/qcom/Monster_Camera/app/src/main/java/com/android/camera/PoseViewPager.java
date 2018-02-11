/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PoseViewPager extends ViewPager {
    public PoseViewPager(Context context) {
        super(context);
    }

    public PoseViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        super.dispatchTouchEvent(ev);
        return true;
    }
}
