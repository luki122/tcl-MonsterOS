/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.systemApp;

import android.os.Environment;

import java.io.DataOutputStream;
import java.util.ArrayList;

import cn.tcl.transfer.ICallback;
import cn.tcl.transfer.util.FilePathUtils;

public class OtherSysApp extends SysBaseApp {

    public OtherSysApp(final String name) {
        super(name);
    }

    public OtherSysApp(DataOutputStream outputStream, ICallback callback, final String name) {
        super(outputStream, callback, name);
    }

}
