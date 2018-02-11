/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.systemApp;

import android.os.RemoteException;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.send.SendBackupDataService;
import cn.tcl.transfer.util.NetWorkUtil;
import cn.tcl.transfer.util.Utils;

public class LauncherSysApp extends SysBaseApp {

    public static final String NAME = "com.monster.launcher";

    public LauncherSysApp() {
        super(NAME);
    }

    public LauncherSysApp(DataOutputStream outputStream, ICallback callback) {
        super(outputStream, callback, NAME);
    }

    @Override
    public long calculateCreatedDirSize() {
        long size = 0;
        return size;
    }

    public void send() throws IOException {
        try {
            sendSysData(mPackageName);
            sendWallpaper();
            sendLockWallpaper();
        } catch (RemoteException e) {
            Log.e("EmailSysApp", "sendAppList", e);
        }
    }

    @Override
    public ArrayList<String> getSysOtherFiles() {
        ArrayList<String> files = new ArrayList<>();
        files.add("/data/system/users/0/wallpaper");
        files.add("/data/system/users/0/wallpaper_lock");
        return files;
    }

    private void sendWallpaper() throws IOException, RemoteException {
        File file = new File(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/wallpaper");
        if(!file.exists()) {
            return;
        }
        if(SendBackupDataService.isCancelled) {
            return;
        }
        mOutputStream.writeLong(NetWorkUtil.TYPE_SYS_OTHER_DATA);
        mOutputStream.flush();
        mOutputStream.writeUTF(Utils.SYS_OTHER_DATA_RECEIVED_PATH + "/wallpaper");
        mOutputStream.flush();
        mOutputStream.writeLong(file.length());
        mOutputStream.flush();
        mOutputStream.writeUTF(mAppName);
        mOutputStream.flush();
        mOutputStream.writeLong(SendBackupDataService.getLeftTime());
        mOutputStream.flush();
        sendFileBody(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/wallpaper");
    }

    private void sendLockWallpaper() throws IOException, RemoteException {
        File file1 = new File(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/wallpaper_lock");
        if(!file1.exists()) {
            return;
        }
        if(SendBackupDataService.isCancelled) {
            return;
        }
        mOutputStream.writeLong(NetWorkUtil.TYPE_SYS_OTHER_DATA);
        mOutputStream.flush();
        mOutputStream.writeUTF(Utils.SYS_OTHER_DATA_RECEIVED_PATH + "/wallpaper_lock");
        mOutputStream.flush();
        mOutputStream.writeLong(file1.length());
        mOutputStream.flush();
        mOutputStream.writeUTF(mAppName);
        mOutputStream.flush();
        mOutputStream.writeLong(SendBackupDataService.getLeftTime());
        mOutputStream.flush();
        sendFileBody(Utils.SYS_OTHER_DATA_BACKUP_PATH + "/wallpaper_lock");
    }
}
