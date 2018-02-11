package com.android.camera.ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.camera.CaptureLayoutHelper;
import com.android.camera.debug.Log;
import com.android.camera.util.LockUtils;
/* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
import com.android.camera.widget.CustomizeScalesBar;
import com.android.camera.widget.ZoomScalesBar;
/* MODIFIED-END by xuan.zhou,BUG-3178291*/
import com.android.external.plantform.ExtBuild; // MODIFIED by shunyin.zhang, 2016-05-05,BUG-2013029
import com.tct.camera.R;

/**
 * Created by sdduser on 10/13/15.
 */
public class ZoomBar extends LinearLayout implements
        /* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
        ZoomScalesBar.OnScalesBarChangedListener, View.OnTouchListener ,Lockable{

    private final Log.Tag TAG = new Log.Tag("ZoomBar");

    private Handler mHandler;

    private ZoomBar mZoomBar;
    private ZoomScalesBar zoomBar;
    /* MODIFIED-END by xuan.zhou,BUG-3178291*/
    private ImageView zoomIn;
    private ImageView zoomOut;

    private final int MIN = 0;
    private final int MAX = 100;

    private static final float ZOOM_STEP = 0.10f;
    private static final long TIME_ADD = 100;

    private float minRatio = 1.0f;
    private float maxRatio;
    private float ratio; // current ratio

    private ProgressChangeListener mListener;
    private CaptureLayoutHelper mCaptureLayoutHelper;

    private final int ZOOM_HIDE_TIME = 3000;

    private int navigationBarHeight = 0;

    // Mark the click state for zoom in/out separately
    private boolean mZoomInPressed = false;
    private boolean mZoomOutPressed = false;

    /* MODIFIED-BEGIN by xuan.zhou, 2016-05-23,BUG-2167404*/
    // If thumb is dragging during module selecting or camera switching, the visibility
    // may be set visible soon after it's set invisible, so I wanna lock the bar to disable
    // these actions.
    private boolean mLocked = false;
    /* MODIFIED-END by xuan.zhou,BUG-2167404*/

    private ControlZoomBarCallback mZoomBarCallback; // MODIFIED by shunyin.zhang, 2016-05-05,BUG-2013029

    private ZoomBarVisibleChangedListener mVisibleListener; // MODIFIED by jianying.zhang, 2016-10-26,BUG-3212745
    private LockUtils.Lock mMultiLock;
    public ZoomBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mHandler = new Handler();
        mMultiLock= LockUtils.getInstance().generateMultiLock(LockUtils.LockType.MULTILOCK);
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-26,BUG-3212745*/
    public void setZoomBarVisibleChangedListener(ZoomBarVisibleChangedListener visibleListener) {
        mVisibleListener = visibleListener;
    }
    /* MODIFIED-END by jianying.zhang,BUG-3212745*/

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mZoomBar = (ZoomBar)findViewById(R.id.zoom_bar);
        /* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
        zoomBar = (ZoomScalesBar) this.findViewById(R.id.zoom_seek);
        zoomIn = (ImageView) this.findViewById(R.id.zoom_in);
        zoomOut = (ImageView) this.findViewById(R.id.zoom_out);
        zoomIn.setOnTouchListener(this);
        zoomOut.setOnTouchListener(this);
        zoomBar.setOnScalesBarChangedListener(this);
        zoomBar.setRange(MIN, MAX);
    }


    @Override
    public void onProgressChanged(CustomizeScalesBar scaleBar, int progress, boolean fromUser) {
    /* MODIFIED-END by xuan.zhou,BUG-3178291*/
        /* MODIFIED-BEGIN by xuan.zhou, 2016-05-23,BUG-2167404*/
        if (!fromUser) {
            Log.i(TAG, "The progress change was not initiated by the user.");
            return;
        }
        if (mListener == null) {
            Log.w(TAG, "Empty receiver.");
            return;
        }
        if (mLocked) {
            Log.w(TAG, "ZoomBar locked.");
            return;
        }
        if (getVisibility() != View.VISIBLE) {
            Log.w(TAG, "Visibility " + getVisibility() + " , ignore.");
            return;
        }

        float per = (maxRatio - minRatio) / MAX;
        ratio = progress * per + minRatio;
        mListener.onProgressChanged(ratio);
        userAction();
        /* MODIFIED-END by xuan.zhou,BUG-2167404*/
    }

    @Override
    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
    public void onStartTrackingTouch(CustomizeScalesBar scaleBar) {
        showZoomBar();
        // I don't want to hide the bar when user still hold the slider.
        mHandler.removeCallbacks(hideRunnable);
    }

    @Override
    public void onStopTrackingTouch(CustomizeScalesBar scaleBar) {
    /* MODIFIED-END by xuan.zhou,BUG-3178291*/
        // userAction(); // MODIFIED by xuan.zhou, 2016-05-23,BUG-2167404
    }

    private void changeRatioBar(float r){
        ratio += r;
        if (ratio >= maxRatio){
            ratio = maxRatio;
            resetPressedState();
        } else if ( ratio <= minRatio){
            ratio = minRatio;
            resetPressedState();
        }
        if (mListener!=null){
            mListener.onProgressChanged(ratio);
        }
        updateSeekBar();
        userAction();
    }

    // Reset the state when the visibility changed or zoom value reach the limit.
    private void resetPressedState() {
        mZoomInPressed = false;
        mZoomOutPressed = false;
    }

    final Runnable mZoomProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mZoomInPressed == mZoomOutPressed) {
                return;
            }
            /* MODIFIED-BEGIN by xuan.zhou, 2016-05-23,BUG-2167404*/
            if (mLocked) {
                return;
            }
            /* MODIFIED-END by xuan.zhou,BUG-2167404*/
            float step = mZoomInPressed ? ZOOM_STEP : -ZOOM_STEP;
            changeRatioBar(step);
            ZoomBar.this.postDelayed(this, TIME_ADD);
        }
    };

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN: {
                if (view == zoomIn) {
                    mZoomInPressed = true;
                } else if (view == zoomOut) {
                    mZoomOutPressed = true;
                }

                // Ignore the state that user presses both view at the same time
                if (mZoomInPressed && mZoomOutPressed) {
                    resetPressedState();
                }
                if (mZoomInPressed != mZoomOutPressed) {
                    ZoomBar.this.removeCallbacks(mZoomProgressRunnable);
                    mZoomProgressRunnable.run();
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                resetPressedState();
                break;
            }
        }
        return false;
    }

    public interface ProgressChangeListener {
        void onProgressChanged(float ratio);
    }

    public void setProgressChangeListener(ProgressChangeListener l) {
        mListener = l;
    }

    public void setZoomMax(float max) {
        maxRatio = max;
        Log.e(TAG, "Max zoom is " + max);
    }

    public void zoomRatioChanged(float r) {
        ratio = r;
        updateSeekBar();
        userAction();
    }

    public void resetZoomRatio() {
        ratio = minRatio;
        hideZoomBar();
    }

    private void updateSeekBar() {
        float per = MAX / (maxRatio - minRatio);
        int pos = (int)((ratio - minRatio) * per);
        Log.e(TAG, "ZoomBar,  current ratio is " + ratio + ", set pos " + pos);
        if (zoomBar != null) {
            zoomBar.setProgress(pos);
        }
    }

    private void userAction() {
        showZoomBar();
        mHandler.removeCallbacks(hideRunnable);
        mHandler.postDelayed(hideRunnable, ZOOM_HIDE_TIME);
    }

    protected final Runnable hideRunnable = new Runnable() {
        public void run() {
            hideZoomBar();
        }
    };

    private void showZoomBar() {
        this.post(new Runnable() {
            @Override
            public void run() {
                if (mZoomBar != null && mZoomBar.getVisibility() != View.VISIBLE) {
                    mZoomBar.setVisibility(View.VISIBLE);
                    resetPressedState();
                    /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
                    if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                        if (mZoomBarCallback != null) {
                            mZoomBarCallback.onVisibility(true);
                        }
                    }
                    /* MODIFIED-END by shunyin.zhang,BUG-2013029*/
                }
            }
        });
     }

    private void hideZoomBar() {
        this.post(new Runnable() {
            @Override
            public void run() {
                if (mZoomBar != null && mZoomBar.getVisibility() == View.VISIBLE) {
                    mZoomBar.setVisibility(View.INVISIBLE);
                    resetPressedState();
                }
            }
        });
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-26,BUG-3212745*/
    public interface ZoomBarVisibleChangedListener {
        void onZoomBarVisibilityChanged(boolean visible);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (mVisibleListener != null) {
            mVisibleListener.onZoomBarVisibilityChanged(visibility == View.VISIBLE);
        }
    }
    /* MODIFIED-END by jianying.zhang,BUG-3212745*/

    /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-05,BUG-2013029*/
    public interface ControlZoomBarCallback{
        void onVisibility(boolean isVisible);
    }

    public void setControlZoomBarCallback(ControlZoomBarCallback callback) {
        mZoomBarCallback = callback;
    }

    public void resetControlZoomBarCallback() {
        if (mZoomBarCallback != null) {
            mZoomBarCallback = null;
        }
    }
    /* MODIFIED-END by shunyin.zhang,BUG-2013029*/

    /* MODIFIED-BEGIN by xuan.zhou, 2016-05-23,BUG-2167404*/
    @Override
    public int lock() {
        return mMultiLock.aquireLock();
    }

    @Override
    public boolean unlockWithToken(int token) {
        return mMultiLock.unlockWithToken(token);
    }

    @Override
    public void lockSelf() {
        mMultiLock.aquireLock(this.hashCode());
    }

    @Override
    public void unLockSelf() {
        mMultiLock.unlockWithToken(this.hashCode());
    }

    @Override
    public boolean isLocked() {
        return mMultiLock.isLocked();
    }
} // MODIFIED by xuan.zhou, 2016-10-22,BUG-3178291
