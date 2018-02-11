/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.send;
import cn.tcl.transfer.ICallback;

interface ISendInfo {

    void cancelSend();

    void connect();
    void disConnect();

    int getSendStatus();
    long getSentSize();
    String getSendingAppName();

    void sendData();
    void sendDataInfo(String info);


    void registerCallback(ICallback cb);
    void unregisterCallback(ICallback cb);
}
