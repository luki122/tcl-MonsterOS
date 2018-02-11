/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.send;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.tct.backupmanager.IBackupManagerService;
import com.tct.backupmanager.IBackupManagerServiceCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.tcl.transfer.File_Exchange;
import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.R;
import cn.tcl.transfer.app.AppSendManager;
import cn.tcl.transfer.app.BaseApp;
import cn.tcl.transfer.data.Calllog.CallLogHelper;
import cn.tcl.transfer.data.calendar.CalendarEventHelper;
import cn.tcl.transfer.data.contact.ContactProcessor;
import cn.tcl.transfer.data.sms.SmsHelper;
import cn.tcl.transfer.operator.wifi.APAdmin;
import cn.tcl.transfer.operator.wifi.Util;
import cn.tcl.transfer.systemApp.SettingsSysApp;
import cn.tcl.transfer.systemApp.SysBaseApp;
import cn.tcl.transfer.util.DataManager;
import cn.tcl.transfer.util.FilePathUtils;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.NetWorkUtil;
import cn.tcl.transfer.util.Utils;

public class SendBackupDataService extends Service {

    private static final String TAG = "SendService";
    private static final int PORT = 33333;
    private static final String IP = Utils.IP;

    private ArrayList<String> mPictureList = new ArrayList<String>();
    private ArrayList<String> mVideoList = new ArrayList<String>();
    private ArrayList<String> mAudioList = new ArrayList<String>();
    private ArrayList<String> mDocumentList = new ArrayList<String>();

    private ArrayList<String> appList = new ArrayList<String>();
    private ArrayList<String> appPackageList;
    private ArrayList<String> systemList;
    private File[] sendSqltoFile;
    private ArrayList<Integer> selectItem = new ArrayList<Integer>();
    private ArrayList<String> messageList = new ArrayList<String>();
    private ArrayList<String> calenderList = new ArrayList<String>();
    private ArrayList<String> contactList = new ArrayList<String>();
    private ArrayList<String> calllogList = new ArrayList<String>();
    private ArrayList<String> settingList = new ArrayList<String>();
    private Context context;
    private APAdmin apadmin;
    private int retryFlag;

    private Socket mSocket = null;
    DataOutputStream mOutputStream;
    DataInputStream mInputStream;

    public static int mCurrentSendType;
    public static volatile boolean isCancelled = false;

    ConcurrentLinkedQueue<String> mSysReadyToSendFiles = new ConcurrentLinkedQueue<String>();
    ConcurrentLinkedQueue<String> mAppReadyToSendFiles = new ConcurrentLinkedQueue<String>();
    private boolean isSysBackupComplete = false;
    private boolean isAppBackupComplete = false;

    public static long startSendTime = 0;

    public static long mTotalSize = 0;
    public static long mCurrentTotalSendSize = 0;
    public static long mCurrentSendSize = 0;

    private static final int BACKUP_SYS_DATA = 50000;
    private static final int BACKUP_SYS_SQL_DATA = 50001;
    private static final int BACKUP_SYS_OTHER_DATA = 50002;

    private static final int BACKUP_APP_DATA = 50003;
    private static final int BACKUP_APP_OTHER_DATA = 50004;

    private volatile boolean isStartSend = false;

    private Object mStartObject = new Object();

    public static long getLeftTime() {
        long leftSize = SendBackupDataService.mTotalSize - SendBackupDataService.mCurrentTotalSendSize;
        long spendTime = System.currentTimeMillis() - startSendTime;
        long leftTime = 0;
        if(SendBackupDataService.mCurrentTotalSendSize == 0) {
            leftTime = leftSize * 1000/(4*1024*1024);
        } else {
            leftTime = leftSize * spendTime/SendBackupDataService.mCurrentTotalSendSize;
        }
        if(leftTime < 0) {
            leftTime = 0;
        }
        return leftTime;
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BACKUP_SYS_DATA:
                    backupSysData();
                    break;
                case BACKUP_SYS_SQL_DATA:
                    backupSqlData();
                    break;
                case BACKUP_SYS_OTHER_DATA:
                    backupSysOtherData();
                    break;
                case BACKUP_APP_DATA:
                    backupAppData();
                    break;
                case BACKUP_APP_OTHER_DATA:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        acquireWakeLock();
    }

    private PowerManager.WakeLock wakeLock;

    private void acquireWakeLock(){
        if (null == wakeLock) {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "keepWifiOn");
            if (null != wakeLock){
                wakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock() {
        if (null != wakeLock){
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra("needbackup")) {
            boolean needBackup = intent.getBooleanExtra("needbackup", false);
            if(needBackup) {

                if(!DataManager.mSelectSysApps.isEmpty()) {
                    mHandler.sendEmptyMessage(BACKUP_SYS_OTHER_DATA);
                } else {
                    mHandler.sendEmptyMessage(BACKUP_APP_DATA);
                }
            }
        }
        return START_NOT_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
        stopSelf();
    }


    private void sendSysList() throws IOException {
        mCurrentSendType = Utils.CATEGORY_SYS;
        try {
            if(mCallback != null) {
                mCallback.onStart(mCurrentSendType);
            }
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_START, Utils.CATEGORY_SYS);

            while(true) {
                if(isCancelled) {
                    return;
                }
                boolean isComplete = isSysBackupComplete && mSysReadyToSendFiles.isEmpty() && mSysBackupList.isEmpty();
                if (isComplete) {
                    break;
                }
                String packageName = null;
                synchronized(mSysReadyToSendFiles) {
                    if(mSysReadyToSendFiles.isEmpty()) {
                        continue;
                    }
                    packageName = mSysReadyToSendFiles.poll();
                }
                if(TextUtils.isEmpty(packageName)) {
                    continue;
                }
                sendSysApp(packageName);
            }
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_END, Utils.CATEGORY_SYS);
            if(mCallback != null) {
                mCallback.onComplete(mCurrentSendType);
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "sendSysList", e);
        } catch (SocketException e) {
            LogUtils.e(TAG, "SocketException", e);
            throw e;
        } catch (IOException e) {
            LogUtils.e(TAG, "IOException", e);
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "sendSysList", e);
        }
    }

    private void sendAppList() throws IOException{
        if(isCancelled) {
            return;
        }
        mCurrentSendType = Utils.CATEGORY_APP;
        try {
            if(mCallback != null) {
                mCallback.onStart(mCurrentSendType);
            }
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_START, Utils.CATEGORY_APP);

            while(true) {
                if(isCancelled) {
                    return;
                }
                String packageName = null;
                synchronized(mAppReadyToSendFiles) {
                    boolean isComplete = isAppBackupComplete && mAppReadyToSendFiles.isEmpty() && mAppBackupList.isEmpty();
                    if (isComplete) {
                        break;
                    }
                    if (mAppReadyToSendFiles.isEmpty()) {
                        continue;
                    }
                    packageName = mAppReadyToSendFiles.poll();
                }
                if(TextUtils.isEmpty(packageName)) {
                    continue;
                }
                sendApp(packageName);
            }
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_END, Utils.CATEGORY_APP);
            if(mCallback != null) {
                mCallback.onComplete(mCurrentSendType);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "sendAppList", e);
        } catch (IOException e) {
            Log.e(TAG, "sendAppList", e);
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "sendAppList", e);
        }
    }

    private void sendApp(final String packageName) throws IOException {
        AppSendManager appSendManager = AppSendManager.getInstance();
        BaseApp app = appSendManager.getAppByName(packageName);
        app.updateStreamAndCallback(mOutputStream, mCallback);
        try {
            String label = app.getName(getApplicationContext());
            if(mCallback != null) {
                mCallback.onFileBeginSend(mCurrentSendType, label);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "sendAppList", e);
        }
        app.send();
        app.setCallback(null);
    }

    private void sendSysApp(final String packageName) throws IOException {
        AppSendManager appSendManager = AppSendManager.getInstance();
        SysBaseApp app = appSendManager.getSysAppByName(packageName);
        app.updateStreamAndCallback(mOutputStream, mCallback);
        try {
            String label = app.getName(getApplicationContext());
            if(mCallback != null) {
                mCallback.onFileBeginSend(mCurrentSendType, label);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "sendSysApp", e);
        }
        app.send();
        app.setCallback(null);
    }

    private void sendPictureList() throws IOException{
        if(isCancelled) {
            return;
        }
        mCurrentSendType = Utils.CATEGORY_IMAGE;
        try {
            if(mCallback != null) {
                mCallback.onStart(mCurrentSendType);
            }
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_START, Utils.CATEGORY_IMAGE);
            sendFileList(mPictureList);
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_END, Utils.CATEGORY_IMAGE);
            if(mCallback != null) {
                mCallback.onComplete(mCurrentSendType);
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "sendPictureList", e);
        } catch (IOException e) {
            Log.e(TAG, "sendPictureList", e);
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "sendPictureList", e);
        }
    }

    private void sendVideoList() throws IOException{
        if(isCancelled) {
            return;
        }
        mVideoList = FilePathUtils.getVideoFilePath(this);
        mCurrentSendType = Utils.CATEGORY_VIDEO;
        try {
            if(mCallback != null) {
                mCallback.onStart(mCurrentSendType);
            }
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_START, Utils.CATEGORY_VIDEO);
            sendFileList(mVideoList);
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_END, Utils.CATEGORY_VIDEO);
            if(mCallback != null) {
                mCallback.onComplete(mCurrentSendType);
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "sendVideoList", e);
        } catch (IOException e) {
            Log.e(TAG, "sendVideoList", e);
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "sendVideoList", e);
        }
    }

    private void sendAudioList() throws IOException {
        if(isCancelled) {
            return;
        }
        mAudioList = FilePathUtils.getAudioFilePath(this);
        mCurrentSendType = Utils.CATEGORY_AUDIO;
        try {
            if(mCallback != null) {
                mCallback.onStart(mCurrentSendType);
            }
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_START, Utils.CATEGORY_AUDIO);
            sendFileList(mAudioList);
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_END, Utils.CATEGORY_AUDIO);
            if(mCallback != null) {
                mCallback.onComplete(mCurrentSendType);
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "sendAudioList", e);
        } catch (IOException e) {
            Log.e(TAG, "sendAudioList", e);
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "sendAudioList", e);
        }
    }


    private void sendDocumentList() throws  IOException{
        if(isCancelled) {
            return;
        }
        mDocumentList = FilePathUtils.getDocumentPath(SendBackupDataService.this);

        mCurrentSendType = Utils.CATEGORY_DOCUMENT;
        try {
            if(mCallback != null) {
                mCallback.onStart(mCurrentSendType);
            }
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_START, Utils.CATEGORY_DOCUMENT);
            sendFileList(mDocumentList);
            sendCatogeryInfo(NetWorkUtil.TYPE_CATEGOY_END, Utils.CATEGORY_DOCUMENT);
            if(mCallback != null) {
                mCallback.onComplete(mCurrentSendType);
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "sendDocumentList", e);
        } catch (IOException e) {
            Log.e(TAG, "sendDocumentList", e);
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "sendDocumentList", e);
        }
    }

    private void sendHeartBeat() throws IOException{
        mOutputStream.writeLong(NetWorkUtil.TYPE_HEART_BEAT);
        mOutputStream.flush();
    }


    private void sendFileList(final ArrayList<String> files) throws IOException, RemoteException {
        if(files == null || files.size() == 0) {
            Log.e(TAG, "files is empty");
            return;
        }
        for(String filePath: files) {
            if(isCancelled) {
                return;
            }
            try {
                sendSingleFile(filePath);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "sendFileList FileNotFoundException", e);
            }
        }
    }

    protected void sendFileHead(int type, final String filePath) throws IOException, FileNotFoundException {
        if(filePath == null) {
            Log.e(TAG, "files is empty");
            return;
        }
        File file = new File(filePath);
        if(!file.exists()) {
            return;
        }
        mOutputStream.writeLong(type);
        mOutputStream.flush();
        mOutputStream.writeUTF(filePath);
        mOutputStream.flush();
        mOutputStream.writeLong(file.length());
        mOutputStream.flush();
        mOutputStream.writeUTF(file.getName());
        mOutputStream.flush();
        mOutputStream.writeLong(getLeftTime());
        mOutputStream.flush();
    }

    private void sendFileBody(final String filePath, float compressionRatio) throws IOException, FileNotFoundException {
        int bufferSize = 8192;
        byte[] buf = new byte[bufferSize];
        File file = new File(filePath);
        if(!file.exists()) {
            return;
        }
        DataInputStream fis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(
                        filePath)));
        int read = 0;
        int passedlen = 0;

        long startTime = System.currentTimeMillis();
        long speed = 0;
        try {
            while ((read = fis.read(buf)) != -1) {
                if(isCancelled) {
                    return;
                }
                passedlen += read;
                mOutputStream.write(buf, 0, read);

                long currentTime = System.currentTimeMillis();
                mCurrentTotalSendSize += read * compressionRatio;
                mCurrentSendSize += read * compressionRatio;
                if(currentTime - startTime != 0) {
                    speed = 1000 * passedlen / (currentTime - startTime);
                } else {
                    speed = NetWorkUtil.DEFAULT_SPEED;
                }
                if(mCallback != null) {
                    mCallback.onProgress(mCurrentSendType, mCurrentSendSize, speed);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "sendFileBody RemoteException:" + filePath + "   error", e);
        }
        mOutputStream.flush();
        LogUtils.d(TAG, "send files:" + filePath + "  successfully");
    }

    private void sendFileBody(final String filePath) throws IOException, FileNotFoundException {
        sendFileBody(filePath, 1.0f);
    }


    private void sendSingleFile(final String filePath) throws IOException, RemoteException, FileNotFoundException {
        try {
            if(mCallback != null) {
                mCallback.onFileBeginSend(mCurrentSendType, getName(filePath));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "sendSysApp", e);
        }
        sendFileHead(NetWorkUtil.TYPE_SIMPLE, filePath);
        sendFileBody(filePath);
    }

    private String getName(final String filePath) {
        String name = filePath.substring(filePath.lastIndexOf("/") + 1);
        return name;
    }

    private void sendEnd() {
        try {
            if(isCancelled) {
                return;
            }
            mOutputStream.writeLong(NetWorkUtil.TYPE_END);
            mOutputStream.flush();
        } catch (FileNotFoundException fe) {
            Log.e(TAG, "sendEnd", fe);
        } catch (IOException ioe) {
            Log.e(TAG, "sendEnd", ioe);
        } catch (Exception e) {
            Log.e(TAG, "sendEnd", e);
        }
    }

    private void sendCatogeryInfo(int type, int catogeryId) {
        try {
            if(isCancelled) {
                return;
            }
            mOutputStream.writeLong(type);
            mOutputStream.flush();
            mOutputStream.writeLong(catogeryId);
            mOutputStream.flush();
            LogUtils.d(TAG, "send type:" + type + " catogeryId:" + catogeryId+ " successfully");
        } catch (FileNotFoundException fe) {
            Log.e(TAG, "send type:" + type + " catogeryId:" + catogeryId+ " error", fe);
        } catch (IOException ioe) {
            Log.e(TAG, "send type:" + type + " catogeryId:" + catogeryId+" error", ioe);
        } catch (Exception e) {
            Log.e(TAG, "send type:" + type + " catogeryId:" + catogeryId+" error", e);
        }
    }

    public void sendDataSize(String info) throws IOException{
        long type = NetWorkUtil.TYPE_SEND_SIZE;
        try {
            mOutputStream.writeLong(type);
            mOutputStream.flush();
            mOutputStream.writeUTF(info);
            mOutputStream.flush();
            LogUtils.d(TAG, "sendDataInfo1:" + info);
        } catch (FileNotFoundException fe) {
            Log.e(TAG, "sendDataInfo1:" + info, fe);
        } catch (IOException ioe) {
            Log.e(TAG, "sendDataInfo1:" + info, ioe);
            throw  ioe;
        } catch (Exception e) {
            Log.e(TAG, "sendDataInfo1:" + info, e);
        }

    }

    private boolean createConnection() {
        try {
            mSocket = new Socket(IP, PORT);
            mOutputStream = new DataOutputStream(mSocket.getOutputStream());
            mInputStream = new DataInputStream(mSocket.getInputStream());
            LogUtils.d(TAG, "createConnection SUCCESSFUL!");
            if(mCallback != null) {
                mCallback.onStart(1000);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        Thread.sleep(5000);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    while(true) {
                        try {
                            sendHeartBeat();
                            Thread.sleep(5000);
                            synchronized (mStartObject) {
                                if (isStartSend || isCancelled) {
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "sendHeartBeat:", e);
                            handleError();
                        }
                    }
                }
            }).start();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "createConnection:" , e);
            mSocket = null;
            handleError();
            return false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private volatile boolean isConnect = true;

    private final ISendInfo.Stub mBinder = new ISendInfo.Stub() {

        @Override
        public void cancelSend() throws RemoteException {
            isCancelled = true;
        }

        @Override
        public void connect() throws RemoteException {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    createConnection();
                }
            }).start();
        }

        @Override
        public void disConnect() throws RemoteException {

        }

        @Override
        public int getSendStatus() throws RemoteException {
            return 0;
        }

        @Override
        public long getSentSize() throws RemoteException {
            return 0;
        }

        @Override
        public String getSendingAppName() throws RemoteException {
            return null;
        }

        @Override
        public void sendData() throws RemoteException {
            synchronized (mStartObject) {
                isStartSend = true;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(mSocket == null) {
                        Log.e(TAG, "sendData");
                        handleError();
                        return;
                    }
                    long sysSize = 0;
                    long appSize = 0;
                    long imageSize = 0;
                    long videoSize = 0;
                    long audioSize = 0;
                    long docSize = 0;
                    if(DataManager.isSysSelect()) {
                        sysSize = DataManager.getSysSize();
                    }
                    if(DataManager.isAppSelect()) {
                        appSize = DataManager.getAppSize();
                    }
                    if(DataManager.isImageSelect()) {
                        mPictureList = FilePathUtils.getPictureFilePath(SendBackupDataService.this);
                        imageSize = FilePathUtils.getFileSize(File_Exchange.TYPE_IMAGE, getApplicationContext());
                    }
                    if(DataManager.isVideoSelect()) {
                        mVideoList = FilePathUtils.getVideoFilePath(SendBackupDataService.this);
                        videoSize = FilePathUtils.getFileSize(File_Exchange.TYPE_VIDEO, getApplicationContext());
                    }
                    if(DataManager.isAudioSelect()) {
                        mAudioList = FilePathUtils.getAudioFilePath(SendBackupDataService.this);
                        audioSize = FilePathUtils.getFileSize(File_Exchange.TYPE_AUDIO, getApplicationContext());
                    }
                    if(DataManager.isDocSelect()) {
                        docSize = FilePathUtils.getFileSize(File_Exchange.TYPE_DOCUMENT, getApplicationContext());
                    }
                    mTotalSize = sysSize + appSize + imageSize + videoSize + audioSize + docSize;
                    JSONObject obj = new JSONObject();
                    try {
                        if(mSocket == null) {
                            throw new IOException("mSocket IS NULL");
                        }
                        obj.put(Utils.CATEGORY_SYS_SIZE, sysSize + "");
                        obj.put(Utils.CATEGORY_APP_SIZE, appSize + "");
                        obj.put(Utils.CATEGORY_IMAGE_SIZE, imageSize + "");
                        obj.put(Utils.CATEGORY_VIDEO_SIZE, videoSize + "");
                        obj.put(Utils.CATEGORY_AUDIO_SIZE, audioSize + "");
                        obj.put(Utils.CATEGORY_DOCUMENT_SIZE, docSize + "");
                        sendDataSize(obj.toString());

                        long aa = mInputStream.readLong();

                        startSendTime = System.currentTimeMillis();
                        if(DataManager.isSysSelect()) {
                            mCurrentSendSize = 0;
                            sendSysList();
                        }
                        if(DataManager.isAppSelect()) {
                            mCurrentSendSize = 0;
                            sendAppList();
                        }
                        if(DataManager.isImageSelect()) {
                            mCurrentSendSize = 0;
                            sendPictureList();
                        }
                        if(DataManager.isVideoSelect()) {
                            mCurrentSendSize = 0;
                            sendVideoList();
                        }
                        if(DataManager.isAudioSelect()) {
                            mCurrentSendSize = 0;
                            sendAudioList();
                        }
                        if(DataManager.isDocSelect()) {
                            mCurrentSendSize = 0;
                            sendDocumentList();
                        }
                        sendEnd();

                        closeSocket();
                        if (mCallback == null) {
                            return;
                        }
                        if (!isCancelled) {
                            mCallback.onAllComplete();
                        } else {
                            mCallback.onCancel();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "sendData", e);
                    } catch (RemoteException e) {
                        LogUtils.e(TAG, "RemoteException", e);
                    } catch (IOException e) {
                        Log.e(TAG, "sendData IOException", e);
                        closeSocket();
                        handleError();
                        return;
                    } finally {
                        FilePathUtils.delFolder(Utils.BACKUP_PATH);
                        SendBackupDataService.this.stopSelf();
                    }
                }
            }).start();
        }

        @Override
        public void sendDataInfo(String info) throws RemoteException {

        }

        @Override
        public void registerCallback(ICallback cb) throws RemoteException {
            setCallback(cb);
        }

        @Override
        public void unregisterCallback(ICallback cb) throws RemoteException {
            removeCallback(cb);
        }
    };


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

    private void sendSizeInfo() throws IOException{
        long sysSize = 0;
        long appSize = 0;
        long imageSize = 0;
        long videoSize = 0;
        long audioSize = 0;
        long docSize = 0;
        if(DataManager.isSysSelect()) {
            sysSize = DataManager.getSysSize();
        }
        if(DataManager.isAppSelect()) {
            appSize = DataManager.getAppSize();
        }
        if(DataManager.isImageSelect()) {
            mPictureList = FilePathUtils.getPictureFilePath(SendBackupDataService.this);
            imageSize = FilePathUtils.getFileSize(File_Exchange.TYPE_IMAGE, getApplicationContext());
        }
        if(DataManager.isVideoSelect()) {
            mVideoList = FilePathUtils.getVideoFilePath(SendBackupDataService.this);
            videoSize = FilePathUtils.getFileSize(File_Exchange.TYPE_VIDEO, getApplicationContext());
        }
        if(DataManager.isAudioSelect()) {
            mAudioList = FilePathUtils.getAudioFilePath(SendBackupDataService.this);
            audioSize = FilePathUtils.getFileSize(File_Exchange.TYPE_AUDIO, getApplicationContext());
        }
        if(DataManager.isDocSelect()) {
            docSize = FilePathUtils.getFileSize(File_Exchange.TYPE_DOCUMENT, getApplicationContext());
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put(Utils.CATEGORY_SYS_SIZE, sysSize + "");
            obj.put(Utils.CATEGORY_APP_SIZE, appSize + "");
            obj.put(Utils.CATEGORY_IMAGE_SIZE, imageSize + "");
            obj.put(Utils.CATEGORY_VIDEO_SIZE, videoSize + "");
            obj.put(Utils.CATEGORY_AUDIO_SIZE, audioSize + "");
            obj.put(Utils.CATEGORY_DOCUMENT_SIZE, docSize + "");
            sendDataSize(obj.toString());
        } catch (JSONException e) {
            LogUtils.e(TAG, "sendData", e);
        } catch (IOException e) {
            Log.e(TAG, "sendAppList", e);
            throw e;
        }
    }

    private IBackupManagerService mBackup = null;

    private HashSet<String> mSysBackupList = new HashSet<String>();
    private HashSet<String> mAppBackupList = new HashSet<String>();
    private void backupSysData() {
        try {
            LogUtils.d(TAG, "beginBack");
            mBackup = (IBackupManagerService) getApplicationContext()
                    .getSystemService("tctbackup");
            ArrayList<String> tmp = new ArrayList<String>();
            for(String pkg : DataManager.mSelectSysApps) {
                if(!TextUtils.equals("com.android.contacts", pkg)
                        && !TextUtils.equals("com.android.mms", pkg)
                        && !TextUtils.equals("com.android.dialer", pkg)
                        && !TextUtils.equals("com.android.settings", pkg)
                        && !TextUtils.equals("com.android.calendar", pkg)) {
                    tmp.add(pkg);
                }
            }
            String[] allList = tmp.toArray(new String[0]);
            if(allList.length == 0) {
                mHandler.sendEmptyMessage(BACKUP_SYS_SQL_DATA);
                return;
            }

            String path = Utils.SYS_DATA_BACKUP_PATH;
            File file = new File(path);
            if(!file.exists()) {
                file.mkdirs();
            }
            mBackup.registerCallback(mBackupSysCallback);
            mBackup.beginBack(allList, path, 1);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:", e);
        }
    }

    private IBackupManagerServiceCallback mBackupOther = new IBackupManagerServiceCallback.Stub() {

        @Override
        public void onStart() throws RemoteException {
            LogUtils.d(TAG, "onStart");
        }

        @Override
        public void onComplete() throws RemoteException {
            LogUtils.d(TAG, "onComplete");
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

    private IBackupManagerServiceCallback mBackupSysCallback = new IBackupManagerServiceCallback.Stub() {

        @Override
        public void onStart() throws RemoteException {
            LogUtils.d(TAG, "onStart");
        }

        @Override
        public void onComplete() throws RemoteException {
            LogUtils.d(TAG, "onComplete");
            isSysBackupComplete = true;
            if (mBackup != null) {
                mBackup.unregisterCallback(mBackupSysCallback);
            }
            mHandler.sendEmptyMessage(BACKUP_SYS_SQL_DATA);
        }

        @Override
        public void onUpdate(String test) throws RemoteException {
            LogUtils.d(TAG, "onUpdate: " + test);
            if(TextUtils.isEmpty(test) || !test.toLowerCase().startsWith("end:")) {
                return;
            }
            try {
                String[] aa = test.split(":");
                String packageName = aa[1];
                mSysBackupList.remove(packageName);
                synchronized (mSysReadyToSendFiles) {
                    mSysReadyToSendFiles.add(packageName);
                }

            } catch (Exception e) {
                Log.e(TAG, "onUpdate: ", e);
            }

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

    private void backupAppData() {
        try {
            if(DataManager.mSelectUserApps.isEmpty()) {
                isAppBackupComplete = true;
                return;
            }
            mAppBackupList.addAll(DataManager.mSelectUserApps);
            LogUtils.d(TAG, "beginBack");
            mBackup = (IBackupManagerService) getApplicationContext()
                    .getSystemService("tctbackup");
            ArrayList<String> tmp = new ArrayList<String>();
            tmp.addAll(DataManager.mSelectUserApps);

            String[] allList = tmp.toArray(new String[0]);

            String path = Utils.APP_DATA_BACKUP_PATH;
            File file = new File(path);
            if(!file.exists()) {
                file.mkdirs();
            }
            mBackup.registerCallback(mBackupAppCallback);
            mBackup.beginBack(allList, path, 1);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:", e);
        }
    }

    private IBackupManagerServiceCallback mBackupAppCallback = new IBackupManagerServiceCallback.Stub() {

        @Override
        public void onStart() throws RemoteException {
            LogUtils.d(TAG, "onStart");
        }

        @Override
        public void onComplete() throws RemoteException {
            LogUtils.d(TAG, "onComplete");
            isAppBackupComplete = true;
            if (mBackup != null) {
                mBackup.unregisterCallback(mBackupAppCallback);
            }
        }

        @Override
        public void onUpdate(String test) throws RemoteException {
            if(TextUtils.isEmpty(test) || !test.toLowerCase().startsWith("end:")) {
                return;
            }
            try {
                String[] aa = test.split(":");
                String packageName = aa[1];
                mAppBackupList.remove(packageName);
                synchronized (mAppReadyToSendFiles) {
                    mAppReadyToSendFiles.add(packageName);
                }
            } catch (Exception e) {
                Log.e(TAG, "onUpdate: ", e);
            }
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

    private void backupSqlData() {

        if(mSysBackupList.contains("com.android.contacts")) {
            mSysReadyToSendFiles.add("com.android.contacts");
            mSysBackupList.remove("com.android.contacts");
        }
        if(mSysBackupList.contains("com.android.calendar")) {
            mSysReadyToSendFiles.add("com.android.calendar");
            mSysBackupList.remove("com.android.calendar");
        }
        if(mSysBackupList.contains("com.android.mms")) {
            mSysReadyToSendFiles.add("com.android.mms");
            mSysBackupList.remove("com.android.mms");
        }
        if(mSysBackupList.contains("com.android.dialer")) {
            mSysReadyToSendFiles.add("com.android.dialer");
            mSysBackupList.remove("com.android.dialer");
        }
        if(mSysBackupList.contains("com.android.settings")) {
            mSysReadyToSendFiles.add("com.android.settings");
            mSysBackupList.remove("com.android.settings");
        }
        isSysBackupComplete = true;
        mHandler.sendEmptyMessage(BACKUP_APP_DATA);
    }

    private void backupSysOtherData() {
        try {
            mSysBackupList.addAll(DataManager.mSelectSysApps);
            if(DataManager.mSelectSysApps.isEmpty()) {
                isSysBackupComplete = true;
                mHandler.sendEmptyMessage(BACKUP_APP_DATA);
                return;
            }
            LogUtils.d(TAG, "beginBack");
            mBackup = (IBackupManagerService) getApplicationContext()
                    .getSystemService("tctbackup");
            ArrayList<String> tmp = new ArrayList<String>();
            for(String packageName : DataManager.mSelectSysApps) {
                SysBaseApp app = AppSendManager.getInstance().getSysAppByName(packageName);
                tmp.addAll(app.getSysOtherFiles());
            }
            String[] allList = tmp.toArray(new String[0]);
            if(allList.length == 0) {
                mHandler.sendEmptyMessage(BACKUP_SYS_DATA);
                return;
            }
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
            mHandler.sendEmptyMessage(BACKUP_SYS_DATA);
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

    private ICallback mCallback;
    public void setCallback(ICallback callback) {
        this.mCallback = callback;
    }

    public void removeCallback(ICallback callback) {
        this.mCallback = null;
    }


    private void handleError() {

        try {
            Log.e(TAG, "handleError", new Exception("error happens"));
            if (mCallback != null) {
                mCallback.onError(0, "");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException", e);
        }
        stopSelf();
    }

}