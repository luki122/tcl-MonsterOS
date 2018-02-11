/*Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.backupmanager;

oneway interface IBackupManagerServiceCallback {
    void onStart();
    void onComplete();
    void onUpdate(String info);
    void onProgress(int progress);
    void onError(String error);
}
