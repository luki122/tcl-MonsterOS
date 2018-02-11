/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import java.util.List;
import android.os.Environment;

public class Utils {

    public static final int CATEGORY_SYS = 0;
    public static final int CATEGORY_APP = 1;
    public static final int CATEGORY_IMAGE = 2;
    public static final int CATEGORY_VIDEO = 3;
    public static final int CATEGORY_AUDIO = 4;
    public static final int CATEGORY_DOCUMENT = 5;

    public static final int ERROR_NETWORK = 90000;

    public static final float TRANSFER_SPEED = 4 * 1024 *1024f;



    public static final String CATEGORY_SYS_SIZE = "sys_size";
    public static final String CATEGORY_APP_SIZE = "app_size";
    public static final String CATEGORY_IMAGE_SIZE = "image_size";
    public static final String CATEGORY_VIDEO_SIZE = "video_size";
    public static final String CATEGORY_AUDIO_SIZE = "audio_size";
    public static final String CATEGORY_DOCUMENT_SIZE = "doc_size";

    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final String RECEIVED_PATH = SDCARD_PATH + "/Transfer/received";
    public static final String BACKUP_PATH = SDCARD_PATH + "/Transfer/backup";



    public static final String SYS_DATA_BACKUP_PATH = BACKUP_PATH + "/sys_data_backup";
    public static final String SYS_SD_DATA_BACKUP_PATH = BACKUP_PATH + "/sd_data_backup";
    public static final String SYS_SQL_DATA_BACKUP_PATH = BACKUP_PATH + "/sd_sql_backup";
    public static final String SYS_OTHER_DATA_BACKUP_PATH = BACKUP_PATH + "/sys_other_backup";

    public static final String APP_DATA_BACKUP_PATH = BACKUP_PATH + "/app_data";
    public static final String APP_SD_DATA_BACKUP_PATH = BACKUP_PATH + "/app_sd_data";
    public static final String APP_CREATE_SD_BACKUP_PATH = BACKUP_PATH + "/app_sd_create";


    public static final String SYS_DATA_RECEIVED_PATH = RECEIVED_PATH + "/sys_data_recv";
    public static final String SYS_SD_DATA_RECEIVED_PATH = RECEIVED_PATH + "/sd_data_recv";
    public static final String SYS_SQL_DATA_RECEIVED_PATH = RECEIVED_PATH + "/sd_sql_recv";
    public static final String SYS_OTHER_DATA_RECEIVED_PATH = RECEIVED_PATH + "/sys_other_backup";

    public static final String APP_APK_RECEIVED_PATH = RECEIVED_PATH + "/app_apk";
    public static final String APP_SD_DATA_RECEIVED_PATH = RECEIVED_PATH + "/app_sd_data";
    public static final String APP_CREATE_SD_RECEIVED_PATH = RECEIVED_PATH + "/app_sd_create";


    public static final String SYS_CONTACTS_BACKUP_PATH = SYS_SQL_DATA_BACKUP_PATH + "/contacts.vcf";
    public static final String SYS_CALENDAR_BACKUP_PATH = SYS_SQL_DATA_BACKUP_PATH + "/calendar.dat";
    public static final String SYS_SMS_BACKUP_PATH = SYS_SQL_DATA_BACKUP_PATH + "/sms.dat";
    public static final String SYS_MMS_BACKUP_PATH = SYS_SQL_DATA_BACKUP_PATH + "/mms.dat";
    public static final String SYS_CALL_BACKUP_PATH = SYS_SQL_DATA_BACKUP_PATH + "/calllog.dat";
    public static final String SYS_SETTINGS_BACKUP_PATH = SYS_SQL_DATA_BACKUP_PATH + "/settings.dat";

    public static final String SYS_CONTACTS_RECEIVED_PATH = SYS_SQL_DATA_RECEIVED_PATH + "/contacts.vcf";
    public static final String SYS_CALENDAR_RECEIVED_PATH = SYS_SQL_DATA_RECEIVED_PATH + "/calendar.dat";
    public static final String SYS_SMS_RECEIVED_PATH = SYS_SQL_DATA_RECEIVED_PATH + "/sms.dat";
    public static final String SYS_MMS_RECEIVED_PATH = SYS_SQL_DATA_RECEIVED_PATH + "/mms.dat";
    public static final String SYS_CALL_RECEIVED_PATH = SYS_SQL_DATA_RECEIVED_PATH + "/calllog.dat";
    public static final String SYS_SETTINGS_RECEIVED_PATH = SYS_SQL_DATA_RECEIVED_PATH + "/settings.dat";


    public static final String IP = "192.168.43.1";
    public static final String IS_SEND = "IS_SEND";
    public static final String IS_FAIL = "IS_FAIL";

    public static class SizeInfo {
        public String packageName;
        public long size;
    }

    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.2f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.2f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.2f KB", f);
        } else
            return String.format("%d Byte", size);
    }

    /**
     * Judge whether the program is running in the foreground
     * @param context
     * @return
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                //foreground
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }



}
