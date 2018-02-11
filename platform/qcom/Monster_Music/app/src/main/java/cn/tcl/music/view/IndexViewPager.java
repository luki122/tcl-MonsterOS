package cn.tcl.music.view;

import android.content.Context;
import mst.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by xiangxiangliu on 2015/12/7.
 */
public class IndexViewPager extends ViewPager {
    private boolean isCanScroll = false;
    public IndexViewPager(Context context) {
        super(context);
    }
    public IndexViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        // TODO Auto-generated method stub
        if (isCanScroll) {
            return super.onTouchEvent(arg0);
        } else {
            return false;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        // TODO Auto-generated method stub
        if (isCanScroll) {
            return super.onInterceptTouchEvent(arg0);
        } else {
            return false;
        }

    }
}