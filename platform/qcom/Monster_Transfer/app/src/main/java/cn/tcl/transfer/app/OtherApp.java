/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.app;

import java.io.DataOutputStream;
import java.util.ArrayList;

import cn.tcl.transfer.ICallback;

public class OtherApp extends BaseApp {

    public OtherApp(final String pkgName) {
        super(pkgName);
    }

    public OtherApp(DataOutputStream outputStream, ICallback callback, final String pkgName) {
        super(outputStream, callback, pkgName);
    }

    public ArrayList<String> getCreateSdcardData() {
        return null;
    }
}
