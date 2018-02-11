package com.android.camera.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.android.camera.AnimationManager;
import com.tct.camera.R;

public class FaceBeautyOption extends LinearLayout implements View.OnClickListener {
    private static final String TAG = "FaceBeautyOption";
    private SeekBar mSeekBar;
    private View mCustomSeekBar;
    private RotateImageView mFacebeautyMenu;

    private ValueAnimator mHideSeekbarAnimator;
    private ValueAnimator mShowSeekbarAnmator;
    private String mKey;
    private int mProgress;
    public FaceBeautyOption(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSeekBar = (SeekBar) findViewById(R.id.customseekbar);
        mCustomSeekBar = findViewById(R.id.seekbar);
        mFacebeautyMenu = (RotateImageView)findViewById(R.id.face_beauty_menu);
        mFacebeautyMenu.setOnClickListener(this);
    }

    public void initData(String key, int defaultValue, int maxValue) {
        mKey = key;
        mSeekBar.setMax(maxValue);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
                mHandler.sendEmptyMessageDelayed(HIDE_MANUAL_PROGRESS, HIDE_MANUAL_PROGRESS_DELAY);
                mHandler.removeMessages(UPDATE_FACEBEAUTY_SETTING);
                mHandler.sendEmptyMessage(UPDATE_FACEBEAUTY_SETTING);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                mProgress = progress;
                mHandler.removeMessages(UPDATE_FACEBEAUTY_SETTING);
                mHandler.sendEmptyMessageDelayed(UPDATE_FACEBEAUTY_SETTING, UPDATE_FACEBEAUTY_SETTING_DELAY);
            }
        });
        mProgress = defaultValue;
        mSeekBar.setProgress(defaultValue);
    }

    public void onClick(View v) {
        if (mCustomSeekBar.getVisibility() == View.VISIBLE) {
            mFacebeautyMenu.setImageLevel(0);
            animateHide();
            mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
        } else {
            mFacebeautyMenu.setImageLevel(1);
            animateShow();
            mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
            mHandler.sendEmptyMessageDelayed(HIDE_MANUAL_PROGRESS, HIDE_MANUAL_PROGRESS_DELAY);
        }
    }

    public void hideSeekBar() {
        if (mCustomSeekBar.getVisibility() == View.VISIBLE) {
            mFacebeautyMenu.setImageLevel(0);
            animateHide();
            mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE && mHandler != null  && mHandler.hasMessages(HIDE_MANUAL_PROGRESS)) {
            mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
        }
    }

    private void animateHide(){
        if(mCustomSeekBar.getVisibility()==View.GONE){
            return;
        }
        if(mHideSeekbarAnimator ==null|| mShowSeekbarAnmator ==null){
            mShowSeekbarAnmator = AnimationManager.buildShowingAnimator(mCustomSeekBar);
            mHideSeekbarAnimator = AnimationManager.buildHidingAnimator(mCustomSeekBar);
        }
        if(mShowSeekbarAnmator.isRunning()){
            mShowSeekbarAnmator.cancel();
        }
        if(!mHideSeekbarAnimator.isRunning()){
            mHideSeekbarAnimator.start();
        }
    }

    private void animateShow(){
        if(mCustomSeekBar.getVisibility()==View.VISIBLE){
            return;
        }
        if(mHideSeekbarAnimator ==null|| mShowSeekbarAnmator ==null){
            mShowSeekbarAnmator = AnimationManager.buildShowingAnimator(mCustomSeekBar);
            mHideSeekbarAnimator = AnimationManager.buildHidingAnimator(mCustomSeekBar);
        }
        if(mHideSeekbarAnimator.isRunning()){
            mHideSeekbarAnimator.cancel();
        }

        if(!mShowSeekbarAnmator.isRunning()){
            mShowSeekbarAnmator.start();
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case HIDE_MANUAL_PROGRESS:
                    animateHide();
                    mFacebeautyMenu.setImageLevel(0);
                    break;
                case UPDATE_FACEBEAUTY_SETTING:
                    if (mCallback != null) {
                        mCallback.updateFaceBeautySetting(mKey, mProgress);
                    }
                    break;

            }
        }
    };
    public void setFaceBeautySettingCallBack(FaceBeautySettingCallBack callback) {
        mCallback = callback;
    }
    public void reset() {
        mCallback = null;
        if (mHandler != null) {
            mHandler.removeMessages(HIDE_MANUAL_PROGRESS);
            mHandler.removeMessages(UPDATE_FACEBEAUTY_SETTING);
        }
    }
    private static final int HIDE_MANUAL_PROGRESS = 0;
    private static final int HIDE_MANUAL_PROGRESS_DELAY = 3000;
    private static final int UPDATE_FACEBEAUTY_SETTING = 1;
    private static final int UPDATE_FACEBEAUTY_SETTING_DELAY = 100;

    private FaceBeautySettingCallBack mCallback;

    public interface FaceBeautySettingCallBack{
        void updateFaceBeautySetting(String key, int value);
    }
}
