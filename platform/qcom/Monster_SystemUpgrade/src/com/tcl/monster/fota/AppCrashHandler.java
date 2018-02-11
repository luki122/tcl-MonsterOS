package com.tcl.monster.fota;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.tcl.monster.fota.downloadengine.DownloadEngine;
import com.tcl.monster.fota.downloadengine.DownloadTask;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.utils.AESUtil;
import com.tcl.monster.fota.utils.FileUtil;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaPref;
import com.tcl.monster.fota.utils.FotaUtil;

public class AppCrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "AppCrashHandler";

    /**
     * System default exception handler .
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;

    private static AppCrashHandler sInstance = new AppCrashHandler();

    /**
     * Make this private .
     */
    private AppCrashHandler() {
    }

    /**
     * Singleton
     */
    public static AppCrashHandler getInstance() {
        return sInstance;
    }

    /**
     * Init method obtain default handler. Set default uncaught exception
     * handler to this class instance.
     * 
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * Override this method to handle exception .
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
		FotaLog.i(TAG, Log.getStackTraceString(ex));

        //TODO 修改AES
		if (mDefaultHandler != null) {
//        if (!handleException(ex) && mDefaultHandler != null) {
            // Let the system default handler to handle it if we don't handle
            // it.
            mDefaultHandler.uncaughtException(thread, ex);
        }

    }

    /**
     * We handle crash this here.
     * 
     * @param ex
     * @return true if we handle false otherwise.
     */
    public boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }
        FotaLog.v(Log.getStackTraceString(ex));
        // Intent intent = new Intent(mContext, FotaClientActivity.class);
        // FotaApp app = (FotaApp)mContext.getApplicationContext();
        // if(app.isMainActivityActive()){
        // Intent intent = mContext.getPackageManager()
        // .getLaunchIntentForPackage(mContext.getPackageName());
        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
        // | Intent.FLAG_ACTIVITY_NEW_TASK
        // | Intent.FLAG_ACTIVITY_NO_HISTORY);
        //
        // // We define an alarm to launch our new intent in very little time
        // PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
        // intent, 0);
        // AlarmManager alarmManager = (AlarmManager) mContext
        // .getSystemService(Context.ALARM_SERVICE);
        // alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 800,
        // pendingIntent);
        //
        // }
        new Thread() {
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext,
                        mContext.getText(R.string.toast_show_crash_info),
                        Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        new Thread() {
            public void run() {
                try {
                    saveCrashInfo2File(ex);

                } catch (Exception e) {
                }
                // commit suicide
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }.start();
        return true;
    }

    /**
     * Collect information about the task.
     * 
     * @return
     */
    private String getDownloadTaskInfo() {
        String downloadId = FotaPref.getInstance(mContext).getString(FotaConstants.DOWNLOAD_ID, "");

        DownloadEngine downloadEngine = DownloadEngine.getInstance();

        downloadEngine.init(mContext);

        DownloadTask currentTask = downloadEngine.findDownloadTaskByTaskId(
                downloadId);
        StringBuffer sb = new StringBuffer();
        sb.append("----------Download Task Info-----------\n");
        if (currentTask == null) {
            sb.append("there is no active download task " + "\n");
        } else {
            sb.append("Task State :" + currentTask.getState() + "\n");
            sb.append("Task finished size :" + currentTask.getCurrentBytes() + "\n");
            sb.append("Update package info :" + currentTask.getUpdateInfoJson() + "\n");
            sb.append("Download info :" + currentTask.getDownloadInfoJson() + "\n");
        }
        return sb.toString();
    }

    private void shrinkFileSizeIfNeed(File crashLogFile) {
        long fileLength = crashLogFile.length();
        FotaLog.v(TAG, "shrinkFileSizeIfNeed:" + fileLength);
        if (fileLength > 10 * FotaConstants.LOG_FILE_SIZE) {
            // avoid OOM error .
            crashLogFile.delete();
            return;
        }
        if (fileLength >= FotaConstants.LOG_FILE_SIZE) {
            File tmp = new File(crashLogFile.getAbsolutePath() + ".tmp");
            FileReader reader;
            List<String> logs;
            try {
                reader = new FileReader(crashLogFile);
                logs = FileUtil.readLines(reader);
                int lines = logs.size();
                for (int i = 0; i < 12; i++) {
                    try {
                        BufferedWriter buf = new BufferedWriter(new FileWriter(
                                tmp, true));
                        buf.append(logs.get(i));
                        buf.newLine();
                        buf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        break;
                    }
                }
                for (int i = lines / 2; i < lines; i++) {
                    try {
                        BufferedWriter buf = new BufferedWriter(new FileWriter(
                                tmp, true));
                        buf.append(logs.get(i));
                        buf.newLine();
                        buf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        break;
                    }
                }
                crashLogFile.delete();
                tmp.renameTo(crashLogFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save crash info to crash.log
     * 
     * @param ex
     */
    private void saveCrashInfo2File(Throwable ex) {
        File crashLogFile = FotaUtil.crashLog();
        String versionName = "";
        String versionCode = "";
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                versionCode = pi.versionCode + "";
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        StringBuffer sb = new StringBuffer();
        if (!crashLogFile.exists()) {
            sb.append("----------Android Info-----------\n");
            sb.append("Current version:" + FotaUtil.currentVersion(mContext) + "\n");
            sb.append("IMEI:" + FotaUtil.IMEI(mContext) + "\n");
            sb.append("REF:" + FotaUtil.REF() + "\n\n\n");

            sb.append("----------FotaApp Info-----------\n");
            sb.append("App package name:" + mContext.getApplicationInfo().packageName + "\n");
            sb.append("App version name:" + versionName + "\n");
            sb.append("App version code:" + versionCode + "\n\n\n");
        }

        sb.append("----------Crash Info-----------\n");
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String timestamp = sdf.format(cal.getTime());
        sb.append("Crash happened at :" + timestamp + "\n");
        String result = Log.getStackTraceString(ex);
        sb.append(result);
        sb.append("\n");
        sb.append(getDownloadTaskInfo());

        sb.append("--------------------------------\n\n\n");
        String encrypt = AESUtil.encrypt(FotaUtil.appendTail(), sb.toString());
        try {
            FileOutputStream fos = new FileOutputStream(crashLogFile, true);
            fos.write(encrypt.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long len = crashLogFile.length();
        while (len > FotaConstants.LOG_FILE_SIZE) {
            shrinkFileSizeIfNeed(crashLogFile);
            len = crashLogFile.length();
        }
    }
}
