/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cn.tcl.weather.R;
import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-11.
 * $desc
 */
public class ViewPagerEx extends ViewPager {

    private static final String TAG = ViewPagerEx.class.getName();

    private Paint mPointPaint;
    private Bitmap currentIconBmp;
    private Bitmap pointIconBmp;
    private Rect currentBmpTargetRect;
    private Rect pointBmpTargetRect;

    private MotionEvent mDownEvent;

    private float mLastMotionX, mLastMotionY;


    public ViewPagerEx(Context context) {
        super(context);
        initPaint();
    }

    public ViewPagerEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        initBitmap();
        initBmpTargetRect();
    }

    private void initPaint() {
        mPointPaint = new Paint();
        mPointPaint.setStrokeJoin(Paint.Join.ROUND);
        mPointPaint.setStrokeCap(Paint.Cap.ROUND);
        mPointPaint.setStrokeWidth((float) 12.0);
        mPointPaint.setAntiAlias(true);
    }

    private void initBitmap() {
        currentIconBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tcl_current);
        pointIconBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tcl_scroll_point);
    }

    private void initBmpTargetRect() {
        currentBmpTargetRect = new Rect();
        pointBmpTargetRect = new Rect();

        currentBmpTargetRect.top = 1170;
        currentBmpTargetRect.bottom = currentBmpTargetRect.top + 18;

        pointBmpTargetRect.top = 1170;
        pointBmpTargetRect.bottom = pointBmpTargetRect.top + 12;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isIntercept = false;
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            resetMotionEvent();
            mDownEvent = MotionEvent.obtain(ev);
            mLastMotionX = ev.getRawX();
            mLastMotionY = ev.getRawY();
        } else if (action == MotionEvent.ACTION_MOVE) {
            final float x = ev.getRawX();
            final float dx = x - mLastMotionX;
            final float xDiff = Math.abs(dx);
            final float y = ev.getRawY();
            final float yDiff = Math.abs(y - mLastMotionY);
            if (xDiff > yDiff) {
                isIntercept = true;
            }
            PagerAdapter adapter = getAdapter();
            if (isIntercept && null != adapter && adapter instanceof PagerAdapterEx) {
                PagerAdapterEx adapterEx = (PagerAdapterEx) adapter;
                isIntercept = !adapterEx.canScrollHorizontally((int) dx, getCurrentItem(), mDownEvent);
                if (isIntercept) {
                    mLastMotionX = ev.getRawX();
                    mLastMotionY = ev.getRawY();
                    onTouchEvent(mDownEvent);
                }
            }
        } else {
            resetMotionEvent();
        }

        return isIntercept;
    }

    private void resetMotionEvent() {
        mLastMotionX = 0;
        mLastMotionY = 0;
        if (null != mDownEvent) {
            mDownEvent.recycle();
            mDownEvent = null;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            resetMotionEvent();
            return super.onTouchEvent(ev);
        } catch (Exception ex) {
            LogUtils.e(TAG, "this is view pager internal error...", ex);
        }
        return false;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        PagerAdapter adapter = getAdapter();
        if (null != adapter) {
            final int dx = getScrollX() + getWidth() / 2 - (adapter.getCount() * 40 / 2);
            canvas.save();
            int currentIndex = getCurrentItem();
            /* if there is some cities , draw correct num black point*/

            for (int i = 0; i < adapter.getCount(); i++) {
                currentBmpTargetRect.left = dx + i * 40;
                pointBmpTargetRect.left = dx + i * 40;

                currentBmpTargetRect.right = currentBmpTargetRect.left + 18;
                pointBmpTargetRect.right = pointBmpTargetRect.left + 12;
                if (i == currentIndex) {
                    canvas.drawBitmap(currentIconBmp, null, currentBmpTargetRect, mPointPaint);
                } else {
                    canvas.drawBitmap(pointIconBmp, null, pointBmpTargetRect, mPointPaint);
                }
            }
            canvas.restore();
        }
    }

    public static abstract class PagerAdapterEx extends PagerAdapter {
        protected abstract boolean canScrollHorizontally(int direction, int currentPosition, MotionEvent downEvent);
    }

    public void recycle() {
        currentIconBmp.recycle();
        pointIconBmp.recycle();
    }


}
