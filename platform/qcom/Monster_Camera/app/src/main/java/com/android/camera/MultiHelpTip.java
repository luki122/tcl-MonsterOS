package com.android.camera;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.PeekImageView;
import com.android.camera.ui.RotatableButton;
import com.tct.camera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sdduser on 16-1-30.
 */
public class MultiHelpTip extends HelpTip implements View.OnClickListener {

    private static final int[] WELCOME_GROUP_LAYOUT_IDS = new int[]{
            R.layout.welcome_tip, R.layout.snap_tip, R.layout.camera_key_tip,
            R.layout.recent_tip, R.layout.video_tip,
    };
    private static boolean mJumpCameraKeyTip = false;//camera key tip doesn't shows yet
    List<Integer> mWelcomeTipList = new ArrayList<Integer>();
    private ShutterButton mShutterButton;
    private PeekImageView mPeekThumb;
    private RotatableButton mVideoShutterButton;
    private LinearLayout mWelcomeLayout;
    private Button mCanCelTourBtn;
    private Button mTakeTourBtn;

    /**
     * Create a multi help tip overlay.
     *
     * @param groupId    tip group's id
     * @param groupId    tip's id
     * @param controller help tip controller
     * @param activity   cameraActvity
     */
    public MultiHelpTip(int groupId, int tipId, HelpTipController controller, CameraActivity activity) {
        super(tipId, controller, activity);
        mCurTipGroupId = groupId;
        mLayoutResId = WELCOME_GROUP_LAYOUT_IDS[tipId];
        mWelcomeTipList = new ArrayList<Integer>();
        mWelcomeTipList.add(HelpTipsManager.WELCOME_TIP);
        mWelcomeTipList.add(HelpTipsManager.SNAP_TIP);
        mWelcomeTipList.add(HelpTipsManager.CAMERAKEY_TIP);
        mWelcomeTipList.add(HelpTipsManager.RENCET_TIP);
        mWelcomeTipList.add(HelpTipsManager.VIDEO_TIP);
    }

    protected void initWidgets() {
        boolean bNeedinitCommom = true;
        if (mCurTipId == HelpTipsManager.WELCOME_TIP) {
            bNeedinitCommom = false;
        }

        if (bNeedinitCommom) initCommomWidget();

        switch (mCurTipId) {
            case HelpTipsManager.WELCOME_TIP:
                mDrawType = NO_DRAW;
                mTakeTourBtn = (Button) mHelpTipCling.findViewById(R.id.take_tour_btn);
                mCanCelTourBtn = (Button) mHelpTipCling.findViewById(R.id.cancel_tour_btn);
                mWelcomeLayout = (LinearLayout) mHelpTipCling.findViewById(R.id.take_tour_layout);
                mTakeTourBtn.setOnClickListener(this);
                mCanCelTourBtn.setOnClickListener(this);
                break;
            case HelpTipsManager.SNAP_TIP:
                mShutterButton = (ShutterButton) mActivity.findViewById(R.id.shutter_button);
                mDrawType = CIRCLE;
                playAnimation();
                break;
            case HelpTipsManager.CAMERAKEY_TIP:
                mShutterButton = (ShutterButton) mActivity.findViewById(R.id.shutter_button);
                mDrawType = NO_DRAW;
                break;
            case HelpTipsManager.RENCET_TIP:
                mPeekThumb = (PeekImageView) mActivity.findViewById(R.id.peek_thumb);
                mDrawType = CIRCLE;
                playAnimation();
                break;
            case HelpTipsManager.VIDEO_TIP:
                mVideoShutterButton = (RotatableButton) mActivity.findViewById(R.id.video_shutter_button);
                mDrawType = CIRCLE;
                playAnimation();
                break;

            default:
                break;
        }

        mHelpTipCling.setListener(this, mDrawType);
    }

    @Override
    protected void goToNextTip(boolean dismiss) {
        Log.e(TAG, "Tony before dismiss goToNextTip mCurTipId = " + mCurTipId
                + "dismiss =" + dismiss + ",mJumpCameraKeytip = " + mJumpCameraKeyTip);
        if (dismiss) {
            if (mCurTipId == HelpTipsManager.CAMERAKEY_TIP) {
                mCurTipId = HelpTipsManager.VIDEO_TIP;
            } else if (mCurTipId == HelpTipsManager.RENCET_TIP) {
                if (mJumpCameraKeyTip) {
                    mCurTipId = HelpTipsManager.CAMERAKEY_TIP;
                } else {
                    mCurTipId++;
                }
            } else {
                mCurTipId++;
            }
        } else {
            if (mCurTipId == HelpTipsManager.SNAP_TIP) {
                mJumpCameraKeyTip = true;
                mCurTipId = HelpTipsManager.RENCET_TIP;
            } else if (mCurTipId == HelpTipsManager.CAMERAKEY_TIP) {
                if (mJumpCameraKeyTip) {
                    mCurTipId = HelpTipsManager.VIDEO_TIP;
                } else {
                    mCurTipId++;
                }
            } else {
                mCurTipId++;
            }
        }
        Log.e(TAG, "Tony after dismiss goToNextTip mCurTipId = " + mCurTipId
                + "dismiss =" + dismiss + ",mJumpCameraKeyTip = " + mJumpCameraKeyTip);
        if (mWelcomeTipList != null && mCurTipId <= mWelcomeTipList.size() - 1) {
            cleanUpHelpTip();
            showDelayHelpTip();
        } else {
            updateCurHelpTipStep(mCurTipId - 1, true);
            closeAndFinishHelptip();
            if (dismiss) {
                mHelpTipController.checkAlarmTaskHelpTip();
            }
        }
    }

    @Override
    public void showDelayHelpTip() {
        mLayoutResId = WELCOME_GROUP_LAYOUT_IDS[mCurTipId];
        mIsShowExist = true;
        updateCurHelpTipStep(mCurTipId, false);
        super.showDelayHelpTip();
        if (mCurTipId == HelpTipsManager.RENCET_TIP) {
            mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_HELP_TIP_RECENT_FINISHED, true);
        }
    }

    @Override
    protected void updateCurHelpTipStep(int tipId, boolean isOver) {
        if (isOver) {
            mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_HELP_TIP_WELCOME_FINISHED, true);
        } else {
            mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_HELP_TIP_WELCOME_STEP, tipId);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_tour_btn:
                goToNextTip(false);
                mWelcomeLayout.setVisibility(View.GONE);
                break;
            case R.id.cancel_tour_btn:
                updateSettingsAllTipsFinished();
                closeAndFinishHelptip();
                break;
            case R.id.anim_focus:
                clickAnimFucus();
                break;
            default:
                break;
        }
    }

    /**
     * update steps into sharedPreference and finish all tips
     */
    public void updateSettingsAllTipsFinished() {
        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_WELCOME_FINISHED, true);

        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_PANO_FINISHED, true);

        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_MANUAL_FINISHED, true);

        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_PINCH_ZOOM_FINISHED, true);

        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_QUICK_SETTINGS_FINISHED, true);

        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_SETTINGS_FINISHED, true);

        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_FRONT_CAMERA_FINISHED, true);

        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_GESTURE_FINISHED, true);

        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_MODE_FINISHED, true);

        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_STOP_VIDEO_FINISHED, true);

        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_VIDEO_SNAP_FINISHED, true);

        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_RECENT_FINISHED, true);

    }

    @Override
    protected void clickAnimFucus() {
        Log.i(TAG, "clickAnimFucus mCurTipGroupId =" + mCurTipGroupId + ",mCurTipId = " + mCurTipId);

        switch (mCurTipId) {
            case HelpTipsManager.SNAP_TIP:
                if (mShutterButton != null) {
                    mShutterButton.performClick();
                }
                break;
            case HelpTipsManager.RENCET_TIP:
                if (mPeekThumb != null) {
                    mPeekThumb.performClick();
                    if (mJumpCameraKeyTip) {
                        updateCurHelpTipStep(HelpTipsManager.CAMERAKEY_TIP, false);
                    } else {
                        updateCurHelpTipStep(mCurTipId + 1, false);
                    }
                    closeAndFinishHelptip();
                }
                break;
            case HelpTipsManager.VIDEO_TIP:
                Log.i(TAG,"mVideoReadyFlag = " + mVideoReadyFlag); //MODIFIED by nie.lei, 2016-04-01,BUG-1875810
                if (mVideoReadyFlag && mVideoShutterButton != null) {
                    mVideoShutterButton.performClick();
                    mVideoReadyFlag = false;
                }
                break;

            default:
                break;

        }
    }

    /**
     * capture button's burst shot on long press
     */
    public void longClickAnimFucus() {
        if (mShutterButton != null) {
            mShutterButton.performLongClick();
            mLongClickAnimFucus = true;
        }
    }

    protected void dismissHelpTip() {
        goToNextTip(true);
    }
}
