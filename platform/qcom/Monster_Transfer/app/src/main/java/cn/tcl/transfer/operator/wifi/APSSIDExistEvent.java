/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.operator.wifi;

import java.util.EventObject;

public class APSSIDExistEvent extends EventObject{
    Object source;
    String ssid;
    public APSSIDExistEvent(Object source, String ssid) {
        super(source);
        this.ssid = ssid;
    }

    public String getSSID() {
        return this.ssid;
    }
}
