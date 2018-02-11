/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.operator.wifi;

import java.util.EventObject;

public class APCreateDoneEvent extends EventObject{

    boolean bSuccess;
    String ssid;
    int module;

    public APCreateDoneEvent(Object source, boolean bSuccess, String ssid, int module) {
        super(source);
        this.bSuccess = bSuccess;
        this.ssid = ssid;
        this.module = module;
    }

    public boolean isSuccess() {
        return this.bSuccess;
    }

    public String getSSID() {
        return this.ssid;
    }

    public int getModule(){
        return this.module;
    }
}
