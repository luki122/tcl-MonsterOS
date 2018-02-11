package cn.tcl.music.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by dongdong.huang on 2015/12/10.
 * 处理手势滑动冲突问题
 */
public class MusicRefreshLayout extends SwipeRefreshLayout{
    private float startX, startY;

    public MusicRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final float touchX = ev.getX();
        final float touchY = ev.getY();

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                startX = touchX;
                startY = touchY;
                break;
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(touchX - startX) > 200 || touchY - startY < 90){
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                startX = 0;
                startY = 0;
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

}
