/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.systemApp;

import android.os.RemoteException;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.send.SendBackupDataService;
import cn.tcl.transfer.util.NetWorkUtil;
import cn.tcl.transfer.util.Utils;

public class CalendarSysApp extends SysBaseApp {

    public static final String NAME = "com.android.calendar";
    private static final String TAG = "CalendarSysApp";
    public static long startTime = 0;
    public static int localCount = 0;
    public static int recvCount = 0;
    public static int inertCount = 0;

    public CalendarSysApp() {
        super(NAME);
    }

    public CalendarSysApp(DataOutputStream outputStream, ICallback callback) {
        super(outputStream, callback, NAME);
    }

    @Override
    public void send() throws IOException {
        try {
            sendSqlFile();
        } catch (RemoteException e) {
            Log.e(TAG, "sendAppList", e);
        }
    }

    @Override
    public void sendSqlFile() throws IOException, RemoteException {
        sendFileHead(NetWorkUtil.TYPE_SYS_SQL_DATA, Utils.SYS_CALENDAR_BACKUP_PATH);
        mOutputStream.writeLong(SendBackupDataService.getLeftTime());
        mOutputStream.flush();
        mOutputStream.writeLong(localCount);
        mOutputStream.flush();
        sendFileBody(Utils.SYS_CALENDAR_BACKUP_PATH);
    }

}
