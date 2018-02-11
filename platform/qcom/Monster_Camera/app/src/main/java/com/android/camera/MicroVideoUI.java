package com.android.camera;

import android.view.View;
import android.widget.TextView;

import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.MicroVideoProgressBar;
import com.android.camera.ui.ModuleLayoutWrapper;
import com.android.camera.ui.PreviewOverlay;
import com.android.camera.ui.StereoModeStripView;
import com.android.camera.widget.MicroVideoGuideLayout;
import com.tct.camera.R;

/**
 * Created by sichao.hu on 10/12/15.
 */
public class MicroVideoUI extends VideoUI {

    private final Log.Tag TAG =new Log.Tag("MicroVideoUI");
    private View mSegmentRemoveButton;
    private View mRemixButton;
    private TextView mShutterTip;
    private TextView mMintimeTip;
    private MicroVideoProgressBar mMicroVideoProgressbar;
    private MicroVideoController mController;
    private static final float PROGRESS_UPPER_BOUND=15000;
    private static final float PROGRESS_LOWER_BOUND=3000;
    private static final int MIN_CAMERA_LAUNCHING_TIMES = 3;
    private final CameraActivity mActivity;

    // micro video guide;
    private PreviewOverlay mPreviewOverlay;
    private StereoModeStripView mModeStripView;
    private MicroVideoGuideLayout mGuideLayout;

    public MicroVideoUI(CameraActivity activity, VideoController controller, View parent) {
        super(activity, controller, parent);
        mActivity = activity;
        mController=(MicroVideoController)controller;
        mMicroVideoProgressbar=(MicroVideoProgressBar)mRootView.findViewById(R.id.micro_video_progressbar);
        mSegmentRemoveButton=mRootView.findViewById(R.id.button_segement_remove);
        mSegmentRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.onSegmentRemoveClicked();
            }
        });
        mRemixButton=mRootView.findViewById(R.id.button_remix);
        mRemixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.onRemixClicked();
            }
        });
        mMicroVideoProgressbar.setProgressUpperBound(PROGRESS_UPPER_BOUND);
        mMicroVideoProgressbar.setProgressLowerBound(PROGRESS_LOWER_BOUND);

        mPreviewOverlay = (PreviewOverlay) mRootView.findViewById(R.id.preview_overlay);
        mModeStripView = (StereoModeStripView) mRootView.findViewById(R.id.mode_strip_view);
        // whether show micro video guide or not
        if (Keys.isShowMicroGuide(mActivity.getSettingsManager()) && Keys.isNewLaunchingForMicroguide(mActivity.getSettingsManager())) {
            ModuleLayoutWrapper moduleRoot = (ModuleLayoutWrapper) mRootView.findViewById(R.id.module_layout);
            mActivity.getLayoutInflater().inflate(R.layout.microvideo_guide_layout,moduleRoot, true);
            mGuideLayout = (MicroVideoGuideLayout) mRootView.findViewById(R.id.micro_video_guide_layout);
            mGuideLayout.changeVisibility(View.VISIBLE);
            mGuideLayout.setGuideSelectionListener(new MicroVideoGuideLayout.GuideSelectionListener() {
                @Override
                public void onGuideSelected(boolean show) {
                    if (!show) {
                        Keys.setMicroGuide(mActivity.getSettingsManager(), show);
                    }
                    mGuideLayout.changeVisibility(View.GONE);
                    enableMicroIcons();
                    initializeShutterTip();
                    Keys.setNewLaunchingForMicroguide(mActivity.getSettingsManager(), false);
                }
            });
        }

        mMicroVideoProgressbar.setProgressUpperBound(PROGRESS_UPPER_BOUND);
        mMintimeTip = (TextView)mRootView.findViewById(R.id.micro_minimum_time_tip);
        mShutterTip = (TextView) mRootView.findViewById(R.id.micro_shoot_help_tip);

        initializeShutterTip();
    }

    private void initializeShutterTip() {
        if (isMircoGuideShow() || getSumDuration() > 0) { //MODIFIED by wenhua.tu, 2016-04-09,BUG-1911880
            return;
        }
        int launchingTimes = mActivity.getSettingsManager()
                .getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_NEW_LAUNCHING_TIMES_FOR_MICROTIP);

        boolean isMicroTipShowWhenPreview = launchingTimes < MIN_CAMERA_LAUNCHING_TIMES ||
                (launchingTimes == MIN_CAMERA_LAUNCHING_TIMES && !Keys.isNewLaunchingForMicrotip(mActivity.getSettingsManager()));

        if (isMicroTipShowWhenPreview) {
            if (Keys.isNewLaunchingForMicrotip(mActivity.getSettingsManager())) {
                // add launch times using micro
                mActivity.getSettingsManager().setValueByIndex(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_NEW_LAUNCHING_TIMES_FOR_MICROTIP, launchingTimes + 1);
                Keys.setNewLaunchingForMicrotip(mActivity.getSettingsManager(), false);
            }
            showShutterTip();
        }
    }

    public void disableMicroVideoButton(){
        mSegmentRemoveButton.setEnabled(false);
        mRemixButton.setEnabled(false);
    }

    public void disableRemixButton(){
        mRemixButton.setEnabled(false);
    }

    public void enableMicroVideoButton(){
        mSegmentRemoveButton.setEnabled(true);
        mRemixButton.setEnabled(true);
    }

    public void updateMicroVideoProgress(float progress){
        mMicroVideoProgressbar.updateProgress(progress);
        if (progress > 0) {
            mActivity.getCameraAppUI().setModeSwitchUIVisibility(false);
            mMicroVideoProgressbar.setVisibility(View.VISIBLE);
        }
    }

    public void markSegment(float duration){
        Log.w(TAG,"markSegment, duration is "+duration);
        mMicroVideoProgressbar.markSegmentStart(duration);
        /*MODIFIED-BEGIN by wenhua.tu, 2016-04-09,BUG-1911880*/
        if (mMicroVideoProgressbar.getVisibility() == View.GONE) {
            mMicroVideoProgressbar.setVisibility(View.VISIBLE);
            mActivity.getCameraAppUI().setModeSwitchUIVisibility(false);
        }
        /*MODIFIED-END by wenhua.tu,BUG-1911880*/
    }

    public float getSumDuration(){
        return mMicroVideoProgressbar.getSumDuration();
    }

    public int segmentRemoveOnProgress(){
        Log.w(TAG, "segmentRemoveOnProgress");
        // return (int)mMicroVideoProgressbar.segmentRemove();
        int removedProgress = (int)mMicroVideoProgressbar.segmentRemove();
        float sumDuration = getSumDuration();
        if (sumDuration == 0) {
            mActivity.getCameraAppUI().setModeSwitchUIVisibility(true);
            mMicroVideoProgressbar.setVisibility(View.GONE);
        }
        return removedProgress;
    }

    public void changeLastSegmentColor() {
        Log.w(TAG, "changeLastSegmentColor");
        mMicroVideoProgressbar.changeLastSegmentColor();
    }

    public void clearPendingProgress(){
        mMicroVideoProgressbar.clearPendingProgress();
    }

    public void resetProgress(){
        Log.w(TAG, "resetProgress");
        mMicroVideoProgressbar.clearProgress();
        mMicroVideoProgressbar.setVisibility(View.GONE);
        mActivity.getCameraAppUI().setModeSwitchUIVisibility(true);
    }

    public boolean isMircoGuideShow() {
        return mGuideLayout != null && mGuideLayout.getVisibility() == View.VISIBLE;
    }
    public void disableMicroIcons() {
        if (isMircoGuideShow()) {
            mActivity.getLockEventListener().onModeSwitching();
            mPreviewOverlay.setTouchEnabled(false);
        }
    }

    public void enableMicroIcons() {
        mActivity.getLockEventListener().onIdle();
        mPreviewOverlay.setTouchEnabled(true);
    }

    @Override
    public void showRecordingUI(boolean recording) {
        super.showRecordingUI(false);
    }

    public void showMintimeTip() {
        if (mMintimeTip != null) {
            mMintimeTip.setVisibility(View.VISIBLE);
        }
        hideShutterTip();
    }

    public void hideMintimeTip() {
        if (mMintimeTip != null) {
            mMintimeTip.setVisibility(View.GONE);
        }
    }

    public void showShutterTip() {
        if (mShutterTip != null) {
            mShutterTip.setVisibility(View.VISIBLE);
        }
        hideMintimeTip();
    }

    public void hideShutterTip() {
        if (mShutterTip != null) {
            mShutterTip.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isMircoGuideShow()) {
            mGuideLayout.changeVisibility(View.GONE);
        }
    }

    private void playMicroGuide() {
        if (isMircoGuideShow()) {
            mGuideLayout.startPlaying();
        }
    }

    public void onResume() {
        disableMicroIcons();
        playMicroGuide();
        initializeShutterTip();
    }
}
