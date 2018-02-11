/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.systemApp;

import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.send.SendBackupDataService;
import cn.tcl.transfer.util.FilePathUtils;
import cn.tcl.transfer.util.NetWorkUtil;
import cn.tcl.transfer.util.Utils;

public class MmsSysApp extends SysBaseApp {

    public static final String NAME = "com.android.mms";
    private static final String TAG = "MmsSysApp";
    public static long smsStartTime = 0;
    public static long mmsStartTime = 0;
    public static int smsCount = 0;
    public static int mmsCount = 0;
    public static int smsRecvCount = 0;
    public static int mmsRecvCount = 0;
    public static int smsInertCount = 0;
    public static int mmsInertCount = 0;

    public MmsSysApp() {
        super(NAME);
    }

    public MmsSysApp(DataOutputStream outputStream, ICallback callback) {
        super(outputStream, callback, NAME);
    }

    @Override
    public void send() throws IOException {
        try {
            sendSqlFile();
            sendAttachFiles();
        } catch (RemoteException e) {
            Log.e(TAG, "sendAppList", e);
        }
    }

    @Override
    public ArrayList<String> getSysOtherFiles() {
        ArrayList<String> files = new ArrayList<>();
        files.add("/data/user_de/0/com.android.providers.telephony/app_parts/");
        return files;
    }

    @Override
    public void sendSqlFile() throws IOException, RemoteException {
        sendFileHead(NetWorkUtil.TYPE_SYS_SQL_DATA, Utils.SYS_SMS_BACKUP_PATH);
        mOutputStream.writeLong(SendBackupDataService.getLeftTime());
        mOutputStream.flush();
        mOutputStream.writeLong(smsCount);
        mOutputStream.flush();
        sendFileBody(Utils.SYS_SMS_BACKUP_PATH);

        sendFileHead(NetWorkUtil.TYPE_SYS_SQL_DATA, Utils.SYS_MMS_BACKUP_PATH);
        mOutputStream.writeLong(SendBackupDataService.getLeftTime());
        mOutputStream.flush();
        mOutputStream.writeLong(mmsCount);
        mOutputStream.flush();
        sendFileBody(Utils.SYS_MMS_BACKUP_PATH);
    }

    private void sendAttachFiles() throws IOException, RemoteException {
        if(SendBackupDataService.isCancelled) {
            return;
        }
        ArrayList<String> files = FilePathUtils.getFilelistFromPath(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/app_parts");
        if(files == null || files.isEmpty()) {
            return;
        }
        for(String filePath : files) {
            String name = filePath.substring(filePath.lastIndexOf("/") + 1);

            File file = new File(filePath);
            if(!file.exists()) {
                continue;
            }
            if(SendBackupDataService.isCancelled) {
                return;
            }
            mOutputStream.writeLong(NetWorkUtil.TYPE_SYS_OTHER_DATA);
            mOutputStream.flush();
            mOutputStream.writeUTF(Utils.SYS_OTHER_DATA_RECEIVED_PATH + "/app_parts/" + name);
            mOutputStream.flush();
            mOutputStream.writeLong(file.length());
            mOutputStream.flush();
            mOutputStream.writeUTF(mAppName);
            mOutputStream.flush();
            mOutputStream.writeLong(SendBackupDataService.getLeftTime());
            mOutputStream.flush();
            sendFileBody(filePath);
        }
    }
}
