// IMyAidlInterface.aidl
package com.monster.cloud;
import src.main.aidl.com.monster.cloud.ICallBack;
//import com.monster.cloud.ICallBack;

// Declare any non-default types here with import statements

interface ProgressConnection {
    void registerCallback(ICallBack callback);
    void unregisterCallback(ICallBack callback);
    void startSynchronize(int type, boolean isOneStep);
    void stopSynchronize();
    void notifyServiceOnStop();
    void notifyServiceOnStart();
}
