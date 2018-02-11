package com.monster.market.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by xiaobin on 16-9-2.
 */
public class DetailScrollView extends ScrollView {

    private static final String TAG = "DetailScrollView";

    private boolean scrollable = true;
    private int initialPosition;

    private int newCheck = 100;

    private OnScrollStoppedListener onScrollStoppedListener;

    public DetailScrollView(Context context) {
        super(context);
    }

    public DetailScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DetailScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DetailScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public interface OnScrollStoppedListener{
        void onScrollStopped();
    }


    public void setOnScrollStoppedListener(DetailScrollView.OnScrollStoppedListener listener){
        onScrollStoppedListener = listener;
    }

    public void startScrollerTask(){

        initialPosition = getScrollY();
        DetailScrollView.this.postDelayed(scrollerTask, newCheck);
    }

    private Runnable scrollerTask = new Runnable() {

        public void run() {

            int newPosition = getScrollY();
            if(initialPosition - newPosition == 0){ //has stopped

                if(onScrollStoppedListener!=null){

                    onScrollStoppedListener.onScrollStopped();
                }
            }else{
                initialPosition = getScrollY();
                DetailScrollView.this.postDelayed(scrollerTask, newCheck);
            }
        }
    };

    private int lastY = 0;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            lastY = getScrollY();
        }
        return super.onInterceptTouchEvent(ev);
    }

    public int getLastY() {
        return lastY;
    }

    public void setLastY(int lastY) {
        this.lastY = lastY;
    }
}
