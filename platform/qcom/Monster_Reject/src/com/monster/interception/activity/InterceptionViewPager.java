package com.monster.interception.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import mst.widget.ViewPager;

public class InterceptionViewPager extends ViewPager {  
    
    private boolean scrollble = true;  
  
    public InterceptionViewPager(Context context) {  
        super(context);  
    }  
  
    public InterceptionViewPager(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    /*@Override  
    public boolean onInterceptTouchEvent(MotionEvent ev) {  
        if (!scrollble) {  
            return true;  
        }
        return super.onInterceptTouchEvent(ev);
    }  */

    @Override  
    public boolean onTouchEvent(MotionEvent ev) {  
        if (!scrollble) {  
            return true;  
        }  
        return super.onTouchEvent(ev);  
    }  
  
  
    public boolean isScrollble() {  
        return scrollble;  
    }  
  
    public void setScrollble(boolean scrollble) {  
        this.scrollble = scrollble;  
    }  
}  
