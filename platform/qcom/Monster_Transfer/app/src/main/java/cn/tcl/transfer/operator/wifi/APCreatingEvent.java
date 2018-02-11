/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.operator.wifi;

import java.util.EventObject;

public class APCreatingEvent extends EventObject{
    Object source;
    public APCreatingEvent(Object source) {
        super(source);
    }
}
