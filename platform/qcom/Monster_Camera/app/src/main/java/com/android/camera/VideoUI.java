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
import android.graphics.PointF; // MODIFIED by xuan.zhou, 2016-10-31,BUG-3178440
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class VideoUI implements PreviewStatusListener ,ModuleLayoutWrapper.OnAllViewRemovedListener{
    private static final Log.Tag TAG = new Log.Tag("VideoUI");

    private final static float UNSET = 0f;
    private final PreviewOverlay mPreviewOverlay;
    // module fields
    private final CameraActivity mActivity;
    protected final View mRootView;
    private final FocusOverlay mFocusUI;
    private final RotatableButton mVideoCaptureButton;
    private final RotatableButton mVideoPauseButton;
    // An review image having same size as preview. It is displayed when
    // recording is stopped in capture intent.
    private ImageView mReviewImage;
    private TextView mRecordingTimeView;
    private TextView mTimeLeftView;
    private RelativeLayout mLabelsLinearLayout;
    private RecordTimeLayout mRecordingTimeRect;
    private boolean mRecordingStarted = false;
    private boolean mDotbBlinkVisible;
    private final VideoController mController;
    private float mZoomMax;
    private final ZoomBar mZoomBar;

    private final RotateLayout mImageReviewLayout;
    private final RotateLayout mIntentReviewLayout;

    private final IntentReviewControls mIntentReviewControls;

    private float mAspectRatio = UNSET;
    private final AnimationManager mAnimationManager;

    private ValueAnimator va;
    private boolean mStop = true;
    private int mBoomKeyTipRepeatCount = 0;
    private View boomkeyTip;
    private static final int DISMISS_BOOK_KEY_TIP_DELAY = 3000;
    private static final int BOOK_KEY_TIP_ANIMATION_DELAY = 1000;
    private static final int BOOK_KEY_TIP_ANIMATION_DURATION = 400;
    private int ORIENTATION_ANGLE_180 = 180;// MODIFIED by nie.lei, 2016-03-25,BUG-1850478


    @Override
    public void onPreviewLayoutChanged(View v, int left, int top, int right,
            int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
    }

    @Override
    public boolean shouldAutoAdjustTransformMatrixOnLayout() {
        return true;
    }

    @Override
    public boolean shouldAutoAdjustBottomBar() {
        return true;
    }

    @Override
    public void onPreviewFlipped() {
        mController.updateCameraOrientation();
    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-31,BUG-3178440*/
    private PointF mTouchDownPos = new PointF();

    private final GestureDetector.OnGestureListener mPreviewGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            if (mTouchDownPos == null) {
                mTouchDownPos = new PointF();
            }
            mTouchDownPos.set(e.getX(), e.getY());
            return super.onDown(e);
        }

        @Override
        /* MODIFIED-BEGIN by jianying.zhang, 2016-10-26,BUG-3212745*/
        public boolean onSingleTapUp(MotionEvent ev) {
            if (!mActivity.getCameraAppUI().hideZoomBar() && isTapUpWithoutScrolling(ev)) {
                mController.onSingleTapUp(null, (int) ev.getX(), (int) ev.getY());
            }
            return true;
        }
    };

    private boolean isTapUpWithoutScrolling(MotionEvent ev) {
        if (ev == null || mTouchDownPos == null) {
            return false;
        }

        final float TOLERANCE = mActivity.getResources().getDimension(
                R.dimen.focus_outer_circle_radius) / 2;

        float distance = CameraUtil.getDistance(mTouchDownPos, new PointF(ev.getX(), ev.getY()));
        return (distance < TOLERANCE);
    }
    /* MODIFIED-END by xuan.zhou,BUG-3178440*/

    public VideoUI(CameraActivity activity, VideoController controller, View parent) {
        mActivity = activity;
        mController = controller;
        mRootView = parent;
        ModuleLayoutWrapper moduleRoot = (ModuleLayoutWrapper) mRootView.findViewById(R.id.module_layout);
        moduleRoot.setOnAllViewRemovedListener(this, mActivity);
        mActivity.getLayoutInflater().inflate(R.layout.video_module,
                moduleRoot, true);

        mPreviewOverlay = (PreviewOverlay) mRootView.findViewById(R.id.preview_overlay);
        mZoomBar = (ZoomBar) mRootView.findViewById(R.id.zoom_bar);
        mZoomBar.setProgressChangeListener(new ZoomBar.ProgressChangeListener() {
            @Override
            public void onProgressChanged(float ratio) {
                mPreviewOverlay.setRatio(ratio);
                mController.onZoomChanged(ratio);
            }
        });

        mImageReviewLayout = (RotateLayout) mRootView.findViewById(R.id.intent_review_imageview_layout);
        mIntentReviewLayout = (RotateLayout) mRootView.findViewById(R.id.intent_review_rotate_layout);
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mImageReviewLayout, true));
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mIntentReviewLayout, false));

        mIntentReviewControls = (IntentReviewControls) mRootView.findViewById(R.id.intent_review_controls);
        mIntentReviewControls.setFromVideoUI(true);
        initializeMiscControls();
        mAnimationManager = new AnimationManager();
        mFocusUI = (FocusOverlay) mRootView.findViewById(R.id.focus_overlay);
        boomkeyTip = mRootView.findViewById(R.id.boom_key_tip);// MODIFIED by nie.lei, 2016-03-25,BUG-1850478
        mVideoCaptureButton=(RotatableButton)mRootView.findViewById(R.id.video_snap_button);
        mVideoCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mActivity.getHelpTipsManager() != null){
                    mActivity.getHelpTipsManager().goNextHelpTipStage();
                }

                if(!mStop){
                   hideBoomKeyTipUI();
                }

                mController.doVideoCapture();

            }
        });

        mActivity.addLockableToListenerPool(mVideoCaptureButton);
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mRecordingTimeRect, true));

        mVideoPauseButton = (RotatableButton)mRootView.findViewById(R.id.pause_record);
        mVideoPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.onVideoPauseButtonClicked();
            }
        });

        mActivity.addLockableToListenerPool(mVideoPauseButton);
    }


    public void unlockCaptureView(){
        if(mVideoCaptureButton!=null) {
            mVideoCaptureButton.unLockSelf();
        }
        if (mVideoPauseButton!=null) {
            mVideoPauseButton.unLockSelf();
        }
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

    public FocusOverlayManager.FocusUI getFocusUI() {
        return mFocusUI;
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

    public void setOrientationIndicator(int orientation, boolean animation) {
        // We change the orientation of the linearlayout only for phone UI
        // because when in portrait the width is not enough.
//        if (mLabelsLinearLayout != null) {
//            if (((orientation / 90) & 1) == 0) {
//                mLabelsLinearLayout.setOrientation(LinearLayout.VERTICAL);
//            } else {
//                mLabelsLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
//            }
//        }
    }

    private void initializeMiscControls() {
        // Use the imageview in cameraappui
        // mReviewImage = (ImageView) mRootView.findViewById(R.id.review_image);
        mRecordingTimeView = (TextView) mRootView.findViewById(R.id.recording_time);
        mTimeLeftView = (TextView) mRootView.findViewById(R.id.time_left_view);
        mRecordingTimeRect = (RecordTimeLayout) mRootView.findViewById(R.id.recording_time_rect);
        mReviewImage = (ImageView) mRootView.findViewById(R.id.intent_review_imageview);
        // The R.id.labels can only be found in phone layout.
        // That is, mLabelsLinearLayout should be null in tablet layout.
        mLabelsLinearLayout = (RelativeLayout) mRootView.findViewById(R.id.labels);
    }

    public void updateOnScreenIndicators(CameraSettings settings) {
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

    public void setSwipingEnabled(boolean enable) {
        mActivity.setSwipingEnabled(enable);
    }

    public void showPreviewBorder(boolean enable) {
       // TODO: mPreviewFrameLayout.showBorder(enable);
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

    public void showBoomKeyTipUI() {
        if(!CustomUtil.getInstance().getBoolean(CustomFields.DEF_VIDEO_RECORDING_BOOMKEY_TIP_ON, false)){
            return;
        }
        String packageName = CameraUtil.TIZR_PACKAGE_NAME;
        PackageManager manager = mActivity.getPackageManager();
        Intent intent = manager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            Log.e(TAG, "No " + packageName + " installed.");
            return;
        }

        final SettingsManager settingsManager = mActivity.getSettingsManager();
        boolean bVideoBoomKeyTip = !settingsManager.isSet(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_VIDEO_BOOM_KEY_TIP);
        if (!mActivity.isReversibleWorking() && bVideoBoomKeyTip) {// MODIFIED by nie.lei, 2016-03-25,BUG-1850478
            mStop = false;
            showBoomKeyTipAnimation();
            settingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_VIDEO_BOOM_KEY_TIP, true);
        }
    }

    private void showBoomKeyTipAnimation() {
        boomkeyTip.setVisibility(View.VISIBLE);
        boomkeyTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.boom_key_tip) {
                    hideBoomKeyTipUI();
                }
            }
        });
        boomkeyTip.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideBoomKeyTipUI();
            }
        }, DISMISS_BOOK_KEY_TIP_DELAY);

        String keyBoomKeyTip = CustomFields.DEF_VIDEO_RECORDING_BOOMKEY_TIP_CUSTOMIZE;
        int defaultMarginTop = mActivity.getResources().getInteger(R.integer.video_recording_boom_key_tip_margin_top_default);
        int boomKeyTipMarginTop =  CustomUtil.getInstance().getInt(keyBoomKeyTip, defaultMarginTop);
        boomkeyTip.setPadding(0, boomKeyTipMarginTop, 0, 0);

        final View boomKeyBtn = boomkeyTip.findViewById(R.id.boom_key_btn);
        boomKeyBtn.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boomKeyBtn.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                final float boomKeyBtnWidth = (float) boomKeyBtn.getMeasuredWidth();
                va = ofFloat(boomKeyBtn, "translationX", 0f, boomKeyBtnWidth);
                va.setRepeatMode(ValueAnimator.REVERSE);
                va.setRepeatCount(ValueAnimator.INFINITE);
                va.setDuration(BOOK_KEY_TIP_ANIMATION_DURATION);
                va.addListener(new AnimatorListenerAdapter() {
                                   public void onAnimationRepeat(Animator animation) {
                                       //view's translationX will be near to the end, but does not equal.
                                       if (!mStop) {
                                           if (boomKeyBtn.getTranslationX() > boomKeyBtnWidth / 2) {
                                               mBoomKeyTipRepeatCount++;
                                               if (mBoomKeyTipRepeatCount % 2 == 0) {
                                                   animation.cancel();
                                                   animation.setStartDelay(BOOK_KEY_TIP_ANIMATION_DELAY);
                                                   animation.start();
                                               }
                                           }
                                       } else {
                                           animation.cancel();
                                       }
                                   }
                               }

                );
                va.start();
            }
        });
    }

    public void hideBoomKeyTipUI(){
        if(!CustomUtil.getInstance().getBoolean(CustomFields.DEF_VIDEO_RECORDING_BOOMKEY_TIP_ON, false)){
            return;
        }

        stopAnimation();
        if (boomkeyTip != null && boomkeyTip.getVisibility() == View.VISIBLE) {
            boomkeyTip.setVisibility(View.GONE);
        }
        if(va != null ){
            if(va.isRunning()){
                va.cancel();
            }

            va = null;
        }
    }

    private static ObjectAnimator ofFloat(View target, String propertyName, float... values) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.setTarget(target);
        anim.setPropertyName(propertyName);
        anim.setFloatValues(values);
        return anim;
    }

    private void stopAnimation() {
        mStop = true;
    }

    public void showReviewImage(Bitmap bitmap) {
        mReviewImage.setImageBitmap(bitmap);
        mReviewImage.setVisibility(View.VISIBLE);
    }

    public void showReviewControls() {
        mActivity.getCameraAppUI().transitionToIntentReviewLayout();
        mReviewImage.setVisibility(View.VISIBLE);
    }

    public void initializeZoom(CameraSettings settings, CameraCapabilities capabilities) {
        mActivity.getCameraAppUI().resetZoomBar(); // MODIFIED by jianying.zhang, 2016-11-01,BUG-3271894
        mZoomMax = capabilities.getMaxZoomRatio();
        // Currently we use immediate zoom for fast zooming to get better UX and
        // there is no plan to take advantage of the smooth zoom.
        // TODO: setup zoom through App UI.
        boolean bSupportFrontPinchZoom = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_FRONTFACING_PINCH_ZOOM,false);
        boolean bFrontFacing = !Keys.isCameraBackFacing(mActivity.getSettingsManager(), SettingsManager.SCOPE_GLOBAL);
        if (bFrontFacing && !bSupportFrontPinchZoom) {
            mPreviewOverlay.setupZoom(mZoomMax, settings.getCurrentZoomRatio(),
                    null);
        }else {
            mPreviewOverlay.setupZoom(mZoomMax, settings.getCurrentZoomRatio(),
                    new ZoomChangeListener());
            if (mZoomBar != null) {
                mZoomBar.setZoomMax(mZoomMax);
            }
        }
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

    public boolean isVisible() {
        return false;
    }

    @Override
    public GestureDetector.OnGestureListener getGestureListener() {
        return mPreviewGestureListener;
    }

    @Override
    public void clearEvoPendingUI() {

    }

    @Override
    public View.OnTouchListener getTouchListener() {
        return null;
    }

    /**
     * Shows or hides focus UI.
     *
     * @param show shows focus UI when true, hides it otherwise
     */
    public void showFocusUI(boolean show) {
        if (mFocusUI != null) {
            mFocusUI.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    /**
     * Shows or hides video recording hints.
     *
     * @param show shows video recording hints when true, hides it otherwise.
     */
    public void showVideoRecordingHints(boolean show) {
//        mVideoHints.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * @return The size of the available preview area.
     */
    public Point getPreviewScreenSize() {
        return new Point(mRootView.getMeasuredWidth(), mRootView.getMeasuredHeight());
    }

    public void onOrientationChanged(int orientation) {
        if (mIntentReviewControls != null) {
            mIntentReviewControls.setLayoutOrientation(orientation);
        }

        /* MODIFIED-BEGIN by nie.lei, 2016-03-25,BUG-1850478 */
        if(mActivity.getServices().isReversibleEnabled() && orientation == ORIENTATION_ANGLE_180){
            hideBoomKeyTipUI();
        }
        /* MODIFIED-END by nie.lei,BUG-1850478 */
    }

    @Override
    public void onAllViewRemoved(AppController controller) {
        controller.removeLockableFromListenerPool(mVideoCaptureButton);
        if (mVideoPauseButton != null) {
            controller.removeLockableFromListenerPool(mVideoPauseButton);
        }
        controller.removeRotatableFromListenerPool(mRecordingTimeRect.hashCode());
        controller.removeRotatableFromListenerPool(mImageReviewLayout.hashCode());
        controller.removeRotatableFromListenerPool(mIntentReviewLayout.hashCode());
    }

    private class ZoomChangeListener implements PreviewOverlay.OnZoomChangedListener {
        @Override
        public void onZoomValueChanged(float ratio) {
            if (mZoomBar != null) {
                mZoomBar.zoomRatioChanged(ratio);
            }
            mController.onZoomChanged(ratio);
        }

        @Override
        public void onZoomStart() {
        }

        @Override
        public void onZoomEnd() {
        }
    }

    // SurfaceTexture callbacks
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mController.onPreviewUIReady();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mController.onPreviewUIDestroyed();
        Log.d(TAG, "surfaceTexture is destroyed");
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void onPause() {
        // recalculate aspect ratio when restarting.
        mAspectRatio = 0.0f;
        if(!mStop){
            hideBoomKeyTipUI();
        }
    }
}
