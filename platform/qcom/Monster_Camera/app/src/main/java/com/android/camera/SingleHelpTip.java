package com.android.camera;

import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.ui.ManualItem;
import com.android.camera.ui.PeekImageView;
import com.android.camera.ui.RotatableButton;
import com.android.camera.util.CameraUtil;
import com.android.camera.widget.FloatingActionsMenu;
import com.android.ex.camera2.portability.Size;
import com.tct.camera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sdduser on 16-1-30.
 */
public class SingleHelpTip extends HelpTip {

    private static final int VIDEO_SNAP_TIP_DELAY = 1500;
    private static final int QUICK_SWITCH_TIP_DELAY = 500;
    private static final int PANORAMA_TIP_DELAY = 1500;
    private static final int MANUAL_TIP_DELAY = 1500;
    private static final int ISO_ITEM = 0;
    private static final int S_ITEM = 1;
    private static final int WB_ITEM = 2;
    private static final int F_ITEM = 3;
    private static final int FLASH_TOGGLE = 0;
    private static final int HDR_TOGGLE = 1;
    private static final int COUNT_DOWN_TOGGLE = 2;
    private static final int LOW_LIGHT_TOGGLE = 3;
    private static final int SCENE_ANIM_FIRST_FRAME = 1;
    private static final int[] PinchZoomFrameList = new int[]{R.drawable.pinch_zoom_animation1, R.drawable.pinch_zoom_animation2, R.drawable.pinch_zoom_animation3,
            R.drawable.pinch_zoom_animation4, R.drawable.pinch_zoom_animation5, R.drawable.pinch_zoom_animation6,
            R.drawable.pinch_zoom_animation7, R.drawable.pinch_zoom_animation8, R.drawable.pinch_zoom_animation9,
            R.drawable.pinch_zoom_animation10, R.drawable.pinch_zoom_animation11, R.drawable.pinch_zoom_animation12,
            R.drawable.pinch_zoom_animation13, R.drawable.pinch_zoom_animation14, R.drawable.pinch_zoom_animation15,
            R.drawable.pinch_zoom_animation16, R.drawable.pinch_zoom_animation17, R.drawable.pinch_zoom_animation18,
            R.drawable.pinch_zoom_animation19, R.drawable.pinch_zoom_animation20, R.drawable.pinch_zoom_animation21,
            R.drawable.pinch_zoom_animation22, R.drawable.pinch_zoom_animation23, R.drawable.pinch_zoom_animation24,
            R.drawable.pinch_zoom_animation25, R.drawable.pinch_zoom_animation26, R.drawable.pinch_zoom_animation27,

    };
    private static final int[] ModeFrameList = new int[]{R.drawable.mode_animation1, R.drawable.mode_animation2, R.drawable.mode_animation3,
            R.drawable.mode_animation4, R.drawable.mode_animation5, R.drawable.mode_animation6,
            R.drawable.mode_animation7, R.drawable.mode_animation8, R.drawable.mode_animation9,
            R.drawable.mode_animation10, R.drawable.mode_animation11, R.drawable.mode_animation12,
            R.drawable.mode_animation13, R.drawable.mode_animation14, R.drawable.mode_animation15,
            R.drawable.mode_animation16, R.drawable.mode_animation17, R.drawable.mode_animation18,
            R.drawable.mode_animation19, R.drawable.mode_animation20, R.drawable.mode_animation21,
            R.drawable.mode_animation22, R.drawable.mode_animation23, R.drawable.mode_animation24,
            R.drawable.mode_animation25, R.drawable.mode_animation26, R.drawable.mode_animation27,
            R.drawable.mode_animation28, R.drawable.mode_animation29, R.drawable.mode_animation30,

    };
    private final int[] mPinchZoomDurations = new int[]{360, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 360, 40, 40, 40, 40, 40, 40, 80, 80, 600,
            440, 120, 120, 10};//Pinch Zoom frame durations
    private final int[] mModeDurations = new int[]{360, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 360, 40, 40, 40, 40, 40, 40, 80, 80, 320,
            120, 120, 120, 120, 120, 120, 10};//Mode frame durations
    private LinearLayout mItemISO;
    private LinearLayout mItemS;
    private LinearLayout mItemWb;
    private LinearLayout mItemF;
    private FloatingActionsMenu mManualMenu;//response for manual arrow menu event
    private PopupWindow mPopupWindow;
    private View mPreViewOverLay;//response for pinch zoom event
    private MultiToggleImageButton mCameraToggleBtn;
    private RotatableButton mCameraSettingsToggle;
//    private MultiToggleImageButton mCameraFlashToggle;
    private MultiToggleImageButton mCameraHDRToggle;
    private MultiToggleImageButton mCameraCountDownToggle;
    private MultiToggleImageButton mCameraLowLightToggle;
    private FrameLayout mPanoramaArrowFrameLayout;
    private SceneAnimation mSceneAnimation;
    private ShutterButton mShutterButton;
    private RotatableButton mCapturebButton;
    private PeekImageView mPeekThumb;

    /**
     * Create a single help tip overlay.
     *
     * @param groupId    tip group's id
     * @param groupId    tip's id
     * @param controller help tip controller
     * @param activity   cameraActvity
     */
    public SingleHelpTip(int groupId, int tipId, HelpTipController controller, CameraActivity activity) {
        super(tipId, controller, activity);
        mCurTipGroupId = groupId;
        adapterLayoutById(groupId);

        if (mSceneAnimation != null) {
            mSceneAnimation.stopAnimation();
            mSceneAnimation = null;
        }
    }

    /**
     * adapt layouts for a single help tip overlay.
     *
     * @param groupId tip group's id
     */
    private void adapterLayoutById(int groupId) {
        switch (groupId) {
            case HelpTipsManager.PINCH_ZOOM_GROUP:
                mLayoutResId = R.layout.pinch_zoom_tip;
                break;

            case HelpTipsManager.QUICK_SETTINGS_GROUP:
                mLayoutResId = R.layout.quick_menu_tip;
                break;
            case HelpTipsManager.SETTINGS_GROUP:
                mLayoutResId = R.layout.settings_menu_tip;
                break;

            case HelpTipsManager.SWITCH_FRONT_CAMERA_GROUP:
                mLayoutResId = R.layout.switch_front_tip;
                break;

            case HelpTipsManager.GESTURE_CONFIRM_GROUP:
                mLayoutResId = R.layout.front_gesture_tip;
                break;

            case HelpTipsManager.MODE_GROUP:
                mLayoutResId = R.layout.mode_tip;
                break;
            case HelpTipsManager.PANORAMA_GROUP:
                mLayoutResId = R.layout.panorama_tip;
                break;

            case HelpTipsManager.MANUAL_GROUP:
                boolean bFrontCameraFacing = (Keys.isCameraBackFacing(mActivity.getSettingsManager(), SettingsManager.SCOPE_GLOBAL));
                String pictureSizeKey = bFrontCameraFacing ? Keys.KEY_PICTURE_SIZE_FRONT
                        : Keys.KEY_PICTURE_SIZE_BACK;

                String defaultPicSize = SettingsUtil.getDefaultPictureSize(bFrontCameraFacing);
                String pictureSize = mActivity.getSettingsManager().getString(SettingsManager.SCOPE_GLOBAL,
                        pictureSizeKey, defaultPicSize);
                Size size = SettingsUtil.sizeFromString(pictureSize);
                if(size != null && (size.width() == size.height())){
                    Log.i(TAG, "Tony size is equal ratio = " + size.toString());
                    mLayoutResId = R.layout.manual_tip_preview;
                }else {
                    mLayoutResId = R.layout.manual_tip;
                }

                break;
            case HelpTipsManager.VIDEO_STOP_GROUP:
                mLayoutResId = R.layout.video_stop_tip;
                break;
            case HelpTipsManager.VIDEO_SNAP_GROUP:
                mLayoutResId = R.layout.video_snap_tip;
                break;
            case HelpTipsManager.RECENT_GROUP:
                mLayoutResId = R.layout.recent_tip;
                break;

        }
    }

    @Override
    protected void initWidgets() {

        boolean bNeedinitCommom = true;
        if (mCurTipGroupId == HelpTipsManager.PINCH_ZOOM_GROUP) {
            bNeedinitCommom = false;
        }

        if (bNeedinitCommom) initCommomWidget();

        switch (mCurTipGroupId) {
            //1 pano tip
            case HelpTipsManager.PANORAMA_GROUP:
                mDrawType = LINE;
                playAnimation();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
//                        mPanoramaArrowFrameLayout = (FrameLayout) mActivity.findViewById(R.id.PanoramaPreview_Frame);
//                        int[] location = new int[2];
//                        mPanoramaArrowFrameLayout.getLocationOnScreen(location);
//                        List<Rect> panoRectList = new ArrayList<Rect>();
//                        Rect panoRect = new Rect(location[0], location[1],
//                                location[0] + mPanoramaArrowFrameLayout.getWidth(), location[1] + mPanoramaArrowFrameLayout.getHeight());
//                        panoRectList.add(panoRect);
//                        if (mHelpTipCling != null)
//                            mHelpTipCling.setHitRect(panoRectList);
//                        if (mRootView != null) {
//                            mRootView.requestLayout();
//                            mRootView.invalidate();
//                        }
                    }
                };
                mHandler.postDelayed(runnable, PANORAMA_TIP_DELAY);
                break;
            //2 manual tip
            case HelpTipsManager.MANUAL_GROUP:
                mDrawType = RECTANGLE;
                Runnable manualRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mManualMenu = (FloatingActionsMenu) mActivity.findViewById(R.id.multiple_actions);
                        ManualItem itemISO = (ManualItem) mActivity.findViewById(R.id.item_iso);
                        mItemISO = (LinearLayout) (itemISO.findViewById(R.id.item_root));

                        ManualItem itemS = (ManualItem) mActivity.findViewById(R.id.item_s);
                        mItemS = (LinearLayout) (itemS.findViewById(R.id.item_root));

                        ManualItem itemWb = (ManualItem) mActivity.findViewById(R.id.item_wb);
                        mItemWb = (LinearLayout) (itemWb.findViewById(R.id.item_root));

                        ManualItem itemF = (ManualItem) mActivity.findViewById(R.id.item_f);
                        mItemF = (LinearLayout) (itemF.findViewById(R.id.item_root));

                        int[] location = new int[2];
                        List<Rect> manualRect = new ArrayList<Rect>();
                        mItemISO.getLocationOnScreen(location);
                        Rect recISO = new Rect(location[0], location[1], location[0] + mItemISO.getWidth(),
                                location[1] + mItemISO.getHeight());
                        manualRect.add(recISO);

                        mItemS.getLocationOnScreen(location);
                        Rect recfS = new Rect(location[0], location[1], location[0] + mItemS.getWidth(),
                                location[1] + mItemS.getHeight());
                        manualRect.add(recfS);

                        mItemWb.getLocationOnScreen(location);
                        Rect recfwb = new Rect(location[0], location[1], location[0] + mItemWb.getWidth(),
                                location[1] + mItemWb.getHeight());
                        manualRect.add(recfwb);

                        mItemF.getLocationOnScreen(location);
                        Rect recfF = new Rect(location[0], location[1], location[0] + mItemF.getWidth(),
                                location[1] + mItemF.getHeight());
                        manualRect.add(recfF);
                        if (mHelpTipCling != null)
                            mHelpTipCling.setHitRect(manualRect);
                        if (mRootView != null) {
                            mRootView.requestLayout();
                            mRootView.invalidate();
                            LinearLayout manualMenu = (LinearLayout) mRootView.findViewById(R.id.help_tip_manual_menu);
                            if (manualMenu != null)
                                manualMenu.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mManualMenu != null) mManualMenu.manualAddButtonClick();
                                    }
                                });
                        }
                    }
                };
                mHandler.postDelayed(manualRunnable, MANUAL_TIP_DELAY);
                break;
            //3 pinch zoom tip
            case HelpTipsManager.PINCH_ZOOM_GROUP:
                mDrawType = NO_DRAW;
                mPreViewOverLay = (View) mActivity.findViewById(R.id.preview_overlay);
                mHelpTipCling.setPreViewOverLay(mPreViewOverLay);
                startAnimation(PinchZoomFrameList, mPinchZoomDurations);
                popupTipDialog(mRingAnimationImageView);
                break;

            //4 quick settings tip
            case HelpTipsManager.QUICK_SETTINGS_GROUP:
                mDrawType = RECTANGLE;
                Runnable quickSettingRunnable = new Runnable() {
                    @Override
                    public void run() {
//                        mCameraFlashToggle = (MultiToggleImageButton) mActivity.findViewById(R.id.flash_toggle_button);
                        mCameraHDRToggle = (MultiToggleImageButton) mActivity.findViewById(R.id.hdr_plus_toggle_button);
                        mCameraCountDownToggle = (MultiToggleImageButton) mActivity.findViewById(R.id.countdown_toggle_button);
                        mCameraLowLightToggle = (MultiToggleImageButton) mActivity.findViewById(R.id.lowlight_toggle_button);

                        int[] location = new int[2];
                        List<Rect> quickSettingsRectF = new ArrayList<Rect>();
//                        mCameraFlashToggle.getLocationOnScreen(location);
//                        Rect recflash = new Rect(location[0], location[1], location[0] + mCameraFlashToggle.getWidth(),
//                                location[1] + mCameraFlashToggle.getHeight());
//                        quickSettingsRectF.add(recflash);

                        mCameraHDRToggle.getLocationOnScreen(location);
                        Rect recfHdr = new Rect(location[0], location[1], location[0] + mCameraHDRToggle.getWidth(),
                                location[1] + mCameraHDRToggle.getHeight());
                        quickSettingsRectF.add(recfHdr);

                        mCameraCountDownToggle.getLocationOnScreen(location);
                        Rect recfCountDown = new Rect(location[0], location[1], location[0] + mCameraCountDownToggle.getWidth(),
                                location[1] + mCameraCountDownToggle.getHeight());
                        quickSettingsRectF.add(recfCountDown);

                        mCameraLowLightToggle.getLocationOnScreen(location);
                        Rect recfLowLight = new Rect(location[0], location[1], location[0] + mCameraLowLightToggle.getWidth(),
                                location[1] + mCameraLowLightToggle.getHeight());
                        quickSettingsRectF.add(recfLowLight);
                        if (mHelpTipCling != null)
                            mHelpTipCling.setHitRect(quickSettingsRectF);
                        if (mRootView != null) {
                            mRootView.requestLayout();
                            mRootView.invalidate();
                        }

                    }
                };
                mHandler.postDelayed(quickSettingRunnable, QUICK_SWITCH_TIP_DELAY);
                break;

            //5 settings tip
            case HelpTipsManager.SETTINGS_GROUP:
                mCameraSettingsToggle = (RotatableButton) mActivity.findViewById(R.id.menu_setting_button);
                mDrawType = CIRCLE;
                playAnimation();
                break;

            //6 front camera tip
            case HelpTipsManager.SWITCH_FRONT_CAMERA_GROUP:
                mCameraToggleBtn = (MultiToggleImageButton) mActivity.findViewById(R.id.camera_toggle_button);
                mDrawType = CIRCLE;
                playAnimation();
                break;

            //7 gesture tip
            case HelpTipsManager.GESTURE_CONFIRM_GROUP:
                mDrawType = NO_DRAW;
                break;

            //8 mode tip
            case HelpTipsManager.MODE_GROUP:
                mDrawType = LINE;
                mRingAnimationImageView = (ImageView) mRootView.findViewById(R.id.anim_focus);
                startAnimation(ModeFrameList, mModeDurations);
                break;

            //9 video snap tip
            case HelpTipsManager.VIDEO_SNAP_GROUP:
                mDrawType = CIRCLE;
                playAnimation();
                Runnable videoSnapRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mCapturebButton = (RotatableButton) mActivity.findViewById(R.id.video_snap_button);
                        if (mRootView != null) {
                            mRootView.requestLayout();
                            mRootView.invalidate();
                        }
                    }
                };
                mHandler.postDelayed(videoSnapRunnable, VIDEO_SNAP_TIP_DELAY);
                break;
            //10 stop video tip
            case HelpTipsManager.VIDEO_STOP_GROUP:
                mShutterButton = (ShutterButton) mActivity.findViewById(R.id.shutter_button);
                mDrawType = CIRCLE;
                playAnimation();
                break;
            //10 recent tip
            case HelpTipsManager.RECENT_GROUP:
                mPeekThumb = (PeekImageView) mActivity.findViewById(R.id.peek_thumb);
                mDrawType = CIRCLE;
                playAnimation();
                break;

            default:
                break;
        }

        mHelpTipCling.setListener(this, mDrawType);
    }

    /**
     * popup Tip Dialog only for pinch zoom
     */
    private void popupTipDialog(View view) {
        int widght = mActivity.getResources().getInteger(R.integer.help_tip_pinch_zoom_dialog_width);
        int height = mActivity.getResources().getInteger(R.integer.help_tip_pinch_zoom_dialog_height);
        int offsetX = mActivity.getResources().getInteger(R.integer.help_tip_pinch_zoom_dialog_offset_x);
        int offsetY = mActivity.getResources().getInteger(R.integer.help_tip_pinch_zoom_dialog_offset_y);
        View tipView = mInflater.inflate(R.layout.help_tip_pinch_zoom_popwindow, null);
        mTipNextButton = (Button) tipView.findViewById(R.id.next);
        mTipNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow != null) {
                    mPopupWindow.dismiss();
                    mPopupWindow = null;
                }
                goToNextTip(true);
            }
        });

        mPopupWindow = new PopupWindow(tipView, CameraUtil.dpToPixel(widght),
                ViewGroup.LayoutParams.WRAP_CONTENT);

        int gravity = Gravity.BOTTOM | Gravity.CENTER;
        mPopupWindow.setContentView(tipView);
        mPopupWindow.setFocusable(false);
        mPopupWindow.setOutsideTouchable(false);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.showAtLocation(view, gravity, CameraUtil.dpToPixel(offsetX),
                CameraUtil.dpToPixel(offsetY));
    }

    @Override
    public void clickHitRectResponse(int index) {
        if (mCurTipGroupId == HelpTipsManager.QUICK_SETTINGS_GROUP) {
            switch (index) {
//                case FLASH_TOGGLE:
//                    mCameraFlashToggle.performClick();
//                    break;
                case HDR_TOGGLE:
                    mCameraHDRToggle.performClick();
                    break;
                case COUNT_DOWN_TOGGLE:
                    mCameraCountDownToggle.performClick();
                    break;
                case LOW_LIGHT_TOGGLE:
                    mCameraLowLightToggle.performClick();
                    break;
            }
        } else if (mCurTipGroupId == HelpTipsManager.MANUAL_GROUP) {
            switch (index) {
                case ISO_ITEM:
                    if (mItemISO != null)
                        mItemISO.performClick();
                    break;
                case S_ITEM:
                    if (mItemS != null)
                        mItemS.performClick();
                    break;
                case WB_ITEM:
                    if (mItemWb != null)
                        mItemWb.performClick();
                    break;
                case F_ITEM:
                    if (mItemF != null)
                        mItemF.performClick();
                    break;
            }
        } else if (mCurTipGroupId == HelpTipsManager.PANORAMA_GROUP) {
            if (mPanoramaArrowFrameLayout != null)
                mPanoramaArrowFrameLayout.performClick();
        }
    }

    @Override
    protected void goToNextTip(boolean dismiss) {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
        if (mCurTipGroupId == HelpTipsManager.MANUAL_GROUP) {
            if (mHelpTipController != null) {
                mHelpTipController.onUpdateUIChangedFromTutorial();
            }
        }
        updateCurHelpTipStep(mCurTipGroupId, true);
        closeAndFinishHelptip();
        mHelpTipController.checkAlarmTaskHelpTip();
    }

    @Override
    protected void updateCurHelpTipStep(int tipId, boolean isOver) {
        boolean bNeedUpdateAlarmTask = false;
        switch (mCurTipGroupId) {
            case HelpTipsManager.GESTURE_CONFIRM_GROUP:
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_GESTURE_FINISHED, isOver);
                bNeedUpdateAlarmTask = true;

                break;
            case HelpTipsManager.PANORAMA_GROUP:
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_PANO_FINISHED, isOver);
                break;

            case HelpTipsManager.MANUAL_GROUP:
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_MANUAL_FINISHED, isOver);
                break;
            case HelpTipsManager.PINCH_ZOOM_GROUP:
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_PINCH_ZOOM_FINISHED, isOver);
                if (isOver) {
                    bNeedUpdateAlarmTask = true;
                }
                break;
            case HelpTipsManager.QUICK_SETTINGS_GROUP:
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_QUICK_SETTINGS_FINISHED, isOver);
                if (isOver) {
                    bNeedUpdateAlarmTask = true;
                }
                break;
            case HelpTipsManager.SETTINGS_GROUP:
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_SETTINGS_FINISHED, isOver);
                if (isOver) {
                    bNeedUpdateAlarmTask = true;
                }
                break;
            case HelpTipsManager.SWITCH_FRONT_CAMERA_GROUP:
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_FRONT_CAMERA_FINISHED, isOver);
                bNeedUpdateAlarmTask = true;
                break;
            case HelpTipsManager.MODE_GROUP:
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_MODE_FINISHED, isOver);
                if (isOver) {
                    bNeedUpdateAlarmTask = true;
                }
                break;
            case HelpTipsManager.VIDEO_STOP_GROUP:
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_STOP_VIDEO_FINISHED, true);
                break;
            case HelpTipsManager.VIDEO_SNAP_GROUP:
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_VIDEO_SNAP_FINISHED, true);
                break;
            case HelpTipsManager.RECENT_GROUP:
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_RECENT_FINISHED, true);
                break;
            default:
                break;
        }

        if (bNeedUpdateAlarmTask) {
            mHelpTipController.removeAlarmTask(mCurTipGroupId);
        }

    }

    @Override
    protected void clickAnimFucus() {
        switch (mCurTipGroupId) {
            case HelpTipsManager.SWITCH_FRONT_CAMERA_GROUP:
                if (mCameraToggleBtn != null)
                    mCameraToggleBtn.performClick();
                break;
            case HelpTipsManager.SETTINGS_GROUP:
                if (mCameraSettingsToggle != null)
                    mCameraSettingsToggle.performClick();
                break;

            case HelpTipsManager.VIDEO_SNAP_GROUP:
                if (mCapturebButton != null) {
                    mCapturebButton.performClick();
                }
                break;
            case HelpTipsManager.VIDEO_STOP_GROUP:
                if (mShutterButton != null) {
                    mShutterButton.performClick();
                }
                updateCurHelpTipStep(mCurTipGroupId, true);
                closeAndFinishHelptip();
                break;
            case HelpTipsManager.RECENT_GROUP:
                if (mPeekThumb != null) {
                    mPeekThumb.performClick();
                }
                break;
            default:
                break;

        }
    }

    @Override
    protected void notifyModeChanged() {
        goToNextTip(false);
    }

    @Override
    protected void dismissHelpTip() {
        if (mCurTipGroupId == HelpTipsManager.VIDEO_STOP_GROUP) {
            updateCurHelpTipStep(mCurTipGroupId, true);
            closeAndFinishHelptip();
        } else {
            goToNextTip(true);
        }
    }

    @Override
    protected void cleanUpHelpTip() {
        //remove scence animation
        if (mSceneAnimation != null) {
            mSceneAnimation.stopAnimation();
            mSceneAnimation = null;
        }
        super.cleanUpHelpTip();
    }

    @Override
    public void doPause() {
        if (mCurTipGroupId != HelpTipsManager.SETTINGS_GROUP) {
            updateCurHelpTipStep(mCurTipGroupId, false);
        }

        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
        super.doPause();
    }

    private void startAnimation(int[] modeFrameList, int[] durations) {
        mRingAnimationImageView = (ImageView) mRootView.findViewById(R.id.anim_focus);
        mSceneAnimation = new SceneAnimation(mRingAnimationImageView, modeFrameList, durations);
        mSceneAnimation.playConstant(SCENE_ANIM_FIRST_FRAME);
    }

    /**
     * scence animation only for mode tip and pinch zoom tip
     * play a animation formed a lot of big pngs , it may be occors OOM.
     */
    private class SceneAnimation {

        private final int[] mFrameRess;
        private final int mLastFrameNo;
        private final int[] mDurationList;//frame durations
        private boolean bPlayEnd = false;
        private ImageView imageView;
        private int mFrameNumber = 0;
        final Runnable SceneRunnable = new Runnable() {
            public void run() {
                try {
                    if (imageView != null) {
                        imageView.setBackgroundResource(mFrameRess[mFrameNumber]);
                    }

                    if (mFrameNumber == mLastFrameNo) {
                        playConstant(0);
                    } else {
                        playConstant(mFrameNumber + 1);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "SceneRunnable OOM out of memory mCurTipId = " + mCurTipId);
                    if (mHelpTipCling != null) {
                        cleanUpHelpTip();
                    }
                }

            }
        };

        public SceneAnimation(ImageView pImageView, int[] pFrameRess, int[] durations) {
            imageView = pImageView;
            mFrameRess = pFrameRess;
            mLastFrameNo = pFrameRess.length - 1;
            imageView.setBackgroundResource(mFrameRess[0]);
            mDurationList = durations;
            bPlayEnd = false;
        }

        public void playConstant(final int pFrameNo) {
            mFrameNumber = pFrameNo;
            if (!bPlayEnd && imageView != null) {
                imageView.postDelayed(SceneRunnable, mDurationList[mFrameNumber]);
            }
        }

        public void stopAnimation() {
            Log.i(TAG, "stopAnimation E mCurTipGroupId = " + mCurTipGroupId + "mCurTipId = " + mCurTipId);
            bPlayEnd = true;
            imageView.setBackground(null);
            imageView.clearAnimation();
            imageView.removeCallbacks(SceneRunnable);
        }
    }
}
