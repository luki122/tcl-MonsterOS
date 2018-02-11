/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

import cn.tcl.meetingassistant.log.MeetingLog;


public class PermissionUtil {
    public static final int REQUEST_CODE_WRITE = 1;
    public static final int REQUEST_CODE_RECORD = 2;
    private static final String TAG = PermissionUtil.class.getSimpleName();
    public static String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static String RECORD_PERMISSION = Manifest.permission.RECORD_AUDIO;
    private static String PHONE_STATE_PERMISSION = Manifest.permission.READ_PHONE_STATE;
    private static String PHONE_OUTGOING_CALL = Manifest.permission.PROCESS_OUTGOING_CALLS;

    /**
     * request write sd permission foe the activity
     *
     * @param activity
     * @return
     */
    public static boolean requestWritePermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, WRITE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{WRITE_PERMISSION}, REQUEST_CODE_WRITE);
            MeetingLog.d(TAG, "Activity don't have write sd permission,so request");
            return false;
        } else {
            MeetingLog.d(TAG, "Activity have had write sd permission");
            return true;
        }
    }

    /**
     * for audio,request write sd permission and record permission.
     *
     * @param activity
     * @return
     */
    public static boolean requestRecordPermission(Activity activity) {
        ArrayList<String> list = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(activity, WRITE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            MeetingLog.d(TAG, "Activity don't have write sd permission,so request");
            list.add(WRITE_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(activity, RECORD_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            MeetingLog.d(TAG, "Activity don't have record permission,so request");
            list.add(RECORD_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(activity, PHONE_OUTGOING_CALL) != PackageManager.PERMISSION_GRANTED) {
            MeetingLog.d(TAG, "Activity don't have record permission,so request");
            list.add(PHONE_OUTGOING_CALL);
        }
        if (ContextCompat.checkSelfPermission(activity, PHONE_STATE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            MeetingLog.d(TAG, "Activity don't have record permission,so request");
            list.add(PHONE_STATE_PERMISSION);
        }
        int size = list.size();
        if (size > 0) {
            String[] permissionGroup = new String[size];
            for (int i = 0; i < size; i++) {
                permissionGroup[i] = list.get(i);
            }
            ActivityCompat.requestPermissions(activity, permissionGroup, REQUEST_CODE_RECORD);
            return false;
        } else {
            MeetingLog.d(TAG, "Activity have had write sd and record permission");
            return true;
        }
    }

}
