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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF; // MODIFIED by xuan.zhou, 2016-10-31,BUG-3178440
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.os.AsyncTask;
import android.text.Html;
import android.util.SparseArray; // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.camera.FocusOverlayManager.FocusUI;
import com.android.camera.app.AppController;
import com.android.camera.debug.DebugPropertyHelper;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.CountDownView;
import com.android.camera.ui.EvoSlider;
import com.android.camera.ui.ExposureSidebar;
import com.android.camera.ui.FaceBeautyOption;
import com.android.camera.ui.FaceView;
import com.android.camera.ui.FocusOverlay;
import com.android.camera.ui.GesturePalmOption;
import com.android.camera.ui.GestureView;
import com.android.camera.ui.IntentReviewControls;
import com.android.camera.ui.LockRotatableButton;
import com.android.camera.ui.MT_FaceBeautyOptionFS;
import com.android.camera.ui.MT_FaceBeautyOptionFW;
import com.android.camera.ui.ModuleLayoutWrapper;
import com.android.camera.ui.PreviewOverlay;
import com.android.camera.ui.PreviewStatusListener;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotatableButton;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.RotateLayout;
import com.android.camera.ui.SoundGroup;
import com.android.camera.ui.ZoomBar;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.GservicesHelper;
import com.android.camera.widget.AspectRatioDialogLayout;
import com.android.camera.widget.AspectRatioSelector;
import com.android.camera.widget.ButtonGroup;
import com.android.camera.widget.LocationDialogLayout;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.ex.camera2.portability.Size;
import com.android.external.ExtSystemProperties;
import com.android.external.plantform.ExtBuild;
import com.tct.camera.R;

import java.util.ArrayList;
import java.util.List;


public class PhotoUI implements PreviewStatusListener,
        CameraAgent.CameraFaceDetectionCallback,
        PreviewStatusListener.PreviewAreaChangedListener,
        ModuleLayoutWrapper.OnAllViewRemovedListener, CameraActivity.ControlPoseCallback {

    private static final Log.Tag TAG = new Log.Tag("PhotoUI");
    private static final int DOWN_SAMPLE_FACTOR = 4;
    private static final float UNSET = 0f;

    private final PreviewOverlay mPreviewOverlay;
    private final FocusUI mFocusUI;
    private final EvoSlider mEvoSlider;
    private final TextView mBurstCountView;
    private final RotateLayout mBurstCountLayout;
    private final ZoomBar mZoomBar;
    private final RotateLayout mImageReviewLayout;
    private final RotateLayout mIntentReviewLayout;
    private final IntentReviewControls mIntentReviewControls;
    protected final CameraActivity mActivity;
    private final PhotoController mController;
    private final TextView mModeOptionsTipView;
    /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
    private FaceBeautyOption mFaceBeautyOption;
    private RotateImageView mFaceBeautyMenu;
    private MT_FaceBeautyOptionFS mFaceBeautyOptionFs;
    private RotateImageView mFaceBeautyMenuFs;
    private MT_FaceBeautyOptionFW mFaceBeautyOptionFw;
    private RotateImageView mFaceBeautyMenuFw;
    /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/

    private final GesturePalmOption mGesturePalmOption;
    private final RotateImageView mGesturePalm;
    private final View mRootView;
    private LinearLayout mGestureToastLayout; // MODIFIED by jianying.zhang, 2016-05-25,BUG-2202266
    private Dialog mDialog = null;
    /* MODIFIED-BEGIN by peixin, 2016-05-09,BUG-2011866*/
    private final FocusOverlay mFocusOverlay;
    private static final int SHOWFOCUS_UI = 0;
    /* MODIFIED-END by peixin,BUG-2011866*/

    // TODO: Remove face view logic if UX does not bring it back within a month.
    private final FaceView mFaceView;
    private final GestureView mGestureView;
    private DecodeImageForReview mDecodeTaskForReview = null;

    private float mZoomMax;

    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;
    private float mAspectRatio = UNSET;

    private ImageView mIntentReviewImageView;

    private SoundGroup mSoundGroup;

    // The info text shown in bottom bar.
    private TextView mInfoText;

    private final ExposureSidebar mExposureSidebar;

    private LockRotatableButton mAspectRatioButton;
    private ButtonGroup mAspectRatioGroup;

    private View mBottomBarLine;
    private PoseAndCompositionUI mPoseUI;

    private ShutterButton mContactsIntentShutterButton;

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
        public boolean onSingleTapUp(MotionEvent ev) {
            /* MODIFIED-BEGIN by xuan.zhou, 2016-11-03,BUG-3311864*/
            if (!mActivity.getCameraAppUI().hideZoomBar() && isTapUpWithoutScrolling(ev)) {
                if (mController.isFilterSelectorScreen()) {
                    mController.onSingleTapUp(null, (int) ev.getX(), (int) ev.getY());
                } else {
                    PointF coordinate = getRevisedCoordinate(ev.getX(), ev.getY());
                    if (!coordinate.equals(UNSET, UNSET)) {
                        mController.onSingleTapUp(null, (int) coordinate.x, (int) coordinate.y);
                    }
                }
                /* MODIFIED-END by xuan.zhou,BUG-3311864*/
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            /* MODIFIED-BEGIN by xuan.zhou, 2016-11-03,BUG-3311864*/
            PointF coordinate = getRevisedCoordinate(e.getX(), e.getY());
            if (!coordinate.equals(UNSET, UNSET)) {
                mController.onLongPress((int) coordinate.x, (int) coordinate.y);
                /* MODIFIED-END by xuan.zhou,BUG-3311864*/
            }
        }
    };

    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-31,BUG-3178440*/
    private boolean isTapUpWithoutScrolling(MotionEvent ev) {
        // If the action of motion event is ACTION_UP, the history size get here is 0 so I have to
        // mark the touch down position.
        if (ev == null || mTouchDownPos == null) {
            return false;
        }

        final float TOLERANCE = mActivity.getResources().getDimension(
                R.dimen.focus_outer_circle_radius) / 2;

        float distance = CameraUtil.getDistance(mTouchDownPos, new PointF(ev.getX(), ev.getY()));
        return (distance < TOLERANCE);
    }
    /* MODIFIED-END by xuan.zhou,BUG-3178440*/

        /* MODIFIED-BEGIN by xuan.zhou, 2016-11-03,BUG-3311864*/
        private PointF getRevisedCoordinate(float x, float y) {
            RectF clickableRectF = mActivity.getCaptureLayoutHelper().getPreviewRect();

            if (clickableRectF.isEmpty()) {
                // Preview area is empty, ignore it.
                return new PointF(UNSET, UNSET);
            }

            if (mActivity.getCameraAppUI().isBottomBarVisible()) {
                // For aspect ratio is 16:9.
                RectF bottomBarRectF = mActivity.getCaptureLayoutHelper().getBottomBarRect();
                clickableRectF.bottom = Math.min(bottomBarRectF.bottom, bottomBarRectF.top);
            }

            if (mActivity.getCameraAppUI().isTopMenusVisible()) {
                float topBarHeight = mActivity.getResources().getDimension(R.dimen.mode_options_height);
                if (clickableRectF.top < topBarHeight) {
                    clickableRectF.top = topBarHeight;
                }
            }

            if (!clickableRectF.contains(x, y)) {
                // If the touch position is not in the clickable area, ignore it.
                return new PointF(UNSET, UNSET);
            }

            float focusRadius = 0.5f *
                    mActivity.getResources().getDimension(R.dimen.focus_outer_ring_size);

            // Avoid the focus circle shown incomplete.
            RectF borderRectF = new RectF(clickableRectF.left + focusRadius,
                    clickableRectF.top + focusRadius,
                    clickableRectF.right - focusRadius,
                    clickableRectF.bottom - focusRadius);

            if (borderRectF.isEmpty()) {
                // Wrong rectF, the focus circle is bigger than the clickable area.
                return new PointF(UNSET, UNSET);
            }

            if (!borderRectF.contains(x, y)) {
                // If (x, y) is not contained, revise the coordinate.
                if (x < borderRectF.left) {
                    x = borderRectF.left;
                } else if (x > borderRectF.right) {
                    x = borderRectF.right;
                }

                if (y < borderRectF.top) {
                    y = borderRectF.top;
                } else if (y > borderRectF.bottom) {
                    y = borderRectF.bottom;
                }
            }

            return new PointF(x, y);
            /* MODIFIED-END by xuan.zhou,BUG-3311864*/
        }

    private final View.OnTouchListener mPreviewTouchListener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (isMeteringShowing() && trackMeteringTouchEvent(event)) {
                return false;
            }
            if(mEvoSlider.getVisibility()!=View.GONE){
                if(event.getAction()==MotionEvent.ACTION_MOVE){
                    mActivity.getCameraAppUI().hideZoomBar(); // MODIFIED by jianying.zhang, 2016-10-27,BUG-3212745
                    return mEvoSlider.onTouchEvent(event);
                }
            }
            return false;
        }
    };

    private final DialogInterface.OnDismissListener mOnDismissListener
            = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            mDialog = null;
        }
    };
    private Runnable mRunnableForNextFrame = null;
    private final CountDownView mCountdownView;

    @Override
    public GestureDetector.OnGestureListener getGestureListener() {
        return mPreviewGestureListener;
    }

    @Override
    public void clearEvoPendingUI() {
        mEvoSlider.resetSlider();
        if(mEvoSlider.getVisibility()==View.VISIBLE){
            mEvoSlider.setVisibility(View.GONE);
        }
    }

    @Override
    public View.OnTouchListener getTouchListener() {
        return mPreviewTouchListener;
    }

    @Override
    public void onPreviewLayoutChanged(View v, int left, int top, int right,
            int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int width = right - left;
        int height = bottom - top;
        if (mPreviewWidth != width || mPreviewHeight != height) {
            mPreviewWidth = width;
            mPreviewHeight = height;
        }
        mEvoSlider.setBound(width, height);
    }

    public void parseEvoBound(int max ,int min){
        mEvoSlider.setValueBound(max, min);
    }

    public void resetEvoSlider(int evo){
        mEvoSlider.resetSlider(evo);
    }

    public void showEvoSlider(){
        mEvoSlider.setVisibility(View.VISIBLE);
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

    /**
     * Sets the runnable to run when the next frame comes in.
     */
    public void setRunnableForNextFrame(Runnable runnable) {
        mRunnableForNextFrame = runnable;
    }

    /**
     * Starts the countdown timer.
     *
     * @param sec seconds to countdown
     */
    public void startCountdown(int sec) {
        mCountdownView.startCountDown(sec);
    }

    /**
     * Sets a listener that gets notified when the countdown is finished.
     */
    public void setCountdownFinishedListener(CountDownView.OnCountDownStatusListener listener) {
        mCountdownView.setCountDownStatusListener(listener);
    }

    /**
     * Returns whether the countdown is on-going.
     */
    public boolean isCountingDown() {
        return mCountdownView.isCountingDown();
    }

    /**
     * Cancels the on-going countdown, if any.
     */
    public void cancelCountDown() {
        mCountdownView.cancelCountDown();
    }
    @Override
    public void onPreviewAreaChanged(RectF previewArea) {
        if (mFaceView != null) {
            mFaceView.onPreviewAreaChanged(previewArea);
        }
        if(mGestureView!=null){
            mGestureView.onPreviewAreaChanged(previewArea);
        }
        mCountdownView.onPreviewAreaChanged(previewArea);
    }

    public void onOrientationChanged(int orientation) {
        if (mIntentReviewControls != null) {
            mIntentReviewControls.setLayoutOrientation(orientation);
        }
    }

    @Override
    public void onAllViewRemoved(AppController controller) {
        controller.removeRotatableFromListenerPool(mImageReviewLayout.hashCode());
        controller.removeRotatableFromListenerPool(mIntentReviewLayout.hashCode());
        controller.removeRotatableFromListenerPool(mCountdownView.hashCode());
        controller.removeRotatableFromListenerPool(mBurstCountLayout.hashCode());
        /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
        if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
            controller.removeRotatableFromListenerPool(mFaceBeautyMenu.hashCode());
        } else {
            controller.removeRotatableFromListenerPool(mFaceBeautyMenuFs.hashCode());
            controller.removeRotatableFromListenerPool(mFaceBeautyMenuFw.hashCode());
        }
        /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/
        controller.removeRotatableFromListenerPool(mGesturePalm.hashCode());
    }

    private class DecodeTask extends AsyncTask<Void, Void, Bitmap> {
        private final byte [] mData;
        private final int mOrientation;
        private final boolean mMirror;

        public DecodeTask(byte[] data, int orientation, boolean mirror) {
            mData = data;
            mOrientation = orientation;
            mMirror = mirror;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            // Decode image in background.
            Bitmap bitmap = CameraUtil.downSample(mData, DOWN_SAMPLE_FACTOR);
            if (mOrientation != 0 || mMirror) {
                Matrix m = new Matrix();
                if (mMirror) {
                    // Flip horizontally
                    m.setScale(-1f, 1f);
                }
                m.preRotate(mOrientation);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m,
                        false);
            }
            return bitmap;
        }
    }

    private class DecodeImageForReview extends DecodeTask {
        public DecodeImageForReview(byte[] data, int orientation, boolean mirror) {
            super(data, orientation, mirror);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                return;
            }

            mIntentReviewImageView.setImageBitmap(bitmap);
            showIntentReviewImageView();

            mDecodeTaskForReview = null;
        }
    }

    public PhotoUI(CameraActivity activity, PhotoController controller, View parent) {
        mActivity = activity;
        mController = controller;
        mRootView = parent;

        ModuleLayoutWrapper moduleRoot = (ModuleLayoutWrapper) mRootView.findViewById(R.id.module_layout);
        moduleRoot.setOnAllViewRemovedListener(this, mActivity);
        mActivity.getLayoutInflater().inflate(R.layout.photo_module,
                moduleRoot, true);
        initIndicators();
        mFocusUI = (FocusUI) mRootView.findViewById(R.id.focus_overlay);
        mFocusOverlay = (FocusOverlay) mRootView.findViewById(R.id.focus_overlay); // MODIFIED by peixin, 2016-05-09,BUG-2011866
        ((View)mFocusUI).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mEvoSlider.getVisibility() == View.VISIBLE) {
                    mEvoSlider.dispatchTouchEvent(event);
                }
                return true;
            }
        });
        mEvoSlider=(EvoSlider)mRootView.findViewById(R.id.evo_slider);
        mEvoSlider.setEvoListener(new EvoSlider.EVOChangedListener() {
            @Override
            public void onEVOChanged(int value) {
                mController.onEvoChanged(value);
            }
        });
        mExposureSidebar = (ExposureSidebar) mRootView.findViewById(R.id.exposure_sidebar);
        mExposureSidebar.setExposureSidebarListener(new ExposureSidebar.ExposureSidebarListener() {
            @Override
            public void onExposureCompensationChanged(int value) {
                mActivity.getCameraAppUI().hideZoomBar(); // MODIFIED by jianying.zhang, 2016-10-27,BUG-3212745
                mController.onExposureCompensationChanged(value);
            }
        });
        mBurstCountView=(TextView)mRootView.findViewById(R.id.burst_count_view);
        mBurstCountLayout = (RotateLayout) mRootView.findViewById(R.id.burst_count_layout);
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
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mBurstCountLayout, false));

        mIntentReviewControls = (IntentReviewControls) mRootView.findViewById(R.id.intent_review_controls);

        mCountdownView = (CountDownView) mRootView.findViewById(R.id.count_down_view);
        mModeOptionsTipView = (TextView) mRootView.findViewById(R.id.mode_options_tip);
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mCountdownView, false));
        // Show faces if we are in debug mode.
        if (DebugPropertyHelper.showCaptureDebugUI()||DebugPropertyHelper.needShowFaceView()) {
            mFaceView = (FaceView) mRootView.findViewById(R.id.face_view);
        } else {
            mFaceView = null;
        }

        mGestureView=(GestureView)mRootView.findViewById(R.id.gesture_view);


        if (mController.isImageCaptureIntent()) {
            initIntentReviewImageView();
        }

        mSoundGroup = (SoundGroup) mRootView.findViewById(R.id.sound_group);
        /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
        if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
            mFaceBeautyOption = (FaceBeautyOption) mRootView.findViewById(R.id.face_beauty);
            mFaceBeautyMenu = (RotateImageView) mRootView.findViewById(R.id.face_beauty_menu);
            mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mFaceBeautyMenu, true));
        } else {
            mFaceBeautyOptionFs = (MT_FaceBeautyOptionFS) mRootView.findViewById(R.id.face_beauty_mt_fs);
            mFaceBeautyMenuFs = (RotateImageView) mRootView.findViewById(R.id.face_beauty_menu_mt_fs);
            mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mFaceBeautyMenuFs, true));

            mFaceBeautyOptionFw = (MT_FaceBeautyOptionFW) mRootView.findViewById(R.id.face_beauty_mt_fw);
            mFaceBeautyMenuFw = (RotateImageView) mRootView.findViewById(R.id.face_beauty_menu_mt_fw);
            mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mFaceBeautyMenuFw, true));

            mGestureToastLayout = (LinearLayout) mRootView.findViewById(R.id.toast_layout_root); // MODIFIED by jianying.zhang, 2016-05-30,BUG-2202266
        }
        /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/

        mGesturePalmOption = (GesturePalmOption) mRootView.findViewById(R.id.gesture_palm_option);
        mGesturePalm = (RotateImageView) mRootView.findViewById(R.id.gesture_palm);
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mGesturePalm, true));

        mInfoText = (TextView) mRootView.findViewById(R.id.info_text);

        mAspectRatioButton = (LockRotatableButton)mRootView.findViewById(R.id.aspect_ratio_selecter);
        mActivity.addLockableToListenerPool(mAspectRatioButton);
        mAspectRatioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.onAspectRatioClicked();
            }
        });
        mAspectRatioButton.setOnTouchListener(new RotatableButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mActivity.getLockEventListener().onMenuClicked(mAspectRatioButton.hashCode());
                Log.d(TAG,"OnTouchListener mAspectRatioButton onTouchDown");
            }

            @Override
            public void onTouchUp() {
                mActivity.getLockEventListener().onIdle();
                Log.d(TAG,"OnTouchListener mAspectRatioButton onTouchUp");
            }
        });
        mBottomBarLine = mRootView.findViewById(R.id.bottom_bar_lineview);
        mContactsIntentShutterButton =
                (ShutterButton) mRootView.findViewById(R.id.contacts_intent_shutter_button);
        mContactsIntentShutterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.onShutterButtonClick();
            }
        });
        mPoseUI = getPoseUI();
        mActivity.setControlPoseCallback(this);
    }

    public void showBottomBarLine() {
        if (mBottomBarLine != null) {
            mBottomBarLine.setVisibility(View.VISIBLE);
        }
    }

    public void hideBottomBarLine() {
        if (mBottomBarLine != null) {
            mBottomBarLine.setVisibility(View.GONE);
        }
    }

    public void setupAspectRatioViewer(int viewerType, ButtonGroup.ButtonCallback cb,
                                       List<Size> sizes) {
        FrameLayout setAspectRatioLayout = (FrameLayout) mRootView
                .findViewById(R.id.aspect_ratio_layout);
        if (setAspectRatioLayout != null) {
            // Creates refocus mAspectRatioGroup.
            LayoutInflater inflater = (LayoutInflater) mActivity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mAspectRatioGroup = (ButtonGroup) inflater
                    .inflate(R.layout.button_group, null, false);
            mAspectRatioGroup.setBackgroundColor(mActivity.getResources()
                    .getColor(R.color.preview_layout_color));
            mAspectRatioGroup.load(mActivity);
            mAspectRatioGroup.initCameraPreference(sizes);
            mAspectRatioGroup.initializeLayout(viewerType, cb, null);
            mAspectRatioGroup.startAnimation(ButtonGroup.BOTTOM_TO_TOP);
            setAspectRatioLayout.addView(mAspectRatioGroup, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            setMutexViewVisible(true);
        }
    }
    private void setMutexViewVisible(boolean aspectRatioViewerVisible) {
        if (mController.isFacebeautyEnabled()) {
            if (aspectRatioViewerVisible) {
                hideFacebeauty();
            } else {
                showFacebeauty();
            }
        }

        mActivity.getCameraAppUI().hideZoomBar(); // MODIFIED by jianying.zhang, 2016-10-27,BUG-3212745

        if (mController.isAttentionSeekerShow()) {
            if (aspectRatioViewerVisible) {
                hideSoundGroup();
            } else {
                showSoundGroup();
            }
        }
    }
    public void clearAspectRatioViewer(boolean needAnimation) {
        if (mAspectRatioGroup == null) {
            // No mAspectRatioGroup is created for the specific viewer type.
            return;
        }
        if (needAnimation) {
            mAspectRatioGroup.startAnimation(ButtonGroup.TOP_TO_BOTTOM, new ButtonGroup.AnimationEndCallBack() {
                @Override
                public void onAnimationEnd() {
                    if (mAspectRatioGroup != null) {
                        mAspectRatioGroup.setVisibility(View.GONE);
                        mAspectRatioGroup = null;
                    }
                    setMutexViewVisible(false);
                }
            });
        } else {
            if (mAspectRatioGroup != null) {
                mAspectRatioGroup.setVisibility(View.GONE);
                mAspectRatioGroup = null;
            }
            setMutexViewVisible(false);
        }
    }
    public boolean isAspectRatioBarVisible() {
        if (mAspectRatioGroup != null
                && mAspectRatioGroup.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    public void setAspectRatioVisible (boolean visible) {
        if (mAspectRatioButton == null) {
            return;
        }
        if (visible) {
            mAspectRatioButton.setVisibility(View.VISIBLE);
        } else {
            mAspectRatioButton.setVisibility(View.GONE);
        }
    }

    public void setAspectRatioClickable (boolean enable) {
        if (mAspectRatioButton == null) {
            return;
        }
        mAspectRatioButton.setEnabled(enable);

    }

    public void showModeOptionsTip(String modeOptionsContext){
        if(modeOptionsContext != null && mModeOptionsTipView != null){
            mModeOptionsTipView.setText(modeOptionsContext);
            mModeOptionsTipView.setVisibility(View.VISIBLE);
        }
    }

    public void hideModeOptionsTip(){
        if(mModeOptionsTipView != null){
            mModeOptionsTipView.setVisibility(View.GONE);
        }
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-05-25,BUG-2202266*/
    protected void setGestureToastLayoutVisibility(boolean visible){
        if (mGestureToastLayout == null) {
            return;
        }
        if (visible) {
            TextView text_cn;
            text_cn = (TextView)mGestureToastLayout.findViewById(R.id.text_cn);
            text_cn.setText(Html.fromHtml(descString(), getImageGetterInstance(), null));
            text_cn.setVisibility(View.VISIBLE);
            mGestureToastLayout.setVisibility(View.VISIBLE);
        }else {
            mGestureToastLayout.setVisibility(View.GONE);
        }
    }
    private String descString() {
        String gesture_toast = mActivity.getResources().getString(R.string.gestureshot_guide_capture
        );
        return gesture_toast + "<img src='" + R.drawable.ic_gesture_on
                + "'/>";
    }

    private Html.ImageGetter getImageGetterInstance() {
        Html.ImageGetter imgGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                int fontH = (int) (mActivity.getResources().getDimension(
                        R.dimen.textSizeMedium) * 1.5);
                int id = Integer.parseInt(source);
                Drawable d = mActivity.getResources().getDrawable(id);
                int height = fontH;
                int width = (int) ((float) d.getIntrinsicWidth() / (float) d
                        .getIntrinsicHeight()) * fontH;
                if (width == 0) {
                    width = d.getIntrinsicWidth();
                }
                d.setBounds(0, 0, width, height);
                return d;
            }
        };
        return imgGetter;
    }
    /* MODIFIED-END by jianying.zhang,BUG-2202266*/
    private void initIntentReviewImageView() {
        mIntentReviewImageView = (ImageView) mRootView.findViewById(R.id.intent_review_imageview);
        // Cancel and done button no longer show in bottombar, no need to set margins.
        /*
        mActivity.getCameraAppUI().addPreviewAreaChangedListener(
                new PreviewStatusListener.PreviewAreaChangedListener() {
                    @Override
                    public void onPreviewAreaChanged(RectF previewArea) {
                        FrameLayout.LayoutParams params =
                            (FrameLayout.LayoutParams) mIntentReviewImageView.getLayoutParams();
                        params.width = (int) previewArea.width();
                        params.height = (int) previewArea.height();
                        params.setMargins((int) previewArea.left, (int) previewArea.top, 0, 0);
                        mIntentReviewImageView.setLayoutParams(params);
                    }
                });
                */
    }

    public void updateBurstCount(int count, int maxCount) {
        if (count==0) {
//            mBurstCountView.setVisibility(View.GONE);
            mInfoText.setVisibility(View.GONE);
        } else {
            if (mInfoText.getVisibility()!=View.VISIBLE) {
//                mBurstCountView.setVisibility(View.VISIBLE);
                mInfoText.setVisibility(View.VISIBLE);
                if (mSoundGroup != null && mSoundGroup.getVisibility() == View.VISIBLE) {
                    mSoundGroup.hideKidSound();
                }
                /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
                if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
                    if (mFaceBeautyOption != null && mFaceBeautyOption.getVisibility() == View.VISIBLE) {
                        mFaceBeautyOption.hideSeekBar();
                    }
                } else {
                    if (mFaceBeautyOptionFs != null && mFaceBeautyOptionFs.getVisibility() == View.VISIBLE) {
                        mFaceBeautyOptionFs.hideSeekBar();
                    }
                    if (mFaceBeautyOptionFw != null && mFaceBeautyOptionFw.getVisibility() == View.VISIBLE) {
                        mFaceBeautyOptionFw.hideSeekBar();
                    }
                    /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/
                }
            }
//            mBurstCountView.setText("" + count);
        }
        if (maxCount > 0) {
            mInfoText.setText("" + count + "/" + maxCount);
        } else {
            mInfoText.setText("" + count);
        }
    }

    public void updateBurstCount(int count) {
        updateBurstCount(count, 0);
    }

    /**
     * Show the image review over the live preview for intent captures.
     */
    public void showIntentReviewImageView() {
        if (mIntentReviewImageView != null) {
            mIntentReviewImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide the image review over the live preview for intent captures.
     */
    public void hideIntentReviewImageView() {
        if (mIntentReviewImageView != null) {
            mIntentReviewImageView.setVisibility(View.INVISIBLE);
        }
    }


    public FocusUI getFocusUI() {
        return mFocusUI;
    }

    public void updatePreviewAspectRatio(float aspectRatio) {
        if (aspectRatio <= 0) {
            Log.e(TAG, "Invalid aspect ratio: " + aspectRatio);
            return;
        }
        if (aspectRatio < 1f) {
            aspectRatio = 1f / aspectRatio;
        }

        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            // Update transform matrix with the new aspect ratio.
            mController.updatePreviewAspectRatio(mAspectRatio);
        }
    }

    public void initEvoSlider(float x,float y){
        mEvoSlider.setCoord(x, y);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mController.onPreviewUIReady();
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
        mController.onSurfaceAvailable(surface, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mController.onSurfaceTextureChanged(surface,width,height);
        /* MODIFIED-END by sichao.hu,BUG-2743263*/
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mController.onPreviewUIDestroyed();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (mRunnableForNextFrame != null) {
            mRootView.post(mRunnableForNextFrame);
            mRunnableForNextFrame = null;
        }
    }

    public View getRootView() {
        return mRootView;
    }

    private void initIndicators() {
        // TODO init toggle buttons on bottom bar here
    }

    public void onCameraOpened(CameraCapabilities capabilities, CameraSettings settings) {
        initializeZoom(capabilities, settings);
        initializeFacebeauty();
        initializeAttentionseeker();
        initializeGesturePalm();
        initializePrettyPose();
        initializePrettyCompose();
        mPoseUI.onCameraOpened();
    }

    public void animateCapture(final byte[] jpegData, int orientation, boolean mirror) {
        // Decode jpeg byte array and then animate the jpeg
        DecodeTask task = new DecodeTask(jpegData, orientation, mirror);
        task.execute();
    }

    // called from onResume but only the first time
    public void initializeFirstTime() {

    }

    // called from onResume every other time
    public void initializeSecondTime(CameraCapabilities capabilities, CameraSettings settings) {
        initializeZoom(capabilities, settings);
        if (mController.isImageCaptureIntent()) {
            hidePostCaptureAlert();
        }
    }

    public void showLocationAndAspectRatioDialog(
            final PhotoModule.LocationDialogCallback locationCallback,
            final PhotoModule.AspectRatioDialogCallback aspectRatioDialogCallback) {
        setDialog(new Dialog(mActivity,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen));
        final LocationDialogLayout locationDialogLayout = (LocationDialogLayout) mActivity
                .getLayoutInflater().inflate(R.layout.location_dialog_layout, null);
        locationDialogLayout.setLocationTaggingSelectionListener(
                new LocationDialogLayout.LocationTaggingSelectionListener() {
                    @Override
                    public void onLocationTaggingSelected(boolean selected) {
                        // Update setting.
                        locationCallback.onLocationTaggingSelected(selected);

                        if (showAspectRatioDialogOnThisDevice()) {
                            // Go to next page.
                            showAspectRatioDialog(aspectRatioDialogCallback, mDialog);
                        } else {
                            // If we don't want to show the aspect ratio dialog,
                            // dismiss the dialog right after the user chose the
                            // location setting.
                            if (mDialog != null) {
                                mDialog.dismiss();
                            }
                        }
                    }
                });
        mDialog.setContentView(locationDialogLayout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDialog.show();
    }

    /**
     * Dismisses previous dialog if any, sets current dialog to the given dialog,
     * and set the on dismiss listener for the given dialog.
     * @param dialog dialog to show
     */
    private void setDialog(Dialog dialog) {
        if (mDialog != null) {
            mDialog.setOnDismissListener(null);
            mDialog.dismiss();
        }
        mDialog = dialog;
        if (mDialog != null) {
            mDialog.setOnDismissListener(mOnDismissListener);
        }
    }

    /**
     * @return Whether the dialog was shown.
     */
    public boolean showAspectRatioDialog(final PhotoModule.AspectRatioDialogCallback callback) {
        if (showAspectRatioDialogOnThisDevice()) {
            setDialog(new Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen));
            showAspectRatioDialog(callback, mDialog);
            return true;
        } else {
            return false;
        }
    }

    private boolean showAspectRatioDialog(final PhotoModule.AspectRatioDialogCallback callback,
            final Dialog aspectRatioDialog) {
        if (aspectRatioDialog == null) {
            Log.e(TAG, "Dialog for aspect ratio is null.");
            return false;
        }
        final AspectRatioDialogLayout aspectRatioDialogLayout =
                (AspectRatioDialogLayout) mActivity
                .getLayoutInflater().inflate(R.layout.aspect_ratio_dialog_layout, null);
        aspectRatioDialogLayout.initialize(
                new AspectRatioDialogLayout.AspectRatioChangedListener() {
                    @Override
                    public void onAspectRatioChanged(AspectRatioSelector.AspectRatio aspectRatio) {
                        // callback to set picture size.
                        callback.onAspectRatioSelected(aspectRatio, new Runnable() {
                            @Override
                            public void run() {
                                if (mDialog != null) {
                                    mDialog.dismiss();
                                }
                            }
                        });
                    }
                }, callback.getCurrentAspectRatio());
        aspectRatioDialog.setContentView(aspectRatioDialogLayout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        aspectRatioDialog.show();
        return true;
    }

    /**
     * @return Whether this is a device that we should show the aspect ratio
     *         intro dialog on.
     */
    private boolean showAspectRatioDialogOnThisDevice() {
        // We only want to show that dialog on N4/N5/N6
        // Don't show if using API2 portability, b/17462976
        return !GservicesHelper.useCamera2ApiThroughPortabilityLayer(mActivity) &&
                (ApiHelper.IS_NEXUS_4 || ApiHelper.IS_NEXUS_5 || ApiHelper.IS_NEXUS_6);
    }

    public void initializeZoom(CameraCapabilities capabilities, CameraSettings settings) {

        if ((capabilities == null) || settings == null ||
                !capabilities.supports(CameraCapabilities.Feature.ZOOM)) {
            return;
        }
        mZoomMax = capabilities.getMaxZoomRatio();
        // Currently we use immediate zoom for fast zooming to get better UX and
        // there is no plan to take advantage of the smooth zoom.
        // TODO: Need to setup a path to AppUI to do this
        mActivity.getCameraAppUI().resetZoomBar(); // MODIFIED by jianying.zhang, 2016-11-01,BUG-3271894
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

    /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
    private static final String MT_ACTION_FACEBEAUTY_SMOOTHING = "android.intent.action.facebeauty";
    private static final String MT_ACTION_FACEBEAUTY_WHITENING = "android.intent.action.facebeauty";
    private static final String MT_ACTION_FACEBEAUTY_SMOOTHING_DATA = "skin_smoothing";
    private static final String MT_ACTION_FACEBEAUTY_WHITENING_DATA = "skin_whitening";
    private static final String MT_PERSIST_SYS_FACEBEAUTY_SMOOTHING = "persist.sys.skin_smoothing";
    private static final String MT_PERSIST_SYS_FACEBEAUTY_WHITENING = "persist.sys.tone_adjustment";


    private boolean mFacebeautyInitialized = false;
    private void initializeFacebeauty() {
        if (!mController.isFacebeautyEnabled() || mActivity.getCameraAppUI().isInIntentReview()) {
            if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
                mFaceBeautyOption.setVisibility(View.GONE);
                mFaceBeautyOption.hideSeekBar();
            } else {
                mFaceBeautyOptionFs.setVisibility(View.GONE);
                mFaceBeautyOptionFs.hideSeekBar();
                mFaceBeautyOptionFw.setVisibility(View.GONE);
                mFaceBeautyOptionFw.hideSeekBar();
            }
        } else {
            mFacebeautyInitialized = true;
            if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
                mFaceBeautyOption.hideSeekBar();
                mFaceBeautyOption.setVisibility(View.VISIBLE);
            } else {
                mFaceBeautyOptionFs.hideSeekBar();
                mFaceBeautyOptionFs.setVisibility(View.VISIBLE);
                mFaceBeautyOptionFw.hideSeekBar();
                mFaceBeautyOptionFw.setVisibility(View.VISIBLE);
            }

            if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
                String key = Keys.KEY_FACEBEAUTY_SKIN_SMOOTHING;
                int defSkinSmoothing = CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_SKIN_SMOOTHING, PhotoModule.SKIN_SMOOTHING_DEFAULT);
                int initialValue = mActivity.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL, key, defSkinSmoothing);
                mFaceBeautyOption.initData(key, initialValue, 100);
                mFaceBeautyOption.setFaceBeautySettingCallBack(new FaceBeautyOption.FaceBeautySettingCallBack() {

                    @Override
                    public void updateFaceBeautySetting(String key, int value) {
                        mController.updateFaceBeautySetting(key, value);
                    }
                });
            } else {
                String keyFs = Keys.KEY_FACEBEAUTY_SKIN_SMOOTHING;
                int defSkinSmoothing = CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_SKIN_SMOOTHING, PhotoModule.SKIN_SMOOTHING_DEFAULT);
//                int initialValueFs = mActivity.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL, keyFs, defSkinSmoothing);
                int initialValueFs = ExtSystemProperties.getInt(MT_PERSIST_SYS_FACEBEAUTY_SMOOTHING, defSkinSmoothing);
                Log.d(TAG, "[initializeFacebeauty][visidon] initialValueFs = " + initialValueFs);
                mFaceBeautyOptionFs.initData(keyFs, initialValueFs, 100);
                mFaceBeautyOptionFs.setFaceBeautySettingCallBack(new MT_FaceBeautyOptionFS.FaceBeautySettingCallBack() {

                    @Override
                    public void updateFaceBeautySetting(String key, int value) {
                        Log.w(TAG, "[updateFaceBeautySetting][visidon] key = " + key + ", value=" + value);
                        mController.updateFaceBeautySetting(key, value);
                        Intent intent = new Intent(MT_ACTION_FACEBEAUTY_SMOOTHING);
                        intent.putExtra(MT_ACTION_FACEBEAUTY_SMOOTHING_DATA, value);
                        mActivity.sendBroadcast(intent);
                    }
                });

                String keyFw = Keys.KEY_FACEBEAUTY_SKIN_WHITE;
                int defSkinWhite = CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_SKIN_WHITE, PhotoModule.SKIN_WHITE_DEFAULT);
//                int initialValueFw = mActivity.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL, keyFw, defSkinWhite);
                int initialValueFw = ExtSystemProperties.getInt(MT_PERSIST_SYS_FACEBEAUTY_WHITENING, defSkinWhite);
                Log.d(TAG, "[initializeFacebeauty][visidon] initialValueFw = " + initialValueFw);
                mFaceBeautyOptionFw.initData(keyFw, initialValueFw, 100);
                mFaceBeautyOptionFw.setFaceBeautySettingCallBack(new MT_FaceBeautyOptionFW.FaceBeautySettingCallBack() {

                    @Override
                    public void updateFaceBeautySetting(String key, int value) {
                        Log.w(TAG, "[updateFaceBeautySetting][visidon] key = " + key + ", value=" + value);
                        mController.updateFaceBeautySetting(key, value);
                        Intent intent = new Intent(MT_ACTION_FACEBEAUTY_WHITENING);
                        intent.putExtra(MT_ACTION_FACEBEAUTY_WHITENING_DATA, value);
                        mActivity.sendBroadcast(intent);
                    }
                });
            }
            /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/
        }

    }
    public void showFacebeauty() {
        if (!mController.isFacebeautyEnabled()|| mActivity.getCameraAppUI().isInIntentReview()) {
            return;
        }
        if (mFacebeautyInitialized ) {
            /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
            if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
                mFaceBeautyOption.setVisibility(View.VISIBLE);
            } else {
                mFaceBeautyOptionFs.setVisibility(View.VISIBLE);
                mFaceBeautyOptionFw.setVisibility(View.VISIBLE);
            }
        } else {
            initializeFacebeauty();
        }
    }

    public void hideFacebeauty() {
        if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
            mFaceBeautyOption.setVisibility(View.GONE);
            mFaceBeautyOption.hideSeekBar();
        } else {
            mFaceBeautyOptionFs.setVisibility(View.GONE);
            mFaceBeautyOptionFs.hideSeekBar();
            mFaceBeautyOptionFw.setVisibility(View.GONE);
            mFaceBeautyOptionFw.hideSeekBar();
        }
        /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/
    }
    private void initializeAttentionseeker() {
        if (mController.isAttentionSeekerShow() && !mController.isImageCaptureIntent()) {
            mSoundGroup.hideKidSound();
            mSoundGroup.setVisibility(View.VISIBLE);
            mSoundGroup.addRotatableToListenerPool();
        } else {
            mSoundGroup.finishKidSound();
            mSoundGroup.setVisibility(View.GONE);
            mSoundGroup.removeRotatableToListenerPool();
        }
    }

    public void animateFlash() {
        mController.startPreCaptureAnimation();
    }

    public boolean onBackPressed() {
        // In image capture mode, back button should:
        // 1) if there is any popup, dismiss them, 2) otherwise, get out of
        // image capture
        if (mController.isImageCaptureIntent()) {
            mController.onCaptureCancelled();
            return true;
        } else if (!mController.canCloseCamera()) {
            // ignore backs while we're taking a picture
            return true;
        } else {
            return false;
        }
    }

    protected void showCapturedImageForReview(byte[] jpegData, int orientation, boolean mirror) {
        mDecodeTaskForReview = new DecodeImageForReview(jpegData, orientation, mirror);
        mDecodeTaskForReview.execute();
        mActivity.getCameraAppUI().transitionToIntentReviewLayout();
        pauseFaceDetection();
        hideFacebeauty();
        hideGesturePalm();
    }

    protected void hidePostCaptureAlert() {
        if (mDecodeTaskForReview != null) {
            mDecodeTaskForReview.cancel(true);
        }
        resumeFaceDetection();
        showFacebeauty();
        showGesturePalm();
    }

    protected void clearReviewImage() {
        if (mIntentReviewImageView != null &&
                mIntentReviewImageView.getDrawable() != null) {
            mIntentReviewImageView.setImageBitmap(null);
        }
    }

    public void setDisplayOrientation(int orientation) {
        if (mFaceView != null) {
            mFaceView.setDisplayOrientation(orientation);
        }
    }

    public void setPostGestureOrientation(int orientation){
        if(mGestureView!=null){
            mGestureView.setPostGestureRotation(orientation);
        }
    }

    public void setGestureDisplayOrientation(int orientation){
        if(mGestureView!=null){
            mGestureView.setDisplayOrientation(orientation);
        }
    }

    public void setSensorOrientation(int orientation){
        if(mGestureView!=null){
            mGestureView.setSensorOrientation(orientation);
        }
    }

    public void setGestureMirrored(boolean isMirrored){
        if(mGestureView!=null){
            mGestureView.setPreviewMirrored(isMirrored);
        }
    }

    public void disableZoom(){
        if(mPreviewOverlay!=null){
            mPreviewOverlay.setTouchEnabled(false);
        }
    }

    public void enableZoom(){
        if(mPreviewOverlay!=null){
            mPreviewOverlay.setTouchEnabled(true);
        }
    }

    private class ZoomChangeListener implements PreviewOverlay.OnZoomChangedListener {
        @Override
        public void onZoomValueChanged(float ratio) {
            if (mZoomBar.isLocked()) {
                Log.d(TAG,"zoom is locked");
                return;
            }
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

    public void setSwipingEnabled(boolean enable) {
        mActivity.setSwipingEnabled(enable);
    }

    public void onPause() {
        if (mFaceView != null) {
            mFaceView.clear();
            mFaceView.setVisibility(View.GONE);
        }
        /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
        if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
            if (!mActivity.getCameraAppUI().isInIntentReview() &&
                    mController.isFacebeautyEnabled() && mFaceBeautyOption != null) {
                mFaceBeautyOption.reset();
                mFaceBeautyOption.hideSeekBar();
            }
        } else {
            if (!mActivity.getCameraAppUI().isInIntentReview() &&
                    mController.isFacebeautyEnabled() && mFaceBeautyOptionFs != null) {
                mFaceBeautyOptionFs.reset();
                mFaceBeautyOptionFs.hideSeekBar();
            }
            if (!mActivity.getCameraAppUI().isInIntentReview() &&
                    mController.isFacebeautyEnabled() && mFaceBeautyOptionFw != null) {
                mFaceBeautyOptionFw.reset();
                mFaceBeautyOptionFw.hideSeekBar();
            }
            /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/
        }

        if (mDialog != null) {
            mDialog.dismiss();
        }
        // recalculate aspect ratio when restarting.
        mAspectRatio = 0.0f;

        if (mSoundGroup != null) {
            mSoundGroup.finishKidSound();
        }

        if (mGesturePalmOption != null) {
            mGesturePalmOption.hideGestureHelpTip();
        }
        setAspectRatioVisible(false);
        mPoseUI.onCameraDestroy();
    }

    public void clearFaces() {
        if (mFaceView != null) {
            mFaceView.clear();
            mFaceView.setVisibility(View.GONE);
        }
    }

    public void clearFocus(){
        if(mFocusUI!=null){
            mFocusUI.clearFocus();
        }
    }

    public void pauseFaceDetection() {
        if (mFaceView != null) {
            mFaceView.pause();
            mFaceView.setVisibility(View.GONE);
        }
    }

    public void resumeFaceDetection() {
        if (mFaceView != null) {
            mFaceView.resume();
            mFaceView.setVisibility(View.VISIBLE);
        }
    }

    public void onStartFaceDetection(int orientation, boolean mirror) {
        if (mFaceView != null) {
            mFaceView.clear();
            mFaceView.setVisibility(View.VISIBLE);
            mFaceView.setDisplayOrientation(orientation);
            mFaceView.setMirror(mirror);
            mFaceView.resume();
        }
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-17,BUG-3501961*/
    public void setFaceDetectionStarted(boolean faceDetectionStarted) {
        mFaceDetectionStarted = faceDetectionStarted;
    }

    @Override
    public void onFaceDetection(Face[] faces, CameraAgent.CameraProxy camera) {
        /**
         * start face detection to prefrom this method;
         * when stop face detection prefrom this method ,lead to not show focuse box at videomodule;
         */
        if (!mFaceDetectionStarted) {
            return;
        }
        /* MODIFIED-END by jianying.zhang,BUG-3501961*/
        mLatestFaces=faces;
        if (mFaceView != null) {
            mFaceView.setFaces(faces);
            /*MODIFIED-BEGIN by sichao.hu, 2016-04-15,BUG-1951866*/
            for(Face face:faces){
                if(face.score>=50){
                    mController.onFaceDetected(true);
                    return;
                }
            }
        }
        mController.onFaceDetected(false);
    }

    public boolean hasFaces(){
        if(mLatestFaces==null){
            return false;
        }
        for(Camera.Face face:mLatestFaces){
            if(face.score>=50){
                return true;
            }
        }
        return false;
    }

    /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
    private SparseArray<Object> mPauseKeys=new SparseArray<>();
    public void pauseFocusFrame(int key){
        if(mPauseKeys.get(key,null)==null){
            mPauseKeys.put(key,new Object());
        }
        mFocusUI.pauseFocusFrame();
    }

    public void resumeFocusFrame(int key) {
        if (mPauseKeys.get(key, null)!=null) {
            mPauseKeys.remove(key);
        }
        if(mPauseKeys.size()==0) {
            mFocusUI.resumeFocusFrame();
        }
        /* MODIFIED-END by sichao.hu,BUG-2743263*/
    }
    /*MODIFIED-END by sichao.hu,BUG-1951866*/


    public void showGesture(Rect gestureBound,Size previewSize){
        if(mGestureView!=null){
            mGestureView.showGesture(gestureBound, previewSize);
        }
    }


    public void hideGesture(){
        if(mGestureView!=null) {
            mGestureView.hideGesture();
        }
    }

    public boolean isGestureViewShow() {
        return mGestureView != null && mGestureView.getVisibility() == View.VISIBLE;
    }

    private Camera.Face[] mLatestFaces;
    private boolean mFaceDetectionStarted; // MODIFIED by jianying.zhang, 2016-11-17,BUG-3501961

    public Face[] getLatestFaces(){
        return mLatestFaces;
    }

    public ArrayList<RectF> filterAndAdjustFaces(boolean mirror, int jpgRotation) {
        ArrayList<RectF> rectFs = new ArrayList<>();
        if (mLatestFaces == null || mLatestFaces.length < 1) {
            return rectFs;
        }
        Matrix matrix = new Matrix();
        Log.d(TAG, "mirror:" + mirror + ", " + jpgRotation);
        matrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(jpgRotation);
        for (int i = 0; i < mLatestFaces.length; i++) {
            // Filter out false positives.
            if (mLatestFaces[i].score < 50) continue;
            RectF rect = new RectF();
            // Transform the coordinates.
            rect.set(mLatestFaces[i].rect);
            matrix.mapRect(rect);
            rectFs.add(rect);
        }

        return rectFs;

    }

    public void showSoundGroup() {
        if (mController.isAttentionSeekerShow() && !mController.isImageCaptureIntent()) {
            if(mSoundGroup != null) {
                mSoundGroup.setVisibility(View.VISIBLE);
            }
        }
    }

    public void hideSoundGroup() {
        if(mSoundGroup != null && mSoundGroup.getVisibility() == View.VISIBLE) {
            mSoundGroup.setVisibility(View.GONE);
            mSoundGroup.finishKidSound();
        }
    }

    public boolean isSoundGroupPlaying() {
        if (mSoundGroup != null && mSoundGroup.getVisibility() == View.VISIBLE && mSoundGroup.isSoundPlaying()) {
            return true;
        }
        return false;
    }

    private void initializeGesturePalm() {
        if (!mController.isGesturePalmShow() || mActivity.getCameraAppUI().isInIntentReview()) {
            mGesturePalmOption.setVisibility(View.GONE);
            mGesturePalmOption.hideGestureHelpTip();
        } else {
            mGesturePalmOption.setVisibility(View.VISIBLE);
        }
    }

    public void startSaveProgress(){
        mActivity.getCameraAppUI().mapShutterProgress(100, 10);
        mActivity.getCameraAppUI().startProgressAnimation();

    }

    public void stopSaveProgress(){
        mActivity.getCameraAppUI().stopProgressAnimation();
    }

    public void setSaveProgress(int progress){
        mActivity.getCameraAppUI().setProgressOfShutterProgress(progress);
    }

    public void showGesturePalm() {
        if (!mController.isGesturePalmShow()|| mActivity.getCameraAppUI().isInIntentReview()) {
            return;
        }
        mGesturePalmOption.setVisibility(View.VISIBLE);
    }

    public void hideGesturePalm() {
        mGesturePalmOption.setVisibility(View.GONE);
        if (mGesturePalmOption != null) {
            mGesturePalmOption.hideGestureHelpTip();
        }
    }

    /* MODIFIED-BEGIN by peixin, 2016-05-09,BUG-2011866*/
    public boolean getFocusUIVisibility() {
        if (mFocusOverlay != null) {
            return mFocusOverlay.getVisibility() == View.VISIBLE;
        }
        return false;
    }
    /* MODIFIED-END by peixin,BUG-2011866*/


    // Exposure sidebar.
    public boolean isExposureSidebarVisible() {
        return (mExposureSidebar == null) ? false :
                (mExposureSidebar.getVisibility() == View.VISIBLE);
    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-22,BUG-3178291*/
    public void fadeInExposureSidebar(int x, int y) {
        if (mExposureSidebar == null) {
            return;
        }

        mExposureSidebar.fadeIn(x, y);
        /* MODIFIED-END by xuan.zhou,BUG-3178291*/
    }

    public void fadeOutExposureSidebar() {
        if (mExposureSidebar == null) {
            return;
        }
        mExposureSidebar.fadeOut();
    }

    public void hideExposureSidebar() {
        if (mExposureSidebar == null) {
            return;
        }
        mExposureSidebar.hide();
    }

    public void setExposureSidebarPrepared(boolean ready) {
        if (mExposureSidebar == null) {
            return;
        }
        mExposureSidebar.prepared(ready);
    }

    public void loadExposureCompensation(final int MIN, final int MAX) {
        if (mExposureSidebar == null) {
            return;
        }
        mExposureSidebar.loadExposureCompensation(MIN, MAX);
    }

    public void resetExposureSidebar() {
        if (mExposureSidebar == null) {
            return;
        }
        mExposureSidebar.reset();
    }


    // Metering.
    // I wanna handle the metering touch event here but not in FocusManager or FocusOverlay because
    // now metering is only requested in photo module.
    private boolean trackMeteringTouchEvent(MotionEvent event) {
        if (event.getPointerCount() != 1) {
            return false;
        }
        if (!isMeteringEnabled()) {
            return false;
        }
        if (mFocusUI == null) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isTouchInMeteringBound(event)) {
                    onMeteringStart();
                } else {
                    onMeteringStop();
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isMeteringDragging() && !isDraggingCrossBorder(event)) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    mController.onMeteringAreaChanged(x, y);
                    mFocusUI.setMeteringPosition(x, y);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                onMeteringStop();
                return true;

            case MotionEvent.ACTION_CANCEL:
                onMeteringStop();
                return true;
        }

        return false;
    }

    private RectF buildMeteringRectF(float x, float y, float radius) {
        return new RectF(x - radius, y - radius, x + radius, y + radius);
    }

    private boolean isTouchInMeteringBound(MotionEvent event) {
        if (mFocusUI == null || mActivity == null) {
            return false;
        }
        final float x = event.getX();
        final float y = event.getY();
        Point meteringCenter = mFocusUI.getMeteringPosition();
        float radius = mActivity.getResources().getDimension(R.dimen.metering_circle_radius);
        radius *= 2; // Double the touch bound.
        return buildMeteringRectF(meteringCenter.x, meteringCenter.y, radius).contains(x, y);
    }

    private boolean isDraggingCrossBorder(MotionEvent event) {
        if (mFocusUI == null || mActivity == null) {
            return true;
        }
        final float x = event.getX();
        final float y = event.getY();
        float topBarHeight = mActivity.getResources().getDimension(R.dimen.mode_options_height);
        float radius = mActivity.getResources().getDimension(R.dimen.metering_circle_radius);

        if (mActivity.getCameraAppUI().isTopMenusVisible() && y < topBarHeight) { // MODIFIED by xuan.zhou, 2016-11-03,BUG-3311864
            return true;
        }

        RectF newMeteringBound = buildMeteringRectF(x, y, radius);

        RectF previewRectF = mActivity.getCaptureLayoutHelper().getPreviewRect();
        if (previewRectF == null || !previewRectF.contains(newMeteringBound)) {
            return true;
        }

        RectF bottomBarRectF = mActivity.getCaptureLayoutHelper().getBottomBarRect();
        if (bottomBarRectF == null || bottomBarRectF.intersect(newMeteringBound)) {
            return true;
        }

        if (isExposureSidebarVisible()) {
            RectF sidebarRectF = mExposureSidebar.getVisualSidebarRectF();
            if (sidebarRectF == null || sidebarRectF.intersect(newMeteringBound)) {
                return true;
            }
        }

        return false;
    }

    public void showMeteringUI() {
        if (mFocusUI == null) {
            return;
        }
        mFocusUI.showMetering();
    }

    public void hideMeteringUI() {
        if (mFocusUI == null) {
            return;
        }
        mFocusUI.hideMetering();
    }

    // Check the state in focus ui directly.
    public boolean isMeteringShowing() {
        return mFocusUI == null ? false :mFocusUI.isMeteringShowing();
    }

    // If it's not enabled, metering would be no response to the touch event.
    private boolean mMeteringEnabled;
    // The state for whether metering circle is dragging.
    private boolean mMeteringDragging;

    public void enableMetering() {
        mMeteringEnabled = true;
    }

    public void disableMetering() {
        mMeteringEnabled = false;
    }

    public boolean isMeteringEnabled() {
        return mMeteringEnabled;
    }

    public boolean isMeteringDragging() {
        return mMeteringDragging;
    }

    public void onMeteringStart() {
        mActivity.getCameraAppUI().hideZoomBar(); // MODIFIED by jianying.zhang, 2016-10-27,BUG-3212745
        mMeteringDragging = true;
        mController.onMeteringStart();
    }

    public void onMeteringStop() {
        mMeteringDragging = false;
        mController.onMeteringStop();
    }

    // The info text.
    public void showInfoText(int stringId) {
        showInfoText(stringId, 0, 0);
    }

    public void showInfoText(int stringId, int drawableStart, int drawableEnd) {
        if (mInfoText == null) {
            return;
        }
        mInfoText.setText(stringId);
        if (drawableStart > 0 || drawableEnd > 0) {
            mInfoText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    drawableStart, 0, drawableEnd, 0);
        } else {
            // If no drawableStart/End set, reset to clear the drawable.
            mInfoText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0, 0, 0, 0);
        }
        mInfoText.setVisibility(View.VISIBLE);
    }

    public void hideInfoText() {
        if (mInfoText == null) {
            return;
        }
        mInfoText.setVisibility(View.GONE);
    }

    public void showContactsGridLines() {
        mActivity.getCameraAppUI().hideGridLines();
        mActivity.getCameraAppUI().showContactsGridLines();
    }

    public void hideContactsGridLines() {
        mActivity.getCameraAppUI().hideGridLines();
        mActivity.getCameraAppUI().hideContactsGridLines();
    }


    public boolean isContainsSingleTapUpPoint(int x, int y) {
        RectF rectF = mActivity.getCameraAppUI().getPreviewArea();
        if (rectF != null && rectF.contains(x, y)) {
            return true;
        }
        return false;
    }

    protected PoseAndCompositionUI getPoseUI() {
        if (mPoseUI == null) {
            mPoseUI = new PoseAndCompositionUI(mActivity, mController, mActivity.getModuleLayoutRoot());
        }
        return mPoseUI;
    }

    @Override
    public void onVisibilityChange(boolean isModeSelecting, boolean isAutomode) {
        if (isModeSelecting) {
            hidePoseLayout();
        } else {
            setPoseTipsVisibility(isAutomode);
        }
    }

    @Override
    public void onResetVisibility(boolean isResume) {
        if (isResume) {
            setPoseTipsVisibility(!getPoseVisibility());
        }
    }

    private void hidePoseLayout() {
        if (mPoseUI.getPoseVisibility()) {
            mPoseUI.hidePoseLayout();
        }
        if (mPoseUI.getComposeVisibility()) {
            mPoseUI.hideComposeLayout();
        }
    }

    private void setPoseTipsVisibility(boolean isVisible) {
        mPoseUI.setPoseTipsVisibility(isVisible);
    }

    private boolean getPoseVisibility() {
        return mPoseUI.getPoseVisibility() || mPoseUI.getComposeVisibility();
    }

    private void initializePrettyPose() {
        if (mController.isShowPose()) {
            mActivity.getCameraAppUI().showPoseButton();
        } else {
            mActivity.getCameraAppUI().hidePoseButton();
            if (getPoseUI().getPoseVisibility()) {
                mPoseUI.hidePoseLayout();
            }
        }
    }

    private void initializePrettyCompose() {
        setPoseTipsVisibility(true);
        if (mController.isShowCompose()) {
            mActivity.getCameraAppUI().showCompose();
        } else {
            mActivity.getCameraAppUI().hideCompose();
            if (getPoseUI().getComposeVisibility()) {
                mPoseUI.hideComposeLayout();
            }
        }
    }

    public void showPose() {
        initializePrettyPose();
    }

    public void onFilterClicked() {
        //when click filter button Immediately hide Facebeauty
        hideFacebeauty();
    }
}
