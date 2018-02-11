/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;

import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Telephony;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.tct.backupmanager.IBackupManagerService;
import com.tct.backupmanager.IBackupManagerServiceCallback;

import java.io.File;
import java.lang.ref.PhantomReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import cn.tcl.transfer.File_Exchange;
import cn.tcl.transfer.activity.CategoryListActivity;
import cn.tcl.transfer.app.AppSendManager;
import cn.tcl.transfer.app.BaseApp;
import cn.tcl.transfer.data.Calllog.CallLogHelper;
import cn.tcl.transfer.data.calendar.CalendarEventHelper;
import cn.tcl.transfer.data.contact.ContactProcessor;
import cn.tcl.transfer.data.sms.SmsHelper;
import cn.tcl.transfer.systemApp.SettingsSysApp;
import cn.tcl.transfer.systemApp.SysBaseApp;


public class CalculateSizeTask extends AsyncTask<Void, Void, Void>{

    private static final String TAG = "CalculateSizeTask";

    private ArrayList<Utils.SizeInfo> mlist = new ArrayList<Utils.SizeInfo>();
    private HashMap<String, PackageDetailInfo> mSizeInfoList;
    private Context mContext;
    CalculateSizeCallback mCalculateSizeCallback;

    public CalculateSizeTask(final Context context, HashMap<String, PackageDetailInfo> hashMap, CalculateSizeCallback calculateSizeCallback) {
        mSizeInfoList = hashMap;
        mContext = context;
        mCalculateSizeCallback = calculateSizeCallback;
    }


    @Override
    protected Void doInBackground(Void... params) {
        calculateDirectSize();
        backupSysOtherData();
        return null;
    }

    private void backupContactsData() {
        ContactProcessor mContactProcessor = new ContactProcessor(mContext);
        mContactProcessor.backupContacts(Utils.SYS_CONTACTS_BACKUP_PATH);
    }

    private void backupCalendarData() {
        CalendarEventHelper mCalendarEventHelper = new CalendarEventHelper(mContext);
        mCalendarEventHelper.backupCalendarEvents(Utils.SYS_CALENDAR_BACKUP_PATH);
    }

    private void backupMmsData() {
        String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(mContext);
        SmsHelper mSmsHelper = new SmsHelper(mContext);

        mSmsHelper.backupSms(Utils.SYS_SMS_BACKUP_PATH);
        mSmsHelper.backupMms(Utils.SYS_MMS_BACKUP_PATH);
    }

    private void backupCalllogData() {
        CallLogHelper callLogHelper = new CallLogHelper(mContext);
        callLogHelper.backupCallLog(Utils.SYS_CALL_BACKUP_PATH);
    }


    /**
     * GET Android Native App's cache, data and app sysDataSize
     *
     * @param context
     * @param pkgName
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void getPkgSize(final Context context, final String pkgName, final CalculateSizeCallback callback) throws NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        Method method = PackageManager.class.getMethod("getPackageSizeInfo",
                new Class[] { String.class, IPackageStatsObserver.class });
        method.invoke(context.getPackageManager(), new Object[] {
                pkgName,
                new IPackageStatsObserver.Stub() {
                    @Override
                    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                        long totalSize = pStats.codeSize + pStats.dataSize + pStats.externalCodeSize + pStats.externalDataSize;
                        LogUtils.d(TAG, pkgName
                                + "   codeSize:" + pStats.codeSize
                                + "   dataSize:" + pStats.dataSize
                                + "   cacheSize:" + pStats.cacheSize
                                + "   externalCodeSize:" + pStats.externalCodeSize
                                + "   externalDataSize:" + pStats.externalDataSize
                                + "   externalCacheSize:" + pStats.externalCacheSize
                                + "   externalMediaSize:" + pStats.externalMediaSize
                                + "   externalObbSize:" + pStats.externalObbSize

                        );
                        long externalDataSize = 0;

                        long apkSize = 0;
                        if(DataManager.isThirdApp(context, pkgName)) {
                            externalDataSize = FilePathUtils.getDirSizeFromPath(Environment.getExternalStorageDirectory() + "/Android/data/" + pkgName);
                            externalDataSize += calculateAppCreatedDirSize(pkgName);
                            try {
                                if (DataManager.mSizeInfo.containsKey(pkgName)) {
                                    String apkPath = DataManager.mSizeInfo.get(pkgName).packageInfo.applicationInfo.sourceDir;
                                    File apkFile = new File(apkPath);
                                    apkSize = apkFile.length();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "apkSize: " , e);
                                apkSize = 0;
                            }
                        } else {
                            externalDataSize = pStats.externalDataSize;
                            if(TextUtils.equals(pkgName, "com.monster.launcher")) {
                                externalDataSize += FilePathUtils.getDirSizeFromPath(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/wallpaper");
                            }
                            externalDataSize += calculateSysCreatedDirSize(pkgName);
                        }
                        LogUtils.d(TAG, pkgName + "    pStats.dataSize:" + pStats.dataSize
                                + "  externalDataSize:" + externalDataSize + "  apkSize:" + apkSize);
                        if(callback != null) {
                            Log.e(TAG, pkgName
                                    + "   dataSize:" + pStats.dataSize
                                    + "   externalDataSize:" + externalDataSize
                                    + "   apkSize:" + apkSize);
                            callback.onUpdate(pkgName, pStats.dataSize, externalDataSize, apkSize);
                        }
                    }
                }
        });
    }

    private static long calculateAppCreatedDirSize(final String pkgName) {
        long size = 0;

        AppSendManager appSendManager = AppSendManager.getInstance();
        BaseApp app = appSendManager.getAppByName(pkgName);

        size = app.calculateCreatedDirSize();
        return size;
    }

    private static long calculateSysCreatedDirSize(final String pkgName) {
        long size = 0;

        AppSendManager appSendManager = AppSendManager.getInstance();
        SysBaseApp app = appSendManager.getSysAppByName(pkgName);

        size = app.calculateCreatedDirSize();
        return size;
    }


    public interface CalculateSizeCallback {
        void onUpdate(final String pkgName, long size, long extenalSize, long apkSize);
    }

    IBackupManagerService mBackup = null;
    private void backupSysOtherData() {
        try {
            LogUtils.d(TAG, "beginBack");
            mBackup = (IBackupManagerService)mContext.getApplicationContext()
                    .getSystemService("tctbackup");
            ArrayList<String> tmp = new ArrayList<String>();
            for(String packageName : DataManager.mSelectSysApps) {
                SysBaseApp app = AppSendManager.getInstance().getSysAppByName(packageName);
                tmp.addAll(app.getSysOtherFiles());
            }
            String[] allList = tmp.toArray(new String[0]);
            String path = Utils.SYS_OTHER_DATA_BACKUP_PATH;
            File file = new File(path);
            if(!file.exists()) {
                file.mkdirs();
            }
            mBackup.registerCallback(mBackupSysOtherCallback);
            mBackup.beginBack(allList, path, 2);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:", e);
        }
    }

    private IBackupManagerServiceCallback mBackupSysOtherCallback = new IBackupManagerServiceCallback.Stub() {

        @Override
        public void onStart() throws RemoteException {
            LogUtils.d(TAG, "onStart");
        }

        @Override
        public void onComplete() throws RemoteException {
            LogUtils.d(TAG, "onComplete");
            if (mBackup != null) {
                mBackup.unregisterCallback(mBackupSysOtherCallback);
            }

            for (String packageName: mSizeInfoList.keySet()) {
                long size = 0;
                if(TextUtils.equals("com.android.contacts", packageName)
                        || TextUtils.equals("com.android.mms", packageName)
                        || TextUtils.equals("com.android.calendar", packageName)
                        || TextUtils.equals("com.android.dialer", packageName)) {
                    continue;
                }

                if(TextUtils.equals("com.android.settings", packageName)) {
                    SettingsSysApp.getData(mContext);
                    File settingsFile = new File(Utils.SYS_SETTINGS_BACKUP_PATH);
                    if(settingsFile.exists() && settingsFile.length() > 0) {
                        size = settingsFile.length();
                    }
                    size += FilePathUtils.getDirSizeFromPath(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/markedPayApList.xml");
                    size += FilePathUtils.getDirSizeFromPath(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/wpa_supplicant.conf");

                    mCalculateSizeCallback.onUpdate("com.android.settings", size, 0, 0);
                    continue;
                }

                mSizeInfoList.get(packageName);
                try {
                    getPkgSize(mContext, packageName, mCalculateSizeCallback);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onUpdate(String test) throws RemoteException {
            LogUtils.d(TAG, "onUpdate: " + test);
        }

        @Override
        public void onProgress(int progress) throws RemoteException {
            LogUtils.d(TAG, "onProgress: " + progress);
        }

        @Override
        public void onError(String error) throws RemoteException {
            LogUtils.d(TAG, "error: " + error);
        }
    };


    private void calculateDirectSize() {


        new Thread(new Runnable() {
            @Override
            public void run() {
                long size = 0;
                backupContactsData();
                File contactFile = new File(Utils.SYS_CONTACTS_BACKUP_PATH);
                if(contactFile.exists()) {
                    size = contactFile.length();
                }
                mCalculateSizeCallback.onUpdate("com.android.contacts", size, 0, 0);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                long size = 0;
                backupMmsData();
                File smsfile = new File(Utils.SYS_SMS_BACKUP_PATH);
                if(smsfile.exists()) {
                    size = smsfile.length();
                }
                File mmsfile = new File(Utils.SYS_MMS_BACKUP_PATH);
                if(mmsfile.exists()) {
                    size += mmsfile.length();
                }
                Cursor cursor = null;
                try {
                    cursor = mContext.getContentResolver().query(Uri.parse("content://mms"), new String[]{"sum(m_size)"}, null, null, null);
                    if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                        size += cursor.getLong(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(cursor != null) {
                        cursor.close();
                    }
                }
                mCalculateSizeCallback.onUpdate("com.android.mms", size, 0, 0);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                long size = 0;
                backupCalendarData();
                File calendarFile = new File(Utils.SYS_CALENDAR_BACKUP_PATH);
                if(calendarFile.exists()) {
                    size = calendarFile.length();
                }
                mCalculateSizeCallback.onUpdate("com.android.calendar", size, 0, 0);

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                long size = 0;
                backupCalllogData();
                File dialerFile = new File(Utils.SYS_CALL_BACKUP_PATH);
                if(dialerFile.exists() && dialerFile.length() > 0) {
                    size = dialerFile.length();
                }
                mCalculateSizeCallback.onUpdate("com.android.dialer", size, 0, 0);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                long audioSize = FilePathUtils.getFileSize(File_Exchange.TYPE_AUDIO, mContext);
                if(mCalculateSizeCallback != null) {
                    mCalculateSizeCallback.onUpdate("audio", 0, audioSize, 0);
                }

                long videoSize = FilePathUtils.getFileSize(File_Exchange.TYPE_VIDEO, mContext);
                if(mCalculateSizeCallback != null) {
                    mCalculateSizeCallback.onUpdate("video", 0, videoSize, 0);
                }
                long imageSize = FilePathUtils.getFileSize(File_Exchange.TYPE_IMAGE, mContext);
                if(mCalculateSizeCallback != null) {
                    mCalculateSizeCallback.onUpdate("image", 0, imageSize, 0);
                }
                long documentSize = FilePathUtils.getFileSize(File_Exchange.TYPE_DOCUMENT, mContext);
                if(mCalculateSizeCallback != null) {
                    mCalculateSizeCallback.onUpdate("document", 0, documentSize, 0);
                }
            }
        }).start();
    }

}
