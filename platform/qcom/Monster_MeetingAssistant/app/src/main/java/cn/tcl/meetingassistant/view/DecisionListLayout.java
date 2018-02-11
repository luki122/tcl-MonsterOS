/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.LinearLayout;

import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-8.
 * Decision List page layout
 */
public class DecisionListLayout extends LinearLayout {

    //layout status flag
    private boolean isFullScreen = false;

    //last record Y coordinate
    private float mLastY;

    //record Y coordinate when action down
    private float mBeginY;

    private float mCrollDistace = 0f;

    //scroll gate value
    private final static float GATE_VALUE = 100;

    private int mBlankHeight = 0;

    private boolean isListTop = true;

    private ScrollListener mScrollListener;

    private String TAG = DecisionListLayout.class.getSimpleName();

    public DecisionListLayout(Context context) {
        super(context);
    }

    public DecisionListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DecisionListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DecisionListLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setBlankHeight(int mBlankHeight) {
        this.mBlankHeight = mBlankHeight;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mLastY = ev.getY();
            mBeginY = ev.getY();
        }

        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            int diff = (int) (ev.getY() - mBeginY);

            // return intercept or not
            return isIntercepted(diff, isListTop);
        }

        if (ev.getAction() == MotionEvent.ACTION_CANCEL) {

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float offSize = mBeginY - ev.getY();
            //if hand move longer than it should did
            if (!isFullScreen && Math.abs(offSize) <= mBlankHeight) {
               // MeetingLog.i(TAG, "offSize: " + offSize);
                this.scrollBy(0, (int) (mLastY - ev.getY()));
               // MeetingLog.i(TAG, "mBlankHeight: " + mBlankHeight);
                mLastY = ev.getY();
            }
            else if(isFullScreen && -offSize <= mBlankHeight && -offSize >=0){
                //MeetingLog.i(TAG, "offSize: " + offSize);
                this.scrollBy(0, (int) (mLastY - ev.getY()));
               // MeetingLog.i(TAG, "mBlankHeight: " + mBlankHeight);
                mLastY = ev.getY();
            }
            else {
                //MeetingLog.i(TAG, "move to Blank height:" + offSize + ":" + mBlankHeight);
            }

        }

        if (ev.getAction() == MotionEvent.ACTION_UP ||
                ev.getAction() == MotionEvent.ACTION_CANCEL) {
            float thisY = ev.getY();
            float diffY = thisY - mBeginY;

            //touch move to up of screen
            if (diffY < - GATE_VALUE) {
                if (mScrollListener != null) {
                    mScrollListener.scrollUpToTop();
                }
            } else if (diffY > GATE_VALUE) {
                if (mScrollListener != null) {
                    mScrollListener.scrollDownToBottom();
                }
            } else {
                if (mScrollListener != null) {
                    mScrollListener.scrollBack();
                }
            }
            return true;
        }

        return super.onTouchEvent(ev);
    }

    public void setScrollListener(ScrollListener mScrollListener) {
        this.mScrollListener = mScrollListener;
    }

    /**
     * set the screen status
     *
     * @param isFull      set true if you want to change it to full screen
     *                    set false if you want to change it to half screen
     * @param blankHeight set blank view's height
     */
    public void setFillScreen(boolean isFull, int blankHeight) {
        isFullScreen = isFull;
        if (!isFullScreen)
            scrollToHalf();
        else
            scrollToFull(blankHeight);
    }

    public void scrollToHalf() {
        MeetingLog.i(TAG, "scrollToHalf");



        this.setScrollY(0);
    }

    public void scrollToFull(int blankHeight) {
        this.setScrollY(blankHeight);
        MeetingLog.i(TAG, "scrollToFull");
    }

    /**
     * whether to intercept
     *
     * @param diff      move direction if diff less than 0,move up;if diff more than 0,move down
     * @param isListTop whether the list is top
     * @return return true if a action need to be intercepted
     */
    public boolean isIntercepted(int diff, boolean isListTop) {
        boolean isIntercepted = false;

        if (!isFullScreen) {
            isIntercepted = true;
        }
        // if in full screen status, pull down and the list is in top status
        // intercept this event
        if (isFullScreen && diff > 0 && isListTop) {
            isIntercepted = true;
        }

        return isIntercepted;
    }

    public void setListIsTop(boolean isListTop) {
        this.isListTop = isListTop;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public interface ScrollListener {
        void scrollUpToTop();

        void scrollDownToBottom();

        void scrollBack();
    }


}
