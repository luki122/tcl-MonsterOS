package com.monster.cloud.utils;

import android.content.Context;

import com.monster.cloud.R;
import com.monster.cloud.constants.Constant;
import com.monster.cloud.preferences.FilePreferences;
import com.monster.cloud.preferences.Preferences;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yubai on 16-11-2.
 */
public class SyncTimeUtil {

    static FilePreferences preferences;

    public static void getInstance(Context context) {
        if (preferences == null) {
            preferences = (FilePreferences) Preferences.Factory.getInstance(context, Constant.FILE_TYPE);
        }
    }

    public static void updateContactSyncTime(Context context, long contactSyncTime) {
        if (preferences == null) {
            getInstance(context);
        }

        if (0l != contactSyncTime) {
            preferences.setContactSyncTime(contactSyncTime);
        }
    }

    public static void updateSmsSyncTime(Context context, long smsSyncTime) {
        if (preferences == null) {
            getInstance(context);
        }

        if (0l != smsSyncTime) {
            preferences.setSmsSyncTime(smsSyncTime);
        }
    }

    public static void updateRecordSyncTime(Context context, long recordSyncTime) {
        if (preferences == null) {
            getInstance(context);
        }

        if (0l != recordSyncTime) {
            preferences.setRecordSyncTime(recordSyncTime);
        }
    }

    public static void updateListSyncTime(Context context, long listSyncTime) {
        if (preferences == null) {
            getInstance(context);
        }

        if (0l != listSyncTime) {
            preferences.setAppListSyncTime(listSyncTime);
        }
    }

    public static long getContactSyncTime(Context context) {
        if (preferences == null) {
            getInstance(context);
        }
        return preferences.getContactSyncTime();
    }

    public static long getSmsSyncTime(Context context) {
        if (preferences == null) {
            getInstance(context);
        }
        return preferences.getSmsSyncTime();
    }

    public static long getRecordSyncTime(Context context) {
        if (preferences == null) {
            getInstance(context);
        }
        return preferences.getRecordSyncTime();
    }

    public static long getAppListSyncTime(Context context) {
        if (preferences == null) {
            getInstance(context);
        }
        return preferences.getAppListSyncTime();
    }

    public static void setContactSyncLabel(Context context, boolean isContactAutoOn) {
        if (preferences == null) {
            getInstance(context);
        }

        preferences.setContactSyncLabel(isContactAutoOn);
    }

    public static void setSmsSyncLabel(Context context, boolean isSmsAutoOn) {
        if (preferences == null) {
            getInstance(context);
        }

        preferences.setSmsSyncLabel(isSmsAutoOn);
    }

    public static void setRecordSyncLabel(Context context, boolean isRecordAutoOn) {
        if (preferences == null) {
            getInstance(context);
        }

        preferences.setRecordSyncLabel(isRecordAutoOn);

    }

    public static void setAppListSyncLabel(Context context, boolean isListAutoOn) {
        if (preferences == null) {
            getInstance(context);
        }

        preferences.setAppListSyncLabel(isListAutoOn);

    }

    public static boolean getContactSyncLabel(Context context) {
        if (preferences == null) {
            getInstance(context);
        }
        return preferences.getContactSyncLabel();
    }

    public static boolean getSmsSyncLabel(Context context) {
        if (preferences == null) {
            getInstance(context);
        }
        return preferences.getSmsSyncLabel();
    }

    public static boolean getRecordSyncLabel(Context context) {
        if (preferences == null) {
            getInstance(context);
        }
        return preferences.getRecordSyncLabel();
    }

    public static boolean getAppListSyncLabel(Context context) {
        if (preferences == null) {
            getInstance(context);
        }
        return preferences.getAppListSyncLabel();
    }

    public static void setContactChangedLabel(Context context, boolean isChanged) {
        if (preferences == null) {
            getInstance(context);
        }

        preferences.setContactChangedLabel(isChanged);
    }

    public static boolean getContactChangedLabel(Context context) {
        if (preferences == null) {
            getInstance(context);
        }

        return preferences.getContactSyncLabel();
    }

    public static void setSyncWhenWifiLabel(Context context, boolean isOnlyWifi) {
        if (preferences == null) {
            getInstance(context);
        }

        preferences.setSyncWhenWifiLabel(isOnlyWifi);
    }

    public static boolean getSyncWhenWifiLabel(Context context) {
        if (preferences == null) {
            getInstance(context);
        }

        return preferences.getSyncWhenWifiLabel();
    }

    public static String setTime(long time, Context context) {

        Date lastSync = new Date(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String lastDate = dateFormat.format(lastSync);
        String lastTime = timeFormat.format(lastSync);

        String s = "";

        long temp = System.currentTimeMillis() - time;    //相差毫秒数
        long seconds = temp / 1000;
        if(seconds < 60){
            String sec = context.getResources().getString(R.string.time_sec);
            s += String.format(sec, seconds);
        } else if(seconds >= 60 && seconds < 60 * 60){
            long minGap = seconds / 60;
            String min = context.getResources().getString(R.string.time_min);
            s += String.format(min, minGap);
        } else if(seconds >= 60 * 60 && seconds <= 60 * 60 * 24){
            s += lastTime;
        } else {
            return s += new SimpleDateFormat("MM月dd日").format(lastSync) + lastTime;
        }

        return s;
    }


}
