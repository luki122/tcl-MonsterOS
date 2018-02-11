/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.operator.wifi;

import java.util.EventListener;

public interface WIFIAdminListener extends EventListener{
    public void onWIFIConnectDone(WIFIConnectDoneEvent evt);
    public void onWIFIConnecting(WIFIConnectingEvent evt);
}
