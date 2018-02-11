/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.entity;

import cn.tcl.transfer.util.Utils;

public abstract class CommEntity {
    private static final String TAG = "CommEntity";
    public static final int PORT = 36363;
    public static final int FILEPORT = 37384;
    public static final String SERVER_IP = Utils.IP;
    public static final int ACCEPT_RESPONSE = 37386;
}
