/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.camera.app;

import android.app.Activity;
import android.app.FragmentManager; // MODIFIED by fei.hui, 2016-09-29,BUG-2994050
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;// MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Looper;
import android.util.CameraPerformanceTracker;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.camera.AnimationManager;
import com.android.camera.ButtonManager;
import com.android.camera.CaptureLayoutHelper;
import com.android.camera.HelpTipsManager;
import com.android.camera.ManualModule;
import com.android.camera.NormalPhotoModule;
import com.android.camera.NormalVideoModule;
import com.android.camera.PhotoModule;
import com.android.camera.PoseFragment; // MODIFIED by fei.hui, 2016-09-29,BUG-2994050
import com.android.camera.ShutterButton;
import com.android.camera.TextureViewHelper;
import com.android.camera.Thumbnail;
import com.android.camera.debug.Log;
import com.android.camera.filmstrip.FilmstripContentPanel;
import com.android.camera.hardware.HardwareSpec;
import com.android.camera.module.ModuleController;
import com.android.camera.settings.CameraSettingsFragment;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.AbstractTutorialOverlay;
import com.android.camera.ui.BottomBar;
import com.android.camera.ui.BottomBarModeOptionsWrapper;
import com.android.camera.ui.CaptureAnimationOverlay;
import com.android.camera.ui.CenterGridLines;
import com.android.camera.ui.GridLines;
import com.android.camera.ui.IntentReviewControls;
import com.android.camera.ui.LockRotatableButton;
import com.android.camera.ui.Lockable;
import com.android.camera.ui.MainActivityLayout;
import com.android.camera.ui.ManualGroup;
import com.android.camera.ui.ModeListView;
import com.android.camera.ui.ModeStrip;
import com.android.camera.ui.ModeStripView;
import com.android.camera.ui.ModeTransitionView;
import com.android.camera.ui.PeekImageView;
import com.android.camera.ui.PreviewOverlay;
import com.android.camera.ui.PreviewStatusListener;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotatableButton;
import com.android.camera.ui.ScrollIndicator;
import com.android.camera.ui.ShutterSaveProgressbar;
import com.android.camera.ui.StereoModeStripView;
import com.android.camera.ui.StereoScrollIndicatorView;
import com.android.camera.ui.TouchCoordinate;
import com.android.camera.ui.ZoomBar;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.BitmapPackager;
import com.android.camera.util.BlurUtil;
import com.android.camera.util.BoostUtil;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.Gusterpolator;
import com.android.camera.util.PhotoSphereHelper;
import com.android.camera.widget.ButtonGroup;
import com.android.camera.widget.Cling;
import com.android.camera.widget.FilmstripLayout;
import com.android.camera.widget.IndicatorIconController;
import com.android.camera.widget.ModeOptionsOverlay;
import com.android.camera.widget.PeekView;
import com.android.camera.widget.TopMenus;
import com.tct.camera.R;

import java.util.HashMap;
import java.util.Map;

/**
 * CameraAppUI centralizes control of views shared across modules. Whereas module
 * specific views will be handled in each Module UI. For example, we can now
 * bring the flash animation and capture animation up from each module to app
 * level, as these animations are largely the same for all modules.
 *
 * This class also serves to disambiguate touch events. It recognizes all the
 * swipe gestures that happen on the preview by attaching a touch listener to
 * a full-screen view on top of preview TextureView. Since CameraAppUI has knowledge
 * of how swipe from each direction should be handled, it can then redirect these
 * events to appropriate recipient views.
 */
public class CameraAppUI implements ModeStripView.OnModeIdListener,
                                    ModeListView.ModeSwitchListener,
                                    TextureView.SurfaceTextureListener,
                                    ModeListView.ModeListOpenListener,
                                    SettingsManager.OnSettingChangedListener,
                                    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-27,BUG-3212745*/
                                    ShutterButton.OnShutterButtonListener,
                                    ZoomBar.ZoomBarVisibleChangedListener {
                                    /* MODIFIED-END by jianying.zhang,BUG-3212745*/

    /**
     * The bottom controls on the filmstrip.
     */
    public static interface BottomPanel {
        /** Values for the view state of the button. */
        public final int VIEWER_NONE = 0;
        public final int VIEWER_PHOTO_SPHERE = 1;
        public final int VIEWER_REFOCUS = 2;
        public final int VIEWER_OTHER = 3;

        /**
         * Sets a new or replaces an existing listener for bottom control events.
         */
        void setListener(Listener listener);

        /**
         * Sets cling for external viewer button.
         */
        void setClingForViewer(int viewerType, Cling cling);

        /**
         * Clears cling for external viewer button.
         */
        void clearClingForViewer(int viewerType);

        /**
         * Returns a cling for the specified viewer type.
         */
        Cling getClingForViewer(int viewerType);

        /**
         * Set if the bottom controls are visible.
         * @param visible {@code true} if visible.
         */
        void setVisible(boolean visible);

        /**
         * @param visible Whether the button is visible.
         */
        void setEditButtonVisibility(boolean visible);

        /**
         * @param enabled Whether the button is enabled.
         */
        void setEditEnabled(boolean enabled);

        /**
         * Sets the visibility of the view-photosphere button.
         *
         * @param state one of {@link #VIEWER_NONE}, {@link #VIEWER_PHOTO_SPHERE},
         *            {@link #VIEWER_REFOCUS}.
         */
        void setViewerButtonVisibility(int state);

        /**
         * @param enabled Whether the button is enabled.
         */
        void setViewEnabled(boolean enabled);

        /**
         * @param enabled Whether the button is enabled.
         */
        void setTinyPlanetEnabled(boolean enabled);

        /**
         * @param visible Whether the button is visible.
         */
        void setDeleteButtonVisibility(boolean visible);

        /**
         * @param enabled Whether the button is enabled.
         */
        void setDeleteEnabled(boolean enabled);

        /**
         * @param visible Whether the button is visible.
         */
        void setShareButtonVisibility(boolean visible);

        /**
         * @param enabled Whether the button is enabled.
         */
        void setShareEnabled(boolean enabled);

        /**
         * Sets the texts for progress UI.
         *
         * @param text The text to show.
         */
        void setProgressText(CharSequence text);

        /**
         * Sets the progress.
         *
         * @param progress The progress value. Should be between 0 and 100.
         */
        void setProgress(int progress);

        /**
         * Replaces the progress UI with an error message.
         */
        void showProgressError(CharSequence message);

        /**
         * Hide the progress error message.
         */
        void hideProgressError();

        /**
         * Shows the progress.
         */
        void showProgress();

        /**
         * Hides the progress.
         */
        void hideProgress();

        /**
         * Shows the controls.
         */
        void showControls();

        /**
         * Hides the controls.
         */
        void hideControls();

        /**
         * Classes implementing this interface can listen for events on the bottom
         * controls.
         */
        public static interface Listener {
            /**
             * Called when the user pressed the "view" button to e.g. view a photo
             * sphere or RGBZ image.
             */
            public void onExternalViewer();

            /**
             * Called when the "edit" button is pressed.
             */
            public void onEdit();

            /**
             * Called when the "tiny planet" button is pressed.
             */
            public void onTinyPlanet();

            /**
             * Called when the "delete" button is pressed.
             */
            public void onDelete();

            /**
             * Called when the "share" button is pressed.
             */
            public void onShare();

            /**
             * Called when the progress error message is clicked.
             */
            public void onProgressErrorClicked();
        }
    }

    /**
     * BottomBarUISpec provides a structure for modules
     * to specify their ideal bottom bar mode options layout.
     *
     * Once constructed by a module, this class should be
     * treated as read only.
     *
     * The application then edits this spec according to
     * hardware limitations and displays the final bottom
     * bar ui.
     */
    public static class BottomBarUISpec {
        /** Mode options UI */

        /**
         * Set true if the camera option should be enabled.
         * If not set or false, and multiple cameras are supported,
         * the camera option will be disabled.
         *
         * If multiple cameras are not supported, this preference
         * is ignored and the camera option will not be visible.
         */
        public boolean enableCamera;

        /**
         * Set true if the camera option should not be visible, regardless
         * of hardware limitations.
         */
        public boolean hideCamera;

        /**
         * Set true if the photo flash option should be enabled.
         * If not set or false, the photo flash option will be
         * disabled.
         *
         * If the hardware does not support multiple flash values,
         * this preference is ignored and the flash option will
         * be disabled.  It will not be made invisible in order to
         * preserve a consistent experience across devices and between
         * front and back cameras.
         */
        public boolean enableFlash;

        /**
         * Set true if the video flash option should be enabled.
         * Same disable rules apply as the photo flash option.
         */
        public boolean enableTorchFlash;

        /**
         * Set true if the HDR+ flash option should be enabled.
         * Same disable rules apply as the photo flash option.
         */
        public boolean enableHdrPlusFlash;

        /**
         * Set true if flash should not be visible, regardless of
         * hardware limitations.
         */
        public boolean hideFlash;

        public boolean hideContactsBack;
        public boolean hideContactsFlash;

        /**
         * Set true if the hdr/hdr+ option should be enabled.
         * If not set or false, the hdr/hdr+ option will be disabled.
         *
         * Hdr or hdr+ will be chosen based on hardware limitations,
         * with hdr+ prefered.
         *
         * If hardware supports neither hdr nor hdr+, then the hdr/hdr+
         * will not be visible.
         */
        public boolean enableHdr;

        /**
         * Set true if hdr/hdr+ should not be visible, regardless of
         * hardware limitations.
         */
        public boolean hideHdr;

        /**
         * Set true if grid lines should be visible.  Not setting this
         * causes grid lines to be disabled.  This option is agnostic to
         * the hardware.
         */
        public boolean enableGridLines;

        /**
         * Set true if grid lines should not be visible.
         */
        public boolean hideGridLines;

        /**
         * Set true if the panorama orientation option should be visible.
         *
         * This option is not constrained by hardware limitations.
         */
        public boolean enablePanoOrientation;

        public boolean enableExposureCompensation;

        /** Intent UI */

        /**
         * Set true if the intent ui cancel option should be visible.
         */
        public boolean showCancel;
        /**
         * Set true if the intent ui done option should be visible.
         */
        public boolean showDone;
        /**
         * Set true if the intent ui retake option should be visible.
         */
        public boolean showRetake;
        /**
         * Set true if the intent ui review option should be visible.
         */
        public boolean showReview;

        public boolean hideLowlight;

        /**
         * Set true if setting button should not be visible.
         */
        public boolean hideSetting;

        /**
         * Set true if Pose button should not be visible.
         */
        public boolean showPose;
        /**
         * Set true if Filter button should not be visible.
         */
        public boolean showFilter;

        /**
         * Set true if switch button should not be visible.
         */
        public boolean hideCameraForced;

        /**
         * hide switch button , other than make it gone , it should still hold the position in the modeOptionBar
         */
        public boolean setCameraInvisible;

        /** Mode options callbacks */

        /**
         * A {@link com.android.camera.ButtonManager.ButtonCallback}
         * that will be executed when the camera option is pressed. This
         * callback can be null.
         */
        public ButtonManager.ButtonCallback cameraCallback;

        public BottomBar.SwitchButtonCallback switchButtonCallback;

        /**
         * A {@link com.android.camera.ButtonManager.ButtonCallback}
         * that will be executed when the flash option is pressed. This
         * callback can be null.
         */
        public ButtonManager.ButtonCallback flashCallback;

        /**
         * A {@link com.android.camera.ButtonManager.ButtonCallback}
         * that will be executed when the hdr/hdr+ option is pressed. This
         * callback can be null.
         */
        public ButtonManager.ButtonCallback hdrCallback;

        /**
         * A {@link com.android.camera.ButtonManager.ButtonCallback}
         * that will be executed when the grid lines option is pressed. This
         * callback can be null.
         */
        public ButtonManager.ButtonCallback gridLinesCallback;

        /**
         * A {@link com.android.camera.ButtonManager.ButtonCallback}
         * that will execute when the panorama orientation option is pressed.
         * This callback can be null.
         */
        public ButtonManager.ButtonCallback panoOrientationCallback;

        public ButtonManager.ButtonCallback lowlightCallback;
        /** Intent UI callbacks */

        /**
         * A {@link android.view.View.OnClickListener} that will execute
         * when the cancel option is pressed. This callback can be null.
         */
        public View.OnClickListener cancelCallback;

        /**
         * A {@link android.view.View.OnClickListener} that will execute
         * when the done option is pressed. This callback can be null.
         */
        public View.OnClickListener doneCallback;

        /**
         * A {@link android.view.View.OnClickListener} that will execute
         * when the retake option is pressed. This callback can be null.
         */
        public View.OnClickListener retakeCallback;

        /**
         * A {@link android.view.View.OnClickListener} that will execute
         * when the review option is pressed. This callback can be null.
         */
        public View.OnClickListener reviewCallback;

        /**
         * A ExposureCompensationSetCallback that will execute
         * when an expsosure button is pressed. This callback can be null.
         */
        public interface ExposureCompensationSetCallback {
            public void setExposure(int value);
        }
        public ExposureCompensationSetCallback exposureCompensationSetCallback;

        /**
         * Exposure compensation parameters.
         */
        public int minExposureCompensation;
        public int maxExposureCompensation;
        public float exposureCompensationStep;

        /**
         * Whether self-timer is enabled.
         */
        public boolean enableSelfTimer = false;

        /**
         * Whether the option for self-timer should show. If true and
         * {@link #enableSelfTimer} is false, then the option should be shown
         * disabled.
         */
        public boolean showSelfTimer = false;

        /**
         * Whether showTimeIndicator is show.
         */
        public boolean showTimeIndicator = false;

        /**
         * Whether the option for expand should show,a wrap for manual module expand button
         */
        public boolean showWrapperButton = false;
    }

    public enum LockState{
        BLOCKING,
        IDLE,
        BLOCK_FROM_SHUTTER,
        BLOCK_FROM_MENU,
        BLOCK_FROM_MODE_SWITCHING,
    }


    private final static Log.Tag TAG = new Log.Tag("CameraAppUI");
    private final static String POSE_TAG = "poseFragment"; // MODIFIED by fei.hui, 2016-09-29,BUG-2994050

    private final AppController mController;
    private final boolean mIsCaptureIntent;
    private final AnimationManager mAnimationManager;

    private LockState mLockState;
    // Swipe states:
    private final static int IDLE = 0;
    private final static int SWIPE_UP = 1;
    private final static int SWIPE_DOWN = 2;
    private final static int SWIPE_LEFT = 3;
    private final static int SWIPE_RIGHT = 4;
    private boolean mSwipeEnabled = true;

    // Shared Surface Texture properities.
    private SurfaceTexture mSurface;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    // Touch related measures:
    private final int mSlop;
    private final static int SWIPE_TIME_OUT_MS = 500;

    // Mode cover states:
    private final static int COVER_HIDDEN = 0;
    private final static int COVER_SHOWN = 1;
    private final static int COVER_WILL_HIDE_AT_NEXT_FRAME = 2;
    private static final int COVER_WILL_HIDE_AT_NEXT_TEXTURE_UPDATE = 3;
    public static final int SHOW_NAVIGATION_VIEW = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LOW_PROFILE;

    public static final int HIDE_NAVIGATION_VIEW = View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE;

    /**
     * Preview down-sample rate when taking a screenshot.
     */
    private final static int DOWN_SAMPLE_RATE_FOR_SCREENSHOT = 2;

    private static final int BUTTON_CAMERA = 1;
    private static final int BUTTON_SWITCH = 2;
    // App level views:
    private final FrameLayout mCameraRootView;
    private final ModeTransitionView mModeTransitionView;
    private final MainActivityLayout mAppRootView;
    private final ModeStrip mModeStripView;
    private ScrollIndicator mModeScrollIndicator;
    private TextureView mTextureView;
    private FrameLayout mModuleUI;
    private ShutterButton mShutterButton;
    private RotatableButton mVideoShuttterBotton;
    private RotatableButton mPauseRecord; // MODIFIED by jianying.zhang, 2016-11-11,BUG-3445767
    private ShutterSaveProgressbar mShutterProgress;
    private PeekImageView mPeekThumb;
    private Button mContactsIntentPeekThumb;
    private CenterGridLines mContactsGridLines;
    private RotatableButton mCaptureButton;
    private RotatableButton mSegmentRemoveButton;
    private RotatableButton mRemixButton;
    private RotatableButton mCancelButton;
    private View mMicroVideoProgressbar;
    private BottomBar mBottomBar;
    private ModeOptionsOverlay mModeOptionsOverlay;
    private View mFocusOverlay;
    private FrameLayout mTutorialsPlaceHolderWrapper;
    private BottomBarModeOptionsWrapper mIndicatorBottomBarWrapper;
    private TextureViewHelper mTextureViewHelper;
    private final GestureDetector mGestureDetector;
    private DisplayManager.DisplayListener mDisplayListener;
    private int mLastRotation;
    private int mSwipeState = IDLE;
    private PreviewOverlay mPreviewOverlay;
    private ZoomBar mZoomBar;
    private GridLines mGridLines;
    private PoseFragment mPoseFragment;

    private TopMenus mTopMenus;
    /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-12,BUG-1892480*/
    private ImageView mComposeImage;
    /*MODIFIED-END by shunyin.zhang,BUG-1892480*/

    private CaptureAnimationOverlay mCaptureOverlay;
    private PreviewStatusListener mPreviewStatusListener;
    private int mModeCoverState = COVER_HIDDEN;
    private Runnable mHideCoverRunnable;
    private final View.OnLayoutChangeListener mPreviewLayoutChangeListener
            = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                int oldTop, int oldRight, int oldBottom) {
            if (mPreviewStatusListener != null) {
                mPreviewStatusListener.onPreviewLayoutChanged(v, left, top, right, bottom, oldLeft,
                        oldTop, oldRight, oldBottom);
            }
        }
    };
    private View mModeOptionsToggle;
    private final CaptureLayoutHelper mCaptureLayoutHelper;
    private boolean mAccessibilityEnabled;

    private FrameLayout mViewFinderLayout;
    private boolean mDisableAllUserInteractions;
    private boolean mPoseSelectorShowing = false;

    private final IntentReviewControls mIntentReviewControls;

    private HelpTipsManager mHelpTipsManager;
    private final boolean mVdfModeSwitcherOn;

    private LockRotatableButton mSwitchButton;

    private FrameLayout mCameraSettingLayout;
    private CameraSettingsFragment mSettingFragment;
    private FrameLayout mPoseDetailLayout; // MODIFIED by fei.hui, 2016-09-29,BUG-2994050

    private final boolean mEnableBlurDuringTransition=CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_ENABLE_BLUR,true);
    /**
     * Provides current preview frame and the controls/overlay from the module that
     * are shown on top of the preview.
     */
    public interface CameraModuleScreenShotProvider {
        /**
         * Returns the current preview frame down-sampled using the given down-sample
         * factor.
         *
         * @param downSampleFactor the down sample factor for down sampling the
         *                         preview frame. (e.g. a down sample factor of
         *                         2 means to scale down the preview frame to 1/2
         *                         the width and height.)
         * @return down-sampled preview frame
         */
        public Bitmap getPreviewFrame(int downSampleFactor);

        public Bitmap getRawPreviewFrame(int downSampleFactor);

        /**
         * @return the controls and overlays that are currently showing on top of
         *         the preview drawn into a bitmap with no scaling applied.
         */
        public Bitmap getPreviewOverlayAndControls();

        /**
         * Returns a bitmap containing the current screenshot.
         *
         * @param previewDownSampleFactor the downsample factor applied on the
         *                                preview frame when taking the screenshot
         */
        public Bitmap getScreenShot(int previewDownSampleFactor);
    }

    /**
     * This listener gets called when the size of the window (excluding the system
     * decor such as status bar and nav bar) has changed.
     */
    public interface NonDecorWindowSizeChangedListener {
        public void onNonDecorWindowSizeChanged(int width, int height, int rotation);
    }

    private final CameraModuleScreenShotProvider mCameraModuleScreenShotProvider =
            new CameraModuleScreenShotProvider() {
                @Override
                public Bitmap getPreviewFrame(int downSampleFactor) {
                    if (mCameraRootView == null || mTextureView == null) {
                        return null;
                    }
                    // Gets the bitmap from the preview TextureView.
                    Bitmap preview = mTextureViewHelper.getPreviewBitmap(downSampleFactor);
                    if (preview == null) {
                        return null;
                    }
                    if(mEnableBlurDuringTransition) {
                        Log.v(TAG, "blur start");
                        preview = BlurUtil.blur(preview);
                        //RenderScript gaussian blur impl currently takes about 1.5s to render a equalize picture with double radius(50), maybe optimized in later work
                        preview = BlurUtil.blur(preview);
                    }
                    Log.v(TAG, "blur record");
                    return preview;
                }

                @Override
                public Bitmap getRawPreviewFrame(int downSampleFactor) {
                    if (mCameraRootView == null || mTextureView == null) {
                        return null;
                    }
                    // Gets the bitmap from the preview TextureView.
                    Bitmap preview = mTextureViewHelper.getPreviewBitmap(downSampleFactor);
                    return preview;
                }

                @Override
                public Bitmap getPreviewOverlayAndControls() {
                    Bitmap overlays = Bitmap.createBitmap(mCameraRootView.getWidth(),
                            mCameraRootView.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(overlays);
                    mCameraRootView.draw(canvas);
                    return overlays;
                }

                @Override
                public Bitmap getScreenShot(int previewDownSampleFactor) {
                    Bitmap screenshot = Bitmap.createBitmap(mCameraRootView.getWidth(),
                            mCameraRootView.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(screenshot);
                    canvas.drawARGB(255, 0, 0, 0);
                    Bitmap preview = mTextureViewHelper.getPreviewBitmap(previewDownSampleFactor);
                    if (preview != null) {
                        canvas.drawBitmap(preview, null, mTextureViewHelper.getPreviewArea(), null);
                    }
                    Bitmap overlay = getPreviewOverlayAndControls();
                    if (overlay != null) {
                        canvas.drawBitmap(overlay, 0f, 0f, null);
                    }

                    return screenshot;
                }

            };

    private long mCoverHiddenTime = -1; // System time when preview cover was hidden.

    public long getCoverHiddenTime() {
        return mCoverHiddenTime;
    }

    /**
     * This resets the preview to have no applied transform matrix.
     */
    public void clearPreviewTransform() {
        mTextureViewHelper.clearTransform();
    }

    public void updatePreviewAspectRatio(float aspectRatio) {
        mTextureViewHelper.updateAspectRatio(aspectRatio);
    }

    /**
     * WAR: Reset the SurfaceTexture's default buffer size to the current view dimensions of
     * its TextureView.  This is necessary to get the expected behavior for the TextureView's
     * HardwareLayer transform matrix (set by TextureView#setTransform) after configuring the
     * SurfaceTexture as an output for the Camera2 API (which involves changing the default buffer
     * size).
     *
     * b/17286155 - Tracking a fix for this in HardwareLayer.
     */
    public void setDefaultBufferSizeToViewDimens() {
        if (mSurface == null || mTextureView == null) {
            Log.w(TAG, "Could not set SurfaceTexture default buffer dimensions, not yet setup");
            return;
        }
        mSurface.setDefaultBufferSize(mTextureView.getWidth(), mTextureView.getHeight());
    }

    /**
     * Updates the preview matrix without altering it.
     *
     * @param matrix
     * @param aspectRatio the desired aspect ratio for the preview.
     */
    public void updatePreviewTransformFullscreen(Matrix matrix, float aspectRatio) {
        mTextureViewHelper.updateTransformFullScreen(matrix, aspectRatio);
    }

    /**
     * @return the rect that will display the preview.
     */
    public RectF getFullscreenRect() {
        return mTextureViewHelper.getFullscreenRect();
    }

    /**
     * This is to support modules that calculate their own transform matrix because
     * they need to use a transform matrix to rotate the preview.
     *
     * @param matrix transform matrix to be set on the TextureView
     */
    public void updatePreviewTransform(Matrix matrix) {
        mTextureViewHelper.updateTransform(matrix);
    }

    public interface AnimationFinishedListener {
        public void onAnimationFinished(boolean success);
    }

    private class MyTouchListener implements View.OnTouchListener {
        private boolean mScaleStarted = false;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mScaleStarted = false;
            } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                mScaleStarted = true;
            }
            return (!mScaleStarted) && mGestureDetector.onTouchEvent(event);
        }
    }

    /**
     * This gesture listener finds out the direction of the scroll gestures and
     * sends them to CameraAppUI to do further handling.
     */
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private MotionEvent mDown;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent ev, float distanceX, float distanceY) {
            if (ev.getEventTime() - ev.getDownTime() > SWIPE_TIME_OUT_MS
                    || mSwipeState != IDLE
                    || mIsCaptureIntent
                    || !mSwipeEnabled) {
                return false;
            }

            int deltaX = (int) (ev.getX() - mDown.getX());
            int deltaY = (int) (ev.getY() - mDown.getY());
            if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
                if (Math.abs(deltaX) > mSlop || Math.abs(deltaY) > mSlop) {
                    // Calculate the direction of the swipe.
                    if (deltaX >= Math.abs(deltaY)) {
                        // Swipe right.
                        setSwipeState(SWIPE_RIGHT);
                    } else if (deltaX <= -Math.abs(deltaY)) {
                        // Swipe left.
                        setSwipeState(SWIPE_LEFT);
                    }
                }
            }
            return true;
        }

        private void setSwipeState(int swipeState) {
            mSwipeState = swipeState;
            // Notify new swipe detected.
            onSwipeDetected(swipeState);
        }

        @Override
        public boolean onDown(MotionEvent ev) {
            mDown = MotionEvent.obtain(ev);
            mSwipeState = IDLE;
            return false;
        }
    }

    /* MODIFIED-BEGIN by Sichao Hu, 2016-09-23,BUG-2989818*/
    public void quitFilter(){
        if (mController == null || mController.isPaused()) {
            Log.e(TAG, "CameraActivity paused, don't start recording");
            return;
        }

        if (mModeStripView.isLocked()) {//Not allowed to change mode under this situation.
            Log.e(TAG, "mModeStripView.isLocked()");
            return;
        }


        gLockFSM.forceBlocking();
        mController.onModeSelecting(true, new ModeTransitionView.OnTransAnimationListener() {
            @Override
            public void onAnimationDone() {
                int photoModeId=mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_photo);
                int videoModeId=mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_video);
                int filterModeId = mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_filter);
                int videoFilterModeId = mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_videofilter);
                mController.setIndenticalCameraQuiteFilter(true); // MODIFIED by jianying.zhang, 2016-11-08,BUG-3255060
                if(mController.getCurrentModuleIndex()==filterModeId){
                    onModeSelected(photoModeId);
                }else if(mController.getCurrentModuleIndex()==videoFilterModeId){
                    onModeSelected(videoModeId);
                }
                hideModeOptions();
            }
        });
    }
    /* MODIFIED-END by Sichao Hu,BUG-2989818*/

    public CameraAppUI(AppController controller, MainActivityLayout appRootView,
            boolean isCaptureIntent) {
        mSlop = ViewConfiguration.get(controller.getAndroidContext()).getScaledTouchSlop();
        mController = controller;
        mIsCaptureIntent = isCaptureIntent;

        mAppRootView = appRootView;
        mCameraRootView = (FrameLayout) appRootView.findViewById(R.id.camera_app_root);
        mModeTransitionView = (ModeTransitionView)
                mAppRootView.findViewById(R.id.mode_transition_view);
        mCameraSettingLayout = (FrameLayout) mAppRootView.findViewById(R.id.camera_settings_fragment);
        mModeStripView =(StereoModeStripView)mAppRootView.findViewById(R.id.mode_strip_view);
        mModeScrollIndicator=(StereoScrollIndicatorView)mAppRootView.findViewById(R.id.mode_scroll_indicator);
        if(mModeStripView!=null){
            mModeStripView.setModeIndexChangeListener(this);
//            mModeStripView.addScrollBar(mModeScrollBar);
        }
        mPoseDetailLayout = (FrameLayout)appRootView.findViewById(R.id.pose_layout); // MODIFIED by fei.hui, 2016-09-29,BUG-2994050
        mTopMenus = (TopMenus)mAppRootView.findViewById(R.id.top_mode_options);
        mVdfModeSwitcherOn = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_FIX_MODE_SWITCHING, false);
        if (!mVdfModeSwitcherOn){
            mModeScrollIndicator=(StereoScrollIndicatorView)mAppRootView.findViewById(R.id.mode_scroll_indicator);
            mModeStripView.attachScrollIndicator(mModeScrollIndicator);
            setModeScrollBarVisibility(true);
        }
        if (mIsCaptureIntent) {
            setModeSwitchUIVisibility(false);
        }

        mGestureDetector = new GestureDetector(controller.getAndroidContext(),
                new MyGestureListener());
        Resources res = controller.getAndroidContext().getResources();
        int minTopOptionHeight;
        int maxTopOptionheight;
        if(CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_SWITCH_CAMERA_ICONS_CUSTOMIZE, false)){
            minTopOptionHeight = res.getDimensionPixelSize(R.dimen.mode_option_height_start_vf);
            maxTopOptionheight = res.getDimensionPixelSize(R.dimen.mode_option_height_end_vf);
        }else {
            minTopOptionHeight = res.getDimensionPixelSize(R.dimen.mode_option_height_start);
            maxTopOptionheight = res.getDimensionPixelSize(R.dimen.mode_option_height_end);
        }

        mCaptureLayoutHelper = new CaptureLayoutHelper(mController.getServices(),
                res.getDimensionPixelSize(R.dimen.bottom_bar_height_min),
                res.getDimensionPixelSize(R.dimen.bottom_bar_height_max),
                res.getDimensionPixelSize(R.dimen.bottom_bar_height_optimal),
                res.getDimensionPixelSize(R.dimen.bottom_bar_height_third_party),
                minTopOptionHeight,
                maxTopOptionheight,
                mIsCaptureIntent);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) controller).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mCaptureLayoutHelper.setDisplayHeight(dm.heightPixels);
        mCaptureLayoutHelper.setNavigationBarHeight(getNavigationHeight());
        mAnimationManager = new AnimationManager();
        mAppRootView.setNonDecorWindowSizeChangedListener(mCaptureLayoutHelper);
        initDisplayListener();

        mIntentReviewControls = (IntentReviewControls) appRootView.findViewById(R.id.intent_review_controls);
        mViewFinderLayout = (FrameLayout) mAppRootView.findViewById(R.id.view_finder_layout);
    }

    public boolean isScreenReversed(){
        boolean isScreenReversed=mCaptureLayoutHelper==null?false:mCaptureLayoutHelper.isScreenReversed();
        return isScreenReversed;
    }

    public void setViewFinderLayoutVisibile(boolean isVisible) {
        if (mViewFinderLayout == null) {
            return;
        }
        if (isVisible) {
            mViewFinderLayout.setVisibility(View.VISIBLE);
        } else {
            mViewFinderLayout.setVisibility(View.GONE);
        }
    }
    public void setModeSwitchUIVisibility(boolean isVisible) {
        setModeScrollBarVisibility(isVisible);
        setModeStripViewVisibility(isVisible);
    }

    public void setModeScrollBarVisibility(boolean isVisible) {
//        if (mModeScrollBar == null) {
//            return;
//        }
        if(mModeScrollIndicator==null){
            return;
        }
        if (!mVdfModeSwitcherOn){
            if(isVisible){
                ((View)mModeScrollIndicator).setVisibility(View.VISIBLE);
            }else{
                ((View)mModeScrollIndicator).setVisibility(View.GONE);
            }

        }else {
            ((View)mModeScrollIndicator).setVisibility(View.GONE);
        }
//        if (isVisible) {
//            mModeScrollBar.show();
//        } else {
//            mModeScrollBar.hide();
//        }
    }

    public void setModeStripViewVisibility(boolean isVisible) {
        if (mModeStripView == null) {
            return;
        }

        if (isVisible) {
            ((StereoModeStripView)mModeStripView).setVisibility(View.VISIBLE);
        } else {
            ((StereoModeStripView)mModeStripView).setVisibility(View.INVISIBLE);
        }
    }

    private void setModeCoverState(int state){
        Log.w(TAG, "set mode cover state: " + state);
        mModeCoverState=state;
        if(mModeStripView!=null){
            if(mModeCoverState==COVER_SHOWN){
                gLockFSM.onModeSwitching();
            }
        }
    }


    /**
     * Freeze what is currently shown on screen until the next preview frame comes
     * in.
     */
    public void freezeScreenUntilPreviewReady() {
        freezeScreenUntilPreviewReady(true, (ModeTransitionView.OnTransAnimationListener[]) null);
    }


    public void freezeScreenUntilPreviewReady(boolean needBlur, final ModeTransitionView.OnTransAnimationListener ... listeners){
        if(mModeCoverState==COVER_SHOWN){
            if(listeners==null||listeners.length==0){
                return;
            }else{
                mModeTransitionView.post(new Runnable() {
                    @Override
                    public void run() {
                        for (ModeTransitionView.OnTransAnimationListener listener : listeners) {
                            if (listener != null) {
                                listener.onAnimationDone();
                            }
                        }
                    }
                });

            }
            return;
        }

        if(!needBlur){
            mModeTransitionView.setupModeCoverTileAnimationDone(mCameraModuleScreenShotProvider
                    .getRawPreviewFrame(DOWN_SAMPLE_RATE_FOR_SCREENSHOT), 0, listeners);
        }else if(listeners==null||listeners.length==0){
            mModeTransitionView.setupModeCoverTileAnimationDone(mCameraModuleScreenShotProvider
                    .getPreviewFrame(DOWN_SAMPLE_RATE_FOR_SCREENSHOT));
        }else {
            mModeTransitionView.setupModeCoverTileAnimationDone(mCameraModuleScreenShotProvider
                    .getPreviewFrame(DOWN_SAMPLE_RATE_FOR_SCREENSHOT), listeners);
        }
        Log.w(TAG, "init hideCoverRunnable");
        mHideCoverRunnable = new Runnable() {
            @Override
            public void run() {
                mModeTransitionView.hideImageCover();
            }
        };
//        mModeCoverState = COVER_SHOWN;
        setModeCoverState(COVER_SHOWN);
    }

    public Bitmap getCoveredBitmap() {
        return mModeTransitionView == null ? null :
                mModeTransitionView.getBackgroundBitmap();
    }

    public RectF getCoveredArea() {
        return mModeTransitionView == null ? new RectF() :
                mModeTransitionView.getCoveredRect();
    }

    public RectF getPreviewArea() {
        if (mTextureViewHelper == null) {
            return new RectF();
        }
        return mTextureViewHelper.getPreviewArea();
    }

    /**
     * Creates a cling for the specific viewer and links the cling to the corresponding
     * button for layout position.
     *
     * @param viewerType defines which viewer the cling is for.
     */
    public void setupClingForViewer(int viewerType) {
        if (viewerType == BottomPanel.VIEWER_REFOCUS) {
            FrameLayout filmstripContent = (FrameLayout) mAppRootView
                    .findViewById(R.id.camera_filmstrip_content_layout);
            if (filmstripContent != null) {
                // Creates refocus cling.
                LayoutInflater inflater = (LayoutInflater) mController.getAndroidContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                Cling refocusCling = (Cling) inflater.inflate(R.layout.cling_widget, null, false);
                // Sets instruction text in the cling.
                refocusCling.setText(mController.getAndroidContext().getResources()
                        .getString(R.string.cling_text_for_refocus_editor_button));

                // Adds cling into view hierarchy.
                int clingWidth = mController.getAndroidContext()
                        .getResources().getDimensionPixelSize(R.dimen.default_cling_width);
                filmstripContent.addView(refocusCling, clingWidth,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    /**
     * Enable or disable swipe gestures. We want to disable them e.g. while we
     * record a video.
     */
    public void setSwipeEnabled(boolean enabled) {
        mSwipeEnabled = enabled;
        // TODO: This can be removed once we come up with a new design for handling swipe
        // on shutter button and mode options. (More details: b/13751653)
        mAppRootView.setSwipeEnabled(enabled);
    }

    public void onDestroy() {
        ((DisplayManager) mController.getAndroidContext()
                .getSystemService(Context.DISPLAY_SERVICE))
                .unregisterDisplayListener(mDisplayListener);
    }

    /**
     * Initializes the display listener to listen to display changes such as
     * 180-degree rotation change, which will not have an onConfigurationChanged
     * callback.
     */
    private void initDisplayListener() {
        if (ApiHelper.HAS_DISPLAY_LISTENER) {
            mLastRotation = CameraUtil.getDisplayRotation(mController.getAndroidContext());

            mDisplayListener = new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int arg0) {
                    // Do nothing.
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    int rotation = CameraUtil.getDisplayRotation(
                            mController.getAndroidContext());
                    if ((rotation - mLastRotation + 360) % 360 == 180
                            && mPreviewStatusListener != null) {
                        mPreviewStatusListener.onPreviewFlipped();
                        mIndicatorBottomBarWrapper.requestLayout();
                        mTextureView.requestLayout();
                    }
                    mLastRotation = rotation;
                }

                @Override
                public void onDisplayRemoved(int arg0) {
                    // Do nothing.
                }
            };

            ((DisplayManager) mController.getAndroidContext()
                    .getSystemService(Context.DISPLAY_SERVICE))
                    .registerDisplayListener(mDisplayListener, null);
        }
    }

    /**
     * Redirects touch events to appropriate recipient views based on swipe direction.
     * More specifically, swipe up and swipe down will be handled by the view that handles
     * mode transition; swipe left will be send to filmstrip; swipe right will be redirected
     * to mode list in order to bring up mode list.
     */
    private void onSwipeDetected(int swipeState) {
        if (swipeState == SWIPE_UP || swipeState == SWIPE_DOWN) {
            // TODO: Polish quick switch after this release.
            // Quick switch between modes.
            int currentModuleIndex = mController.getCurrentModuleIndex();
            final int moduleToTransitionTo =
                    mController.getQuickSwitchToModuleId(currentModuleIndex);
            if (currentModuleIndex != moduleToTransitionTo) {
//                mAppRootView.redirectTouchEventsTo(mModeTransitionView);
                int shadeColorId = R.color.mode_cover_default_color;
                int iconRes = CameraUtil.getCameraModeCoverIconResId(moduleToTransitionTo,
                        mController.getAndroidContext());

                AnimationFinishedListener listener = new AnimationFinishedListener() {
                    @Override
                    public void onAnimationFinished(boolean success) {
                        if (success) {
                            mHideCoverRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    mModeTransitionView.startPeepHoleAnimation();
                                }
                            };
//                            mModeCoverState = COVER_SHOWN;
                            setModeCoverState(COVER_SHOWN);
                            // Go to new module when the previous operation is successful.
                            mController.onModeSelected(moduleToTransitionTo);
                        }
                    }
                };
            }
        } else if (swipeState == SWIPE_LEFT) {
            // Pass the touch sequence to filmstrip layout.
//            mAppRootView.redirectTouchEventsTo(mFilmstripLayout);
        } else if (swipeState == SWIPE_RIGHT) {
            // Pass the touch to mode switcher
//            mAppRootView.redirectTouchEventsTo(mModeListView);
        }
    }

    /**
     * Gets called when activity resumes in preview.
     */
    public void resume() {
        // Show mode theme cover until preview is ready
//        showModeCoverUntilPreviewReady();

        // Show UI that is meant to only be used when spoken feedback is
        // enabled.
        mAccessibilityEnabled = isSpokenFeedbackAccessibilityEnabled();
    }

    /**
     * @return Whether any spoken feedback accessibility feature is currently
     *         enabled.
     */
    private boolean isSpokenFeedbackAccessibilityEnabled() {
//        AccessibilityManager accessibilityManager = (AccessibilityManager) mController
//                .getAndroidContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
//        List<AccessibilityServiceInfo> infos = accessibilityManager
//                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);
//        return infos != null && !infos.isEmpty();
        return false;
    }

    /**
     * Opens the mode list (e.g. because of the menu button being pressed) and
     * adapts the rest of the UI.
     */
    public void openModeList() {
        mModeOptionsOverlay.closeModeOptions();
    }

    /**
     * A cover view showing the mode theme color and mode icon will be visible on
     * top of preview until preview is ready (i.e. camera preview is started and
     * the first frame has been received).
     */
    private void showModeCoverUntilPreviewReady() {
        int modeId = mController.getCurrentModuleIndex();
        int colorId = R.color.mode_cover_default_color;;
        int iconId = CameraUtil.getCameraModeCoverIconResId(modeId, mController.getAndroidContext());
        mModeTransitionView.setupModeCover(colorId, iconId);
        mHideCoverRunnable = new Runnable() {
            @Override
            public void run() {
                mModeTransitionView.hideModeCover(null);
                if (!mDisableAllUserInteractions) {
                    showShimmyDelayed();
                }
            }
        };
//        mModeCoverState = COVER_SHOWN;
        setModeCoverState(COVER_SHOWN);
    }

    private void showShimmyDelayed() {
        if (!mIsCaptureIntent) {
            // Show shimmy in SHIMMY_DELAY_MS
        }
    }

    private void hideModeCover() {
        if (mHideCoverRunnable != null) {
            Log.w(TAG,"hideModeCover in CameraAppUI");
            mAppRootView.post(mHideCoverRunnable);
            mHideCoverRunnable = null;
        }
//        mModeCoverState = COVER_HIDDEN;
        setModeCoverState(COVER_HIDDEN);
        if (mCoverHiddenTime < 0) {
            mCoverHiddenTime = System.currentTimeMillis();
        }
    }


    public void onPreviewVisiblityChanged(int visibility) {
        if (visibility == ModuleController.VISIBILITY_HIDDEN) {
            setIndicatorBottomBarWrapperVisible(false);
        } else {
            setIndicatorBottomBarWrapperVisible(true);
        }
    }

    /**
     * Call to stop the preview from being rendered.
     */
    public void pausePreviewRendering() {
        mTextureView.setVisibility(View.INVISIBLE);
    }

    /**
     * Call to begin rendering the preview again.
     */
    public void resumePreviewRendering() {
        mTextureView.setVisibility(View.VISIBLE);
    }

    /**
     * Returns the transform associated with the preview view.
     *
     * @param m the Matrix in which to copy the current transform.
     * @return The specified matrix if not null or a new Matrix instance
     *         otherwise.
     */
    public Matrix getPreviewTransform(Matrix m) {
        return mTextureView.getTransform(m);
    }

    @Override
    public void onOpenFullScreen() {
        // Do nothing.
    }

    @Override
    public void onModeListOpenProgress(float progress) {
        progress = 1 - progress;
        float interpolatedProgress = Gusterpolator.INSTANCE.getInterpolation(progress);
        mModeOptionsToggle.setAlpha(interpolatedProgress);
        // Change shutter button alpha linearly based on the mode list open progress:
        // set the alpha to disabled alpha when list is fully open, to enabled alpha
        // when the list is fully closed.
        mShutterButton.setAlpha(progress * ShutterButton.ALPHA_WHEN_ENABLED
                + (1 - progress) * ShutterButton.ALPHA_WHEN_DISABLED);
    }

    @Override
    public void onModeListClosed() {
        // Make sure the alpha on mode options ellipse is reset when mode drawer
        // is closed.
        mModeOptionsToggle.setAlpha(1f);
        mShutterButton.setAlpha(ShutterButton.ALPHA_WHEN_ENABLED);
    }

    public void setCameraSettingVisible(boolean isVisible) {
        if (mCameraSettingLayout == null) {
            return;
        }
        if (isVisible) {
            mCameraSettingLayout.setVisibility(View.VISIBLE);
            gLockFSM.forceBlocking();
        } else {
            mCameraSettingLayout.setVisibility(View.GONE);
            gLockFSM.onIdle();
        }
    }

    /**
     * Called when the back key is pressed.
     *
     * @return Whether the UI responded to the key event.
     */
    public boolean onBackPressed() {
        if (isCameraSettingVisible() && mSettingFragment != null) {
            return mSettingFragment.onBackPressed();
        }

        if (needClosePoseFragment()) {
            closePoseFragment();
            /* MODIFIED-BEGIN by fei.hui, 2016-10-25,BUG-3167899*/
            if (isPoseSelectorShowing()) {
                setIsPoseSelectorShowing(false);
            }
            /* MODIFIED-END by fei.hui,BUG-3167899*/

            return true;
        } else {
            return false;
        }
    }

    public boolean needClosePoseFragment() {
        return ((mPoseDetailLayout != null) && (mPoseDetailLayout.getVisibility() == View.VISIBLE));
    }

    public void closePoseFragment() {
        if(mPoseFragment != null){
            FragmentManager fragmentManager = mController.getFragmentManager();
            fragmentManager.beginTransaction().remove(mPoseFragment).commit();
            mPoseDetailLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Sets a {@link com.android.camera.ui.PreviewStatusListener} that
     * listens to SurfaceTexture changes. In addition, listeners are set on
     * dependent app ui elements.
     *
     * @param previewStatusListener the listener that gets notified when SurfaceTexture
     *                              changes
     */
    public void setPreviewStatusListener(PreviewStatusListener previewStatusListener) {
        mPreviewStatusListener = previewStatusListener;
        if (mPreviewStatusListener != null) {
            onPreviewListenerChanged();
        }
    }

    /**
     * When the PreviewStatusListener changes, listeners need to be
     * set on the following app ui elements:
     * {@link com.android.camera.ui.PreviewOverlay},
     * {@link com.android.camera.ui.BottomBar},
     * {@link com.android.camera.ui.IndicatorIconController}.
     */
    private void onPreviewListenerChanged() {
        // Set a listener for recognizing preview gestures.
        GestureDetector.OnGestureListener gestureListener
            = mPreviewStatusListener.getGestureListener();
        if (gestureListener != null) {
            mPreviewOverlay.setGestureListener(gestureListener);
        }
        View.OnTouchListener touchListener = mPreviewStatusListener.getTouchListener();
        if (touchListener != null) {
            mPreviewOverlay.setTouchListener(touchListener);
        }

        mTextureViewHelper.setAutoAdjustTransform(
                mPreviewStatusListener.shouldAutoAdjustTransformMatrixOnLayout());
    }

    /**
     * This method should be called in onCameraOpened.  It defines CameraAppUI
     * specific changes that depend on the camera or camera settings.
     */
    public void onChangeCamera() {
        ModuleController moduleController = mController.getCurrentModuleController();
        applyModuleSpecs(moduleController.getHardwareSpec(), moduleController.getBottomBarSpec());

        if (mIsCaptureIntent) {
            setModeSwitchUIVisibility(false);
        }
    }

    /**
     * Adds a listener to receive callbacks when preview area changes.
     */
    public void addPreviewAreaChangedListener(
            PreviewStatusListener.PreviewAreaChangedListener listener) {
        mTextureViewHelper.addPreviewAreaSizeChangedListener(listener);
    }

    /**
     * Removes a listener that receives callbacks when preview area changes.
     */
    public void removePreviewAreaChangedListener(
            PreviewStatusListener.PreviewAreaChangedListener listener) {
        mTextureViewHelper.removePreviewAreaSizeChangedListener(listener);
    }

    /**
     * This inflates generic_module layout, which contains all the shared views across
     * modules. Then each module inflates their own views in the given view group. For
     * now, this is called every time switching from a not-yet-refactored module to a
     * refactored module. In the future, this should only need to be done once per app
     * start.
     */
    public void prepareModuleUI(SurfaceTexture surface,
                                boolean isPhotoContactsIntent) {
        mHelpTipsManager = mController.getHelpTipsManager();
        mAppRootView.setHelpTipManager(mHelpTipsManager);
        mController.getSettingsManager().addListener(this);
        mModuleUI = (FrameLayout) mCameraRootView.findViewById(R.id.module_layout);
        mTextureView = (TextureView) mCameraRootView.findViewById(R.id.preview_content);
        mTextureViewHelper = new TextureViewHelper(mTextureView,mModeTransitionView, mCaptureLayoutHelper,
                mController.getCameraProvider(),surface);
        mTextureViewHelper.setSurfaceTextureListener(this);
        mTextureViewHelper.setOnLayoutChangeListener(mPreviewLayoutChangeListener);

        mBottomBar = (BottomBar) mCameraRootView.findViewById(R.id.bottom_bar);
        int unpressedColor = mController.getAndroidContext().getResources()
            .getColor(R.color.bottombar_unpressed);
        setBottomBarColor(unpressedColor);
        updateModeSpecificUIColors();

        mBottomBar.setCaptureLayoutHelper(mCaptureLayoutHelper);

        mModeOptionsOverlay
            = (ModeOptionsOverlay) mCameraRootView.findViewById(R.id.mode_options_overlay);

        // Sets the visibility of the bottom bar and the mode options.
        resetBottomControls(mController.getCurrentModuleController(),// MODIFIED by sichao.hu, 2016-03-22, BUG-1027573
                mController.getCurrentModuleIndex());
        mModeOptionsOverlay.setCaptureLayoutHelper(mCaptureLayoutHelper);

        mShutterButton = (ShutterButton) mCameraRootView.findViewById(R.id.shutter_button);
        mPauseRecord = (RotatableButton) mCameraRootView.findViewById(R.id.pause_record); // MODIFIED by jianying.zhang, 2016-11-11,BUG-3445767
        mShutterProgress=(ShutterSaveProgressbar)mCameraRootView.findViewById(R.id.progressbar_of_shutter);
        addShutterListener(mController.getCurrentModuleController());
        addShutterListener(mModeOptionsOverlay);
        addShutterListener(this);

        if(mHelpTipsManager !=null){
            addShutterListener(mHelpTipsManager);
        }
        mPeekThumb=(PeekImageView)mCameraRootView.findViewById(R.id.peek_thumb);

        mPeekThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* MODIFIED-BEGIN by xuan.zhou, 2016-05-23,BUG-2167404*/
                if (mPeekThumb.getUri() == null) {
                    Log.v(TAG, "peekThumb invalid");
                    /* MODIFIED-END by xuan.zhou,BUG-2167404*/
                    return;
                }
                mController.onPeekThumbClicked(mPeekThumb.getUri());
            }
        });

        mContactsIntentPeekThumb=(Button)mCameraRootView.findViewById(R.id.contacts_intent_peek_thumb);
        mContactsIntentPeekThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.onPeekThumbClicked();
            }
        });
        mSwitchButton = (LockRotatableButton)mCameraRootView
                .findViewById(R.id.camera_toggle_button_botto_bottom);
        mMicroVideoProgressbar=mCameraRootView.findViewById(R.id.micro_video_progressbar);
        mSegmentRemoveButton=(RotatableButton)mCameraRootView.findViewById(R.id.button_segement_remove);
        mRemixButton=(RotatableButton)mCameraRootView.findViewById(R.id.button_remix);
        mController.addLockableToListenerPool(mRemixButton);
        mCaptureButton=(RotatableButton)mCameraRootView.findViewById(R.id.video_snap_button);
        mVideoShuttterBotton =(RotatableButton)mCameraRootView.findViewById(R.id.video_shutter_button);
        mCancelButton = (RotatableButton) mCameraRootView.findViewById(R.id.shutter_cancel_button);


        mVideoShuttterBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*MODIFIED-BEGIN by xuan.zhou, 2016-04-14,BUG-1945139*/
                if (mController == null || mController.isPaused()) {
                    Log.e(TAG, "CameraActivity paused, don't start recording");
                    return;
                }
                /*MODIFIED-END by xuan.zhou,BUG-1945139*/

                if (mModeStripView.isLocked()) {//Not allowed to change mode under this situation.
                    Log.e(TAG, "mModeStripView.isLocked()");
                    return;
                }

                if (mHelpTipsManager != null) {
                    mHelpTipsManager.goNextHelpTipStage();
                }

                Log.v(TAG, "click video shutter");
                gLockFSM.forceBlocking();
                mController.onModeSelecting(true, new ModeTransitionView.OnTransAnimationListener() {
                    @Override
                    public void onAnimationDone() {
                        Log.v(TAG, "try start video mode");
                        int videoModuleId = mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_video);
                        onModeSelected(videoModuleId);
                        hideModeOptions();
                    }
                });
            }
        });
        mController.addLockableToListenerPool(mPeekThumb);
        mController.addLockableToListenerPool(mVideoShuttterBotton);
        mController.addLockableToListenerPool(mShutterButton);
        mController.addLockableToListenerPool(mPauseRecord); // MODIFIED by jianying.zhang, 2016-11-11,BUG-3445767
        mController.addLockableToListenerPool(mSwitchButton);

        mController.addLockableToListenerPool(mModeStripView);
        mController.addRotatableToListenerPool(new Rotatable.RotateEntity(mPeekThumb, true));
        mController.addRotatableToListenerPool(new Rotatable.RotateEntity(mShutterButton, true));
        mController.addRotatableToListenerPool(new Rotatable.RotateEntity(mVideoShuttterBotton, true));
        mController.addRotatableToListenerPool(new Rotatable.RotateEntity(mShutterButton, true));
        mController.addRotatableToListenerPool(new Rotatable.RotateEntity(mPauseRecord, true)); // MODIFIED by jianying.zhang, 2016-11-11,BUG-3445767
        mController.addRotatableToListenerPool(new Rotatable.RotateEntity(mSegmentRemoveButton,true));
        mController.addRotatableToListenerPool(new Rotatable.RotateEntity(mRemixButton, true));
        mController.addRotatableToListenerPool(new Rotatable.RotateEntity(mCaptureButton, true));
        mController.addRotatableToListenerPool(new Rotatable.RotateEntity(mCancelButton, true));
        mController.addRotatableToListenerPool(new Rotatable.RotateEntity(mSwitchButton, true));

        mGridLines = (GridLines) mCameraRootView.findViewById(R.id.grid_lines);
        mContactsGridLines = (CenterGridLines) mCameraRootView.findViewById(R.id.grid_lines_contacts);
        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-12,BUG-1892480*/
        mComposeImage = (ImageView)mCameraRootView.findViewById(R.id.composition_image);
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/

        mTextureViewHelper.addPreviewAreaSizeChangedListener(mGridLines);

        mPreviewOverlay = (PreviewOverlay) mCameraRootView.findViewById(R.id.preview_overlay);
        mPreviewOverlay.setOnTouchListener(new MyTouchListener());
        mPreviewOverlay.addOnPreviewTouchedListener(mModeOptionsOverlay);
        mPreviewOverlay.setHelpTipsListener(mHelpTipsManager);
        mZoomBar = (ZoomBar)mCameraRootView.findViewById(R.id.zoom_bar);
        mZoomBar.setZoomBarVisibleChangedListener(this); // MODIFIED by jianying.zhang, 2016-10-27,BUG-3212745

        mCaptureOverlay = (CaptureAnimationOverlay)
                mCameraRootView.findViewById(R.id.capture_overlay);
        mTextureViewHelper.addPreviewAreaSizeChangedListener(mPreviewOverlay);
        mTextureViewHelper.addPreviewAreaSizeChangedListener(mCaptureOverlay);

        mController.getButtonManager().load(mCameraRootView);

        mTopMenus.load(mCameraRootView, mController, mIsCaptureIntent);

        mModeOptionsToggle = mCameraRootView.findViewById(R.id.mode_options_toggle);
        mFocusOverlay = mCameraRootView.findViewById(R.id.focus_overlay);
        mTutorialsPlaceHolderWrapper = (FrameLayout) mCameraRootView
                .findViewById(R.id.tutorials_placeholder_wrapper);
        mIndicatorBottomBarWrapper = (BottomBarModeOptionsWrapper) mAppRootView
                .findViewById(R.id.indicator_bottombar_wrapper);
        mIndicatorBottomBarWrapper.setPhotoCaptureIntent(isPhotoContactsIntent);
        mIndicatorBottomBarWrapper.setCaptureLayoutHelper(mCaptureLayoutHelper);
        mTextureViewHelper.addPreviewAreaSizeChangedListener(
                new PreviewStatusListener.PreviewAreaChangedListener() {
                    @Override
                    public void onPreviewAreaChanged(RectF previewArea) {
//                        mPeekView.setTranslationX(previewArea.right - mAppRootView.getRight());
//                        mPeekView.setVisibility(View.GONE);
                    }
                });

        mTextureViewHelper.addAspectRatioChangedListener(
                new PreviewStatusListener.PreviewAspectRatioChangedListener() {
                    @Override
                    public void onPreviewAspectRatioChanged(float aspectRatio) {
                        mModeOptionsOverlay.requestLayout();
                        mBottomBar.requestLayout();
                    }
                }
        );

        filterBottomBarIconsWithoutAnimation(getCurrentModeIndex());
    }

    public Uri getPeekThumbUri() {
        return mPeekThumb == null ? null :
                mPeekThumb.getUri();
    }

    public void dismissButtonGroupBar(boolean needAnimation, int animationType) {
        if (mTopMenus == null) {
            return;
        }
        mTopMenus.dismissButtonGroupBar(needAnimation, animationType);
    }

    public void mapShutterProgress(int maxProgress,int step){
        mShutterProgress.init(maxProgress, step);
    }

    public void setProgressOfShutterProgress(int progress){
        mShutterProgress.setProgress(progress);
    }

    public void startProgressAnimation(){
        mShutterProgress.startPlay();
        mShutterProgress.setVisibility(View.VISIBLE);
    }

    public void stopProgressAnimation(){
        mShutterProgress.stopPlay();
        mShutterProgress.setVisibility(View.GONE);
    }


    public void onVideoRecordingStateChanged(boolean started){
//        if(started){
//            mBottomBar.animateHidePeekAndVideoShutter();
//        }else{
//            mBottomBar.animateShowPeekAndVideoShutter();
//        }
    }

    public void updatePeekThumbContent(Thumbnail thumbnail){
        mPeekThumb.setViewThumb(thumbnail);
    }

    public void updatePeekThumbBitmapWithAnimation(Bitmap bitmap){
        mPeekThumb.animateThumbBitmap(bitmap);
    }

    public void updatePeekThumbBitmap(Bitmap bitmap){
        mPeekThumb.setViewThumbBitmap(bitmap);
    }


    public void updatePeekThumbUri(Uri peekthumbUri) {
        mPeekThumb.setViewThumbUri(peekthumbUri);
    }

    /**
     * Called indirectly from each module in their initialization to get a view group
     * to inflate the module specific views in.
     *
     * @return a view group for modules to attach views to
     */
    public FrameLayout getModuleRootView() {
        // TODO: Change it to mModuleUI when refactor is done
        return mCameraRootView;
    }

    public Integer getShutterHash(){
        return mShutterButton==null?null:mShutterButton.hashCode();
    }

    public boolean isShutterLocked(){
        return mShutterButton==null?true:(mShutterButton.isLocked());
    }

    /**
     * Remove all the module specific views.
     */
    public void clearModuleUI() {
        if (mModuleUI != null) {
            Log.v(TAG, "onAllView Removing "); //MODIFIED by xuan.zhou, 2016-04-07,BUG-1920473
            mModuleUI.removeAllViews();
        }

        Log.w(TAG, "Remove shutter listener"); // MODIFIED by xuan.zhou, 2016-05-23,BUG-2167404
        removeShutterListener(mController.getCurrentModuleController());
        mTutorialsPlaceHolderWrapper.removeAllViews();
        mTutorialsPlaceHolderWrapper.setVisibility(View.GONE);

        setShutterButtonEnabled(true);
        mPreviewStatusListener = null;
        mZoomBar.resetZoomRatio();
        mPreviewOverlay.reset();
        mFocusOverlay.setVisibility(View.INVISIBLE);
    }

    /**
     * Gets called when preview is ready to start. It sets up one shot preview callback
     * in order to receive a callback when the preview frame is available, so that
     * the preview cover can be hidden to reveal preview.
     *
     * An alternative for getting the timing to hide preview cover is through
     * {@link CameraAppUI#onSurfaceTextureUpdated(android.graphics.SurfaceTexture)},
     * which is less accurate but therefore is the fallback for modules that manage
     * their own preview callbacks (as setting one preview callback will override
     * any other installed preview callbacks), or use camera2 API.
     */
    public void onPreviewReadyToStart() {
        if (mModeCoverState == COVER_SHOWN) {
//            mModeCoverState = COVER_WILL_HIDE_AT_NEXT_FRAME;
            setModeCoverState(COVER_WILL_HIDE_AT_NEXT_FRAME);
            mController.setupOneShotPreviewListener();
        }
    }

    /**
     * Gets called when preview is started.
     */
    public void onPreviewStarted() {
        Log.v(TAG, "onPreviewStarted");
        if (mModeCoverState == COVER_SHOWN) {
//            mModeCoverState = COVER_WILL_HIDE_AT_NEXT_TEXTURE_UPDATE;
            setModeCoverState(COVER_WILL_HIDE_AT_NEXT_TEXTURE_UPDATE);
        }
        enableModeOptions();
//        unLockZoom(); // MODIFIED by xuan.zhou, 2016-05-23,BUG-2167404
    }

    /**
     * Gets notified when next preview frame comes in.
     */
    public void onNewPreviewFrame() {
        Log.v(TAG, "onNewPreviewFrame");
        CameraPerformanceTracker.onEvent(CameraPerformanceTracker.FIRST_PREVIEW_FRAME);
        hideModeCover();
    }

    @Override
    public void onShutterButtonClick() {
        /*
         * Set the mode options toggle unclickable, generally
         * throughout the app, whenever the shutter button is clicked.
         *
         * This could be done in the OnShutterButtonListener of the
         * ModeOptionsOverlay, but since it is very important that we
         * can clearly see when the toggle becomes clickable again,
         * keep all of that logic at this level.
         */
        disableModeOptions();
    }



    @Override
    public void onShutterButtonLongClick() {

    }

    @Override
    public void onShutterCoordinate(TouchCoordinate coord) {
        // Do nothing.
    }

    @Override
    public void onShutterButtonFocus(boolean pressed) {
        // noop
    }

    /**
     * Set the mode options toggle clickable.
     */
    public void enableModeOptions() {
        /*
         * For modules using camera 1 api, this gets called in
         * onSurfaceTextureUpdated whenever the preview gets stopped and
         * started after each capture.  This also takes care of the
         * case where the mode options might be unclickable when we
         * switch modes
         *
         * For modules using camera 2 api, they're required to call this
         * method when a capture is "completed".  Unfortunately this differs
         * per module implementation.
         */
        if (!mDisableAllUserInteractions) {
            if(mModeOptionsOverlay!=null) {
                mModeOptionsOverlay.setToggleClickable(true);
            }
        }
    }

    /**
     * Set the mode options toggle not clickable.
     */
    public void disableModeOptions() {
        mModeOptionsOverlay.setToggleClickable(false);
    }

    private Integer mToken;
    public void setDisableAllUserInteractions(boolean disable) {
        if (disable) {
            disableModeOptions();
            setShutterButtonEnabled(false);
            setSwipeEnabled(false);
            if (mToken == null) {
                mToken = mController.lockModuleSelection();
            }
        } else {
            enableModeOptions();
            setShutterButtonEnabled(true);
            setSwipeEnabled(true);
            if (mToken != null){
                mController.unlockModuleSelection(mToken);
                mToken = null;
            }
        }
        mDisableAllUserInteractions = disable;
    }

    /**
     * Get called when a mode is scrolled to from{@link com.android.camera.ui.ModeStripView}
     * @param id mode index of the selected mode
     */
    @Override
    public void onModeIdChanged(int id) {
        onModeSelected(id);
        Log.e(TAG, "end mode selecting");
        BoostUtil.getInstance().releaseCpuLock();
    }

    @Override
    public void onModeIdChanging() {
        BoostUtil.getInstance().acquireCpuLock();
        Log.v(TAG, "KPI start mode selecting"); //MODIFIED by xuan.zhou, 2016-04-07,BUG-1920473
        mController.onModeSelecting();
    }

    /**
     * Gets called when a mode is selected from {@link com.android.camera.ui.ModeListView}
     *
     * @param modeIndex mode index of the selected mode
     */
    @Override
    public void onModeSelected(int modeIndex) {
//        if(mController.getCurrentModuleIndex()==modeIndex){
//            return;
//        }
        //During continuous mode switching , current will be closed at first ,
        //then the target mode is totally arbitrary , the target mode could be very possible to be the same mode as previous
        updateMode(modeIndex);
    }

    private void updateMode(int modeIndex){
        Log.w(TAG, "updateMode create hideCover runnable");
        mHideCoverRunnable = new Runnable() {
            @Override
            public void run() {
                mModeTransitionView.hideImageCover();
            }
        };
        mShutterButton.setAlpha(ShutterButton.ALPHA_WHEN_ENABLED);
//        mModeCoverState = COVER_SHOWN;
        setModeCoverState(COVER_SHOWN);

        int lastIndex = mController.getCurrentModuleIndex();
        // Actual mode teardown / new mode initialization happens here
        mController.onModeSelected(modeIndex);
        int currentIndex = mController.getCurrentModuleIndex();

//        if(!AppController.ENABLE_BLUR_TRANS) {
//            hideModeCover();
//        }

        if(currentIndex==mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_pano)){
            mPreviewOverlay.setTouchEnabled(false);
        }else{
            mPreviewOverlay.setTouchEnabled(true);
        }
        filterBottomBarIcons(currentIndex, false);

        updateModeSpecificUIColors();
            /* MODIFIED-BEGIN by feifei.xu, 2016-11-03,BUG-3312848*/
        //Do not close pose fragment while select to photomodule or filtermodule.
        //in this case ,just hide posedisplaylayout
        if (needClosePoseFragment()
                && modeIndex != mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_filter)
                && modeIndex != mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_photo)) {
            closePoseFragment();
        }
        /* MODIFIED-END by feifei.xu,BUG-3312848*/
    }

    public void changeShutterButtonSavePanorama(boolean isInSave){
        mShutterButton.setActivated(isInSave);
        mShutterButton.setClickable(!isInSave);
    }

    public void changeBottomBarInCapturePanorama(){
        mBottomBar.animateHidePeek();
        setModeSwitchUIVisibility(false);
        mBottomBar.setShutterButtonIcon(R.drawable.ic_panorama_capture, false);
        mBottomBar.overrideBottomBarColor(Color.TRANSPARENT);
        // hideModeOptions();
        getTopMenus().setTopModeOptionVisibility(false);
        mBottomBar.animateHideSwitchButton();
        setSwipeEnabled(false);
        setModeStripViewVisibility(false);
    }

    public void restoreBottomBarFinishPanorama(){
        /*MODIFIED-BEGIN by xuan.zhou, 2016-04-07,BUG-1920473*/
        restoreBottomBarFinishPanorama(false);
    }

    public void restoreBottomBarFinishPanorama(boolean updatePeekThumb){
        if (!updatePeekThumb) {
            mBottomBar.animateShowPeek();
            mBottomBar.animateShowSwitchButton();
        }
        /*MODIFIED-END by xuan.zhou,BUG-1920473*/
        setModeSwitchUIVisibility(true);
        mBottomBar.overrideBottomBarColor(Color.TRANSPARENT);
        int panoIndex = mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_pano);
        int shutterIconId = CameraUtil.getCameraShutterIconId(panoIndex, mController.getAndroidContext());
        mBottomBar.setShutterButtonIcon(shutterIconId, false);
        // showModeOptions();
        getTopMenus().setTopModeOptionVisibility(true);
        setSwipeEnabled(true);
        mBottomBar.overrideBottomBarColor(null);
        setModeStripViewVisibility(true);
    }

    /*MODIFIED-BEGIN by xuan.zhou, 2016-04-07,BUG-1920473*/
    public void showPeek(boolean animate) {
        if (animate) {
            mBottomBar.animateShowPeek();
        } else {
            mBottomBar.showPeek();
        }
    }
    /*MODIFIED-END by xuan.zhou,BUG-1920473*/

    public void showSwitchButton(boolean animate) {
        if (animate) {
            mBottomBar.animateShowSwitchButton();
        } else {
            mBottomBar.showSwitchButton();
        }
    }

    private void filterBottomBarIcons(int currentIndex,boolean fullSize){
        if(currentIndex == mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_photo)) {
            if(mIsCaptureIntent){
                mBottomBar.animateHidePeek();
                mBottomBar.animateHideVideoShutter();
            }else {
                mBottomBar.showPeek();
//                mBottomBar.animateShowVideoShutter();
            }
            mBottomBar.animateHideSegementRemove();
            mBottomBar.animateHideRemix();
            mBottomBar.animateHideVideoCapture();
            mBottomBar.hideContactsIntentPeek();
        }else if(currentIndex==mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_micro_video)){
            if(!fullSize) {
                mBottomBar.animateHideSegementRemove();
                mBottomBar.animateHideRemix();
                mBottomBar.animateShowPeek();
                mBottomBar.animateHideVideoShutter();
                mBottomBar.animateHideVideoCapture();
            }
            mBottomBar.hideContactsIntentPeek();
        } else if (currentIndex == mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_contacts_intent)) {
            mBottomBar.animateHidePeek();
            mBottomBar.hideSwitchButton();
            mBottomBar.animateHideVideoShutter();
            mBottomBar.animateHideSegementRemove();
            mBottomBar.animateHideRemix();
            mBottomBar.animateHideVideoCapture();
            mBottomBar.hideSwitchButton();
            mBottomBar.showContactsIntentPeek();
            mBottomBar.hideShutterButton();
            mBottomBar.showContactsIntentShutterButton();
        }else if(currentIndex==mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_video) ||
                currentIndex==mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_video_capture)){
            // It's better to show video capture button when recording start.
            // if(mIsCaptureIntent){
            //     mBottomBar.animateHideVideoCapture();
            // }else {
            //    mBottomBar.animateShowVideoCapture();
            // }
//            mBottomBar.animateHidePeek();
//            mBottomBar.animateHideVideoShutter();
            if(mIsCaptureIntent){
                mBottomBar.hidePeek();
            }else {
                mBottomBar.showPeek();
            }
            mBottomBar.hideVideoShutter();
            mBottomBar.animateHideRemix();
            mBottomBar.animateHideSegementRemove();

            mBottomBar.hideContactsIntentPeek();
        }else{
            mBottomBar.animateHideVideoCapture();
            mBottomBar.animateShowPeek();
            mBottomBar.animateHideVideoShutter();
            mBottomBar.animateHideSegementRemove();
            mBottomBar.animateHideRemix();

            mBottomBar.hideContactsIntentPeek();
        }

        if(currentIndex!=mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_micro_video)){
            mMicroVideoProgressbar.setVisibility(View.GONE);
        }
    }

    private void  filterBottomBarIconsWithoutAnimation(int currentIndex){
        if(currentIndex==mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_photo)) {
            if(mIsCaptureIntent){
                mBottomBar.hidePeek();
                mBottomBar.hideVideoShutter();
            }else {
                mBottomBar.showPeek();
//                mBottomBar.showVideoShutter();
            }
            mBottomBar.hideSegementRemove();
            mBottomBar.hideRemix();
            mBottomBar.hideVideoCapture();
            mBottomBar.hideContactsIntentPeek();
        }else if(currentIndex==mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_micro_video)){
            mBottomBar.showPeek();
            mBottomBar.hideVideoShutter();
            mBottomBar.hideSegementRemove();
            mBottomBar.hideRemix();;
            mBottomBar.hideVideoCapture();
            mBottomBar.hideContactsIntentPeek();
        } else if (currentIndex==mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_contacts_intent)) {
            mBottomBar.animateHidePeek();
            mBottomBar.hideSwitchButton();
            mBottomBar.animateHideVideoShutter();
            mBottomBar.animateHideSegementRemove();
            mBottomBar.animateHideRemix();
            mBottomBar.animateHideVideoCapture();
            mBottomBar.showContactsIntentPeek();
            mBottomBar.hideShutterButton();
            mBottomBar.showContactsIntentShutterButton();
        } else if(currentIndex==mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_video) ||
                currentIndex==mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_video_capture)){
            if(mIsCaptureIntent){
                mBottomBar.hideVideoCapture();
                mBottomBar.hidePeek();
            }else {
                mBottomBar.showPeek();
            }
            mBottomBar.hideVideoShutter();
            mBottomBar.hideSegementRemove();
            mBottomBar.hideRemix();
            mBottomBar.hideContactsIntentPeek();
        }else{
            mBottomBar.showPeek();
            mBottomBar.hideVideoShutter();
            mBottomBar.hideSegementRemove();
            mBottomBar.hideRemix();
            mBottomBar.hideVideoCapture();
            mBottomBar.hideContactsIntentPeek();
        }

        if(currentIndex!=mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_micro_video)){
            mMicroVideoProgressbar.setVisibility(View.GONE);
        }
        if(currentIndex==mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_pano)){
            mPreviewOverlay.setTouchEnabled(false);
        }
    }

    public void showMicroVideoEditButtons(boolean withAnimation){
        if(withAnimation) {
            mBottomBar.animateHidePeek();
            mBottomBar.animateShowRemix();
            mBottomBar.animateShowSegmentRemove();
        }else{
            mBottomBar.hidePeek();
            mBottomBar.showRemix();
            mBottomBar.showSegmentRemove();
        }
    }

    public void hideMicroVideoEditButtons(boolean withAnimation){
        if(withAnimation) {
            mBottomBar.animateShowPeek();
            mBottomBar.animateHideRemix();
            mBottomBar.animateHideSegementRemove();
        }else{
            mBottomBar.showPeek();
            mBottomBar.hideRemix();
            mBottomBar.hideSegementRemove();
        }
    }


    public void animateHidePeek(){
        mBottomBar.animateHidePeek();
    }

    public void showVideoCaptureButton(boolean animate) {
        if (animate) {
            if (mBottomBar != null) {
                mBottomBar.animateShowVideoCapture();
            }
        } else {
            if (mCaptureButton != null) {
                mBottomBar.showVideoCapture();
            }
        }
    }

    public void hideVideoCaptureButton(boolean animate) {
        if (animate) {
            if (mBottomBar != null) {
                mBottomBar.animateHideVideoCapture();
            }
        } else {
            if (mCaptureButton != null) {
                mBottomBar.hideVideoCapture();
            }
        }
    }

    private void updateModeSpecificUIColors() {
        setBottomBarColorsForModeIndex(mController.getCurrentModuleIndex());
    }

    @Override
    public void onSettingsSelected() {
        mController.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_SHOULD_SHOW_SETTINGS_BUTTON_CLING, false);
        initCameraSettingFragment();
        mController.onSettingsSelected();
    }

    @Override
    public void onFlashClicked() {
        mController.onFlashClicked();
    }

    @Override
    public void onPoseClicked() {
        mPoseFragment = new PoseFragment();
        mController.getFragmentManager().beginTransaction().replace(R.id.pose_layout, mPoseFragment,POSE_TAG).commit();
        mPoseDetailLayout.setVisibility(View.VISIBLE);
        setIsPoseSelectorShowing(true); // MODIFIED by fei.hui, 2016-10-25,BUG-3167899
    }
    public void setIsPoseSelectorShowing(Boolean isPoseSelectorShowing) {
        mPoseSelectorShowing = isPoseSelectorShowing;
    }
    public boolean isPoseSelectorShowing(){
        return mPoseSelectorShowing;
    }

    /* MODIFIED-BEGIN by feifei.xu, 2016-11-03,BUG-3312848*/
    public void hidePoseFragment() {
        if(mPoseFragment != null && mPoseDetailLayout.getVisibility() == View.VISIBLE){
            mPoseDetailLayout.setVisibility(View.GONE);
        }
    }

    public void showPoseFragment() {
        if(mPoseFragment != null && mPoseDetailLayout.getVisibility() != View.VISIBLE){
            mPoseDetailLayout.setVisibility(View.VISIBLE);
        }
    }
    /* MODIFIED-END by feifei.xu,BUG-3312848*/

    /* MODIFIED-BEGIN by feifei.xu, 2016-11-02,BUG-3299499*/
    public void hidePoseBackView() {
        if (mPoseFragment != null) {
            mPoseFragment.hidePoseBackView();
        }
    }

    public void showPoseBackView() {
        if (mPoseFragment != null) {
            mPoseFragment.showPoseBackView();
        }
    }
    /* MODIFIED-END by feifei.xu,BUG-3299499*/

    @Override
    public void onBackClicked() {
        mController.onBackClicked();
    }

    @Override
    public void onFilterClicked() {

        /* MODIFIED-BEGIN by Sichao Hu, 2016-09-23,BUG-2989818*/
        if (mController == null || mController.isPaused()) {
            Log.e(TAG, "CameraActivity paused, don't start recording");
            return;
        }

        if (mModeStripView.isLocked()) {//Not allowed to change mode under this situation.
            Log.e(TAG, "mModeStripView.isLocked()");
            return;
        }
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-01,BUG-3271894*/
        resetZoomBar();
        lockZoom();
        /* MODIFIED-END by jianying.zhang,BUG-3271894*/
        Log.d(TAG,"mController.getCurrentModuleIndex() : " + mController.getCurrentModuleIndex());
        int filterModeId = mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_filter);
        int videoFilterModeId = mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_videofilter);
        if(mController.getCurrentModuleIndex() == filterModeId
                || mController.getCurrentModuleIndex() == videoFilterModeId){
            mController.onFilterClicked();
            return;
        }

        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
        int photoModeId = mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_photo);
        int videoModeId = mController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_video);
        if (mController.getCurrentModuleIndex() == photoModeId) {
            onFilterModuleSelected(filterModeId);
        } else if (mController.getCurrentModuleIndex() == videoModeId) {
            onFilterModuleSelected(videoFilterModeId);
        }
                /* MODIFIED-END by Sichao Hu,BUG-2989818*/
    }

    public void onFilterModuleSelected(final int modeIndex) {
        gLockFSM.forceBlocking();
        mController.onModeSelecting(true, new ModeTransitionView.OnTransAnimationListener() {
                @Override
                public void onAnimationDone() {
                    onModeSelected(modeIndex);
                    hideModeOptions();
                    getTopMenus().setTopModeOptionVisibility(false);
                }
            });
                /* MODIFIED-END by Sichao Hu,BUG-2989818*/
        }
        /* MODIFIED-END by jianying.zhang,BUG-3255060*/

    @Override
    public int getCurrentModeIndex() {
        return mController.getCurrentModuleIndex();
    }

    /********************** Capture animation **********************/
    /* TODO: This session is subject to UX changes. In addition to the generic
       flash animation and post capture animation, consider designating a parameter
       for specifying the type of animation, as well as an animation finished listener
       so that modules can have more knowledge of the status of the animation. */


    /**
     * Starts the pre-capture animation.
     *
     * @param shortFlash show shortest possible flash instead of regular long version.
     */
    public void startPreCaptureAnimation(boolean shortFlash) {
        mCaptureOverlay.startFlashAnimation(shortFlash);
    }

    /**
     * Cancels the pre-capture animation.
     */
    public void cancelPreCaptureAnimation() {
        mAnimationManager.cancelAnimations();
    }

    /**
     * Cancels the post-capture animation.
     */
    public void cancelPostCaptureAnimation() {
        mAnimationManager.cancelAnimations();
    }

    /***************************SurfaceTexture Api and Listener*********************************/

    /**
     * Return the shared surface texture.
     */
    public SurfaceTexture getSurfaceTexture() {
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
//        mTextureView.setSurfaceTexture(mSurface);
        return mSurface;
    }
    public Point getSurfaceTextureSize(){
        return mTextureViewHelper.getSurfaceSize();
    }
    /* MODIFIED-END by sichao.hu,BUG-2743263*/

    /**
     * Return the shared {@link android.graphics.SurfaceTexture}'s width.
     */
    public int getSurfaceWidth() {
        return mSurfaceWidth;
    }

    /**
     * Return the shared {@link android.graphics.SurfaceTexture}'s height.
     */
    public int getSurfaceHeight() {
        return mSurfaceHeight;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = surface;
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        Log.v(TAG, "KPI SurfaceTexture is available");
        if (mPreviewStatusListener != null) {
            mPreviewStatusListener.onSurfaceTextureAvailable(surface, width, height);
        }
        enableModeOptions();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mSurface = surface;
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        if (mPreviewStatusListener != null) {
            mPreviewStatusListener.onSurfaceTextureSizeChanged(surface, width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurface = null;
        Log.v(TAG, "SurfaceTexture is destroyed");
        if (mPreviewStatusListener != null) {
            return mPreviewStatusListener.onSurfaceTextureDestroyed(surface);
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.v(TAG, "KPI on surfaceTexture updated");
        mSurface = surface;
        if (mPreviewStatusListener != null) {
            mPreviewStatusListener.onSurfaceTextureUpdated(surface);
        }
        if (!PhotoModule.firstFrame) {
            Log.e(TAG, "zhanghong: get First Frame");
            PhotoModule.firstFrame = true;
            if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_ENABLE_OPTIMIZE_SWITCH, false) &&
                    mController.getCurrentModuleController() instanceof PhotoModule) {
                ((PhotoModule) mController.getCurrentModuleController()).updateFaceBeautyWhenFrameReady();
            }
        }
        if (mModeCoverState == COVER_WILL_HIDE_AT_NEXT_TEXTURE_UPDATE) {
            Log.v(TAG, "hiding cover via onSurfaceTextureUpdated");
            CameraPerformanceTracker.onEvent(CameraPerformanceTracker.FIRST_PREVIEW_FRAME);
            hideModeCover();
        }
    }

    /****************************Grid lines api ******************************/

    /**
     * Show a set of evenly spaced lines over the preview.  The number
     * of lines horizontally and vertically is determined by
     * {@link com.android.camera.ui.GridLines}.
     */
    public void showGridLines() {
        if (mGridLines != null) {
            mGridLines.setVisibility(View.VISIBLE);
        }
    }

    public void showContactsGridLines() {
        if (mContactsGridLines != null) {
            mContactsGridLines.setVisibility(View.VISIBLE);
        }
    }

    public void hideContactsGridLines() {
        if (mContactsGridLines != null) {
            mContactsGridLines.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Hide the set of evenly spaced grid lines overlaying the preview.
     */
    public void hideGridLines() {
        if (mGridLines != null) {
            mGridLines.setVisibility(View.INVISIBLE);
        }
    }

    public void showPoseButton(){
        mTopMenus.initializeButton(TopMenus.BUTTON_POSE,
                new Runnable() {
                    @Override
                    public void run() {
                        onPoseClicked();
                    }
                }
        );
    }

    public void hidePoseButton(){
        mTopMenus.hideButton(TopMenus.BUTTON_POSE);
    }

    public void showCompose() {
        if (mComposeImage != null) {
            mComposeImage.setVisibility(View.VISIBLE);
        }
    }

    public void hideCompose() {
        if (mComposeImage != null) {
            mComposeImage.setVisibility(View.INVISIBLE);
        }
    }
    /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
    /**
     * Return a callback which shows or hide the preview grid lines
     * depending on whether the grid lines setting is set on.
     */
    public ButtonManager.ButtonCallback getGridLinesCallback() {
        return new ButtonManager.ButtonCallback() {
            @Override
            public void onStateChanged(int state) {
                if (Keys.areGridLinesOn(mController.getSettingsManager())) {
                    showGridLines();
                } else {
                    hideGridLines();
                }
            }

        };
    }

    /***************************Mode options api *****************************/

    /**
     * Set the mode options visible.
     */
    public void showModeOptions() {
        /* Make mode options clickable. */
        enableModeOptions();
        mModeOptionsOverlay.setVisibility(View.VISIBLE);
        if (mOnModeOptionsVisibilityChangedListener != null) {
            mOnModeOptionsVisibilityChangedListener.onModeOptionsVisibilityChanged(View.VISIBLE);
        }
    }

    /**
     * Set the mode options invisible.  This is necessary for modes
     * that don't show a bottom bar for the capture UI.
     */
    public void hideModeOptions() {
        mModeOptionsOverlay.setVisibility(View.INVISIBLE);
        if (mOnModeOptionsVisibilityChangedListener != null) {
            mOnModeOptionsVisibilityChangedListener.onModeOptionsVisibilityChanged(View.INVISIBLE);
        }
    }

    /****************************Bottom bar api ******************************/

    /**
     * Sets up the bottom bar and mode options with the correct
     * shutter button and visibility based on the current module.
     */
    public void resetBottomControls(ModuleController module, int moduleIndex) {
        if (areBottomControlsUsed(module)) {
            setBottomBarShutterIcon(moduleIndex);
            mCaptureLayoutHelper.setShowBottomBar(true);
        } else {
            mCaptureLayoutHelper.setShowBottomBar(false);
        }
    }
/* MODIFIED-BEGIN by sichao.hu, 2016-03-22, BUG-1027573 */
//
//    public void initBootmControls(ModuleController module, int moduleIndex) {
//        if (areBottomControlsUsed(module)) {
//            mCaptureLayoutHelper.setShowBottomBar(true);
//        } else {
//            mCaptureLayoutHelper.setShowBottomBar(false);
//        }
//    }
/* MODIFIED-END by sichao.hu,BUG-1027573 */

    /**
     * Show or hide the mode options and bottom bar, based on
     * whether the current module is using the bottom bar.  Returns
     * whether the mode options and bottom bar are used.
     */
    private boolean areBottomControlsUsed(ModuleController module) {
        if (module.isUsingBottomBar()) {
            showBottomBar();
            showModeOptions();
            return true;
        } else {
            hideBottomBar();
            hideModeOptions();
            return false;
        }
    }

    /**
     * Set the bottom bar visible.
     */
    public void showBottomBar() {
        mBottomBar.setVisibility(View.VISIBLE);
    }

    /**
     * Set the bottom bar invisible.
     */
    public void hideBottomBar() {
        mBottomBar.setVisibility(View.INVISIBLE);
    }

    /**
     * Sets the color of the bottom bar.
     */
    public void setBottomBarColor(int colorId) {
        mBottomBar.setBackgroundColor(colorId);
    }

    /**
     * Sets the pressed color of the bottom bar for a camera mode index.
     */
    public void setBottomBarColorsForModeIndex(int index) {
        mBottomBar.setColorsForModeIndex(index);
    }

    /**
     * Sets the shutter button icon on the bottom bar, based on
     * the mode index.
     */
    public void setBottomBarShutterIcon(int modeIndex) {
        int shutterIconId = CameraUtil.getCameraShutterIconId(modeIndex,
            mController.getAndroidContext());
        if (mIsCaptureIntent) {
            // In image/video capture intent, the rotation animation looks a little abrupt.
            mBottomBar.setShutterButtonIcon(shutterIconId, false);
        } else {
            mBottomBar.setShutterButtonIcon(shutterIconId);
        }
    }

    public void animateBottomBarToVideoStop(int shutterIconId) {
        mBottomBar.animateToVideoStop(shutterIconId);
    }

    public void animateBottomBarToFullSize(int shutterIconId,BottomBar.BottomBarSizeListener listener) {
        int currentIndex = mController.getCurrentModuleIndex();
        filterBottomBarIcons(currentIndex,true);
        mBottomBar.animateToFullSize(shutterIconId, listener);
    }

    public void setVideoBottomBarVisible(boolean stopVideo) {
        if (stopVideo) {
            mBottomBar.animateShowPeek();
            mBottomBar.animateShowSwitchButton();
            if (!mIsCaptureIntent) {
                mBottomBar.animateHideVideoCapture();
                mBottomBar.animateHidePauseRecord();
            }
        } else {
            mBottomBar.animateHidePeek();
            mBottomBar.animateHideSwitchButton();
            if (!mIsCaptureIntent) {
                mBottomBar.animateShowVideoCapture();
                mBottomBar.animateShowPauseRecord();
            }
        }
    }

    public void setShutterButtonEnabled(final boolean enabled) {
        /* MODIFIED-BEGIN by sichao.hu, 2016-03-22, BUG-1027573 */
        setShutterButtonEnabled(enabled,true);
    }

    public void setShutterButtonEnabled(final boolean enabled, final boolean needChangeAlpha) {
        if (!mDisableAllUserInteractions) {
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                if(!needChangeAlpha){
                    mBottomBar.setShutterbuttonEnabledWithoutAppearenceChanged(enabled);
                }else {
                    mBottomBar.setShutterButtonEnabled(enabled);
                }
            } else {
                mBottomBar.post(new Runnable() {
                    @Override
                    public void run() {
                        if(!needChangeAlpha){
                            mBottomBar.setShutterbuttonEnabledWithoutAppearenceChanged(enabled);
                        }
                        else{
                            mBottomBar.setShutterButtonEnabled(enabled);
                        }
                        /* MODIFIED-END by sichao.hu,BUG-1027573 */
                    }
                });
            }
        }
    }

    public void setShutterButtonPress(final boolean press) {
        if (!mDisableAllUserInteractions) {
            mBottomBar.post(new Runnable() {
                @Override
                public void run() {
                    mBottomBar.setShutterButtonPress(press);
                }
            });
        }
    }
    public void setShutterButtonLongClickable(final boolean enabled) {
        if (!mDisableAllUserInteractions) {
            mBottomBar.post(new Runnable() {
                @Override
                public void run() {
                    mBottomBar.setShutterButtonLongClickable(enabled);
                }
            });
        }
    }
    public void setShutterButtonImportantToA11y(boolean important) {
        mBottomBar.setShutterButtonImportantToA11y(important);
    }

    public boolean isShutterButtonEnabled() {
        return mBottomBar.isShutterButtonEnabled();
    }

    public void setIndicatorBottomBarWrapperVisible(boolean visible) {
        mIndicatorBottomBarWrapper.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Set the visibility of the bottom bar.
     */
    // TODO: needed for when panorama is managed by the generic module ui.
    public void setBottomBarVisible(boolean visible) {
        mBottomBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }


    private final Log.Tag SHUTTER_TAG=new Log.Tag("ShutterListener");
    /**
     * Add a {@link #ShutterButton.OnShutterButtonListener} to the shutter button.
     */
    public void addShutterListener(ShutterButton.OnShutterButtonListener listener) {
        Log.w(SHUTTER_TAG, "add shutter listener" + listener, new Throwable());
        mShutterButton.addOnShutterButtonListener(listener);
    }

    /**
     * Remove a {@link #ShutterButton.OnShutterButtonListener} from the shutter button.
     */
    public void removeShutterListener(ShutterButton.OnShutterButtonListener listener) {
        Log.w(SHUTTER_TAG, "remove shutter listener" + listener, new Throwable());
        mShutterButton.removeOnShutterButtonListener(listener);
    }

    /**
     * Performs a transition to the capture layout of the bottom bar.
     */
    public void transitionToCapture() {
        ModuleController moduleController = mController.getCurrentModuleController();
        applyModuleSpecs(moduleController.getHardwareSpec(),
                moduleController.getBottomBarSpec());
        mBottomBar.transitionToCapture();
        mIntentReviewControls.hide();
        mBottomBar.setVisibility(View.VISIBLE);
        if (mCaptureLayoutHelper.shouldOverlayBottomBar()) {
            mBottomBar.setIsBackgroundTransparent(false);
        }
        if (!mIsCaptureIntent) {
            setModeSwitchUIVisibility(true);
        }
//        ((CameraActivity)mController).getWindow().getDecorView().setSystemUiVisibility(SHOW_NAVIGATION_VIEW);
        unLockZoom();
        setSwipeEnabled(true);
    }

    /**
     * Displays the Cancel button instead of the capture button.
     */
    public void transitionToCancel() {
        ModuleController moduleController = mController.getCurrentModuleController();
        applyModuleSpecs(moduleController.getHardwareSpec(),
                moduleController.getBottomBarSpec());
        mBottomBar.transitionToCancel();
        mIntentReviewControls.hide();
        mBottomBar.setVisibility(View.VISIBLE);
        if (mCaptureLayoutHelper.shouldOverlayBottomBar()) {
            mBottomBar.setIsBackgroundTransparent(true);
        }
        hideZoomBar();
        setModeSwitchUIVisibility(false);
        //shouldn't hide navigation view in count down module
//        ((CameraActivity)mController).getWindow().getDecorView().setSystemUiVisibility(HIDE_NAVIGATION_VIEW);
        lockZoom();
        setSwipeEnabled(false);
    }

    /**
     * Performs a transition to the global intent layout.
     */
    public void transitionToIntentCaptureLayout() {
        ModuleController moduleController = mController.getCurrentModuleController();
        applyModuleSpecs(moduleController.getHardwareSpec(),
            moduleController.getBottomBarSpec());
        mBottomBar.transitionToIntentCaptureLayout();
        mIntentReviewControls.hide();
        mModeOptionsOverlay.setVisibility(View.VISIBLE);
        mBottomBar.setVisibility(View.VISIBLE);
    }

    /**
     * Performs a transition to the global intent review layout.
     */
    public void transitionToIntentReviewLayout() {
        ModuleController moduleController = mController.getCurrentModuleController();
        applyModuleSpecs(moduleController.getHardwareSpec(),
                moduleController.getBottomBarSpec());
        /* MODIFIED-BEGIN by fei.hui, 2016-10-17,BUG-3135406*/
        if(needClosePoseFragment()){
            closePoseFragment();
        }
        /* MODIFIED-END by fei.hui,BUG-3135406*/
        mBottomBar.transitionToIntentReviewLayout();
        mIntentReviewControls.show(moduleController.getBottomBarSpec().showCancel,
                moduleController.getBottomBarSpec().showDone,
                moduleController.getBottomBarSpec().showRetake,
                moduleController.getBottomBarSpec().showReview);
        mModeOptionsOverlay.setVisibility(View.GONE);
        mBottomBar.setVisibility(View.GONE);
    }

    /**
     * @return whether UI is in intent review mode
     */
    public boolean isInIntentReview() {
        return mBottomBar.isInIntentReview();
    }

    @Override
    public void onSettingChanged(SettingsManager settingsManager, String key) {
        // Update the mode options based on the hardware spec,
        // when hdr changes to prevent flash from getting out of sync.
        if (key.equals(Keys.KEY_CAMERA_HDR)) {
            ModuleController moduleController = mController.getCurrentModuleController();
            applyModuleSpecs(moduleController.getHardwareSpec(),
                             moduleController.getBottomBarSpec());
        }
    }

    /**
     * Applies a {@link com.android.camera.CameraAppUI.BottomBarUISpec}
     * to the bottom bar mode options based on limitations from a
     * {@link com.android.camera.hardware.HardwareSpec}.
     *
     * Options not supported by the hardware are either hidden
     * or disabled, depending on the option.
     *
     * Otherwise, the option is fully enabled and clickable.
     */
    public void applyModuleSpecs(final HardwareSpec hardwareSpec,
           final BottomBarUISpec bottomBarSpec) {
        if (hardwareSpec == null || bottomBarSpec == null) {
            return;
        }

        Log.v(TAG,"Timon apply bottomSpec");
        ButtonManager buttonManager = mController.getButtonManager();
        SettingsManager settingsManager = mController.getSettingsManager();

        buttonManager.setToInitialState();

        // No setting icon when capture intent
        if (!mIsCaptureIntent) {
            buttonManager.initializeSettingButton(new Runnable() {
                @Override
                public void run() {
                    onSettingsSelected();
                }
            });

            // For Fyuse mode only.
            if (bottomBarSpec.hideSetting) {
                buttonManager.hideSettings();
            } else {
                buttonManager.showSettings();
            }
        }

        if (!mIsCaptureIntent) {
            mTopMenus.initializeButton(TopMenus.BUTTON_SETTING,
                    new Runnable() {
                        @Override
                        public void run() {
                            onSettingsSelected();
                        }
                    });

            // For Fyuse mode only.
            if (bottomBarSpec.hideSetting) {
                mTopMenus.hideButton(TopMenus.BUTTON_SETTING);
            } else {
                mTopMenus.showButton(TopMenus.BUTTON_SETTING);
            }
        }
        if (bottomBarSpec.showPose) {
            showPoseButton();
        }else {
            hidePoseButton();
        }

        /* MODIFIED-BEGIN by feifei.xu, 2016-10-26,BUG-3136375*/
        if (!mIsCaptureIntent) {
            if (bottomBarSpec.showFilter) {
                mTopMenus.initializeButton(TopMenus.BUTTON_FILTER,
                        new Runnable() {
                            @Override
                            public void run() {
                                onFilterClicked();
                            }
                        });
            } else {
                mTopMenus.hideButton(TopMenus.BUTTON_FILTER);
            }
            /* MODIFIED-END by feifei.xu,BUG-3136375*/
        }

        /** Standard mode options */
        if (!bottomBarSpec.hideCamera && mController.getCameraProvider().getNumberOfCameras() > 1 &&
                hardwareSpec.isFrontCameraSupported()) {
            if (bottomBarSpec.enableCamera) {
                buttonManager.initializeButton(ButtonManager.BUTTON_CAMERA,
                        bottomBarSpec.cameraCallback);
                initializeButton(mSwitchButton, BUTTON_CAMERA,
                        bottomBarSpec.switchButtonCallback);
            } else {
                buttonManager.disableButton(ButtonManager.BUTTON_CAMERA);
                setSwitchBtnEnabled(false);
            }
        } else {
            // Hide camera icon if front camera not available.
            if (mController.getCameraProvider().getNumberOfCameras() > 1 &&
                    hardwareSpec.isFrontCameraSupported() &&
                        !bottomBarSpec.hideCameraForced&&!bottomBarSpec.setCameraInvisible) {
                buttonManager.initializeButton(ButtonManager.BUTTON_SWITCH,
                        null);
                initializeButton(mSwitchButton, BUTTON_SWITCH, null);
            }else if(bottomBarSpec.setCameraInvisible) {
                Log.v(TAG,"set camera button invisible");
                buttonManager.setButtonInvisible(ButtonManager.BUTTON_CAMERA);
                mBottomBar.hideSwitchButton();
            } else {
                Log.v(TAG,"hide camera button");
                buttonManager.hideButton(ButtonManager.BUTTON_CAMERA);
                mBottomBar.hideSwitchButton();
            }
        }

        if (bottomBarSpec.hideContactsBack) {
            mTopMenus.initializeButton(TopMenus.BUTTON_CONTACTS_BACK,
                    new Runnable() {
                        @Override
                        public void run() {
                            onBackClicked();
                        }
                    }
            );
        } else {
            mTopMenus.hideButton(TopMenus.BUTTON_CONTACTS_BACK);
        }

        boolean flashBackCamera = mController.getSettingsManager().getBoolean(
            SettingsManager.SCOPE_GLOBAL, Keys.KEY_FLASH_SUPPORTED_BACK_CAMERA);
        if (bottomBarSpec.hideContactsFlash) {
            initializeFlashButton(TopMenus.BUTTON_CONTACTS_FLASH);
            mTopMenus.hideButton(TopMenus.BUTTON_FLASH);
        } else if (bottomBarSpec.hideFlash || !flashBackCamera) {
            // Hide both flash and torch button in flash disable logic
//            buttonManager.hideButton(ButtonManager.BUTTON_FLASH);
//            buttonManager.hideButton(ButtonManager.BUTTON_TORCH);
            mTopMenus.hideButton(TopMenus.BUTTON_FLASH);
            mTopMenus.hideButton(TopMenus.BUTTON_TORCH);
            mTopMenus.hideButton(TopMenus.BUTTON_CONTACTS_FLASH);
        } else {
            mTopMenus.hideButton(TopMenus.BUTTON_CONTACTS_FLASH);
            if (hardwareSpec.isFlashSupported()) {
                if (bottomBarSpec.enableFlash) {
//                    buttonManager.initializeButton(ButtonManager.BUTTON_FLASH,
//                        bottomBarSpec.flashCallback);
                    initializeFlashButton(TopMenus.BUTTON_FLASH);
                } else if (bottomBarSpec.enableTorchFlash) {
//                    buttonManager.initializeButton(ButtonManager.BUTTON_TORCH,
//                        bottomBarSpec.flashCallback);
                    initializeFlashButton(TopMenus.BUTTON_TORCH);
                } else if (bottomBarSpec.enableHdrPlusFlash) {
//                    buttonManager.initializeButton(ButtonManager.BUTTON_HDR_PLUS_FLASH,
//                        bottomBarSpec.flashCallback);
                    initializeFlashButton(TopMenus.BUTTON_HDR_PLUS_FLASH);
                } else {
//                    buttonManager.initializeButton(ButtonManager.BUTTON_FLASH, null);
                    // Hide both flash and torch button in flash disable logic
//                    buttonManager.disableButton(ButtonManager.BUTTON_FLASH);
//                    buttonManager.disableButton(ButtonManager.BUTTON_TORCH);
                    mTopMenus.initializeButton(TopMenus.BUTTON_FLASH, null);
                    // Hide both flash and torch button in flash disable logic
                    mTopMenus.disableButton(TopMenus.BUTTON_FLASH);
                    mTopMenus.disableButton(TopMenus.BUTTON_TORCH);
                }
            } else {
//                buttonManager.initializeButton(ButtonManager.BUTTON_FLASH, null);
                // Disable both flash and torch icon if not supported
                // by the chosen camera hardware.
//                buttonManager.disableButton(ButtonManager.BUTTON_FLASH);
//                buttonManager.disableButton(ButtonManager.BUTTON_TORCH);
                mTopMenus.initializeButton(TopMenus.BUTTON_FLASH, null);
                // Disable both flash and torch icon if not supported
                // by the chosen camera hardware.
                mTopMenus.disableButton(TopMenus.BUTTON_FLASH);
                mTopMenus.disableButton(TopMenus.BUTTON_TORCH);
            }
        }

        // HDR shows when capture intent
        // if (bottomBarSpec.hideHdr || mIsCaptureIntent) {
        if (bottomBarSpec.hideHdr) {
            // Force hide hdr or hdr plus icon.
            buttonManager.hideButton(ButtonManager.BUTTON_HDR_PLUS);
        } else {
            if (hardwareSpec.isHdrPlusSupported()) {
                if (bottomBarSpec.enableHdr && Keys.isCameraBackFacing(settingsManager,
                                                                       SettingsManager.SCOPE_GLOBAL)) {
                    buttonManager.initializeButton(ButtonManager.BUTTON_HDR_PLUS,
                            bottomBarSpec.hdrCallback);
                } else {
                    buttonManager.disableButton(ButtonManager.BUTTON_HDR_PLUS);
                }
            } else if (hardwareSpec.isHdrSupported()) {
                if (bottomBarSpec.enableHdr && Keys.isCameraBackFacing(settingsManager,
                                                                       SettingsManager.SCOPE_GLOBAL)) {
                    buttonManager.initializeButton(ButtonManager.BUTTON_HDR,
                            bottomBarSpec.hdrCallback);
                } else {
                    buttonManager.disableButton(ButtonManager.BUTTON_HDR);
                }
            } else {
                // Hide hdr plus or hdr icon if neither are supported.
                buttonManager.hideButton(ButtonManager.BUTTON_HDR_PLUS);
            }
        }
        if (bottomBarSpec.hideLowlight) {
            // Force hide hdr or hdr plus icon.
            buttonManager.hideButton(ButtonManager.BUTTON_LOWLIGHT);
        } else {
            buttonManager.initializeButton(ButtonManager.BUTTON_LOWLIGHT,
                    bottomBarSpec.lowlightCallback);
        }

        if (bottomBarSpec.hideGridLines) {
            // Force hide grid lines icon.
            buttonManager.hideButton(ButtonManager.BUTTON_GRID_LINES);
            String moduleScope = mController.getModuleScope();
            if (Keys.areGridLinesOn(mController.getSettingsManager()) &&
                    (moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID) ||
                            moduleScope.endsWith(ManualModule.MANUAL_MODULE_STRING_ID) ||
                                    moduleScope.endsWith(NormalVideoModule.NORMAL_VIDEO_MODULE_STRING_ID))) {
                showGridLines();
            } else {
                hideGridLines();
            }
        } else {
            if (bottomBarSpec.enableGridLines) {
                buttonManager.initializeButton(ButtonManager.BUTTON_GRID_LINES,
                        bottomBarSpec.gridLinesCallback != null ?
                                bottomBarSpec.gridLinesCallback : getGridLinesCallback()
                );
            } else {
                buttonManager.disableButton(ButtonManager.BUTTON_GRID_LINES);
                if (Keys.areGridLinesOn(mController.getSettingsManager())) {
                    showGridLines();
                } else {
                    hideGridLines();
                }
            }
        }

        if (bottomBarSpec.enableSelfTimer) {
            buttonManager.initializeButton(ButtonManager.BUTTON_COUNTDOWN, null);
        } else {
            if (bottomBarSpec.showSelfTimer) {
                buttonManager.disableButton(ButtonManager.BUTTON_COUNTDOWN);
            } else {
                buttonManager.hideButton(ButtonManager.BUTTON_COUNTDOWN);
            }
        }

        if (bottomBarSpec.showTimeIndicator) {
            mTopMenus.showIndicator(TopMenus.TIME_INDICATOR);
        }else {
            mTopMenus.dismissIndicator();
        }

        if (bottomBarSpec.enablePanoOrientation
                && PhotoSphereHelper.getPanoramaOrientationOptionArrayId() > 0) {
            buttonManager.initializePanoOrientationButtons(bottomBarSpec.panoOrientationCallback);
        }

        boolean enableExposureCompensation = bottomBarSpec.enableExposureCompensation &&
            !(bottomBarSpec.minExposureCompensation == 0 && bottomBarSpec.maxExposureCompensation == 0) &&
            mController.getSettingsManager().getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_EXPOSURE_COMPENSATION_ENABLED);
        if (enableExposureCompensation) {
            buttonManager.initializePushButton(ButtonManager.BUTTON_EXPOSURE_COMPENSATION, null);
            buttonManager.setExposureCompensationParameters(
                bottomBarSpec.minExposureCompensation,
                bottomBarSpec.maxExposureCompensation,
                bottomBarSpec.exposureCompensationStep);

            buttonManager.setExposureCompensationCallback(
                    bottomBarSpec.exposureCompensationSetCallback);
            buttonManager.updateExposureButtons();
        } else {
            buttonManager.hideButton(ButtonManager.BUTTON_EXPOSURE_COMPENSATION);
            buttonManager.setExposureCompensationCallback(null);
        }

        /** Intent UI */
        if (bottomBarSpec.showCancel) {
            buttonManager.initializePushButton(ButtonManager.BUTTON_CANCEL,
                    bottomBarSpec.cancelCallback);
        }
        if (bottomBarSpec.showDone) {
            buttonManager.initializePushButton(ButtonManager.BUTTON_DONE,
                    bottomBarSpec.doneCallback);
        }
        if (bottomBarSpec.showRetake) {
            buttonManager.initializePushButton(ButtonManager.BUTTON_RETAKE,
                    bottomBarSpec.retakeCallback);
        }
        if (bottomBarSpec.showReview) {
            buttonManager.initializePushButton(ButtonManager.BUTTON_REVIEW,
                    bottomBarSpec.reviewCallback,
                    R.drawable.ic_play);
        }
        if (bottomBarSpec.showWrapperButton) {
            buttonManager.initializePushButton(ButtonManager.BUTTON_WRAPPER, null);
        } else {
            buttonManager.hideButton(ButtonManager.BUTTON_WRAPPER);
        }
    }

    public void  initializeButton(final RotatableButton button,final int buttonId,
                                  final BottomBar.SwitchButtonCallback cb){
        if (button == null) {
            return;
        }
        if (button.getVisibility() != View.VISIBLE) {
            button.setVisibility(View.VISIBLE);
        }
        button.setEnabled(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cameraId = mController.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_ID);
                cameraId = cameraId == 0 ? 1 : 0;
                mController.getSettingsManager().setValueByIndex(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_ID, cameraId);
                button.setEnabled(false);
                switch (buttonId) {
                    case BUTTON_CAMERA:
                        // This is a quick fix for ISE in Gcam module which can be
                        // found by rapid pressing camera switch button. The assumption
                        // here is that each time this button is clicked, the listener
                        // will do something and then enable this button again.
                        if (cb != null) {
                            cb.onToggleStateChanged(cameraId);
                            if (mTopMenus.buttonGroupBarVisible()) {
                                dismissButtonGroupBar(false, ButtonGroup.OUT_LEFT);
                            }
                        }
                        break;
                    case BUTTON_SWITCH:
                        mController.getLockEventListener().onSwitching();
                        mController.switchToMode(mController.getAndroidContext()
                                .getResources().getInteger(R.integer.camera_mode_photo), false);
                        break;
                }
            }
        });
        mSwitchButton.setOnTouchListener(new RotatableButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mController.getLockEventListener().onMenuClicked(mSwitchButton.hashCode());
                Log.d(TAG,"OnTouchListener mSwitchButton onTouchDown");
            }

            @Override
            public void onTouchUp() {
                mController.getLockEventListener().onIdle();
                Log.d(TAG,"OnTouchListener mSwitchButton onTouchUp");
            }
        });
    }

    public void setSwitchBtnEnabled(boolean enabled) {
        if (mBottomBar == null) {
            return;
        }
        mBottomBar.setSwitchBtnEnabled(enabled);
    }
    public TopMenus getTopMenus() {
        return mTopMenus;
    }

    public void setFiltersButtonActivated(int typeView, int index) {
        if (mTopMenus == null) {
            return;
        }
        mTopMenus.setButtonImageResource(typeView, index);
    }
    private void initializeFlashButton(int buttonType){
        mTopMenus.initializeButton(buttonType, new Runnable() {
            @Override
            public void run() {
                onFlashClicked();
            }
        });
    }

    private void setLockState(LockState state){
        switch (state){
            case BLOCK_FROM_MENU:
                break;
            case BLOCKING:
                lockPool(null);
                break;
            case BLOCK_FROM_MODE_SWITCHING:
                lockPool(mModeStripView);
                break;
            case IDLE:
                unlockPool();
                break;
            case BLOCK_FROM_SHUTTER:
                lockPool(mShutterButton);
                break;
        }

    }

    public interface LockEventListener{
        public void onShutter();
        public void onIdle();
        public void onSwitching();
        public void onMenuClicked(int hash);
        public void forceBlocking();
        public void onModeSwitching();
    }

    public final LockEventListener gLockFSM =new LockEventListener() {
        @Override
        public void onIdle() {
            setLockState(LockState.IDLE);
        }

        @Override
        public void onShutter() {
            setLockState(LockState.BLOCK_FROM_SHUTTER);
        }

        @Override
        public void onSwitching() {
            setLockState(LockState.BLOCKING);
        }

        @Override
        public void onMenuClicked(int hash) {
            Log.w(TAG,"on Menu clicked");
            lockPool(hash);
            setLockState(LockState.BLOCK_FROM_MENU);
        }

        @Override
        public void forceBlocking() {
            setLockState(LockState.BLOCKING);
        }

        @Override
        public void onModeSwitching() {
            setLockState(LockState.BLOCK_FROM_MODE_SWITCHING);
        }
    };

    private Map<Integer,Lockable> mListeningLockable=new HashMap<>();

    public synchronized void addLockableToListenerPool(Lockable lockable) {
        mListeningLockable.put(lockable.hashCode(),lockable);
    }

    public synchronized void removeLockableFromListenerPool(Lockable lockable) {
        mListeningLockable.remove(lockable.hashCode());
    }

    /**
     * Lock all lockable in the pool
     * @param inUseLockable is the lockable in use , this lockable would not be locked ,
     *                      could be null ,then all lockable in the pool would be locked
     */
    private synchronized void lockPool(Lockable inUseLockable) {
        Log.v(TAG, "call lock pool with lockable ", new Throwable());
        for(Lockable lockable:mListeningLockable.values()){
            if(lockable!=inUseLockable){
                lockable.lockSelf();
            }
        }
    }

    private synchronized void lockPool(int hash) {
        Log.v(TAG, "call lock pool with hash ", new Throwable());
        for(Lockable lockable:mListeningLockable.values()){
            if(lockable.hashCode()!=hash){
                lockable.lockSelf();
            }
        }
    }

    private synchronized void unlockPool() {
        Log.v(TAG, "call unlock pool ", new Throwable());
        for(Lockable lockable:mListeningLockable.values()){
            lockable.unLockSelf();
        }
    }

    /**
     * Shows the given tutorial on the screen.
     */
    public void showTutorial(AbstractTutorialOverlay tutorial, LayoutInflater inflater) {
        tutorial.show(mTutorialsPlaceHolderWrapper, inflater);
    }

    /***************************Filmstrip api *****************************/

    private OnModeOptionsVisibilityChangedListener mOnModeOptionsVisibilityChangedListener;
    public void addManualModeListener(ManualGroup manualGroup) {
//        mPreviewOverlay.addOnPreviewTouchedListener(manualGroup);
        addShutterListener(manualGroup);
        mOnModeOptionsVisibilityChangedListener = manualGroup;
    }

    public interface OnModeOptionsVisibilityChangedListener {
        /**
         * This gets called on modeoptions visibility change.
         */
        public void onModeOptionsVisibilityChanged(int vis);
    }

    public void removeManualModeListener(ManualGroup manualGroup) {
//        mPreviewOverlay.removeOnPreviewTouchedListener(manualGroup);
        removeShutterListener(manualGroup);
        mOnModeOptionsVisibilityChangedListener = null;
    }

    public int getNavigationHeight() {
        Resources resources = mController.getAndroidContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    public CaptureLayoutHelper getCaptureLayoutHelper() {
        return mCaptureLayoutHelper;
    }

    public void lockButtonOrientation() {
        mController.lockRotatableOrientation(mShutterButton.hashCode());
        mController.lockRotatableOrientation(mPauseRecord.hashCode()); // MODIFIED by jianying.zhang, 2016-11-11,BUG-3445767
        mController.lockRotatableOrientation(mCaptureButton.hashCode());
        mController.lockRotatableOrientation(mSegmentRemoveButton.hashCode());
        mController.lockRotatableOrientation(mRemixButton.hashCode());
    }

    public void unlockButtonOrientation() {
        mController.unlockRotatableOrientation(mShutterButton.hashCode());
        mController.unlockRotatableOrientation(mPauseRecord.hashCode()); // MODIFIED by jianying.zhang, 2016-11-11,BUG-3445767
        mController.unlockRotatableOrientation(mCaptureButton.hashCode());
        mController.unlockRotatableOrientation(mSegmentRemoveButton.hashCode());
        mController.unlockRotatableOrientation(mRemixButton.hashCode());
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-01,BUG-3271894*/
    public void resetZoomBar() {
        if (mZoomBar != null) {
            mZoomBar.resetZoomRatio();
        }
        if (mPreviewOverlay != null) {
            mPreviewOverlay.resetZoom();
        }
    }
    /* MODIFIED-END by jianying.zhang,BUG-3271894*/

    /* MODIFIED-BEGIN by xuan.zhou, 2016-05-23,BUG-2167404*/
    public void lockZoom() {
        if (mZoomBar != null) {
            mZoomBar.lockSelf();
        }
    }

    public void unLockZoom() {
        if (mZoomBar != null) {
            mZoomBar.unLockSelf();
        }
    }
    /* MODIFIED-END by xuan.zhou,BUG-2167404*/

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-27,BUG-3212745*/
    @Override
    public void onZoomBarVisibilityChanged(boolean visible) {
        mController.onZoomBarVisibilityChanged(visible);
    }

    public boolean hideZoomBar() {
        if (mZoomBar != null && mZoomBar.getVisibility() == View.VISIBLE) {
            mZoomBar.setVisibility(View.GONE);
            return true;
        }
        return false;
    }
    /* MODIFIED-END by jianying.zhang,BUG-3212745*/

    /* MODIFIED-BEGIN by xuan.zhou, 2016-06-17,BUG-2377722*/
    public void cancelShutterButtonClick() {
        if (mShutterButton != null) {
            mShutterButton.setPressed(false);
        }
    }
    /* MODIFIED-END by xuan.zhou,BUG-2377722*/

    // If it's in metering, obstruct the touch event so that
    // it won't do module switch when swipe right/left.
    public void setTouchObstruct(boolean metering) {
        mAppRootView.setTouchObstruct(metering);
    }

    public void initCameraSettingFragment() {
        if (mSettingFragment == null){
            mSettingFragment = new CameraSettingsFragment();
        }
        mSettingFragment.init(mController);
        mController.getFragmentManager().beginTransaction().replace(R.id.camera_settings_fragment, mSettingFragment).commit();
        setCameraSettingVisible(true);
        setTouchObstruct(true);
        mPreviewOverlay.setTouchEnabled(false);
    }

    public boolean isCameraSettingVisible() {
        if (mCameraSettingLayout != null
                && mCameraSettingLayout.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    public boolean dismissCameraSettingFragment() {
        if (mSettingFragment != null) {
            mController.getFragmentManager().beginTransaction().remove(mSettingFragment).commit();
            setCameraSettingVisible(false);
            setTouchObstruct(false);
            mPreviewOverlay.setTouchEnabled(true);
            mSettingFragment = null;
            return true;
        } else {
            return false;
        }
    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-11-03,BUG-3311864*/
    public boolean isTopMenusVisible() {
        return (mTopMenus != null && mTopMenus.getVisibility() == View.VISIBLE);
    }

    public boolean isBottomBarVisible() {
        return (mBottomBar != null && mBottomBar.getVisibility() == View.VISIBLE);
    }
    /* MODIFIED-END by xuan.zhou,BUG-3311864*/
}
