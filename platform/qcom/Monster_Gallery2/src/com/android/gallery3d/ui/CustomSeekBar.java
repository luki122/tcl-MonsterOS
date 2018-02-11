/* ----------|----------------------|----------------------|----------------- */
/* 13/02/2015| jian.pan1            | PR929635             |inconsistent with GD design
/* ----------|----------------------|----------------------|----------------- */
/* 03/06/2015| jian.pan1            | PR929635             |inconsistent with GD design
/* ----------|----------------------|----------------------|----------------- */
/* 03/06/2015| jian.pan1            | PR939118             |The current value display wrong when eidting picture
/* ----------|----------------------|----------------------|----------------- */
/* 03/09/2015| jian.pan1            | PR916254             |[GenericApp][Gallery]HDPI resolution adaptation
/* ----------|----------------------|----------------------|----------------- */
package com.android.gallery3d.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.gallery3d.R;

public class CustomSeekBar extends LinearLayout {

    private LayoutInflater mInflater;
    private View mView;
    private TextView mSeekBarDefaultValueIndicator;
    private SeekBar mSeekBar;
    private int mSeekBarWidth = 0;
    private float seekbarPosition_y = 0;
    private int maxValue = 30;
    //private int[] locations = new int[2];
    
    private int mLeftDeviation = 10;
    private int mRightDeviation = 95;
    
    //private int mScreenWidth;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR939118 begin
    private boolean isNeedReverse = false;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR939118 end
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-09,PR916254 begin
    private boolean hasFocusOnInit = false;
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-09,PR916254 end

    public void setOnSeekBarChangeListener(
            SeekBar.OnSeekBarChangeListener listener) {
        mSeekBar.setOnSeekBarChangeListener(listener);
    }

    public CustomSeekBar(Context context) {
        super(context);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR939118 begin
        isNeedReverse = context.getResources().getBoolean(R.bool.is_undo_reverse);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR939118 end
        mRightDeviation = context.getResources().getInteger(R.integer.custom_seekbar_right_deviation);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR929635 begin
        mLeftDeviation = context.getResources().getInteger(R.integer.custom_seekbar_left_deviation);
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR929635 end
        Log.i(VIEW_LOG_TAG, "RIGHT_DEVIATION:" + mRightDeviation);
        mInflater = LayoutInflater.from(context);
        mView = mInflater.inflate(R.layout.beauty_face_seekar, this);
        mSeekBarDefaultValueIndicator = (TextView) mView.findViewById(R.id.seekbarvalue);
        mSeekBar = (SeekBar) mView.findViewById(R.id.seekbar);
        // TCL ShenQianfeng Begin on 2016.08.30
        // Annotated Below:
        //seekbarValue.setVisibility(View.GONE);
        this.setGravity(Gravity.CENTER);
        // TCL ShenQianfeng End on 2016.08.30
        
        // TCL ShenQianfeng Begin on 2016.08.30
		//modify begin by liaoanhua
        mSeekBarDefaultValueIndicator.setText("");
		//modify end
        /*
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        mView.setLayoutParams(lp);
        */

        //this.addView(mView);
        // TCL ShenQianfeng End on 2016.08.30
        /*
       WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mSeekBar.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        mSeekBar.requestLayout();
        */
        
        //mScreenWidth = wm.getDefaultDisplay().getWidth();
    }

    public void onStopTouch() {
        // TCL ShenQianfeng Begin on 2016.08.30
        // Annotated Below:
        // seekbarValue.setVisibility(View.GONE);
        // TCL ShenQianfeng End on 2016.08.30
    }

    public void onStartTouch() {
        // TCL ShenQianfeng Begin on 2016.08.30
        // Annotated Below:
        /*
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR929635 begin
        valueAnimationIn();
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR929635 end
        seekbarValue.setVisibility(View.VISIBLE);
         */
        // TCL ShenQianfeng End on 2016.08.30
    }

    public void onProgress(int progress) {
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-09,PR916254 begin
        if (!hasFocusOnInit) {
            onWindowFocusChanged(true);
        }
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-09,PR916254 end
        
        // TCL ShenQianfeng Begin on 2016.08.30
        // Annotated Below:
        /*
        seekbarValue.setText(progress + "");
        float positionX = getPositionX();
        startAnimation(positionX);
        */
        // TCL ShenQianfeng End on 2016.08.30
    }
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR929635 begin
    private void valueAnimationIn() {
        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.playTogether(
            ObjectAnimator.ofFloat(mSeekBarDefaultValueIndicator, "scaleX", 0, 1f),
            ObjectAnimator.ofFloat(mSeekBarDefaultValueIndicator, "scaleY", 0, 1f),
            ObjectAnimator.ofFloat(mSeekBarDefaultValueIndicator, "alpha", 0, 1f, 1)
        );
        scaleSet.setDuration(150).start();
    }
    //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR929635 end

    public void setMax(int maxValue) {
        this.maxValue = maxValue;
        Log.i(VIEW_LOG_TAG, "maxValue:" + maxValue);
        mSeekBar.setMax(maxValue);
    }
    
    /*
    public void setScreenWidth(int screenWidth) {
        this.mScreenWidth = screenWidth;
    }
    */

    public void setProgress(int progress, int offset) {
        mSeekBar.setProgress(progress);
        // TCL ShenQianfeng Begin on 2016.08.30
        // Annotated Below:
        //mSeekBarDefaultValueIndicator.setText(progress + offset + "");
        // TCL ShenQianfeng End on 2016.08.30
        Log.i(VIEW_LOG_TAG, "progress:" + progress + " offset:" + offset);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mSeekBar.onTouchEvent(event);
    }

    @SuppressLint("NewApi")
    public void startAnimation(float positionX) {
        Path path = new Path();
        path.moveTo(positionX, seekbarPosition_y);
        ObjectAnimator mAnimator = ObjectAnimator.ofFloat(mSeekBarDefaultValueIndicator, "x",
                "y", path);
        mAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                if (animation.isRunning()) {
                    animation.end();
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        mAnimator.setDuration(0);
        mAnimator.setStartDelay(0);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.start();
    }

    private float getPositionX() {
        float positionX = ((float) mSeekBar.getProgress() / maxValue) * mSeekBarWidth;
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR939118 begin
        if (isNeedReverse) {
            positionX = getWidth() - positionX - mRightDeviation;
        } else {
            positionX += mLeftDeviation;
        }
        return positionX;
        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-06,PR939118 end
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //mSeekBarDefaultValueIndicator.getLocationOnScreen(locations);
        if (mSeekBar.getWidth() != 0) {
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-09,PR916254 begin
            hasFocusOnInit = true;
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-03-09,PR916254 end
            mSeekBarWidth = mSeekBar.getWidth() - mRightDeviation;
        } else {
            mSeekBarWidth = getWidth() - mRightDeviation;
        }
        
        // TCL ShenQianfeng Begin on 2016.08.30
        // Annotated Below:
        /*
        seekbarPosition_y = mSeekBarDefaultValueIndicator.getY();
        float positionX = getPositionX();
        Log.i(VIEW_LOG_TAG, "onWindowFocusChanged positionX->" + positionX);
         startAnimation(positionX);
         */
        // TCL ShenQianfeng End on 2016.08.30
    }
}