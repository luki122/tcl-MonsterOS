/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.systemApp;

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

public abstract class SysBaseApp {

    private static final String TAG = "BaseApp";
    DataOutputStream mOutputStream;
    protected String mPackageName;
    protected String mAppName = null;

    private ICallback mCallback;
    public void setCallback(ICallback callback) {
        this.mCallback = callback;
    }

    public SysBaseApp(final String pkgName) {
        mPackageName = pkgName;
    }

    public SysBaseApp(DataOutputStream outputStream, ICallback callback, final String pkgName) {
        mOutputStream = outputStream;
        mCallback = callback;
        mPackageName = pkgName;
    }

    public void updateStreamAndCallback(DataOutputStream outputStream, ICallback callback) {
        mOutputStream = outputStream;
        mCallback = callback;
    }

    public ArrayList<String> getSysOtherFiles() {
        ArrayList<String> files = new ArrayList<>();
        return files;
    }

    public void send() throws IOException {
        try {
            sendSysData(mPackageName);
            sendFileList(getSdcardData(mPackageName));
            sendFileList(getCreateSdcardData());
        } catch (RemoteException e) {
            Log.e(TAG, "sendAppList", e);
        }
    }

    protected void sendSysData(final String packageName) throws IOException, RemoteException, FileNotFoundException {
        if(SendBackupDataService.isCancelled) {
            return;
        }

        final String filePath = Utils.SYS_DATA_BACKUP_PATH + "/" +packageName + ".tar";
        if(filePath == null) {
            Log.e(TAG, "files is empty");
            return;
        }
        File file = new File(filePath);
        if(!file.exists()) {
            return;
        }
        long originSize = DataManager.mSizeInfo.get(packageName).sysDataSize;
        float compressionRatio = (float)originSize/(float)file.length();

        sendSysFileHead(packageName);
        sendFileBody(filePath, compressionRatio);
        LogUtils.v(TAG, "sendSysData one file:" + filePath);
    }

    private void sendSysFileHead(final String pkgName) throws IOException, FileNotFoundException {
        final String filePath = Utils.SYS_DATA_BACKUP_PATH + "/" + pkgName + ".tar";
        if(filePath == null) {
            Log.e(TAG, "files is empty");
            return;
        }
        File file = new File(filePath);
        if(!file.exists()) {
            return;
        }
        long originSize = DataManager.mSizeInfo.get(pkgName).sysDataSize;
        mOutputStream.writeLong(NetWorkUtil.TYPE_SYS_DATA);
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


    protected void sendFileList(final ArrayList<String> files) throws IOException, RemoteException {
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

    public void sendSqlFile() throws IOException, RemoteException {

    }

    private ArrayList<String> getSdcardData(String packageName) {
        ArrayList<String> pathList = new ArrayList<String>();
        String path = "/sdcard/Android/data/" + packageName;
        return  FilePathUtils.getFilelistFromPath(path);
    }

    public ArrayList<String> getCreateSdcardData() {
        return null;
    }

    private void sendSingleFile(final String filePath) throws IOException, RemoteException {
        sendSimpleFileHead(filePath);
        sendFileBody(filePath);
    }

    protected void sendFileBody(final String filePath) throws IOException {
        sendFileBody(filePath, 1.0f);
    }


    String getPath(String file) {
        return file.substring(0, file.lastIndexOf("/") + 1);
    }


    private void sendSimpleFileHead(final String filePath) throws IOException {
        if(SendBackupDataService.isCancelled) {
            return;
        }
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
        try {
            while ((read = fis.read(buf)) != -1) {
                if(SendBackupDataService.isCancelled) {
                    return;
                }
                mOutputStream.write(buf, 0, read);
                SendBackupDataService.mCurrentTotalSendSize += read * compressionRatio;
                SendBackupDataService.mCurrentSendSize += read * compressionRatio;
                if(mCallback != null) {
                    mCallback.onProgress(SendBackupDataService.mCurrentSendType, SendBackupDataService.mCurrentSendSize, 0);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "sendFileBody RemoteException:" + filePath + "   error", e);
        }
        mOutputStream.flush();
        LogUtils.d(TAG, "send files:" + filePath + "  successfully");
    }

    protected void sendFileHead(int type, final String filePath) throws IOException, FileNotFoundException {
        if(SendBackupDataService.isCancelled) {
            return;
        }
        if(filePath == null) {
            Log.e(TAG, "files is empty");
            return;
        }
        File file = new File(filePath);
        if(!file.exists()) {
            return;
        }
        switch (type) {
            case NetWorkUtil.TYPE_SYS_DATA:
                mOutputStream.writeLong(type);
                mOutputStream.flush();
                mOutputStream.writeUTF(filePath);
                mOutputStream.flush();
                mOutputStream.writeLong(file.length());
                mOutputStream.flush();
                mOutputStream.writeUTF(mAppName);
                mOutputStream.flush();
                break;
            case NetWorkUtil.TYPE_APP_DATA:
                mOutputStream.writeLong(type);
                mOutputStream.flush();
                mOutputStream.writeUTF(filePath);
                mOutputStream.flush();
                mOutputStream.writeLong(file.length());
                mOutputStream.flush();
                mOutputStream.writeUTF(mAppName);
                mOutputStream.flush();
                break;
            case NetWorkUtil.TYPE_APP_SD_DATA:
                mOutputStream.writeLong(type);
                mOutputStream.flush();
                mOutputStream.writeUTF(filePath);
                mOutputStream.flush();
                mOutputStream.writeLong(file.length());
                mOutputStream.flush();
                mOutputStream.writeUTF(mAppName);
                mOutputStream.flush();
                break;
            case NetWorkUtil.TYPE_SYS_SQL_DATA:
                String recvPath = Utils.SYS_SQL_DATA_RECEIVED_PATH + "/" + file.getName();
                mOutputStream.writeLong(NetWorkUtil.TYPE_SYS_SQL_DATA);
                mOutputStream.flush();
                mOutputStream.writeUTF(recvPath);
                mOutputStream.flush();
                mOutputStream.writeLong(file.length());
                mOutputStream.flush();
                mOutputStream.writeUTF(mAppName);
                mOutputStream.flush();
                break;
            case NetWorkUtil.TYPE_APK:
                mOutputStream.writeLong(type);
                mOutputStream.flush();
                mOutputStream.writeUTF(filePath);
                mOutputStream.flush();
                mOutputStream.writeLong(file.length());
                mOutputStream.flush();
                mOutputStream.writeUTF(mAppName);
                mOutputStream.flush();
                break;
            case NetWorkUtil.TYPE_APP_SD_CREATE_DATA:
                mOutputStream.writeLong(type);
                mOutputStream.flush();
                mOutputStream.writeUTF(filePath);
                mOutputStream.flush();
                mOutputStream.writeLong(file.length());
                mOutputStream.flush();
                mOutputStream.writeUTF(mAppName);
                mOutputStream.flush();
                break;
            case NetWorkUtil.TYPE_SIMPLE:
                mOutputStream.writeLong(type);
                mOutputStream.flush();
                mOutputStream.writeUTF(filePath);
                mOutputStream.flush();
                mOutputStream.writeLong(file.length());
                mOutputStream.flush();
            case NetWorkUtil.TYPE_SYS_OTHER_DATA:
                mOutputStream.writeLong(type);
                mOutputStream.flush();
                mOutputStream.writeUTF(filePath);
                mOutputStream.flush();
                mOutputStream.writeLong(file.length());
                mOutputStream.flush();
                mOutputStream.writeUTF(mAppName);
                mOutputStream.flush();
                break;
            default:
                break;
        }
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

    public long getSqlSize() {
        return 0;
    }

    public String getName(Context context) {
        if(!TextUtils.isEmpty(mAppName)) {
            return mAppName;
        }
        if(TextUtils.isEmpty(mPackageName) || context == null) {
            return "file";
        }
        PackageDetailInfo packageDetailInfo = DataManager.mSizeInfo.get(mPackageName);
        if(packageDetailInfo == null) {
            return "file";
        }
        PackageManager pm = context.getPackageManager();
        mAppName = packageDetailInfo.packageInfo.applicationInfo.loadLabel(pm).toString();
        return mAppName;
    }
}
