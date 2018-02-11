/* ----------|----------------------|----------------------|----------------- */
/* 06/12/2015| jian.pan1            | PR1003170            |[5.0][Gallery] Frame grids' position should retain the previous position after cancel/accept editing
/* ----------|----------------------|----------------------|----------------- */
package com.android.gallery3d.filtershow.category;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class CustomHorizontalScrollView extends HorizontalScrollView {
    public CustomHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomHorizontalScrollView(Context context) {
        super(context);
    }

    public interface ScrollViewListener {
        void onScrollChanged(ScrollType scrollType, int scrollX);
    }

    private Handler mHandler;
    private ScrollViewListener scrollViewListener;

    enum ScrollType {
        IDLE, TOUCH_SCROLL, FLING
    };

    private int currentX = -1;
    private ScrollType scrollType = ScrollType.IDLE;
    private int scrollDealy = 50;

    private Runnable scrollRunnable = new Runnable() {

        @Override
        public void run() {
            if (getScrollX() == currentX) {
                scrollType = ScrollType.IDLE;
                if (scrollViewListener != null) {
                    scrollViewListener.onScrollChanged(scrollType, currentX);
                }
                mHandler.removeCallbacks(this);
                return;
            } else {
                scrollType = ScrollType.FLING;
                if (scrollViewListener != null) {
                    scrollViewListener.onScrollChanged(scrollType, getScrollX());
                }
            }
            currentX = getScrollX();
            mHandler.postDelayed(this, scrollDealy);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_MOVE:
            this.scrollType = ScrollType.TOUCH_SCROLL;
            scrollViewListener.onScrollChanged(scrollType, getScrollX());
            mHandler.removeCallbacks(scrollRunnable);
            break;
        case MotionEvent.ACTION_UP:
            mHandler.post(scrollRunnable);
            break;
        }
        return super.onTouchEvent(ev);
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public void setOnStateChangedListener(ScrollViewListener listener) {
        this.scrollViewListener = listener;
    }
}
