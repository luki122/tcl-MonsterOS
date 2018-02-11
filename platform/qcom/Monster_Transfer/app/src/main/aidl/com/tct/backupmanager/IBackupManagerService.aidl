/*Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.backupmanager;
import com.tct.backupmanager.IBackupManagerServiceCallback;

interface IBackupManagerService {
    void start();
    boolean beginBack(in String[] files, String dst, int type);
    boolean beginRestore(in String[] files, String dst, int type);
    void pause();
    void registerCallback(IBackupManagerServiceCallback cb);
    void unregisterCallback(IBackupManagerServiceCallback cb);
}




