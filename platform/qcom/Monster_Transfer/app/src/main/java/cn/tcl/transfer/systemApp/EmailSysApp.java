/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.systemApp;

import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.util.DataManager;
import cn.tcl.transfer.util.FilePathUtils;
import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.util.Utils;

public class EmailSysApp extends SysBaseApp {

    public static final String NAME = "com.tct.email";

    public EmailSysApp() {
        super(NAME);
    }

    public EmailSysApp(DataOutputStream outputStream, ICallback callback) {
        super(outputStream, callback, NAME);
    }

    @Override
    public long calculateCreatedDirSize() {
        long size = 0;
        return size;
    }

    @Override
    public ArrayList<String> getSysOtherFiles() {
        ArrayList<String> files = new ArrayList<>();
        return files;
    }

    public void send() throws IOException {
        try {
            sendSysData(mPackageName);
            sendAccountDB();
        } catch (RemoteException e) {
            Log.e("EmailSysApp", "sendAppList", e);
        }
    }

    private void sendAccountDB() {
    }
}
