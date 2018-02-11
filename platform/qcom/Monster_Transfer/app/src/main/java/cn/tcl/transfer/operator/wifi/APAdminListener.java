/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.operator.wifi;

import java.util.EventListener;

public interface APAdminListener extends EventListener{
    public void onAPCreateDone(APCreateDoneEvent evt);
    public void onAPCreating(APCreatingEvent evt);
    public void onAPSSIDExist(APSSIDExistEvent evt);
}
