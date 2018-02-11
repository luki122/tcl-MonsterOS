package com.mst.wallpaper.widget;

import mst.widget.ViewPager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CropViewPager extends ViewPager {

    private boolean isCanScroll = true;

    public CropViewPager(Context context) {
        super(context);
    }

    public CropViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
    	// TODO Auto-generated method stub
    	return false;
    }
}
