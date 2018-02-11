/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.receiver;
import cn.tcl.transfer.IReceiveCallback;
import com.tct.backupmanager.IBackupManagerServiceCallback;

interface IReceiveInfo {

    void cancelReceive();
    void canSendNow();

    void registerCallback(IReceiveCallback cb);
    void unregisterCallback(IReceiveCallback cb);

    void disConnect();
    void beginRestore(IBackupManagerServiceCallback cb);
}
