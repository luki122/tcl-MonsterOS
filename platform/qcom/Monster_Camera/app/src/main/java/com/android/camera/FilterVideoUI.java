/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.FocusOverlay;
import com.android.camera.ui.IntentReviewControls;
import com.android.camera.ui.ModuleLayoutWrapper;
import com.android.camera.ui.PreviewOverlay;
import com.android.camera.ui.PreviewStatusListener;
import com.android.camera.ui.RecordTimeLayout;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotatableButton;
import com.android.camera.ui.RotateLayout;
import com.android.camera.ui.ZoomBar;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraSettings;
import com.tct.camera.R;

public class FilterVideoUI implements ModuleLayoutWrapper.OnAllViewRemovedListener{
    private static final Log.Tag TAG = new Log.Tag("FilterVideoUI");
    private final static float UNSET = 0f;
    // module fields
    private final CameraActivity mActivity;
    protected final View mRootView;
    private final RotatableButton mVideoCaptureButton;
    private final RotatableButton mVideoPauseButton;
    private TextView mRecordingTimeView;
    private TextView mTimeLeftView;
    private RecordTimeLayout mRecordingTimeRect;
    private boolean mRecordingStarted = false;
    private boolean mDotbBlinkVisible;
    private final PhotoController mController;

    private final AnimationManager mAnimationManager;
    private ButtonClickListener mButtonClickListener;
    private float mAspectRatio = UNSET;


    public FilterVideoUI(CameraActivity activity, PhotoController controller, View parent) {
        mActivity = activity;
        mController = controller;
        mRootView = parent;
        ModuleLayoutWrapper moduleRoot = (ModuleLayoutWrapper) mRootView.findViewById(R.id.module_layout);
        moduleRoot.setOnAllViewRemovedListener(this, mActivity);
        mActivity.getLayoutInflater().inflate(R.layout.filter_video_module,
                moduleRoot, true);

        initializeMiscControls();
        mAnimationManager = new AnimationManager();
        mVideoCaptureButton=(RotatableButton)mRootView.findViewById(R.id.video_snap_button);
        mVideoCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mActivity.getHelpTipsManager() != null){
                    mActivity.getHelpTipsManager().goNextHelpTipStage();
                }
                if (mButtonClickListener != null) {
                    mButtonClickListener.doVideoCapture();
                }
            }
        });

        mActivity.addLockableToListenerPool(mVideoCaptureButton);
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mRecordingTimeRect, true));

        mVideoPauseButton = (RotatableButton)mRootView.findViewById(R.id.pause_record);
        mVideoPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mButtonClickListener != null) {
                    mButtonClickListener.onVideoPauseButtonClicked();
                }
            }
        });

        mActivity.addLockableToListenerPool(mVideoPauseButton);
    }

    /**
     * @return The size of the available preview area.
     */
    public Point getPreviewScreenSize() {
        return new Point(mRootView.getMeasuredWidth(), mRootView.getMeasuredHeight());
    }
    /**
     * Sets the ButtonStatusListener.
     */
    public void setListener(ButtonClickListener listener) {
        mButtonClickListener = listener;
    }
    private void initializeMiscControls() {
        mRecordingTimeView = (TextView) mRootView.findViewById(R.id.recording_time);
        mTimeLeftView = (TextView) mRootView.findViewById(R.id.time_left_view);
        mRecordingTimeRect = (RecordTimeLayout) mRootView.findViewById(R.id.recording_time_rect);
    }

    /**
     * Starts a flash animation
     */
    public void animateFlash() {
        mController.startPreCaptureAnimation();
    }
    /**
     * Cancels on-going animations
     */
    public void cancelAnimations() {
        mAnimationManager.cancelAnimations();
    }
    public void setSwipingEnabled(boolean enable) {
        mActivity.setSwipingEnabled(enable);
    }

    public void showRecordingUI(boolean recording) {
        mRecordingStarted = recording;
        if (recording) {
            Log.w(TAG,"show recording UI");
            mRecordingTimeView.setText("");
            mRecordingTimeView.setVisibility(View.VISIBLE);
            mRecordingTimeView.announceForAccessibility(
                    mActivity.getResources().getString(R.string.video_recording_started));
        } else {
            mRecordingTimeView.announceForAccessibility(
                    mActivity.getResources().getString(R.string.video_recording_stopped));
            mRecordingTimeView.setVisibility(View.GONE);
        }
    }
    public void unlockCaptureView(){
        if(mVideoCaptureButton!=null) {
            mVideoCaptureButton.unLockSelf();
        }
        if (mVideoPauseButton!=null) {
            mVideoPauseButton.unLockSelf();
        }
    }
    public void setTimeLeftUI(boolean needTimeLapse, String leftTime) {
        if (mTimeLeftView == null) {
            return;
        }
        if (needTimeLapse) {
            if (!"".equals(leftTime)) {
                String timeLeft = mActivity.getResources().getString(
                        R.string.time_left) + leftTime;
                mTimeLeftView.setAllCaps(true);
                mTimeLeftView.setText(timeLeft);
                mTimeLeftView.setVisibility(View.VISIBLE);
            }
        } else {
            mTimeLeftView.setText("");
            mTimeLeftView.setVisibility(View.GONE);
        }
    }

    public void showPausedUI(boolean videoRecordingPaused) {
        CharSequence mCharSequence;
        if (videoRecordingPaused) {
            mCharSequence = mActivity.getResources().getString(R.string.video_recording_paused);
            mVideoPauseButton.setImageResource(R.drawable.ic_video_resume);
        } else {
            mCharSequence = mActivity.getResources().getString(R.string.video_recording_started);
            mVideoPauseButton.setImageResource(R.drawable.ic_video_pause);
        }
        mVideoPauseButton.setContentDescription(mCharSequence);
    }

    public void lockRecordingOrientation() {
        mActivity.lockRotatableOrientation(mRecordingTimeRect.hashCode());
        mActivity.getCameraAppUI().lockButtonOrientation();
    }

    public void unlockRecordingOrientation() {
        mActivity.unlockRotatableOrientation(mRecordingTimeRect.hashCode());
        mActivity.getCameraAppUI().unlockButtonOrientation();
    }


    public void setRecordingTime(String text) {
        mRecordingTimeView.setText(text);
    }

    public void setRecordingTimeTextColor(int color) {
        mRecordingTimeView.setTextColor(color);
    }

    public void setDotbBlink(boolean videoRecordingPaused) {
        if (mRecordingStarted) {
            Drawable drawable;
            if (!videoRecordingPaused) {
                if(mDotbBlinkVisible){
                    mDotbBlinkVisible = false;
                    drawable= mActivity.getResources().getDrawable(R.drawable.ic_recording_indicator_blink_oval);
                } else{
                    mDotbBlinkVisible = true;
                    drawable= mActivity.getResources().getDrawable(R.drawable.ic_recording_indicator_oval);
                }
            } else {
                drawable= mActivity.getResources().getDrawable(R.drawable.ic_recording_indicator_oval);
            }
            mRecordingTimeView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
    }


    @Override
    public void onAllViewRemoved(AppController controller) {
        controller.removeLockableFromListenerPool(mVideoCaptureButton);
        if (mVideoPauseButton != null) {
            controller.removeLockableFromListenerPool(mVideoPauseButton);
        }
        controller.removeRotatableFromListenerPool(mRecordingTimeRect.hashCode());
    }


    public interface ButtonClickListener{
        /**
         * Starts to capture when taking a video.
         */
        public void doVideoCapture();

        public void onVideoPauseButtonClicked();
    }

    public void setAspectRatio(float ratio) {
        if (ratio <= 0) {
            return;
        }
        float aspectRatio = ratio > 1 ? ratio : 1 / ratio;
        if (aspectRatio != mAspectRatio) {
            mAspectRatio = aspectRatio;
            mController.updatePreviewAspectRatio(mAspectRatio);
        }
    }
    public void onPause() {
        // recalculate aspect ratio when restarting.
        mAspectRatio = 0.0f;
    }
    public void setPreviewSize(int width, int height) {
        if (width == 0 || height == 0) {
            Log.w(TAG, "Preview size should not be 0.");
            return;
        }
        float aspectRatio;
        if (width > height) {
            aspectRatio = (float) width / height;
        } else {
            aspectRatio = (float) height / width;
        }
        setAspectRatio(aspectRatio);
    }

    public void stopRecording() {

        mActivity.stopBatteryInfoChecking();
        mActivity.stopInnerStorageChecking();
        setSwipingEnabled(true);
//        unlockRecordingOrientation();
//        mActivity.unlockOrientation();
        showRecordingUI(false);
        mActivity.enableKeepScreenOn(false);
        mActivity.updateStorageSpaceAndHint(null);
        setTimeLeftUI(false, "");
    }
}
