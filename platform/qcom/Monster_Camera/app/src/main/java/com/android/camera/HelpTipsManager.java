package com.android.camera;

import android.view.ViewGroup;

import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.TouchCoordinate;
import com.tct.camera.R;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by nielei on 15-11-16.
 */
public class HelpTipsManager implements HelpTipController, ShutterButton.OnShutterButtonListener {

    //help tip group
    public static final int WELCOME_GROUP = 0;
    public static final int PANORAMA_GROUP = 1;
    public static final int MANUAL_GROUP = 2;
    public static final int SWITCH_FRONT_CAMERA_GROUP = 3;
    public static final int GESTURE_CONFIRM_GROUP = 4;
    public static final int QUICK_SETTINGS_GROUP = 5;
    public static final int PINCH_ZOOM_GROUP = 6;
    public static final int SETTINGS_GROUP = 7;
    public static final int MODE_GROUP = 8;
    public static final int VIDEO_STOP_GROUP = 9;
    public static final int VIDEO_SNAP_GROUP = 10;
    public static final int RECENT_GROUP = 11;
    //welcome group tip
    public static final int WELCOME_TIP = 0;
    public static final int SNAP_TIP = 1;
    public static final int CAMERAKEY_TIP = 2;
    public static final int RENCET_TIP = 3;
    public static final int VIDEO_TIP = 4;
    public static final int UNKOWN_GROUP_ID = -1;
    public static final int UNKOWN_TIP_ID = -1;
    private static final Log.Tag TAG = new Log.Tag("HelpTipsManager");
    private static final long UNKOWN_TIME = -1;
    private static final int DAY = 1000 * 60 * 60 * 24;
    private static final int INIT_DAY = 0;
    private static final int FIRST_DAY = 1;
    private static final int SECOND_DAY = 2;
    private static final int THIRD_DAY = 3;
    private static final int FOURTH_DAY = 4;
    private static final int FRONT_CAMERA_OPEND_TIMES = 2;

    private final WeakReference<CameraActivity> mWrActivity;
    private final CameraActivity mActivity;
    private final ViewGroup mRootView;
    private HelpTip mHelpTip;
    private SettingsManager mSettingsManager;
    private int mCurTipGroupId = UNKOWN_GROUP_ID;
    private int mCurTipMemberId = UNKOWN_TIP_ID;
    private static final int SINGLE_MEMBER_TIP_ID = 0;
    private ManualUI mManualUpdateUIListener;
    private static final int SECOND_TIME_USE_CAMERA = 2;
    private int SECOND_TIME_USE_GESTURE = 2;
    private boolean mSwitchToPanoMode = false;
    private static final int READY = 1;

    //mamage ready alarming tasks. key : help tip groupid ; value: tip status
    private Map<Integer, Integer> alarmTasksMap = new TreeMap<>();
    private static final int FIRST_START_RECORD = 1;
    private static final int SECOND_START_RECORD = 2;

    public HelpTipsManager(CameraActivity cameraActivity) {
        mWrActivity = new WeakReference<CameraActivity>(cameraActivity);
        mActivity = mWrActivity.get();
        mSettingsManager = mActivity.getSettingsManager();
        mRootView = (ViewGroup) mActivity.findViewById(R.id.helptips_placeholder_wrapper);
    }

    /**
     * start alarm task according the used time of camera app
     */
    public void startAlarmTask() {
        alarmTasksMap.clear();
        long systemTime = mSettingsManager.getLong(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_SYSTEM_TIME, UNKOWN_TIME);
        int days = INIT_DAY;
        if (systemTime == UNKOWN_TIME) {
            mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_HELP_TIP_SYSTEM_TIME, System.currentTimeMillis());
        } else {
            Date firstDate = new Date(systemTime);
            Date currDate = new Date(System.currentTimeMillis());
            days = (int) (currDate.getTime() - firstDate.getTime()) / DAY;
        }

        while (days > INIT_DAY) {
            if (days == FIRST_DAY) {
                if (!checkHelpTipOverByGroupId(QUICK_SETTINGS_GROUP)) {
                    addAlarmTask(QUICK_SETTINGS_GROUP);
                }

            } else if (days == SECOND_DAY) {
                if (!checkHelpTipOverByGroupId(PINCH_ZOOM_GROUP)) {
                    addAlarmTask(PINCH_ZOOM_GROUP);
                }
            } else if (days == THIRD_DAY) {
                if (!checkHelpTipOverByGroupId(SETTINGS_GROUP)) {
                    addAlarmTask(SETTINGS_GROUP);
                }

            } else if (days == FOURTH_DAY) {
                if (!checkHelpTipOverByGroupId(MODE_GROUP)) {
                    addAlarmTask(MODE_GROUP);
                }
            }
            days--;
        }
    }

    /**
     * calculate the times that camera app is used or shows.
     */
    public void calcCameraUseTimes() {
        int cameraAppUseTimes = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_USER_APP_TIMES, 0);
        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_USER_APP_TIMES, ++cameraAppUseTimes);
    }

    /**
     * response gesture shot
     */
    public void gestureShotResponse() {
        if (isHelpTipShowExist() && mHelpTip.getCurTipGroupId() == GESTURE_CONFIRM_GROUP) {
            mHelpTip.goToNextTip(false);
        }
    }

    /**
     * check if help tip needs to response events of clicking boom key
     *
     * @return boolean, true meams that it responses boom key.
     */
    public boolean isNeedBoomKeyResponse() {
        if (mHelpTip != null) {
            if (mHelpTip.getCurTipGroupId() == WELCOME_GROUP &&
                    mHelpTip.getCurTipId() == CAMERAKEY_TIP) {
                return true;
            }
        }

        return false;
    }

    /**
     * isBackCameraFacing
     *
     * @return boolean, true meams back camera facing
     */
    private boolean isBackCameraFacing() {
        boolean bBackCameraFacing = true;
        if (mSettingsManager != null) {
            bBackCameraFacing = (Keys.isCameraBackFacing(mSettingsManager, SettingsManager.SCOPE_GLOBAL));
        }
        return bBackCameraFacing;
    }

    /**
     * auto mode
     *
     * @return boolean, true meams auto mode.
     */
    public boolean isAutoMode() {
        return (mActivity.getCurrentModuleIndex() ==
                mActivity.getResources().getInteger(R.integer.camera_mode_photo));
    }

    /**
     * video mode
     *
     * @return boolean, true meams video mode.
     */
    public boolean isVideoMode() {
        return (mActivity.getCurrentModuleIndex() ==
                mActivity.getResources().getInteger(R.integer.camera_mode_video));
    }

    /**
     * pano mode
     *
     * @return boolean, true meams pano mode.
     */
    public boolean isPanoMode() {
        return (mActivity.getCurrentModuleIndex() ==
                mActivity.getResources().getInteger(R.integer.camera_mode_pano));
    }

    /**
     * manual mode
     *
     * @return boolean, true meams manual mode.
     */
    public boolean isManualMode() {
        return (mActivity.getCurrentModuleIndex() ==
                mActivity.getResources().getInteger(R.integer.camera_mode_manual));
    }

    /**
     * schedule special help tip's task by group id
     *
     * @return boolean, true meams successful.
     */
    public boolean scheduleTaskHelpTip(int groudId) {
        Log.i(TAG, "scheduleTaskHelpTip groudId = " + printGroupName(groudId));
        boolean secheduleResult = false;
        //check conditions required for help tip
        if (checkConditions(groudId)) {
            createAndShowHelpTip(groudId, false);
            secheduleResult = true;
        }
        return secheduleResult;
    }

    /**
     * remove alarm task by group id
     */
    @Override
    public void removeAlarmTask(int groudid) {
        if (alarmTasksMap != null) {
            alarmTasksMap.remove(groudid);
        }
    }

    /**
     * add alarm task by group id
     */
    public void addAlarmTask(int groudid) {
        if (alarmTasksMap != null) {
            alarmTasksMap.put(groudid, READY);
        }
    }

    /**
     * check alarm tasks that are ready and
     * schedule prior task when help tip is idle.
     * it inculdes the 6 tips : gesture , switch front camera,
     * settings, quick settings , pinch zoom, mode
     */
    @Override
    public void checkAlarmTaskHelpTip() {
        Log.i(TAG, "checkAlarmTaskHelpTip E");
        //check if switch front camera needs to show
        int cameraUseTimes = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_USER_APP_TIMES, 0);
        boolean bSwitchFrontFinished = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_FRONT_CAMERA_FINISHED, false);
        boolean bShowSwitchFront = ((cameraUseTimes >= SECOND_TIME_USE_CAMERA) && !bSwitchFrontFinished);
        if (bShowSwitchFront) {
            addAlarmTask(SWITCH_FRONT_CAMERA_GROUP);
        }

        //check if front camera gesture needs to show
        boolean bGesturePrompted = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_GESTURE_FINISHED, false);
        int gestureOpenedTimes = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_FRONT_CAMERA_OPENED_TIMES, 0);
        boolean bShowGesture = !bGesturePrompted && (gestureOpenedTimes >= SECOND_TIME_USE_GESTURE);
        if (bShowGesture) {
            addAlarmTask(GESTURE_CONFIRM_GROUP);
        }

        if (!isHelpTipShowExist()) {
            Log.i(TAG, "checkAlarmTaskHelpTip alarmTasksMap.size = " + alarmTasksMap.size());
            for (int key : alarmTasksMap.keySet()) {
                int value = alarmTasksMap.get(key);
                Log.i(TAG, "checkAlarmTaskHelpTip begin = " + printGroupName(key) + ",value = " + value);
                if (value == READY) {
                    boolean executeTaskFlags = true;
                    //when sliding to panorama mode,pano's tip shows first.
                    if (mSwitchToPanoMode) {
                        mSwitchToPanoMode = false;
                        executeTaskFlags = false;
                    }

                    if (executeTaskFlags) {
                        //schedule the task and stop checking.
                        if (scheduleTaskHelpTip(key)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * the 2nd time a user opens the front facing camera.
     */
    public void openGestureHelpTip(int cameraId) {
        int frontCameraId = Integer.valueOf(mActivity.getAndroidContext().getString(R.string.pref_camera_id_index_front));
        boolean bGesturePrompted = mActivity.getSettingsManager().getBoolean(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_GESTURE_FINISHED, false);
        boolean isGestureDetectionOn = Keys.isGestureDetectionOn(mSettingsManager);
        Log.d(TAG,"isEnableGestureRecognization"+isGestureDetectionOn);
        if(isGestureDetectionOn && !bGesturePrompted && cameraId == frontCameraId){
            int frontCameraOpenedTimes = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_HELP_TIP_FRONT_CAMERA_OPENED_TIMES, 0);
            frontCameraOpenedTimes++;
            mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_HELP_TIP_FRONT_CAMERA_OPENED_TIMES, frontCameraOpenedTimes);
            if(isHelpTipShowExist()){
                goNextHelpTipStage();
            }else {
                if(frontCameraOpenedTimes == FRONT_CAMERA_OPEND_TIMES){
                    if (!scheduleTaskHelpTip(GESTURE_CONFIRM_GROUP)) {
                        checkAlarmTaskHelpTip();
                    }
                }
            }
        }else if(isHelpTipShowExist()){
            notifyEventFinshed();
        }else {
            checkAlarmTaskHelpTip();
        }
    }

    /**
     * check executing conditions for help tip
     */
    private boolean checkConditions(int groupId) {
        boolean canShow = checkHelpTipOverByGroupId(WELCOME_GROUP)
                && !isHelpTipShowExist() && !checkHelpTipOverByGroupId(groupId);

        switch (groupId) {
            case WELCOME_GROUP:
            case QUICK_SETTINGS_GROUP:
                canShow = canShow && isAutoMode() && isBackCameraFacing();
                break;
            case GESTURE_CONFIRM_GROUP:
                canShow = canShow && !isBackCameraFacing();
                break;
            case VIDEO_STOP_GROUP:
            case VIDEO_SNAP_GROUP:
                canShow = canShow && isVideoMode();
                break;
            case PINCH_ZOOM_GROUP:
                canShow = canShow && !isPanoMode();
                break;
            case MANUAL_GROUP:
                canShow = canShow && isManualMode();
                break;
            case PANORAMA_GROUP:
                canShow = canShow && isPanoMode();
                break;
            case SETTINGS_GROUP:
                canShow = canShow && isAutoMode();
                break;
            case RECENT_GROUP:
                boolean bWelComePrompted = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_WELCOME_FINISHED, false);
                boolean bRecentPrompted = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_RECENT_FINISHED, false);
                canShow = canShow && bWelComePrompted && !bRecentPrompted;
                break;
            case MODE_GROUP:
            case SWITCH_FRONT_CAMERA_GROUP:
                break;
            default:
                break;
        }
        Log.i(TAG, "checkConditions groupId = " + printGroupName(groupId) + ",isAutoMode() = " + isAutoMode()
                + ",WELCOME_GROUP is over = " + checkHelpTipOverByGroupId(WELCOME_GROUP)
                + ",HelpTip is ShowExist() = " + isHelpTipShowExist()
                + ",special groupId is over = " + checkHelpTipOverByGroupId(groupId)
                + ",isBackCameraFacing =" + isBackCameraFacing()
                + ",canShow = " + canShow);

        return canShow;
    }

    /**
     * create Help Tip include groudid , mCurTipMemberId ,mCurHelpTipLayoutId
     *
     * @param groupid ,help tip's group id
     * @param outCall ,true if it called by outside.
     * @return boolean, true meams successful
     */
    public boolean createHelpTip(int groupid, boolean outCall) {
        Log.e(TAG, "createHelpTip groupid = " + printGroupName(groupid));
        if (checkHelpTipOverByGroupId(groupid)) {
            Log.e(TAG, "createHelpTip is over" + printGroupName(groupid));
            return false;
        }

        mCurTipGroupId = groupid;
        switch (mCurTipGroupId) {
            case WELCOME_GROUP:
                if (outCall) {
                    String TutorialStep = mSettingsManager.getString(SettingsManager.SCOPE_GLOBAL,
                            Keys.KEY_HELP_TIP_WELCOME_STEP, "0");
                    int localeTipId = Integer.valueOf(TutorialStep);
                    mCurTipMemberId = localeTipId;
                }

                //member tip index
                if(mCurTipMemberId > VIDEO_TIP){
                    Log.e(TAG, "member tip index is invalid :groupid = " + printGroupName(groupid)
                        + ",mCurTipMemberId = " + mCurTipMemberId);
                    return false;
                }

                mHelpTip = new MultiHelpTip(WELCOME_GROUP, mCurTipMemberId, this, mActivity);
                break;

            case QUICK_SETTINGS_GROUP:
            case SETTINGS_GROUP:
            case SWITCH_FRONT_CAMERA_GROUP:
                mActivity.getButtonManager().setHelpTipListener(this);
            case PINCH_ZOOM_GROUP:
            case GESTURE_CONFIRM_GROUP:
            case MODE_GROUP:
            case PANORAMA_GROUP:
            case MANUAL_GROUP:
            case VIDEO_STOP_GROUP:
            case VIDEO_SNAP_GROUP:
            case RECENT_GROUP:
                mHelpTip = new SingleHelpTip(groupid, SINGLE_MEMBER_TIP_ID, this, mActivity);
                break;

            default:
                break;
        }

        return true;
    }

    /**
     * check if Help Tip is over
     *
     * @param specialGroupId ,if specialGroupId is -1, check mCurTipGroupId default.
     * @return boolean, true meams help tip of the group is over
     */
    public boolean checkHelpTipOverByGroupId(int specialGroupId) {
        boolean isOver = false;
        switch (specialGroupId) {
            case WELCOME_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_WELCOME_FINISHED, false);
                break;

            case PANORAMA_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_PANO_FINISHED, false);
                break;

            case MANUAL_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_MANUAL_FINISHED, false);
                break;

            case PINCH_ZOOM_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_PINCH_ZOOM_FINISHED, false);
                break;
            case QUICK_SETTINGS_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_QUICK_SETTINGS_FINISHED, false);
                break;
            case SETTINGS_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_SETTINGS_FINISHED, false);
                break;
            case SWITCH_FRONT_CAMERA_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_FRONT_CAMERA_FINISHED, false);
                break;

            case GESTURE_CONFIRM_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_GESTURE_FINISHED, false);
                break;
            case MODE_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_MODE_FINISHED, false);
                break;
            case VIDEO_STOP_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_STOP_VIDEO_FINISHED, false);
                break;
            case VIDEO_SNAP_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_VIDEO_SNAP_FINISHED, false);
                break;
            case RECENT_GROUP:
                isOver = mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_RECENT_FINISHED, false);
                break;

        }
        return isOver;
    }

    /**
     * show Help Tip
     */
    public void showHelpTipTutorial() {
        if (mHelpTip != null) {
            mHelpTip.showHelpTipCling();
        }

    }

    /**
     * go to next Help Tip include groudid , mCurTipMemberId ,mCurHelpTipLayoutId
     */
    public void goNextHelpTipStage() {
        Log.i(TAG, "goNextHelpTipStage mCurTipGroupId = " + mCurTipGroupId
                + ",mCurTipMemberId = " + mCurTipMemberId);
        if (isHelpTipShowExist()) {
            mHelpTip.goToNextTip(false);
        }
    }

    /**
     * notify to finish current tip
     */
    @Override
    public void notifyFinishHelpTip() {
        mActivity.getButtonManager().setHelpTipListener(null);

        if (mHelpTip != null) {
            mHelpTip = null;
        }
    }

    @Override
    public void onUpdateUIChangedFromTutorial() {
        if (mManualUpdateUIListener != null)
            mManualUpdateUIListener.onUpdateUIChangedFromTutorial();
    }

    /**
     * response when camera bursts shot by long pressing
     * shutter button or boom key
     */
    public void onBurstShotResponse() {
        Log.i(TAG, "onBurstShotResponse E isHelpTipShowExist = " + isHelpTipShowExist());
        if (isHelpTipShowExist()) {
            Log.i(TAG, "mHelpTip.getCurTipGroupId() = " + mHelpTip.getCurTipGroupId()
                    + ",mHelpTip.getCurTipId() = " + mHelpTip.getCurTipId());
            if (mHelpTip.getCurTipGroupId() == WELCOME_GROUP) {
                if (mHelpTip.getCurTipId() == SNAP_TIP || mHelpTip.getCurTipId() == CAMERAKEY_TIP) {
                    mHelpTip.goToNextTip(false);
                }
            }
        } else {
            if (!scheduleTaskHelpTip(RECENT_GROUP)) {
                checkAlarmTaskHelpTip();
            }
        }
    }

    /**
     * response to single shot when clicking boom key.
     */
    public void onBoomKeySingleShotResponse() {
        Log.i(TAG, "onBoomKeySingleShotResponse isHelpTipShowExist =" + isHelpTipShowExist()
                + "mHelpTip.getCurTipGroupId() = " + mHelpTip.getCurTipGroupId()
                + ",mHelpTip.getCurTipId() = " + mHelpTip.getCurTipId());
        if (isHelpTipShowExist()) {
            if (mHelpTip.getCurTipGroupId() == WELCOME_GROUP) {
                if (mHelpTip.getCurTipId() == CAMERAKEY_TIP) {
                    mHelpTip.goToNextTip(false);
                }
            }
        }
    }

    /**
     * response to single shot when clicking capture button.
     */
    public void onRecentTipResponse() {
        Log.e(TAG, "onRecentTipResponse E isHelpTipShowExist = " + isHelpTipShowExist());
        if (!isHelpTipShowExist()) {
            if (!scheduleTaskHelpTip(RECENT_GROUP)) {
                checkAlarmTaskHelpTip();
            }
        }
    }

    /**
     * if Help Tip is show or exists
     *
     * @return true meams show and exists
     */
    public boolean isHelpTipShowExist() {
        return mHelpTip != null ? mHelpTip.IsShowExist() : false;
    }

    /**
     * when event finshed ,it notifys to go next.
     * event comes from flash/hdr/night,front camera ,camera settings ,
     * pinch zoom,manual mode and panorama mode
     */
    public void notifyEventFinshed() {
        Log.i(TAG, "notifyEventFinshed mCurTipGroupId = " + mCurTipGroupId
                + ",mCurTipMemberId = " + mCurTipMemberId + ",isShowExist =" + isHelpTipShowExist());
        if (isHelpTipShowExist()) {
            goNextHelpTipStage();
        }
    }

    /**
     * when mode changes finshed only for help tip is MODE_GROUP
     *
     * @param modeIndex , current mode index
     */
    public void notifyModeChanged(int modeIndex, final Runnable modeChangeRunnable) {
        Log.i(TAG, "notifyModeChanged mCurTipGroupId = " + mCurTipGroupId
                + ",mCurTipMemberId = " + mCurTipMemberId
                + ",modeIndex = " + modeIndex
                + " ,isShowExist = " + isHelpTipShowExist());
        if (modeIndex == mActivity.getResources().getInteger(R.integer.camera_mode_pano)) {
            mSwitchToPanoMode = true;
        }

        if (modeChangeRunnable != null) {
            modeChangeRunnable.run();
        }

        if (isHelpTipShowExist()) {
            if (mCurTipGroupId == MODE_GROUP) {
                mHelpTip.notifyModeChanged();
            }
        } else if (modeIndex != mActivity.getResources().getInteger(R.integer.camera_mode_video)) {
            checkAlarmTaskHelpTip();
        }
    }

    /**
     * set Update UI Listener only for help tip is MODE_GROUP
     *
     * @param updateUIListener , ManualUI
     */
    public void setManualUpdateUIListener(ManualUI updateUIListener) {
        mManualUpdateUIListener = updateUIListener;
    }

    /**
     * pause help tip
     */
    public void pause() {
        HelpTip.mVideoReadyFlag = false; //MODIFIED by nie.lei, 2016-04-01,BUG-1875810
        if (mHelpTip != null) {
            mHelpTip.doPause();
            mHelpTip = null;
        }

        mCurTipGroupId = UNKOWN_GROUP_ID;
        mCurTipMemberId = UNKOWN_TIP_ID;
    }

    @Override
    public void onShutterButtonFocus(boolean pressed) {

    }

    @Override
    public void onShutterCoordinate(TouchCoordinate coord) {

    }

    /**
     * response single shot for snap tip
     */
    @Override
    public void onShutterButtonClick() {
        if (isHelpTipShowExist() && mHelpTip != null) {
            if (mHelpTip.getCurTipGroupId() == WELCOME_GROUP && mHelpTip.getCurTipId() == SNAP_TIP) {
                mHelpTip.goToNextTip(false);
            }
        }
    }

    @Override
    public void onShutterButtonLongClick() {

    }

    /**
     * get root view of help tip
     */
    public ViewGroup getHelpTipView() {
        return mRootView;
    }

    /**
     * check if it intercepts events from main activity Layout
     */
    public boolean helpTipCheckToIntercept() {
        return (mHelpTip != null ? mHelpTip.checkToIntercept() : false);
    }

    /**
     * destroy help tip
     */
    public void destroy() {
        if (mHelpTip != null) {
            mHelpTip.cleanUpHelpTip();
            mHelpTip = null;
        }

        alarmTasksMap.clear();
        alarmTasksMap = null;
    }

    /**
     * make sure help tip begins to record until video mode is ready,
     * or it will cause camera error.
     */
    public void setVideoReadlyFlags() {
         /*MODIFIED-BEGIN by nie.lei, 2016-04-01,BUG-1875810*/
        if(!checkHelpTipOverByGroupId(WELCOME_GROUP)){
            HelpTip.mVideoReadyFlag = true;
             /*MODIFIED-END by nie.lei,BUG-1875810*/
        }
    }

    public String printGroupName(int groudId) {
        switch (groudId) {
            case WELCOME_GROUP:
                return "welcome";
            case QUICK_SETTINGS_GROUP:
                return "quick settings";
            case SETTINGS_GROUP:
                return "SETTINGS_GROUP";
            case SWITCH_FRONT_CAMERA_GROUP:
                return "front camera";
            case PINCH_ZOOM_GROUP:
                return "pinch zoom";
            case GESTURE_CONFIRM_GROUP:
                return "gesture confirm";
            case MODE_GROUP:
                return "mode";
            case PANORAMA_GROUP:
                return "panorama";
            case MANUAL_GROUP:
                return "manual";
            case VIDEO_STOP_GROUP:
                return "stop video";
            case VIDEO_SNAP_GROUP:
                return "video snap";
            case RECENT_GROUP:
                return "recent";
        }
        return "unknow";
    }

    /**
     * response when clicking settings button
     */
    public void clickSettingResponse() {
        if (isHelpTipShowExist()) {
            if (mHelpTip.getCurTipGroupId() == SETTINGS_GROUP) {
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_HELP_TIP_SETTINGS_FINISHED, true);
                removeAlarmTask(SETTINGS_GROUP);
            }
        }

    }

    public void createAndShowHelpTip(int groupId, boolean outCall) {
        Log.i(TAG,"createAndShowHelpTip groupId = " + printGroupName(groupId) + ",outCall = " + outCall);
        if(!isHelpTipShowExist() && !checkHelpTipOverByGroupId(groupId)){
            if(createHelpTip(groupId, outCall)){
                showHelpTipTutorial();
            }else {
                Log.e(TAG, "createAndShowHelpTip failed");
            }
        }
    }

    public void startRecordVideoResponse() {
        int videoTimes = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_VIDEO_TIMES, 0);
        videoTimes++;
        mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_VIDEO_TIMES, videoTimes);
        int groupID = UNKOWN_GROUP_ID;
        if(videoTimes == FIRST_START_RECORD){
            groupID = VIDEO_STOP_GROUP;
        }

        if(videoTimes == SECOND_START_RECORD){
            groupID = VIDEO_SNAP_GROUP;
        }
        if (groupID != UNKOWN_GROUP_ID && !scheduleTaskHelpTip(groupID)) {
            checkAlarmTaskHelpTip();
        }
    }

    /**
     * NextButtonListener notify click events from next button
     */
    public interface NextButtonListener {
        public void onUpdateUIChangedFromTutorial();
    }

}
