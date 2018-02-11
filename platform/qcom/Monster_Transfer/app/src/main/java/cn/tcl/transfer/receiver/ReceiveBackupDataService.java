/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.receiver;

import android.app.ActivityManager;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.tct.backupmanager.IBackupManagerService;
import com.tct.backupmanager.IBackupManagerServiceCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.MemoryHandler;

import cn.tcl.transfer.IReceiveCallback;
import cn.tcl.transfer.R;
import cn.tcl.transfer.activity.ReceivingActivity;
import cn.tcl.transfer.data.Calllog.CallLogHelper;
import cn.tcl.transfer.data.calendar.CalendarEventHelper;
import cn.tcl.transfer.data.contact.ContactProcessor;
import cn.tcl.transfer.data.sms.SmsHelper;
import cn.tcl.transfer.systemApp.CalendarSysApp;
import cn.tcl.transfer.systemApp.ContactsSysApp;
import cn.tcl.transfer.systemApp.DialerSysApp;
import cn.tcl.transfer.systemApp.MmsSysApp;
import cn.tcl.transfer.systemApp.SettingsSysApp;
import cn.tcl.transfer.util.AppUtils;
import cn.tcl.transfer.util.FilePathUtils;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.NetWorkUtil;
import cn.tcl.transfer.util.ReceiverItem;
import cn.tcl.transfer.util.Utils;

public class ReceiveBackupDataService extends Service {

private static final String TAG = "ReceiveService";

    private List<Integer> selectItem;
    private String[] SQLItem;
    private Context context;
    private boolean closeThread = true;
    private int startId;

    private static final int PORT = 33333;

    private static final int RESTORE_INSTALL_APK = 1001;
    private static final int RESTORE_INSTALL_APK_COMPLETE = 1002;
    private static final int RESTORE_SYS_OTHER_DATA = 1003;

    private static final int CHECK_RESTORE_COMPLETE = 2003;

    private static final int CONNECT_ERROR = 90000;

    private static final int CHECK_CONNECT = 90001;
    private static final int CLEAR_CHECK_CONNECT = 90002;

    private static final int HEART_BEAT = 44444;


    private static final int RECV_TIMEOUT = 3 * 1000;


    private ServerSocket mServerSocket;
    private Socket mSocket;
    private int tryBindTimes = 0;

    DataOutputStream mOutputStream;
    DataInputStream mInputStream;


    private int mCurrentReceiveType;
    public static boolean isCancelled = false;
    private IBackupManagerService mBackup = null;
    private IBackupManagerServiceCallback mRestoreCallback;

    ConcurrentLinkedQueue<String> mApkList = new ConcurrentLinkedQueue<String>();

    private long totalSysSize = 0;
    private long totalAppSize = 0;

    private volatile boolean needCheckSocket = false;

    public static long startRecvTime = 0;
    public static long mTotalReceiveSize;
    public static long mCurrentReceiveSize;

    List<String> mScanPaths = new ArrayList<String>();

    ConcurrentLinkedQueue<String> mSysOtherRestoreFiles = new ConcurrentLinkedQueue<String>();
    public static HashMap<String, String> mAppBackupList = new HashMap<String, String>();

    private boolean isRestoreSysDataComplete = false;
    private boolean isRestoreSysOtherComplete = false;
    private boolean isRecvComplete = false;
    private boolean isAllComplete = false;

    private boolean isContactRecv = false;
    private boolean isCalendarRecv = false;
    private boolean isCalllogRecv = false;
    private boolean isSmsRecv = false;
    private boolean isMmsRecv = false;
    private boolean isSettingsRecv = false;

    private boolean isContactRestoreComplete = false;
    private boolean isCalendarRestoreComplete = false;
    private boolean isCalllogRestoreComplete = false;
    private boolean isSmsRestoreComplete = false;
    private boolean isMmsRestoreComplete = false;
    private boolean isSettingsRestoreComplete = false;

    public static long mStartRestoreTime = 0;
    public static long mTotalSize = 0;

    private volatile boolean hasHeartBeat = false;
    private Object mStartObject = new Object();

    static {
        mAppBackupList.put("wallpaper", "/data/system/users/0/");
        mAppBackupList.put("wallpaper_lock", "/data/system/users/0/");

        mAppBackupList.put("app_parts", "/data/user_de/0/com.android.providers.telephony/");

        mAppBackupList.put("markedPayApList.xml", "/data/user_de/0/com.android.settings/shared_prefs/");
        mAppBackupList.put("wpa_supplicant.conf", "/data/misc/wifi/");
    }


    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case RESTORE_INSTALL_APK:
                    String apkPath = null;
                    synchronized (mApkList) {
                        if(!mApkList.isEmpty()) {
                            apkPath = mApkList.poll();
                        }
                    }
                    if(apkPath == null) {
                        restoreSysData();
                        return;
                    }
                    File file = new File(apkPath);
                    if (!file.exists()) {
                        return;
                    }
                    if(AppUtils.checkNeedInstall(ReceiveBackupDataService.this, apkPath)) {
                        installSlient(apkPath);
                    } else {
                        Log.e(TAG, apkPath  + " can't be installed!");
                        FilePathUtils.delAllFile(apkPath);
                        sendEmptyMessage(RESTORE_INSTALL_APK);
                    }
                    return;
                case RESTORE_INSTALL_APK_COMPLETE:
                    if(isRecvComplete) {
                        restoreSysData();
                    }
                    return;
                case RESTORE_SYS_OTHER_DATA:
                    if(!mSysOtherRestoreFiles.isEmpty()) {
                        String path = mSysOtherRestoreFiles.poll();
                        restoreSysOtherData1(path);
                    } else {
                        isRestoreSysOtherComplete = true;
                        if(checkRestoreComplete()) {
                            try {
                                if(!isAllComplete) {
                                    isAllComplete = true;
                                    mRestoreCallback.onComplete();
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case CONNECT_ERROR:
                    try {
                        closeSocket();
                        if (mCallback == null) {
                            return;
                        }
                        mCallback.onError(Utils.ERROR_NETWORK, getString(R.string.text_disconnected));
                        stopSelf();
                    } catch (RemoteException e1) {
                        Log.e(TAG, "CONNECT_ERROR", e1);
                    }
                    return;
                case CHECK_RESTORE_COMPLETE:
                    if(checkRestoreComplete()) {
                        try {
                            if(!isAllComplete) {
                                isAllComplete = true;
                                mRestoreCallback.onComplete();
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case CHECK_CONNECT:
                    Log.e(TAG, "CHECK_CONNECT");
                    mHandler.sendEmptyMessage(CONNECT_ERROR);
                    break;
                case HEART_BEAT:
                    synchronized (mStartObject) {
                        if (!hasHeartBeat) {
                            Log.e(TAG, "HEART_BEAT");
                            mHandler.sendEmptyMessage(CONNECT_ERROR);
                            return;
                        }
                        hasHeartBeat = false;
                        mHandler.sendEmptyMessageDelayed(HEART_BEAT, 8000);
                    }
                default:
                    break;
            }

        }
    };

    private boolean checkRestoreComplete() {
        return isRecvComplete
                && (isContactRestoreComplete == isContactRecv)
                && (isCalendarRestoreComplete == isCalendarRecv)
                && (isCalllogRestoreComplete == isCalllogRecv)
                && (isSmsRestoreComplete == isSmsRecv)
                && (isMmsRestoreComplete == isMmsRecv)
                && isRestoreSysDataComplete
                && isRestoreSysOtherComplete;
    }

    private void receiveFileBody(long length, String savePath, float compressionRatio) throws IOException, RemoteException {
        DataOutputStream dos = null;
        int bufferSize = 8192;
        byte[] buf = new byte[bufferSize];
        try {
            File dir = new File(getPath(savePath));
            if(!dir.exists()) {
                dir.mkdirs();
            }
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(savePath)));
            int read = 0;
            long passedlen = 0;
            long speed = 0;
            long startTime = System.currentTimeMillis();
            while (length -  passedlen> 0 ) {
                if(isCancelled) {
                    FilePathUtils.delFile(savePath);
                    return;
                }
                long needReadLen = 0;
                if (length - passedlen > bufferSize) {
                    needReadLen = bufferSize;
                } else {
                    needReadLen = length - passedlen;
                }
                mHandler.sendEmptyMessageDelayed(CHECK_CONNECT, 10 * 1000);
                read = mInputStream.read(buf, 0, (int)needReadLen);
                mHandler.removeMessages(CHECK_CONNECT);
                if (read > 0) {
                    passedlen += read;
                    long currentTime = System.currentTimeMillis();
                    mTotalReceiveSize += (long)(((float)read) * compressionRatio);
                    mCurrentReceiveSize += (long)(((float)read) * compressionRatio);
                    if(currentTime - startTime != 0) {
                        speed = 1000 * passedlen / (currentTime - startTime);
                    } else {
                        speed = NetWorkUtil.DEFAULT_SPEED;
                    }
                    if(mCallback != null) {
                        mCallback.onProgress(mCurrentReceiveType, mCurrentReceiveSize, speed);
                    }
                    dos.write(buf, 0, read);
                } else {
                    Log.e(TAG, "receiveFileBody:" + read);
                    throw new IOException("disconnect");
                }
            }
            LogUtils.v(TAG, "receiveFileBody one file:" + savePath);
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if(mCurrentReceiveType == Utils.CATEGORY_IMAGE
                        || mCurrentReceiveType == Utils.CATEGORY_VIDEO
                        || mCurrentReceiveType == Utils.CATEGORY_AUDIO
                        || mCurrentReceiveType == Utils.CATEGORY_DOCUMENT) {
                    if(!isCancelled) {
                        mScanPaths.add(savePath);
                    }
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "exception", e);
            }
        }
    }

    private void receiveFileBody(long length, String savePath) throws IOException, RemoteException {
        receiveFileBody(length, savePath, 1.0f);
    }

    private boolean receive() {
        try {
            if(isCancelled) {
                return false;
            }
            mHandler.sendEmptyMessageDelayed(CHECK_CONNECT, 2 * 60 * 1000);
            int fileType = (int)mInputStream.readLong();
            mHandler.removeMessages(CHECK_CONNECT);

            if(fileType != NetWorkUtil.TYPE_HEART_BEAT) {
                mHandler.removeMessages(HEART_BEAT);
            }
            switch ( fileType) {
                case NetWorkUtil.TYPE_SIMPLE:
                    return receiveSimpleFile();
                case NetWorkUtil.TYPE_SYS_DATA:
                    receiveSysData();
                    break;
                case NetWorkUtil.TYPE_SYS_SQL_DATA:
                    receiveSysSqlData();
                    break;
                case NetWorkUtil.TYPE_SYS_OTHER_DATA:
                    receiveSimpleFile();
                    break;
                case NetWorkUtil.TYPE_APK:
                    receiveApkData();
                    break;
                case NetWorkUtil.TYPE_APP_DATA:
                    receiveAppData();
                    break;
                case NetWorkUtil.TYPE_APP_SD_DATA:
                    receiveAppSdcardData();
                    break;
                case NetWorkUtil.TYPE_APP_SD_CREATE_DATA:
                    receiveAppSdcardCreatData();
                    break;
                case NetWorkUtil.TYPE_SEND_INFO:
                    receiveDataInfo();
                    break;
                case NetWorkUtil.TYPE_SEND_SIZE:
                    receiveDataSize();
                    startRecvTime = System.currentTimeMillis();
                    break;
                case NetWorkUtil.TYPE_CATEGOY_START:
                    mCurrentReceiveSize = 0;
                    mCurrentReceiveType = (int)readLong();
                    ReceivingActivity.mAllDataList.get(mCurrentReceiveType).recvStatus = ReceivingActivity.RecvItem.RECVING;
                    if(mCallback != null) {
                        mCallback.onStart(mCurrentReceiveType);
                    }
                    break;
                case NetWorkUtil.TYPE_CATEGOY_END:
                    mCurrentReceiveType = (int)readLong();
                    ReceivingActivity.mAllDataList.get(mCurrentReceiveType).recvStatus = ReceivingActivity.RecvItem.RECV_OK;
                    if(mCallback != null) {
                        mCallback.onComplete(mCurrentReceiveType);
                        if (mCurrentReceiveType == ReceiverItem.TYPE_DOCUMENT) {
                            mCallback.onAllComplete();
                        }
                    }
                    break;
                case NetWorkUtil.TYPE_END:
                    isRecvComplete = true;
                    long dataSize = FilePathUtils.caculateFolderSize(Utils.SYS_DATA_RECEIVED_PATH);
                    long otherDataSize = FilePathUtils.caculateFolderSize(Utils.SYS_OTHER_DATA_RECEIVED_PATH);
                    mTotalSize = dataSize + otherDataSize;
                    if(mCallback != null) {
                        mCallback.onAllComplete();
                    }
                    return false;
                case NetWorkUtil.TYPE_HEART_BEAT:
                    synchronized (mStartObject) {
                        hasHeartBeat = true;
                    }
                    break;
                default:
                    break;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "receive EXCEPTION", e);
            mHandler.sendEmptyMessage(CONNECT_ERROR);
            return false;
        }
    }

    private long readLong() throws IOException{
        mHandler.sendEmptyMessageDelayed(CHECK_CONNECT, NetWorkUtil.TIME_OUT);
        long data = mInputStream.readLong();
        mHandler.removeMessages(CHECK_CONNECT);
        return data;
    }

    private String readUTF() throws IOException{
        mHandler.sendEmptyMessageDelayed(CHECK_CONNECT, NetWorkUtil.TIME_OUT);
        String info = mInputStream.readUTF();
        mHandler.removeMessages(CHECK_CONNECT);
        return info;
    }


    private void closeSocket() {
        try {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "sendData", e);
            mSocket = null;
        }
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }
        } catch (Exception e) {
            mOutputStream = null;
            LogUtils.e(TAG, "Exception", e);
        }
        try {
            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }
        } catch (Exception e) {
            mInputStream = null;
            LogUtils.e(TAG, "Exception", e);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mBackup = (IBackupManagerService) getApplicationContext()
                .getSystemService("tctbackup");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    private IReceiveCallback mCallback;

    private final IReceiveInfo.Stub mBinder = new IReceiveInfo.Stub() {

        @Override
        public void cancelReceive() throws RemoteException {
            isCancelled = true;
        }

        @Override
        public void canSendNow() throws RemoteException {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mOutputStream != null) {
                            mOutputStream.writeLong(10000);
                            mOutputStream.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void disConnect() throws RemoteException {

        }

        @Override
        public void registerCallback(IReceiveCallback cb) throws RemoteException {
            mCallback = cb;
        }

        @Override
        public void unregisterCallback(IReceiveCallback cb) throws RemoteException {
            mCallback = null;
        }

        @Override
        public void beginRestore(IBackupManagerServiceCallback cb) {
            mRestoreCallback = cb;
            if(checkAllApkInstalled()) {
                restoreSysData();
            }
        }
    };

    private void restoreSysData() {

        try {
            mStartRestoreTime = System.currentTimeMillis();
            ArrayList<String> tmp = new ArrayList<String>();
            String path = Utils.SYS_DATA_RECEIVED_PATH;
            File dirFile = new File(path);
            if(dirFile.exists() && dirFile.isDirectory()) {
                File[] files = dirFile.listFiles();
                for(File file: files ) {
                    String packageName = file.getName().substring(0, file.getName().lastIndexOf("."));
                    tmp.add(packageName);
                }
            }
            String[] allList = tmp.toArray(new String[0]);
            if(allList.length == 0) {
                isRestoreSysDataComplete = true;
                restoreSysOtherData();
                return;
            }
            mBackup.registerCallback(mRestoreSysCallback);
            mBackup.beginRestore(allList, path, 1);

        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception:", e);
        }
    }

    private IBackupManagerServiceCallback mRestoreSysCallback = new IBackupManagerServiceCallback.Stub() {
        @Override
        public void onStart() throws RemoteException {

        }

        @Override
        public void onComplete() throws RemoteException {
            isRestoreSysDataComplete = true;
            mBackup.unregisterCallback(mRestoreSysCallback);
            restoreSysOtherData();
        }

        @Override
        public void onUpdate(String info) throws RemoteException {
            LogUtils.d(TAG, "onUpdate: " + info);
            if(TextUtils.isEmpty(info) || !info.toLowerCase().startsWith("end:")) {
                return;
            }
            try {
                String[] aa = info.split(":");
                String packageName = aa[1];
                FilePathUtils.delFile(Utils.SYS_DATA_RECEIVED_PATH + "/"+ packageName  +".tar");
            } catch (Exception e) {
                Log.e(TAG, "onUpdate: ", e);
            }

        }

        @Override
        public void onProgress(int i) throws RemoteException {

        }

        @Override
        public void onError(String s) throws RemoteException {

        }
    };

    private void restoreSqlData(String fileName, final int count) {
        if(TextUtils.isEmpty(fileName)) {
            return;
        }
        if(TextUtils.equals(fileName, Utils.SYS_CONTACTS_RECEIVED_PATH)) {
            ContactsSysApp.recvCount = count;
        } else if(TextUtils.equals(fileName, Utils.SYS_CALENDAR_RECEIVED_PATH)) {
            CalendarSysApp.recvCount = count;
        } else if(TextUtils.equals(fileName, Utils.SYS_SMS_RECEIVED_PATH)) {
            MmsSysApp.smsRecvCount = count;
        } else if(TextUtils.equals(fileName, Utils.SYS_MMS_RECEIVED_PATH)) {
            MmsSysApp.mmsRecvCount = count;
        } else if(TextUtils.equals(fileName, Utils.SYS_CALL_RECEIVED_PATH)) {
            DialerSysApp.recvCount = count;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(TextUtils.equals(fileName, Utils.SYS_CONTACTS_RECEIVED_PATH)) {
                    int localCount = 0;
                    ContentResolver resolver = getContentResolver();
                    Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                    try {
                        localCount = cursor.getCount();
                        cursor.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if(cursor != null) {
                            cursor.close();
                        }
                    }
                    ContactsSysApp.localCount = localCount;
                    ContactsSysApp.recvCount = count;
                    ContactProcessor mContactProcessor = new ContactProcessor(getApplicationContext());
                    ContactsSysApp.startTime = System.currentTimeMillis();
                    isContactRecv = true;
                    mContactProcessor.restoreContacts(Utils.SYS_CONTACTS_RECEIVED_PATH, null);
                    isContactRestoreComplete = true;
                    FilePathUtils.delFile(Utils.SYS_CONTACTS_RECEIVED_PATH);
                } else if(TextUtils.equals(fileName, Utils.SYS_CALENDAR_RECEIVED_PATH)) {
                    CalendarSysApp.recvCount = count;
                    CalendarSysApp.startTime = System.currentTimeMillis();
                    CalendarEventHelper mCalendarEventHelper = new CalendarEventHelper(getApplicationContext());
                    isCalendarRecv = true;
                    mCalendarEventHelper.restoreCalendarEvents(Utils.SYS_CALENDAR_RECEIVED_PATH);
                    isCalendarRestoreComplete = true;
                    FilePathUtils.delFile(Utils.SYS_CALENDAR_RECEIVED_PATH);
                } else if(TextUtils.equals(fileName, Utils.SYS_SMS_RECEIVED_PATH)) {
                    MmsSysApp.smsRecvCount = count;
                    MmsSysApp.smsStartTime = System.currentTimeMillis();
                    SmsHelper mSmsHelper = new SmsHelper(getApplicationContext());
                    isSmsRecv = true;
                    mSmsHelper.restoreSms(Utils.SYS_SMS_RECEIVED_PATH);
                    isSmsRestoreComplete = true;
                    FilePathUtils.delFile(Utils.SYS_SMS_RECEIVED_PATH);
                } else if(TextUtils.equals(fileName, Utils.SYS_MMS_RECEIVED_PATH)) {
                    MmsSysApp.mmsRecvCount = count;
                    MmsSysApp.mmsStartTime = System.currentTimeMillis();
                    SmsHelper mSmsHelper = new SmsHelper(getApplicationContext());
                    isMmsRecv = true;
                    Map<String, String> pairName = mSmsHelper.restoreMms(Utils.SYS_MMS_RECEIVED_PATH);
                    isMmsRestoreComplete = true;
                    rename(pairName);
                    FilePathUtils.delFile(Utils.SYS_MMS_RECEIVED_PATH);
                } else if(TextUtils.equals(fileName, Utils.SYS_CALL_RECEIVED_PATH)) {
                    DialerSysApp.recvCount = count;
                    DialerSysApp.startTime = System.currentTimeMillis();
                    CallLogHelper callLogHelper = new CallLogHelper(getApplicationContext());
                    isCalllogRecv = true;
                    callLogHelper.restoreCallLog(Utils.SYS_CALL_RECEIVED_PATH);
                    isCalllogRestoreComplete = true;
                    FilePathUtils.delFile(Utils.SYS_CALL_RECEIVED_PATH);
                }
                mHandler.sendEmptyMessage(CHECK_RESTORE_COMPLETE);
            }
        }).start();
    }

    private void rename(Map<String, String> pairName) {

        if (pairName != null && pairName.size() > 0) {
            for (Map.Entry<String, String> entry : pairName.entrySet()) {

                String oldPath = entry.getKey();
                String newPath = entry.getValue();

                String oldName = oldPath.substring(oldPath.lastIndexOf("/") + 1);
                String newName = newPath.substring(newPath.lastIndexOf("/") + 1);

                String oldDataPath =  Utils.SYS_OTHER_DATA_RECEIVED_PATH + "/app_parts/" + oldName;
                String updatedDataPath = Utils.SYS_OTHER_DATA_RECEIVED_PATH + "/app_parts/" + newName;

                try {
                    if (oldDataPath != null && updatedDataPath != null) {
                        Log.i(TAG, "renamePartDataName: oldDataPath = " + oldDataPath);
                        Log.i(TAG, "renamePartDataName: updatedDataPath = " + updatedDataPath);
                        File oldFile = new File(oldDataPath);
                        File updatedFile = new File(updatedDataPath);

                        if (oldFile.exists() && updatedFile.exists()) {
                            updatedFile.delete();
                            oldFile.renameTo(new File(updatedDataPath));
                        }
                        if (oldFile.exists() && !updatedFile.exists()) {
                            oldFile.renameTo(new File(updatedDataPath));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.i(TAG, "renamePartDataName: map is null or size < 1");
        }

    }

    private void setWallPaper(String path) {
        if(TextUtils.isEmpty(path)) {
            return;
        }
        WallpaperManager mWallManager = WallpaperManager.getInstance(this);
        try {
            InputStream is = new FileInputStream(path);
            mWallManager.setStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setLockedWallPaper(String path) {
        if(TextUtils.isEmpty(path)) {
            return;
        }
        WallpaperManager mWallManager = WallpaperManager.getInstance(this);
        try {
            InputStream is = new FileInputStream(path);
            mWallManager.setStream(is, null, true, WallpaperManager.FLAG_LOCK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreSysOtherData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SettingsSysApp.setData(getApplicationContext());
                    initOther();

                    ArrayList<String> tmp = new ArrayList<String>();

                    String path = mSysOtherRestoreFiles.poll();
                    if(TextUtils.isEmpty(path)) {
                        mRestoreSysOtherCallback.onComplete();
                        return;
                    }
                    File file = new File(path);
                    if (file.exists()) {
                        if(TextUtils.equals(file.getName(), "wallpaper")) {
                            setWallPaper(path);
                            mHandler.sendEmptyMessage(RESTORE_SYS_OTHER_DATA);
                            return;
                        } else if(TextUtils.equals(file.getName(), "wallpaper_lock")) {
                            setLockedWallPaper(path);
                            mHandler.sendEmptyMessage(RESTORE_SYS_OTHER_DATA);
                            return;
                        }
                        tmp.add(path);
                    }
                    String dstPath = mAppBackupList.get(file.getName());
                    if(dstPath == null) {
                        mRestoreSysOtherCallback.onComplete();
                        return;
                    }
                    String[] allList = tmp.toArray(new String[0]);
                    if (allList.length == 0) {
                        mRestoreSysOtherCallback.onComplete();
                        return;
                    }
                    mBackup.registerCallback(mRestoreSysOtherCallback);
                    mBackup.beginRestore(allList, dstPath, 2);

                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException:", e);
                } catch (Exception e) {
                    Log.e(TAG, "Exception:", e);
                }
            }
        }).start();
    }

    private IBackupManagerServiceCallback mRestoreSysOtherCallback = new IBackupManagerServiceCallback.Stub() {
        @Override
        public void onStart() throws RemoteException {

        }

        @Override
        public void onComplete() throws RemoteException {
            mBackup.unregisterCallback(mRestoreSysCallback);
            mHandler.sendEmptyMessage(RESTORE_SYS_OTHER_DATA);
        }

        @Override
        public void onUpdate(String info) throws RemoteException {
            LogUtils.d(TAG, "onUpdate: " + info);

            if(TextUtils.isEmpty(info) || !info.toLowerCase().startsWith("end:")) {
                return;
            }
            try {
                String[] aa = info.split(":");
                String fileName = aa[1];
                FilePathUtils.delFile(fileName);
            } catch (Exception e) {
                Log.e(TAG, "onUpdate: ", e);
            }

        }

        @Override
        public void onProgress(int i) throws RemoteException {

        }

        @Override
        public void onError(String s) throws RemoteException {

        }
    };

    private void initOther() {

        File otherFileDir = new File(Utils.SYS_OTHER_DATA_RECEIVED_PATH);
        if(!otherFileDir.exists()) {
            return;
        }
        File[] files =  otherFileDir.listFiles();
        for(File file : files) {
            mSysOtherRestoreFiles.add(file.getAbsolutePath());
        }
    }

    private void restoreSysOtherData1(String path) {
        try {
            ArrayList<String> tmp = new ArrayList<String>();
            File file = new File(path);
            if (!file.exists()) {
                mRestoreSysOtherCallback.onComplete();
                return;
            }
            String dstPath = mAppBackupList.get(file.getName());
            if(dstPath == null) {
                mRestoreSysOtherCallback.onComplete();
                return;
            }
            if(TextUtils.equals(file.getName(), "wallpaper")) {
                setWallPaper(path);
                mHandler.sendEmptyMessage(RESTORE_SYS_OTHER_DATA);
                return;
            } else if(TextUtils.equals(file.getName(), "wallpaper_lock")) {
                setLockedWallPaper(path);
                mHandler.sendEmptyMessage(RESTORE_SYS_OTHER_DATA);
                return;
            }
            tmp.add(path);
            String[] allList = tmp.toArray(new String[0]);
            if (allList.length == 0) {
                mRestoreSysOtherCallback.onComplete();
                return;
            }
            mBackup.registerCallback(mRestoreSysOtherCallback);
            mBackup.beginRestore(allList, dstPath, 2);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception:", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.startId = startId;
        if (intent != null) {
            selectItem = intent.getIntegerArrayListExtra("SelectItem");
            SQLItem = intent.getStringArrayExtra("SQLItem");
            closeThread = intent.getBooleanExtra("closeThread", true);
            int i;
            if (closeThread) {
                i = 1;
            } else {
                i = 0;
            }
            LogUtils.d("FAI", "icloseThread ............." + i);
        }
        context = this;
        bingToServerPort(PORT);
        return START_NOT_STICKY;
    }


    public void onDestroy() {
        super.onDestroy();
        LogUtils.v(TAG, "onDestroy");
        //stopSelf();
        //stopServerSocket();
        //shutdownThreadPool();
        stopSelf(startId);

    }

    private void bingToServerPort(final int port){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket(port);

                    while (closeThread) {
                        mSocket = mServerSocket.accept();
                        if(mCallback != null) {
                            mCallback.onConnected();
                        }
                        mInputStream = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));
                        mOutputStream = new DataOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));
                        LogUtils.v(TAG, "bingToServerPort");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mHandler.sendEmptyMessageDelayed(HEART_BEAT, 10 * 1000);
                                while (receive()) {
                                    LogUtils.d(TAG, "receiveSimpleFile one file");
                                }
                                if(isCancelled) {
                                    Log.e(TAG, "bingToServerPort");
                                    mHandler.sendEmptyMessage(CONNECT_ERROR);
                                }
                                closeSocket();
                                scanPathforMediaStore();
                                ReceiveBackupDataService.this.stopSelf();
                            }
                        }).start();
                        mServerSocket.close();
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "bingToServerPort error",e);
                    tryBindTimes = tryBindTimes + 1;
                    int port1 = port + tryBindTimes;
                    if (tryBindTimes >= 20) {
                        Log.e(TAG, "pls use other port to bind");
                        return;
                    }
                    //bingToServerPort(port1);
                }
            }
        }).start();

    }

    public void scanPathforMediaStore() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!mScanPaths.isEmpty()) {
                    String[] paths = new String[mScanPaths.size()];
                    mScanPaths.toArray(paths);
                    MediaScannerConnection.scanFile(getApplicationContext(), paths, null, null);
                }
            }
        }).start();
    }

    private boolean receiveSimpleFile() throws IOException, RemoteException {
        if(isCancelled) {
            return false;
        }
        String savePath = readUTF();
        long length = readLong();
        updateRecvInfo();
        receiveFileBody(length, savePath);
        LogUtils.v(TAG, "receiveSimpleFile one file:" + savePath);
        return true;
    }

    private boolean receiveSysData() throws IOException, RemoteException {
        if(isCancelled) {
            return false;
        }
        String savePath = readUTF();
        long length = readLong();
        updateRecvInfo();
        long originSize = readLong();
        totalSysSize += length;

        String fileName = getName(savePath);
        if(TextUtils.equals(fileName, "com.monster.launcher.tar")) {
            ReceivingActivity.hasLauncher = true;
        }

        float compressionRatio = (float)originSize/(float)length;
        receiveFileBody(length, Utils.SYS_DATA_RECEIVED_PATH + "/" +fileName, compressionRatio);
        LogUtils.v(TAG, "receiveSysData one file:" + savePath
                + "   length=" + length
                + "   originSize=" + originSize
                + "  totalSysSize=" + totalSysSize);
        return true;
    }

    private boolean receiveSysSqlData() throws IOException, RemoteException {
        if(isCancelled) {
            return false;
        }
        String savePath = readUTF();
        long length = readLong();
        updateRecvInfo();
        int count = (int)readLong();
        String fileName = getName(savePath);
        receiveFileBody(length, savePath);
        LogUtils.v(TAG, "receiveSysSqlData one file:" + savePath);
        restoreSqlData(savePath, count);
        return true;
    }

    private boolean receiveApkData() throws IOException, RemoteException {
        if(isCancelled) {
            return false;
        }
        String savePath = readUTF();
        long length = readLong();
        updateRecvInfo();
        totalAppSize += length;
        String fileName = getName(savePath);
        receiveFileBody(length, Utils.APP_APK_RECEIVED_PATH + "/" + fileName);

        File file = new File(Utils.APP_APK_RECEIVED_PATH + "/" + fileName);
        if(!file.exists()) {
            return false;
        }
        synchronized (mApkList) {
            mApkList.add(file.getAbsolutePath());
        }
        String apkPath = mApkList.poll();
        if(AppUtils.checkNeedInstall(this, apkPath)) {
            installSlient(apkPath);
        } else {
            FilePathUtils.delFile(apkPath);
            mApkList.remove(apkPath);
            Log.e(TAG, fileName  + " can't be installed!");
        }
        LogUtils.v(TAG, "receiveApkData one file:" + savePath+ "   length=" + length + "  totalSysSize=" + totalAppSize);
        return true;
    }

    private boolean receiveAppData() throws IOException, RemoteException {
        if(isCancelled) {
            return false;
        }
        String savePath = readUTF();
        long length = readLong();
        updateRecvInfo();
        long originSize = readLong();
        float compressionRatio = (float)originSize/(float)length;

        totalAppSize += length;

        String fileName = getName(savePath);
        receiveFileBody(length, Utils.SYS_DATA_RECEIVED_PATH + "/" + fileName, compressionRatio);
        LogUtils.v(TAG, "receiveAppData one file:" + savePath+ "   length=" + length + "  totalSysSize=" + totalAppSize);
        return true;
    }

    private boolean receiveAppSdcardData() throws IOException, RemoteException {
        if(isCancelled) {
            return false;
        }
        String savePath = readUTF();
        long length = readLong();
        updateRecvInfo();
        String fileName = getName(savePath);
        receiveFileBody(length, Utils.APP_SD_DATA_RECEIVED_PATH + "/" + fileName);
        LogUtils.v(TAG, "receiveAppSdcardData one file:" + savePath);
        return true;
    }

    private boolean receiveAppSdcardCreatData() throws IOException, RemoteException {
        if(isCancelled) {
            return false;
        }
        String savePath = readUTF();
        long length = readLong();
        updateRecvInfo();
        String fileName = getName(savePath);
        receiveFileBody(length, Utils.APP_CREATE_SD_RECEIVED_PATH + "/" + fileName);
        LogUtils.v(TAG, "receiveAppSdcardCreatData one file:" + savePath);
        return true;
    }

    private void updateDataInfo(String info) {
        try {
            JSONObject json = new JSONObject(info);
//            long sysSize = Long.parseLong((String)json.get(Utils.CATEGORY_SYS_SIZE));
//            long appSize = Long.parseLong((String)json.get(Utils.CATEGORY_APP_SIZE));
//            long imageSize = Long.parseLong((String)json.get(Utils.CATEGORY_IMAGE_SIZE));
//            long videoSize = Long.parseLong((String)json.get(Utils.CATEGORY_VIDEO_SIZE));
//            long audioSize = Long.parseLong((String)json.get(Utils.CATEGORY_AUDIO_SIZE));
//            long docSize = Long.parseLong((String)json.get(Utils.CATEGORY_DOCUMENT_SIZE));
            if(mCallback != null) {
                mCallback.onReceiveDataSize(info);
            }
        } catch (JSONException e) {
            LogUtils.e(TAG, "updateDataInfo", e);
        } catch (RemoteException e) {
            LogUtils.e(TAG, "updateDataInfo", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "updateDataInfo", e);
        }
    }

    private boolean receiveDataSize() {
        try {
            String info = readUTF();
            LogUtils.v(TAG, "receiveDataSize: " + info);
            updateDataInfo(info);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "receiveDataSize", e);
            try {
                if (mSocket != null) {
                    mSocket.close();
                }
                if (mInputStream != null) {
                    mInputStream.close();
                }
                if (mOutputStream != null) {
                    mOutputStream.close();
                }
            } catch (Exception ee) {
                LogUtils.e(TAG, "exception", ee);
            }
            return false;
        }
    }

    private boolean receiveDataInfo() {
        DataOutputStream dos = null;

        int bufferSize = 8192;
        byte[] buf = new byte[bufferSize];

        try {
            String info = readUTF();
            updateDataInfo(info);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "receiveDataInfo", e);
            try {
                if (mSocket != null) {
                    mSocket.close();
                }
                if (mInputStream != null) {
                    mInputStream.close();
                }
                if (mOutputStream != null) {
                    mOutputStream.close();
                }
            } catch (Exception ee) {
                LogUtils.e(TAG, "exception", ee);
            }
            return false;
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "exception", e);
            }
        }
    }

    private void restoreApkData() {
        try {

            File apkDir = new File(Utils.APP_APK_RECEIVED_PATH);
            if(!apkDir.exists()) {
                restoreSysData();
                return;
            }
            File[] apks = apkDir.listFiles();
            if(apks == null || apks.length == 0) {
                restoreSysData();
                return;
            }
            synchronized (mApkList) {
                for (File apk : apks) {
                    mApkList.add(apk.getAbsolutePath());
                }
            }
            String apkPath = mApkList.poll();
            File file = new File(apkPath);
            if(!file.exists()) {
                return;
            }
            if(AppUtils.checkNeedInstall(this, apkPath)) {
                installSlient(apkPath);
            } else {
                FilePathUtils.delFile(apkPath);
                mHandler.sendEmptyMessage(RESTORE_INSTALL_APK);
                Log.e(TAG, apkPath  + " can't be installed!");
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception:", e);
        }
    }

    private IPackageInstallObserver2 mApkInstallCallback = new IPackageInstallObserver2.Stub() {
        @Override
        public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) throws RemoteException {
            FilePathUtils.delFile(Utils.APP_APK_RECEIVED_PATH + "/" + basePackageName + ".apk");
            synchronized (mApkList) {
                if(mRestoreCallback != null) {
                    mRestoreCallback.onUpdate("apk end:" + Utils.APP_APK_RECEIVED_PATH + "/" + basePackageName + ".apk");
                }
                if(checkAllApkInstalled()) {
                    mHandler.sendEmptyMessage(RESTORE_INSTALL_APK_COMPLETE);
                }
            }
        }

        @Override
        public void onUserActionRequired(Intent intent) throws RemoteException {

        }
    };

    private void installSlient(String apkPath) {
        try {
            if(TextUtils.isEmpty(apkPath)) {
                return;
            }
            Class<?> pmService;
            Class<?> activityTherad;
            Method method;

            activityTherad = Class.forName("android.app.ActivityThread");
            Class<?> paramTypes[] = getParamTypes(activityTherad , "getPackageManager");
            method = activityTherad.getMethod("getPackageManager", paramTypes);
            Object PackageManagerService = method.invoke(activityTherad);

            pmService = PackageManagerService.getClass();
            getPackageManager();

            Class<?> paramTypes1[] = getParamTypes(pmService , "installPackageAsUser");
            method = pmService.getMethod("installPackageAsUser", paramTypes1);
            method.invoke(PackageManagerService , apkPath , mApkInstallCallback , PackageManager.INSTALL_REPLACE_EXISTING , null, 0);
        } catch (Exception e) {
            Log.e(TAG, "installSlient", e);
            FilePathUtils.deleteFile(apkPath);
        }
    }

    String getPath(String file) {
        return file.substring(0, file.lastIndexOf("/") + 1);
    }

    String getName(String file) {
        return file.substring(file.lastIndexOf("/") + 1);
    }


    private static void stop(Context context, final String pkgName) {
        if(context == null || TextUtils.isEmpty(pkgName)) {
            return;
        }
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        Method m = null;
        try {
            Class c = Class.forName("android.app.ActivityManager");
            Class<?> paramTypes[] = getParamTypes(c , "forceStopPackageAsUser");
            m = c.getMethod("forceStopPackageAsUser", paramTypes);
            m.invoke(am, pkgName, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Class<?>[] getParamTypes(Class<?> cls, String mName) {
        Class<?> cs[] = null;

        Method[] mtd = cls.getMethods();

        for (int i = 0; i < mtd.length; i++) {
            if (!mtd[i].getName().equals(mName)) {
                continue;
            }
            cs = mtd[i].getParameterTypes();
        }
        return cs;
    }


    private void updateRecvInfo() throws IOException, RemoteException{
        String sendInfo = readUTF();
        long leftTime = readLong();
        ReceivingActivity.mAllDataList.get(mCurrentReceiveType).currentSendInfo = sendInfo;
        if(mCallback != null) {
            mCallback.onFileBeginRecv(mCurrentReceiveType, sendInfo);
        }
    }

    private boolean checkAllApkInstalled() {
        if(isRecvComplete && mApkList.isEmpty()){
            return true;
        }
        return false;
    }

}

