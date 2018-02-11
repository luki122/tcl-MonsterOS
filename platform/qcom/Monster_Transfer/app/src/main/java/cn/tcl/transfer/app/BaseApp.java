/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.send.SendBackupDataService;
import cn.tcl.transfer.util.DataManager;
import cn.tcl.transfer.util.FilePathUtils;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.NetWorkUtil;
import cn.tcl.transfer.util.PackageDetailInfo;
import cn.tcl.transfer.util.Utils;

public abstract class BaseApp {

    private static final String TAG = "BaseApp";
    DataOutputStream mOutputStream;
    protected String mPackageName;

    private final int mCurrentSendType = Utils.CATEGORY_APP;

    private String mAppName = null;

    private ICallback mCallback;
    public void setCallback(ICallback callback) {
        this.mCallback = callback;
    }

    public BaseApp(final String pkgName) {
        mPackageName = pkgName;
    }

    public BaseApp(DataOutputStream outputStream, ICallback callback, final String pkgName) {
        mOutputStream = outputStream;
        mCallback = callback;
        mPackageName = pkgName;
    }

    public void updateStreamAndCallback(DataOutputStream outputStream, ICallback callback) {
        mOutputStream = outputStream;
        mCallback = callback;
    }

    public void send() throws IOException {
        try {
            sendAppData(mPackageName);
            sendApkData(DataManager.mSizeInfo.get(mPackageName).packageInfo.applicationInfo.sourceDir, mPackageName);
            sendFileList(getSdcardData(mPackageName));
            sendFileList(getCreateSdcardData());
        } catch (RemoteException e) {
            Log.e(TAG, "sendAppList", e);
        }
    }

    private void sendAppData(final String packageName) throws IOException, RemoteException {
        final String filePath = Utils.APP_DATA_BACKUP_PATH + "/" + packageName + ".tar";
        if(!checkFileExist(filePath)) {
            Log.e(TAG, filePath + " is not exist!");
            return;
        }
        File file = new File(filePath);
        long originSize = DataManager.mSizeInfo.get(packageName).sysDataSize;
        float compressionRatio = (float)originSize/(float)file.length();

        sendAppFileHead(packageName);
        sendFileBody(filePath, compressionRatio);
        LogUtils.v(TAG, "sendAppData one file:" + filePath);
    }

    private void sendApkData(final String filePath, String packageName) throws IOException, RemoteException {
        try {
            if(!checkFileExist(filePath)) {
                Log.e(TAG, filePath + " is not exist!");
                return;
            }
            File file = new File(filePath);
            mOutputStream.writeLong(NetWorkUtil.TYPE_APK);
            mOutputStream.flush();
            mOutputStream.writeUTF(getPath(filePath) + packageName + ".apk");
            mOutputStream.flush();
            mOutputStream.writeLong(file.length());
            mOutputStream.flush();
            mOutputStream.writeUTF(mAppName);
            mOutputStream.flush();
            mOutputStream.writeLong(SendBackupDataService.getLeftTime());
            mOutputStream.flush();

            sendFileBody(filePath);
            LogUtils.v(TAG, "sendApkData one file:" + filePath);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "sendApkData FileNotFoundException", e);
        }

    }

    private void sendFileList(final ArrayList<String> files) throws IOException, RemoteException {
        if(files == null || files.size() == 0) {
            Log.e(TAG, "files is empty");
            return;
        }
        for(String filePath: files) {
            if(SendBackupDataService.isCancelled) {
                return;
            }
            try {
                sendSingleFile(filePath);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "sendFileList FileNotFoundException", e);
            }
        }
    }

    private ArrayList<String> getSdcardData(String packageName) {
        ArrayList<String> pathList = new ArrayList<String>();
        String path = "/sdcard/Android/data/" + packageName;
        return  FilePathUtils.getFilelistFromPath(path);
    }

    public abstract ArrayList<String> getCreateSdcardData();

    private void sendSingleFile(final String filePath) throws IOException, RemoteException {
        sendSimpleFileHead(filePath);
        sendFileBody(filePath);
    }

    private void sendFileBody(final String filePath) throws IOException {
        sendFileBody(filePath, 1.0f);
    }


    String getPath(String file) {
        return file.substring(0, file.lastIndexOf("/") + 1);
    }



    private void sendAppFileHead(final String pkgName) throws IOException {
        final String filePath = Utils.APP_DATA_BACKUP_PATH + "/" + pkgName + ".tar";
        if(!checkFileExist(filePath)) {
            Log.e(TAG, filePath + " is not exist!");
            return;
        }
        File file = new File(filePath);
        long originSize = DataManager.mSizeInfo.get(pkgName).sysDataSize;
        mOutputStream.writeLong(NetWorkUtil.TYPE_APP_DATA);
        mOutputStream.flush();
        mOutputStream.writeUTF(filePath);
        mOutputStream.flush();
        mOutputStream.writeLong(file.length());
        mOutputStream.flush();
        mOutputStream.writeUTF(mAppName);
        mOutputStream.flush();
        mOutputStream.writeLong(SendBackupDataService.getLeftTime());
        mOutputStream.flush();
        mOutputStream.writeLong(originSize);
        mOutputStream.flush();

    }

    private void sendSimpleFileHead(final String filePath) throws IOException {
        if(!checkFileExist(filePath)) {
            Log.e(TAG, filePath + " is not exist!");
            return;
        }
        File file = new File(filePath);
        mOutputStream.writeLong(NetWorkUtil.TYPE_SIMPLE);
        mOutputStream.flush();
        mOutputStream.writeUTF(filePath);
        mOutputStream.flush();
        mOutputStream.writeLong(file.length());
        mOutputStream.flush();
        mOutputStream.writeUTF(mAppName);
        mOutputStream.flush();
        mOutputStream.writeLong(SendBackupDataService.getLeftTime());
        mOutputStream.flush();
    }

    private void sendFileBody(final String filePath, float compressionRatio) throws IOException {
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
                if(SendBackupDataService.isCancelled) {
                    return;
                }
                passedlen += read;
                mOutputStream.write(buf, 0, read);
                long currentTime = System.currentTimeMillis();
                SendBackupDataService.mCurrentTotalSendSize += read * compressionRatio;
                SendBackupDataService.mCurrentSendSize += read * compressionRatio;
                if(currentTime - startTime != 0) {
                    speed = 1000 * passedlen / (currentTime - startTime);
                } else {
                    speed = NetWorkUtil.DEFAULT_SPEED;
                }
                if(mCallback != null) {
                    mCallback.onProgress(mCurrentSendType, SendBackupDataService.mCurrentSendSize, speed);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "sendFileBody RemoteException:" + filePath + "   error", e);
        }
        mOutputStream.flush();
        LogUtils.d(TAG, "send files:" + filePath + "  successfully");
    }

    private boolean checkFileExist(final String filePath) {
        if(filePath == null) {
            Log.e(TAG, "files is empty");
            return false;
        }
        File file = new File(filePath);
        if(!file.exists()) {
            return false;
        }
        return true;
    }

    public long calculateCreatedDirSize() {
        return 0;
    }

    public String getName(Context context) {
        if(!TextUtils.isEmpty(mAppName)) {
            return mAppName;
        }

        if(TextUtils.isEmpty(mPackageName) || context == null) {
            return "";
        }
        PackageDetailInfo packageDetailInfo = DataManager.mSizeInfo.get(mPackageName);
        if(packageDetailInfo == null) {
            return "";
        }
        PackageManager pm = context.getPackageManager();
        mAppName = packageDetailInfo.packageInfo.applicationInfo.loadLabel(pm).toString();
        return mAppName;
    }

}
