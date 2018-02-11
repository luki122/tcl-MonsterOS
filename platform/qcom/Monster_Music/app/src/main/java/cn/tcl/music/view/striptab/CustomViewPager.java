package cn.tcl.music.view.striptab;

import android.content.Context;
import mst.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2015/12/1.
 */
public class CustomViewPager extends ViewPager {


    public  boolean enabled=true;


    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //触摸没有反应就可以了
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (enabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}