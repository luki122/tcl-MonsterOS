/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.operator.wifi;

import java.util.EventObject;

public class WIFIConnectDoneEvent extends EventObject{
    Object source;
    boolean bSuccess;
    String ssid;

    public WIFIConnectDoneEvent(Object source, boolean bSuccess, String ssid) {
        super(source);
        this.bSuccess = bSuccess;
        this.ssid = ssid;
    }

    public boolean isSuccess() {
        return this.bSuccess;
    }

    public String getSSID() {
        return this.ssid;
    }
}
