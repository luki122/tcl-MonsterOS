package com.android.camera.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;// MODIFIED by nie.lei, 2016-03-21, BUG-1761286
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.camera.AnimationManager;
import com.android.camera.CameraActivity;
import com.android.camera.debug.Log;
import com.tct.camera.R;

/**
 * Created by wenhua.tu on 2/20/16.
 */
public class GesturePalmOption extends LinearLayout {
    private static final Log.Tag TAG = new Log.Tag("GesturePalmOption");
    private static final int HIDE_GESTURE_HELP_TIP = 0;
    private static final int HIDE_GESTURE_HELP_TIP_DELAY = 3000;
    private CameraActivity mActivity;
    private Button mGestureDismissButton;
    private RotateImageView mGesturePalm;
    private FrameLayout mGestureHelpTip;

    private ValueAnimator mHideHelpTipAnimator;
    private ValueAnimator mShowHelpTipAnmator;

    public GesturePalmOption(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (CameraActivity) context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mGesturePalm = (RotateImageView) findViewById(R.id.gesture_palm);
        mGesturePalm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                animateShowGestureHelpTip();
                mHandler.removeMessages(HIDE_GESTURE_HELP_TIP);
                mHandler.sendEmptyMessageDelayed(HIDE_GESTURE_HELP_TIP, HIDE_GESTURE_HELP_TIP_DELAY);
            }
        });
        mGestureHelpTip = (FrameLayout) mActivity.findViewById(R.id.front_gesture_help_view);
        mGestureDismissButton = (Button) mGestureHelpTip.findViewById(R.id.gesture_help_dismiss);
        mGestureDismissButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideGestureHelpTip();
            }
        });
    }

    public void hideGestureHelpTip() {
        if (mGestureHelpTip.getVisibility() == View.VISIBLE) {
            animateHideGestureHelpTip();
            mHandler.removeMessages(HIDE_GESTURE_HELP_TIP);
        }
    }

    private void animateShowGestureHelpTip() {
        if(mGestureHelpTip.getVisibility()==View.VISIBLE){
            return;
        }
        if(mHideHelpTipAnimator ==null|| mShowHelpTipAnmator ==null){
            mShowHelpTipAnmator = AnimationManager.buildShowingAnimator(mGestureHelpTip);
            mHideHelpTipAnimator = AnimationManager.buildHidingAnimator(mGestureHelpTip);
        }
        if(mHideHelpTipAnimator.isRunning()){
            mHideHelpTipAnimator.cancel();
        }

        if(!mShowHelpTipAnmator.isRunning()){
            mShowHelpTipAnmator.start();
        }
    }

    private void animateHideGestureHelpTip() {
        if (mGestureHelpTip.getVisibility() == View.INVISIBLE) {
            return;
        }
        if(mHideHelpTipAnimator ==null|| mShowHelpTipAnmator ==null){
            mShowHelpTipAnmator = AnimationManager.buildShowingAnimator(mGestureHelpTip);
            mHideHelpTipAnimator = AnimationManager.buildHidingAnimator(mGestureHelpTip);
        }
        if(mShowHelpTipAnmator.isRunning()){
            mShowHelpTipAnmator.cancel();
        }
        if(!mHideHelpTipAnimator.isRunning()){
            mHideHelpTipAnimator.start();
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case HIDE_GESTURE_HELP_TIP:
                    hideGestureHelpTip();
                    break;
            }
        }
    };

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE && mHandler != null  && mHandler.hasMessages(HIDE_GESTURE_HELP_TIP)) {
            mHandler.removeMessages(HIDE_GESTURE_HELP_TIP);
        }
    }
}
