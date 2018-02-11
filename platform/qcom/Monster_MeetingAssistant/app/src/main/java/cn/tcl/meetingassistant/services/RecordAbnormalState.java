/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.services;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.tcl.meetingassistant.EditImportPointActivity;
import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.FileUtils;
import cn.tcl.meetingassistant.view.DialogHelper;
import cn.tcl.meetingassistant.view.NotificationHelper;

public class RecordAbnormalState {
    private final String TAG = RecordAbnormalState.class.getSimpleName();
    private boolean isBackApp;
    public static final int STATE_LOW_SIZE_100MB = 100;
    public static final int STATE_LOW_SIZE_50MB = 50;
    public static final int STATE_LOW_BATTERY_10 = 10;
    public static final int STATE_LOW_BATTERY_5 = 5;
    public static final int STATE_NORMAL = 0;
    private int mCurrentState;
    private List<Integer> mStateList = Collections.synchronizedList(new ArrayList<Integer>());
    private EditImportPointActivity mContext;

    private int mMode;
    private static final int MODE_START = 1;
    private static final int MODE_PLAY = 2;
    private static final int MODE_RESUME = 3;

    public RecordAbnormalState(EditImportPointActivity context) {
        mContext = context;
    }

    /**
     * when available is low
     *
     * @param availableSize available size
     */
    public void setLowSize(double availableSize) {
        setState(availableSize, STATE_LOW_SIZE_50MB, STATE_LOW_SIZE_100MB);
    }


    /**
     * manager low battery
     *
     * @param battery
     */
    public void setLowBattery(float battery) {
        setState(battery, STATE_LOW_BATTERY_5, STATE_LOW_BATTERY_10);
    }

    private void setState(double value, int minValue, int maxValue) {
        MeetingLog.d(TAG, "set state value = " + value);
        if (value >= maxValue) {
            int latestItem = getLatestStateItem();
            if (mStateList.contains(maxValue)) {
                mStateList.remove(mStateList.indexOf(maxValue));
            }
            if (mStateList.contains(minValue)) {
                mStateList.remove(mStateList.indexOf(minValue));
            }
            //no update,return
            if (latestItem == getLatestStateItem()) {
                return;
            }
        }
        if (value < maxValue && value >= minValue) {
            if (!mStateList.contains(maxValue)) {
                mStateList.add(maxValue);
            } else {
                return;
            }
        }
        if (value < minValue) {
            if (!mStateList.contains(minValue)) {
                mStateList.add(minValue);
            } else {
                return;
            }
        }
        manager();
    }

    /**
     * manager show dialog or notification
     */
    public void manager() {
        if (MeetingLog.DEBUG) {
            for (int i : mStateList) {
                MeetingLog.d(TAG, "state list= " + i);
            }
        }
        MeetingLog.d(TAG, "mode=" + mMode);
        int state = -1;
        state = getLatestStateItem();
        if (mMode == MODE_PLAY) {
            if (isBackground()) {
                showNotify(state);
            } else {
                showPlayDialog(state);
            }
        } else if (mMode == MODE_START) {
            showStartDialog(state);
        }
    }

    private int getLatestStateItem() {
        int state;
        if (mStateList.size() != 0) {
            state = mStateList.get(mStateList.size() - 1);
        } else {
            state = STATE_NORMAL;
        }
        return state;
    }

    private void showStartDialog(int state) {
        switch (state) {
            case STATE_NORMAL:
                break;
            case STATE_LOW_BATTERY_5:
                DialogHelper.showDialog(mContext, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, R.string.dialog_back_title, R.string.dialog_start_battery_5, R.string.dialog_i_know, 0);
                break;
            case STATE_LOW_BATTERY_10:
                DialogHelper.showDialog(mContext, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            double size = FileUtils.getSdAvailableSize();
                            if (size > STATE_LOW_SIZE_50MB) {
                                startRecord();
                                setLowSize(size);
                            } else {
                                setLowSize(size);
                            }

                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {

                        }
                    }
                }, R.string.dialog_back_title, R.string.dialog_start_battery_10, R.string.audio, R.string.cancel);
                break;
            case STATE_LOW_SIZE_100MB:
                DialogHelper.showDialog(mContext, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            goToFileManager();
                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {

                        }
                    }
                }, R.string.dialog_back_title, R.string.dialog_space_100m, R.string.dialog_space_clear, R.string.dialog_ignore);
                break;
            case STATE_LOW_SIZE_50MB:
                DialogHelper.showDialog(mContext, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            goToFileManager();
                        }
                    }
                }, R.string.dialog_back_title, R.string.dialog_space_50m_start, R.string.dialog_space_clear, R.string.cancel);
                break;
        }
    }

    private void showPlayDialog(int state) {
        switch (state) {
            case STATE_NORMAL:
                break;
            case STATE_LOW_BATTERY_5:
                stopRecord();
                DialogHelper.showDialog(mContext, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, R.string.dialog_back_title, R.string.dialog_battery_5, R.string.dialog_i_know, 0);
                break;
            case STATE_LOW_BATTERY_10:
                DialogHelper.showDialog(mContext, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            stopRecord();
                        }
                    }
                }, R.string.dialog_back_title, R.string.dialog_battery_10, R.string.dialog_stop, R.string.dialog_ignore);
                break;
            case STATE_LOW_SIZE_50MB:
                stopRecord();
                DialogHelper.showDialog(mContext, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, R.string.dialog_back_title, R.string.dialog_space_50m_stop, R.string.dialog_i_know, 0);
                break;
            case STATE_LOW_SIZE_100MB:
                DialogHelper.showDialog(mContext, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            goToFileManager();
                        }
                    }
                }, R.string.dialog_back_title, R.string.dialog_space_100m, R.string.dialog_space_clear, R.string.dialog_ignore);
                break;
        }

    }

    private void showNotify(int state) {
        int strId = 0;
        switch (state) {
            case STATE_NORMAL:
                int recordState = getRecordState();
                if (recordState == SoundRecorderService.STATE_RECORDING) {
                    strId = R.string.notification_recording;
                } else if (recordState == SoundRecorderService.STATE_PAUSE_RECORDING) {
                    strId = R.string.notification_pause_record;
                }
                break;
            case STATE_LOW_BATTERY_5:
                stopRecord();
                strId = R.string.notification_battery_low_stop;
                break;
            case STATE_LOW_BATTERY_10:
                strId = R.string.notification_battery_low;
                break;
            case STATE_LOW_SIZE_50MB:
                stopRecord();
                strId = R.string.notification_space_not_enough_stop;
                break;
            case STATE_LOW_SIZE_100MB:
                strId = R.string.notification_space_not_enough;
                break;
        }
        NotificationHelper.showNotification(mContext, strId);
    }

    //true mean app is in background,false is in foreground.
    private boolean isBackground() {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context
                .ACTIVITY_SERVICE);
        //Here will return to my own information only
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.processName.equals(mContext.getPackageName())) {
                if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo
                        .IMPORTANCE_BACKGROUND) {
                    MeetingLog.d(TAG, "this is run in background: " + appProcessInfo.processName);
                    return true;
                } else {
                    MeetingLog.d(TAG, "this is run in foreground: " + appProcessInfo.processName);
                    return false;
                }
            }
        }
        return false;
    }

    private void goToFileManager() {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("cn.tcl.filemanager", "cn.tcl.filemanager.activity.FileBrowserActivity");
        intent.setComponent(componentName);
        mContext.startActivity(intent);
    }

    public boolean beforeStartRecord() {
        mMode = MODE_START;
        float battery = getCurrentBattery();
        if (battery < 10) {
            setLowBattery(battery);
        } else {
            double size = FileUtils.getSdAvailableSize();
            if (size >= STATE_LOW_SIZE_50MB) {
                startRecord();
            }
            setLowSize(size);
        }
        return false;
    }

    private float getCurrentBattery() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //not should a receiver
        Intent intentBattery = mContext.registerReceiver(null, filter);

        int batteryLevel = intentBattery.getIntExtra("level", 0);
        int batterySum = intentBattery.getIntExtra("scale", 100);
        float rotatio = batteryLevel * 1.0f / batterySum * 100;
        return rotatio;
    }

    private void startRecord() {
        mContext.startAudioRecord();
        mMode = MODE_PLAY;
    }

    private void stopRecord() {
        mContext.stopAudioRecord();
//        mStateList.clear();
    }

    private int getRecordState() {
        return mContext.getRecordState();
    }

    public void resumeDialog() {
        if (isBackApp) {
            showPlayDialog(getLatestStateItem());
            isBackApp = false;
        }
    }

    public void switchback() {
        isBackApp = true;
        showNotify(getLatestStateItem());
    }
}
