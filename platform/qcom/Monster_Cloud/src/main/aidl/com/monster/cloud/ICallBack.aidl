// ICallback.aidl
package com.monster.cloud;
// Declare any non-default types here with import statements

interface ICallBack {
    void updateProgress(int taskType, int progress);
    void notifyCurrentSyncFinished(int currentType);
    void notifyAllSyncFinished();
}
